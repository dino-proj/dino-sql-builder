// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.wheres;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSqlWithParams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.Logic;
import cn.dinodev.sql.builder.SelectSqlBuilder;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

/**
 * 空值检查 WHERE 子句测试类。
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("NullCheckWhereClause 测试")
public class NullCheckWhereClauseTest {

  private MysqlDialect mysql;

  @BeforeEach
  public void setUp() {
    mysql = new MysqlDialect(null, new CamelNamingConversition());
  }

  @Test
  @DisplayName("isNull - 基本用法")
  void testIsNull() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .isNull("deleted_at");

    assertSqlWithParams(builder, "isNull",
        "SELECT * FROM users WHERE deleted_at IS NULL",
        new Object[] {});
  }

  @Test
  @DisplayName("isNull - 带逻辑符")
  void testIsNullWithLogic() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eq("status", 1)
        .isNull("deleted_at", Logic.OR);

    assertSqlWithParams(builder, "isNull with logic",
        "SELECT * FROM users WHERE status = ? OR (deleted_at IS NULL)",
        new Object[] { 1 });
  }

  @Test
  @DisplayName("isNullIf - 条件判断（true）")
  void testIsNullIfTrue() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .isNullIf(true, "deleted_at");

    assertSqlWithParams(builder, "isNullIf true",
        "SELECT * FROM users WHERE deleted_at IS NULL",
        new Object[] {});
  }

  @Test
  @DisplayName("isNullIf - 条件判断（false）")
  void testIsNullIfFalse() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .isNullIf(false, "deleted_at");

    assertSqlWithParams(builder, "isNullIf false",
        "SELECT * FROM users",
        new Object[] {});
  }

  @Test
  @DisplayName("isNotNull - 基本用法")
  void testIsNotNull() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .isNotNull("email");

    assertSqlWithParams(builder, "isNotNull",
        "SELECT * FROM users WHERE email IS NOT NULL",
        new Object[] {});
  }

  @Test
  @DisplayName("isNotNull - 带逻辑符")
  void testIsNotNullWithLogic() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eq("status", 1)
        .isNotNull("email", Logic.OR);

    assertSqlWithParams(builder, "isNotNull with logic",
        "SELECT * FROM users WHERE status = ? OR (email IS NOT NULL)",
        new Object[] { 1 });
  }

  @Test
  @DisplayName("isNotNullIf - 条件判断（true）")
  void testIsNotNullIfTrue() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .isNotNullIf(true, "email");

    assertSqlWithParams(builder, "isNotNullIf true",
        "SELECT * FROM users WHERE email IS NOT NULL",
        new Object[] {});
  }

  @Test
  @DisplayName("isNotNullIf - 条件判断（false）")
  void testIsNotNullIfFalse() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .isNotNullIf(false, "email");

    assertSqlWithParams(builder, "isNotNullIf false",
        "SELECT * FROM users",
        new Object[] {});
  }

  @Test
  @DisplayName("组合测试 - 多个空值检查")
  void testCombined() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eq("status", 1)
        .isNull("deleted_at")
        .isNotNull("email")
        .isNotNull("phone");

    assertSqlWithParams(builder, "combined",
        "SELECT * FROM users WHERE status = ? AND (deleted_at IS NULL) AND (email IS NOT NULL) AND (phone IS NOT NULL)",
        new Object[] { 1 });
  }

  @Test
  @DisplayName("实际场景 - 筛选活跃用户")
  void testActiveUsers() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("id", "name", "email")
        .eq("status", 1)
        .isNull("deleted_at")
        .isNotNull("email")
        .isNotNull("last_login_at");

    assertSqlWithParams(builder, "active users",
        "SELECT id, name, email FROM users WHERE status = ? AND (deleted_at IS NULL) AND (email IS NOT NULL) AND (last_login_at IS NOT NULL)",
        new Object[] { 1 });
  }
}
