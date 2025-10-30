package com.paperhub.service;

import org.apache.commons.io.FilenameUtils;
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
    String base = FilenameUtils.removeExtension(Path.of(file.getOriginalFilename()).getFileName().toString());
    String ext = FilenameUtils.getExtension(file.getOriginalFilename());
    if (!ext.isEmpty()) ext = "." + ext;
    Path dest = unique(uploadsDir.resolve(base + ext));
    Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
    return dest.getFileName().toString();
  }

  public Path getUploadPath(String filename){ return uploadsDir.resolve(filename); }
  public Path getOutputPath(String filename){ return outputsDir.resolve(filename); }
  public Path getOutputsDir(){ return outputsDir; }
  public Resource asResource(Path p){ return new FileSystemResource(p.toFile()); }

  public List<String> listOutputs() throws IOException {
    try (var s = Files.list(outputsDir)) {
      return s.filter(Files::isRegularFile).map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList());
    }
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
