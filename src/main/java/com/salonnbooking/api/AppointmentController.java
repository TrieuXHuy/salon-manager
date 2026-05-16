package com.salonnbooking.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.domain.Appointment;
import com.salonnbooking.service.AppointmentService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController extends BaseCrudController<Appointment> {

    public AppointmentController(AppointmentService service) {
        super(service);
    }
}
