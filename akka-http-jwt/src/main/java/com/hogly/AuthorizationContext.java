package com.hogly;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AuthorizationContext {

  @JsonProperty
  private final String instanceAlias;
  @JsonProperty
  private final String userId;
  @JsonProperty
  private final List<String> roles;
  @JsonProperty
  private final List<String> permissions;

  @JsonCreator
  public AuthorizationContext(
    @JsonProperty("instanceAlias") String instanceAlias,
    @JsonProperty("userId") String userId,
    @JsonProperty("roles") List<String> roles,
    @JsonProperty("permissions") List<String> permissions) {
    this.instanceAlias = instanceAlias;
    this.userId = userId;
    this.roles = roles;
    this.permissions = permissions;
  }

  public String getInstanceAlias() {
    return instanceAlias;
  }

  public String getUserId() {
    return userId;
  }

  public List<String> getRoles() {
    return roles;
  }

  public List<String> getPermissions() {
    return permissions;
  }

  @Override
  public String toString() {
    return "AuthorizationContext { " +
      "instanceAlias='" + instanceAlias + '\'' +
      ", userId='" + userId + '\'' +
      ", roles=" + roles +
      ", permissions=" + permissions +
      '}';
  }

  public static Optional<AuthorizationContext> buildFromJwt(String token) {
    if (token.startsWith("Bearer")) {
      try {
        DecodedJWT jwt = JWT.decode(token.substring(6));
        List<String> audience = jwt.getAudience();
        String subject = jwt.getSubject();
        List<String> roles = Arrays.asList(jwt.getClaim("roles").asArray(String.class));
        List<String> permissions = Arrays.asList(jwt.getClaim("permissions").asArray(String.class));
        return Optional.of(new AuthorizationContext(audience.get(0), subject, roles, permissions));
      } catch (JWTDecodeException e) {
        System.err.println(e);
      }
    }
    return Optional.empty();
  }
}
