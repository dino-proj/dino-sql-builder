// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.JsonType;
import cn.dinodev.sql.naming.SnakeNamingConversition;
import cn.dinodev.sql.testutil.DatabaseMetaDataMocks;

/**
 * JsonDialect API 测试类。
 * <p>
 * 测试 JsonDialect 接口，所有方法现在都需要 JsonType 参数。
 */
@DisplayName("JsonDialect API 测试")
public class JsonDialectTest {

  private JsonDialect postgresDialect;
  private JsonDialect mysqlDialect;

  @BeforeEach
  void setUp() throws SQLException {
    PostgreSQLDialect postgreSQL = new PostgreSQLDialect(
        DatabaseMetaDataMocks.postgresV15,
        new SnakeNamingConversition());
    postgresDialect = postgreSQL.jsonDialect();

    MysqlDialect mysql = new MysqlDialect(
        DatabaseMetaDataMocks.mysqlV8,
        new SnakeNamingConversition());
    mysqlDialect = mysql.jsonDialect();
  }

  // ==================== PostgreSQL 测试 ====================

  @Test
  @DisplayName("PostgreSQL - makeTypeCast(JSON) 应该返回 ?::json")
  void testPostgresJsonTypeCast() {
    String result = postgresDialect.makeTypeCast(JsonType.JSON);
    assertEquals("?::json", result);
  }

  @Test
  @DisplayName("PostgreSQL - makeTypeCast(JSONB) 应该返回 ?::jsonb")
  void testPostgresJsonbTypeCast() {
    String result = postgresDialect.makeTypeCast(JsonType.JSONB);
    assertEquals("?::jsonb", result);
  }

  @Test
  @DisplayName("PostgreSQL - makeJsonMerge 对 JSON 类型应该抛出异常")
  void testPostgresJsonMergeWithJsonType() {
    UnsupportedOperationException exception = assertThrows(
        UnsupportedOperationException.class,
        () -> postgresDialect.makeJsonMerge(JsonType.JSON, "data"));

    assertEquals("PostgreSQL JSON type does not support || operator, use JSONB instead",
        exception.getMessage());
  }

  @Test
  @DisplayName("PostgreSQL - makeJsonMerge 对 JSONB 类型应该返回正确的合并表达式")
  void testPostgresJsonMergeWithJsonbType() {
    String result = postgresDialect.makeJsonMerge(JsonType.JSONB, "data");
    assertEquals("data || ?::jsonb", result);
  }

  @Test
  @DisplayName("PostgreSQL - makeJsonSetPath 对 JSON 类型应该抛出异常")
  void testPostgresJsonSetPathWithJsonType() {
    UnsupportedOperationException exception = assertThrows(
        UnsupportedOperationException.class,
        () -> postgresDialect.makeJsonSetPath(JsonType.JSON, "data", "{address,city}", true));

    assertEquals("PostgreSQL JSON type does not support path operations, use JSONB instead",
        exception.getMessage());
  }

  @Test
  @DisplayName("PostgreSQL - makeJsonSetPath 对 JSONB 类型应该返回正确的路径设置表达式")
  void testPostgresJsonSetPathWithJsonbType() {
    String result = postgresDialect.makeJsonSetPath(JsonType.JSONB, "data", "{address,city}", true);
    assertEquals("jsonb_set(data, '{address,city}', ?::jsonb, true)", result);
  }

  @Test
  @DisplayName("PostgreSQL - makeJsonArrayAppend 对 JSON 类型应该抛出异常")
  void testPostgresJsonArrayAppendWithJsonType() {
    UnsupportedOperationException exception = assertThrows(
        UnsupportedOperationException.class,
        () -> postgresDialect.makeJsonArrayAppend(JsonType.JSON, "data"));

    assertEquals("PostgreSQL JSON type does not support || operator, use JSONB instead",
        exception.getMessage());
  }

  @Test
  @DisplayName("PostgreSQL - makeJsonArrayAppend 对 JSONB 类型应该返回正确的数组追加表达式")
  void testPostgresJsonArrayAppendWithJsonbType() {
    String result = postgresDialect.makeJsonArrayAppend(JsonType.JSONB, "data");
    assertEquals("data || ?::jsonb", result);
  }

  @Test
  @DisplayName("PostgreSQL - makeJsonArrayPrepend 对 JSON 类型应该抛出异常")
  void testPostgresJsonArrayPrependWithJsonType() {
    UnsupportedOperationException exception = assertThrows(
        UnsupportedOperationException.class,
        () -> postgresDialect.makeJsonArrayPrepend(JsonType.JSON, "data"));

    assertEquals("PostgreSQL JSON type does not support || operator, use JSONB instead",
        exception.getMessage());
  }

  @Test
  @DisplayName("PostgreSQL - makeJsonArrayPrepend 对 JSONB 类型应该返回正确的数组前置表达式")
  void testPostgresJsonArrayPrependWithJsonbType() {
    String result = postgresDialect.makeJsonArrayPrepend(JsonType.JSONB, "data");
    assertEquals("?::jsonb || data", result);
  }

  @Test
  @DisplayName("PostgreSQL - makeJsonStripNulls 对 JSON 类型应该返回 json_strip_nulls")
  void testPostgresJsonStripNullsWithJsonType() {
    String result = postgresDialect.makeJsonStripNulls(JsonType.JSON, "data");
    assertEquals("json_strip_nulls(data)", result);
  }

