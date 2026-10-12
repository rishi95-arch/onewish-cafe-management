package in.onewish.web;

import in.onewish.config.Database;
import in.onewish.dao.MenuDao;
import in.onewish.model.OrderMath;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
 @SuppressWarnings("unchecked") protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{var cart=(Map<Long,Integer>)q.getSession().getAttribute("cart");if(cart==null||cart.isEmpty()){s.sendRedirect(q.getContextPath()+"/cart");return;}q.setAttribute("csrf",Web.csrf(q));q.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(q,s);}
 @SuppressWarnings("unchecked") protected void doPost(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{
  if(!Web.validCsrf(q)){s.sendError(403);return;}String name=q.getParameter("name"),phone=q.getParameter("phone"),fulfil=q.getParameter("fulfilment"),address=q.getParameter("address");
  if(name==null||name.trim().length()<2||name.length()>100||phone==null||!phone.matches("[+0-9 ()-]{8,20}")||!("pickup".equals(fulfil)||"delivery".equals(fulfil))||("delivery".equals(fulfil)&&(address==null||address.trim().length()<8||address.length()>500))){Web.flash(q,"Please check your name, phone number, and fulfilment details.");s.sendRedirect(q.getContextPath()+"/checkout");return;}
  Map<Long,Integer> cart=(Map<Long,Integer>)q.getSession().getAttribute("cart");if(cart==null||cart.isEmpty()){s.sendRedirect(q.getContextPath()+"/cart");return;}
  try(Connection c=Database.connect()){c.setAutoCommit(false);try{BigDecimal total=BigDecimal.ZERO;Map<Long,BigDecimal> prices=new LinkedHashMap<>();try(PreparedStatement p=c.prepareStatement("SELECT id,price,available FROM menu_items WHERE id=? FOR UPDATE")){for(var e:cart.entrySet()){p.setLong(1,e.getKey());try(ResultSet r=p.executeQuery()){if(!r.next()||!r.getBoolean("available"))throw new IllegalArgumentException();BigDecimal price=r.getBigDecimal("price");prices.put(e.getKey(),price);total=total.add(OrderMath.lineTotal(price,e.getValue()));}}}
   String ref="OW-"+UUID.randomUUID().toString().replace("-","").toUpperCase(Locale.ROOT);long orderId;
   try(PreparedStatement p=c.prepareStatement("INSERT INTO orders(reference,customer_name,phone,fulfilment,address,status,total) VALUES(?,?,?,?,?,'PLACED',?)",Statement.RETURN_GENERATED_KEYS)){p.setString(1,ref);p.setString(2,name.trim());p.setString(3,phone.trim());p.setString(4,fulfil);p.setString(5,"delivery".equals(fulfil)?address.trim():null);p.setBigDecimal(6,total);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){r.next();orderId=r.getLong(1);}}
   try(PreparedStatement p=c.prepareStatement("INSERT INTO order_items(order_id,menu_item_id,item_name,unit_price,quantity) SELECT ?,id,name,?,? FROM menu_items WHERE id=?")){for(var e:cart.entrySet()){p.setLong(1,orderId);p.setBigDecimal(2,prices.get(e.getKey()));p.setInt(3,e.getValue());p.setLong(4,e.getKey());p.addBatch();}p.executeBatch();}c.commit();q.getSession().removeAttribute("cart");s.sendRedirect(q.getContextPath()+"/track?ref="+ref);return;
  }catch(Exception e){c.rollback();throw e;}}catch(IllegalArgumentException e){Web.flash(q,"An item became unavailable. Please review your bag.");s.sendRedirect(q.getContextPath()+"/cart");}catch(Exception e){getServletContext().log("Order could not be placed",e);Web.flash(q,"We couldn't place your order. Please try again.");s.sendRedirect(q.getContextPath()+"/checkout");}
 }
}
