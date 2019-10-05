package com.hogly.cluster.multitenancy.admin;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.RejectionHandler;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.ValidationRejection;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.hogly.cluster.multitenancy.DirectiveUtils;
import com.hogly.cluster.multitenancy.MarshallingUtils;
import com.typesafe.config.Config;

import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.PathMatchers.segment;

public class AdminController extends AllDirectives {

  private final ActorSystem system;
  private final Http http;
  private final ConnectHttp connect;
  private final AdminService adminService;
  private final ActorMaterializer materializer;
  private final Unmarshaller<HttpEntity, CreateApplicationInstance> createAdminUnMarshaller;

  public AdminController(ActorSystem system, Config config, AdminService adminService) {
    this.system = system;
    this.http = Http.get(system);
    this.materializer = ActorMaterializer.create(system);
    this.connect = ConnectHttp.toHost(config.getString("host"), config.getInt("port"));
    this.adminService = adminService;
    this.createAdminUnMarshaller = MarshallingUtils.jsonUnMarshaller(CreateApplicationInstance.class);
  }

  public CompletionStage<ServerBinding> start() {
    Flow<HttpRequest, HttpResponse, NotUsed> flow = createRoute().flow(system, materializer);
    return http.bindAndHandle(flow, connect, materializer);
  }

  private Route createRoute() {

    Route applicationRoute = route(pathPrefix(segment("api").slash("admin"),
      () -> route(
        post(() -> entity(createAdminUnMarshaller, body -> createClient(body))),
        delete(() -> path(segment(), clientId -> deleteClient(clientId))),
        get(() -> path(segment(), clientId -> getClient(clientId))),
        get(() -> getClients())
      )
    ));

    return handleRejections(rejectionHandler(),
      () -> handleExceptions(exceptionHandler(),
        () -> applicationRoute
      )
    );
  }

  private RejectionHandler rejectionHandler() {
    return RejectionHandler.newBuilder()
      .handle(ValidationRejection.class, validationRejection -> complete(StatusCodes.BAD_REQUEST, "Rejection. An Error has occurred: " + validationRejection.message()))
      .build();
  }

  private ExceptionHandler exceptionHandler() {
    return ExceptionHandler.newBuilder()
      .matchAny(t -> complete(StatusCodes.BAD_REQUEST, "Exception. An Error has occurred: " + t.getMessage()))
      .build();
  }

  private Route createClient(CreateApplicationInstance command) {
    CompletionStage<HttpResponse> response = adminService.createClient(command)
      .thenApply(serverBinding -> DirectiveUtils.TextPlainHttpResponse(StatusCodes.CREATED, command.id()));
    return completeWithFuture(response);
  }

  private Route deleteClient(String clientId) {
    CompletionStage<HttpResponse> response = adminService.deleteClient(clientId)
      .thenApply(terminated -> DirectiveUtils.TextPlainHttpResponse(StatusCodes.OK, clientId));
    return completeWithFuture(response);
  }

  private Route getClients() {
    CompletionStage<HttpResponse> response = adminService.getClients()
      .thenApply(clientDescriptors -> DirectiveUtils.JsonHttpResponse(StatusCodes.OK, clientDescriptors));
    return completeWithFuture(response);
  }

  private Route getClient(String clientId) {
    CompletionStage<HttpResponse> response = adminService.getClient(clientId)
      .thenApply(clientDescriptor -> DirectiveUtils.JsonHttpResponse(StatusCodes.OK, clientDescriptor));
    return completeWithFuture(response);
  }

}
