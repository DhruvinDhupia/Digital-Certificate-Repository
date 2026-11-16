import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class ActivityLogger {
    private ActivityLogger() {}
    public static void log(String action, String details) {
        try {
            DatabaseSetup.ensureSchema();
            String sql = "INSERT INTO activity_logs(username, action, details) VALUES (?, ?, ?)";
            try (Connection c = DatabaseConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, Session.getUsername());
                p.setString(2, action);
                p.setString(3, details == null ? "" : details.substring(0, Math.min(500, details.length())));
                p.executeUpdate();
            }
        } catch (SQLException ignored) {
            // Logging must never block the main certificate operation.
        }
    }
}
