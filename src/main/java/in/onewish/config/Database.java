package in.onewish.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public final class Database {
    private Database() {}
    public static Connection connect() throws SQLException {
        DatabaseSettings settings = DatabaseSettings.from(System.getenv());
        return DriverManager.getConnection(settings.url(), settings.user(), settings.password());
    }
}
