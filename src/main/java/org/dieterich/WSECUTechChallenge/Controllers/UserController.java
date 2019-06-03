package org.dieterich.WSECUTechChallenge.Controllers;

import org.dieterich.WSECUTechChallenge.DataAccess.UserService;
import org.dieterich.WSECUTechChallenge.Exceptions.DuplicateUserException;
import org.dieterich.WSECUTechChallenge.Exceptions.NothingFoundException;
import org.dieterich.WSECUTechChallenge.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    protected class UserError {
        private String statusString;
        private String errorMessage;

        protected UserError() {}

        public UserError setStatusString(String statusString) {
            this.statusString = statusString;
            return this;
        }

        public String getStatusString() {
            return statusString;
        }

        public UserError setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    @GetMapping("/username/{username}")
    public User getUserByUsername(@PathVariable String username) throws NothingFoundException {
        return userService.getUserByUsername(username);
    }

    @GetMapping("/id/{userId}")
    public User getUserById(@PathVariable String userId) throws NothingFoundException {
        return userService.getUserById(userId);
    }

    @PostMapping("/create-new")
    public User createUser(@RequestBody User data) throws DuplicateUserException {
        return userService.createUser(data.getUsername(), data.getName(), data.getEmail());
    }

    @PostMapping("/update-existing")
    public User updateUser(@RequestBody User data) throws NothingFoundException {
        userService.updateUser(data);
        return userService.getUserById(data.getId());
    }

    @ExceptionHandler(NothingFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public UserError handleNotFoundException(NothingFoundException nfe) {
        UserError error = new UserError();
        error.setErrorMessage("Cannot find any matching user");
        error.setStatusString("Not Found");
        return error;
    }

    @ExceptionHandler(DuplicateUserException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public UserError handleDuplicateUserException(DuplicateUserException nfe) {
        UserError error = new UserError();
        error.setErrorMessage("User data provided must be unique");
        error.setStatusString("Unable to complete");
        return error;
    }
}
