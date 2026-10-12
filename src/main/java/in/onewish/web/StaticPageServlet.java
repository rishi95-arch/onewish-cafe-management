package in.onewish.web;
import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;
@WebServlet(urlPatterns={"/about"}) public class StaticPageServlet extends HttpServlet {protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{q.getRequestDispatcher("/WEB-INF/views/about.jsp").forward(q,s);}}
