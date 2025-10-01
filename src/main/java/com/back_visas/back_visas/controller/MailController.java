package com.back_visas.back_visas.controller;

import com.back_visas.back_visas.dto.request.MailRequestDTO;
import com.back_visas.back_visas.service.MailService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    private final MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping("/send")
    public void sendMail(@RequestBody MailRequestDTO request) throws Exception {
        mailService.sendMailWithTemplate(request);
    }
}

