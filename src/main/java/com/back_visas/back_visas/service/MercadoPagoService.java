package com.back_visas.back_visas.service;

import com.back_visas.back_visas.dto.request.CreatePreferenceRequest;
import com.back_visas.back_visas.dto.request.MailRequestDTO;
import com.back_visas.back_visas.dto.request.ProcessPaymentRequest;
import com.back_visas.back_visas.feign.MercadoPagoAPIClient;
import com.back_visas.back_visas.model.Order;
import com.back_visas.back_visas.model.Payment;
import com.back_visas.back_visas.model.PaymentStatus;
import com.back_visas.back_visas.repository.OrderRepository;
import com.back_visas.back_visas.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MercadoPagoService implements iMercadoPagoService {

    @Value("${front.domain}")
    private String frontDomain;

    @Value("${front.protocol}")
    private String frontProtocol;

    @Value("${webhook.url}")
    private String urlNotification;

    @Value("${template.id}")
    private String templateId;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final MailService mailService;
    private final CurrencyService currencyService;
    private final MercadoPagoAPIClient mercadoPagoAPIClient;

    public MercadoPagoService(OrderRepository orderRepository,
                              PaymentRepository paymentRepository,
                              ObjectMapper objectMapper,
                              MailService mailService,
                              CurrencyService currencyService, MercadoPagoAPIClient mercadoPagoAPIClient) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.objectMapper = objectMapper;
        this.mailService = mailService;
        this.currencyService = currencyService;
        this.mercadoPagoAPIClient = mercadoPagoAPIClient;
    }

    @Override
    public Map<String, Object> getPreferenceUrl(String preferenceId) throws Exception {
        Map<String, Object> preference = mercadoPagoAPIClient.getPreference(preferenceId);

        Map<String, Object> result = new HashMap<>();
        result.put("preferenceId", preference.get("id"));
        result.put("checkoutUrl", preference.get("init_point"));
        result.put("sandboxUrl", preference.get("sandbox_init_point"));

        return result;
    }

    @Override
    public Map<String, Object> createPreference(CreatePreferenceRequest request) throws Exception {
        // Buscar la orden por externalReference
        Order order = orderRepository.findByExternalReference(request.getExternalReference())
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        // Convertir precio de USD a ARS
        BigDecimal priceUSD = BigDecimal.valueOf(request.getTotalPrice());
        BigDecimal priceARS = currencyService.convertUsdToArs(priceUSD);

        Map<String, Object> preference = new HashMap<>();

        // Items
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("title", request.getDescription());
        item.put("quantity", request.getQuantity() != null ? request.getQuantity() : 1);
        item.put("unit_price", priceARS.doubleValue());
        items.add(item);
        preference.put("items", items);

        // Back URLs
        Map<String, String> backUrls = new HashMap<>();
        backUrls.put("success", frontDomain + "/payment/success?order=" + order.getIdOrder());
        backUrls.put("failure", frontDomain  + "/payment/failure?order=" + order.getIdOrder());
        backUrls.put("pending", frontDomain + "/payment/pending?order=" + order.getIdOrder());
        preference.put("back_urls", backUrls);

        preference.put("external_reference", request.getExternalReference());
        preference.put("auto_return", "approved");
        preference.put("notification_url", urlNotification + "/api/mercadopago/webhook");

        // Payer info
        Map<String, String> payer = new HashMap<>();
        payer.put("email", request.getCustomerMail());
        if (request.getCustomerName() != null) {
            payer.put("name", request.getCustomerName());
        }
        preference.put("payer", payer);

        // Llamada a MP
        Map<String, Object> response = mercadoPagoAPIClient.createPreference(preference);

        Map<String, Object> result = new HashMap<>();
        result.put("preferenceId", response.get("id"));
        result.put("checkoutUrl", response.get("init_point"));
        result.put("sandboxUrl", response.get("sandbox_init_point"));

        return result;
    }

    @Override
    public Map<String, Object> processPayment(ProcessPaymentRequest request) throws Exception {
        BigDecimal amountUSD = BigDecimal.valueOf(request.getAmount());
        BigDecimal amountARS = currencyService.convertUsdToArs(amountUSD);
        String idempotencyKey = UUID.randomUUID().toString();
        Map<String, Object> paymentData = buildPaymentData(request, amountARS.doubleValue());

        try {
            Map<String, Object> mpResponse = mercadoPagoAPIClient.processPayment(paymentData, idempotencyKey);
            System.out.println("=== RESPUESTA DE MERCADOPAGO ===");
            System.out.println("MP Response: " + mpResponse);

            if (mpResponse == null) {
                throw new Exception("Respuesta nula de MercadoPago");
            }

            Object paymentId = mpResponse.get("id");
            Object status = mpResponse.get("status");

            if (paymentId == null) {
                throw new Exception("MercadoPago no devolvió un ID de pago válido");
            }

            Order order = orderRepository.findByExternalReference(request.getExternalReference())
                    .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

            // Crear Payment vinculado
            Payment payment = new Payment(order, request.getAmount(), null);
            payment.setMercadoPagoPaymentId(Long.valueOf(paymentId.toString()));
            payment.updateFromMercadoPagoResponse(mpResponse);
            paymentRepository.save(payment);

            // Actualizar el estado de la orden según MP
            order.setPaymentId(payment.getMercadoPagoPaymentId().toString());
            order.updateStatus();
            orderRepository.save(order);

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getIdOrder());
            result.put("paymentId", payment.getId());
            result.put("status", status);
            result.put("id", paymentId);

            return result;

        } catch (FeignException e) {
            throw new Exception("Error procesando pago con MercadoPago: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error procesando pago: " + e.getMessage());
        }
    }

    @Override
    public void handleWebhook(String body, Map<String, String> headers) throws Exception {
        Map<String, Object> notification = new ObjectMapper().readValue(body, Map.class);

        if ("payment".equals(notification.get("type"))) {
            Long paymentId = Long.valueOf(((Map<String, Object>) notification.get("data")).get("id").toString());

            Map<String, Object> mpPayment = mercadoPagoAPIClient.getPayment(paymentId);

            Payment payment = paymentRepository.findByMercadoPagoPaymentId(paymentId).orElse(null);

            if (payment == null) {
                String externalReference = (String) mpPayment.get("external_reference");
                Order order = orderRepository.findByExternalReference(externalReference)
                        .orElseThrow(() -> new Exception("Orden no encontrada"));

                payment = new Payment();
                payment.setOrder(order);
                payment.setAmount(order.getTotalPrice());
            }

            payment.updateFromMercadoPagoResponse(mpPayment);
            paymentRepository.save(payment);

            Order order = payment.getOrder();
            if (order.getPaymentId() == null) {
                order.setPaymentId(payment.getMercadoPagoPaymentId().toString());
            }
            order.updateStatus();
            orderRepository.save(order);

            if (payment.getStatus() == PaymentStatus.APPROVED) {
                try {
                    MailRequestDTO mailDto = new MailRequestDTO();
                    mailDto.setNameTo(order.getCustomerName());
                    mailDto.setTo(order.getCustomerMail());
                    mailDto.setTemplateId(templateId);
                    mailDto.setFirstName(order.getCustomerName());
                    mailDto.setServiceName(order.getService().getServiceName());
                    mailDto.setIdOrder(order.getIdOrder().toString());
                    LocalDateTime fecha = order.getCreationDatetime();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    String dateFormat = fecha.format(formatter);
                    mailDto.setCreationDatetime(dateFormat);
                    mailDto.setTotalPrice(order.getTotalPrice().toString());
                    mailDto.setQuantity(order.getQuantity());
                    mailDto.setCouponCode("MUNDIAL26");
                    mailService.sendMailWithTemplate(mailDto);
                } catch (Exception e) {
                    System.err.println("Error al enviar mail de confirmación: " + e.getMessage());
                }
            }
        }
    }

    private Order createOrUpdateOrder(CreatePreferenceRequest request) {
        // Si viene orderId, buscar la orden existente
        if (request.getIdOrder() != null) {
            return orderRepository.findById(request.getIdOrder())
                    .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
        }

        // Crear nueva orden
        Order order = new Order(
                request.getCustomerMail(),
                request.getCustomerName(),
                request.getTotalPrice(),
                request.getDescription()
        );

        // Generar external reference único
        order.setExternalReference(UUID.randomUUID().toString());

        return orderRepository.save(order);
    }

    private Order findOrderByExternalReference(String externalReference) {
        return orderRepository.findByExternalReference(externalReference)
                .orElse(null);
    }

    private Map<String, Object> buildPreference(Order order, CreatePreferenceRequest request) {
        Map<String, Object> preference = new HashMap<>();

        // Items
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("title", order.getDescription());
        item.put("quantity", 1);
        item.put("unit_price", order.getTotalPrice());
        item.put("external_reference", order.getExternalReference());
        items.add(item);
        preference.put("items", items);

        // URLs de retorno
        Map<String, String> backUrls = new HashMap<>();
        backUrls.put("success", frontDomain + "/payment/success?order_id=" + order.getIdOrder());
        backUrls.put("failure", frontDomain + "/payment/failure?order_id=" + order.getIdOrder());
        backUrls.put("pending", frontDomain + "/payment/pending?order_id=" + order.getIdOrder());
        preference.put("back_urls", backUrls);
        preference.put("auto_return", "approved");

        // Información del pagador
        if (order.getCustomerMail() != null) {
            Map<String, String> payer = new HashMap<>();
            payer.put("email", order.getCustomerMail());
            if (order.getCustomerName() != null) {
                payer.put("name", order.getCustomerName());
            }
            preference.put("payer", payer);
        }

        return preference;
    }

    private Map<String, Object> buildPaymentData(ProcessPaymentRequest request, Double amountARS) {

        Map<String, Object> paymentData = new HashMap<>();

        paymentData.put("transaction_amount", amountARS);
        paymentData.put("description", request.getDescription());
        paymentData.put("external_reference", request.getExternalReference());
        Object token = request.getFormData().get("token");
        if (token == null) throw new RuntimeException("Token es requerido");
        paymentData.put("token", token);
        Object paymentMethodId = request.getFormData().get("payment_method_id");
        if (paymentMethodId == null) throw new RuntimeException("payment_method_id es requerido");
        paymentData.put("payment_method_id", paymentMethodId);

        // Installments - opcional, default 1
        Object installments = request.getFormData().get("installments");
        paymentData.put("installments", installments != null ? installments : 1);

        // Issuer - opcional
        Object issuerId = request.getFormData().get("issuer_id");
        if (issuerId != null && !issuerId.toString().trim().isEmpty()) {
            paymentData.put("issuer_id", issuerId);
        }

        Map<String, Object> payer = new HashMap<>();
        String email = (String) request.getFormData().get("email");
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email del pagador es requerido");
        }
        payer.put("email", email);

        String firstName = (String) request.getFormData().get("first_name");
        if (firstName != null && !firstName.trim().isEmpty()) {
            payer.put("first_name", firstName);
        }

        paymentData.put("payer", payer);

        System.out.println("PaymentData final: " + paymentData);
        System.out.println("=== FIN DEBUG ===");

        return paymentData;
    }

    private void executePostPaymentActions(Payment payment) {
        if (payment.isApproved()) {
            System.out.println("Pago aprobado para orden: " + payment.getOrder().getIdOrder());
            // Aquí podrías ejecutar otras acciones post-pago, como enviar mail, notificación, etc.
        }
    }

    public Map<String, Object> verifyPayment(Long paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findByMercadoPagoPaymentId(paymentId);

        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            return Map.of(
                    "paymentId", payment.getMercadoPagoPaymentId(),
                    "status", payment.getStatus().name(),
                    "statusDetail", payment.getStatusDetail() != null ? payment.getStatusDetail() : "",
                    "amount", payment.getAmount(),
                    "payerEmail", payment.getPayerEmail() != null ? payment.getPayerEmail() : "",
                    "idOrder", payment.getOrder().getIdOrder(),
                    "idService", payment.getOrder().getService().getIdService()
            );
        }

        Map<String, Object> mpResponse = mercadoPagoAPIClient.getPayment(paymentId);
        String externalReference = (String) mpResponse.get("external_reference");

        Order order = orderRepository.findByExternalReference(externalReference)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        Payment payment = new Payment(order, order.getTotalPrice(), null);
        payment.setMercadoPagoPaymentId(paymentId);
        payment.updateFromMercadoPagoResponse(mpResponse);
        paymentRepository.save(payment);

        return Map.of(
                "paymentId", payment.getMercadoPagoPaymentId(),
                "status", payment.getStatus().name(),
                "statusDetail", payment.getStatusDetail() != null ? payment.getStatusDetail() : "",
                "amount", payment.getAmount(),
                "payerEmail", payment.getPayerEmail() != null ? payment.getPayerEmail() : "",
                "idOrder", order.getIdOrder(),
                "idService", order.getService().getIdService()
        );
    }
}
