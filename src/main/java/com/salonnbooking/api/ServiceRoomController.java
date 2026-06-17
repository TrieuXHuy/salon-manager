package com.salonnbooking.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.ServiceRoomRequests;
import com.salonnbooking.domain.ServiceRoom;
import com.salonnbooking.service.ServiceRoomService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/service-rooms")
public class ServiceRoomController {
	private final ServiceRoomService serviceRoomService;

	public ServiceRoomController(ServiceRoomService serviceRoomService) {
		this.serviceRoomService = serviceRoomService;
	}

	/** Lấy toàn bộ danh sách phòng dịch vụ. */
	@GetMapping
	public List<ServiceRoomRequests.Response> list() {
		return serviceRoomService.findAll().stream().map(ServiceRoomRequests.Response::from).toList();
	}

	/** Lấy danh sách phòng dịch vụ đang hoạt động. */
	@GetMapping("/active")
	public List<ServiceRoomRequests.Response> active() {
		return serviceRoomService.findActive().stream().map(ServiceRoomRequests.Response::from).toList();
	}

	/** Tạo mới một phòng dịch vụ. */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ServiceRoomRequests.Response create(@Valid @RequestBody ServiceRoomRequests.Create req) {
		ServiceRoom room = serviceRoomService.save(req);
		return ServiceRoomRequests.Response.from(room);
	}

	/** Cập nhật thông tin phòng dịch vụ theo id. */
	@PutMapping("/{id}")
	public ServiceRoomRequests.Response update(@PathVariable Integer id,
			@Valid @RequestBody ServiceRoomRequests.Update req) {
		ServiceRoom room = serviceRoomService.update(id, req);
		return ServiceRoomRequests.Response.from(room);
	}

	/** Xóa phòng dịch vụ theo id. */
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Integer id) {
		serviceRoomService.delete(id);
	}
}
