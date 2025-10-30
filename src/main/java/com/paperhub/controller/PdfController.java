package com.paperhub.controller;

import com.paperhub.dto.ApiResponse;
import com.paperhub.service.FileStorageService;
import com.paperhub.service.PdfService;
import jakarta.validation.constraints.NotEmpty;
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
  public ResponseEntity<ApiResponse<List<String>>> upload(@RequestPart("files") @NotEmpty MultipartFile[] files){
    try{
      List<String> names = new ArrayList<>();
      for (MultipartFile f : files) names.add(fs.saveUpload(f));
      return ResponseEntity.ok(ApiResponse.ok("Uploaded", names));
    }catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
  }

  @PostMapping("/merge")
  public ResponseEntity<ApiResponse<Map<String,String>>> merge(@RequestBody Map<String,Object> req){
    try{
      List<?> names = (List<?>)req.get("filenames");
      Path[] inputs = names.stream().map(Object::toString).map(fs::getUploadPath).toArray(Path[]::new);
      Path out = fs.getOutputPath("merged.pdf");
      pdf.merge(inputs, out);
      return ResponseEntity.ok(ApiResponse.ok("Merged", Map.of("filename", out.getFileName().toString())));
    }catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
  }

  @PostMapping("/compress")
  public ResponseEntity<ApiResponse<Map<String,String>>> compress(@RequestBody Map<String,Object> req){
    try{
      String filename = req.get("filename").toString();
      Path in = fs.getUploadPath(filename);
      Path out = fs.getOutputPath(filename.replace(".pdf","_compressed.pdf"));
      pdf.compress(in, out);
      return ResponseEntity.ok(ApiResponse.ok("Compressed", Map.of("filename", out.getFileName().toString())));
    }catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
  }

  @PostMapping("/to-image")
  public ResponseEntity<ApiResponse<Map<String,String>>> toImage(@RequestBody Map<String,Object> req){
    try{
      String filename = req.get("filename").toString();
      String format = req.getOrDefault("format", "png").toString();
      Path in = fs.getUploadPath(filename);
      Path zip = pdf.toImagesZip(in, fs.getOutputsDir(), format);
      return ResponseEntity.ok(ApiResponse.ok("Rendered", Map.of("filename", zip.getFileName().toString())));
    }catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
  }
}
