package service;

import model.Portfolio;
import model.User;

import java.util.concurrent.ConcurrentHashMap;

public class UserService {

    private static final ConcurrentHashMap<String, User> USERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Portfolio> PORTFOLIOS = new ConcurrentHashMap<>();

    private static final double FREE_INITIAL_BALANCE = 100000.0;
    private static final double PREMIUM_INITIAL_BALANCE = 500000.0;

    public static String getOrCreateUser(String userId) {
        User user = USERS.computeIfAbsent(userId, id -> {
            boolean premium = false;
            double initialBalance = premium ? PREMIUM_INITIAL_BALANCE : FREE_INITIAL_BALANCE;
            Portfolio portfolio = new Portfolio(id, initialBalance, initialBalance, 0.0);
            PORTFOLIOS.put(id, portfolio);
            return new User(id, premium, initialBalance, System.currentTimeMillis(), System.currentTimeMillis());
        });

        user.setLastLoginAt(System.currentTimeMillis());

        Portfolio portfolio = PORTFOLIOS.computeIfAbsent(
                userId,
                id -> new Portfolio(id, user.getAllocatedCapital(), user.getAllocatedCapital(), 0.0)
        );

        if (!user.isPremium() && RewardService.grantDailyLoginReward(userId)) {
            portfolio.setBalance(portfolio.getBalance() + RewardService.DAILY_LOGIN_REWARD);
        }

        String rewards = RewardService.getRewardSnapshot(userId);

        return "{"
                + "\"success\":true,"
                + "\"timestamp\":" + System.currentTimeMillis() + ","
                + "\"data\":{"
                + "\"user\":" + user.toJson() + ","
                + "\"portfolio\":" + portfolio.toJson() + ","
                + "\"rewards\":" + rewards
                + "},"
                + "\"error\":null"
                + "}";
    }

    public static User getUserObject(String userId) {
        return USERS.computeIfAbsent(userId, id -> {
            double initialBalance = FREE_INITIAL_BALANCE;
            PORTFOLIOS.putIfAbsent(id, new Portfolio(id, initialBalance, initialBalance, 0.0));
            return new User(id, false, initialBalance, System.currentTimeMillis(), System.currentTimeMillis());
        });
    }

    public static Portfolio getPortfolioObject(String userId) {
        User user = getUserObject(userId);
        return PORTFOLIOS.computeIfAbsent(
                userId,
                id -> new Portfolio(id, user.getAllocatedCapital(), user.getAllocatedCapital(), 0.0)
        );
    }

    public static void savePortfolio(Portfolio portfolio) {
        if (portfolio != null && portfolio.getUserId() != null) {
            PORTFOLIOS.put(portfolio.getUserId(), portfolio);
        }
    }

    public static boolean grantAdReward(String userId) {
        User user = getUserObject(userId);
        if (user.isPremium()) return false;

        boolean granted = RewardService.grantAdReward(userId);
        if (!granted) return false;

        Portfolio portfolio = getPortfolioObject(userId);
        portfolio.setBalance(portfolio.getBalance() + RewardService.AD_REWARD_AMOUNT);
        savePortfolio(portfolio);
        return true;
    }

    public static void enablePremium(String userId) {
        User user = getUserObject(userId);
        user.setPremium(true);
        user.setAllocatedCapital(PREMIUM_INITIAL_BALANCE);

        Portfolio portfolio = getPortfolioObject(userId);
        if (portfolio.getBalance() < PREMIUM_INITIAL_BALANCE) {
            portfolio.setBalance(PREMIUM_INITIAL_BALANCE);
        }
        if (portfolio.getInitialBalance() < PREMIUM_INITIAL_BALANCE) {
            portfolio.setInitialBalance(PREMIUM_INITIAL_BALANCE);
        }

        savePortfolio(portfolio);
    }
}
