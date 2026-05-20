package com.salonnbooking.config;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.salonnbooking.domain.Appointment;
import com.salonnbooking.domain.AppointmentService;
import com.salonnbooking.domain.AppointmentStatus;
import com.salonnbooking.domain.Gender;
import com.salonnbooking.domain.Payment;
import com.salonnbooking.domain.PaymentMethod;
import com.salonnbooking.domain.PaymentStatus;
import com.salonnbooking.domain.Review;
import com.salonnbooking.domain.Role;
import com.salonnbooking.domain.Service;
import com.salonnbooking.domain.ServiceCategory;
import com.salonnbooking.domain.StaffService;
import com.salonnbooking.domain.StaffWorkingHour;
import com.salonnbooking.domain.User;
import com.salonnbooking.repository.AppointmentRepository;
import com.salonnbooking.repository.AppointmentServiceRepository;
import com.salonnbooking.repository.PaymentRepository;
import com.salonnbooking.repository.ReviewRepository;
import com.salonnbooking.repository.ServiceCategoryRepository;
import com.salonnbooking.repository.ServiceRepository;
import com.salonnbooking.repository.StaffServiceRepository;
import com.salonnbooking.repository.StaffWorkingHourRepository;
import com.salonnbooking.repository.UserRepository;

@Configuration
public class SampleDataInitializer {

