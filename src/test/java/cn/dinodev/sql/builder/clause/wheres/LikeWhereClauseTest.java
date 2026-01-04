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
 * 模糊匹配 WHERE 子句测试类。
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("LikeWhereClause 测试")
public class LikeWhereClauseTest {

  private MysqlDialect mysql;

  @BeforeEach
  public void setUp() {
    mysql = new MysqlDialect(null, new CamelNamingConversition());
  }

  @Test
  @DisplayName("like - 基本用法")
  void testLike() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .like("name", "张");

    assertSqlWithParams(builder, "like",
        "SELECT * FROM users WHERE name LIKE ?",
        new Object[] { "%张%" });
  }

  @Test
  @DisplayName("like - 带逻辑符")
  void testLikeWithLogic() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eq("status", 1)
        .like("name", "张", Logic.OR);

    assertSqlWithParams(builder, "like with logic",
        "SELECT * FROM users WHERE status = ? OR (name LIKE ?)",
        new Object[] { 1, "%张%" });
  }

  @Test
  @DisplayName("likeIf - 条件判断")
  void testLikeIf() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .likeIf(true, "name", "张")
        .likeIf(false, "email", "test");

    assertSqlWithParams(builder, "likeIf",
        "SELECT * FROM users WHERE name LIKE ?",
        new Object[] { "%张%" });
  }

  @Test
  @DisplayName("likeIfNotNull - 非空判断")
  void testLikeIfNotNull() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .likeIfNotNull("name", "张")
        .likeIfNotNull("email", null);

    assertSqlWithParams(builder, "likeIfNotNull",
        "SELECT * FROM users WHERE name LIKE ?",
        new Object[] { "%张%" });
  }

  @Test
  @DisplayName("likeIfNotBlank - 非空字符串判断")
  void testLikeIfNotBlank() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .likeIfNotBlank("name", "张")
        .likeIfNotBlank("email", "")
        .likeIfNotBlank("phone", null);

    assertSqlWithParams(builder, "likeIfNotBlank",
        "SELECT * FROM users WHERE name LIKE ?",
        new Object[] { "%张%" });
  }

  @Test
  @DisplayName("notLike - 基本用法")
  void testNotLike() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .notLike("status", "deleted");

    assertSqlWithParams(builder, "notLike",
        "SELECT * FROM users WHERE status NOT LIKE ?",
        new Object[] { "%deleted%" });
  }

  @Test
  @DisplayName("startWith - 前缀匹配")
  void testStartWith() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .startWith("email", "admin");

    assertSqlWithParams(builder, "startWith",
        "SELECT * FROM users WHERE email LIKE ?",
        new Object[] { "admin%" });
  }

  @Test
  @DisplayName("startWithIf - 条件前缀匹配")
  void testStartWithIf() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .startWithIf(true, "email", "admin")
        .startWithIf(false, "phone", "138");

    assertSqlWithParams(builder, "startWithIf",
        "SELECT * FROM users WHERE email LIKE ?",
        new Object[] { "admin%" });
  }

  @Test
  @DisplayName("endWith - 后缀匹配")
  void testEndWith() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .endWith("phone", "8888");

    assertSqlWithParams(builder, "endWith",
        "SELECT * FROM users WHERE phone LIKE ?",
        new Object[] { "%8888" });
  }

  @Test
  @DisplayName("endWithIf - 条件后缀匹配")
  void testEndWithIf() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .endWithIf(true, "phone", "8888")
        .endWithIf(false, "email", "@example.com");

    assertSqlWithParams(builder, "endWithIf",
        "SELECT * FROM users WHERE phone LIKE ?",
        new Object[] { "%8888" });
  }

  @Test
  @DisplayName("likePattern - 自定义模式")
  void testLikePattern() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .likePattern("code", "A_B%");

    assertSqlWithParams(builder, "likePattern",
        "SELECT * FROM users WHERE code LIKE ?",
        new Object[] { "A_B%" });
  }

  @Test
  @DisplayName("组合测试 - 多种模糊匹配")
  void testCombined() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .like("name", "张")
        .startWith("email", "admin")
        .endWith("phone", "8888")
        .notLike("status", "deleted");

    assertSqlWithParams(builder, "combined",
        "SELECT * FROM users WHERE name LIKE ? AND (email LIKE ?) AND (phone LIKE ?) AND (status NOT LIKE ?)",
        new Object[] { "%张%", "admin%", "%8888", "%deleted%" });
  }

  @Test
  @DisplayName("实际场景 - 搜索用户")
  void testSearchUsers() {
    String keyword = "test";
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("id", "name", "email", "phone")
        .eq("status", 1)
        .some(new String[] { "name", "email", "phone" }, cn.dinodev.sql.Oper.LIKE, "%" + keyword + "%");

    assertSqlWithParams(builder, "search users",
        "SELECT id, name, email, phone FROM users WHERE status = ? AND ((name LIKE ? OR email LIKE ? OR phone LIKE ?))",
        new Object[] { 1, "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%" });
  }
}
