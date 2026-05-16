package com.salonnbooking.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salonnbooking.domain.User;
import com.salonnbooking.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseCrudController<User> {

    public UserController(UserService service) {
        super(service);
    }
}
