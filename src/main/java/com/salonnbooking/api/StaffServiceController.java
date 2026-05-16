package com.salonnbooking.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.domain.StaffService;
import com.salonnbooking.service.StaffServiceService;

@RestController
@RequestMapping("/api/staff-services")
public class StaffServiceController extends BaseCrudController<StaffService> {

    public StaffServiceController(StaffServiceService service) {
        super(service);
    }
}
