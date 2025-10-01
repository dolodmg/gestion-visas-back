package com.back_visas.back_visas.controller;

import com.back_visas.back_visas.service.CurrencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {
    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/usd-to-ars")
    public BigDecimal convertUsdToArs(@RequestParam Double amount) {
        return currencyService.convertUsdToArs(BigDecimal.valueOf(amount));
    }

}
