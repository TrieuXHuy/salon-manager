package com.salonnbooking.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.ServiceCategoryDtos;
import com.salonnbooking.service.ServiceCategoryService;

@RestController
@RequestMapping("/api/admin/service-categories")
public class AdminServiceCategoryController {

    private final ServiceCategoryService serviceCategoryService;

    public AdminServiceCategoryController(ServiceCategoryService serviceCategoryService) {
        this.serviceCategoryService = serviceCategoryService;
    }

    @GetMapping
    public List<ServiceCategoryDtos.Response> findAll() {
        return serviceCategoryService.getAdminCategories();
    }

    @PostMapping
    public ServiceCategoryDtos.Response create(@RequestBody ServiceCategoryDtos.UpsertRequest request) {
        return serviceCategoryService.create(request);
    }

    @PutMapping("/{id}")
    public ServiceCategoryDtos.Response update(
            @PathVariable Long id,
            @RequestBody ServiceCategoryDtos.UpsertRequest request) {
        return serviceCategoryService.update(id, request);
    }

    @PatchMapping("/{id}/toggle-active")
    public ServiceCategoryDtos.Response toggleActive(@PathVariable Long id) {
        return serviceCategoryService.toggleActive(id);
    }
}
