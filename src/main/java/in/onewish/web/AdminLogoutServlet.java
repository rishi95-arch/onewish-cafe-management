package in.onewish.web;
import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;
@WebServlet("/admin/logout") public class AdminLogoutServlet extends HttpServlet {protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException{if(!Web.validCsrf(q)){s.sendError(403);return;}q.getSession().invalidate();s.sendRedirect(q.getContextPath()+"/");}}
