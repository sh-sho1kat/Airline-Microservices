package com.sho1kat.service;

public interface MailService {

    void sendVerificationEmail(String to, String Name, String token);

    void sendPasswordResetEmail(String to, String Name, String token);

    void sendStaffApprovedEmail(String to, String Name);

    void sendStaffRejectedEmail(String to, String Name, String reason);

    void sendMail(String to, String subject, String body);
}