package com.back_visas.back_visas.service;

import java.math.BigDecimal;

public interface iCurrencyService {
    public BigDecimal convertUsdToArs(BigDecimal usdAmount);
}
