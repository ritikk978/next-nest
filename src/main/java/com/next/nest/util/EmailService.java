package com.next.nest.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
//    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    @Async
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String templateName) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//            helper.setFrom(fromEmail);
//            helper.setTo(to);
//            helper.setSubject(subject);
//
//            String htmlContent = templateEngine.process(templateName, context);
//            helper.setText(htmlContent, true);
//
//            mailSender.send(message);
//            log.info("HTML Email sent successfully to: {}", to);
//        } catch (MessagingException e) {
//            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
//        }
    }

    @Async
    public void sendVerificationEmail(String to, String token) {
//        Context context = new Context();
//        context.setVariable("verificationUrl", appUrl + "/verify-email?token=" + token);
//
//        sendHtmlEmail(
//            to,
//            "Verify Your NextNest Account",
//            "email-verification",
//            context
//        );
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
//        Context context = new Context();
//        context.setVariable("resetUrl", appUrl + "/reset-password?token=" + token);
//
//        sendHtmlEmail(
//            to,
//            "Reset Your NextNest Password",
//            "password-reset",
//            context
//        );
    }

    @Async
    public void sendBookingConfirmationEmail(String to, String name, String propertyTitle, 
                                            String date, String time, String bookingId) {
//        Context context = new Context();
//        context.setVariable("name", name);
//        context.setVariable("propertyTitle", propertyTitle);
//        context.setVariable("date", date);
//        context.setVariable("time", time);
//        context.setVariable("bookingId", bookingId);
//        context.setVariable("bookingDetailUrl", appUrl + "/bookings/" + bookingId);
//
//        sendHtmlEmail(
//            to,
//            "Booking Confirmation - NextNest",
//            "booking-confirmation",
//            context
//        );
    }

    @Async
    public void sendPaymentConfirmationEmail(String to, String name, String propertyTitle,
                                           String amount, String transactionId, String receiptUrl) {
//        Context context = new Context();
//        context.setVariable("name", name);
//        context.setVariable("propertyTitle", propertyTitle);
//        context.setVariable("amount", amount);
//        context.setVariable("transactionId", transactionId);
//        context.setVariable("receiptUrl", receiptUrl);
//        context.setVariable("transactionDetailUrl", appUrl + "/transactions/" + transactionId);
//
//        sendHtmlEmail(
//            to,
//            "Payment Confirmation - NextNest",
//            "payment-confirmation",
//            context
//        );
    }

    @Async
    public void sendPropertyListingApprovedEmail(String to, String name, String propertyTitle) {
//        Context context = new Context();
//        context.setVariable("name", name);
//        context.setVariable("propertyTitle", propertyTitle);
//        context.setVariable("propertyUrl", appUrl + "/properties?owner=" + name);
//
//        sendHtmlEmail(
//            to,
//            "Property Listing Approved - NextNest",
//            "property-approved",
//            context
//        );
    }
}