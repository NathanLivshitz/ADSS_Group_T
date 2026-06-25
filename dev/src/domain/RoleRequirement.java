package domain;

public class RoleRequirement {
    private int amount;
    private Role role;

    public RoleRequirement(int amount, Role role){
        if (role == null || amount < 0) {
            throw new IllegalArgumentException("invalid input");
        }
        this.amount = amount;
        this.role = role;
    }

    public int getAmount(){
        return amount;
    }

    public Role getRole(){
        return role;
    }

    public void setAmount(int amount){
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be greater than or equal to 0");
        }
        this.amount = amount;
    }
}
