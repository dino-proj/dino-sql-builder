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
 * 正则表达式 WHERE 子句测试类。
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("RegexpWhereClause 测试")
public class RegexpWhereClauseTest {

  private MysqlDialect mysql;

  @BeforeEach
  public void setUp() {
    mysql = new MysqlDialect(null, new CamelNamingConversition());
  }

  @Test
  @DisplayName("regexp - MySQL基本用法")
  void testRegexpMySQL() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .regexp("email", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    assertSqlWithParams(builder, "regexp mysql",
        "SELECT * FROM users WHERE email REGEXP ?",
        new Object[] { "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$" });
  }

  @Test
  @DisplayName("regexp - PostgreSQL基本用法")
  void testRegexpPostgreSc() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eq("status", 1)
        .regexp("phone", "^\\d{11}$", Logic.OR);

    assertSqlWithParams(builder, "regexp with logic",
        "SELECT * FROM users WHERE status = ? OR (phone REGEXP ?)",
        new Object[] { 1, "^\\d{11}$" });
  }

  @Test
  @DisplayName("regexpIf - 条件判断")
  void testRegexpIf() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .regexpIf(true, "email", "^[a-zA-Z]+@")
        .regexpIf(false, "phone", "^138");

    assertSqlWithParams(builder, "regexpIf",
        "SELECT * FROM users WHERE email REGEXP ?",
        new Object[] { "^[a-zA-Z]+@" });
  }

  @Test
  @DisplayName("regexpIfNotBlank - 非空判断")
  void testRegexpIfNotBlank() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .regexpIfNotBlank("email", "^admin@")
        .regexpIfNotBlank("phone", "")
        .regexpIfNotBlank("code", null);

    assertSqlWithParams(builder, "regexpIfNotBlank",
        "SELECT * FROM users WHERE email REGEXP ?",
        new Object[] { "^admin@" });
  }

  @Test
  @DisplayName("notRegexp - 排除匹配")
  void testNotRegexp() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "products")
        .column("*")
        .notRegexp("code", "^[0-9]+$");

    assertSqlWithParams(builder, "notRegexp",
        "SELECT * FROM products WHERE code NOT REGEXP ?",
        new Object[] { "^[0-9]+$" });
  }

  @Test

  @DisplayName("组合测试 - 多个正则条件")
  void testCombined() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eq("status", 1)
        .regexp("email", "@example\\.com$")
        .regexp("phone", "^1[3-9]\\d{9}$")
        .notRegexp("code", "^TEST");

    assertSqlWithParams(builder, "combined",
        "SELECT * FROM users WHERE status = ? AND (email REGEXP ?) AND (phone REGEXP ?) AND (code NOT REGEXP ?)",
        new Object[] { 1, "@example\\.com$", "^1[3-9]\\d{9}$", "^TEST" });
  }

  @Test
  @DisplayName("实际场景 - 邮箱和手机号验证")
  void testEmailPhoneValidation() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .columns("id", "name", "email", "phone")
        .eq("status", 1)
        .regexp("email", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        .regexp("phone", "^1[3-9]\\d{9}$");

    assertSqlWithParams(builder, "email phone validation",
        "SELECT id, name, email, phone FROM users WHERE status = ? AND (email REGEXP ?) AND (phone REGEXP ?)",
        new Object[] { 1, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", "^1[3-9]\\d{9}$" });
  }
}
