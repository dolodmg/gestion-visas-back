package com.back_visas.back_visas.service;

import com.back_visas.back_visas.model.Service;

import java.util.List;

public interface iServiceService {
    public List<Service> getServices();
    public Service getService(Long idService);
    public Service createService(Service service);
    public Service updateService(Long idService, Service serviceDetails);
    public void deleteService(Long idService);
}
