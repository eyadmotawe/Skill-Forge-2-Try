package utils;


import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MIN_USERNAME_LENGTH = 3;

    private ValidationUtils() {}

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    public static boolean isValidUsername(String username) {
        return username != null && username.length() >= MIN_USERNAME_LENGTH
                && username.matches("^[a-zA-Z0-9_]+$");
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static String validateSignup(String username, String email,
                                        String password, String confirmPassword) {
        if (!isNotEmpty(username)) {
            return "Username is required";
        }
        if (!isValidUsername(username)) {
            return "Username must be at least 3 characters (letters, numbers, underscore)";
        }
        if (!isNotEmpty(email)) {
            return "Email is required";
        }
        if (!isValidEmail(email)) {
            return "Invalid email format";
        }
        if (!isNotEmpty(password)) {
            return "Password is required";
        }
        if (!isValidPassword(password)) {
            return "Password must be at least 6 characters";
        }
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }
        return null;
    }

    public static String validateLogin(String email, String password) {
        if (!isNotEmpty(email)) {
            return "Email is required";
        }
        if (!isNotEmpty(password)) {
            return "Password is required";
        }
        return null;
    }

    public static String validateCourse(String title, String description) {
        if (!isNotEmpty(title)) {
            return "Course title is required";
        }
        if (title.length() < 5) {
            return "Course title must be at least 5 characters";
        }
        if (!isNotEmpty(description)) {
            return "Course description is required";
        }
        return null;
    }

    public static String validateLesson(String title, String content) {
        if (!isNotEmpty(title)) {
            return "Lesson title is required";
        }
        if (!isNotEmpty(content)) {
            return "Lesson content is required";
        }
        return null;
    }
}