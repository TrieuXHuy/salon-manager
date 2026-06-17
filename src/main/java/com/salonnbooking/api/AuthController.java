package com.salonnbooking.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.api.dto.AuthRequests;
import com.salonnbooking.domain.User;
import com.salonnbooking.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	/** Đăng ký tài khoản mới cho customer. */
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthRequests.Response register(@Valid @RequestBody AuthRequests.Register req) {
		User user = authService.register(req);
		return AuthRequests.Response.from(user, "Register successful");
	}

	/** Đăng nhập hệ thống. */
	@PostMapping("/login")
	public AuthRequests.Response login(@Valid @RequestBody AuthRequests.Login req) {
		User user = authService.login(req);
		return AuthRequests.Response.from(user, "Login successful");
	}

	/** Đăng xuất khỏi hệ thống. */
	@PostMapping("/logout")
	public AuthRequests.Message logout() {
		return new AuthRequests.Message("Logout successful");
	}

	/** Lấy danh sách user, chỉ owner mới được phép. */
	@GetMapping("/users")
	public List<AuthRequests.UserResponse> listUsers(@RequestParam String requesterUsername) {
		return authService.findAllUsers(requesterUsername).stream().map(AuthRequests.UserResponse::from).toList();
	}

	/** Tạo user mới, có thể kèm role tùy theo quyền người tạo. */
	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthRequests.UserResponse createUser(@Valid @RequestBody AuthRequests.CreateUser req) {
		return AuthRequests.UserResponse.from(authService.createUser(req));
	}

	/** Cập nhật thông tin user theo id. */
	@PutMapping("/users/{id}")
	public AuthRequests.UserResponse updateUser(@PathVariable Integer id,
			@Valid @RequestBody AuthRequests.UpdateUser req) {
		return AuthRequests.UserResponse.from(authService.updateUser(id, req));
	}

	/** Đổi role của user theo id. */
	@PutMapping("/users/{id}/role")
	public AuthRequests.UserResponse changeRole(@PathVariable Integer id,
			@Valid @RequestBody AuthRequests.ChangeRole req) {
		return AuthRequests.UserResponse.from(authService.changeRole(id, req));
	}

	/** Xóa user theo id. */
	@DeleteMapping("/users/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteUser(@PathVariable Integer id, @RequestParam String requesterUsername) {
		authService.deleteUser(id, requesterUsername);
	}
}
