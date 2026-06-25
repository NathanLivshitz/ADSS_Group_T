package domain;

import java.util.LinkedList;
import java.util.List;

// import java.util.HashMap;
// import java.util.Map;

public class Shift {

    private String shiftType;
    private String day;
    private List<RoleRequirement> roleRequirements;
    private ShiftManager shiftManager;
    private Branch branch;

    public Shift(String shiftType, String day) {
        if (shiftType == null || day == null) {
            throw new IllegalArgumentException("invalid input");
        }

        this.shiftType = shiftType.trim().toLowerCase();
        this.day = day.trim().toLowerCase();

        if (!isValidShiftType(this.shiftType)) {
            throw new IllegalArgumentException("invalid shift type");
        }
     
        this.roleRequirements = new LinkedList<>();
        this.branch = null;
    }

    public void setDesignatedManager(Employee employee) {
        if (employee == null) {
            this.shiftManager = null;
            return;
        }
        if (!(employee instanceof ShiftManager)) {
            throw new IllegalArgumentException("Employee can't be a shift manager");
        }
        this.shiftManager = (ShiftManager) employee;
    }

    public String getShiftType() {
        return shiftType;
    }

    public String getDay() {
        return day;
    }

      public List<RoleRequirement> getRoleRequirements() {
        return roleRequirements;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public int numOfRoles(Role role) {
         for (RoleRequirement requirement : roleRequirements) {
            if (requirement.getRole().equals(role)) {
                return requirement.getAmount();
            }
        }
        return 0;
    }

    public void addRoleRequirement(Role role, Integer num) {
        if (role == null || num < 0 || num == null) {
            throw new IllegalArgumentException("invalid input");
        }
        for (RoleRequirement requirement : roleRequirements) {
            if (requirement.getRole().equals(role)) {
                requirement.setAmount(num);
                return;
            }
        }
        roleRequirements.add(new RoleRequirement(num, role));    
    }

    private boolean isValidShiftType(String shiftType) {
        return shiftType.equals("morning") || shiftType.equals("evening");
    }

    public ShiftManager getShiftManager() {
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
