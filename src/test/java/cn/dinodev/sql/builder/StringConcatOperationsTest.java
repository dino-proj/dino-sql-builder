// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSqlWithParams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.builder.clause.UpdateSetClause;
import cn.dinodev.sql.dialect.Dialect;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.dialect.PostgreSQLDialect;

/**
 * 字符串拼接操作测试类。
 * 
 * <p>测试 {@link UpdateSetClause} 中的字符串拼接方法，包括：
 * <ul>
 *   <li>stringConcat() - 字符串追加</li>
 *   <li>stringPrepend() - 字符串前置</li>
 *   <li>MySQL 方言适配</li>
 *   <li>PostgreSQL 方言适配</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("字符串拼接操作测试")
public class StringConcatOperationsTest {

  private Dialect mysqlDialect;
  private Dialect postgresDialect;

  @BeforeEach
  public void setUp() throws Exception {
    mysqlDialect = new MysqlDialect(null, null);
    postgresDialect = new PostgreSQLDialect(null, null);
  }

  // ==================== MySQL 方言测试 ====================

  @Test
  @DisplayName("MySQL - 字符串追加拼接")
  void testMysqlConcatString() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "users")
        .stringConcat("name", " Jr.")
        .where("id = ?", 1);

    assertSqlWithParams(builder, "MySQL字符串追加",
        "UPDATE users SET name = CONCAT(name, ?) WHERE id = ?",
        new Object[] { " Jr.", 1 });
  }

  @Test
  @DisplayName("MySQL - 字符串前置拼接")
  void testMysqlPrependString() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "users")
        .stringPrepend("title", "Mr. ")
        .where("id = ?", 1);

    assertSqlWithParams(builder, "MySQL字符串前置",
        "UPDATE users SET title = CONCAT(?, title) WHERE id = ?",
        new Object[] { "Mr. ", 1 });
  }

  @Test
  @DisplayName("MySQL - 多个字符串操作")
  void testMysqlMultipleStringOperations() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "users")
        .stringPrepend("first_name", "Dr. ")
        .stringConcat("last_name", " PhD")
        .set("updated_at = NOW()")
        .where("status = ?", 1);

    assertSqlWithParams(builder, "MySQL多个字符串操作",
        "UPDATE users SET first_name = CONCAT(?, first_name), last_name = CONCAT(last_name, ?), updated_at = NOW() WHERE status = ?",
        new Object[] { "Dr. ", " PhD", 1 });
  }

  @Test
  @DisplayName("MySQL - 字符串拼接与普通SET混合")
  void testMysqlMixedStringAndSetOperations() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "products")
        .set("price = ?", 99.99)
        .stringConcat("description", " (Limited Edition)")
        .set("stock = stock - 1")
        .where("id = ?", 100);

    assertSqlWithParams(builder, "MySQL混合操作",
        "UPDATE products SET price = ?, description = CONCAT(description, ?), stock = stock - 1 WHERE id = ?",
        new Object[] { 99.99, " (Limited Edition)", 100 });
  }

  @Test
  @DisplayName("MySQL - StringBuilder类型参数")
  void testMysqlStringBuilderParameter() {
    StringBuilder suffix = new StringBuilder(" (Updated)");
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "users")
        .stringConcat("note", suffix)
        .where("id = ?", 1);

    assertSqlWithParams(builder, "MySQL StringBuilder参数",
        "UPDATE users SET note = CONCAT(note, ?) WHERE id = ?",
        new Object[] { suffix, 1 });
  }

  // ==================== PostgreSQL 方言测试 ====================

  @Test
  @DisplayName("PostgreSQL - 字符串追加拼接")
  void testPostgresConcatString() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "users")
        .stringConcat("name", " Jr.")
        .where("id = ?", 1);

    assertSqlWithParams(builder, "PostgreSQL字符串追加",
        "UPDATE users SET name = name || ? WHERE id = ?",
        new Object[] { " Jr.", 1 });
  }

  @Test
  @DisplayName("PostgreSQL - 字符串前置拼接")
  void testPostgresPrependString() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "users")
        .stringPrepend("title", "Mr. ")
        .where("id = ?", 1);

    assertSqlWithParams(builder, "PostgreSQL字符串前置",
        "UPDATE users SET title = ? || title WHERE id = ?",
        new Object[] { "Mr. ", 1 });
  }

  @Test
  @DisplayName("PostgreSQL - 多个字符串操作")
  void testPostgresMultipleStringOperations() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "users")
        .stringPrepend("first_name", "Dr. ")
        .stringConcat("last_name", " PhD")
        .set("updated_at = NOW()")
        .where("status = ?", 1);

    assertSqlWithParams(builder, "PostgreSQL多个字符串操作",
        "UPDATE users SET first_name = ? || first_name, last_name = last_name || ?, updated_at = NOW() WHERE status = ?",
        new Object[] { "Dr. ", " PhD", 1 });
  }

  @Test
  @DisplayName("PostgreSQL - 字符串拼接与普通SET混合")
  void testPostgresMixedStringAndSetOperations() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "products")
        .set("price = ?", 99.99)
        .stringConcat("description", " (Limited Edition)")
        .set("stock = stock - 1")
        .where("id = ?", 100);

    assertSqlWithParams(builder, "PostgreSQL混合操作",
        "UPDATE products SET price = ?, description = description || ?, stock = stock - 1 WHERE id = ?",
        new Object[] { 99.99, " (Limited Edition)", 100 });
  }

  @Test
  @DisplayName("PostgreSQL - 复杂字符串拼接场景")
  void testPostgresComplexStringConcatScenario() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "messages")
        .stringPrepend("content", "[URGENT] ")
        .stringConcat("content", " [END]")
        .set("priority = ?", 1)
        .where("id = ?", 5);

    assertSqlWithParams(builder, "PostgreSQL复杂拼接",
        "UPDATE messages SET content = ? || content, content = content || ?, priority = ? WHERE id = ?",
        new Object[] { "[URGENT] ", " [END]", 1, 5 });
  }

  // ==================== 边界条件测试 ====================

  @Test
  @DisplayName("空字符串拼接")
  void testEmptyStringConcat() {
    UpdateSqlBuilder mysqlBuilder = UpdateSqlBuilder.create(mysqlDialect, "users")
        .stringConcat("name", "")
        .where("id = ?", 1);

    assertSqlWithParams(mysqlBuilder, "MySQL空字符串拼接",
        "UPDATE users SET name = CONCAT(name, ?) WHERE id = ?",
        new Object[] { "", 1 });

    UpdateSqlBuilder postgresBuilder = UpdateSqlBuilder.create(postgresDialect, "users")
        .stringConcat("name", "")
        .where("id = ?", 1);

    assertSqlWithParams(postgresBuilder, "PostgreSQL空字符串拼接",
        "UPDATE users SET name = name || ? WHERE id = ?",
        new Object[] { "", 1 });
  }

  @Test
  @DisplayName("特殊字符拼接")
  void testSpecialCharactersConcat() {
    String specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?";

    UpdateSqlBuilder mysqlBuilder = UpdateSqlBuilder.create(mysqlDialect, "users")
        .stringConcat("note", specialChars)
        .where("id = ?", 1);

    assertSqlWithParams(mysqlBuilder, "MySQL特殊字符",
        "UPDATE users SET note = CONCAT(note, ?) WHERE id = ?",
        new Object[] { specialChars, 1 });

    UpdateSqlBuilder postgresBuilder = UpdateSqlBuilder.create(postgresDialect, "users")
        .stringConcat("note", specialChars)
        .where("id = ?", 1);

    assertSqlWithParams(postgresBuilder, "PostgreSQL特殊字符",
        "UPDATE users SET note = note || ? WHERE id = ?",
        new Object[] { specialChars, 1 });
  }

  @Test
  @DisplayName("Unicode字符拼接")
  void testUnicodeCharactersConcat() {
    String unicode = "你好世界🌍🚀";

    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "users")
        .stringPrepend("message", unicode)
        .where("id = ?", 1);

    assertSqlWithParams(builder, "Unicode字符",
        "UPDATE users SET message = CONCAT(?, message) WHERE id = ?",
        new Object[] { unicode, 1 });
  }

  // ==================== 实际业务场景测试 ====================

  @Test
  @DisplayName("业务场景 - 添加日志前缀")
  void testBusinessScenarioAddLogPrefix() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "audit_logs")
        .stringPrepend("message", "[ADMIN] ")
        .set("updated_by = ?", "system")
        .where("severity = ?", "high")
        .and("created_at > NOW() - INTERVAL '1 day'");

    assertSqlWithParams(builder, "添加日志前缀",
        "UPDATE audit_logs SET message = ? || message, updated_by = ? WHERE severity = ? AND (created_at > NOW() - INTERVAL '1 day')",
        new Object[] { "[ADMIN] ", "system", "high" });
  }

  @Test
  @DisplayName("业务场景 - 添加备注后缀")
  void testBusinessScenarioAddNoteSuffix() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "orders")
        .stringConcat("notes", " | Processed by automated system")
        .set("status = ?", "completed")
        .where("id IN (?, ?, ?)", 1, 2, 3);

    assertSqlWithParams(builder, "添加备注后缀",
        "UPDATE orders SET notes = CONCAT(notes, ?), status = ? WHERE id IN (?, ?, ?)",
        new Object[] { " | Processed by automated system", "completed", 1, 2, 3 });
  }

  @Test
  @DisplayName("业务场景 - 格式化用户名")
  void testBusinessScenarioFormatUserName() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "users")
        .stringPrepend("display_name", "👤 ")
        .stringConcat("display_name", " ⭐")
        .set("vip_status = ?", true)
        .where("points > ?", 1000);

    assertSqlWithParams(builder, "格式化VIP用户名",
        "UPDATE users SET display_name = ? || display_name, display_name = display_name || ?, vip_status = ? WHERE points > ?",
        new Object[] { "👤 ", " ⭐", true, 1000 });
  }
}
