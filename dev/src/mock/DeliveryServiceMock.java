package mock;

import domain.Shift;

public class DeliveryServiceMock {

    public DeliveryServiceMock() {}

    public boolean hasDeliveryInShift(Shift shift) {
        if (shift == null) {
            throw new IllegalArgumentException("shift cannot be null");
        }

        return shift.getDay().equalsIgnoreCase("Sunday")
                && shift.getShiftType().equalsIgnoreCase("morning");
    }

    public String getTruckLicenseForDelivery(Shift shift) {
        if (shift == null) {
            throw new IllegalArgumentException("shift cannot be null");
        }
        return "B";
    }

    public String getDriverIdForDelivery(Shift shift) {
        if (shift == null) {
            throw new IllegalArgumentException("shift cannot be null");
        }
        if (!hasDeliveryInShift(shift)) {
            return null;
        }
        return "1010";
    }
}
