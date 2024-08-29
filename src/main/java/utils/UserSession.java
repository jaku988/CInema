package utils;

public class UserSession {
    private static int userId;
    private static boolean loggedIn = false;

    public static void login(int userId) {
        UserSession.userId = userId;
        UserSession.loggedIn = true;
    }

    public static void logout() {
        UserSession.userId = 0;
        UserSession.loggedIn = false;
    }

    public static int getUserId() {
        return userId;
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }
}
