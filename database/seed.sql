INSERT INTO categories(slug,name) VALUES
 ('coffee','Coffee & Hot Beverages'),('cold-drinks','Cold Coffee & Shakes'),('tea','Tea'),
 ('burgers','Burgers & Sandwiches'),('pizza','Pizza'),('pasta','Pasta'),
 ('snacks','Snacks & Starters'),('desserts','Desserts'),('bakery','Bakery')
ON DUPLICATE KEY UPDATE name=VALUES(name);
INSERT INTO menu_items(name,description,price,category_id,image_url,food_type,available)
SELECT v.name,v.description,v.price,c.id,v.image,v.kind,TRUE FROM (
 SELECT 'Cappuccino' name,'Double espresso softened with silky steamed milk and a delicate cocoa finish.' description,160.00 price,'coffee' slug,'https://images.unsplash.com/photo-1572442388796-11668a67e53d?auto=format&fit=crop&w=900&q=85' image,'VEG' kind UNION ALL
 SELECT 'Flat White','Rich espresso with fine microfoam for a smooth, balanced cup.',175,'coffee','https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Americano','A clean double espresso topped with hot water.',130,'coffee','https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Hot Chocolate','Velvety cocoa, steamed milk and a cloud of cream.',190,'coffee','https://images.unsplash.com/photo-1542990253-0b8be4efc5c2?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Iced Latte','Chilled espresso and milk over ice, lightly sweetened.',185,'cold-drinks','https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Mocha Shake','Espresso, chocolate and thick vanilla ice cream blended cold.',225,'cold-drinks','https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Mango Smoothie','Alphonso mango blended with yoghurt and a touch of honey.',210,'cold-drinks','https://images.unsplash.com/photo-1505252585461-04db1eb84625?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Classic Lemon Iced Tea','Black tea, fresh lemon and a bright, cooling finish.',145,'tea','https://images.unsplash.com/photo-1556679343-c7306c1976bc?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Masala Chai','Slow-brewed Assam tea with ginger and warming spices.',110,'tea','https://images.unsplash.com/photo-1571934811356-5cc061b6821f?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Garden Veg Burger','Crisp house-made vegetable patty, greens and tangy cafe sauce.',240,'burgers','https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Grilled Chicken Sandwich','Herb grilled chicken, tomato and lettuce on toasted sourdough.',285,'burgers','https://images.unsplash.com/photo-1521390188846-e2a3a97453a0?auto=format&fit=crop&w=900&q=85','NON_VEG' UNION ALL
 SELECT 'Paneer Tikka Panini','Smoky paneer, peppers and mint chutney in grilled ciabatta.',255,'burgers','https://images.unsplash.com/photo-1553909489-cd47e0ef937f?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Margherita Pizza','Tomato, mozzarella and fragrant basil on a crisp thin crust.',320,'pizza','https://images.unsplash.com/photo-1574071318508-1cdbab80d002?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Farmhouse Pizza','Roasted peppers, mushrooms, onion and olives with mozzarella.',380,'pizza','https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Smoky Chicken Pizza','Smoky chicken, peppers and mozzarella on our signature base.',420,'pizza','https://images.unsplash.com/photo-1594007654729-407eedc4be65?auto=format&fit=crop&w=900&q=85','NON_VEG' UNION ALL
 SELECT 'Creamy Alfredo Pasta','Penne in a parmesan cream sauce with cracked black pepper.',310,'pasta','https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Penne Arrabbiata','Penne tossed with slow-roasted tomato, garlic and chilli.',285,'pasta','https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Pesto Fusilli','Basil pesto, parmesan and toasted seeds through fusilli.',325,'pasta','https://images.unsplash.com/photo-1551183053-bf91a1d81141?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Truffle Fries','Golden fries finished with parmesan and a hint of truffle.',190,'snacks','https://images.unsplash.com/photo-1573080496219-bb080dd4f877?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Crispy Corn','Sweet corn tossed with chilli, lime and coriander.',175,'snacks','https://images.unsplash.com/photo-1551754655-cd27e38d2076?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Chicken Popcorn','Bite-size seasoned chicken, fried crisp and served with dip.',240,'snacks','https://images.unsplash.com/photo-1562967914-608f82629710?auto=format&fit=crop&w=900&q=85','NON_VEG' UNION ALL
 SELECT 'Classic Tiramisu','Espresso-soaked sponge layered with mascarpone and cocoa.',260,'desserts','https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'New York Cheesecake','Baked vanilla cheesecake with a buttery biscuit base.',245,'desserts','https://images.unsplash.com/photo-1533134242443-d4fd215305ad?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Chocolate Brownie','Warm fudgy chocolate brownie with toasted walnuts.',180,'bakery','https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=900&q=85','VEG' UNION ALL
 SELECT 'Butter Croissant','Flaky, all-butter pastry baked fresh each morning.',135,'bakery','https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=900&q=85','VEG'
) v JOIN categories c ON c.slug=v.slug
ON DUPLICATE KEY UPDATE description=VALUES(description),price=VALUES(price),image_url=VALUES(image_url),food_type=VALUES(food_type);
