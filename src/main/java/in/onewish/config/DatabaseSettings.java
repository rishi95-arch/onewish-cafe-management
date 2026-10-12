package in.onewish.config;

import java.sql.SQLException;
import java.util.Map;

record DatabaseSettings(String url, String user, String password) {
    static DatabaseSettings from(Map<String, String> env) throws SQLException {
        String url = value(env, "DB_URL");
        String user = first(value(env, "DB_USER"), value(env, "MYSQLUSER"));
        String password = first(value(env, "DB_PASSWORD"), value(env, "MYSQLPASSWORD"));
        if (url == null) {
            String host = value(env, "MYSQLHOST");
            String port = value(env, "MYSQLPORT");
            String database = value(env, "MYSQLDATABASE");
            if (host == null || !host.matches("[A-Za-z0-9._-]+") || port == null || !port.matches("[0-9]{1,5}") || database == null || !database.matches("[A-Za-z0-9_$-]+")) {
                throw new SQLException("Database configuration is missing or invalid.");
            }
            int portNumber;
            try { portNumber = Integer.parseInt(port); }
            catch (NumberFormatException e) { throw new SQLException("Database configuration is missing or invalid."); }
            if (portNumber < 1 || portNumber > 65535) throw new SQLException("Database configuration is missing or invalid.");
            url = "jdbc:mysql://" + host + ":" + portNumber + "/" + database + "?serverTimezone=UTC&connectTimeout=5000&socketTimeout=5000";
        }
        if (user == null || password == null) throw new SQLException("Database configuration is missing.");
        return new DatabaseSettings(url, user, password);
    }

    private static String value(Map<String, String> env, String key) {
        String value = env.get(key);
        return value == null || value.isBlank() ? null : value.trim();
    }
    private static String first(String preferred, String fallback) { return preferred == null ? fallback : preferred; }

    @Override public String toString() {
        return "DatabaseSettings[url=" + url + ", user=" + user + ", password=<redacted>]";
    }
}
