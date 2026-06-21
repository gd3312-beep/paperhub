package com.paperhub.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class MergeRequest {
  @NotEmpty
  private List<String> filenames;

  public List<String> getFilenames() {
    return filenames;
  }

  public void setFilenames(List<String> filenames) {
    this.filenames = filenames;
  }
}
