package org.dieterich.TechChallenge.Controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.dieterich.TechChallenge.DataAccess.UserService;
import org.dieterich.TechChallenge.Exceptions.DuplicateUserException;
import org.dieterich.TechChallenge.Exceptions.NothingFoundException;
import org.dieterich.TechChallenge.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping(produces = "application/json")
    @ApiOperation("Creates a new user.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = User.class),
            @ApiResponse(code = 400, message = "Bad Request", response = UserError.class )
    })
    public User createUser(@RequestBody User userData) throws DuplicateUserException {
        return userService.createUser(userData.getUsername(), userData.getName(), userData.getEmail());
    }

    @GetMapping(path = "/username/{username}", produces = "application/json")
    @ApiOperation("Retrieves an existing user by username")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = User.class),
            @ApiResponse(code = 404, message = "Not Found", response = UserError.class )
    })
    public User getUserByUsername(@PathVariable String username) throws NothingFoundException {
        return userService.getUserByUsername(username);
    }

    @GetMapping(path = "/id/{userId}", produces = "application/json")
    @ApiOperation("Retrieves an existing user by id.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = User.class),
            @ApiResponse(code = 404, message = "Not Found", response = UserError.class )
    })
    public User getUserById(@PathVariable String userId) throws NothingFoundException {
        return userService.getUserById(userId);
    }

    @PostMapping(path = "/id/{userId}", produces = "application/json")
    @ApiOperation("Updates an existing user.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = User.class),
            @ApiResponse(code = 404, message = "Not Found", response = UserError.class )
    })
    public User updateUser(@RequestBody User userData, @PathVariable String userId) throws NothingFoundException {
        userData.setId(userId);
        userService.updateUser(userData);
        return userService.getUserById(userData.getId());
    }

    @DeleteMapping(path = "/id/{userId}")
    @ApiOperation("Deletes an existing user.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "No Content", response = void.class)
    })
    public void deleteUser(@PathVariable String userId) {
        userService.deleteUserById(userId);
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
}
