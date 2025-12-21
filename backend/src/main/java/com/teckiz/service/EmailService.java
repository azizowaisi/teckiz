package com.teckiz.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.List;

@Slf4j
@Service
public class EmailService {

    @Value("${app.aws.region:us-east-1}")
    private String region;

    @Value("${app.is-dev-env:false}")
    private Boolean isDevEnv;

    @Value("${app.dev-env-email:}")
    private String devEnvEmail;

    @Value("${app.email.from-address:noreply@teckiz.com}")
    private String fromAddress;

    @Value("${app.email.from-name:Teckiz}")
    private String fromName;

    private SesClient sesClient; // Can be null if SES not configured

    @Autowired(required = false)
    public void setSesClient(@Nullable SesClient sesClient) {
        this.sesClient = sesClient; // Can be null if SES not configured
    }

    /**
     * Send email using AWS SES
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body (HTML or plain text)
     * @param isHtml Whether body is HTML
     * @return true if sent successfully
     */
    public boolean sendEmail(String to, String subject, String body, boolean isHtml) {
        if (to == null || to.isEmpty()) {
            log.error("Cannot send email: recipient is null or empty");
            return false;
        }

        if (sesClient == null) {
            log.warn("SES client not configured. Email to {} would be sent: {}", to, subject);
            return false;
        }

        // In dev environment, redirect all emails to dev email
        if (isDevEnv && devEnvEmail != null && !devEnvEmail.isEmpty()) {
            log.info("Dev environment: Redirecting email from {} to {}", to, devEnvEmail);
            to = devEnvEmail;
        }

        try {
            Destination destination = Destination.builder()
                    .toAddresses(to)
                    .build();

            Content subjectContent = Content.builder()
                    .data(subject)
                    .charset("UTF-8")
                    .build();

            Content bodyContent = Content.builder()
                    .data(body)
                    .charset("UTF-8")
                    .build();

            Body emailBody;
            if (isHtml) {
                emailBody = Body.builder()
                        .html(bodyContent)
                        .build();
            } else {
                emailBody = Body.builder()
                        .text(bodyContent)
                        .build();
            }

            Message message = Message.builder()
                    .subject(subjectContent)
                    .body(emailBody)
                    .build();

            String from = fromName != null && !fromName.isEmpty()
                    ? String.format("%s <%s>", fromName, fromAddress)
                    : fromAddress;

            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source(from)
                    .destination(destination)
                    .message(message)
                    .build();

            SendEmailResponse response = sesClient.sendEmail(emailRequest);
            log.info("Email sent successfully. MessageId: {}, To: {}", response.messageId(), to);
            return true;

        } catch (SesException e) {
            log.error("Error sending email to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send email to multiple recipients
     */
    public boolean sendBulkEmail(List<String> toList, String subject, String body, boolean isHtml) {
        if (toList == null || toList.isEmpty()) {
            log.error("Cannot send bulk email: recipient list is empty");
            return false;
        }

        boolean allSent = true;
        for (String to : toList) {
            if (!sendEmail(to, subject, body, isHtml)) {
                allSent = false;
            }
        }
        return allSent;
    }

    /**
     * Send HTML email
     */
    public boolean sendHtmlEmail(String to, String subject, String htmlBody) {
        return sendEmail(to, subject, htmlBody, true);
    }

    /**
     * Send plain text email
     */
    public boolean sendTextEmail(String to, String subject, String textBody) {
        return sendEmail(to, subject, textBody, false);
    }

    /**
     * Send verification email
     */
    public boolean sendVerificationEmail(String to, String verificationLink) {
        String subject = "Verify Your Email Address";
        String htmlBody = String.format("""
            <html>
            <body>
                <h2>Email Verification</h2>
                <p>Please click the link below to verify your email address:</p>
                <p><a href="%s">Verify Email</a></p>
                <p>Or copy and paste this link into your browser:</p>
                <p>%s</p>
                <p>This link will expire in 24 hours.</p>
            </body>
            </html>
            """, verificationLink, verificationLink);

        return sendHtmlEmail(to, subject, htmlBody);
    }

    /**
     * Send password reset email
     */
    public boolean sendPasswordResetEmail(String to, String resetLink) {
        String subject = "Password Reset Request";
        String htmlBody = String.format("""
            <html>
            <body>
                <h2>Password Reset</h2>
                <p>You requested to reset your password. Click the link below to reset it:</p>
                <p><a href="%s">Reset Password</a></p>
                <p>Or copy and paste this link into your browser:</p>
                <p>%s</p>
                <p>This link will expire in 1 hour.</p>
                <p>If you didn't request this, please ignore this email.</p>
            </body>
            </html>
            """, resetLink, resetLink);

        return sendHtmlEmail(to, subject, htmlBody);
    }

    /**
     * Send welcome email
     */
    public boolean sendWelcomeEmail(String to, String name) {
        String subject = "Welcome to Teckiz";
        String htmlBody = String.format("""
            <html>
            <body>
                <h2>Welcome, %s!</h2>
                <p>Thank you for joining Teckiz. We're excited to have you on board!</p>
                <p>You can now access your account and start using our services.</p>
            </body>
            </html>
            """, name != null ? name : "User");

        return sendHtmlEmail(to, subject, htmlBody);
    }

    /**
     * Send notification email
     */
    public boolean sendNotificationEmail(String to, String title, String message) {
        String subject = title != null ? title : "Notification from Teckiz";
        String htmlBody = String.format("""
            <html>
            <body>
                <h2>%s</h2>
                <p>%s</p>
            </body>
            </html>
            """, title != null ? title : "Notification", message);

        return sendHtmlEmail(to, subject, htmlBody);
    }

    /**
     * Verify email address with SES
     */
    public boolean verifyEmailAddress(String email) {
        if (sesClient == null) {
            log.warn("SES client not configured. Cannot verify email: {}", email);
            return false;
        }

        try {
            VerifyEmailAddressRequest request = VerifyEmailAddressRequest.builder()
                    .emailAddress(email)
                    .build();

            sesClient.verifyEmailAddress(request);
            log.info("Verification email sent to: {}", email);
            return true;
        } catch (SesException e) {
            log.error("Error verifying email address {}: {}", email, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if email address is verified in SES
     */
    public boolean isEmailVerified(String email) {
        if (sesClient == null) {
            log.warn("SES client not configured. Cannot check email verification: {}", email);
            return false;
        }

        try {
            GetIdentityVerificationAttributesRequest request = GetIdentityVerificationAttributesRequest.builder()
                    .identities(email)
                    .build();

            GetIdentityVerificationAttributesResponse response = sesClient.getIdentityVerificationAttributes(request);
            var attributes = response.verificationAttributes();

            if (attributes.containsKey(email)) {
                var status = attributes.get(email).verificationStatus();
                return status == VerificationStatus.SUCCESS;
            }
            return false;
        } catch (SesException e) {
            log.error("Error checking email verification status for {}: {}", email, e.getMessage(), e);
            return false;
        }
    }
}

