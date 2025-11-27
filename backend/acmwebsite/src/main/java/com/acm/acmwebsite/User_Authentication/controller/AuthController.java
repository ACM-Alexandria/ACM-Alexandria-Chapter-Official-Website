package com.acm.acmwebsite.User_Authentication.controller;

import com.acm.acmwebsite.User_Authentication.dto.RegisterDTO;
import com.acm.acmwebsite.User_Authentication.dto.SuccessRegisterResponse;
import com.acm.acmwebsite.User_Authentication.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** RegisterController */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final RegisterService registerService;

  @PostMapping("register")
  public ResponseEntity<SuccessRegisterResponse> registerUser(
      @RequestBody @Valid RegisterDTO registerDTO) {
    SuccessRegisterResponse savedUser = registerService.createUser(registerDTO);
    return ResponseEntity.status(201).body(savedUser);
  }
}
