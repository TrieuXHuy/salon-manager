package com.salonnbooking.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.domain.StaffWorkingHour;
import com.salonnbooking.service.StaffWorkingHourService;

@RestController
@RequestMapping("/api/staff-working-hours")
public class StaffWorkingHourController extends BaseCrudController<StaffWorkingHour> {

    public StaffWorkingHourController(StaffWorkingHourService service) {
        super(service);
    }
}
