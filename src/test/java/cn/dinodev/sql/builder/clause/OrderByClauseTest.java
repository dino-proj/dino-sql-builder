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
 * ORDER BY 子句测试类。
 * 
 * <p>测试 {@link SelectSqlBuilder} 中 ORDER BY 相关功能，包括：
 * <ul>
 *   <li>基本排序（ASC/DESC）</li>
 *   <li>多列排序</li>
 *   <li>链式调用排序方法</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2024-12-31
 */
@DisplayName("ORDER BY子句测试")
public class OrderByClauseTest {

  private MysqlDialect mysql;

  @BeforeEach
  public void setUp() {
    mysql = new MysqlDialect(null, new CamelNamingConversition());
  }

  /**
   * 测试基本排序。
   */
  @Test
  @DisplayName("基本排序")
  void testBasicOrderBy() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("id", "name", "age")
        .orderBy("age DESC");

    assertSql(builder, "基本排序",
        "SELECT id, name, age FROM users ORDER BY age DESC");
  }

  /**
   * 测试多列排序。
   */
  @Test
  @DisplayName("多列排序")
  void testMultipleOrderBy() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("id", "name", "age")
        .orderBy("age DESC", "name ASC");

    assertSql(builder, "多列排序",
        "SELECT id, name, age FROM users ORDER BY age DESC, name ASC");
  }

  /**
   * 测试 orderByAsc 和 orderByDesc。
   */
  @Test
  @DisplayName("orderByAsc 和 orderByDesc")
  void testOrderByAscDesc() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("id", "name", "age", "created_at")
        .orderByDesc("created_at")
        .orderByAsc("name");

    assertSql(builder, "orderByAsc 和 orderByDesc",
        "SELECT id, name, age, created_at FROM users ORDER BY created_at DESC, name ASC");
  }

  /**
   * 测试条件排序。
   */
  @Test
  @DisplayName("条件排序")
  void testOrderByCondition() {
    boolean sortByAge = true;
    SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysql, "users")
        .column("id", "name", "age")
        .orderByIf(sortByAge, "age DESC")
        .orderBy("name ASC");

    assertSql(builder1, "条件排序(true)",
        "SELECT id, name, age FROM users ORDER BY age DESC, name ASC");

    sortByAge = false;
    SelectSqlBuilder builder2 = SelectSqlBuilder.create(mysql, "users")
        .column("id", "name", "age")
        .orderByIf(sortByAge, "age DESC")
        .orderBy("name ASC");

    assertSql(builder2, "条件排序(false)",
        "SELECT id, name, age FROM users ORDER BY name ASC");
  }
}
