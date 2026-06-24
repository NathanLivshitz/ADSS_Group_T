package dataAccess.DTO;

import java.util.ArrayList;
import java.util.List;

public class ShiftDTO {
    public String day;
    public String shiftType;
    public String managerId;
    public String branchId;
    public List<String> requirementRoles = new ArrayList<>();
    public List<Integer> requirementAmounts = new ArrayList<>();

    public ShiftDTO() {}
}