// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSql;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

/**
 * 参数化测试示例类。
 * 
 * <p>展示如何使用 JUnit 5 的参数化测试功能来减少重复代码。
 * 适用于需要测试多组相似数据的场景。
 * 
 * @author Cody Lu
 * @since 2026-01-03
 */
@DisplayName("参数化测试示例")
public class ParameterizedTestExample {

  private MysqlDialect mysql;

  @BeforeEach
  public void setUp() {
    mysql = new MysqlDialect(null, new CamelNamingConversition());
  }

  /**
   * 使用 @ValueSource 测试不同的字段名
   */
  @ParameterizedTest(name = "测试字段 {0} 的 GROUP BY")
  @ValueSource(strings = { "customer_id", "user_id", "order_id", "product_id" })
  @DisplayName("不同字段的 GROUP BY")
  public void testGroupByWithDifferentFields(String field) {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
        .column(field, "COUNT(*) AS cnt")
        .groupBy(field);

    String sql = builder.getSql();
    assertTrue(sql.contains("GROUP BY " + field));
    assertTrue(sql.contains(field));
  }

  /**
   * 使用 @CsvSource 测试多组数据
   */
  @ParameterizedTest(name = "LIMIT {0}, OFFSET {1}")
  @CsvSource({
      "10, 0",
      "20, 10",
      "50, 100",
      "100, 500"
  })
  @DisplayName("不同的 LIMIT 和 OFFSET 组合")
  public void testLimitOffsetCombinations(int limit, int offset) {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("id", "name")
        .limit(limit);

    if (offset > 0) {
      builder.offset(offset);
    }

    String sql = builder.getSql();
    assertTrue(sql.contains("LIMIT " + limit));
    if (offset > 0) {
      assertTrue(sql.contains("OFFSET " + offset));
    }
  }

  /**
   * 使用 @CsvSource 测试排序方向
   */
  @ParameterizedTest(name = "ORDER BY {0} {1}")
  @CsvSource({
      "name, ASC",
      "age, DESC",
      "created_at, ASC",
      "updated_at, DESC"
  })
  @DisplayName("不同列和排序方向的组合")
  public void testOrderByDirections(String column, String direction) {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("id", column)
        .orderBy(column + " " + direction);

    assertSql(builder, "ORDER BY " + column + " " + direction,
        "SELECT id, " + column + " FROM users ORDER BY " + column + " " + direction);
  }
}
