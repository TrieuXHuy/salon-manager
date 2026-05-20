package com.salonnbooking.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salonnbooking.api.dto.AdminUserDtos;
import com.salonnbooking.domain.Gender;
import com.salonnbooking.domain.Role;
import com.salonnbooking.security.TokenAuthenticationFilter;
import com.salonnbooking.service.AdminUserManagementService;

@WebMvcTest(AdminCustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Admin Customer Controller Tests")
class AdminCustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminUserManagementService adminUserManagementService;

    @MockitoBean
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @Test
    void findAllReturnsCustomers() throws Exception {
        when(adminUserManagementService.getCustomers()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/admin/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("customer@example.com"))
                .andExpect(jsonPath("$[0].role").value("CUSTOMER"));

        verify(adminUserManagementService).getCustomers();
    }

    @Test
    void createDelegatesToService() throws Exception {
        AdminUserDtos.CreateCustomerRequest request = new AdminUserDtos.CreateCustomerRequest(
                "Customer", "customer@example.com", "0900000001", "secret", Gender.FEMALE, true);
        when(adminUserManagementService.createCustomer(any())).thenReturn(response());

        mockMvc.perform(post("/api/admin/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("customer@example.com"));

        verify(adminUserManagementService).createCustomer(any());
    }

    @Test
    void updateDelegatesToService() throws Exception {
        AdminUserDtos.UpdateUserRequest request = new AdminUserDtos.UpdateUserRequest(
                "Updated", "customer@example.com", "0900000002", Gender.OTHER, true);
        when(adminUserManagementService.updateCustomer(eq(1L), any())).thenReturn(response());

        mockMvc.perform(put("/api/admin/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(adminUserManagementService).updateCustomer(eq(1L), any());
    }

    @Test
    void deleteDelegatesToService() throws Exception {
        doNothing().when(adminUserManagementService).deleteCustomer(1L);

        mockMvc.perform(delete("/api/admin/customers/1"))
                .andExpect(status().isNoContent());

        verify(adminUserManagementService).deleteCustomer(1L);
    }

    private AdminUserDtos.UserResponse response() {
        LocalDateTime now = LocalDateTime.now();
        return new AdminUserDtos.UserResponse(
                1L,
                "Customer",
                "customer@example.com",
                "0900000001",
                Gender.FEMALE,
                Role.CUSTOMER,
                true,
                now,
                now);
    }
}
