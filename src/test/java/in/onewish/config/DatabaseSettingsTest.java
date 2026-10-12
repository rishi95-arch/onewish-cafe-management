package in.onewish.config;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DatabaseSettingsTest {
    @Test void buildsJdbcUrlFromRailwayMysqlVariables() throws Exception {
        var s = DatabaseSettings.from(Map.of("MYSQLHOST", "mysql.railway.internal", "MYSQLPORT", "3306", "MYSQLDATABASE", "railway", "MYSQLUSER", "app", "MYSQLPASSWORD", "private"));
        assertEquals("jdbc:mysql://mysql.railway.internal:3306/railway?serverTimezone=UTC&connectTimeout=5000&socketTimeout=5000", s.url());
        assertEquals("app", s.user());
        assertEquals("private", s.password());
    }

    @Test void explicitJdbcSettingsTakePrecedence() throws Exception {
        var s = DatabaseSettings.from(Map.of("DB_URL", "jdbc:mysql://db.example:3306/cafe", "DB_USER", "local-user", "DB_PASSWORD", "local-pass", "MYSQLUSER", "railway-user", "MYSQLPASSWORD", "railway-pass"));
        assertEquals("jdbc:mysql://db.example:3306/cafe", s.url());
        assertEquals("local-user", s.user());
        assertEquals("local-pass", s.password());
    }

    @Test void rejectsInvalidRailwayHostWithoutEchoingSecrets() {
        SQLException error = assertThrows(SQLException.class, () -> DatabaseSettings.from(Map.of("MYSQLHOST", "bad/host", "MYSQLPORT", "3306", "MYSQLDATABASE", "db", "MYSQLUSER", "user", "MYSQLPASSWORD", "secret")));
        assertFalse(error.getMessage().contains("secret"));
    }
}
