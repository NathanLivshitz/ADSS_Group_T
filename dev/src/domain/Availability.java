package domain;

public class Availability {

    private String day;
    private String shiftType;

    public Availability(String day, String shift) {
        if (day == null || shift == null) {
            throw new IllegalArgumentException("invalid input");
        }
        this.day = day.trim().toLowerCase();
        this.shiftType = shift.trim().toLowerCase();
    }

    public String getDay() {
        return day;
    }

    public String getShiftType() {
        return shiftType;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Availability other = (Availability) obj;
        return day.equals(other.day) && shiftType.equals(other.shiftType);
    }
}
