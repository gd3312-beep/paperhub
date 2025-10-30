package com.paperhub.controller;

import com.paperhub.dto.ApiResponse;
import com.paperhub.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Path;
import java.util.List;

@RestController @RequestMapping("/api")
public class DownloadController {
  private final FileStorageService fs;
  public DownloadController(FileStorageService fs){ this.fs = fs; }

  @GetMapping("/outputs")
  public ResponseEntity<ApiResponse<List<String>>> listOutputs(){
    try{ return ResponseEntity.ok(ApiResponse.ok("OK", fs.listOutputs())); }
    catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
  }

  @GetMapping("/download/{filename}")
  public ResponseEntity<Resource> download(@PathVariable String filename){
    Path path = fs.getOutputPath(filename);
    Resource res = fs.asResource(path);
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + res.getFilename() + "\"")
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(res);
  }
}
