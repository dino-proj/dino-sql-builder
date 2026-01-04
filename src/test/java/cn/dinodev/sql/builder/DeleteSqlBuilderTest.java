// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSqlWithParams;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.dialect.Dialect;
import cn.dinodev.sql.dialect.MysqlDialect;

/**
 * DELETE 语句构建器测试类。
 * 
 * <p>测试 {@link DeleteSqlBuilder} 的基本功能，包括：
 * <ul>
 *   <li>DELETE 语句构建</li>
 *   <li>WHERE 条件</li>
 *   <li>表别名</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("DELETE构建器测试")
public class DeleteSqlBuilderTest {

  private Dialect dialect;

  @BeforeEach
  public void setUp() {
    dialect = new MysqlDialect(null, null);
  }

  @Test
  @DisplayName("测试基础DELETE构建器")
  void testBasicDeleteBuilder() {
    DeleteSqlBuilder deleteBuilder = DeleteSqlBuilder.create(dialect, "users")
        .where("age > ?", 18)
        .and("status = ?", 1);

    assertSqlWithParams(deleteBuilder, "基础DELETE",
        "DELETE FROM users WHERE age > ? AND (status = ?)",
        new Object[] { 18, 1 });
    assertEquals(2, deleteBuilder.getParamCount());
  }

  @Test
  @DisplayName("测试带别名的DELETE构建器")
  void testDeleteBuilderWithAlias() {
    DeleteSqlBuilder deleteBuilder = DeleteSqlBuilder.create(dialect, "users", "u")
        .where("u.age > ?", 30)
        .or("u.name = ?", "John");

    assertSqlWithParams(deleteBuilder, "带别名的DELETE",
        "DELETE FROM users AS u WHERE u.age > ? OR (u.name = ?)",
        new Object[] { 30, "John" });
  }
}
