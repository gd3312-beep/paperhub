package com.paperhub.service;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PdfService {

  public Path merge(Path[] inputs, Path output) throws IOException {
    if (inputs.length < 2) {
      throw new IllegalArgumentException("Choose at least two PDFs to merge.");
    }
    PDFMergerUtility util = new PDFMergerUtility();
    for (Path p : inputs) {
      if (!Files.isRegularFile(p)) {
        throw new IllegalArgumentException("One of the selected PDFs was not found.");
      }
      util.addSource(p.toFile());
    }
    util.setDestinationFileName(output.toString());
    util.mergeDocuments(null);
    return output;
  }

  public Path compress(Path input, Path output) throws IOException {
    if (!Files.isRegularFile(input)) {
      throw new IllegalArgumentException("PDF was not found.");
    }
    try (PDDocument doc = PDDocument.load(input.toFile())) {
      doc.setAllSecurityToBeRemoved(true);
      doc.save(new FileOutputStream(output.toFile()));
    }
    return output;
  }

  public Path toImagesZip(Path inputPdf, Path outputsDir, String format) throws IOException {
    format = format.toLowerCase();
    if (!format.equals("png") && !format.equals("jpeg")) {
      throw new IllegalArgumentException("Unsupported format: " + format);
    }
    if (!Files.isRegularFile(inputPdf)) {
      throw new IllegalArgumentException("PDF was not found.");
    }
    String baseName = inputPdf.getFileName().toString().replaceAll("\\.pdf$", "");
    String zipName = baseName + "_images_" + format + ".zip";
    Path zipPath = unique(outputsDir.resolve(zipName));
    float dpi = 144f;

    try (PDDocument doc = PDDocument.load(inputPdf.toFile());
         ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
      PDFRenderer renderer = new PDFRenderer(doc);
      for (int i = 0; i < doc.getNumberOfPages(); i++) {
        BufferedImage bim = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
        String imgName = String.format("%s_page_%03d.%s", baseName, i+1, format);
        zos.putNextEntry(new ZipEntry(imgName));
        ImageIO.write(bim, format, zos);
        zos.closeEntry();
      }
    }
    return zipPath;
  }

  private Path unique(Path desired) throws IOException {
    if (!Files.exists(desired)) return desired;
    String name = desired.getFileName().toString();
    int dot = name.lastIndexOf('.');
    String base = dot > 0 ? name.substring(0, dot) : name;
    String ext = dot > 0 ? name.substring(dot) : "";
    int i = 1;
    Path candidate;
    do {
      candidate = desired.getParent().resolve(base + " (" + i++ + ")" + ext);
    } while (Files.exists(candidate));
    return candidate;
  }
}
