package com.salonnbooking.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.service.ServiceService;

@RestController
@RequestMapping("/api/services")
public class ServiceController extends BaseCrudController<com.salonnbooking.domain.Service> {

    public ServiceController(ServiceService service) {
        super(service);
    }
}
