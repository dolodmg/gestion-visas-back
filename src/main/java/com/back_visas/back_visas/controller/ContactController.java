package com.back_visas.back_visas.controller;

import com.back_visas.back_visas.dto.request.ContactRequestDTO;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Value("${spring.mail.username}")
    private String fromMail;

    private final JavaMailSender mailSender;

    public ContactController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostMapping
    public ResponseEntity<String> sendContactEmail(@RequestBody ContactRequestDTO contact) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(fromMail);
            helper.setTo(fromMail);
            helper.setSubject("Nueva consulta desde la web");
            helper.setReplyTo(contact.getEmail());
            String htmlMsg = "<div style='font-family:Arial,sans-serif; padding:20px; background-color:#f9f9f9; border:1px solid #ddd;'>"
                    + "<h2 style='color:#333;'>Nueva consulta desde la web</h2>"
                    + "<p><strong>Nombre:</strong> " + contact.getName() + "</p>"
                    + "<p><strong>Email:</strong> " + contact.getEmail() + "</p>"
                    + "<p><strong>Mensaje:</strong><br/>" + contact.getMessage() + "</p>"
                    + "</div>";

            helper.setText(htmlMsg, true);

            mailSender.send(mimeMessage);
            return ResponseEntity.ok("Consulta enviada con éxito");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar el correo: " + e.getMessage());
        }
    }
}
