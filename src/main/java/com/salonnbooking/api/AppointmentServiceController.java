package com.salonnbooking.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.service.AppointmentServiceService;

@RestController
@RequestMapping("/api/appointment-services")
public class AppointmentServiceController extends BaseCrudController<com.salonnbooking.domain.AppointmentService> {

    public AppointmentServiceController(AppointmentServiceService service) {
        super(service);
    }
}
