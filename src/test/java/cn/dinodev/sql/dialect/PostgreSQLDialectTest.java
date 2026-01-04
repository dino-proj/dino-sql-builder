// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.naming.CamelNamingConversition;
import cn.dinodev.sql.naming.NamingConversition;
import cn.dinodev.sql.naming.SnakeNamingConversition;
import cn.dinodev.sql.testutil.DatabaseMetaDataMocks;

/**
 * PostgreSQL 数据库方言测试类。
 * 
 * <p>测试 {@link PostgreSQLDialect} 的所有功能，重点关注版本差异：
 * <ul>
 *   <li><b>PostgreSQL 12</b>: CTE 物化支持</li>
 *   <li><b>PostgreSQL 13</b>: 内置 UUID 函数（gen_random_uuid）</li>
 *   <li><b>PostgreSQL 17</b>: GROUP BY ALL 支持</li>
 *   <li>基础功能：Sequence、正则表达式、引号包裹</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2026-01-03
 */
@DisplayName("PostgreSQLDialect 功能测试")
class PostgreSQLDialectTest {

  private NamingConversition snakeNaming;
  private NamingConversition camelNaming;

  @BeforeEach
  void setUp() {
    snakeNaming = new SnakeNamingConversition();
    camelNaming = new CamelNamingConversition();
  }

  // ==================== 基础信息测试 ====================

