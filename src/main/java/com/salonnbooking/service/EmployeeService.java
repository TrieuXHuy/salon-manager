package com.salonnbooking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.salonnbooking.api.dto.EmployeeRequests;
import com.salonnbooking.domain.Employee;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.EmployeeRepository;

@Service
@Transactional
public class EmployeeService {
	private final EmployeeRepository employeeRepository;

	public EmployeeService(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	@Transactional(readOnly = true)
	public List<Employee> findAll() {
		return employeeRepository.findAll();
	}

	@Transactional(readOnly = true)
	public List<Employee> findActive() {
		return employeeRepository.findByIsActiveTrue();
	}

	@Transactional(readOnly = true)
	public Employee findById(Integer id) {
		return employeeRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
	}

	public Employee save(EmployeeRequests.Create req) {
		Employee employee = new Employee();
		apply(employee, req.employeeCode(), req.fullName(), req.phone(), req.email(), req.specialization(),
				req.hireDate(), req.isActive());
		return employeeRepository.save(employee);
	}

	public Employee update(Integer id, EmployeeRequests.Update req) {
		Employee employee = findById(id);
		apply(employee, req.employeeCode(), req.fullName(), req.phone(), req.email(), req.specialization(),
				req.hireDate(), req.isActive());
		return employeeRepository.save(employee);
	}

	public void delete(Integer id) {
		if (!employeeRepository.existsById(id)) {
			throw new ResourceNotFoundException("Employee not found with id: " + id);
		}
		employeeRepository.deleteById(id);
	}

	private void apply(Employee employee, String employeeCode, String fullName, String phone, String email,
			String specialization, java.time.LocalDate hireDate, Boolean isActive) {
		employee.setEmployeeCode(employeeCode);
		employee.setFullName(fullName);
		employee.setPhone(phone);
		employee.setEmail(email);
		employee.setSpecialization(specialization);
		employee.setHireDate(hireDate);
		employee.setIsActive(isActive == null ? Boolean.TRUE : isActive);
	}
}
