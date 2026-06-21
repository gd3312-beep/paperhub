package com.paperhub.controller;

import com.paperhub.dto.ApiResponse;
import com.paperhub.dto.SingleFileRequest;
import com.paperhub.service.AiService;
import com.paperhub.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Path;
import java.util.Map;

@RestController @RequestMapping("/api/ai")
public class AiController {
  private final FileStorageService fs; private final AiService ai;
  public AiController(FileStorageService fs, AiService ai){ this.fs = fs; this.ai = ai; }

  @PostMapping("/summarize")
  public ResponseEntity<ApiResponse<Map<String,String>>> summarize(@Valid @RequestBody SingleFileRequest req){
    try{
      Path in = fs.getUploadPath(req.getFilename());
      String result = ai.summarizePdf(in);
      return ResponseEntity.ok(ApiResponse.ok("OK", Map.of("result", result)));
    }catch(IllegalArgumentException e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
    catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail("AI summary failed.")); }
  }

  @PostMapping("/quiz")
  public ResponseEntity<ApiResponse<Map<String,String>>> quiz(@Valid @RequestBody SingleFileRequest req){
    try{
      Path in = fs.getUploadPath(req.getFilename());
      String result = ai.quizPdf(in);
      return ResponseEntity.ok(ApiResponse.ok("OK", Map.of("result", result)));
    }catch(IllegalArgumentException e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
    catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail("AI quiz generation failed.")); }
  }

  @PostMapping("/mindmap")
  public ResponseEntity<ApiResponse<Map<String,String>>> mindmap(@Valid @RequestBody SingleFileRequest req){
    try{
      Path in = fs.getUploadPath(req.getFilename());
      String result = ai.mindmapPdf(in);
      return ResponseEntity.ok(ApiResponse.ok("OK", Map.of("result", result)));
    }catch(IllegalArgumentException e){ return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage())); }
    catch(Exception e){ return ResponseEntity.badRequest().body(ApiResponse.fail("AI mind map generation failed.")); }
  }
}
