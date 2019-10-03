package com.hogly.cluster.multitenancy;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.io.IOException;

public class MarshallingUtils {

  private static final ObjectMapper MAPPER = (new ObjectMapper()).registerModules(new Module[]{new Jdk8Module(), new JavaTimeModule(), new ParameterNamesModule(JsonCreator.Mode.PROPERTIES)}).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  public static ObjectMapper mapper() {
    return MAPPER;
  }

  public static <T> String toJson(T object) {
    try {
      return MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error serializing to JSON", e);
    }
  }

  public static <T> T fromJson(String json, Class<T> clazz) {
    try {
      return MAPPER.readValue(json, clazz);
    } catch (IOException e) {
      throw new RuntimeException("Error de-serializing", e);
    }
  }

  public static <T> T fromJson(String json, TypeReference<T> typeReference) {
    try {
      return MAPPER.readValue(json, typeReference);
    } catch (IOException e) {
      throw new RuntimeException("Error de-serializing", e);
    }
  }

  public static <T> Unmarshaller<HttpEntity, T> jsonUnMarshaller(Class<T> expectedType) {
    return Jackson.unmarshaller(expectedType);
    //return Unmarshaller.forMediaType(MediaTypes.APPLICATION_JSON, Unmarshaller.entityToString())
    //  .thenApply(s ->  fromJson(s, expectedType));
  }

  public static Marshaller<Object, RequestEntity> jsonMarshaller() {
    return Jackson.marshaller(MAPPER);
    //return Marshaller.withFixedContentType(ContentTypes.APPLICATION_JSON, (Object value) -> HttpEntities.create(ContentTypes.APPLICATION_JSON, toJson(value)));
  }


}
