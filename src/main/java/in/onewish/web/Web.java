package in.onewish.web;

import jakarta.servlet.http.*;
import java.util.UUID;

public final class Web {
    private Web() {}
    public static String csrf(HttpServletRequest r){HttpSession s=r.getSession();String t=(String)s.getAttribute("csrf");if(t==null){t=UUID.randomUUID().toString();s.setAttribute("csrf",t);}return t;}
    public static boolean validCsrf(HttpServletRequest r){Object expected=r.getSession().getAttribute("csrf");return expected instanceof String && expected.equals(r.getParameter("csrf"));}
    public static void flash(HttpServletRequest r,String msg){r.getSession().setAttribute("flash",msg);}
    public static String takeFlash(HttpServletRequest r){Object x=r.getSession().getAttribute("flash");r.getSession().removeAttribute("flash");return x==null?null:x.toString();}
}
