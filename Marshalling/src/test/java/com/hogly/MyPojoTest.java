package com.hogly;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class MyPojoTest {

  private final static ObjectMapper MAPPER = new ObjectMapper();

  @Test
  public void test1() throws IOException {
    MyPojo myPojo = new MyPojoBuilder()
      .setDoubleValue(123D)
      .setIntegerValue(456)
      .setLongValue(789L)
      .setStringValue("My object")
      .build();

    String json = MAPPER.writeValueAsString(myPojo);

    MyPojo newPojo = MAPPER.readValue(json, MyPojo.class);

    Assert.assertEquals(newPojo, myPojo);
  }


}
