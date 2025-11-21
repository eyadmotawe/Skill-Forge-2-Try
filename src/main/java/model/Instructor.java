package model;

import java.util.ArrayList;
import java.util.List;

public class Instructor extends User {
    private List<String> createdCourses;

    public Instructor() {
        super();
        this.role = Role.INSTRUCTOR;
        this.createdCourses = new ArrayList<>();
    }

    public Instructor(String userId, String username, String email, String passwordHash) {
        super(userId, Role.INSTRUCTOR, username, email, passwordHash);
        this.createdCourses = new ArrayList<>();
    }

    public List<String> getCreatedCourses() { return createdCourses; }
    public void setCreatedCourses(List<String> createdCourses) { this.createdCourses = createdCourses; }

    public void addCreatedCourse(String courseId) {
        if (!createdCourses.contains(courseId)) {
            createdCourses.add(courseId);
        }
    }

    public void removeCreatedCourse(String courseId) {
        createdCourses.remove(courseId);
    }

    @Override
    public String getDashboardTitle() {
        return "Instructor Dashboard - " + username;
    }
}