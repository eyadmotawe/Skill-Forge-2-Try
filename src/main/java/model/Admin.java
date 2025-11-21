package model;

public class Admin extends User {

    public Admin() {
        super();
        this.role = Role.ADMIN;
    }

    public Admin(String userId, String username, String email, String passwordHash) {
        super(userId, Role.ADMIN, username, email, passwordHash);
    }

    @Override
    public String getDashboardTitle() {
        return "Admin Dashboard - " + username;
    }

    public boolean canApproveCourses() {
        return true;
    }

    public boolean canManageUsers() {
        return true;
    }
}