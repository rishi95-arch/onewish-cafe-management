package in.onewish.dao;

import in.onewish.config.Database;
import in.onewish.model.MenuItem;
import java.sql.*;
import java.util.*;

public class MenuDao {
    private MenuItem map(ResultSet r) throws SQLException { return new MenuItem(r.getLong("id"), r.getString("name"), r.getString("description"), r.getBigDecimal("price"), r.getLong("category_id"), r.getString("category"), r.getString("image_url"), r.getString("food_type"), r.getBoolean("available")); }
    public List<MenuItem> available(String search, String category, String sort) throws SQLException {
        String order = "price_asc".equals(sort) ? "price ASC" : "price_desc".equals(sort) ? "price DESC" : "name ASC";
        String sql = "SELECT m.*, c.name category FROM menu_items m JOIN categories c ON c.id=m.category_id WHERE m.available=1 AND (?='' OR m.name LIKE ? OR m.description LIKE ?) AND (?='' OR c.slug=?) ORDER BY " + order;
        try (Connection c=Database.connect(); PreparedStatement p=c.prepareStatement(sql)) { String q=search==null?"":search.trim(); String cat=category==null?"":category; p.setString(1,q); p.setString(2,"%"+q+"%"); p.setString(3,"%"+q+"%"); p.setString(4,cat); p.setString(5,cat); try(ResultSet r=p.executeQuery()){ List<MenuItem> out=new ArrayList<>(); while(r.next())out.add(map(r)); return out; } }
    }
    public List<MenuItem> all() throws SQLException {
        try(Connection c=Database.connect(); PreparedStatement p=c.prepareStatement("SELECT m.*,c.name category FROM menu_items m JOIN categories c ON c.id=m.category_id ORDER BY m.id DESC"); ResultSet r=p.executeQuery()){ List<MenuItem> out=new ArrayList<>(); while(r.next())out.add(map(r)); return out; }
    }
    public Optional<MenuItem> find(long id) throws SQLException {
        try(Connection c=Database.connect(); PreparedStatement p=c.prepareStatement("SELECT m.*,c.name category FROM menu_items m JOIN categories c ON c.id=m.category_id WHERE m.id=?")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(map(r)):Optional.empty();}}
    }
    public List<String[]> categories() throws SQLException {
        try(Connection c=Database.connect(); PreparedStatement p=c.prepareStatement("SELECT slug,name FROM categories ORDER BY name");ResultSet r=p.executeQuery()){List<String[]> a=new ArrayList<>();while(r.next())a.add(new String[]{r.getString(1),r.getString(2)});return a;}
    }
    public List<Object[]> adminCategories() throws SQLException {
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("SELECT id,name FROM categories ORDER BY name");ResultSet r=p.executeQuery()){List<Object[]> a=new ArrayList<>();while(r.next())a.add(new Object[]{r.getLong(1),r.getString(2)});return a;}
    }
    public void save(long id, String name, String description, java.math.BigDecimal price, long categoryId, String image, String type, boolean available) throws SQLException {
        String sql=id==0?"INSERT INTO menu_items(name,description,price,category_id,image_url,food_type,available) VALUES(?,?,?,?,?,?,?)":"UPDATE menu_items SET name=?,description=?,price=?,category_id=?,image_url=?,food_type=?,available=? WHERE id=?";
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,name);p.setString(2,description);p.setBigDecimal(3,price);p.setLong(4,categoryId);p.setString(5,image);p.setString(6,type);p.setBoolean(7,available);if(id!=0)p.setLong(8,id);p.executeUpdate();}
    }
}
