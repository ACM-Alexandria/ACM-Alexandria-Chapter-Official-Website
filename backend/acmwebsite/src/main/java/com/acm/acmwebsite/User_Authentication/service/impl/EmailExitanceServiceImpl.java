package com.acm.acmwebsite.User_Authentication.service.impl;

import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/*
 this api chekcs only for syntax and mx records but not reliable in  checking smtp as most of this apis are paid
*/

@Service
public class EmailExitanceServiceImpl implements EmailExitanceService {

  private static final String API_URL = "https://rapid-email-verifier.fly.dev/api/validate?email=";

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public boolean isEmailReal(String email) {
    return verifyWithRapid(email);
  }

  public boolean verifyWithRapid(String email) {
    try {
      JsonNode response = restTemplate.getForObject(API_URL + email, JsonNode.class);

      if (response == null || response.get("validations") == null) {
        return false;
      }

      JsonNode validations = response.get("validations");
      boolean syntax = validations.get("syntax").asBoolean();
      boolean domainExists = validations.get("domain_exists").asBoolean();
      boolean mxRecords = validations.get("mx_records").asBoolean();
      boolean mailboxExists = validations.get("mailbox_exists").asBoolean();

      return syntax && domainExists && mxRecords && mailboxExists;

    } catch (Exception e) {
      System.out.println(e.getMessage());
      return false;
    }
  }
}
