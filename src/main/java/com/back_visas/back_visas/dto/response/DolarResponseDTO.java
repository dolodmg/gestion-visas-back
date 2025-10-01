package com.back_visas.back_visas.dto.response;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DolarResponseDTO {
    private String moneda;
    private String casa;
    private String nombre;
    private BigDecimal compra;
    private BigDecimal venta;
    private LocalDateTime fechaActualizacion;
}
