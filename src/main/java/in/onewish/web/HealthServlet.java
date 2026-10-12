package in.onewish.web;

import in.onewish.config.Database;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/health")
public class HealthServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement("SELECT 1");
             ResultSet result = statement.executeQuery()) {
            if (result.next() && result.getInt(1) == 1) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("ok");
                return;
            }
        } catch (Exception e) {
            getServletContext().log("Health check failed", e);
        }
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.getWriter().println("unavailable");
    }
}
