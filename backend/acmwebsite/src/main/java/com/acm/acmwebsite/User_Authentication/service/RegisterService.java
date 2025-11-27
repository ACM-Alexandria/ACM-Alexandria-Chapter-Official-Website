package com.acm.acmwebsite.User_Authentication.service;

import com.acm.acmwebsite.User_Authentication.dto.RegisterDTO;
import com.acm.acmwebsite.User_Authentication.dto.SuccessRegisterResponse;

/** RegisterService */
public interface RegisterService {

  SuccessRegisterResponse createUser(RegisterDTO registerDTO);
}
