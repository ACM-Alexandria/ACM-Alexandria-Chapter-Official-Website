package com.acm.acmwebsite.EmailServer.service;

import com.acm.acmwebsite.EmailServer.dto.EmailRequest;

import jakarta.validation.Valid;
/**
 *All the Implementation of this interface should process the request asynchronously.
 So this method is non blocking and all of the processing happens in the background
 *@param emailRequest fully Validated {@link EmailRequest} 
 */
public interface EmailService {
  void send(@Valid EmailRequest emailRequest);
}
