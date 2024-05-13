package com.example.registerapp.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.exception.InvalidEmailFormatException;
import com.example.exception.InvalidMobileNumberFormatException;
import com.example.exception.UserNotFoundException;
import com.example.registerapp.dto.LoginDto;
import com.example.registerapp.dto.RegisterDto;
import com.example.registerapp.entity.User;
import com.example.registerapp.repository.UserRepository;
import com.example.registerapp.util.EmailUtil;
import com.example.registerapp.util.OtpUtil;

import jakarta.mail.MessagingException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private OtpUtil otpUtil;
    
    public boolean isAccountLocked(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with this email: " + email));
        LocalDateTime accountLockedUntil = user.getAccountLockedUntil();
        return accountLockedUntil != null && LocalDateTime.now().isBefore(accountLockedUntil);
    }

    public void incrementWrongLoginAttempts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with this email: " + email));
        int wrongAttempts = user.getWrongOtpAttempts();
        user.setWrongOtpAttempts(wrongAttempts + 1);
        if (wrongAttempts + 1 >= 3) {
            // Lock user's account for 5 minutes
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(5));
        }
        userRepository.save(user);
    }

    public String register(RegisterDto registerDto) {
        // Validate email format
        if (!isValidEmail(registerDto.getEmail())) {
        	throw new InvalidEmailFormatException("Invalid email format");
        }

        // Validate mobile number format
        if (!isValidMobileNo(registerDto.getMobileNo())) {
        	throw new InvalidMobileNumberFormatException("Invalid mobile number format");
        }
        
     // Check if the email already exists
        if (userRepository.existsByEmail(registerDto.getEmail())) {
        	throw new IllegalArgumentException("Email address already exists");
        }

        User user = new User();
        user.setName(registerDto.getName());
        user.setEmail(registerDto.getEmail());
        user.setPassword(registerDto.getPassword());
        user.setMobileNo(registerDto.getMobileNo());
        userRepository.save(user);

        return "User registration successful";
    }

    public String login(LoginDto loginDto) {
        String email = loginDto.getEmail();

        // Check if the user is locked
        if (isAccountLocked(email)) {
            return "You have exceeded the maximum number of wrong OTP attempts. Your account is locked for 5 minutes.";
        }

        // Find the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with this email: " + email));

        // Validate password
        if (!user.getPassword().equals(loginDto.getPassword())) {
            incrementWrongLoginAttempts(email);
            return "Incorrect password. Please try again.";
        } else {
            // Generate OTP
            String otp = otpUtil.generateOtp();

            // Send OTP email
            try {
                emailUtil.sendOtpEmail(email, otp);
            } catch (MessagingException e) {
                throw new RuntimeException("Unable to send OTP. Please try again.");
            }

            // Update user's OTP and OTP generation time in the database
            user.setOtp(otp);
            user.setOtpGeneratedTime(LocalDateTime.now());
            userRepository.save(user);

            return "OTP generated and sent to your email";
        }
    }

    public String verifyOtp(String email, String otp) {
        // Find the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with this email")); 
        
     // Check if OTP is null
        if (user.getOtp() == null) {
            return "OTP has already been used.";
        }
        
        // Check if OTP matches
        if (!user.getOtp().equals(otp)) {
            // Increment wrong OTP attempts
            user.setWrongOtpAttempts(user.getWrongOtpAttempts() + 1);
            userRepository.save(user);

            if (user.getWrongOtpAttempts() > 3) {
                // Lock user's account for 5 minutes
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(5));
                userRepository.save(user);
                return "You have exceeded the maximum number of wrong OTP attempts. Your account is locked for 5 minutes.";
            } else {
                // Invalid OTP
                return "Invalid OTP. Please try again.";
            }
        }

        // Check if OTP is within validity period
        long otpAgeInSeconds = Duration.between(user.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds();
        if (otpAgeInSeconds >= (2 * 60)) {
            // OTP has expired
            return "OTP has expired. Please try to login again.";
        }
       

     // Reset wrong OTP attempts count
        user.setWrongOtpAttempts(0);
        
     // Mark OTP as used
        user.setOtp(null);
        
     // Clear OTP and OTP generation time to mark it as expired
        user.setOtp(null);
        user.setOtpGeneratedTime(null);
        
        userRepository.save(user);
        // OTP is valid
        return "OTP verified. You can now proceed with login.";
    }


    private boolean isValidEmail(String email) {
        // Simple email format validation
        return email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    }

    private boolean isValidMobileNo(String mobileNo) {
        // Mobile number format validation
        return mobileNo.matches("[6-9]\\d{9}");
    }
}
