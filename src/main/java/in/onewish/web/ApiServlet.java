package in.onewish.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.onewish.config.Database;
import in.onewish.dao.MenuDao;
import in.onewish.model.OrderMath;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;
import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** JSON API used by the static Netlify storefront. The existing JSP routes remain available. */
@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {
    private static final ObjectMapper JSON=new ObjectMapper();
    private static final Set<String> STATUSES=Set.of("PLACED","PREPARING","READY","COMPLETED","CANCELLED");
    private Map<String,Object> body(HttpServletRequest r) throws IOException { return JSON.readValue(r.getInputStream(),Map.class); }
    private void send(HttpServletResponse r,int status,Object value) throws IOException { r.setStatus(status);r.setContentType("application/json;charset=UTF-8");r.setHeader("Cache-Control","no-store");JSON.writeValue(r.getOutputStream(),value); }
    private void error(HttpServletResponse r,int status,String message)throws IOException{send(r,status,Map.of("error",message));}
    private String csrf(HttpServletRequest r){return Web.csrf(r);}
    private boolean validCsrf(HttpServletRequest r)throws IOException {String supplied=r.getHeader("X-CSRF-Token");return supplied!=null&&supplied.equals(csrf(r));}
    private boolean admin(HttpServletRequest r){return r.getSession(false)!=null&&r.getSession(false).getAttribute("adminId")!=null;}
    @SuppressWarnings("unchecked") private Map<Long,Integer> cart(HttpSession s){Map<Long,Integer> c=(Map<Long,Integer>)s.getAttribute("cart");if(c==null){c=new LinkedHashMap<>();s.setAttribute("cart",c);}return c;}
    private Map<String,Object> item(in.onewish.model.MenuItem i){return Map.of("id",i.id(),"name",i.name(),"description",i.description(),"price",i.price(),"categoryId",i.categoryId(),"category",i.category(),"imageUrl",i.imageUrl(),"foodType",i.foodType(),"available",i.available());}
    private Map<String,Object> cartView(HttpServletRequest r)throws Exception {var entries=new ArrayList<Map<String,Object>>();BigDecimal total=BigDecimal.ZERO;MenuDao dao=new MenuDao();for(var e:cart(r.getSession()).entrySet()){var x=dao.find(e.getKey()).orElse(null);if(x==null||!x.available())continue;BigDecimal line=x.price().multiply(BigDecimal.valueOf(e.getValue()));total=total.add(line);entries.add(Map.of("item",item(x),"quantity",e.getValue(),"lineTotal",line));}return Map.of("items",entries,"total",total,"csrfToken",csrf(r));}
    @Override protected void service(HttpServletRequest r,HttpServletResponse s)throws ServletException,IOException {if("PATCH".equalsIgnoreCase(r.getMethod())){patch(r,s);return;}super.service(r,s);}
    @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws IOException {
        String path=r.getPathInfo()==null?"/":r.getPathInfo();
        try {
            switch(path){
                case "/session" -> send(s,200,Map.of("csrfToken",csrf(r),"admin",admin(r)));
                case "/menu" -> {MenuDao d=new MenuDao();var rows=d.available(r.getParameter("q"),r.getParameter("category"),r.getParameter("sort")).stream().map(this::item).toList();var cats=d.categories().stream().map(c->Map.of("slug",c[0],"name",c[1])).toList();send(s,200,Map.of("items",rows,"categories",cats));}
                case "/item" -> {long id=Long.parseLong(r.getParameter("id"));var it=new MenuDao().find(id).orElse(null);if(it==null||!it.available()){error(s,404,"Menu item not found.");return;}send(s,200,item(it));}
                case "/cart" -> send(s,200,cartView(r));
                case "/track" -> {String ref=r.getParameter("ref");if(ref==null||!ref.matches("OW-[A-F0-9]{32}")){error(s,400,"Enter a valid order reference.");return;}try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("SELECT reference,status,total,created_at,fulfilment FROM orders WHERE reference=?")){p.setString(1,ref);try(ResultSet z=p.executeQuery()){if(!z.next()){error(s,404,"Order not found.");return;}send(s,200,Map.of("reference",z.getString(1),"status",z.getString(2),"total",z.getBigDecimal(3),"createdAt",z.getTimestamp(4).toInstant().toString(),"fulfilment",z.getString(5)));}}}
                case "/admin" -> {if(!admin(r)){error(s,401,"Sign in required.");return;}send(s,200,adminData(r));}
                default -> error(s,404,"API route not found.");
            }
        }catch(Exception e){getServletContext().log("API request failed: "+path,e);error(s,500,"The request could not be completed.");}
    }
    @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws IOException {
        String path=r.getPathInfo()==null?"/":r.getPathInfo();
        try {
            if(!validCsrf(r)){error(s,403,"The security token expired. Refresh and try again.");return;}
            Map<String,Object>b=body(r);
            if("/admin/login".equals(path)) {String email=str(b,"email"),password=str(b,"password");try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("SELECT id,password_hash FROM users WHERE email=? AND role='ADMIN' AND active=1")){p.setString(1,email.toLowerCase(Locale.ROOT));try(ResultSet z=p.executeQuery()){if(!z.next()||!BCrypt.checkpw(password,z.getString(2))){error(s,401,"Sign-in failed. Check your details.");return;}r.getSession().invalidate();HttpSession session=r.getSession(true);session.setAttribute("adminId",z.getLong(1));session.setAttribute("csrf",UUID.randomUUID().toString());send(s,200,Map.of("csrfToken",csrf(r)));return;}}
            }
            switch(path){
                case "/cart/add" -> {long id=number(b,"id");int qty=(int)number(b,"quantity");if(qty<1||qty>20){error(s,400,"Quantity must be between 1 and 20.");return;}var it=new MenuDao().find(id).orElse(null);if(it==null||!it.available()){error(s,404,"That item is unavailable.");return;}Map<Long,Integer> c=cart(r.getSession());c.put(id,Math.min(20,c.getOrDefault(id,0)+qty));send(s,200,cartView(r));}
                case "/checkout" -> {checkout(r,s,b);}
                case "/contact" -> {String name=str(b,"name"),email=str(b,"email"),message=str(b,"message");if(name.trim().length()<2||name.length()>100||!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")||email.length()>190||message.trim().length()<10||message.length()>2000){error(s,400,"Please check your details and message.");return;}try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("INSERT INTO contact_messages(name,email,message) VALUES(?,?,?)")){p.setString(1,name.trim());p.setString(2,email.trim());p.setString(3,message.trim());p.executeUpdate();}send(s,201,Map.of("message","Thank you. Your note has been sent to the Onewish team."));}
                case "/admin/logout" -> {if(!admin(r)){error(s,401,"Sign in required.");return;}r.getSession().invalidate();send(s,200,Map.of("ok",true));}
                case "/admin/order-status" -> {if(!admin(r)){error(s,401,"Sign in required.");return;}String status=str(b,"status");if(!STATUSES.contains(status)){error(s,400,"Invalid order status.");return;}try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("UPDATE orders SET status=? WHERE id=?")){p.setString(1,status);p.setLong(2,number(b,"id"));p.executeUpdate();}send(s,200,Map.of("ok",true));}
                case "/admin/menu" -> {if(!admin(r)){error(s,401,"Sign in required.");return;}saveMenu(b);send(s,200,Map.of("ok",true));}
                default -> error(s,404,"API route not found.");
            }
        }catch(IllegalArgumentException e){error(s,400,"Check the submitted values and try again.");}
        catch(Exception e){getServletContext().log("API write failed: "+path,e);error(s,500,"The request could not be completed.");}
    }
    private void patch(HttpServletRequest r,HttpServletResponse s)throws IOException {
        if(!"/cart".equals(r.getPathInfo())){error(s,404,"API route not found.");return;}if(!validCsrf(r)){error(s,403,"The security token expired. Refresh and try again.");return;}
        try{Map<String,Object>b=body(r);long id=number(b,"id");int delta=(int)number(b,"delta");Map<Long,Integer> c=cart(r.getSession());int next=c.getOrDefault(id,0)+delta;if(next<=0)c.remove(id);else if(next>20)c.put(id,20);else c.put(id,next);send(s,200,cartView(r));}catch(Exception e){error(s,400,"Invalid cart update.");}
    }
    @Override protected void doDelete(HttpServletRequest r,HttpServletResponse s)throws IOException {
        if(!"/cart".equals(r.getPathInfo())){error(s,404,"API route not found.");return;}if(!validCsrf(r)){error(s,403,"The security token expired. Refresh and try again.");return;}
        try{long id=Long.parseLong(r.getParameter("id"));cart(r.getSession()).remove(id);send(s,200,cartView(r));}catch(Exception e){error(s,400,"Invalid cart item.");}
    }
    private String str(Map<String,Object>b,String k){Object v=b.get(k);return v==null?"":String.valueOf(v);}
    private long number(Map<String,Object>b,String k){Object v=b.get(k);if(!(v instanceof Number))throw new IllegalArgumentException();return ((Number)v).longValue();}
    private void checkout(HttpServletRequest r,HttpServletResponse s,Map<String,Object>b)throws Exception {
        String name=str(b,"name"),phone=str(b,"phone"),fulfil=str(b,"fulfilment"),address=str(b,"address");if(name.trim().length()<2||name.length()>100||!phone.matches("[+0-9 ()-]{8,20}")||!(fulfil.equals("pickup")||fulfil.equals("delivery"))||(fulfil.equals("delivery")&&(address.trim().length()<8||address.length()>500))){error(s,400,"Please check your name, phone number, and fulfilment details.");return;}
        Map<Long,Integer> items=cart(r.getSession(false)==null?r.getSession():r.getSession(false));if(items.isEmpty()){error(s,400,"Your bag is empty.");return;}
        try(Connection c=Database.connect()){c.setAutoCommit(false);try{BigDecimal total=BigDecimal.ZERO;Map<Long,BigDecimal>prices=new LinkedHashMap<>();try(PreparedStatement p=c.prepareStatement("SELECT id,price,available FROM menu_items WHERE id=? FOR UPDATE")){for(var e:items.entrySet()){p.setLong(1,e.getKey());try(ResultSet z=p.executeQuery()){if(!z.next()||!z.getBoolean("available"))throw new IllegalArgumentException();BigDecimal price=z.getBigDecimal("price");prices.put(e.getKey(),price);total=total.add(OrderMath.lineTotal(price,e.getValue()));}}}
            String ref="OW-"+UUID.randomUUID().toString().replace("-","").toUpperCase(Locale.ROOT);long orderId;try(PreparedStatement p=c.prepareStatement("INSERT INTO orders(reference,customer_name,phone,fulfilment,address,status,total) VALUES(?,?,?,?,?,'PLACED',?)",Statement.RETURN_GENERATED_KEYS)){p.setString(1,ref);p.setString(2,name.trim());p.setString(3,phone.trim());p.setString(4,fulfil);p.setString(5,fulfil.equals("delivery")?address.trim():null);p.setBigDecimal(6,total);p.executeUpdate();try(ResultSet z=p.getGeneratedKeys()){z.next();orderId=z.getLong(1);}}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO order_items(order_id,menu_item_id,item_name,unit_price,quantity) SELECT ?,id,name,?,? FROM menu_items WHERE id=?")){for(var e:items.entrySet()){p.setLong(1,orderId);p.setBigDecimal(2,prices.get(e.getKey()));p.setInt(3,e.getValue());p.setLong(4,e.getKey());p.addBatch();}p.executeBatch();}c.commit();r.getSession().removeAttribute("cart");send(s,201,Map.of("reference",ref,"total",total));
        }catch(Exception e){c.rollback();throw e;}}
    }
    private Map<String,Object> adminData(HttpServletRequest r)throws Exception {
        Map<String,Object> result=new LinkedHashMap<>();try(Connection c=Database.connect();Statement st=c.createStatement();ResultSet z=st.executeQuery("SELECT COUNT(*),COALESCE(SUM(total),0) FROM orders")){z.next();result.put("orderCount",z.getInt(1));result.put("revenue",z.getBigDecimal(2));}
        String status=r.getParameter("status");if(status==null||!STATUSES.contains(status))status="";String date=r.getParameter("date");if(date==null||!date.matches("\\d{4}-\\d{2}-\\d{2}"))date="";
        var orders=new ArrayList<Map<String,Object>>();try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("SELECT id,reference,customer_name,status,total,created_at FROM orders WHERE (?='' OR status=?) AND (?='' OR DATE(created_at)=?) ORDER BY created_at DESC LIMIT 200")){p.setString(1,status);p.setString(2,status);p.setString(3,date);p.setString(4,date);try(ResultSet z=p.executeQuery()){while(z.next())orders.add(Map.of("id",z.getLong(1),"reference",z.getString(2),"customerName",z.getString(3),"status",z.getString(4),"total",z.getBigDecimal(5),"createdAt",z.getTimestamp(6).toInstant().toString()));}}
        MenuDao d=new MenuDao();result.put("orders",orders);result.put("items",d.all().stream().map(this::item).toList());result.put("categories",d.adminCategories().stream().map(x->Map.of("id",x[0],"name",x[1])).toList());result.put("csrfToken",csrf(r));return result;
    }
    private void saveMenu(Map<String,Object>b)throws Exception {
        String name=str(b,"name"),desc=str(b,"description"),image=str(b,"imageUrl"),type=str(b,"foodType");BigDecimal price=new BigDecimal(str(b,"price"));long id=number(b,"id"),category=number(b,"categoryId");if(name.isBlank()||name.length()>120||desc.length()>1000||!image.startsWith("https://")||image.length()>500||price.signum()<=0||!Set.of("VEG","NON_VEG","EGG").contains(type))throw new IllegalArgumentException();new MenuDao().save(id,name.trim(),desc,price,category,image,type,Boolean.TRUE.equals(b.get("available")));
    }
}
