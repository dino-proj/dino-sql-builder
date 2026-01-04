// Copyright 2026 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.JsonPath;
import cn.dinodev.sql.naming.SnakeNamingConversition;
import cn.dinodev.sql.testutil.DatabaseMetaDataMocks;

/**
 * JsonPath 格式化测试类。
 * <p>
 * 测试不同数据库方言对 JsonPath 的格式化输出。
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("JsonPath 格式化测试")
class JsonPathFormattingTest {

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

  // ==================== PostgreSQL 路径格式化测试 ====================

  @Test
  @DisplayName("PostgreSQL - formatPath(root) 应该返回 {}")
  void testPostgresFormatPathRoot() {
    JsonPath path = JsonPath.of();
    String result = postgresDialect.formatPath(path);
    assertEquals("{}", result);
  }

  @Test
  @DisplayName("PostgreSQL - formatPath(单键) 应该返回 {key}")
  void testPostgresFormatPathSingleKey() {
    JsonPath path = JsonPath.of("address");
    String result = postgresDialect.formatPath(path);
    assertEquals("{address}", result);
  }

  @Test
  @DisplayName("PostgreSQL - formatPath(多键) 应该返回 {key1,key2,key3}")
  void testPostgresFormatPathMultipleKeys() {
    JsonPath path = JsonPath.of("user.profile.email");
    String result = postgresDialect.formatPath(path);
    assertEquals("{user,profile,email}", result);
  }

  @Test
  @DisplayName("PostgreSQL - formatPath(键+索引) 应该返回 {key,0}")
  void testPostgresFormatPathKeyIndex() {
    JsonPath path = JsonPath.of("users", 0);
    String result = postgresDialect.formatPath(path);
    assertEquals("{users,0}", result);
  }

  @Test
  @DisplayName("PostgreSQL - formatPath(复杂路径) 应该返回正确格式")
  void testPostgresFormatPathComplex() {
    JsonPath path = JsonPath.of("data", "items", 5, "tags", 2);
    String result = postgresDialect.formatPath(path);
    assertEquals("{data,items,5,tags,2}", result);
  }

  // ==================== MySQL 路径格式化测试 ====================

  @Test
  @DisplayName("MySQL - formatPath(root) 应该返回 $")
  void testMysqlFormatPathRoot() {
    JsonPath path = JsonPath.of();
    String result = mysqlDialect.formatPath(path);
    assertEquals("$", result);
  }

  @Test
  @DisplayName("MySQL - formatPath(单键) 应该返回 $.key")
  void testMysqlFormatPathSingleKey() {
    JsonPath path = JsonPath.of("address");
    String result = mysqlDialect.formatPath(path);
    assertEquals("$.address", result);
  }

  @Test
  @DisplayName("MySQL - formatPath(多键) 应该返回 $.key1.key2.key3")
  void testMysqlFormatPathMultipleKeys() {
    JsonPath path = JsonPath.of("user.profile.email");
    String result = mysqlDialect.formatPath(path);
    assertEquals("$.user.profile.email", result);
  }

  @Test
  @DisplayName("MySQL - formatPath(键+索引) 应该返回 $.key[0]")
  void testMysqlFormatPathKeyIndex() {
    JsonPath path = JsonPath.of("users", 0);
    String result = mysqlDialect.formatPath(path);
    assertEquals("$.users[0]", result);
  }

  @Test
  @DisplayName("MySQL - formatPath(复杂路径) 应该返回正确格式")
  void testMysqlFormatPathComplex() {
    JsonPath path = JsonPath.of("data", "items", 5, "tags", 2);
    String result = mysqlDialect.formatPath(path);
    assertEquals("$.data.items[5].tags[2]", result);
  }
}
