package com.back_visas.back_visas.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PaymentRedirectController {

    @Value("${front.protocol}")
    private String frontProtocol;

    @Value("${front.domain}")
    private String frontDomain;

    private String frontUrl;

    @PostConstruct
    public void init() {
        frontUrl = frontProtocol + "://" + frontDomain;
        System.out.println("🌍 FRONT URL = " + frontUrl);
    }


    @GetMapping("/payment/success")
    public String redirectSuccess(@RequestParam Map<String,String> params) {
        return "redirect:" + frontUrl + "/payment/success?" + buildQuery(params);
    }

    @GetMapping("/payment/failure")
    public String redirectFailure(@RequestParam Map<String,String> params) {
        return "redirect:" + frontUrl + "/payment/failure?" + buildQuery(params);
    }

    @GetMapping("/payment/pending")
    public String redirectPending(@RequestParam Map<String,String> params) {
        return "redirect:" + frontUrl + "/payment/pending?" + buildQuery(params);
    }

    private String buildQuery(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }
}
