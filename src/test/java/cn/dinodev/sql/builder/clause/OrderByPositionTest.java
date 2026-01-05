// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.builder.SelectSqlBuilder;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

/**
 * ORDER BY 位置排序功能测试类。
 * 
 * <p>测试使用列序号（position）进行排序的功能，如：
 * <ul>
 *   <li>ORDER BY 1, 2 DESC</li>
 *   <li>基于 SELECT 列表的位置排序</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2024-12-31
 */
@DisplayName("ORDER BY位置排序功能测试")
public class OrderByPositionTest {

  private MysqlDialect mysqlDialect;

  @BeforeEach
  public void setUp() {
    mysqlDialect = new MysqlDialect(null, new CamelNamingConversition());
  }

  @Test
  @DisplayName("测试基本位置排序")
  void testOrderByPosition() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysqlDialect, "users")
        .columns("name", "age", "score")
        .orderByPosition(3, false)
        .orderByPosition(1, true);

    assertSql(builder, "基本位置排序",
        "SELECT name, age, score FROM users ORDER BY 3 DESC, 1 ASC");
  }

  @Test
  @DisplayName("测试orderByPositionAsc升序排序")
  void testOrderByPositionAsc() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysqlDialect, "users")
        .columns("name", "age", "score", "status")
        .orderByPositionAsc(1, 2, 3);

    assertSql(builder, "orderByPositionAsc升序",
        "SELECT name, age, score, status FROM users ORDER BY 1 ASC, 2 ASC, 3 ASC");
  }

  @Test
  @DisplayName("测试orderByPositionDesc降序排序")
  void testOrderByPositionDesc() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysqlDialect, "products")
        .columns("id", "name", "price", "stock")
        .orderByPositionDesc(3, 4);

    assertSql(builder, "orderByPositionDesc降序",
        "SELECT id, name, price, stock FROM products ORDER BY 3 DESC, 4 DESC");
  }

  @Test
  @DisplayName("测试混合位置排序")
  void testOrderByPositionMixed() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysqlDialect, "orders")
        .columns("id", "customer", "amount", "created_at")
        .orderByPositionDesc(3)
        .orderByPositionAsc(4, 1);

    assertSql(builder, "混合位置排序",
        "SELECT id, customer, amount, created_at FROM orders ORDER BY 3 DESC, 4 ASC, 1 ASC");
  }
}
