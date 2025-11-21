package auth;

import database.JsonDatabaseManager;
import model.*;
import utils.IdGenerator;
import utils.PasswordUtils;
import utils.ValidationUtils;

public class AuthService {
    private static AuthService instance;
    private final JsonDatabaseManager db;
    private User currentUser;

    private AuthService() {
        db = JsonDatabaseManager.getInstance();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public AuthResult signup(String username, String email, String password,
                             String confirmPassword, Role role) {
        String validationError = ValidationUtils.validateSignup(username, email, password, confirmPassword);
        if (validationError != null) {
            return new AuthResult(false, validationError, null);
        }

        if (db.emailExists(email)) {
            return new AuthResult(false, "Email already registered", null);
        }

        String passwordHash = PasswordUtils.hashPassword(password);
        String userId = IdGenerator.generateUserId();

        User newUser;
        switch (role) {
            case INSTRUCTOR -> newUser = new Instructor(userId, username, email, passwordHash);
            case ADMIN -> newUser = new Admin(userId, username, email, passwordHash);
            default -> newUser = new Student(userId, username, email, passwordHash);
        }

        db.addUser(newUser);
        this.currentUser = newUser;
        return new AuthResult(true, "Registration successful", newUser);
    }

    public AuthResult login(String email, String password) {
        String validationError = ValidationUtils.validateLogin(email, password);
        if (validationError != null) {
            return new AuthResult(false, validationError, null);
        }

        User user = db.findUserByEmail(email);
        if (user == null) {
            return new AuthResult(false, "Email not found", null);
        }

        if (!PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            return new AuthResult(false, "Incorrect password", null);
        }

        this.currentUser = user;
        return new AuthResult(true, "Login successful", user);
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isStudent() {
        return currentUser instanceof Student;
    }

    public boolean isInstructor() {
        return currentUser instanceof Instructor;
    }

    public boolean isAdmin() {
        return currentUser instanceof Admin;
    }

    public void refreshCurrentUser() {
        if (currentUser != null) {
            currentUser = db.findUserById(currentUser.getUserId());
        }
    }

    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final User user;

        public AuthResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
    }
}