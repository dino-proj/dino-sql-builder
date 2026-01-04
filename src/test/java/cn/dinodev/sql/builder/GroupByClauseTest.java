// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSql;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.dialect.Dialect;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.dialect.PostgreSQLDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;
import cn.dinodev.sql.testutil.DatabaseMetaDataMocks;

/**
 * GROUP BY 子句功能测试类。
 * 
 * <p>测试 {@link SelectSqlBuilder} 中 GROUP BY 相关功能，包括：
 * <ul>
 *   <li>基本的单列和多列分组</li>
 *   <li>GROUP BY 表达式和函数</li>
 *   <li>GROUP BY ALL 语法（PostgreSQL 17+）</li>
 *   <li>异常情况和边界测试</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2024-12-31
 */
@DisplayName("GroupByClause 接口功能测试")
public class GroupByClauseTest {

  private Dialect mysql;
  private Dialect postgresql;

  @BeforeEach
  public void setUp() {
    mysql = new MysqlDialect(null, new CamelNamingConversition());
    try {
      postgresql = new PostgreSQLDialect(
          DatabaseMetaDataMocks.createPostgreSQL(17),
          new CamelNamingConversition());
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize PostgreSQL dialect", e);
    }
  }

  /**
   * 测试 1: 基本 GROUP BY 功能
   */
  @Test
  @DisplayName("基本 GROUP BY")
  void testBasicGroupBy() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
        .column("customer_id", "COUNT(*) AS order_count")
        .groupBy("customer_id");

