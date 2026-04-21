package domain;

import java.util.HashMap;
import java.util.Map;

public class Shift {

    private String shiftType;
    private String day;
    private Map<Role, Integer> roleRequirements;
    private Employee shiftManager;

    public Shift(String shiftType, String day) {
        if (shiftType == null || day == null) {
            throw new IllegalArgumentException("invalid input");
        }

        this.shiftType = shiftType.trim().toLowerCase();
        this.day = day.trim().toLowerCase();

        if (!isValidShiftType(shiftType)) {
            throw new IllegalArgumentException("invalid shift type");
        }
     
        this.roleRequirements = new HashMap<>();
    }

    public void setDesignatedManager(Employee employee) {
        if (employee != null && !employee.isShiftManager()) {
            throw new IllegalArgumentException("Employee can't be a shift manager");
        }
        this.shiftManager = employee;
    }

    public String getShiftType() {
        return shiftType;
    }

    public String getDay() {
        return day;
    }

    public int numOfRoles(Role role) {
        Integer num = roleRequirements.get(role);
        if (num != null) {
            return num;
        }
        return 0;
    }

    public void addRoleRequirement(Role role, Integer num) {
        if (role == null || num < 0) {
            throw new IllegalArgumentException("invalid input");
        }
        roleRequirements.put(role, num);
    }

    private boolean isValidShiftType(String shiftType) {
        return shiftType.equals("morning") || shiftType.equals("evening");
    }

    public Employee getShiftManager() {
        return shiftManager;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Shift shift = (Shift) obj;
        return shiftType.equals(shift.shiftType) && day.equals(shift.day);
    }
}
