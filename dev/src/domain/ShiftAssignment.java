package domain;

public class ShiftAssignment {

    private Employee employee;
    private Shift shift;
    private Role role;

    public ShiftAssignment(Employee employee, Shift shift, Role role){
        this.employee = employee;
        this.shift = shift;
        this.role = role;
    }

    public Employee getEmployee(){
        return employee;
    }

    public Shift getShift(){
        return shift;
    }

    public Role getRole(){
        return role;
    }

    public void setRole(Role role){
        this.role = role;
    }

    public void setShift(Shift shift){
        this.shift = shift;
    }


}