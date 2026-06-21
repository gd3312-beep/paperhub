package com.paperhub.dto;

import jakarta.validation.constraints.NotBlank;

public class SingleFileRequest {
  @NotBlank
  private String filename;

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }
}
