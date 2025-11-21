package database;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.*;
import utils.IdGenerator;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class JsonDatabaseManager {
    private static JsonDatabaseManager instance;
    private final Gson gson;
    private List<User> users;
    private List<Course> courses;
    private Path usersFilePath;
    private Path coursesFilePath;

    private JsonDatabaseManager() {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(User.class, new UserDeserializer())
                .setPrettyPrinting()
                .create();
        users = new ArrayList<>();
        courses = new ArrayList<>();
        initializeFilePaths();
        loadData();
    }

    public static synchronized JsonDatabaseManager getInstance() {
        if (instance == null) {
            instance = new JsonDatabaseManager();
        }
        return instance;
    }

    private void initializeFilePaths() {
        String userHome = System.getProperty("user.home");
        Path dataDir = Paths.get(userHome, ".skillforge");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        usersFilePath = dataDir.resolve("users.json");
        coursesFilePath = dataDir.resolve("courses.json");
    }

    private void loadData() {
        loadUsers();
        loadCourses();
        initializeIdCounters();
    }

    private void loadUsers() {
        try {
            if (Files.exists(usersFilePath)) {
                String content = Files.readString(usersFilePath);
                parseUsersJson(content);
            } else {
                InputStream is = getClass().getResourceAsStream("/users.json");
                if (is != null) {
                    String content = new String(is.readAllBytes());
                    parseUsersJson(content);
                    saveUsers();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            users = new ArrayList<>();
        }
    }

    private void parseUsersJson(String content) {
        JsonObject root = JsonParser.parseString(content).getAsJsonObject();
        JsonArray usersArray = root.getAsJsonArray("users");
        users = new ArrayList<>();
        for (JsonElement el : usersArray) {
            User user = gson.fromJson(el, User.class);
            users.add(user);
        }
    }

    private void loadCourses() {
        try {
            if (Files.exists(coursesFilePath)) {
                String content = Files.readString(coursesFilePath);
                parseCoursesJson(content);
            } else {
                InputStream is = getClass().getResourceAsStream("/courses.json");
                if (is != null) {
                    String content = new String(is.readAllBytes());
                    parseCoursesJson(content);
                    saveCourses();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            courses = new ArrayList<>();
        }
    }

    private void parseCoursesJson(String content) {
        JsonObject root = JsonParser.parseString(content).getAsJsonObject();
        JsonArray coursesArray = root.getAsJsonArray("courses");
        Type listType = new TypeToken<List<Course>>(){}.getType();
        courses = gson.fromJson(coursesArray, listType);
        if (courses == null) courses = new ArrayList<>();
    }

    private void initializeIdCounters() {
        int maxUser = 0, maxCourse = 0, maxLesson = 0, maxQuiz = 0, maxQuestion = 0, maxCert = 0;
        for (User u : users) {
            maxUser = Math.max(maxUser, extractNum(u.getUserId(), "USR"));
            for (Certificate c : u.getCertificates()) {
                maxCert = Math.max(maxCert, extractNum(c.getCertificateId(), "CERT"));
            }
        }
        for (Course c : courses) {
            maxCourse = Math.max(maxCourse, extractNum(c.getCourseId(), "CRS"));
            for (Lesson l : c.getLessons()) {
                maxLesson = Math.max(maxLesson, extractNum(l.getLessonId(), "LSN"));
                if (l.getQuiz() != null) {
                    maxQuiz = Math.max(maxQuiz, extractNum(l.getQuiz().getQuizId(), "QZ"));
                    for (Question q : l.getQuiz().getQuestions()) {
                        maxQuestion = Math.max(maxQuestion, extractNum(q.getQuestionId(), "Q"));
                    }
                }
            }
        }
        IdGenerator.initializeCounters(maxUser, maxCourse, maxLesson, maxQuiz, maxQuestion, maxCert);
    }

    private int extractNum(String id, String prefix) {
        try {
            return Integer.parseInt(id.replace(prefix, ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public synchronized void saveUsers() {
        try {
            JsonObject root = new JsonObject();
            root.add("users", gson.toJsonTree(users));
            Files.writeString(usersFilePath, gson.toJson(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void saveCourses() {
        try {
            JsonObject root = new JsonObject();
            root.add("courses", gson.toJsonTree(courses));
            Files.writeString(coursesFilePath, gson.toJson(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // User operations
    public User findUserByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst().orElse(null);
    }

    public User findUserById(String userId) {
        return users.stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst().orElse(null);
    }

    public boolean emailExists(String email) {
        return findUserByEmail(email) != null;
    }

    public void addUser(User user) {
        users.add(user);
        saveUsers();
    }

    public void updateUser(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(user.getUserId())) {
                users.set(i, user);
                break;
            }
        }
        saveUsers();
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public List<Student> getAllStudents() {
        return users.stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .collect(Collectors.toList());
    }

    public List<Instructor> getAllInstructors() {
        return users.stream()
                .filter(u -> u instanceof Instructor)
                .map(u -> (Instructor) u)
                .collect(Collectors.toList());
    }

    // Course operations
    public void addCourse(Course course) {
        courses.add(course);
        saveCourses();
    }

    public void updateCourse(Course course) {
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getCourseId().equals(course.getCourseId())) {
                courses.set(i, course);
                break;
            }
        }
        saveCourses();
    }

    public void deleteCourse(String courseId) {
        courses.removeIf(c -> c.getCourseId().equals(courseId));
        saveCourses();
    }

    public Course findCourseById(String courseId) {
        return courses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst().orElse(null);
    }

    public List<Course> getAllCourses() {
        return new ArrayList<>(courses);
    }

    public List<Course> getApprovedCourses() {
        return courses.stream()
                .filter(c -> c.getApprovalStatus() == ApprovalStatus.APPROVED)
                .collect(Collectors.toList());
    }

    public List<Course> getPendingCourses() {
        return courses.stream()
                .filter(c -> c.getApprovalStatus() == ApprovalStatus.PENDING)
                .collect(Collectors.toList());
    }

    public List<Course> getCoursesByInstructor(String instructorId) {
        return courses.stream()
                .filter(c -> c.getInstructorId().equals(instructorId))
                .collect(Collectors.toList());
    }

    public List<Course> getEnrolledCourses(String studentId) {
        return courses.stream()
                .filter(c -> c.getStudents().contains(studentId))
                .collect(Collectors.toList());
    }

    // Adapters
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>,
            JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime src, Type t, JsonSerializationContext ctx) {
            return new JsonPrimitive(src.format(fmt));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type t, JsonDeserializationContext ctx) {
            return LocalDateTime.parse(json.getAsString(), fmt);
        }
    }

    private class UserDeserializer implements JsonDeserializer<User> {
        @Override
        public User deserialize(JsonElement json, Type t, JsonDeserializationContext ctx) {
            JsonObject obj = json.getAsJsonObject();
            String roleStr = obj.get("role").getAsString();
            Role role = Role.valueOf(roleStr);

            User user;
            switch (role) {
                case ADMIN -> user = new Admin();
                case INSTRUCTOR -> user = new Instructor();
                default -> user = new Student();
            }

            user.setUserId(obj.get("userId").getAsString());
            user.setRole(role);
            user.setUsername(obj.get("username").getAsString());
            user.setEmail(obj.get("email").getAsString());
            user.setPasswordHash(obj.get("passwordHash").getAsString());

            if (obj.has("createdAt")) {
                user.setCreatedAt(LocalDateTime.parse(obj.get("createdAt").getAsString()));
            }
            if (obj.has("enrolledCourses")) {
                Type listType = new TypeToken<List<String>>(){}.getType();
                user.setEnrolledCourses(gson.fromJson(obj.get("enrolledCourses"), listType));
            }
            if (obj.has("progress")) {
                Type mapType = new TypeToken<Map<String, CourseProgress>>(){}.getType();
                user.setProgress(gson.fromJson(obj.get("progress"), mapType));
            }
            if (obj.has("quizAttempts")) {
                Type listType = new TypeToken<List<QuizAttempt>>(){}.getType();
                user.setQuizAttempts(gson.fromJson(obj.get("quizAttempts"), listType));
            }
            if (obj.has("certificates")) {
                Type listType = new TypeToken<List<Certificate>>(){}.getType();
                user.setCertificates(gson.fromJson(obj.get("certificates"), listType));
            }
            if (user instanceof Instructor && obj.has("createdCourses")) {
                Type listType = new TypeToken<List<String>>(){}.getType();
                ((Instructor) user).setCreatedCourses(gson.fromJson(obj.get("createdCourses"), listType));
            }
            return user;
        }
    }
}