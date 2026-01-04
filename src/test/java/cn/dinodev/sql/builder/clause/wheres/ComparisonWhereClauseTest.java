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
 * 比较操作 WHERE 子句测试类。
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("ComparisonWhereClause 测试")
public class ComparisonWhereClauseTest {

  private MysqlDialect mysql;

  @BeforeEach
  public void setUp() {
    mysql = new MysqlDialect(null, new CamelNamingConversition());
  }

  @Test
  @DisplayName("eq - 等值条件")
  void testEq() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eq("name", "张三");

    assertSqlWithParams(builder, "eq",
        "SELECT * FROM users WHERE name = ?",
        new Object[] { "张三" });
  }

  @Test
  @DisplayName("eq - 等值条件带逻辑符（OR）")
  void testEqWithLogic() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eq("name", "张三")
        .eq("email", "test@example.com", Logic.OR);

    assertSqlWithParams(builder, "eq with logic",
        "SELECT * FROM users WHERE name = ? OR (email = ?)",
        new Object[] { "张三", "test@example.com" });
  }

  @Test
  @DisplayName("eqIf - 条件等值（true）")
  void testEqIfTrue() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eqIf(true, "status", 1);

    assertSqlWithParams(builder, "eqIf(true)",
        "SELECT * FROM users WHERE status = ?",
        new Object[] { 1 });
  }

  @Test
  @DisplayName("eqIf - 条件等值（false）")
  void testEqIfFalse() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eqIf(false, "status", 1);

    assertSqlWithParams(builder, "eqIf(false)",
        "SELECT * FROM users",
        new Object[] {});
  }

  @Test
  @DisplayName("eqIfNotNull - 非空等值")
  void testEqIfNotNull() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eqIfNotNull("name", "张三")
        .eqIfNotNull("email", null);

    assertSqlWithParams(builder, "eqIfNotNull",
        "SELECT * FROM users WHERE name = ?",
        new Object[] { "张三" });
  }

  @Test
  @DisplayName("eqIfNotBlank - 非空字符串等值")
  void testEqIfNotBlank() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eqIfNotBlank("name", "张三")
        .eqIfNotBlank("email", "")
        .eqIfNotBlank("phone", null);

    assertSqlWithParams(builder, "eqIfNotBlank",
        "SELECT * FROM users WHERE name = ?",
        new Object[] { "张三" });
  }

  @Test
  @DisplayName("ne - 不等值条件")
  void testNe() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .ne("status", 0);

    assertSqlWithParams(builder, "ne",
        "SELECT * FROM users WHERE status <> ?",
        new Object[] { 0 });
  }

  @Test
  @DisplayName("ne - 不等值条件带逻辑符（OR）")
  void testNeWithLogic() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .ne("status", 0)
        .ne("deleted", 1, Logic.OR);

    assertSqlWithParams(builder, "ne with logic",
        "SELECT * FROM users WHERE status <> ? OR (deleted <> ?)",
        new Object[] { 0, 1 });
  }

  @Test
  @DisplayName("neIf - 条件不等值")
  void testNeIf() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .neIf(true, "status", 0)
        .neIf(false, "deleted", 1);

    assertSqlWithParams(builder, "neIf",
        "SELECT * FROM users WHERE status <> ?",
        new Object[] { 0 });
  }

  @Test
  @DisplayName("neIfNotNull - 非空不等值")
  void testNeIfNotNull() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .neIfNotNull("status", 0)
        .neIfNotNull("deleted", null);

    assertSqlWithParams(builder, "neIfNotNull",
        "SELECT * FROM users WHERE status <> ?",
        new Object[] { 0 });
  }

  @Test
  @DisplayName("neIfNotBlank - 非空字符串不等值")
  void testNeIfNotBlank() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .neIfNotBlank("name", "admin")
        .neIfNotBlank("email", "");

    assertSqlWithParams(builder, "neIfNotBlank",
        "SELECT * FROM users WHERE name <> ?",
        new Object[] { "admin" });
  }

  @Test
  @DisplayName("gt - 大于条件")
  void testGt() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .gt("age", 18);

    assertSqlWithParams(builder, "gt",
        "SELECT * FROM users WHERE age > ?",
        new Object[] { 18 });
  }

  @Test
  @DisplayName("gt - 大于条件带逻辑符（OR）")
  void testGtWithLogic() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .gt("age", 18)
        .gt("score", 60, Logic.OR);

    assertSqlWithParams(builder, "gt with logic",
        "SELECT * FROM users WHERE age > ? OR (score > ?)",
        new Object[] { 18, 60 });
  }

  @Test
  @DisplayName("gtIf - 条件大于")
  void testGtIf() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .gtIf(true, "age", 18)
        .gtIf(false, "score", 60);

    assertSqlWithParams(builder, "gtIf",
        "SELECT * FROM users WHERE age > ?",
        new Object[] { 18 });
  }

  @Test
  @DisplayName("gtIfNotNull - 非空大于")
  void testGtIfNotNull() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .gtIfNotNull("age", 18)
        .gtIfNotNull("score", null);

    assertSqlWithParams(builder, "gtIfNotNull",
        "SELECT * FROM users WHERE age > ?",
        new Object[] { 18 });
  }

  @Test
  @DisplayName("lt - 小于条件")
  void testLt() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .lt("age", 60);

    assertSqlWithParams(builder, "lt",
        "SELECT * FROM users WHERE age < ?",
        new Object[] { 60 });
  }

  @Test
  @DisplayName("lt - 小于条件带逻辑符（OR）")
  void testLtWithLogic() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .lt("age", 60)
        .lt("score", 40, Logic.OR);

    assertSqlWithParams(builder, "lt with logic",
        "SELECT * FROM users WHERE age < ? OR (score < ?)",
        new Object[] { 60, 40 });
  }

  @Test
  @DisplayName("ltIf - 条件小于")
  void testLtIf() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .ltIf(true, "age", 60)
        .ltIf(false, "score", 40);

    assertSqlWithParams(builder, "ltIf",
        "SELECT * FROM users WHERE age < ?",
        new Object[] { 60 });
  }

  @Test
  @DisplayName("ltIfNotNull - 非空小于")
  void testLtIfNotNull() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .ltIfNotNull("age", 60)
        .ltIfNotNull("score", null);

    assertSqlWithParams(builder, "ltIfNotNull",
        "SELECT * FROM users WHERE age < ?",
        new Object[] { 60 });
  }

  @Test
  @DisplayName("gte - 大于等于条件")
  void testGte() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .gte("age", 18);

    assertSqlWithParams(builder, "gte",
        "SELECT * FROM users WHERE age >= ?",
        new Object[] { 18 });
  }

  @Test
  @DisplayName("gte - 大于等于条件带逻辑符（OR）")
  void testGteWithLogic() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .gte("age", 18)
        .gte("score", 60, Logic.OR);

    assertSqlWithParams(builder, "gte with logic",
        "SELECT * FROM users WHERE age >= ? OR (score >= ?)",
        new Object[] { 18, 60 });
  }

  @Test
  @DisplayName("gteIf - 条件大于等于")
  void testGteIf() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .gteIf(true, "age", 18)
        .gteIf(false, "score", 60);

    assertSqlWithParams(builder, "gteIf",
        "SELECT * FROM users WHERE age >= ?",
        new Object[] { 18 });
  }

  @Test
  @DisplayName("gteIfNotNull - 非空大于等于")
  void testGteIfNotNull() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .gteIfNotNull("age", 18)
        .gteIfNotNull("score", null);

    assertSqlWithParams(builder, "gteIfNotNull",
        "SELECT * FROM users WHERE age >= ?",
        new Object[] { 18 });
  }

  @Test
  @DisplayName("lte - 小于等于条件")
  void testLte() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .lte("age", 60);

    assertSqlWithParams(builder, "lte",
        "SELECT * FROM users WHERE age <= ?",
        new Object[] { 60 });
  }

  @Test
  @DisplayName("lte - 小于等于条件带逻辑符（OR）")
  void testLteWithLogic() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .lte("age", 60)
        .lte("score", 40, Logic.OR);

    assertSqlWithParams(builder, "lte with logic",
        "SELECT * FROM users WHERE age <= ? OR (score <= ?)",
        new Object[] { 60, 40 });
  }

  @Test
  @DisplayName("lteIf - 条件小于等于")
  void testLteIf() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .lteIf(true, "age", 60)
        .lteIf(false, "score", 40);

    assertSqlWithParams(builder, "lteIf",
        "SELECT * FROM users WHERE age <= ?",
        new Object[] { 60 });
  }

  @Test
  @DisplayName("lteIfNotNull - 非空小于等于")
  void testLteIfNotNull() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .lteIfNotNull("age", 60)
        .lteIfNotNull("score", null);

    assertSqlWithParams(builder, "lteIfNotNull",
        "SELECT * FROM users WHERE age <= ?",
        new Object[] { 60 });
  }

  @Test
  @DisplayName("组合测试 - 多个比较条件")
  void testCombinedComparisons() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "products")
        .column("id", "name", "price", "stock")
        .eq("category", "electronics")
        .gte("price", 100)
        .lte("price", 500)
        .gt("stock", 0)
        .ne("status", "discontinued");

    assertSqlWithParams(builder, "combined comparisons",
        "SELECT id, name, price, stock FROM products WHERE category = ? AND (price >= ?) AND (price <= ?) AND (stock > ?) AND (status <> ?)",
        new Object[] { "electronics", 100, 500, 0, "discontinued" });
  }
}
