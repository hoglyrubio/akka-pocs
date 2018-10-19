package com.hogly.jwt;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Authorization;
import akka.http.javadsl.model.headers.BasicHttpCredentials;
import akka.http.javadsl.model.headers.HttpCredentials;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import akka.http.javadsl.testkit.TestRouteResult;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AppTest extends JUnitRouteTest {

  private static ActorSystem system;

  @BeforeClass
  public static void beforeClass() {
    system = ActorSystem.create("testing");
  }

  @AfterClass
  public static void afterClass() {
    TestKit.shutdownActorSystem(system);
    system = null;
  }

  @Test
  public void shouldFailValidateBasicAuthentication() {
    new TestKit(system) {
      {
        App app = new App(system);
        TestRoute route = testRoute(app.myRoutes());

        HttpRequest request = HttpRequest.GET("/basicSecured");

        route.run(request)
          .assertStatusCode(StatusCodes.UNAUTHORIZED)
          .assertEntity("The resource requires authentication, which was not supplied with the request")
          .assertHeaderExists("WWW-Authenticate", "Basic realm=\"secure site\",charset=UTF-8");
      }
    };
  }

  @Test
  public void shouldValidateBasicAuthentication() {
    new TestKit(system) {
      {
        App app = new App(system);
        TestRoute route = testRoute(app.myRoutes());

        HttpCredentials validCredentials = BasicHttpCredentials
          .createBasicHttpCredentials("hogly", "p4ssw0rd");

        HttpRequest request = HttpRequest.GET("/basicSecured")
          .addCredentials(validCredentials);

        route.run(request)
          .assertStatusCode(StatusCodes.OK)
          .assertEntity("The user is: hogly");
      }
    };
  }

  @Test
  public void shouldFailValidateBasicAuthenticationBecauseWrongCredentials() {
    new TestKit(system) {
      {
        App app = new App(system);
        TestRoute route = testRoute(app.myRoutes());

        HttpCredentials invalidCredentials = BasicHttpCredentials
          .createBasicHttpCredentials("hogly", "wrong-password");

        HttpRequest request = HttpRequest.GET("/basicSecured")
          .addCredentials(invalidCredentials);

        route.run(request)
          .assertStatusCode(StatusCodes.UNAUTHORIZED)
          .assertEntity("The supplied authentication is invalid")
          .assertHeaderExists("WWW-Authenticate", "Basic realm=\"secure site\",charset=UTF-8");
      }
    };
  }

  @Test
  public void shouldFailValidateOAuth2Authentication() {
    new TestKit(system) {
      {
        App app = new App(system);
        TestRoute route = testRoute(app.myRoutes());

        HttpRequest request = HttpRequest.GET("/oauth2Secured");

        TestRouteResult result = route.run(request);
          result.assertStatusCode(StatusCodes.UNAUTHORIZED)
          .assertEntity("The resource requires authentication, which was not supplied with the request")
          .assertHeaderExists("WWW-Authenticate", "Bearer realm=\"secure site\"");
      }
    };
  }

  @Test
  public void shouldValidateOAuth2Authentication() {
    new TestKit(system) {
      {
        App app = new App(system);
        TestRoute route = testRoute(app.myRoutes());

        HttpCredentials validCredentials = Authorization
          .oauth2("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJocnViaW90IiwiYXVkIjoibHNmLnh0aXZhY2xvdWQuY29tIiwicGVybWlzc2lvbnMiOlsiY3JlYXRlIiwidXBkYXRlIiwiZGVsZXRlIl0sInJvbGVzIjpbIkNvbXAgQWRtaW4iLCJBZHZpc29yIl0sImlzcyI6Ilh0aXZhIEZpbmFuY2lhbCBTeXN0ZW1zIn0.TftTRlCnT5OZQBuZ_rkZQuU8SP4e-8UcXQXjt9DeuYg")
          .credentials();

        HttpRequest request = HttpRequest.GET("/oauth2Secured")
          .addCredentials(validCredentials);

        route.run(request)
          .assertStatusCode(StatusCodes.OK)
          .assertEntity("AuthorizationContext { instanceAlias='company.url.com', userId='hrubiot', roles=[Comp Admin, Advisor], permissions=[create, update, delete]}");
      }
    };
  }

  @Test
  public void shouldFailValidateOAuth2AuthenticationBecauseWrongCredentials() {
    new TestKit(system) {
      {
        App app = new App(system);
        TestRoute route = testRoute(app.myRoutes());

        HttpCredentials invalidCredentials = Authorization
          .oauth2("Vearer yo-soy-el-token-malito")
          .credentials();

        HttpRequest request = HttpRequest.GET("/oauth2Secured")
          .addCredentials(invalidCredentials);

        route.run(request)
          .assertStatusCode(StatusCodes.UNAUTHORIZED)
          .assertEntity("The supplied authentication is invalid")
          .assertHeaderExists("WWW-Authenticate", "Bearer realm=\"secure site\"");
      }
    };
  }

}
