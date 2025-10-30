package com.paperhub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

@Service
public class AiService {
  private final String apiKey;
  private final String endpoint;
  private final String model;
  private final WebClient http;
  private final ObjectMapper om = new ObjectMapper();

  public AiService(@Value("${gemini.api-key:}") String apiKey,
                   @Value("${gemini.endpoint}") String endpoint,
                   @Value("${gemini.model}") String model){
    this.apiKey = apiKey;
    this.endpoint = endpoint;
    this.model = model;
    this.http = WebClient.builder().build();
  }

  public String summarizePdf(Path pdfPath) throws Exception {
    String text = extractStub(pdfPath);
    return generate("Summarize this document in crisp bullet points:\n\n" + text);
  }
  public String quizPdf(Path pdfPath) throws Exception {
    String text = extractStub(pdfPath);
    return generate("Create 5 quiz questions with short answers from this content:\n\n" + text);
  }
  public String mindmapPdf(Path pdfPath) throws Exception {
    String text = extractStub(pdfPath);
    return generate("Make a hierarchical outline (mind map style) of the key concepts:\n\n" + text);
  }


private String extractStub(Path pdf) throws Exception {
  try (PDDocument document = PDDocument.load(pdf.toFile())) {
    PDFTextStripper stripper = new PDFTextStripper();
    String text = stripper.getText(document).trim();
    // Keep it short enough for Gemini input limits
    return text.length() > 8000 ? text.substring(0, 8000) : text;
  }
}


  private String generate(String prompt) throws Exception {
    if (apiKey == null || apiKey.isBlank()) {
      return "[Gemini not configured] " + (prompt.length()>180?prompt.substring(0,180)+"...":prompt);
    }
    String url = String.format("%s/%s:generateContent?key=%s", endpoint, model, apiKey);
    String body = new ObjectMapper().writeValueAsString(Map.of(
      "contents", new Object[]{ Map.of("parts", new Object[]{ Map.of("text", prompt) }) }
    ));
    String resp = http.post().uri(url)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(body)
      .retrieve().bodyToMono(String.class).block();
    JsonNode root = om.readTree(resp);
    JsonNode cand = root.path("candidates").isArray() && root.path("candidates").size()>0 ? root.path("candidates").get(0) : null;
    String text = (cand != null) ? cand.path("content").path("parts").get(0).path("text").asText("") : "";
    if (text.isEmpty()) return "[Empty response from Gemini]";
    return text;
  }
}
