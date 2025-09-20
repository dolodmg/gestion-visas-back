package com.back_visas.back_visas.service;

import com.back_visas.back_visas.repository.ServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ServiceService implements iServiceService {
    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public List<com.back_visas.back_visas.model.Service> getServices() {
        return serviceRepository.findAll();
    }

    @Override
    public com.back_visas.back_visas.model.Service getService(Long idService) {
        return serviceRepository.findById(idService).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró un servicio con id: " + idService));
    }

    @Override
    public com.back_visas.back_visas.model.Service createService(com.back_visas.back_visas.model.Service service) {
        return serviceRepository.save(service);
    }

    @Override
    public com.back_visas.back_visas.model.Service updateService(Long idService, com.back_visas.back_visas.model.Service serviceDetails) {
        com.back_visas.back_visas.model.Service service = serviceRepository.findById(idService)
                .orElseThrow(() -> new RuntimeException("No se encontró ese servicio"));

        service.setServiceName(serviceDetails.getServiceName());
        service.setPricePerPerson(serviceDetails.getPricePerPerson());
        service.setAllowsVariableQuantity(serviceDetails.isAllowsVariableQuantity());
        service.setFixedQuantity(serviceDetails.getFixedQuantity());

        // Validación: si no permite cantidad variable, fixedQuantity es 1
        if (!service.isAllowsVariableQuantity() && service.getFixedQuantity() != 1) {
            throw new IllegalArgumentException("Los servicios con cantidad fija deben tener fixedQuantity = 1");
        }

        return serviceRepository.save(service);
    }

    @Override
    public void deleteService(Long idService) {
        serviceRepository.deleteById(idService);
    }
}
