// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.dialect.Dialect;
import cn.dinodev.sql.dialect.PostgreSQLDialect;
import cn.dinodev.sql.naming.SnakeNamingConversition;
import cn.dinodev.sql.testutil.DatabaseMetaDataMocks;

/**
 * JSONB 操作测试类。
 * <p>
 * 测试 JSONB 链式操作的三种使用方式：
 * 1. 显式调用 apply()
 * 2. 使用 try-with-resources 自动应用
 * 3. 使用 Consumer 回调自动应用（推荐）
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("JSONB 操作测试")
class JsonbOperationsTest {

  private Dialect dialect;

  @BeforeEach
  void setUp() throws Exception {
    dialect = new PostgreSQLDialect(DatabaseMetaDataMocks.postgresV15, new SnakeNamingConversition());
  }

  @Test
  @DisplayName("使用 Consumer 回调自动应用（推荐方式）")
  void testConsumerAutoApply() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(dialect, "users");

    builder.jsonb("settings", ops -> ops
        .merge("{\"theme\":\"dark\"}")
        .setPath("{notifications,email}", true)
        .removeKey("deprecated")) // 自动应用，无需调用 apply()
        .where("id", 1);

    System.out.println("SQL: " + builder.getSql());
    System.out.println("Params: " + java.util.Arrays.toString(builder.getParams()));
  }

  @Test
  @DisplayName("使用 Consumer 回调 - 简单设置")
  void testConsumerSimpleSet() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(dialect, "users");

    builder.jsonb("settings", ops -> ops
        .set("{\"theme\":\"dark\",\"language\":\"en\"}"))
        .where("id", 1);

    System.out.println("SQL: " + builder.getSql());
    System.out.println("Params: " + java.util.Arrays.toString(builder.getParams()));
  }

  @Test
  @DisplayName("使用 Consumer 回调 - 合并操作")
  void testConsumerMerge() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(dialect, "users");

    builder.jsonb("profile", ops -> ops
        .merge("{\"city\":\"Beijing\",\"country\":\"CN\"}"))
        .where("id", 1);

    System.out.println("SQL: " + builder.getSql());
    System.out.println("Params: " + java.util.Arrays.toString(builder.getParams()));
  }

  @Test
  @DisplayName("使用 Consumer 回调 - 路径设置")
  void testConsumerSetPath() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(dialect, "users");

    builder.jsonb("settings", ops -> ops
        .setPath("{notifications,email}", true)
        .setPath("{notifications,sms}", false))
        .where("id", 1);

    System.out.println("SQL: " + builder.getSql());
    System.out.println("Params: " + java.util.Arrays.toString(builder.getParams()));
  }

  @Test
  @DisplayName("使用 Consumer 回调 - 删除键")
  void testConsumerRemoveKey() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(dialect, "users");

    builder.jsonb("settings", ops -> ops
        .removeKey("deprecated")
        .removeKey("old_setting"))
        .where("id", 1);

    System.out.println("SQL: " + builder.getSql());
    System.out.println("Params: " + java.util.Arrays.toString(builder.getParams()));
  }

  @Test
  @DisplayName("使用 Consumer 回调 - 数组操作")
  void testConsumerArrayOperations() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(dialect, "users");

    builder.jsonb("tags", ops -> ops
        .appendArray("[\"new-tag\"]")
        .stripNulls())
        .where("id", 1);

    System.out.println("SQL: " + builder.getSql());
    System.out.println("Params: " + java.util.Arrays.toString(builder.getParams()));
  }

  @Test
  @DisplayName("使用 Consumer 回调 - 复杂组合操作")
  void testConsumerComplexOperations() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(dialect, "users");

    builder.jsonb("profile", ops -> ops
        .merge("{\"updated\":true}")
        .setPath("{address,city}", "Beijing")
        .setPath("{address,country}", "CN")
        .removeKey("temp")
        .stripNulls())
        .set("updated_at = NOW()")
        .where("id", 1);

    System.out.println("SQL: " + builder.getSql());
    System.out.println("Params: " + java.util.Arrays.toString(builder.getParams()));
  }

  @Test
  @DisplayName("条件 JSONB 操作 - 条件为 true")
  void testConditionalJsonbTrue() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(dialect, "users");

    boolean shouldUpdate = true;
    builder.jsonbIf(shouldUpdate, "settings", ops -> ops
        .merge("{\"enabled\":true}"))
        .where("id", 1);

    System.out.println("SQL: " + builder.getSql());
    System.out.println("Params: " + java.util.Arrays.toString(builder.getParams()));
  }

  @Test
  @DisplayName("条件 JSONB 操作 - 条件为 false")
  void testConditionalJsonbFalse() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(dialect, "users");

    boolean shouldUpdate = false;
    builder.jsonbIf(shouldUpdate, "settings", ops -> ops
        .merge("{\"enabled\":true}"))
        .set("name", "Test")
        .where("id", 1);

    // 应该只包含 name 的更新，不包含 settings
    System.out.println("SQL: " + builder.getSql());
    System.out.println("Params: " + java.util.Arrays.toString(builder.getParams()));
  }

  @Test
  @DisplayName("链式组合多个 JSONB 操作")
  void testChainedJsonbOperations() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(dialect, "users");

    builder.jsonb("settings", ops -> ops
        .merge("{\"theme\":\"dark\"}"))
        .jsonb("profile", ops -> ops
            .setPath("{city}", "Beijing"))
        .set("updated_at = NOW()")
        .where("id", 1);

    System.out.println("SQL: " + builder.getSql());
    System.out.println("Params: " + java.util.Arrays.toString(builder.getParams()));
  }
}
