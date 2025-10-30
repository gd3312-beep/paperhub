package com.paperhub.controller;

import com.paperhub.dto.ApiResponse;
import com.paperhub.service.AiService;
import com.paperhub.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Path;
import java.util.Map;

@RestController @RequestMapping("/api/ai")
public class AiController {
  private final FileStorageService fs; private final AiService ai;
  public AiController(FileStorageService fs, AiService ai){ this.fs = fs; this.ai = ai; }

  @PostMapping("/summarize")
  public ResponseEntity<ApiResponse<Map<String,String>>> summarize(@RequestBody Map<String,Object> req){
    try{
      String filename = req.get("filename").toString();
      Path in = fs.getUploadPath(filename);
      String result = ai.summarizePdf(in);
      return ResponseEntity.ok(ApiResponse.ok("OK", Map.of("result", result)));
    }catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
  }

  @PostMapping("/quiz")
  public ResponseEntity<ApiResponse<Map<String,String>>> quiz(@RequestBody Map<String,Object> req){
    try{
      String filename = req.get("filename").toString();
      Path in = fs.getUploadPath(filename);
      String result = ai.quizPdf(in);
      return ResponseEntity.ok(ApiResponse.ok("OK", Map.of("result", result)));
    }catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
  }

  @PostMapping("/mindmap")
  public ResponseEntity<ApiResponse<Map<String,String>>> mindmap(@RequestBody Map<String,Object> req){
    try{
      String filename = req.get("filename").toString();
      Path in = fs.getUploadPath(filename);
      String result = ai.mindmapPdf(in);
      return ResponseEntity.ok(ApiResponse.ok("OK", Map.of("result", result)));
    }catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
  }
}
