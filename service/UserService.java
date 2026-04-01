package service;

import model.Portfolio;
import model.User;

import java.util.concurrent.ConcurrentHashMap;

public class UserService {

    private static final ConcurrentHashMap<String, User> USERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Portfolio> PORTFOLIOS = new ConcurrentHashMap<>();

    public static String getOrCreateUser(String userId) {
        User user = USERS.computeIfAbsent(userId, id -> {
            boolean premium = false;
            double initialBalance = premium ? 500000.0 : 100000.0;
            Portfolio portfolio = new Portfolio(id, initialBalance, initialBalance, 0.0);
            PORTFOLIOS.put(id, portfolio);
            return new User(id, premium, initialBalance, System.currentTimeMillis(), System.currentTimeMillis());
        });

        user.setLastLoginAt(System.currentTimeMillis());

        Portfolio portfolio = PORTFOLIOS.computeIfAbsent(
                userId,
                id -> new Portfolio(id, user.getAllocatedCapital(), user.getAllocatedCapital(), 0.0)
        );

        if (RewardService.grantDailyLoginReward(userId)) {
            double reward = user.isPremium() ? 0.0 : 2000.0;
            if (reward > 0) {
                portfolio.setBalance(portfolio.getBalance() + reward);
            }
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
            double initialBalance = 100000.0;
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

    public static void enablePremium(String userId) {
        User user = getUserObject(userId);
        user.setPremium(true);
        user.setAllocatedCapital(500000.0);

        Portfolio portfolio = getPortfolioObject(userId);
        if (portfolio.getBalance() < 500000.0) {
            portfolio.setBalance(500000.0);
        }
        if (portfolio.getInitialBalance() < 500000.0) {
            portfolio.setInitialBalance(500000.0);
        }

        savePortfolio(portfolio);
    }
}
