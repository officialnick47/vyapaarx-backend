package model;

public class Portfolio {

    private String userId;
    private double initialBalance;
    private double balance;
    private double usedMargin;

    public Portfolio(String userId, double initialBalance, double balance, double usedMargin) {
        this.userId = userId;
        this.initialBalance = initialBalance;
        this.balance = balance;
        this.usedMargin = usedMargin;
    }

    public String getUserId() {
        return userId;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public double getBalance() {
        return balance;
    }

    public double getUsedMargin() {
        return usedMargin;
    }

    public void setInitialBalance(double initialBalance) {
        this.initialBalance = initialBalance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setUsedMargin(double usedMargin) {
        this.usedMargin = usedMargin;
    }

    public String toJson() {
        double pnl = balance - initialBalance;
        return "{"
                + "\"userId\":\"" + escape(userId) + "\","
                + "\"initialBalance\":" + initialBalance + ","
                + "\"balance\":" + balance + ","
                + "\"usedMargin\":" + usedMargin + ","
                + "\"pnl\":" + pnl
                + "}";
    }

    private String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
