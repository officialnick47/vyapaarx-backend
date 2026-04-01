package model;

public class User {

    private String userId;
    private boolean premium;
    private double allocatedCapital;
    private long createdAt;
    private long lastLoginAt;

    public User(String userId, boolean premium, double allocatedCapital, long createdAt, long lastLoginAt) {
        this.userId = userId;
        this.premium = premium;
        this.allocatedCapital = allocatedCapital;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isPremium() {
        return premium;
    }

    public double getAllocatedCapital() {
        return allocatedCapital;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastLoginAt() {
        return lastLoginAt;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public void setAllocatedCapital(double allocatedCapital) {
        this.allocatedCapital = allocatedCapital;
    }

    public void setLastLoginAt(long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String toJson() {
        return "{"
                + "\"userId\":\"" + escape(userId) + "\","
                + "\"premium\":" + premium + ","
                + "\"allocatedCapital\":" + allocatedCapital + ","
                + "\"createdAt\":" + createdAt + ","
                + "\"lastLoginAt\":" + lastLoginAt
                + "}";
    }

    private String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
