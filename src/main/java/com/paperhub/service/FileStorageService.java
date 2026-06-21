package com.paperhub.service;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileStorageService {
  private final Path uploadsDir;
  private final Path outputsDir;

  public FileStorageService(@Value("${paperhub.storage.uploads-dir}") String uploads,
                            @Value("${paperhub.storage.outputs-dir}") String outputs) throws IOException {
    this.uploadsDir = Paths.get(uploads).toAbsolutePath().normalize();
    this.outputsDir = Paths.get(outputs).toAbsolutePath().normalize();
    Files.createDirectories(this.uploadsDir);
    Files.createDirectories(this.outputsDir);
  }

  public String saveUpload(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Choose a PDF before uploading.");
    }

    String originalName = file.getOriginalFilename();
    if (originalName == null || originalName.isBlank()) {
      throw new IllegalArgumentException("Uploaded file needs a name.");
    }

    String cleanName = Path.of(originalName).getFileName().toString();
    String ext = FilenameUtils.getExtension(cleanName).toLowerCase();
    if (!"pdf".equals(ext)) {
      throw new IllegalArgumentException("Only PDF files are supported.");
    }

    String base = FilenameUtils.removeExtension(cleanName).replaceAll("[^A-Za-z0-9._-]", "_");
    if (base.isBlank()) {
      base = "document";
    }

    Path dest = unique(uploadsDir.resolve(base + ".pdf"));
    Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

    try (PDDocument ignored = PDDocument.load(dest.toFile())) {
      return dest.getFileName().toString();
    } catch (IOException e) {
      Files.deleteIfExists(dest);
      throw new IllegalArgumentException("That file does not look like a readable PDF.");
    }
  }

  public Path getUploadPath(String filename){ return safeResolve(uploadsDir, filename); }
  public Path getOutputPath(String filename){ return safeResolve(outputsDir, filename); }
  public Path getOutputsDir(){ return outputsDir; }
  public Resource asResource(Path p){ return new FileSystemResource(p.toFile()); }
  public Path createOutputPath(String filename) throws IOException { return unique(getOutputPath(filename)); }

  public List<String> listOutputs() throws IOException {
    try (var s = Files.list(outputsDir)) {
      return s.filter(Files::isRegularFile).map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList());
    }
  }

  private Path safeResolve(Path baseDir, String filename) {
    if (filename == null || filename.isBlank()) {
      throw new IllegalArgumentException("File name is required.");
    }
    String cleanName = Path.of(filename).getFileName().toString();
    if (!cleanName.equals(filename) || cleanName.contains("..")) {
      throw new IllegalArgumentException("Invalid file name.");
    }
    Path resolved = baseDir.resolve(cleanName).normalize();
    if (!resolved.startsWith(baseDir)) {
      throw new IllegalArgumentException("Invalid file name.");
    }
    return resolved;
  }

  private Path unique(Path desired) throws IOException {
    if (!Files.exists(desired)) return desired;
    String base = FilenameUtils.removeExtension(desired.getFileName().toString());
    String ext = FilenameUtils.getExtension(desired.getFileName().toString());
    if (!ext.isEmpty()) ext = "." + ext;
    int i = 1; Path candidate;
    do { candidate = desired.getParent().resolve(base + " (" + i++ + ")" + ext); }
    while (Files.exists(candidate));
    return candidate;
  }
}
