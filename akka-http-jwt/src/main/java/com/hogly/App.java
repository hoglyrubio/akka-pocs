package com.hogly;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import java.util.Optional;
import java.util.function.Function;

public class App extends AllDirectives {

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("akka-http-jwt");
    App app = new App(system);
  }

  public App(ActorSystem system) {
    system.log().info("Starting application");
    Http http = Http.get(system);
    ActorMaterializer materializer = ActorMaterializer.create(system);
    Flow<HttpRequest, HttpResponse, NotUsed> flow = myRoutes().flow(system, materializer);
    ConnectHttp connectHttp = ConnectHttp.toHost("localhost", 8080);
    http.bindAndHandle(flow, connectHttp, materializer);
  }

  public Route myRoutes() {
    return route(basicAuthenticationRoute(), oAuth2JwtRoute());
  }

  private Route basicAuthenticationRoute() {
    Function<Optional<ProvidedCredentials>, Optional<String>> authenticator = credentials -> credentials
      .filter(providedCredentials -> providedCredentials.verify("p4ssw0rd"))
      .map(ProvidedCredentials::identifier);
    return path("basicSecured", () -> authenticateBasic("secure site", authenticator, user -> complete("The user is: " + user)));
  }

  private Route oAuth2JwtRoute() {
    Function<Optional<ProvidedCredentials>, Optional<AuthorizationContext>> authenticator = credentials -> credentials
      .flatMap(providedCredentials -> AuthorizationContext.buildFromJwt(providedCredentials.identifier()));
    return path("oauth2Secured", () -> authenticateOAuth2("secure site", authenticator, authorizationContext -> complete(authorizationContext.toString())));
  }
}
