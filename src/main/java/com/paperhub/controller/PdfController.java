package com.paperhub.controller;

import com.paperhub.dto.ApiResponse;
import com.paperhub.dto.MergeRequest;
import com.paperhub.dto.SingleFileRequest;
import com.paperhub.dto.ToImageRequest;
import com.paperhub.service.FileStorageService;
import com.paperhub.service.PdfService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.util.*;

@RestController @RequestMapping("/api/pdf")
public class PdfController {
  private final FileStorageService fs; private final PdfService pdf;
  public PdfController(FileStorageService fs, PdfService pdf){ this.fs = fs; this.pdf = pdf; }

  @PostMapping(value="/upload", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<List<String>>> upload(@RequestPart("files") MultipartFile[] files){
    try{
      if (files == null || files.length == 0) {
        return ResponseEntity.badRequest().body(ApiResponse.fail("Choose at least one PDF."));
      }
      List<String> names = new ArrayList<>();
      for (MultipartFile f : files) names.add(fs.saveUpload(f));
      return ResponseEntity.ok(ApiResponse.ok("Uploaded", names));
    }catch(IllegalArgumentException e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
    catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail("Upload failed.")); }
  }

  @PostMapping("/merge")
  public ResponseEntity<ApiResponse<Map<String,String>>> merge(@Valid @RequestBody MergeRequest req){
    try{
      Path[] inputs = req.getFilenames().stream().map(fs::getUploadPath).toArray(Path[]::new);
      Path out = fs.createOutputPath("merged.pdf");
      pdf.merge(inputs, out);
      return ResponseEntity.ok(ApiResponse.ok("Merged", Map.of("filename", out.getFileName().toString())));
    }catch(IllegalArgumentException e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
    catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail("Merge failed.")); }
  }

  @PostMapping("/compress")
  public ResponseEntity<ApiResponse<Map<String,String>>> compress(@Valid @RequestBody SingleFileRequest req){
    try{
      Path in = fs.getUploadPath(req.getFilename());
      String outName = in.getFileName().toString().replaceFirst("(?i)\\.pdf$", "_compressed.pdf");
      Path out = fs.createOutputPath(outName);
      pdf.compress(in, out);
      return ResponseEntity.ok(ApiResponse.ok("Compressed", Map.of("filename", out.getFileName().toString())));
    }catch(IllegalArgumentException e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
    catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail("Compression failed.")); }
  }

  @PostMapping("/to-image")
  public ResponseEntity<ApiResponse<Map<String,String>>> toImage(@Valid @RequestBody ToImageRequest req){
    try{
      String format = Optional.ofNullable(req.getFormat()).orElse("png");
      Path in = fs.getUploadPath(req.getFilename());
      Path zip = pdf.toImagesZip(in, fs.getOutputsDir(), format);
      return ResponseEntity.ok(ApiResponse.ok("Rendered", Map.of("filename", zip.getFileName().toString())));
    }catch(IllegalArgumentException e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
    catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail("PDF conversion failed.")); }
  }
}
