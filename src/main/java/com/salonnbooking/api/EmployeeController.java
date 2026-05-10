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

import com.salonnbooking.api.dto.EmployeeRequests;
import com.salonnbooking.domain.Employee;
import com.salonnbooking.service.EmployeeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@GetMapping
	public List<EmployeeRequests.Response> list() {
		return employeeService.findAll().stream().map(EmployeeRequests.Response::from).toList();
	}

	@GetMapping("/active")
	public List<EmployeeRequests.Response> listActive() {
		return employeeService.findActive().stream().map(EmployeeRequests.Response::from).toList();
	}

	@GetMapping("/{id}")
	public EmployeeRequests.Response get(@PathVariable Integer id) {
		Employee employee = employeeService.findById(id);
		return EmployeeRequests.Response.from(employee);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EmployeeRequests.Response create(@Valid @RequestBody EmployeeRequests.Create req) {
		Employee employee = employeeService.save(req);
		return EmployeeRequests.Response.from(employee);
	}

	@PutMapping("/{id}")
	public EmployeeRequests.Response update(@PathVariable Integer id, @Valid @RequestBody EmployeeRequests.Update req) {
		Employee employee = employeeService.update(id, req);
		return EmployeeRequests.Response.from(employee);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Integer id) {
		employeeService.delete(id);
	}
}
