package in.onewish.web;

import in.onewish.dao.MenuDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    @SuppressWarnings("unchecked") private Map<Long,Integer> cart(HttpSession s){Map<Long,Integer> c=(Map<Long,Integer>)s.getAttribute("cart");if(c==null){c=new LinkedHashMap<>();s.setAttribute("cart",c);}return c;}
    protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{
        if(!Web.validCsrf(req)){res.sendError(403);return;} if("remove".equals(req.getParameter("action"))||"quantity".equals(req.getParameter("action"))){try{long id=Long.parseLong(req.getParameter("id"));Map<Long,Integer> items=cart(req.getSession());if("remove".equals(req.getParameter("action")))items.remove(id);else{int next=items.getOrDefault(id,0)+Integer.parseInt(req.getParameter("delta"));if(next<=0)items.remove(id);else items.put(id,Math.min(20,next));}}catch(Exception ignored){}res.sendRedirect(req.getContextPath()+"/cart");return;} try{long id=Long.parseLong(req.getParameter("id"));int qty=Math.max(1,Math.min(20,Integer.parseInt(req.getParameter("quantity")==null?"1":req.getParameter("quantity"))));var item=new MenuDao().find(id).orElseThrow();if(!item.available())throw new IllegalArgumentException();var c=cart(req.getSession());c.put(id,Math.min(20,c.getOrDefault(id,0)+qty));Web.flash(req,item.name()+" added to your bag.");}catch(Exception e){Web.flash(req,"That item is unavailable. Please refresh the menu.");}res.sendRedirect(req.getContextPath()+"/menu");
    }
    protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException, jakarta.servlet.ServletException {
        Map<Long,Integer> c=cart(req.getSession());List<Map<String,Object>> lines=new ArrayList<>();java.math.BigDecimal total=java.math.BigDecimal.ZERO;
        try{MenuDao dao=new MenuDao();for(var entry:c.entrySet()){var item=dao.find(entry.getKey()).orElse(null);if(item==null||!item.available())continue;var line=item.price().multiply(java.math.BigDecimal.valueOf(entry.getValue()));total=total.add(line);lines.add(Map.of("item",item,"quantity",entry.getValue(),"lineTotal",line));}}catch(Exception e){getServletContext().log("Unable to load cart",e);}
        req.setAttribute("lines",lines);req.setAttribute("total",total);req.setAttribute("csrf",Web.csrf(req));req.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(req,res);
    }
}
