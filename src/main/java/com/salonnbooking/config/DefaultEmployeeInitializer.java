package com.salonnbooking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.salonnbooking.domain.Employee;
import com.salonnbooking.repository.EmployeeRepository;

@Component
public class DefaultEmployeeInitializer implements CommandLineRunner {
	private final EmployeeRepository employeeRepository;

	public DefaultEmployeeInitializer(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	@Override
	public void run(String... args) {
		if (employeeRepository.count() > 0) {
			return;
		}

		employeeRepository.save(create("EMP001", "Nguyen Minh Anh", "Cắt tóc nữ"));
		employeeRepository.save(create("EMP002", "Tran Hoang Nam", "Nhuộm và tạo kiểu"));
		employeeRepository.save(create("EMP003", "Le Thu Ha", "Chăm sóc và phục hồi tóc"));
	}

	private Employee create(String code, String fullName, String specialization) {
		Employee employee = new Employee();
		employee.setEmployeeCode(code);
		employee.setFullName(fullName);
		employee.setPhone("0900000000");
		employee.setEmail(code.toLowerCase() + "@salon.local");
		employee.setSpecialization(specialization);
		employee.setIsActive(true);
		return employee;
	}
}
