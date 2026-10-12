package in.onewish.web;
import in.onewish.config.Database;import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.*;
@WebServlet("/track") public class TrackServlet extends HttpServlet {
 protected void doGet(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException {String ref=q.getParameter("ref");if(ref!=null&&ref.matches("OW-[A-F0-9]{32}")){try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("SELECT reference,status,total,created_at,fulfilment FROM orders WHERE reference=?")){p.setString(1,ref);try(ResultSet r=p.executeQuery()){if(r.next()){q.setAttribute("order",new Object[]{r.getString(1),r.getString(2),r.getBigDecimal(3),r.getTimestamp(4),r.getString(5)});}}}catch(Exception e){getServletContext().log("Order lookup failed",e);}}q.getRequestDispatcher("/WEB-INF/views/track.jsp").forward(q,s);}
}
