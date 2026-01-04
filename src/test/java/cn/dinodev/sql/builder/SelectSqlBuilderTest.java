// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSqlWithParams;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

/**
 * SELECT 语句构建器综合功能测试类。
 * 
 * <p>测试 {@link SelectSqlBuilder} 的核心功能，包括：
 * <ul>
 *   <li>基本 SELECT 查询</li>
 *   <li>JOIN 操作（INNER, LEFT, RIGHT）</li>
 *   <li>WHERE 条件和子查询</li>
 *   <li>聚合函数和分组</li>
 *   <li>LIMIT 和 OFFSET</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2024-12-04
 */
@DisplayName("SelectSqlBuilder 功能测试")
public class SelectSqlBuilderTest {

  private MysqlDialect mysql;

  @BeforeEach
  public void setUp() {
    mysql = new MysqlDialect(null, new CamelNamingConversition());
  }

  /**
   * 测试基本 SELECT 查询。
   */
  @Test
  @DisplayName("基本 SELECT 查询")
  void testBasicSelect() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("id", "name", "email")
        .eq("status", 1)
        .gt("age", 18)
        .orderBy("created_at", false)
        .limit(10);

    assertSqlWithParams(builder, "基本 SELECT 查询",
        "SELECT id, name, email FROM users WHERE status = ? AND (age > ?) ORDER BY created_at DESC LIMIT 10",
        new Object[] { 1, 18 });
  }

  /**
   * 测试 JOIN 和 WHERE 条件。
   */
  @Test
  @DisplayName("JOIN 和 WHERE 条件")
  void testJoinAndWhere() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users", "u")
        .column("u.id", "u.name", "o.total")
        .leftJoin("orders", "o", "u.id = o.user_id")
        .eqIfNotNull("u.status", 1)
        .like("u.name", "张")
        .orderByAsc("u.name")
        .limit(20);

    String sql = builder.getSql();
    Object[] params = builder.getParams();

    System.out.println("JOIN 和 WHERE 条件 SQL:");
    System.out.println(sql);
    System.out.println("Params: " + java.util.Arrays.toString(params));
    System.out.println();

    String expectedSql = "SELECT u.id, u.name, o.total FROM users AS u "
        + "LEFT JOIN orders AS o ON u.id = o.user_id "
        + "WHERE u.status = ? AND (u.name LIKE ?) ORDER BY u.name ASC LIMIT 20";

    assertEquals(expectedSql, sql, "生成的SQL应该完全匹配预期");
    assertArrayEquals(new Object[] { 1, "%张%" }, params, "参数应该匹配");
  }

  /**
   * 测试 GROUP BY 和 HAVING。
   */
  @Test
  @DisplayName("GROUP BY 和 HAVING")
  void testGroupByHaving() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
        .column("user_id", "COUNT(*) AS order_count", "SUM(amount) AS total_amount")
        .eq("status", "completed")
        .groupBy("user_id")
        .havingCountGt(5)
        .orderByDesc("total_amount")
        .limit(10);

    assertSqlWithParams(builder, "GROUP BY 和 HAVING",
        "SELECT user_id, COUNT(*) AS order_count, SUM(amount) AS total_amount "
            + "FROM orders WHERE status = ? GROUP BY user_id HAVING COUNT(*) > ? "
            + "ORDER BY total_amount DESC LIMIT 10",
        new Object[] { "completed", 5L });
  }

  /**
   * 测试 UNION 查询。
   */
  @Test
  @DisplayName("UNION 查询")
  void testUnion() {
    SelectSqlBuilder query1 = SelectSqlBuilder.create(mysql, "users")
        .column("name", "email")
        .eq("type", "admin");

    SelectSqlBuilder query2 = SelectSqlBuilder.create(mysql, "customers")
        .column("name", "email")
        .eq("vip", 1);

    SelectSqlBuilder builder = query1.union(query2);

    String sql = builder.getSql();
    Object[] params = builder.getParams();

    System.out.println("UNION 查询 SQL:");
    System.out.println(sql);
    System.out.println("Params: " + java.util.Arrays.toString(params));
    System.out.println();

    String expectedSql = "SELECT name, email FROM users WHERE type = ?\n"
        + "UNION\n"
        + "SELECT name, email FROM customers WHERE vip = ?";

    assertEquals(expectedSql, sql, "生成的SQL应该完全匹配预期");
    assertArrayEquals(new Object[] { "admin", 1 }, params, "参数应该匹配");
  }

  /**
   * 测试复杂查询（综合功能）。
   */
  @Test
  @DisplayName("复杂查询（综合功能）")
  void testComplexQuery() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "products", "p")
        .distinct()
        .column("p.id", "p.name", "p.price", "c.name AS category_name")
        .leftJoin("categories", "c", "p.category_id = c.id")
        .eqIfNotNull("p.status", 1)
        .gtIfNotNull("p.price", 100)
        .in("p.category_id", java.util.Arrays.asList(1, 2, 3))
        .likeIfNotBlank("p.name", "手机")
        .isNotNull("p.inventory")
        .groupBy("p.id", "c.name")
        .havingCountGt(0)
        .orderBy("p.price", false)
        .orderByAsc("p.name")
        .limitPage(2, 20);

    String sql = builder.getSql();
    Object[] params = builder.getParams();

    System.out.println("复杂查询（综合功能） SQL:");
    System.out.println(sql);
    System.out.println("Params: " + java.util.Arrays.toString(params));
    System.out.println();

    String expectedSql = "SELECT DISTINCT p.id, p.name, p.price, c.name AS category_name "
        + "FROM products AS p LEFT JOIN categories AS c ON p.category_id = c.id "
        + "WHERE p.status = ? AND (p.price > ?) AND (p.category_id IN (?, ?, ?)) "
        + "AND (p.name LIKE ?) AND (p.inventory IS NOT NULL) "
        + "GROUP BY p.id, c.name HAVING COUNT(*) > ? "
        + "ORDER BY p.price DESC, p.name ASC LIMIT 20 OFFSET 20";

    assertEquals(expectedSql, sql, "生成的SQL应该完全匹配预期");
    assertArrayEquals(new Object[] { 1, 100, 1, 2, 3, "%手机%", 0L }, params, "参数应该匹配");

    // 测试 COUNT SQL
    String countSql = builder.getCountSql();
    System.out.println("COUNT SQL:");
    System.out.println(countSql);
    System.out.println();

    String expectedCountSql = "SELECT count(1) AS cnt FROM products AS p LEFT JOIN categories AS c ON p.category_id = c.id "
        + "WHERE p.status = ? AND (p.price > ?) AND (p.category_id IN (?, ?, ?)) "
        + "AND (p.name LIKE ?) AND (p.inventory IS NOT NULL) GROUP BY p.id, c.name HAVING COUNT(*) > ?";

    assertEquals(expectedCountSql, countSql, "COUNT SQL应该完全匹配预期");
  }
}
