package com.back_visas.back_visas.service;

import com.back_visas.back_visas.dto.request.MailRequestDTO;
import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import com.mailersend.sdk.recipients.Recipient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MailService {

    @Value("${mailersend.api.key}")
    private String apiKey;

    @Value("${mailersend.sender}")
    private String sender;
    String senderName = "ArgenVisa";

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    public void sendMailWithTemplate(MailRequestDTO mailRequest) throws MailerSendException {
        logger.info("📧 Intentando enviar email a: '{}' <{}>", mailRequest.getNameTo(), mailRequest.getTo());

        if (sender == null || sender.trim().isEmpty()) {
            throw new IllegalStateException("❌ El email del remitente no está configurado");
        }

        MailerSend ms = new MailerSend();
        ms.setToken(apiKey);

        Email email = new Email();
        email.setFrom(senderName, sender.trim());
        email.setSubject("Confirmación de tu orden - ArgenVisa");
        email.setTemplateId(mailRequest.getTemplateId());

        // Agregar destinatario
        email.addRecipient(mailRequest.getNameTo(), mailRequest.getTo());

        // Variables para la plantilla
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", mailRequest.getFirstName() != null ? mailRequest.getFirstName() : "Cliente");
        variables.put("serviceName", mailRequest.getServiceName() != null ? mailRequest.getServiceName() : "Visa EEUU");
        variables.put("idOrder", mailRequest.getIdOrder() != null ? mailRequest.getIdOrder() : "");
        variables.put("creationDatetime", mailRequest.getCreationDatetime() != null ? mailRequest.getCreationDatetime() : "");
        variables.put("totalPrice", mailRequest.getTotalPrice() != null ? mailRequest.getTotalPrice() : "Indefinido");
        variables.put("quantity", mailRequest.getQuantity() != 0 ? mailRequest.getQuantity() : "1");
        variables.put("couponCode", mailRequest.getCouponCode() != null ? mailRequest.getCouponCode() : "Sin cupón");

        // Agregar las variables **una por una**
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            email.addPersonalization(entry.getKey(), entry.getValue());
        }

        try {
            MailerSendResponse response = ms.emails().send(email);
            logger.info("✅ Correo enviado exitosamente. Message ID: {}", response.messageId);
        } catch (MailerSendException e) {
            logger.error("❌ Error al enviar el correo: {}", e.getMessage(), e);
            throw e;
        }
    }
}