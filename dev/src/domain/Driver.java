package domain;

public class Driver extends Employee {
    private String licenseType;

    public Driver(String name, String id, String bankAccount, EmploymentConditions employmentConditions,
                  String licenseType) {
        super(name, id, bankAccount, employmentConditions);
        if (licenseType == null || licenseType.trim().isEmpty()) {
            throw new IllegalArgumentException("license type cannot be null or empty");
        }
        this.licenseType = licenseType.trim();
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        if (licenseType == null || licenseType.trim().isEmpty()) {
            throw new IllegalArgumentException("license type cannot be empty");
        }

        this.licenseType = licenseType;
    }

    public boolean hasLicenseFor(String requiredLicense) {
        if (requiredLicense == null) {
            return false;
        }
        return licenseType.equalsIgnoreCase(requiredLicense.trim());
    }
}
