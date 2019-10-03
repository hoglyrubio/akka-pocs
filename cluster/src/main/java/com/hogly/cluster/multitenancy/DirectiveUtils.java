package com.hogly.cluster.multitenancy;

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCode;

public class DirectiveUtils {

  public static HttpResponse TextPlainHttpResponse(StatusCode statusCode, String body) {
    return HttpResponse.create()
      .withStatus(statusCode)
      .withEntity(HttpEntities.create(ContentTypes.TEXT_PLAIN_UTF8, body));
  }

  public static <T> HttpResponse JsonHttpResponse(StatusCode statusCode, T body) {
    return HttpResponse.create()
      .withStatus(statusCode)
      .withEntity(HttpEntities.create(ContentTypes.APPLICATION_JSON, MarshallingUtils.toJson(body)));
  }

}
