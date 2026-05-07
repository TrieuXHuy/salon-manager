package com.salonnbooking.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.SmsLogRequests;
import com.salonnbooking.domain.SmsLog;
import com.salonnbooking.service.SmsLogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sms-logs")
public class SmsLogController {
	private final SmsLogService smsLogService;

	public SmsLogController(SmsLogService smsLogService) {
		this.smsLogService = smsLogService;
	}

	@GetMapping
	@Transactional(readOnly = true)
	public List<SmsLogRequests.Response> list() {
		return smsLogService.findAll().stream().map(SmsLogRequests.Response::from).toList();
	}

	@GetMapping("/{id}")
	@Transactional(readOnly = true)
	public SmsLogRequests.Response get(@PathVariable Integer id) {
		SmsLog log = smsLogService.findById(id);
		return SmsLogRequests.Response.from(log);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SmsLogRequests.Response create(@Valid @RequestBody SmsLogRequests.Create req) {
		SmsLog log = smsLogService.save(req);
		return SmsLogRequests.Response.from(log);
	}

	@PutMapping("/{id}")
	public SmsLogRequests.Response update(@PathVariable Integer id, @Valid @RequestBody SmsLogRequests.Update req) {
		SmsLog log = smsLogService.update(id, req);
		return SmsLogRequests.Response.from(log);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Integer id) {
		smsLogService.delete(id);
	}
}
