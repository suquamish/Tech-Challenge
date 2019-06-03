package org.dieterich.WSECUTechChallenge.Controllers;

import org.dieterich.WSECUTechChallenge.DataAccess.UserService;
import org.dieterich.WSECUTechChallenge.Exceptions.NothingFoundException;
import org.dieterich.WSECUTechChallenge.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/{username}")
    public User getUserByUsername(@PathVariable String username) throws NothingFoundException {
        return userService.getUserByUsername(username);
    }

    @ExceptionHandler(NothingFoundException.class)
    public UserError handleNotFoundException(NothingFoundException nfe) {
        UserError result = new UserError();
        result.setErrorMessage("Cannot find any matching user");
        result.setStatusString("Not Found");
        return result;
    }
}
