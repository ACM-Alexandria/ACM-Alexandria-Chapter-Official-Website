package com.acm.acmwebsite.User_Authentication.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** SuccessRegisterResponse */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SuccessRegisterResponse {
  private UUID id;
  private String email;
}
