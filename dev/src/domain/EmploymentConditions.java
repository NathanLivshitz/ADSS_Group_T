package domain;

import java.util.Date;

public class EmploymentConditions {

    private String employmentType;
    private String salaryType;
    private double salary;
    private Date startDate;
    private int vacationDays;

    public EmploymentConditions(String employmentType, String salaryType, double salary,Date startDate, int vacationDays){
        this.employmentType = employmentType;
        this.salaryType = salaryType;
        this.salary = salary;
        this.startDate = startDate;
        this.vacationDays = vacationDays;
    }

    public String getEmploymentType(){
        return employmentType;
    }

    public String getSalaryType(){
        return salaryType;
    }

    public double getSalary(){
        return salary;
    }

    public Date getStartDate(){
        return startDate;
    }

    public int getVacationDays(){
        return vacationDays;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public void setSalaryType(String salaryType) {
        this.salaryType = salaryType;
    }

    public void setSalary(double salary) {
        if (salary < 0) {
            throw new IllegalArgumentException("salary must be greater than or equal to 0");
        }
        this.salary = salary;
    }

    public void setVacationDays(int vacationDays) {
        if (vacationDays < 0) {
            throw new IllegalArgumentException("vacation days must be greater than or equal to 0");
        }
        this.vacationDays = vacationDays;
    }

    public String toString() {
        return "employmentType='" + employmentType + '\'' +
                ", salaryType='" + salaryType + '\'' +
                ", salary=" + salary +
                ", startDate=" + startDate +
                ", vacationDays=" + vacationDays;
    }
}
