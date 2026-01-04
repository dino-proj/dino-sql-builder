// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.builder.SelectSqlBuilder;
import cn.dinodev.sql.dialect.Dialect;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

/**
 * JOIN 子句测试类。
 * 
 * <p>测试 JOIN 相关功能，包括：
 * <ul>
 *   <li>INNER JOIN</li>
 *   <li>LEFT JOIN</li>
 *   <li>RIGHT JOIN</li>
 *   <li>多表连接</li>
 *   <li>复杂 ON 条件</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("JOIN子句测试")
public class JoinClauseTest {

  private Dialect dialect;

  @BeforeEach
  public void setUp() {
    dialect = new MysqlDialect(null, new CamelNamingConversition());
  }

  @Test
  @DisplayName("测试INNER JOIN")
  void testInnerJoin() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "users", "u")
        .column("u.id", "u.name", "o.order_id")
        .innerJoin("orders", "o", "u.id = o.user_id");

    assertSql(builder, "INNER JOIN",
        "SELECT u.id, u.name, o.order_id FROM users AS u JOIN orders AS o ON u.id = o.user_id");
  }

  @Test
  @DisplayName("测试LEFT JOIN")
  void testLeftJoin() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "users", "u")
        .column("u.id", "u.name", "o.order_id")
        .leftJoin("orders", "o", "u.id = o.user_id");

    assertSql(builder, "LEFT JOIN",
        "SELECT u.id, u.name, o.order_id FROM users AS u LEFT JOIN orders AS o ON u.id = o.user_id");
  }

  @Test
  @DisplayName("测试RIGHT JOIN")
  void testRightJoin() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "users", "u")
        .column("u.id", "u.name", "o.order_id")
        .rightJoin("orders", "o", "u.id = o.user_id");

    assertSql(builder, "RIGHT JOIN",
        "SELECT u.id, u.name, o.order_id FROM users AS u RIGHT JOIN orders AS o ON u.id = o.user_id");
  }

  @Test
  @DisplayName("测试多表JOIN")
  void testMultipleJoins() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "users", "u")
        .column("u.id", "u.name", "o.order_id", "p.product_name")
        .innerJoin("orders", "o", "u.id = o.user_id")
        .leftJoin("products", "p", "o.product_id = p.id");

    assertSql(builder, "多表JOIN",
        "SELECT u.id, u.name, o.order_id, p.product_name FROM users AS u " +
            "JOIN orders AS o ON u.id = o.user_id " +
            "LEFT JOIN products AS p ON o.product_id = p.id");
  }

  @Test
  @DisplayName("测试复杂ON条件")
  void testComplexJoinCondition() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "users", "u")
        .column("u.id", "u.name", "o.order_id")
        .innerJoin("orders", "o", "u.id = o.user_id AND o.status = 1");

    assertSql(builder, "复杂ON条件",
        "SELECT u.id, u.name, o.order_id FROM users AS u " +
            "JOIN orders AS o ON u.id = o.user_id AND o.status = 1");
  }
}
