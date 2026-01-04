// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.operations;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.JsonPath;
import cn.dinodev.sql.builder.UpdateSqlBuilder;
import cn.dinodev.sql.dialect.Dialect;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.dialect.PostgreSQLDialect;
import cn.dinodev.sql.naming.SnakeNamingConversition;
import cn.dinodev.sql.testutil.DatabaseMetaDataMocks;

/**
 * JSON/JSONB 操作测试类。
 * <p>
 * 测试跨数据库的 JSON 链式操作，包括：
 * - PostgreSQL JSONB 操作
 * - MySQL JSON 操作
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("JSON/JSONB 操作测试")
class JsonOperationsTest {

  private Dialect postgresDialect;
  private Dialect mysqlDialect;

  @BeforeEach
  void setUp() throws Exception {
    postgresDialect = new PostgreSQLDialect(DatabaseMetaDataMocks.postgresV15, new SnakeNamingConversition());
    mysqlDialect = new MysqlDialect(DatabaseMetaDataMocks.mysqlV8, new SnakeNamingConversition());
  }

  // ==================== PostgreSQL JSONB 测试 ====================

  @Test
  @DisplayName("PostgreSQL: 使用 jsonb 方法 - 合并操作")
  void testPostgresJsonMerge() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "users");

    builder.jsonb("settings", ops -> ops
        .merge("{\"theme\":\"dark\"}")
        .setPath(JsonPath.of("notifications", "email"), true)
        .removeKey("deprecated"))
        .where("id", 1);

    System.out.println("PostgreSQL SQL: " + builder.getSql());
    System.out.println("Params: " + java.util.Arrays.toString(builder.getParams()));
  }

  @Test
  @DisplayName("PostgreSQL: jsonb 方法别名")
  void testPostgresJsonbAlias() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "users");

    builder.jsonb("settings", ops -> ops
        .merge("{\"theme\":\"dark\"}")
        .removeKey("deprecated"))
        .where("id", 1);

    System.out.println("PostgreSQL SQL: " + builder.getSql());
    System.out.println("Params: " + java.util.Arrays.toString(builder.getParams()));
  }

  @Test
  @DisplayName("PostgreSQL: 简单设置")
  void testPostgresSimpleSet() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "users");

    builder.json("settings", ops -> ops
        .set("{\"theme\":\"dark\",\"language\":\"en\"}"))
        .where("id", 1);

    System.out.println("PostgreSQL SQL: " + builder.getSql());
  }

  @Test
  @DisplayName("PostgreSQL: 路径设置")
  void testPostgresSetPath() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "users");

    builder.jsonb("settings", ops -> ops
        .setPath(JsonPath.of("notifications", "email"), true))
        .where("id", 1);

    System.out.println("PostgreSQL SQL: " + builder.getSql());
  }

  // ==================== PostgreSQL JSON 类型异常测试 ====================

  @Test
  @DisplayName("PostgreSQL: JSON 类型不支持 merge 操作")
  void testPostgresJsonMergeNotSupported() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "users");

    UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
      builder.json("settings", ops -> ops
          .merge("{\"theme\":\"dark\"}"))
          .where("id", 1);
      builder.getSql(); // 触发操作执行
    });

    System.out.println("预期异常: " + exception.getMessage());
  }

  @Test
  @DisplayName("PostgreSQL: JSON 类型不支持 setPath 操作")
  void testPostgresJsonSetPathNotSupported() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "users");

    UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
      builder.json("settings", ops -> ops
          .setPath(JsonPath.of("notifications", "email"), true))
          .where("id", 1);
      builder.getSql(); // 触发操作执行
    });

    System.out.println("预期异常: " + exception.getMessage());
  }

  @Test
  @DisplayName("PostgreSQL: JSON 类型不支持数组操作")
  void testPostgresJsonArrayOperationNotSupported() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(postgresDialect, "users");

    UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
      builder.json("tags", ops -> ops
          .appendArray("'new_tag'"))
          .where("id", 1);
      builder.getSql(); // 触发操作执行
    });

    System.out.println("预期异常: " + exception.getMessage());
  }

  // ==================== MySQL JSON 测试 ====================

  @Test
  @DisplayName("MySQL: 合并操作")
  void testMySQLJsonMerge() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "users");

    builder.json("settings", ops -> ops
        .merge("{\"theme\":\"dark\"}"))
        .where("id", 1);

    System.out.println("MySQL SQL: " + builder.getSql());
  }

  @Test
  @DisplayName("MySQL: 路径设置（自动转换路径格式）")
  void testMySQLSetPath() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "users");

    builder.json("settings", ops -> ops
        .setPath(JsonPath.of("notifications", "email"), true))
        .where("id", 1);

    System.out.println("MySQL SQL: " + builder.getSql());
  }

  @Test
  @DisplayName("MySQL: 删除键")
  void testMySQLRemoveKey() {
    UpdateSqlBuilder builder = UpdateSqlBuilder.create(mysqlDialect, "users");

    builder.json("settings", ops -> ops
        .removeKey("deprecated"))
        .where("id", 1);

    System.out.println("MySQL SQL: " + builder.getSql());
  }
}