  @Test
  @DisplayName("PostgreSQL - makeJsonStripNulls 对 JSONB 类型应该返回 jsonb_strip_nulls")
  void testPostgresJsonStripNullsWithJsonbType() {
    String result = postgresDialect.makeJsonStripNulls(JsonType.JSONB, "data");
    assertEquals("jsonb_strip_nulls(data)", result);
  }

  // ==================== MySQL 测试 ====================

  @Test
  @DisplayName("MySQL - makeTypeCast 对任何类型都应该返回 ?")
  void testMysqlTypeCast() {
    // MySQL 不区分 JSON 和 JSONB，都返回 ?
    assertEquals("?", mysqlDialect.makeTypeCast(JsonType.JSON));
    assertEquals("?", mysqlDialect.makeTypeCast(JsonType.JSONB));
  }

  @Test
  @DisplayName("MySQL - makeJsonMerge 对任何类型都应该返回 JSON_MERGE_PATCH")
  void testMysqlJsonMerge() {
    // MySQL 忽略 JsonType 参数，统一使用 JSON_MERGE_PATCH
    assertEquals("JSON_MERGE_PATCH(data, ?)", mysqlDialect.makeJsonMerge(JsonType.JSON, "data"));
    assertEquals("JSON_MERGE_PATCH(data, ?)", mysqlDialect.makeJsonMerge(JsonType.JSONB, "data"));
  }

  @Test
  @DisplayName("MySQL - makeJsonSetPath 对任何类型都应该返回 JSON_SET")
  void testMysqlJsonSetPath() {
    // MySQL 忽略 JsonType 参数，统一使用 JSON_SET
    // MySQL 使用自己的路径格式，不转换 PostgreSQL 格式
    String resultJson = mysqlDialect.makeJsonSetPath(JsonType.JSON, "data", "{address,city}", true);
    String resultJsonb = mysqlDialect.makeJsonSetPath(JsonType.JSONB, "data", "{address,city}", true);

    assertEquals("JSON_SET(data, '{address,city}', ?)", resultJson);
    assertEquals("JSON_SET(data, '{address,city}', ?)", resultJsonb);
  }

  @Test
  @DisplayName("MySQL - makeJsonArrayAppend 对任何类型都应该返回 JSON_ARRAY_APPEND")
  void testMysqlJsonArrayAppend() {
    // MySQL 忽略 JsonType 参数，统一使用 JSON_ARRAY_APPEND
    assertEquals("JSON_ARRAY_APPEND(data, '$', ?)",
        mysqlDialect.makeJsonArrayAppend(JsonType.JSON, "data"));
    assertEquals("JSON_ARRAY_APPEND(data, '$', ?)",
        mysqlDialect.makeJsonArrayAppend(JsonType.JSONB, "data"));
  }

  @Test
  @DisplayName("MySQL - makeJsonArrayPrepend 对任何类型都应该返回 JSON_ARRAY_INSERT")
  void testMysqlJsonArrayPrepend() {
    // MySQL 忽略 JsonType 参数，统一使用 JSON_ARRAY_INSERT
    assertEquals("JSON_ARRAY_INSERT(data, '$[0]', ?)",
        mysqlDialect.makeJsonArrayPrepend(JsonType.JSON, "data"));
    assertEquals("JSON_ARRAY_INSERT(data, '$[0]', ?)",
        mysqlDialect.makeJsonArrayPrepend(JsonType.JSONB, "data"));
  }

  @Test
  @DisplayName("MySQL - makeJsonStripNulls 对任何类型都应该抛出 UnsupportedOperationException")
  void testMysqlJsonStripNulls() {
    // MySQL 不支持 JSON strip nulls 操作
    UnsupportedOperationException exceptionJson = assertThrows(
        UnsupportedOperationException.class,
        () -> mysqlDialect.makeJsonStripNulls(JsonType.JSON, "data"));

    UnsupportedOperationException exceptionJsonb = assertThrows(
        UnsupportedOperationException.class,
        () -> mysqlDialect.makeJsonStripNulls(JsonType.JSONB, "data"));

    assertEquals(
        "MySQL does not support JSON strip nulls operation natively, you need to implement a custom function or handle it in application layer",
        exceptionJson.getMessage());
    assertEquals(
        "MySQL does not support JSON strip nulls operation natively, you need to implement a custom function or handle it in application layer",
        exceptionJsonb.getMessage());
  }

  // ==================== API 一致性测试 ====================

  @Test
  @DisplayName("所有方法都应该要求 JsonType 参数")
  void testApiConsistency() {
    // 这个测试确保新 API 的一致性：所有方法都需要 JsonType 参数
    // 如果编译通过，说明 API 设计一致

    // PostgreSQL
    postgresDialect.makeTypeCast(JsonType.JSONB);
    postgresDialect.makeJsonMerge(JsonType.JSONB, "col");
    postgresDialect.makeJsonSetPath(JsonType.JSONB, "col", "path", true);
    postgresDialect.makeJsonArrayAppend(JsonType.JSONB, "col");
    postgresDialect.makeJsonArrayPrepend(JsonType.JSONB, "col");
    postgresDialect.makeJsonStripNulls(JsonType.JSONB, "col");

    // MySQL
    mysqlDialect.makeTypeCast(JsonType.JSON);
    mysqlDialect.makeJsonMerge(JsonType.JSON, "col");
    mysqlDialect.makeJsonSetPath(JsonType.JSON, "col", "path", true);
    mysqlDialect.makeJsonArrayAppend(JsonType.JSON, "col");
    mysqlDialect.makeJsonArrayPrepend(JsonType.JSON, "col");
    // MySQL 的 stripNulls 会抛出异常，但 API 签名是一致的
  }
}
