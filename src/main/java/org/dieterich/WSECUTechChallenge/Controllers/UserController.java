package org.dieterich.WSECUTechChallenge.Controllers;

import org.dieterich.WSECUTechChallenge.Models.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{username}")
    public User getUserByUsername(@PathVariable String username) {
        return new User();
    }
}
