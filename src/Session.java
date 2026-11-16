public final class Session {
    private static String username = "admin";
    private Session() {}
    public static String getUsername() { return username; }
    public static void setUsername(String value) { username = (value == null || value.isBlank()) ? "admin" : value; }
}
