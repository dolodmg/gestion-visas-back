package com.back_visas.back_visas.service;

import com.back_visas.back_visas.dto.response.DolarResponseDTO;
import com.back_visas.back_visas.feign.DolarAPIClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CurrencyService implements iCurrencyService {
    private final DolarAPIClient dolarAPIClient;

    public CurrencyService(DolarAPIClient dolarAPIClient) {
        this.dolarAPIClient = dolarAPIClient;
    }

    @Override
    public BigDecimal convertUsdToArs(BigDecimal usdAmount) {
        DolarResponseDTO response = dolarAPIClient.getDolarOficial();
        return usdAmount.multiply(response.getVenta());
    }
}
