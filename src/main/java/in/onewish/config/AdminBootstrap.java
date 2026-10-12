package in.onewish.config;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

/** One-time helper: run with APP_ADMIN_EMAIL and APP_ADMIN_PASSWORD set. */
public final class AdminBootstrap {
    public static void main(String[] args) throws Exception {
        String email=System.getenv("APP_ADMIN_EMAIL"), password=System.getenv("APP_ADMIN_PASSWORD");
        if(email==null||!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")||password==null||password.length()<12) throw new IllegalArgumentException("Set a valid APP_ADMIN_EMAIL and a password of at least 12 characters.");
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("INSERT INTO users(name,email,password_hash,role) VALUES('Onewish Administrator',?,?,'ADMIN') ON DUPLICATE KEY UPDATE email=email")) {p.setString(1,email.toLowerCase());p.setString(2,BCrypt.hashpw(password,BCrypt.gensalt(12)));int changed=p.executeUpdate();System.out.println(changed==1?"Administrator account created.":"That email already exists; no account was changed.");}
    }
}
