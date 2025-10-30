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
    PDFMergerUtility util = new PDFMergerUtility();
    for (Path p : inputs) util.addSource(p.toFile());
    util.setDestinationFileName(output.toString());
    util.mergeDocuments(null);
    return output;
  }

  public Path compress(Path input, Path output) throws IOException {
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
    String baseName = inputPdf.getFileName().toString().replaceAll("\\.pdf$", "");
    String zipName = baseName + "_images_" + format + ".zip";
    Path zipPath = outputsDir.resolve(zipName);
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
}
