package com.github.prajjwal.florio.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Florio verification code");
            helper.setText(buildOtpEmailBody(otp), true);

            mailSender.send(message);

            log.debug("OTP email sent to={}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to={} : {}", toEmail, e.getMessage());
        }
    }

    private String buildOtpEmailBody(String otp) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
                    <h2>Verify your email</h2>
                        <p>Use the code below to verify your Florio account. It expires in <strong>10 minutes</strong>.</p>
                            <div style="font-size: 32px; font-weight: bold; letter-spacing: 8px; padding: 16px;
                                                background: #f4f4f4; text-align: center; border-radius: 8px;">
                                        %s
                            </div>
                                <p style="color: #888; font-size: 12px; margin-top: 16px;">
                                    If you didn't request this, you can safely ignore this email.
                                </p>
                </div>
                """.formatted(otp);
    }
}