package com.salonnbooking.api;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salonnbooking.api.dto.CustomerRequests;
import com.salonnbooking.domain.Customer;
import com.salonnbooking.domain.Gender;
import com.salonnbooking.exception.ResourceNotFoundException;
import com.salonnbooking.service.CustomerService;

@WebMvcTest(CustomerController.class)
@DisplayName("Customer Controller Tests")
public class CustomerControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CustomerService customerService;

	@Autowired
	private ObjectMapper objectMapper;

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
	@DisplayName("GET /api/customers should return all customers")
	void testListCustomers() throws Exception {
		List<Customer> customers = Arrays.asList(testCustomer);
		when(customerService.findAll()).thenReturn(customers);

		mockMvc.perform(get("/api/customers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].fullName").value("Test Customer"));

		verify(customerService, times(1)).findAll();
	}

	@Test
	@DisplayName("GET /api/customers/{id} should return customer by ID")
	void testGetCustomerById() throws Exception {
		when(customerService.findById(1)).thenReturn(testCustomer);

		mockMvc.perform(get("/api/customers/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fullName").value("Test Customer"))
				.andExpect(jsonPath("$.phone").value("0901234567"));

		verify(customerService, times(1)).findById(1);
	}

	@Test
	@DisplayName("POST /api/customers should create new customer")
	void testCreateCustomer() throws Exception {
		CustomerRequests.Create request = new CustomerRequests.Create(
				"New Customer", "0987654321", "new@example.com", Gender.female);

		when(customerService.save(any())).thenReturn(testCustomer);

		mockMvc.perform(post("/api/customers")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.fullName").value("Test Customer"));

		verify(customerService, times(1)).save(any());
	}

	@Test
	@DisplayName("PUT /api/customers/{id} should update customer")
	void testUpdateCustomer() throws Exception {
		CustomerRequests.Update request = new CustomerRequests.Update(
				"Updated", "0901234567", "updated@example.com", Gender.male);

		when(customerService.update(eq(1), any())).thenReturn(testCustomer);

		mockMvc.perform(put("/api/customers/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());

		verify(customerService, times(1)).update(eq(1), any());
	}

	@Test
	@DisplayName("DELETE /api/customers/{id} should delete customer")
	void testDeleteCustomer() throws Exception {
		doNothing().when(customerService).delete(1);

		mockMvc.perform(delete("/api/customers/1"))
				.andExpect(status().isNoContent());

		verify(customerService, times(1)).delete(1);
	}

	@Test
	@DisplayName("GET /api/customers/{id} should return 404 when not found")
	void testGetCustomerNotFound() throws Exception {
		when(customerService.findById(99))
				.thenThrow(new ResourceNotFoundException("Customer not found with id: 99"));

		mockMvc.perform(get("/api/customers/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Not Found"));

		verify(customerService, times(1)).findById(99);
	}
}
