// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.wheres;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSqlWithParams;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.Range;
import cn.dinodev.sql.builder.SelectSqlBuilder;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

/**
 * 范围查询 WHERE 子句测试类。
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("RangeWhereClause 测试")
public class RangeWhereClauseTest {

  private MysqlDialect mysql;

  @BeforeEach
  public void setUp() {
    mysql = new MysqlDialect(null, new CamelNamingConversition());
  }

  @Test
  @DisplayName("between - 区间查询")
  void testBetween() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "products")
        .column("*")
        .between("price", 100, 500);

    assertSqlWithParams(builder, "between",
        "SELECT * FROM products WHERE price >= ? AND (price <= ?)",
        new Object[] { 100, 500 });
  }

  @Test
  @DisplayName("between - 只有起始值")
  void testBetweenStartOnly() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "products")
        .column("*")
        .between("price", 100, null);

    assertSqlWithParams(builder, "between start only",
        "SELECT * FROM products WHERE price >= ?",
        new Object[] { 100 });
  }

  @Test
  @DisplayName("between - 只有结束值")
  void testBetweenEndOnly() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "products")
        .column("*")
        .between("price", null, 500);

    assertSqlWithParams(builder, "between end only",
        "SELECT * FROM products WHERE price <= ?",
        new Object[] { 500 });
  }

  @Test
  @DisplayName("between - 使用Range对象")
  void testBetweenWithRange() {
    Range<Integer> priceRange = Range.of(100, 500);
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "products")
        .column("*")
        .between("price", priceRange);

    assertSqlWithParams(builder, "between with range",
        "SELECT * FROM products WHERE price >= ? AND (price <= ?)",
        new Object[] { 100, 500 });
  }

  @Test
  @DisplayName("in - 集合查询")
  void testIn() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .in("status", Arrays.asList(1, 2, 3));

    assertSqlWithParams(builder, "in",
        "SELECT * FROM users WHERE status IN (?, ?, ?)",
        new Object[] { 1, 2, 3 });
  }

  @Test
  @DisplayName("in - 可变参数")
  void testInVarargs() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .in("id", new Object[] { 1, 2, 3, 4, 5 });

    assertSqlWithParams(builder, "in varargs",
        "SELECT * FROM users WHERE id IN (?, ?, ?, ?, ?)",
        new Object[] { 1, 2, 3, 4, 5 });
  }

  @Test
  @DisplayName("inIf - 条件IN查询")
  void testInIf() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .inIf(true, "status", Arrays.asList(1, 2))
        .inIf(false, "role", Arrays.asList("admin", "user"));

    assertSqlWithParams(builder, "inIf",
        "SELECT * FROM users WHERE status IN (?, ?)",
        new Object[] { 1, 2 });
  }

  @Test
  @DisplayName("inIfNotEmpty - 非空集合IN查询")
  void testInIfNotEmpty() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .inIf(!Arrays.asList(1, 2).isEmpty(), "status", Arrays.asList(1, 2))
        .inIf(!Arrays.asList().isEmpty(), "role", Arrays.asList());

    assertSqlWithParams(builder, "inIfNotEmpty",
        "SELECT * FROM users WHERE status IN (?, ?)",
        new Object[] { 1, 2 });
  }

  @Test
  @DisplayName("notIn - NOT IN查询")
  void testNotIn() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .notIn("status", Arrays.asList(0, -1));

    assertSqlWithParams(builder, "notIn",
        "SELECT * FROM users WHERE status NOT IN (?, ?)",
        new Object[] { 0, -1 });
  }

  @Test
  @DisplayName("notInIf - 条件NOT IN查询")
  void testNotInIf() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*");

    if (true) {
      builder.notIn("status", new Object[] { 0, -1 });
    }

    assertSqlWithParams(builder, "notInIf",
        "SELECT * FROM users WHERE status NOT IN (?, ?)",
        new Object[] { 0, -1 });
  }

  @Test
  @DisplayName("组合测试 - 范围和集合查询")
  void testCombined() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "products")
        .column("id", "name", "price", "category_id")
        .between("price", 100, 500)
        .in("category_id", Arrays.asList(1, 2, 3))
        .notIn("status", Arrays.asList(0, -1));

    assertSqlWithParams(builder, "combined",
        "SELECT id, name, price, category_id FROM products WHERE price >= ? AND (price <= ?) AND (category_id IN (?, ?, ?)) AND (status NOT IN (?, ?))",
        new Object[] { 100, 500, 1, 2, 3, 0, -1 });
  }

  @Test
  @DisplayName("实际场景 - 价格筛选")
  void testPriceFilter() {
    Integer minPrice = 100;
    Integer maxPrice = 1000;
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "products")
        .column("id", "name", "price")
        .eq("status", 1)
        .between("price", minPrice, maxPrice)
        .in("category_id", Arrays.asList(1, 2, 3));

    assertSqlWithParams(builder, "price filter",
        "SELECT id, name, price FROM products WHERE status = ? AND (price >= ?) AND (price <= ?) AND (category_id IN (?, ?, ?))",
        new Object[] { 1, 100, 1000, 1, 2, 3 });
  }
}
