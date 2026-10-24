package com.back_visas.back_visas.service;

import com.back_visas.back_visas.dto.request.MailRequestDTO;
import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class MailService {

    // Bloques de "Pasos a seguir" disponibles en templates/mail/steps/
    public static final String TEMPLATE_USA = "usa";
    public static final String TEMPLATE_CANADA = "canada";
    public static final String TEMPLATE_ETA = "eta";
    private static final Set<String> VALID_TEMPLATES = Set.of(TEMPLATE_USA, TEMPLATE_CANADA, TEMPLATE_ETA);

    @Value("${mailersend.api.key}")
    private String apiKey;

    @Value("${mailersend.sender}")
    private String sender;

    @Value("${mailersend.reply.to}")
    private String reply_to;

    String senderName = "ArgenVisa";

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final ITemplateEngine templateEngine;

    public MailService(ITemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void sendMailWithTemplate(MailRequestDTO mailRequest) throws MailerSendException {
        logger.info("Intentando enviar email a: '{}' <{}>", mailRequest.getNameTo(), mailRequest.getTo());

        if (sender == null || sender.trim().isEmpty()) {
            throw new IllegalStateException("El email del remitente no está configurado");
        }

        MailerSend ms = new MailerSend();
        ms.setToken(apiKey);

        Email email = new Email();
        email.setFrom(senderName, sender.trim());
        email.setSubject("Confirmación de tu orden - ArgenVisa");
        email.setHtml(renderOrderConfirmation(mailRequest));
        email.AddReplyTo("ArgenVisa", reply_to);
        email.addRecipient(mailRequest.getNameTo(), mailRequest.getTo());

        try {
            MailerSendResponse response = ms.emails().send(email);
            logger.info(" Correo enviado exitosamente. Message ID: {}", response.messageId);
        } catch (MailerSendException e) {
            logger.error("Error al enviar el correo: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String renderOrderConfirmation(MailRequestDTO mailRequest) {
        // Solo se aceptan nombres conocidos: el valor se usa para resolver un fragmento de Thymeleaf
        String stepsTemplate = VALID_TEMPLATES.contains(mailRequest.getTemplate())
                ? mailRequest.getTemplate()
                : TEMPLATE_USA;

        // Variables para la plantilla
        Map<String, Object> variables = new HashMap<>();
        variables.put("stepsTemplate", stepsTemplate);
        variables.put("firstName", mailRequest.getFirstName() != null ? mailRequest.getFirstName() : "Cliente");
        variables.put("serviceName", mailRequest.getServiceName() != null ? mailRequest.getServiceName() : "Visa EEUU");
        variables.put("idOrder", mailRequest.getIdOrder() != null ? mailRequest.getIdOrder() : "");
        variables.put("creationDatetime", mailRequest.getCreationDatetime() != null ? mailRequest.getCreationDatetime() : "");
        variables.put("totalPrice", mailRequest.getTotalPrice() != null ? mailRequest.getTotalPrice() : "Indefinido");
        variables.put("quantity", mailRequest.getQuantity() != 0 ? mailRequest.getQuantity() : "1");
        // Sin cupón vigente el template oculta el bloque del cupón
        String couponCode = mailRequest.getCouponCode();
        variables.put("couponCode", couponCode != null && !couponCode.isBlank() ? couponCode : null);

        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process("mail/order-confirmation", context);
    }
}
