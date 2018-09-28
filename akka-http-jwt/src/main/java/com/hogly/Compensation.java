package com.hogly;

public class Compensation {

  private final Integer id;

  public Compensation(Integer id) {
    this.id = id;
  }

  public String toJson() {
    return "{\"id\": \""+ id +"\"}";
  }
}
