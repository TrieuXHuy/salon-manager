package com.salonnbooking.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.ServiceDtos;
import com.salonnbooking.service.ServiceService;

@RestController
@RequestMapping("/api/admin/services")
public class AdminServiceController {

    private final ServiceService serviceService;

    public AdminServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public List<ServiceDtos.Response> findAll(@RequestParam(required = false) Long categoryId) {
        return serviceService.getAdminServices(categoryId);
    }

    @PostMapping
    public ServiceDtos.Response create(@RequestBody ServiceDtos.UpsertRequest request) {
        return serviceService.create(request);
    }

    @PutMapping("/{id}")
    public ServiceDtos.Response update(@PathVariable Long id, @RequestBody ServiceDtos.UpsertRequest request) {
        return serviceService.update(id, request);
    }

    @PatchMapping("/{id}/toggle-active")
    public ServiceDtos.Response toggleActive(@PathVariable Long id) {
        return serviceService.toggleActive(id);
    }
}
