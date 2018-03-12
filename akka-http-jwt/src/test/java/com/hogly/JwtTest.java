package com.hogly;

import akka.Done;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JwtTest {

  private static ActorSystem system = ActorSystem.create("testing");
  private final static ObjectMapper MAPPER = new ObjectMapper();

  private final static List<Pair<String, String>> JWT_JSON_PAIR = generateJwt(1000);

  @Test
  public void testCreatingJwt() {
    JWT.create()
      .withClaim("iss", "Xtiva Financial Systems")
      .withClaim("sub", "hrubiot")
      .withArrayClaim("aud", new String[]{"lsf.xtivacloud.com"})
      .withArrayClaim("roles", new String[]{"Comp Admin"})
      .withArrayClaim("permissions", new String[]{"read:accounts", "create:accounts", "edit:accounts", "read:reps", "create:reps", "edit:reps", "read:orgs", "create:orgs", "edit:orgs", "read:payees", "create:payees", "edit:payees", "read:securities", "create:securities", "edit:securities", "read:adjustments", "create:adjustments", "edit:adjustments", "delete:adjustments", "read:payouts", "create:payouts", "edit:payouts", "export:payouts", "read:files", "create:files", "edit:files", "read:adjustmentTypes", "create:adjustmentTypes", "edit:adjustmentTypes", "delete:adjustmentTypes", "read:productContracts", "create:productContracts", "edit:productContracts", "delete:productContracts", "read:overrideContracts", "create:overrideContracts", "edit:overrideContracts", "delete:overrideContracts", "read:productRules", "create:productRules", "delete:productRules", "read:overrideRules", "create:overrideRules", "delete:overrideRules", "read:scheduleAdjustments", "create:scheduleAdjustments", "edit:scheduleAdjustments", "delete:scheduleAdjustments", "read:home", "read:conflicts", "read:compensations", "read:overrides", "read:transactions", "read:closedPayrecordsByPayee", "create:users", "read:orgAggregateReport", "read:aggregateReport", "read:grossRevenue", "list:accounts", "list:reps", "list:payees", "list:securities", "list:adjustments", "list:productContracts", "list:productRules", "list:compensations", "list:overrides", "list:orgs", "list:overrideContracts", "list:overrideRules", "list:transactions", "list:files", "list:payouts", "list:scheduleAdjustments", "list:adjustmentTypes", "list:conflicts", "read:transactionDetails"})
      .sign(getAlgorithm("secret"));
  }

  @Test
  public void testDecodingJwt() {
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJocnViaW90IiwiYXVkIjoibHNmLnh0aXZhY2xvdWQuY29tIiwicGVybWlzc2lvbnMiOlsicmVhZDphY2NvdW50cyIsImNyZWF0ZTphY2NvdW50cyIsImVkaXQ6YWNjb3VudHMiLCJyZWFkOnJlcHMiLCJjcmVhdGU6cmVwcyIsImVkaXQ6cmVwcyIsInJlYWQ6b3JncyIsImNyZWF0ZTpvcmdzIiwiZWRpdDpvcmdzIiwicmVhZDpwYXllZXMiLCJjcmVhdGU6cGF5ZWVzIiwiZWRpdDpwYXllZXMiLCJyZWFkOnNlY3VyaXRpZXMiLCJjcmVhdGU6c2VjdXJpdGllcyIsImVkaXQ6c2VjdXJpdGllcyIsInJlYWQ6YWRqdXN0bWVudHMiLCJjcmVhdGU6YWRqdXN0bWVudHMiLCJlZGl0OmFkanVzdG1lbnRzIiwiZGVsZXRlOmFkanVzdG1lbnRzIiwicmVhZDpwYXlvdXRzIiwiY3JlYXRlOnBheW91dHMiLCJlZGl0OnBheW91dHMiLCJleHBvcnQ6cGF5b3V0cyIsInJlYWQ6ZmlsZXMiLCJjcmVhdGU6ZmlsZXMiLCJlZGl0OmZpbGVzIiwicmVhZDphZGp1c3RtZW50VHlwZXMiLCJjcmVhdGU6YWRqdXN0bWVudFR5cGVzIiwiZWRpdDphZGp1c3RtZW50VHlwZXMiLCJkZWxldGU6YWRqdXN0bWVudFR5cGVzIiwicmVhZDpwcm9kdWN0Q29udHJhY3RzIiwiY3JlYXRlOnByb2R1Y3RDb250cmFjdHMiLCJlZGl0OnByb2R1Y3RDb250cmFjdHMiLCJkZWxldGU6cHJvZHVjdENvbnRyYWN0cyIsInJlYWQ6b3ZlcnJpZGVDb250cmFjdHMiLCJjcmVhdGU6b3ZlcnJpZGVDb250cmFjdHMiLCJlZGl0Om92ZXJyaWRlQ29udHJhY3RzIiwiZGVsZXRlOm92ZXJyaWRlQ29udHJhY3RzIiwicmVhZDpwcm9kdWN0UnVsZXMiLCJjcmVhdGU6cHJvZHVjdFJ1bGVzIiwiZGVsZXRlOnByb2R1Y3RSdWxlcyIsInJlYWQ6b3ZlcnJpZGVSdWxlcyIsImNyZWF0ZTpvdmVycmlkZVJ1bGVzIiwiZGVsZXRlOm92ZXJyaWRlUnVsZXMiLCJyZWFkOnNjaGVkdWxlQWRqdXN0bWVudHMiLCJjcmVhdGU6c2NoZWR1bGVBZGp1c3RtZW50cyIsImVkaXQ6c2NoZWR1bGVBZGp1c3RtZW50cyIsImRlbGV0ZTpzY2hlZHVsZUFkanVzdG1lbnRzIiwicmVhZDpob21lIiwicmVhZDpjb25mbGljdHMiLCJyZWFkOmNvbXBlbnNhdGlvbnMiLCJyZWFkOm92ZXJyaWRlcyIsInJlYWQ6dHJhbnNhY3Rpb25zIiwicmVhZDpjbG9zZWRQYXlyZWNvcmRzQnlQYXllZSIsImNyZWF0ZTp1c2VycyIsInJlYWQ6b3JnQWdncmVnYXRlUmVwb3J0IiwicmVhZDphZ2dyZWdhdGVSZXBvcnQiLCJyZWFkOmdyb3NzUmV2ZW51ZSIsImxpc3Q6YWNjb3VudHMiLCJsaXN0OnJlcHMiLCJsaXN0OnBheWVlcyIsImxpc3Q6c2VjdXJpdGllcyIsImxpc3Q6YWRqdXN0bWVudHMiLCJsaXN0OnByb2R1Y3RDb250cmFjdHMiLCJsaXN0OnByb2R1Y3RSdWxlcyIsImxpc3Q6Y29tcGVuc2F0aW9ucyIsImxpc3Q6b3ZlcnJpZGVzIiwibGlzdDpvcmdzIiwibGlzdDpvdmVycmlkZUNvbnRyYWN0cyIsImxpc3Q6b3ZlcnJpZGVSdWxlcyIsImxpc3Q6dHJhbnNhY3Rpb25zIiwibGlzdDpmaWxlcyIsImxpc3Q6cGF5b3V0cyIsImxpc3Q6c2NoZWR1bGVBZGp1c3RtZW50cyIsImxpc3Q6YWRqdXN0bWVudFR5cGVzIiwibGlzdDpjb25mbGljdHMiLCJyZWFkOnRyYW5zYWN0aW9uRGV0YWlscyJdLCJyb2xlcyI6WyJDb21wIEFkbWluIl0sImlzcyI6Ilh0aXZhIEZpbmFuY2lhbCBTeXN0ZW1zIn0.v3E65YdVU_Gc3Wc1QBrKUX1S9ffpCUUYSSdeiS4XFAo";
    JWT.decode(token);
  }

  @Test
  public void testDecodingJwtAndCreatinAnAuthorizationContext() {
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJocnViaW90IiwiYXVkIjoibHNmLnh0aXZhY2xvdWQuY29tIiwicGVybWlzc2lvbnMiOlsicmVhZDphY2NvdW50cyIsImNyZWF0ZTphY2NvdW50cyIsImVkaXQ6YWNjb3VudHMiLCJyZWFkOnJlcHMiLCJjcmVhdGU6cmVwcyIsImVkaXQ6cmVwcyIsInJlYWQ6b3JncyIsImNyZWF0ZTpvcmdzIiwiZWRpdDpvcmdzIiwicmVhZDpwYXllZXMiLCJjcmVhdGU6cGF5ZWVzIiwiZWRpdDpwYXllZXMiLCJyZWFkOnNlY3VyaXRpZXMiLCJjcmVhdGU6c2VjdXJpdGllcyIsImVkaXQ6c2VjdXJpdGllcyIsInJlYWQ6YWRqdXN0bWVudHMiLCJjcmVhdGU6YWRqdXN0bWVudHMiLCJlZGl0OmFkanVzdG1lbnRzIiwiZGVsZXRlOmFkanVzdG1lbnRzIiwicmVhZDpwYXlvdXRzIiwiY3JlYXRlOnBheW91dHMiLCJlZGl0OnBheW91dHMiLCJleHBvcnQ6cGF5b3V0cyIsInJlYWQ6ZmlsZXMiLCJjcmVhdGU6ZmlsZXMiLCJlZGl0OmZpbGVzIiwicmVhZDphZGp1c3RtZW50VHlwZXMiLCJjcmVhdGU6YWRqdXN0bWVudFR5cGVzIiwiZWRpdDphZGp1c3RtZW50VHlwZXMiLCJkZWxldGU6YWRqdXN0bWVudFR5cGVzIiwicmVhZDpwcm9kdWN0Q29udHJhY3RzIiwiY3JlYXRlOnByb2R1Y3RDb250cmFjdHMiLCJlZGl0OnByb2R1Y3RDb250cmFjdHMiLCJkZWxldGU6cHJvZHVjdENvbnRyYWN0cyIsInJlYWQ6b3ZlcnJpZGVDb250cmFjdHMiLCJjcmVhdGU6b3ZlcnJpZGVDb250cmFjdHMiLCJlZGl0Om92ZXJyaWRlQ29udHJhY3RzIiwiZGVsZXRlOm92ZXJyaWRlQ29udHJhY3RzIiwicmVhZDpwcm9kdWN0UnVsZXMiLCJjcmVhdGU6cHJvZHVjdFJ1bGVzIiwiZGVsZXRlOnByb2R1Y3RSdWxlcyIsInJlYWQ6b3ZlcnJpZGVSdWxlcyIsImNyZWF0ZTpvdmVycmlkZVJ1bGVzIiwiZGVsZXRlOm92ZXJyaWRlUnVsZXMiLCJyZWFkOnNjaGVkdWxlQWRqdXN0bWVudHMiLCJjcmVhdGU6c2NoZWR1bGVBZGp1c3RtZW50cyIsImVkaXQ6c2NoZWR1bGVBZGp1c3RtZW50cyIsImRlbGV0ZTpzY2hlZHVsZUFkanVzdG1lbnRzIiwicmVhZDpob21lIiwicmVhZDpjb25mbGljdHMiLCJyZWFkOmNvbXBlbnNhdGlvbnMiLCJyZWFkOm92ZXJyaWRlcyIsInJlYWQ6dHJhbnNhY3Rpb25zIiwicmVhZDpjbG9zZWRQYXlyZWNvcmRzQnlQYXllZSIsImNyZWF0ZTp1c2VycyIsInJlYWQ6b3JnQWdncmVnYXRlUmVwb3J0IiwicmVhZDphZ2dyZWdhdGVSZXBvcnQiLCJyZWFkOmdyb3NzUmV2ZW51ZSIsImxpc3Q6YWNjb3VudHMiLCJsaXN0OnJlcHMiLCJsaXN0OnBheWVlcyIsImxpc3Q6c2VjdXJpdGllcyIsImxpc3Q6YWRqdXN0bWVudHMiLCJsaXN0OnByb2R1Y3RDb250cmFjdHMiLCJsaXN0OnByb2R1Y3RSdWxlcyIsImxpc3Q6Y29tcGVuc2F0aW9ucyIsImxpc3Q6b3ZlcnJpZGVzIiwibGlzdDpvcmdzIiwibGlzdDpvdmVycmlkZUNvbnRyYWN0cyIsImxpc3Q6b3ZlcnJpZGVSdWxlcyIsImxpc3Q6dHJhbnNhY3Rpb25zIiwibGlzdDpmaWxlcyIsImxpc3Q6cGF5b3V0cyIsImxpc3Q6c2NoZWR1bGVBZGp1c3RtZW50cyIsImxpc3Q6YWRqdXN0bWVudFR5cGVzIiwibGlzdDpjb25mbGljdHMiLCJyZWFkOnRyYW5zYWN0aW9uRGV0YWlscyJdLCJyb2xlcyI6WyJDb21wIEFkbWluIl0sImlzcyI6Ilh0aXZhIEZpbmFuY2lhbCBTeXN0ZW1zIn0.v3E65YdVU_Gc3Wc1QBrKUX1S9ffpCUUYSSdeiS4XFAo";
    DecodedJWT jwt = JWT.decode(token);
    String instanceAlias = jwt.getAudience().get(0);
    String userId = jwt.getSubject();
    List<String> roles = jwt.getClaim("roles").asList(String.class);
    List<String> permissions = jwt.getClaim("permissions").asList(String.class);
    AuthorizationContext context = new AuthorizationContext(instanceAlias, userId, roles, permissions);
  }

  @Test
  public void testSerialization() throws IOException {
    String json = "{\"instanceAlias\":\"lsf.xtivacloud.com\",\"userId\":\"hrubiot\",\"roles\":[\"Comp Admin\"],\"permissions\":[\"read:accounts\",\"create:accounts\",\"edit:accounts\",\"read:reps\",\"create:reps\",\"edit:reps\",\"read:orgs\",\"create:orgs\",\"edit:orgs\",\"read:payees\",\"create:payees\",\"edit:payees\",\"read:securities\",\"create:securities\",\"edit:securities\",\"read:adjustments\",\"create:adjustments\",\"edit:adjustments\",\"delete:adjustments\",\"read:payouts\",\"create:payouts\",\"edit:payouts\",\"export:payouts\",\"read:files\",\"create:files\",\"edit:files\",\"read:adjustmentTypes\",\"create:adjustmentTypes\",\"edit:adjustmentTypes\",\"delete:adjustmentTypes\",\"read:productContracts\",\"create:productContracts\",\"edit:productContracts\",\"delete:productContracts\",\"read:overrideContracts\",\"create:overrideContracts\",\"edit:overrideContracts\",\"delete:overrideContracts\",\"read:productRules\",\"create:productRules\",\"delete:productRules\",\"read:overrideRules\",\"create:overrideRules\",\"delete:overrideRules\",\"read:scheduleAdjustments\",\"create:scheduleAdjustments\",\"edit:scheduleAdjustments\",\"delete:scheduleAdjustments\",\"read:home\",\"read:conflicts\",\"read:compensations\",\"read:overrides\",\"read:transactions\",\"read:closedPayrecordsByPayee\",\"create:users\",\"read:orgAggregateReport\",\"read:aggregateReport\",\"read:grossRevenue\",\"list:accounts\",\"list:reps\",\"list:payees\",\"list:securities\",\"list:adjustments\",\"list:productContracts\",\"list:productRules\",\"list:compensations\",\"list:overrides\",\"list:orgs\",\"list:overrideContracts\",\"list:overrideRules\",\"list:transactions\",\"list:files\",\"list:payouts\",\"list:scheduleAdjustments\",\"list:adjustmentTypes\",\"list:conflicts\",\"read:transactionDetails\"]}\n";
    AuthorizationContext context = MAPPER.readValue(json, AuthorizationContext.class);
  }

  @Test
  public void testJwt() {
    String token = JWT.create()
      .withClaim("iss", "Xtiva Financial Systems")
      .withClaim("sub", "hrubiot")
      .withArrayClaim("aud", new String[]{"lsf.xtivacloud.com"})
      .withArrayClaim("roles", new String[]{"Comp Admin"})
      .withArrayClaim("permissions", new String[]{"read:accounts", "create:accounts", "edit:accounts", "read:reps", "create:reps", "edit:reps", "read:orgs", "create:orgs", "edit:orgs", "read:payees", "create:payees", "edit:payees", "read:securities", "create:securities", "edit:securities", "read:adjustments", "create:adjustments", "edit:adjustments", "delete:adjustments", "read:payouts", "create:payouts", "edit:payouts", "export:payouts", "read:files", "create:files", "edit:files", "read:adjustmentTypes", "create:adjustmentTypes", "edit:adjustmentTypes", "delete:adjustmentTypes", "read:productContracts", "create:productContracts", "edit:productContracts", "delete:productContracts", "read:overrideContracts", "create:overrideContracts", "edit:overrideContracts", "delete:overrideContracts", "read:productRules", "create:productRules", "delete:productRules", "read:overrideRules", "create:overrideRules", "delete:overrideRules", "read:scheduleAdjustments", "create:scheduleAdjustments", "edit:scheduleAdjustments", "delete:scheduleAdjustments", "read:home", "read:conflicts", "read:compensations", "read:overrides", "read:transactions", "read:closedPayrecordsByPayee", "create:users", "read:orgAggregateReport", "read:aggregateReport", "read:grossRevenue", "list:accounts", "list:reps", "list:payees", "list:securities", "list:adjustments", "list:productContracts", "list:productRules", "list:compensations", "list:overrides", "list:orgs", "list:overrideContracts", "list:overrideRules", "list:transactions", "list:files", "list:payouts", "list:scheduleAdjustments", "list:adjustmentTypes", "list:conflicts", "read:transactionDetails"})
      .sign(getAlgorithm("secret"));

    DecodedJWT jwt = JWT.decode(token);
    String issuer = jwt.getIssuer();
    Date issuedAt = jwt.getIssuedAt();
    Date expiresAt = jwt.getExpiresAt();
    List<String> audience = jwt.getAudience();
    String subject = jwt.getSubject();
    List<String> roles = jwt.getClaim("roles").asList(String.class);
    List<String> permissions = jwt.getClaim("permissions").asList(String.class);

    system.log().info("iss: {} iat: {} exp: {}", issuer, issuedAt, expiresAt);
    system.log().info("sub: {} aud: {}", subject, audience);
    system.log().info("roles: {}", roles);
    system.log().info("permissions: {}", permissions);
  }

  private static List<Pair<String, String>> generateJwt(int quantity) {
    Random random = new Random();
    String secretKey = UUID.randomUUID().toString();
    Algorithm algorithm = getAlgorithm(secretKey);

    return IntStream.range(0, quantity).boxed()
      .map(a -> {
        AuthorizationContext authorizationContext = createContext(random);

        String[] roles = new String[authorizationContext.getRoles().size()];
        roles = authorizationContext.getRoles().toArray(roles);

        String[] permissions = new String[authorizationContext.getPermissions().size()];
        permissions = authorizationContext.getPermissions().toArray(permissions);

        String jwt = JWT.create()
          .withClaim("iss", UUID.randomUUID().toString())
          .withClaim("sub", authorizationContext.getUserId())
          .withArrayClaim("aud", new String[]{authorizationContext.getInstanceAlias()})
          .withArrayClaim("roles", roles)
          .withArrayClaim("permissions", permissions)
          .sign(algorithm);

        String authorizationContexJson = toJson(authorizationContext);

        return Pair.create(jwt, authorizationContexJson);
      })
      .collect(Collectors.toList());
  }

  private static AuthorizationContext createContext(Random random) {
    int size = random.ints(50, 80).findFirst().getAsInt();
    List<String> permissions = IntStream.range(0, size)
      .boxed().map(b -> UUID.randomUUID().toString())
      .collect(Collectors.toList());
    List<String> roles = Collections.singletonList(UUID.randomUUID().toString());
    return new AuthorizationContext(UUID.randomUUID().toString(), UUID.randomUUID().toString(), roles, permissions);
  }

  private static Algorithm getAlgorithm(String secretKey) {
    try {
      return Algorithm.HMAC256(secretKey);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private static String toJson(Object obj) {
    try {
      return MAPPER.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static <T> T toObject(String json, Class<T> clazz) {
    try {
      return MAPPER.readValue(json, clazz);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testSpeedJwt() {
    JWT_JSON_PAIR.stream()
      .map(Pair::first)
      .map(jwt -> JWT.decode(jwt))
      .map(decodedJWT -> {
        String instanceAlias = decodedJWT.getAudience().get(0);
        String userId = decodedJWT.getSubject();
        List<String> roles = decodedJWT.getClaim("roles").asList(String.class);
        List<String> permissions = decodedJWT.getClaim("permissions").asList(String.class);
        return new AuthorizationContext(instanceAlias, userId, roles, permissions);
      })
      .forEach(System.out::println);
  }

  @Test
  public void testSpeedSerialization() {
    JWT_JSON_PAIR.stream()
      .map(Pair::second)
      .map(json -> toObject(json, AuthorizationContext.class))
      .forEach(System.out::println);
  }

  @Test
  public void testFileIORead() throws ExecutionException, InterruptedException {
    Path path = Paths.get("/tmp/jwt.txt");
    Sink<ByteString, CompletionStage<Done>> printlnSink =
      Sink.foreach(chunk -> System.out.println(chunk.utf8String()));

    FileIO.fromPath(path)
      .to(printlnSink)
      .run(ActorMaterializer.create(system))
      .toCompletableFuture().get();
  }

  @Test
  public void testFileIOWrite() throws ExecutionException, InterruptedException {
    Path path = Paths.get("/tmp/jwt.txt");

    List<String> jwts = JWT_JSON_PAIR.stream()
      .map(Pair::first)
      .collect(Collectors.toList());

    Sink<ByteString, CompletionStage<IOResult>> sink = FileIO.toPath(path);
    Source.fromIterator(() -> jwts.iterator())
      .map(jws -> ByteString.fromString(jws + "\n"))
      .runWith(sink, ActorMaterializer.create(system))
      .toCompletableFuture().get();
  }

}
