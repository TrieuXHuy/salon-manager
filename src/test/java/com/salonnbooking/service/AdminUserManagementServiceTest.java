package com.salonnbooking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.salonnbooking.api.dto.AdminUserDtos;
import com.salonnbooking.domain.Gender;
import com.salonnbooking.domain.Role;
import com.salonnbooking.domain.User;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.StaffServiceRepository;
import com.salonnbooking.repository.StaffWorkingHourRepository;
import com.salonnbooking.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Admin User Management Service Tests")
class AdminUserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private StaffServiceRepository staffServiceRepository;

    @Mock
    private StaffWorkingHourRepository staffWorkingHourRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserManagementService service;

    @Test
    void createCustomerCreatesUserWithCustomerRole() {
        AdminUserDtos.CreateCustomerRequest request = new AdminUserDtos.CreateCustomerRequest(
                "Customer", "customer@example.com", "0900000001", "secret", Gender.FEMALE, true);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        AdminUserDtos.UserResponse response = service.createCustomer(request);

        assertEquals(Role.CUSTOMER, response.role());
        assertEquals("customer@example.com", response.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteCustomerRejectsExistingAppointments() {
        User customer = User.builder().id(1L).role(Role.CUSTOMER).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(appointmentRepository.existsByCustomerId(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.deleteCustomer(1L));

        verify(userRepository, never()).delete(any());
    }
}
