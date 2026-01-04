// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.wheres;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSqlWithParams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.Oper;
import cn.dinodev.sql.builder.SelectSqlBuilder;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

/**
 * 条件控制 WHERE 子句测试类。
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("ConditionalWhereClause 测试")
public class ConditionalWhereClauseTest {

  private MysqlDialect mysql;

  @BeforeEach
  public void setUp() {
    mysql = new MysqlDialect(null, new CamelNamingConversition());
  }

  @Test
  @DisplayName("whereIf - 表达式（true）")
  void testWhereIfExprTrue() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .whereIf(true, "age > ?", 18);

    assertSqlWithParams(builder, "whereIf expr true",
        "SELECT * FROM users WHERE age > ?",
        new Object[] { 18 });
  }

  @Test
  @DisplayName("whereIf - 表达式（false）")
  void testWhereIfExprFalse() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .whereIf(false, "age > ?", 18);

    assertSqlWithParams(builder, "whereIf expr false",
        "SELECT * FROM users",
        new Object[] {});
  }

  @Test
  @DisplayName("whereIf - 列操作符（true）")
  void testWhereIfColumnTrue() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .whereIf(true, "age", Oper.GT, 18);

    assertSqlWithParams(builder, "whereIf column true",
        "SELECT * FROM users WHERE age > ?",
        new Object[] { 18 });
  }

  @Test
  @DisplayName("whereIf - 列操作符（false）")
  void testWhereIfColumnFalse() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .whereIf(false, "age", Oper.GT, 18);

    assertSqlWithParams(builder, "whereIf column false",
        "SELECT * FROM users",
        new Object[] {});
  }

  @Test
  @DisplayName("whereIfNotNull - 表达式非空")
  void testWhereIfNotNullExpr() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .whereIfNotNull("age > ?", 18)
        .whereIfNotNull("name = ?", null);

    assertSqlWithParams(builder, "whereIfNotNull expr",
        "SELECT * FROM users WHERE age > ?",
        new Object[] { 18 });
  }

  @Test
  @DisplayName("whereIfNotNull - 列操作符非空")
  void testWhereIfNotNullColumn() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .whereIfNotNull("age", Oper.GT, 18)
        .whereIfNotNull("name", Oper.EQ, null);

    assertSqlWithParams(builder, "whereIfNotNull column",
        "SELECT * FROM users WHERE age > ?",
        new Object[] { 18 });
  }

  @Test
  @DisplayName("andIf - 表达式（true）")
  void testAndIfExprTrue() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .where("status = ?", 1)
        .andIf(true, "age > ?", 18);

    assertSqlWithParams(builder, "andIf expr true",
        "SELECT * FROM users WHERE status = ? AND (age > ?)",
        new Object[] { 1, 18 });
  }

  @Test
  @DisplayName("andIf - 表达式（false）")
  void testAndIfExprFalse() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .where("status = ?", 1)
        .andIf(false, "age > ?", 18);

    assertSqlWithParams(builder, "andIf expr false",
        "SELECT * FROM users WHERE status = ?",
        new Object[] { 1 });
  }

  @Test
  @DisplayName("andIf - 列操作符（true）")
  void testAndIfColumnTrue() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .where("status = ?", 1)
        .andIf(true, "age", Oper.GT, 18);

    assertSqlWithParams(builder, "andIf column true",
        "SELECT * FROM users WHERE status = ? AND (age > ?)",
        new Object[] { 1, 18 });
  }

  @Test
  @DisplayName("andIf - 列操作符（false）")
  void testAndIfColumnFalse() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .where("status = ?", 1)
        .andIf(false, "age", Oper.GT, 18);

    assertSqlWithParams(builder, "andIf column false",
        "SELECT * FROM users WHERE status = ?",
        new Object[] { 1 });
  }

  @Test
  @DisplayName("andIfNotNull - 表达式非空")
  void testAndIfNotNullExpr() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .where("status = ?", 1)
        .andIfNotNull("age > ?", 18)
        .andIfNotNull("name = ?", null);

    assertSqlWithParams(builder, "andIfNotNull expr",
        "SELECT * FROM users WHERE status = ? AND (age > ?)",
        new Object[] { 1, 18 });
  }

  @Test
  @DisplayName("andIfNotNull - 列操作符非空")
  void testAndIfNotNullColumn() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .where("status = ?", 1)
        .andIfNotNull("age", Oper.GT, 18)
        .andIfNotNull("name", Oper.EQ, null);

    assertSqlWithParams(builder, "andIfNotNull column",
        "SELECT * FROM users WHERE status = ? AND (age > ?)",
        new Object[] { 1, 18 });
  }

  @Test
  @DisplayName("orIf - 表达式（true）")
  void testOrIfExprTrue() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .where("status = ?", 1)
        .orIf(true, "age > ?", 60);

    assertSqlWithParams(builder, "orIf expr true",
        "SELECT * FROM users WHERE status = ? OR (age > ?)",
        new Object[] { 1, 60 });
  }

  @Test
  @DisplayName("orIf - 表达式（false）")
  void testOrIfExprFalse() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .where("status = ?", 1)
        .orIf(false, "age > ?", 60);

    assertSqlWithParams(builder, "orIf expr false",
        "SELECT * FROM users WHERE status = ?",
        new Object[] { 1 });
  }

  @Test
  @DisplayName("orIf - 列操作符（true）")
  void testOrIfColumnTrue() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .where("status = ?", 1)
        .orIf(true, "age", Oper.GT, 60);

    assertSqlWithParams(builder, "orIf column true",
        "SELECT * FROM users WHERE status = ? OR (age > ?)",
        new Object[] { 1, 60 });
  }

  @Test
  @DisplayName("orIf - 列操作符（false）")
  void testOrIfColumnFalse() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .where("status = ?", 1)
        .orIf(false, "age", Oper.GT, 60);

    assertSqlWithParams(builder, "orIf column false",
        "SELECT * FROM users WHERE status = ?",
        new Object[] { 1 });
  }

  @Test
  @DisplayName("orIfNotNull - 表达式非空")
  void testOrIfNotNullExpr() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .where("status = ?", 1)
        .orIfNotNull("age > ?", 60)
        .orIfNotNull("name = ?", null);

    assertSqlWithParams(builder, "orIfNotNull expr",
        "SELECT * FROM users WHERE status = ? OR (age > ?)",
        new Object[] { 1, 60 });
  }

  @Test
  @DisplayName("orIfNotNull - 列操作符非空")
  void testOrIfNotNullColumn() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .where("status = ?", 1)
        .orIfNotNull("age", Oper.GT, 60)
        .orIfNotNull("name", Oper.EQ, null);

    assertSqlWithParams(builder, "orIfNotNull column",
        "SELECT * FROM users WHERE status = ? OR (age > ?)",
        new Object[] { 1, 60 });
  }

  @Test
  @DisplayName("组合测试 - 复杂条件组合")
  void testCombined() {
    String searchName = "张三";
    Integer minAge = 18;
    Integer maxAge = null;
    String email = "";

    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .whereIfNotNull("name = ?", searchName)
        .andIfNotNull("age", Oper.GTE, minAge)
        .andIfNotNull("age", Oper.LTE, maxAge)
        .andIf(!email.isEmpty(), "email = ?", email);

    assertSqlWithParams(builder, "combined",
        "SELECT * FROM users WHERE name = ? AND (age >= ?)",
        new Object[] { "张三", 18 });
  }
}
