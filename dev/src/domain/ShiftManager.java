package domain;

public class ShiftManager extends Employee {

    public ShiftManager(String name, String id, String bankAccount,
                        EmploymentConditions employmentConditions) {
        super(name, id, bankAccount, employmentConditions);
    }

    public ShiftManager(Employee employee) {
        super(employee.getName(),
              employee.getId(),
              employee.getBankAccount(),
              employee.getEmploymentConditions());

        setActive(employee.isActive());

        for (Role role : employee.getRoles()) {
            addRole(role);
        }

        for (Availability availability : employee.getAvailabilities()) {
            addAvailability(availability);
        }
    }
}