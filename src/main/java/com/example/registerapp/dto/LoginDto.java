package com.example.registerapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {

  private String email;
  private String password;
  private String otp;
}
