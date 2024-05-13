package com.example.registerapp.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.exception.InvalidEmailFormatException;
import com.example.exception.InvalidMobileNumberFormatException;
import com.example.exception.UserNotFoundException;
import com.example.registerapp.dto.LoginDto;
import com.example.registerapp.dto.RegisterDto;
import com.example.registerapp.service.UserService;

@RestController
public class UserController {

  @Autowired
  private UserService userService;
  
  private static final Map<String, LocalDateTime> lockedUsers = new ConcurrentHashMap<>();

  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
	  try {
          String response = userService.register(registerDto);
          return ResponseEntity.ok(response);
      } catch (InvalidEmailFormatException | InvalidMobileNumberFormatException | IllegalArgumentException e) {
          return ResponseEntity.badRequest().body(e.getMessage());
      }
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody LoginDto loginDto) {
      String email = loginDto.getEmail();

      // Check if the user is locked
      if (lockedUsers.containsKey(email)) {
          LocalDateTime lockTime = lockedUsers.get(email);
          if (lockTime.isAfter(LocalDateTime.now())) {
              // User account is still locked
              return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Your account is locked for 5 minutes.");
          } else {
              // Account lock period has expired, remove from locked users
              lockedUsers.remove(email);
          }
      }

      // Attempt login
      try {
          String response = userService.login(loginDto);
          return ResponseEntity.ok(response);
      } catch (UserNotFoundException e) {
          // Increment wrong login attempts
          userService.incrementWrongLoginAttempts(email);
          // Check if the user has exceeded the maximum number of wrong login attempts
          if (userService.isAccountLocked(email)) {
              // Lock user's account for 5 minutes
              lockedUsers.put(email, LocalDateTime.now().plusMinutes(5));
              return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Your account is locked for 5 minutes.");
          } else {
              return ResponseEntity.badRequest().body(e.getMessage());
          }
      }
  }
  @PostMapping("/verify-otp")
  public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
    return new ResponseEntity<>(userService.verifyOtp(email, otp), HttpStatus.OK);
  }
}

