package domain;

import java.util.LinkedList;
import java.util.List;

public class Employee {

    private String name;
    private String id;
    private String bankAccount;
    private EmploymentConditions employmentConditions;
    private List<Role> roles;
    private List<Availability> availabilities;
    private boolean active;
    private boolean isShiftManager;

    public Employee(String name, String id, String bankAccount, EmploymentConditions employmentConditions) {
        this.name = name;
        this.id = id;
        this.bankAccount = bankAccount;
        this.employmentConditions = employmentConditions;
        this.availabilities = new LinkedList<Availability>();
        this.roles = new LinkedList<Role>();
        this.active = true;
        this.isShiftManager = false;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public EmploymentConditions getEmploymentConditions() {
        return employmentConditions;
    }

    public void setEmploymentConditions(EmploymentConditions employmentConditions) {
        this.employmentConditions = employmentConditions;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public List<Availability> getAvailabilities() {
        return availabilities;
    }

    public void addAvailability(Availability availability) {
        if (availability == null) {
            throw new IllegalArgumentException("availability cannot be null");
        }
        if (availabilities.contains(availability)) {
            throw new IllegalArgumentException("availability already exists");
        }
        getAvailabilities().add(availability);
    }

    public boolean isShiftManager() {
        return isShiftManager;
    }

    public void setShiftManager(boolean shiftManager) {
        this.isShiftManager = shiftManager;
    }

    public boolean containsRole(Role role) {
        return roles.contains(role);
    }

    public boolean containsAvailability(Availability availability) {
        return availabilities.contains(availability);
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public void setAvailabilities(List<Availability> availability) {
        this.availabilities = new LinkedList<>(availability);
    }

    public void setRoles(List<Role> role) {
        this.roles = new LinkedList<>(role);
    }

    public void addRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }
        if (roles.contains(role)) {
            throw new IllegalArgumentException("role already exists");
        }
        roles.add(role);
    }

    public void removeRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("role cannot be null");
        }
        if (!roles.contains(role)) {
            throw new IllegalArgumentException("role does not exist");
        }
        roles.remove(role);
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Employee other = (Employee) obj;
        return id.equals(other.id);
    }

}