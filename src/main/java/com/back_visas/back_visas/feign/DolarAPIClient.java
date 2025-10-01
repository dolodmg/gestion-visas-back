package com.back_visas.back_visas.feign;

import com.back_visas.back_visas.dto.response.DolarResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "dolarApiClient", url = "https://dolarapi.com/v1/dolares")
public interface DolarAPIClient {
    @GetMapping("/oficial")
    DolarResponseDTO getDolarOficial();
}
