import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Safe, additive schema setup. It does not drop or recreate existing data. */
public final class DatabaseSetup {
    private DatabaseSetup() {}
    public static void ensureSchema() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS activity_logs (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) NOT NULL," +
                "action VARCHAR(80) NOT NULL," +
                "details VARCHAR(500)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate(sql);
        }
    }
}
