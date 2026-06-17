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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.domain.Customer;
import com.salonnbooking.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	/** Lấy toàn bộ danh sách customer. */
	@GetMapping
	public List<CustomerRequests.Response> list() {
		return customerService.findAll().stream().map(CustomerRequests.Response::from).toList();
	}

	/** Lấy chi tiết customer theo id. */
	@GetMapping("/{id}")
	public CustomerRequests.Response get(@PathVariable Integer id) {
		Customer customer = customerService.findById(id);
		return CustomerRequests.Response.from(customer);
	}

	/** Lấy profile customer theo username. */
	@GetMapping("/profile")
	public CustomerRequests.Response profile(@RequestParam String username) {
		return CustomerRequests.Response.from(customerService.findProfile(username));
	}

	/** Hoàn thiện profile customer theo username và số điện thoại. */
	@PutMapping("/profile")
	public CustomerRequests.Response completeProfile(@Valid @RequestBody CustomerRequests.CompleteProfile req) {
		return CustomerRequests.Response.from(customerService.completeProfile(req));
	}

	/** Tạo customer mới kèm user đăng nhập tương ứng. */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CustomerRequests.Response create(@Valid @RequestBody CustomerRequests.Create req) {
		Customer customer = customerService.save(req);
		return CustomerRequests.Response.from(customer);
	}

	/** Cập nhật thông tin customer theo id. */
	@PutMapping("/{id}")
	public CustomerRequests.Response update(@PathVariable Integer id, @Valid @RequestBody CustomerRequests.Update req) {
		Customer customer = customerService.update(id, req);
		return CustomerRequests.Response.from(customer);
	}

	/** Xóa customer theo id. */
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Integer id) {
		customerService.delete(id);
	}
}