    assertSql(builder, "基本 GROUP BY",
        "SELECT customer_id, COUNT(*) AS order_count FROM orders GROUP BY customer_id");
  }

  /**
   * 测试 2: 多字段 GROUP BY
   */
  @Test
  @DisplayName("多字段 GROUP BY")
  void testMultipleGroupBy() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
        .column("customer_id", "status", "COUNT(*) AS order_count")
        .groupBy("customer_id", "status");

    assertSql(builder, "多字段 GROUP BY",
        "SELECT customer_id, status, COUNT(*) AS order_count FROM orders GROUP BY customer_id, status");
  }

  /**
   * 测试 3: 逗号分隔的分组字段
   */
  @Test
  @DisplayName("逗号分隔的分组字段")
  void testGroupByWithCommaDelimited() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
        .column("customer_id", "status", "COUNT(*) AS order_count")
        .groupBy("customer_id, status");

    assertSql(builder, "逗号分隔的分组字段",
        "SELECT customer_id, status, COUNT(*) AS order_count FROM orders GROUP BY customer_id, status");
  }

  /**
   * 测试 4: GROUP BY 表达式
   */
  @Test
  @DisplayName("GROUP BY 表达式")
  void testGroupByWithExpression() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
        .column("DATE(created_at) AS order_date", "COUNT(*) AS order_count")
        .groupBy("DATE(created_at)");

    assertSql(builder, "GROUP BY 表达式",
        "SELECT DATE(created_at) AS order_date, COUNT(*) AS order_count FROM orders GROUP BY DATE(created_at)");
  }

  /**
   * 测试 5: 条件 GROUP BY
   */
  @Test
  @DisplayName("条件 GROUP BY")
  void testGroupByIf() {
    // 条件为真
    SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysql, "orders")
        .column("customer_id", "COUNT(*) AS order_count")
        .groupByIf(true, "customer_id");

    assertSql(builder1, "条件 GROUP BY (true)",
        "SELECT customer_id, COUNT(*) AS order_count FROM orders GROUP BY customer_id");

    // 条件为假
    SelectSqlBuilder builder2 = SelectSqlBuilder.create(mysql, "orders")
        .column("customer_id", "COUNT(*) AS order_count")
        .groupByIf(false, "customer_id");

    assertSql(builder2, "条件 GROUP BY (false)",
        "SELECT customer_id, COUNT(*) AS order_count FROM orders");
  }

  /**
   * 测试 6: GROUP BY ALL（PostgreSQL 17+）
   */
  @Test
  @DisplayName("GROUP BY ALL (PostgreSQL 17+)")
  void testGroupByAll() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(postgresql, "orders")
        .column("customer_id", "status", "COUNT(*) AS order_count")
        .groupByAll();

    assertSql(builder, "GROUP BY ALL (PostgreSQL 17+)",
        "SELECT customer_id, status, COUNT(*) AS order_count FROM orders GROUP BY ALL");

    // 测试不支持的方言
    assertThrows(UnsupportedOperationException.class, () -> {
      SelectSqlBuilder.create(mysql, "orders")
          .column("customer_id", "COUNT(*) AS order_count")
          .groupByAll();
    });
  }

  /**
   * 测试 7: 条件 GROUP BY ALL
   */
  @Test
  @DisplayName("条件 GROUP BY ALL (PostgreSQL 17+)")
  void testGroupByAllIf() {
    // 条件为真
    SelectSqlBuilder builder1 = SelectSqlBuilder.create(postgresql, "orders")
        .column("customer_id", "status", "COUNT(*) AS order_count")
        .groupByAllIf(true);

    assertSql(builder1, "条件 GROUP BY ALL (true)",
        "SELECT customer_id, status, COUNT(*) AS order_count FROM orders GROUP BY ALL");

    // 条件为假
    SelectSqlBuilder builder2 = SelectSqlBuilder.create(postgresql, "orders")
        .column("customer_id", "status", "COUNT(*) AS order_count")
        .groupByAllIf(false);

    assertSql(builder2, "条件 GROUP BY ALL (false)",
        "SELECT customer_id, status, COUNT(*) AS order_count FROM orders");

    // 测试不支持的方言（条件为真时才抛出异常）
    assertThrows(UnsupportedOperationException.class, () -> {
      SelectSqlBuilder.create(mysql, "orders")
          .column("customer_id", "COUNT(*) AS order_count")
          .groupByAllIf(true);
    });

    // 测试不支持的方言（条件为假时不抛出异常）
    assertDoesNotThrow(() -> {
      SelectSqlBuilder.create(mysql, "orders")
          .column("customer_id", "COUNT(*) AS order_count")
          .groupByAllIf(false);
    });
  }

  /**
   * 测试 8: GROUP BY 与 HAVING 结合
   */
  @Test
  @DisplayName("GROUP BY 与 HAVING 结合")
  void testGroupByWithHaving() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
        .column("customer_id", "COUNT(*) AS order_count")
        .groupBy("customer_id")
        .having("COUNT(*) > 5");

    assertSql(builder, "GROUP BY 与 HAVING 结合",
        "SELECT customer_id, COUNT(*) AS order_count FROM orders GROUP BY customer_id HAVING COUNT(*) > 5");
  }

  /**
   * 测试 9: 空 GROUP BY（不调用任何 groupBy 方法）
   */
  @Test
  @DisplayName("空 GROUP BY")
  void testEmptyGroupBy() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
        .column("customer_id", "COUNT(*) AS order_count");

    assertSql(builder, "空 GROUP BY",
        "SELECT customer_id, COUNT(*) AS order_count FROM orders");
  }

  /**
   * 测试 10: 链式调用
   */
  @Test
  @DisplayName("链式调用")
  void testChainability() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
        .column("customer_id", "status", "COUNT(*) AS order_count", "SUM(total) AS total_amount")
        .groupBy("customer_id")
        .groupBy("status")
        .having("COUNT(*) > 5")
        .orderBy("COUNT(*) DESC")
        .limit(10);

    assertSql(builder, "链式调用",
        "SELECT customer_id, status, COUNT(*) AS order_count, SUM(total) AS total_amount FROM orders GROUP BY customer_id, status HAVING COUNT(*) > 5 ORDER BY COUNT(*) DESC LIMIT 10");
  }

  /**
   * 测试 11: 异常场景 - 空字符串参数
   */
  @Test
  @DisplayName("异常场景 - 空字符串参数")
  public void testGroupByWithEmptyString() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
        .column("customer_id", "COUNT(*) AS cnt");

    // 空字符串应该被忽略或抛出异常，取决于实现
    assertDoesNotThrow(() -> builder.groupBy(""));
  }

  /**
   * 测试 12: 异常场景 - null 参数
   */
  @Test
  @DisplayName("异常场景 - null 参数")
  public void testGroupByWithNullString() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
        .column("customer_id", "COUNT(*) AS cnt");

    // null 参数应该被处理（忽略或抛出异常）
    assertDoesNotThrow(() -> builder.groupBy((String) null));
  }

  /**
   * 测试 13: 边界场景 - 大量分组列
   */
  @Test
  @DisplayName("边界场景 - 大量分组列")
  public void testGroupByWithManyColumns() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
        .column("col1", "col2", "col3", "col4", "col5", "col6", "col7", "col8", "col9", "col10")
        .groupBy("col1", "col2", "col3", "col4", "col5", "col6", "col7", "col8", "col9", "col10");

    String sql = builder.getSql();
    assertDoesNotThrow(() -> builder.getSql());
    // 验证包含所有分组列
    assert sql.contains("GROUP BY col1, col2, col3, col4, col5, col6, col7, col8, col9, col10");
  }
}