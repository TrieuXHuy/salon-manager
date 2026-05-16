package com.salonnbooking.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.domain.ServiceCategory;
import com.salonnbooking.service.ServiceCategoryService;

@RestController
@RequestMapping("/api/service-categories")
public class ServiceCategoryController extends BaseCrudController<ServiceCategory> {

    public ServiceCategoryController(ServiceCategoryService service) {
        super(service);
    }
}
