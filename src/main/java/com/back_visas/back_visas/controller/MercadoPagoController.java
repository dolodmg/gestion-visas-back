package com.back_visas.back_visas.controller;

import com.back_visas.back_visas.dto.request.CreatePreferenceRequest;
import com.back_visas.back_visas.dto.request.MailRequestDTO;
import com.back_visas.back_visas.dto.request.ProcessPaymentRequest;
import com.back_visas.back_visas.model.Order;
import com.back_visas.back_visas.model.PaymentStatus;
import com.back_visas.back_visas.repository.OrderRepository;
import com.back_visas.back_visas.service.MailService;
import com.back_visas.back_visas.service.MercadoPagoService;
import com.back_visas.back_visas.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mercadopago")
@CrossOrigin(origins = "http://localhost:3000")
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;
    private final MailService mailService;
    private final OrderRepository orderRepository;

    @Value("${template.id}")
    private String templateId;

    public MercadoPagoController(MercadoPagoService mercadoPagoService, MailService mailService, OrderRepository orderRepository) {
        this.mercadoPagoService = mercadoPagoService;
        this.mailService = mailService;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/preference/{preferenceId}")
    public ResponseEntity<Map<String, Object>> getPreferenceUrl(@PathVariable String preferenceId) {
        try {
            Map<String, Object> result = mercadoPagoService.getPreferenceUrl(preferenceId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error obteniendo preference: " + e.getMessage()));
        }
    }

    @PostMapping("/create-preference")
    public ResponseEntity<Map<String, Object>> createPreference(@RequestBody CreatePreferenceRequest request) {
        try {
            Map<String, Object> response = mercadoPagoService.createPreference(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error creando preferencia: " + e.getMessage()));
        }
    }

    @PostMapping("/process-payment")
    public ResponseEntity<?> processPayment(@RequestBody ProcessPaymentRequest request) {
        try {
            Map<String, Object> response = mercadoPagoService.processPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error procesando pago: " + e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveWebhook(
            @RequestBody String body,
            @RequestHeader Map<String, String> headers) {
        try {
            mercadoPagoService.handleWebhook(body, headers);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error procesando webhook");
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestParam("paymentId") Long paymentId) {
        try {
            Map<String, Object> paymentData = mercadoPagoService.verifyPayment(paymentId);
            return ResponseEntity.ok(paymentData);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error verificando pago: " + e.getMessage()));
        }
    }

    @PostMapping("/resend-confirmation/{orderId}")
    public ResponseEntity<?> resendConfirmationMail(@PathVariable Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new Exception("Orden no encontrada"));

            boolean hasApprovedPayment = order.getPayments().stream()
                    .anyMatch(p -> p.getStatus() == PaymentStatus.APPROVED);

            if (!hasApprovedPayment) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No hay pagos aprobados para esta orden"));
            }

            MailRequestDTO mailDto = new MailRequestDTO();
            mailDto.setNameTo(order.getCustomerName());
            mailDto.setTo(order.getCustomerMail());
            mailDto.setTemplateId(templateId);
            mailDto.setFirstName(order.getCustomerName());
            mailDto.setServiceName(order.getService().getServiceName());
            mailDto.setIdOrder(order.getIdOrder().toString());
            mailDto.setCreationDatetime(order.getCreationDatetime().toString());
            mailDto.setTotalPrice(order.getTotalPrice().toString());
            mailDto.setCouponCode(order.getCoupon() != null ? order.getCoupon().getCouponCode() : "");

            mailService.sendMailWithTemplate(mailDto);

            return ResponseEntity.ok(Map.of("message", "Correo de confirmación reenviado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error reenviando el correo: " + e.getMessage()));
        }
    }

}