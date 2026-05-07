package com.salonnbooking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.domain.Customer;
import com.salonnbooking.domain.Gender;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Tests")
public class CustomerServiceTest {

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private CustomerService customerService;

	private Customer testCustomer;

	@BeforeEach
	void setUp() {
		testCustomer = new Customer();
		testCustomer.setFullName("Test Customer");
		testCustomer.setPhone("0901234567");
		testCustomer.setEmail("test@example.com");
		testCustomer.setGender(Gender.male);
	}

	@Test
	@DisplayName("Should find all customers")
	void testFindAll() {
		List<Customer> customers = Arrays.asList(testCustomer);
		when(customerRepository.findAll()).thenReturn(customers);

		List<Customer> result = customerService.findAll();

		assertEquals(1, result.size());
		assertEquals("Test Customer", result.get(0).getFullName());
		verify(customerRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("Should find customer by ID")
	void testFindById() {
		when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));

		Customer result = customerService.findById(1);

		assertNotNull(result);
		assertEquals("Test Customer", result.getFullName());
		verify(customerRepository, times(1)).findById(1);
	}

	@Test
	@DisplayName("Should throw exception when customer not found")
	void testFindByIdNotFound() {
		when(customerRepository.findById(99)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> customerService.findById(99));
		verify(customerRepository, times(1)).findById(99);
	}

	@Test
	@DisplayName("Should save new customer")
	void testSave() {
		CustomerRequests.Create request = new CustomerRequests.Create(
				"New Customer", "0987654321", "new@example.com", Gender.female);

		when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

		Customer result = customerService.save(request);

		assertNotNull(result);
		verify(customerRepository, times(1)).save(any(Customer.class));
	}

	@Test
	@DisplayName("Should update customer")
	void testUpdate() {
		CustomerRequests.Update request = new CustomerRequests.Update(
				"Updated Customer", "0901234567", "updated@example.com", Gender.female);

		when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
		when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

		Customer result = customerService.update(1, request);

		assertNotNull(result);
		verify(customerRepository, times(1)).findById(1);
		verify(customerRepository, times(1)).save(any(Customer.class));
	}

	@Test
	@DisplayName("Should delete customer")
	void testDelete() {
		when(customerRepository.existsById(1)).thenReturn(true);

		customerService.delete(1);

		verify(customerRepository, times(1)).existsById(1);
		verify(customerRepository, times(1)).deleteById(1);
	}

	@Test
	@DisplayName("Should throw exception when deleting non-existent customer")
	void testDeleteNotFound() {
		when(customerRepository.existsById(99)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> customerService.delete(99));
		verify(customerRepository, times(1)).existsById(99);
		verify(customerRepository, never()).deleteById(99);
	}
}
