package org.dieterich.TechChallenge.Models;

public class UserError {
    private String statusString;
    private String errorMessage;

    public UserError() {}

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
