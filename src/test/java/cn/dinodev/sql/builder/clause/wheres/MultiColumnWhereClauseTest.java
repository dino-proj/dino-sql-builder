// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.wheres;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSqlWithParams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.Logic;
import cn.dinodev.sql.Oper;
import cn.dinodev.sql.builder.SelectSqlBuilder;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

/**
 * 多列操作 WHERE 子句测试类。
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("MultiColumnWhereClause 测试")
public class MultiColumnWhereClauseTest {

  private MysqlDialect mysql;

  @BeforeEach
  public void setUp() {
    mysql = new MysqlDialect(null, new CamelNamingConversition());
  }

  @Test
  @DisplayName("some - 任意列满足条件")
  void testSome() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .some(new String[] { "name", "email", "phone" }, Oper.LIKE, "%test%");

    assertSqlWithParams(builder, "some",
        "SELECT * FROM users WHERE (name LIKE ? OR email LIKE ? OR phone LIKE ?)",
        new Object[] { "%test%", "%test%", "%test%" });
  }

  @Test
  @DisplayName("some - 带外围逻辑符")
  void testSomeWithLogic() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eq("status", 1)
        .some(new String[] { "name", "email" }, Oper.LIKE, "%test%", Logic.AND);

    assertSqlWithParams(builder, "some with logic",
        "SELECT * FROM users WHERE status = ? AND ((name LIKE ? OR email LIKE ?))",
        new Object[] { 1, "%test%", "%test%" });
  }

  @Test
  @DisplayName("some - null值处理（EQ）")
  void testSomeWithNullEq() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .some(new String[] { "deleted_at", "archived_at" }, Oper.EQ, null);

    assertSqlWithParams(builder, "some with null eq",
        "SELECT * FROM users WHERE (deleted_at IS NULL OR archived_at IS NULL)",
        new Object[] {});
  }

  @Test
  @DisplayName("some - null值处理（NE）")
  void testSomeWithNullNe() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .some(new String[] { "email", "phone" }, Oper.NE, null);

    assertSqlWithParams(builder, "some with null ne",
        "SELECT * FROM users WHERE (email IS NOT NULL OR phone IS NOT NULL)",
        new Object[] {});
  }

  @Test
  @DisplayName("someIf - 条件任意列")
  void testSomeIf() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .someIf(true, new String[] { "name", "email" }, Oper.LIKE, "%test%")
        .someIf(false, new String[] { "phone", "mobile" }, Oper.LIKE, "%888%");

    assertSqlWithParams(builder, "someIf",
        "SELECT * FROM users WHERE (name LIKE ? OR email LIKE ?)",
        new Object[] { "%test%", "%test%" });
  }

  @Test
  @DisplayName("every - 所有列满足条件")
  void testEvery() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .isNotNull("email")
        .isNotNull("phone");

    assertSqlWithParams(builder, "every",
        "SELECT * FROM users WHERE email IS NOT NULL AND (phone IS NOT NULL)",
        new Object[] {});
  }

  @Test
  @DisplayName("every - 带外围逻辑符")
  void testEveryWithLogic() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eq("status", 1)
        .isNotNull("email", Logic.AND)
        .isNotNull("phone", Logic.AND);

    assertSqlWithParams(builder, "every with logic",
        "SELECT * FROM users WHERE status = ? AND (email IS NOT NULL) AND (phone IS NOT NULL)",
        new Object[] { 1 });
  }

  @Test
  @DisplayName("everyIf - 条件所有列")
  void testEveryIf() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .isNotNullIf(true, "email")
        .isNotNullIf(true, "phone")
        .isNotNullIf(false, "name");

    assertSqlWithParams(builder, "everyIf",
        "SELECT * FROM users WHERE email IS NOT NULL AND (phone IS NOT NULL)",
        new Object[] {});
  }

  @Test
  @DisplayName("组合测试 - some和every结合")
  void testCombined() {
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .column("*")
        .eq("status", 1)
        .some(new String[] { "name", "email", "phone" }, Oper.LIKE, "%test%")
        .isNotNull("email")
        .isNotNull("phone");

    assertSqlWithParams(builder, "combined",
        "SELECT * FROM users WHERE status = ? AND ((name LIKE ? OR email LIKE ? OR phone LIKE ?)) AND (email IS NOT NULL) AND (phone IS NOT NULL)",
        new Object[] { 1, "%test%", "%test%", "%test%" });
  }

  @Test
  @DisplayName("实际场景 - 多字段搜索")
  void testMultiFieldSearch() {
    String keyword = "张三";
    SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
        .columns("id", "name", "email", "phone")
        .eq("status", 1)
        .some(new String[] { "name", "email", "phone" }, Oper.LIKE, "%" + keyword + "%")
        .isNotNull("deleted_at", Logic.AND);

    assertSqlWithParams(builder, "multi-field search",
        "SELECT id, name, email, phone FROM users WHERE status = ? AND ((name LIKE ? OR email LIKE ? OR phone LIKE ?)) AND (deleted_at IS NOT NULL)",
        new Object[] { 1, "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%" });
  }
}
