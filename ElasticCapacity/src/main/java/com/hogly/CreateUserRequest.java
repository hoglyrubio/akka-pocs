package com.hogly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;

public class CreateUserRequest {

  public final static ObjectMapper MAPPER = new ObjectMapper();

  public static void main(String[] args) throws IOException {

    FileReader reader = new FileReader("c:\\tmp\\lsf-2.csv");
    CSVParser records = CSVFormat.EXCEL.withHeader().parse(reader);
    for (CSVRecord record : records) {
      System.out.println(createUserRequest(record));
    }
  }

  public static String createUserRequest(CSVRecord record) throws JsonProcessingException {
    ObjectNode objectNode = MAPPER.createObjectNode();
    objectNode.put("eid", sanitizeEid(record.get("name")));
    objectNode.put("firstName", firstName(record.get("name")));
    objectNode.put("lastName", lastName(record.get("name")));
    objectNode.put("email", email(record.get("email")));
    objectNode.put("roles", roles(record.get("role")));
    objectNode.put("payees", eids(record.get("eids")));
    objectNode.put("auth0User", auth0User(record.get("auth0User")));
    return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
  }

  private static Boolean auth0User(String auth0User) {
    return Boolean.valueOf(auth0User);
  }

  private static ArrayNode eids(String eids) {
    ArrayNode node = MAPPER.createArrayNode();
    for(String eid : eids.split(",")) {
      node.add(eid.trim());
    }
    return node;
  }

  private static ArrayNode roles(String role) {
    return MAPPER.createArrayNode().add(role);
  }

  private static String email(String email) {
    return email.toLowerCase();
  }

  private static String lastName(String name) {
    return name.substring(name.indexOf(" ") + 1);
  }

  private static String firstName(String name) {
    return name.substring(0, name.indexOf(" "));
  }

  private static String sanitizeEid(String name) {
    String step1 = name.toLowerCase().replace(" ", ".");
    return step1.substring(0, step1.length() > 15 ? 15 : step1.length());
  }

}
