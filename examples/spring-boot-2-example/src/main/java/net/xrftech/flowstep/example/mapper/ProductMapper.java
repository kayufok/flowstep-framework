package net.xrftech.flowstep.example.mapper;

import net.xrftech.flowstep.example.model.Product;
import net.xrftech.flowstep.example.dto.UserOrderSummaryResponse.ProductSummary;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * MyBatis Mapper for Product entity.
 * Demonstrates MyBatis-based data access in FlowStep operations.
 */
@Mapper
public interface ProductMapper {
    
    @Select("SELECT * FROM products WHERE id = #{id}")
    Product findById(Long id);
    
    @Select("SELECT * FROM products WHERE is_active = true ORDER BY name")
    List<Product> findAllActive();
    
    @Update("UPDATE products SET stock_quantity = stock_quantity - #{quantity} WHERE id = #{productId} AND stock_quantity >= #{quantity}")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    @Update("UPDATE products SET stock_quantity = stock_quantity + #{quantity} WHERE id = #{productId}")
    int increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    @Insert("INSERT INTO products (name, description, price, stock_quantity, is_active, created_at, updated_at) " +
            "VALUES (#{name}, #{description}, #{price}, #{stockQuantity}, #{isActive}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);
    
    @Update("UPDATE products SET name = #{name}, description = #{description}, price = #{price}, " +
            "stock_quantity = #{stockQuantity}, is_active = #{isActive}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Product product);
    
    @Delete("DELETE FROM products WHERE id = #{id}")
    int deleteById(Long id);
    
    // Complex query for user's top products
    @Select("""
        SELECT p.id as productId, p.name as productName, 
               COUNT(oi.order_id) as orderCount, 
               SUM(oi.quantity * oi.unit_price) as totalSpent
        FROM products p 
        JOIN order_items oi ON p.id = oi.product_id 
        JOIN orders o ON oi.order_id = o.id 
        WHERE o.user_id = #{userId}
        GROUP BY p.id, p.name 
        ORDER BY totalSpent DESC 
        LIMIT #{limit}
        """)
    @Results({
        @Result(property = "productId", column = "productId"),
        @Result(property = "productName", column = "productName"),
        @Result(property = "orderCount", column = "orderCount"),
        @Result(property = "totalSpent", column = "totalSpent")
    })
    List<ProductSummary> findTopProductsByUser(@Param("userId") Long userId, @Param("limit") Integer limit);
}