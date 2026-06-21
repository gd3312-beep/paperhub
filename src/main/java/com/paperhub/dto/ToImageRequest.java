package com.paperhub.dto;

import jakarta.validation.constraints.NotBlank;

public class ToImageRequest {
  @NotBlank
  private String filename;
  private String format = "png";

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }
}
