package com.back_visas.back_visas.controller;

import com.back_visas.back_visas.model.Service;
import com.back_visas.back_visas.service.ServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public ResponseEntity<List<Service>> getServices() {
        List<Service> services = serviceService.getServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{idService}")
    public ResponseEntity<Service> getService(@PathVariable Long idService) {
        Service service = serviceService.getService(idService);
        return ResponseEntity.ok(service);
    }

    @PostMapping
    public ResponseEntity<Service> createService(@RequestBody Service service) {
        Service serviceCreated = serviceService.createService(service);
        return ResponseEntity.ok(serviceCreated);
    }

    @PutMapping("/{idService}")
    public ResponseEntity<Service> updateService(@PathVariable Long idService, @RequestBody Service serviceDetails) {
        Service service = serviceService.updateService(idService, serviceDetails);
        return ResponseEntity.ok(service);
    }

    @DeleteMapping("/{idService}")
    public void deleteService(@PathVariable Long idService) {
        serviceService.deleteService(idService);
    }


}
