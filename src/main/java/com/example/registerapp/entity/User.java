package com.example.registerapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Data
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  private String name;
  private String email;
  private String password;
  private String mobileNo;
  private boolean active;
  private String otp;
  private LocalDateTime otpGeneratedTime;
  private int wrongOtpAttempts;
  private LocalDateTime accountLockedUntil;
  private boolean otpUsed;
}

 