  @Test
  @DisplayName("测试方言名称应该是 'postgresql'")
  void testGetDialectName() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("postgresql", dialect.getDialectName(),
        "PostgreSQL 方言名称应该是 'postgresql'");
  }

  @Test
  @DisplayName("测试命名转换器集成")
  void testNamingConversition() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialectWithSnake = new PostgreSQLDialect(metadata, snakeNaming);
    PostgreSQLDialect dialectWithCamel = new PostgreSQLDialect(metadata, camelNaming);

    assertEquals(snakeNaming, dialectWithSnake.namingConversition(),
        "应该返回构造时传入的下划线命名转换器");
    assertEquals(camelNaming, dialectWithCamel.namingConversition(),
        "应该返回构造时传入的驼峰命名转换器");
  }

  // ==================== 版本号获取测试 ====================

  @Test
  @DisplayName("测试 PostgreSQL 12 版本号获取")
  void testGetMajorVersionPostgreSQL12() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV12;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals(12, dialect.getMajorVersion(), "应该正确获取 PostgreSQL 12 主版本号");
  }

  @Test
  @DisplayName("测试 PostgreSQL 13 版本号获取")
  void testGetMajorVersionPostgreSQL13() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV13;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals(13, dialect.getMajorVersion(), "应该正确获取 PostgreSQL 13 主版本号");
  }

  @Test
  @DisplayName("测试 PostgreSQL 15 版本号获取")
  void testGetMajorVersionPostgreSQL15() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals(15, dialect.getMajorVersion(), "应该正确获取 PostgreSQL 15 主版本号");
  }

  @Test
  @DisplayName("测试 PostgreSQL 16 版本号获取")
  void testGetMajorVersionPostgreSQL16() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV16;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals(16, dialect.getMajorVersion(), "应该正确获取 PostgreSQL 16 主版本号");
  }

  @Test
  @DisplayName("测试元数据为 null 时版本号默认为 0")
  void testGetMajorVersionWithNullMetadata() throws SQLException {
    PostgreSQLDialect dialect = new PostgreSQLDialect(null, snakeNaming);

    assertEquals(0, dialect.getMajorVersion(), "元数据为 null 时，版本号应该默认为 0");
  }

  // ==================== UUID 功能版本差异测试（关键测试点）====================

  @Test
  @DisplayName("测试 PostgreSQL 12 使用传统 UUID 函数（uuid_generate_v4）")
  void testPostgreSQL12UsesLegacyUuidFunction() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV12;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertTrue(dialect.supportUUID(), "PostgreSQL 12 应该支持 UUID");
    assertEquals("SELECT uuid_generate_v4()", dialect.getSelectUUIDSql(),
        "PostgreSQL 12 应该使用 uuid-ossp 扩展的 uuid_generate_v4() 函数");
  }

  @Test
  @DisplayName("测试 PostgreSQL 13 使用内置 UUID 函数（gen_random_uuid）")
  void testPostgreSQL13UsesBuiltinUuidFunction() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV13;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertTrue(dialect.supportUUID(), "PostgreSQL 13 应该支持 UUID");
    assertEquals("SELECT gen_random_uuid()", dialect.getSelectUUIDSql(),
        "PostgreSQL 13 应该使用内置的 gen_random_uuid() 函数");
  }

  @Test
  @DisplayName("测试 PostgreSQL 14 使用内置 UUID 函数")
  void testPostgreSQL14UsesBuiltinUuidFunction() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV14;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("SELECT gen_random_uuid()", dialect.getSelectUUIDSql(),
        "PostgreSQL 14 应该使用内置的 gen_random_uuid() 函数");
  }

  @Test
  @DisplayName("测试 PostgreSQL 12 UUID 函数表达式（传统）")
  void testPostgreSQL12UuidFunctionExpression() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV12;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("uuid_generate_v4()", dialect.getUuidFunction(),
        "PostgreSQL 12 UUID 函数表达式应该是 uuid_generate_v4()");
  }

  @Test
  @DisplayName("测试 PostgreSQL 13+ UUID 函数表达式（内置）")
  void testPostgreSQL13UuidFunctionExpression() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV13;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("gen_random_uuid()", dialect.getUuidFunction(),
        "PostgreSQL 13+ UUID 函数表达式应该是 gen_random_uuid()");
  }

  @Test
  @DisplayName("测试 PostgreSQL 15 UUID 函数表达式（内置）")
  void testPostgreSQL15UuidFunctionExpression() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("gen_random_uuid()", dialect.getUuidFunction(),
        "PostgreSQL 15 UUID 函数表达式应该是 gen_random_uuid()");
  }

  @Test
  @DisplayName("测试 PostgreSQL 15 使用内置 UUID 函数")
  void testPostgreSQL15UsesBuiltinUuidFunction() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("SELECT gen_random_uuid()", dialect.getSelectUUIDSql(),
        "PostgreSQL 15 应该使用内置的 gen_random_uuid() 函数");
  }

  @Test
  @DisplayName("测试 PostgreSQL 16+ 使用内置 UUID 函数")
  void testPostgreSQL16UsesBuiltinUuidFunction() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV16;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("SELECT gen_random_uuid()", dialect.getSelectUUIDSql(),
        "PostgreSQL 16+ 应该使用内置的 gen_random_uuid() 函数");
  }

  @Test
  @DisplayName("测试 PostgreSQL 18+ UUID 函数表达式（UUIDv7）")
  void testPostgreSQL18UuidFunctionExpression() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV18;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("uuidv7()", dialect.getUuidFunction(),
        "PostgreSQL 18+ UUID 函数表达式应该是 uuidv7()");
  }

  @Test
  @DisplayName("测试 PostgreSQL 18+ 使用 UUIDv7 函数")
  void testPostgreSQL18UsesUuidv7Function() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV18;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("SELECT uuidv7()", dialect.getSelectUUIDSql(),
        "PostgreSQL 18+ 应该使用 uuidv7() 函数");
  }

  // ==================== CTE 物化功能版本差异测试（关键测试点）====================

  @Test
  @DisplayName("测试 PostgreSQL 12 支持 CTE 物化")
  void testPostgreSQL12SupportsMaterializedCTE() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV12;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertTrue(dialect.supportsMaterializedCTE(),
        "PostgreSQL 12 应该支持 CTE 物化提示（MATERIALIZED/NOT MATERIALIZED）");
  }

  @Test
  @DisplayName("测试 PostgreSQL 13 支持 CTE 物化")
  void testPostgreSQL13SupportsMaterializedCTE() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV13;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertTrue(dialect.supportsMaterializedCTE(),
        "PostgreSQL 13 应该支持 CTE 物化提示");
  }

  @Test
  @DisplayName("测试 PostgreSQL 14 支持 CTE 物化")
  void testPostgreSQL14SupportsMaterializedCTE() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV14;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertTrue(dialect.supportsMaterializedCTE(),
        "PostgreSQL 14 应该支持 CTE 物化提示");
  }

  @Test
  @DisplayName("测试 PostgreSQL 15 支持 CTE 物化")
  void testPostgreSQL15SupportsMaterializedCTE() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertTrue(dialect.supportsMaterializedCTE(),
        "PostgreSQL 15 应该支持 CTE 物化提示");
  }

  // ==================== GROUP BY ALL 功能版本差异测试（关键测试点）====================

  @Test
  @DisplayName("测试 PostgreSQL 12 不支持 GROUP BY ALL")
  void testPostgreSQL12NotSupportsGroupByAll() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV12;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertFalse(dialect.supportsGroupByAll(),
        "PostgreSQL 12 不应该支持 GROUP BY ALL 语法");
  }

  @Test
  @DisplayName("测试 PostgreSQL 13 不支持 GROUP BY ALL")
  void testPostgreSQL13NotSupportsGroupByAll() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV13;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertFalse(dialect.supportsGroupByAll(),
        "PostgreSQL 13 不应该支持 GROUP BY ALL 语法");
  }

  @Test
  @DisplayName("测试 PostgreSQL 14 不支持 GROUP BY ALL")
  void testPostgreSQL14NotSupportsGroupByAll() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV14;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertFalse(dialect.supportsGroupByAll(),
        "PostgreSQL 14 不应该支持 GROUP BY ALL 语法");
  }

  @Test
  @DisplayName("测试 PostgreSQL 15 不支持 GROUP BY ALL")
  void testPostgreSQL15NotSupportsGroupByAll() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertFalse(dialect.supportsGroupByAll(),
        "PostgreSQL 15 不应该支持 GROUP BY ALL 语法");
  }

  @Test
  @DisplayName("测试 PostgreSQL 16 不支持 GROUP BY ALL")
  void testPostgreSQL16NotSupportsGroupByAll() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV16;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertFalse(dialect.supportsGroupByAll(),
        "PostgreSQL 16 不应该支持 GROUP BY ALL 语法");
  }

  @Test
  @DisplayName("测试 PostgreSQL 17 支持 GROUP BY ALL")
  void testPostgreSQL17SupportsGroupByAll() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV17;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertTrue(dialect.supportsGroupByAll(),
        "PostgreSQL 17 开始支持 GROUP BY ALL 语法");
  }

  @Test
  @DisplayName("测试 PostgreSQL 18 支持 GROUP BY ALL")
  void testPostgreSQL18SupportsGroupByAll() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV18;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertTrue(dialect.supportsGroupByAll(),
        "PostgreSQL 18 应该支持 GROUP BY ALL 语法");
  }

  // ==================== LIMIT/OFFSET 测试 ====================

  @Test
  @DisplayName("测试仅 LIMIT 语句生成")
  void testLimitOnly() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("LIMIT 10", dialect.limitOffset(10, 0),
        "只有 LIMIT 时应该生成 'LIMIT 10'");
  }

  @Test
  @DisplayName("测试 LIMIT 和 OFFSET 语句生成")
  void testLimitWithOffset() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("LIMIT 20 OFFSET 5", dialect.limitOffset(20, 5),
        "LIMIT 和 OFFSET 都存在时应该生成 'LIMIT 20 OFFSET 5'");
  }

  @Test
  @DisplayName("测试 LIMIT 为 0 时返回空字符串")
  void testLimitZero() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("", dialect.limitOffset(0, 10),
        "LIMIT 为 0 时应该返回空字符串");
  }

  @Test
  @DisplayName("测试大数值的 OFFSET")
  void testLargeOffset() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("LIMIT 10 OFFSET 1000000", dialect.limitOffset(10, 1000000),
        "应该支持大数值的 OFFSET");
  }

  // ==================== 表名和字段名引号包裹测试 ====================

  @Test
  @DisplayName("测试表名用双引号包裹")
  void testQuoteTableName() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("\"users\"", dialect.quoteTableName("users"),
        "PostgreSQL 应该用双引号包裹表名");
    assertEquals("\"order_items\"", dialect.quoteTableName("order_items"),
        "PostgreSQL 应该用双引号包裹带下划线的表名");
  }

  @Test
  @DisplayName("测试已包含引号的表名不重复包裹")
  void testQuoteTableNameAlreadyQuoted() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("\"users\"", dialect.quoteTableName("\"users\""),
        "已包含双引号的表名不应该重复包裹");
  }

  @Test
  @DisplayName("测试字段名用双引号包裹")
  void testQuoteColumnName() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("\"user_name\"", dialect.quoteColumnName("user_name"),
        "PostgreSQL 应该用双引号包裹字段名");
    assertEquals("\"id\"", dialect.quoteColumnName("id"),
        "PostgreSQL 应该用双引号包裹简单字段名");
  }

  @Test
  @DisplayName("测试已包含引号的字段名不重复包裹")
  void testQuoteColumnNameAlreadyQuoted() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("\"user_name\"", dialect.quoteColumnName("\"user_name\""),
        "已包含双引号的字段名不应该重复包裹");
  }

  // ==================== Sequence 支持测试 ====================

  @Test
  @DisplayName("测试 PostgreSQL 支持 Sequence")
  void testSupportSequence() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertTrue(dialect.supportSequence(), "PostgreSQL 应该支持 Sequence");
  }

  @Test
  @DisplayName("测试 PostgreSQL Sequence 查询 SQL")
  void testGetSequenceNextValSql() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("SELECT nextval('user_id_seq')",
        dialect.getSequenceNextValSql("user_id_seq"),
        "PostgreSQL 应该使用 nextval() 函数查询 Sequence");
    assertEquals("SELECT nextval('order_seq')",
        dialect.getSequenceNextValSql("order_seq"),
        "PostgreSQL Sequence 查询应该支持任意序列名");
  }

  // ==================== 当前 Schema 查询测试 ====================

  @Test
  @DisplayName("测试 PostgreSQL 当前 Schema 查询 SQL")
  void testGetCurrentSchemaSql() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("SELECT current_schema()", dialect.getCurrentSchemaSql(),
        "PostgreSQL 应该使用 current_schema() 函数查询当前 schema");
  }

  // ==================== 正则表达式支持测试 ====================

  @Test
  @DisplayName("测试 PostgreSQL 正则表达式匹配操作符")
  void testMakeRegexpExpr() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("email ~ ?", dialect.makeRegexpExpr("email"),
        "PostgreSQL 应该使用 ~ 操作符进行正则表达式匹配");
    assertEquals("user_name ~ ?", dialect.makeRegexpExpr("user_name"),
        "PostgreSQL 正则表达式应该支持任意列名");
  }

  @Test
  @DisplayName("测试 PostgreSQL 正则表达式不匹配操作符")
  void testMakeNotRegexpExpr() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals("email !~ ?", dialect.makeNotRegexpExpr("email"),
        "PostgreSQL 应该使用 !~ 操作符进行正则表达式不匹配");
    assertEquals("user_name !~ ?", dialect.makeNotRegexpExpr("user_name"),
        "PostgreSQL 正则表达式不匹配应该支持任意列名");
  }

  // ==================== 兼容性检测测试 ====================

  @Test
  @DisplayName("测试 PostgreSQL 数据库兼容性检测")
  void testIsCompatibleWithPostgreSQL() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertTrue(dialect.isCompatible(metadata),
        "应该识别 PostgreSQL 数据库为兼容");
  }

  @Test
  @DisplayName("测试 MySQL 数据库不兼容")
  void testIsNotCompatibleWithMySQL() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.mysqlV8;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertFalse(dialect.isCompatible(metadata),
        "MySQL 不应该与 PostgreSQL 方言兼容");
  }

  @Test
  @DisplayName("测试未知数据库不兼容")
  void testIsNotCompatibleWithUnknownDatabase() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.create(1, "OracleDB");
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertFalse(dialect.isCompatible(metadata),
        "未知数据库不应该与 PostgreSQL 方言兼容");
  }

  // ==================== 综合版本特性测试 ====================

  @Test
  @DisplayName("测试 PostgreSQL 12 完整特性集")
  void testPostgreSQL12CompleteFeatureSet() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV12;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals(12, dialect.getMajorVersion(), "版本号应该是 12");
    assertEquals("SELECT uuid_generate_v4()", dialect.getSelectUUIDSql(),
        "应该使用传统 UUID 函数");
    assertTrue(dialect.supportsMaterializedCTE(), "应该支持 CTE 物化");
    assertFalse(dialect.supportsGroupByAll(), "不应该支持 GROUP BY ALL");
    assertTrue(dialect.supportSequence(), "应该支持 Sequence");
    assertTrue(dialect.supportUUID(), "应该支持 UUID");
  }

  @Test
  @DisplayName("测试 PostgreSQL 13 完整特性集")
  void testPostgreSQL13CompleteFeatureSet() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV13;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals(13, dialect.getMajorVersion(), "版本号应该是 13");
    assertEquals("SELECT gen_random_uuid()", dialect.getSelectUUIDSql(),
        "应该使用内置 UUID 函数");
    assertTrue(dialect.supportsMaterializedCTE(), "应该支持 CTE 物化");
    assertFalse(dialect.supportsGroupByAll(), "不应该支持 GROUP BY ALL");
    assertTrue(dialect.supportSequence(), "应该支持 Sequence");
    assertTrue(dialect.supportUUID(), "应该支持 UUID");
  }

  @Test
  @DisplayName("测试 PostgreSQL 15 完整特性集")
  void testPostgreSQL15CompleteFeatureSet() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV15;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals(15, dialect.getMajorVersion(), "版本号应该是 15");
    assertEquals("SELECT gen_random_uuid()", dialect.getSelectUUIDSql(),
        "应该使用内置 UUID 函数");
    assertTrue(dialect.supportsMaterializedCTE(), "应该支持 CTE 物化");
    assertFalse(dialect.supportsGroupByAll(), "不应该支持 GROUP BY ALL");
    assertTrue(dialect.supportSequence(), "应该支持 Sequence");
    assertTrue(dialect.supportUUID(), "应该支持 UUID");
  }

  @Test
  @DisplayName("测试 PostgreSQL 17 完整特性集")
  void testPostgreSQL17CompleteFeatureSet() throws SQLException {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.postgresV17;
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    assertEquals(17, dialect.getMajorVersion(), "版本号应该是 17");
    assertEquals("SELECT gen_random_uuid()", dialect.getSelectUUIDSql(),
        "应该使用内置 UUID 函数");
    assertTrue(dialect.supportsMaterializedCTE(), "应该支持 CTE 物化");
    assertTrue(dialect.supportsGroupByAll(), "应该支持 GROUP BY ALL");
    assertTrue(dialect.supportSequence(), "应该支持 Sequence");
    assertTrue(dialect.supportUUID(), "应该支持 UUID");
  }

  // ==================== 字符串拼接操作测试 ====================

  @Test
  @DisplayName("测试字符串追加拼接表达式")
  void testMakeStringConcat() throws Exception {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createPostgreSQL(15);
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    String result = dialect.makeStringConcat("user_name");
    assertEquals("user_name || ?", result,
        "PostgreSQL 应该使用 || 运算符进行字符串拼接");
  }

  @Test
  @DisplayName("测试字符串前置拼接表达式")
  void testMakeStringPrepend() throws Exception {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createPostgreSQL(15);
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    String result = dialect.makeStringPrepend("title");
    assertEquals("? || title", result,
        "PostgreSQL 应该使用 || 运算符进行字符串前置拼接");
  }

  @Test
  @DisplayName("测试带表别名的字符串拼接")
  void testMakeStringConcatWithTableAlias() throws Exception {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createPostgreSQL(15);
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    String result = dialect.makeStringConcat("u.full_name");
    assertEquals("u.full_name || ?", result,
        "PostgreSQL 应该正确处理带表别名的列名");
  }

  @Test
  @DisplayName("测试带双引号的列名字符串拼接")
  void testMakeStringConcatWithQuotedColumn() throws Exception {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createPostgreSQL(15);
    PostgreSQLDialect dialect = new PostgreSQLDialect(metadata, snakeNaming);

    String result = dialect.makeStringConcat("\"Order\"");
    assertEquals("\"Order\" || ?", result,
        "PostgreSQL 应该正确处理带双引号的列名");
  }

  @Test
  @DisplayName("测试不同版本的字符串拼接一致性")
  void testStringConcatConsistencyAcrossVersions() throws Exception {
    // PostgreSQL 12
    PostgreSQLDialect dialect12 = new PostgreSQLDialect(DatabaseMetaDataMocks.createPostgreSQL(12), snakeNaming);
    assertEquals("name || ?", dialect12.makeStringConcat("name"));
    assertEquals("? || name", dialect12.makeStringPrepend("name"));

    // PostgreSQL 15
    PostgreSQLDialect dialect15 = new PostgreSQLDialect(DatabaseMetaDataMocks.createPostgreSQL(15), snakeNaming);
    assertEquals("name || ?", dialect15.makeStringConcat("name"));
    assertEquals("? || name", dialect15.makeStringPrepend("name"));

    // PostgreSQL 17
    PostgreSQLDialect dialect17 = new PostgreSQLDialect(DatabaseMetaDataMocks.postgresV17, snakeNaming);
    assertEquals("name || ?", dialect17.makeStringConcat("name"));
    assertEquals("? || name", dialect17.makeStringPrepend("name"));
  }
}
