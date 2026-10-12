package in.onewish.web;

import in.onewish.dao.MenuDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;

@WebServlet(urlPatterns={"/","/menu"})
public class HomeServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        try { var dao=new MenuDao(); req.setAttribute("items",dao.available(req.getParameter("q"),req.getParameter("category"),req.getParameter("sort"))); req.setAttribute("categories",dao.categories()); }
        catch(Exception e){getServletContext().log("Unable to load menu",e);req.setAttribute("menuError",true);}
        req.setAttribute("csrf",Web.csrf(req)); req.setAttribute("flash",Web.takeFlash(req));
        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req,res);
    }
}
