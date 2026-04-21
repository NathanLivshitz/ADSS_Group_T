package domain;

public class Role {

    private String roleName;

    public Role(String roleName) {
          if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("role name cannot be null or empty");
        }
        this.roleName = roleName.trim();
    }

    public String getRoleName() {
        return roleName;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Role role = (Role) obj;
        return roleName.equals(role.roleName);
    }

    public String toString() {
        return this.roleName;
    }

}
