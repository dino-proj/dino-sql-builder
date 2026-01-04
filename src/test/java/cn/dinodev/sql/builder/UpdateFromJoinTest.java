// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSqlWithParams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.dialect.PostgreSQLDialect;
import cn.dinodev.sql.naming.SnakeNamingConversition;

/**
 * UPDATE 语句 FROM 和 JOIN 子句测试类。
 * 
 * <p>测试不同数据库中 UPDATE 语句的关联语法，包括：
 * <ul>
 *   <li>PostgreSQL 的 UPDATE ... FROM 语法</li>
 *   <li>MySQL 的 UPDATE ... JOIN 语法</li>
 * </ul>
 *
 * @author Cody Lu
 * @since 2026-01-03
 */
@DisplayName("UPDATE语句FROM和JOIN子句测试")
public class UpdateFromJoinTest {

  private PostgreSQLDialect postgresDialect;
  private MysqlDialect mysqlDialect;

  @BeforeEach
  public void setUp() throws Exception {
    postgresDialect = new PostgreSQLDialect(null, new SnakeNamingConversition());
    mysqlDialect = new MysqlDialect(null, new SnakeNamingConversition());
  }

  @Test
  @DisplayName("测试PostgreSQL基础FROM子句")
  void testUpdateFromPostgreSQL_BasicFrom() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "orders", "o")
        .from("customers", "c")
        .set("o.customer_name", "c.name")
        .where("o.customer_id = c.id")
        .eq("c.status", "active");

    assertSqlWithParams(builder, "PostgreSQL基础FROM",
        "UPDATE orders AS o SET o.customer_name = ? FROM customers AS c WHERE o.customer_id = c.id AND (c.status = ?)",
        new Object[] { "c.name", "active" });
  }

  @Test
  @DisplayName("测试PostgreSQL多个FROM子句")
  void testUpdateFromPostgreSQL_MultipleFrom() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "products", "p")
        .from("categories", "c")
        .from("suppliers", "s")
        .set("p.category_name", "c.name")
        .set("p.supplier_name", "s.name")
        .where("p.category_id = c.id AND p.supplier_id = s.id");

    assertSqlWithParams(builder, "PostgreSQL多个FROM",
        "UPDATE products AS p SET p.category_name = ?, p.supplier_name = ? FROM categories AS c, suppliers AS s WHERE p.category_id = c.id AND p.supplier_id = s.id",
        new Object[] { "c.name", "s.name" });
  }

  @Test
  @DisplayName("测试PostgreSQL带条件FROM子句")
  void testUpdateFromPostgreSQL_ConditionalFrom() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "orders", "o")
        .from("customers", "c")
        .set("o.customer_name", "c.name")
        .where("o.customer_id = c.id");

    assertSqlWithParams(builder, "PostgreSQL条件FROM",
        "UPDATE orders AS o SET o.customer_name = ? FROM customers AS c WHERE o.customer_id = c.id",
        new Object[] { "c.name" });
  }

  @Test
  @DisplayName("测试MySQL INNER JOIN")
  void testUpdateJoinMySQL_InnerJoin() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "users", "u")
        .join("orders", "o", "u.id = o.user_id")
        .set("u.total_orders", "u.total_orders + 1")
        .eq("o.status", "completed");

    assertSqlWithParams(builder, "MySQL INNER JOIN",
        "UPDATE users AS u JOIN orders AS o ON u.id = o.user_id SET u.total_orders = ? WHERE o.status = ?",
        new Object[] { "u.total_orders + 1", "completed" });
  }

  @Test
  @DisplayName("测试MySQL LEFT JOIN")
  void testUpdateJoinMySQL_LeftJoin() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "products", "p")
        .leftJoin("inventory i", "p.id = i.product_id")
        .set("p.stock", "COALESCE(i.quantity, 0)");

    assertSqlWithParams(builder, "MySQL LEFT JOIN",
        "UPDATE products AS p LEFT JOIN inventory i ON p.id = i.product_id SET p.stock = ?",
        new Object[] { "COALESCE(i.quantity, 0)" });
  }

  @Test
  @DisplayName("测试MySQL多个JOIN")
  void testUpdateJoinMySQL_MultipleJoins() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "orders", "o")
        .join("customers", "c", "o.customer_id = c.id")
        .leftJoin("discounts d", "c.discount_id = d.id")
        .set("o.discount_amount", "COALESCE(d.amount, 0)")
        .eq("c.status", "active");

    assertSqlWithParams(builder, "MySQL多个JOIN",
        "UPDATE orders AS o JOIN customers AS c ON o.customer_id = c.id LEFT JOIN discounts d ON c.discount_id = d.id SET o.discount_amount = ? WHERE c.status = ?",
        new Object[] { "COALESCE(d.amount, 0)", "active" });
  }

  @Test
  @DisplayName("测试多表UPDATE（MySQL风格）")
  void testMultiTableUpdate_MySQL() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.createMultiTable(mysqlDialect, "orders o", "order_items oi")
        .set("o.status", "completed")
        .set("oi.shipped", true)
        .where("o.id = oi.order_id")
        .eq("o.id", 123);

    assertSqlWithParams(builder, "多表UPDATE(MySQL)",
        "UPDATE orders o, order_items oi SET o.status = ?, oi.shipped = ? WHERE o.id = oi.order_id AND (o.id = ?)",
        new Object[] { "completed", true, 123 });
  }

  @Test
  @DisplayName("测试addTable方法")
  void testMultiTableUpdate_AddTable() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "orders", "o")
        .addTable("order_items", "oi")
        .addTable("products p")
        .set("o.total", "oi.quantity * p.price")
        .where("o.id = oi.order_id AND oi.product_id = p.id");

    assertSqlWithParams(builder, "addTable方法",
        "UPDATE orders AS o, order_items AS oi, products p SET o.total = ? WHERE o.id = oi.order_id AND oi.product_id = p.id",
        new Object[] { "oi.quantity * p.price" });
  }

  @Test
  @DisplayName("测试实际场景：库存同步")
  void testRealWorldScenario_InventorySync() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "products", "p")
        .join("inventory", "i", "p.id = i.product_id")
        .leftJoin("warehouse_stock ws", "p.id = ws.product_id")
        .set("p.stock", "COALESCE(i.quantity, 0) + COALESCE(ws.quantity, 0)")
        .set("p.last_sync_at", "NOW()")
        .where("p.active = 1");

    assertSqlWithParams(builder, "实际场景:库存同步",
        "UPDATE products AS p JOIN inventory AS i ON p.id = i.product_id LEFT JOIN warehouse_stock ws ON p.id = ws.product_id SET p.stock = ?, p.last_sync_at = ? WHERE p.active = 1",
        new Object[] { "COALESCE(i.quantity, 0) + COALESCE(ws.quantity, 0)", "NOW()" });
  }
}