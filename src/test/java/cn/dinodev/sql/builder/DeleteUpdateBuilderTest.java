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
 * DELETE 和 UPDATE 语句构建器测试类。
 * 
 * <p>测试 {@link DeleteSqlBuilder} 和 {@link UpdateSqlBuilder} 的基本功能，包括：
 * <ul>
 *   <li>DELETE 语句构建</li>
 *   <li>UPDATE 语句构建</li>
 *   <li>WHERE 条件</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2026-01-03
 */
@DisplayName("DELETE和UPDATE构建器测试")
public class DeleteUpdateBuilderTest {

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

  @Test
  @DisplayName("测试基础UPDATE构建器")
  void testBasicUpdateBuilder() {
    UpdateSqlBuilder updateBuilder = UpdateSqlBuilder.create(dialect, "users")
        .set("name = ?", "Alice")
        .set("age = ?", 25)
        .where("id = ?", 1);

    assertSqlWithParams(updateBuilder, "基础UPDATE",
        "UPDATE users SET name = ?, age = ? WHERE id = ?",
        new Object[] { "Alice", 25, 1 });
    assertEquals(3, updateBuilder.getParamCount());
  }

  @Test
  @DisplayName("测试带别名的UPDATE构建器")
  void testUpdateBuilderWithAlias() {
    UpdateSqlBuilder updateBuilder = UpdateSqlBuilder.create(dialect, "users", "u")
        .set("u.status = ?", 0)
        .set("u.updated_at = NOW()")
        .where("u.age > ?", 60)
        .and("u.status = ?", 1);

    assertSqlWithParams(updateBuilder, "带别名的UPDATE",
        "UPDATE users AS u SET u.status = ?, u.updated_at = NOW() WHERE u.age > ? AND (u.status = ?)",
        new Object[] { 0, 60, 1 });
  }

  @Test
  @DisplayName("测试UPDATE条件方法")
  void testUpdateConditionalMethods() {
    UpdateSqlBuilder updateBuilder = UpdateSqlBuilder.create(dialect, "users")
        .set("name = ?", "Bob")
        .setIf(true, "age = ?", 30)
        .setIfNotNull("email = ?", "bob@example.com")
        .setIf(false, "phone = ?", "123456")
        .where("id = ?", 2);

    assertSqlWithParams(updateBuilder, "UPDATE条件方法",
        "UPDATE users SET name = ?, age = ?, email = ? WHERE id = ?",
        new Object[] { "Bob", 30, "bob@example.com", 2 });
  }
}
