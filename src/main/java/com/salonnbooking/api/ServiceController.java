package com.salonnbooking.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.ServiceDtos;
import com.salonnbooking.service.ServiceService;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public List<ServiceDtos.Response> findAll(@RequestParam(required = false) Long categoryId) {
        return serviceService.getPublicServices(categoryId);
    }

    @GetMapping("/{id}")
    public ServiceDtos.Response findById(@PathVariable Long id) {
        return serviceService.getPublicServiceById(id);
    }
}
