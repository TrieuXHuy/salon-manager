package com.salonnbooking.api;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.BookingDtos;
import com.salonnbooking.service.BookingService;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/services/{serviceId}/staff")
    public List<BookingDtos.StaffResponse> getStaffByService(@PathVariable Long serviceId) {
        return bookingService.getStaffByService(serviceId);
    }

    @GetMapping("/staff")
    public List<BookingDtos.StaffResponse> getStaffByServices(@RequestParam String serviceIds) {
        return bookingService.getStaffByServices(parseIds(serviceIds));
    }

    @GetMapping("/available-slots")
    public List<BookingDtos.AvailableSlotResponse> getAvailableSlots(
            @RequestParam Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String serviceIds) {
        return bookingService.getAvailableSlots(staffId, date, parseIds(serviceIds));
    }

    private List<Long> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(Long::valueOf)
                .toList();
    }
}
