package com.example.eventmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    public JavaMailSender emailSender;

    public void sendVerificationLink(String email, String token) {
        // TODO: Errors handlers
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Email verification");
        message.setText("Please verify your email:\nhttp://localhost:4200/email-verification/" + token);
        emailSender.send(message);
    }

}
