package dataAccess.DTO;

import java.util.ArrayList;
import java.util.List;

public class EmployeeDTO {
    public String id;
    public String name;
    public String bankAccount;
    public int active;
    public int isManager;
    public String branchId;
    public String employmentType;
    public String salaryType;
    public double salary;
    public long startDate;
    public int vacationDays;
    public String licenseType;

    public List<String> roles = new ArrayList<>();
    public List<AvailabilityInfo> availabilities = new ArrayList<>();

    public EmployeeDTO() {}

    public static class AvailabilityInfo {
        public String day;
        public String shiftType;

        public AvailabilityInfo() {}
        public AvailabilityInfo(String day, String shiftType) {
            this.day = day;
            this.shiftType = shiftType;
        }
    }
}