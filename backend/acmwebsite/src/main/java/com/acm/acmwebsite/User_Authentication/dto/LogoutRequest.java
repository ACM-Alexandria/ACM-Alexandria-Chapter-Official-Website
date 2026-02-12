package com.acm.acmwebsite.User_Authentication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LogoutRequest {
    @JsonProperty("refresh_token")
    private String refreshToken;
}