    @Bean
    @Order(2)
    ApplicationRunner sampleDataRunner(
            UserRepository userRepository,
            ServiceCategoryRepository serviceCategoryRepository,
            ServiceRepository serviceRepository,
            StaffServiceRepository staffServiceRepository,
            StaffWorkingHourRepository staffWorkingHourRepository,
            AppointmentRepository appointmentRepository,
            AppointmentServiceRepository appointmentServiceRepository,
            PaymentRepository paymentRepository,
            ReviewRepository reviewRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            LocalDateTime now = LocalDateTime.now();

            ServiceCategory hair = findOrCreateCategory(
                    serviceCategoryRepository,
                    "Tóc",
                    "Cắt, gội, uốn, nhuộm và phục hồi tóc");
            ServiceCategory nails = findOrCreateCategory(
                    serviceCategoryRepository,
                    "Nail",
                    "Chăm sóc móng tay và móng chân");
            ServiceCategory skincare = findOrCreateCategory(
                    serviceCategoryRepository,
                    "Chăm sóc da",
                    "Dịch vụ chăm sóc da mặt cơ bản");

            Service haircut = findOrCreateService(serviceRepository, hair, "Cắt tóc nam",
                    "Tư vấn kiểu tóc và cắt tạo form", "120000", 45, now);
            Service shampoo = findOrCreateService(serviceRepository, hair, "Gội đầu thảo dược",
                    "Gội, massage da đầu và sấy tạo kiểu", "90000", 40, now);
            Service koreanPerm = findOrCreateService(serviceRepository, hair, "Uốn tóc Hàn Quốc",
                    "Uốn setting phong cách Hàn Quốc", "850000", 150, now);
            Service color = findOrCreateService(serviceRepository, hair, "Nhuộm tóc + Phục hồi",
                    "Nhuộm màu thời trang kèm phục hồi", "650000", 120, now);
            Service manicure = findOrCreateService(serviceRepository, nails, "Sơn gel cao cấp",
                    "Làm sạch móng và sơn gel", "180000", 60, now);
            Service facial = findOrCreateService(serviceRepository, skincare, "Chăm sóc da mặt",
                    "Làm sạch, massage và đắp mặt nạ", "300000", 75, now);

            User staffA = findOrCreateUser(userRepository, passwordEncoder,
                    "staff.an@example.com", "Nguyễn Minh An", "0911000001", Gender.MALE, Role.STAFF, now);
            User staffB = findOrCreateUser(userRepository, passwordEncoder,
                    "staff.binh@example.com", "Trần Bình", "0911000002", Gender.MALE, Role.STAFF, now);
            User staffC = findOrCreateUser(userRepository, passwordEncoder,
                    "staff.thao@example.com", "Lê Thảo", "0911000003", Gender.FEMALE, Role.STAFF, now);

            User customerA = findOrCreateUser(userRepository, passwordEncoder,
                    "nguyenvana@example.com", "Nguyễn Văn A", "0901000001", Gender.MALE, Role.CUSTOMER, now);
            User customerB = findOrCreateUser(userRepository, passwordEncoder,
                    "tranthib@example.com", "Trần Thị B", "0901000002", Gender.FEMALE, Role.CUSTOMER, now);
            User customerC = findOrCreateUser(userRepository, passwordEncoder,
                    "levanm@example.com", "Lê Văn M", "0901000003", Gender.MALE, Role.CUSTOMER, now);
            User customerD = findOrCreateUser(userRepository, passwordEncoder,
                    "phamhoangnam@example.com", "Phạm Hoàng Nam", "0901000004", Gender.MALE, Role.CUSTOMER, now);

            assignServices(staffServiceRepository, staffA, List.of(haircut, shampoo, koreanPerm, color));
            assignServices(staffServiceRepository, staffB, List.of(haircut, shampoo, koreanPerm, color, facial));
            assignServices(staffServiceRepository, staffC, List.of(shampoo, manicure, facial));

            addWorkingWeek(staffWorkingHourRepository, staffA);
            addWorkingWeek(staffWorkingHourRepository, staffB);
            addWorkingWeek(staffWorkingHourRepository, staffC);

            if (appointmentRepository.count() == 0) {
                createAppointment(
                        appointmentRepository,
                        appointmentServiceRepository,
                        paymentRepository,
                        reviewRepository,
                        customerA,
                        staffA,
                        LocalDate.now().with(DayOfWeek.MONDAY).atTime(10, 0),
                        AppointmentStatus.COMPLETED,
                        PaymentMethod.CASH,
                        PaymentStatus.PAID,
                        5,
                        "Dịch vụ tốt, nhân viên tư vấn kỹ.",
                        haircut);
                createAppointment(
                        appointmentRepository,
                        appointmentServiceRepository,
                        paymentRepository,
                        reviewRepository,
                        customerB,
                        staffB,
                        LocalDate.now().with(DayOfWeek.TUESDAY).atTime(11, 30),
                        AppointmentStatus.IN_PROGRESS,
                        PaymentMethod.MOMO,
                        PaymentStatus.PAID,
                        null,
                        null,
                        color);
                createAppointment(
                        appointmentRepository,
                        appointmentServiceRepository,
                        paymentRepository,
                        reviewRepository,
                        customerC,
                        staffC,
                        LocalDate.now().plusDays(1).atTime(14, 0),
                        AppointmentStatus.CONFIRMED,
                        null,
                        PaymentStatus.UNPAID,
                        null,
                        null,
                        shampoo);
                createAppointment(
                        appointmentRepository,
                        appointmentServiceRepository,
                        paymentRepository,
                        reviewRepository,
                        customerD,
                        staffB,
                        LocalDate.now().plusDays(2).atTime(16, 0),
                        AppointmentStatus.PENDING,
                        null,
                        PaymentStatus.UNPAID,
                        null,
                        null,
                        koreanPerm);
                createAppointment(
                        appointmentRepository,
                        appointmentServiceRepository,
                        paymentRepository,
                        reviewRepository,
                        customerB,
                        staffC,
                        LocalDate.now().minusDays(2).atTime(9, 30),
                        AppointmentStatus.COMPLETED,
                        PaymentMethod.CARD,
                        PaymentStatus.PAID,
                        4,
                        "Không gian sạch sẽ, sẽ quay lại.",
                        facial);
            }
        };
    }

    private ServiceCategory findOrCreateCategory(
            ServiceCategoryRepository repository,
            String name,
            String description) {
        return repository.findByName(name)
                .orElseGet(() -> repository.save(ServiceCategory.builder()
                        .name(name)
                        .description(description)
                        .isActive(true)
                        .build()));
    }

    private Service findOrCreateService(
            ServiceRepository repository,
            ServiceCategory category,
            String name,
            String description,
            String price,
            int durationMinutes,
            LocalDateTime now) {
        return repository.findByName(name)
                .orElseGet(() -> repository.save(Service.builder()
                        .category(category)
                        .name(name)
                        .description(description)
                        .price(new BigDecimal(price))
                        .durationMinutes(durationMinutes)
                        .isActive(true)
                        .createdAt(now)
                        .build()));
    }

    private User findOrCreateUser(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            String email,
            String fullName,
            String phone,
            Gender gender,
            Role role,
            LocalDateTime now) {
        return repository.findByEmail(email)
                .orElseGet(() -> repository.save(User.builder()
                        .fullName(fullName)
                        .email(email)
                        .phone(phone)
                        .password(passwordEncoder.encode("123456"))
                        .gender(gender)
                        .role(role)
                        .isActive(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()));
    }

    private void assignServices(
            StaffServiceRepository repository,
            User staff,
            List<Service> services) {
        for (Service service : services) {
            repository.findByStaffIdAndServiceId(staff.getId(), service.getId())
                    .orElseGet(() -> repository.save(StaffService.builder()
                            .staff(staff)
                            .service(service)
                            .build()));
        }
    }

    private void addWorkingWeek(StaffWorkingHourRepository repository, User staff) {
        if (!repository.findByStaffId(staff.getId()).isEmpty()) {
            return;
        }
        for (DayOfWeek day : List.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY)) {
            repository.save(StaffWorkingHour.builder()
                    .staff(staff)
                    .dayOfWeek(day)
                    .startTime(LocalTime.of(8, 30))
                    .endTime(LocalTime.of(18, 0))
                    .isActive(true)
                    .build());
        }
    }

    private void createAppointment(
            AppointmentRepository appointmentRepository,
            AppointmentServiceRepository appointmentServiceRepository,
            PaymentRepository paymentRepository,
            ReviewRepository reviewRepository,
            User customer,
            User staff,
            LocalDateTime start,
            AppointmentStatus status,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            Integer rating,
            String comment,
            Service service) {
        LocalDateTime now = LocalDateTime.now();
        Appointment appointment = Appointment.builder()
                .customer(customer)
                .staff(staff)
                .appointmentStart(start)
                .appointmentEnd(start.plusMinutes(service.getDurationMinutes()))
                .status(status)
                .note("Dữ liệu mẫu")
                .totalAmount(service.getPrice())
                .createdAt(now)
                .updatedAt(now)
                .build();
        appointment = appointmentRepository.save(appointment);

        appointmentServiceRepository.save(AppointmentService.builder()
                .appointment(appointment)
                .service(service)
                .priceSnapshot(service.getPrice())
                .durationSnapshot(service.getDurationMinutes())
                .build());

        paymentRepository.save(Payment.builder()
                .appointment(appointment)
                .amount(service.getPrice())
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .paidAt(paymentStatus == PaymentStatus.PAID ? now : null)
                .createdAt(now)
                .build());

        if (rating != null) {
            reviewRepository.save(Review.builder()
                    .appointment(appointment)
                    .customer(customer)
                    .staff(staff)
                    .rating(rating)
                    .comment(comment)
                    .createdAt(now)
                    .build());
        }
    }
}
