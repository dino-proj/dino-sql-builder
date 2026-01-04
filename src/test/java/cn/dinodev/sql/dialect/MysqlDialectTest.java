// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.DatabaseMetaData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.naming.CamelNamingConversition;
import cn.dinodev.sql.naming.NamingConversition;
import cn.dinodev.sql.naming.SnakeNamingConversition;
import cn.dinodev.sql.testutil.DatabaseMetaDataMocks;

/**
 * MySQL 数据库方言测试类。
 * 
 * <p>测试 {@link MysqlDialect} 的所有功能，包括：
 * <ul>
 *   <li>基础信息获取（方言名称、版本号）</li>
 *   <li>SQL 生成（LIMIT/OFFSET、UUID、引号包裹）</li>
 *   <li>功能支持检测（UUID、Sequence、高级特性）</li>
 *   <li>命名转换集成</li>
 *   <li>兼容性检测</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2026-01-03
 */
@DisplayName("MysqlDialect 功能测试")
class MysqlDialectTest {

  private NamingConversition snakeNaming;
  private NamingConversition camelNaming;

  @BeforeEach
  void setUp() {
    snakeNaming = new SnakeNamingConversition();
    camelNaming = new CamelNamingConversition();
  }

  // ==================== 基础信息测试 ====================

  @Test
  @DisplayName("测试方言名称应该是 'mysql'")
  void testGetDialectName() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("mysql", dialect.getDialectName(), "MySQL 方言名称应该是 'mysql'");
  }

  @Test
  @DisplayName("测试 MySQL 8 版本号获取")
  void testGetMajorVersionMySQL8() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals(8, dialect.getMajorVersion(), "应该正确获取 MySQL 主版本号");
  }

  @Test
  @DisplayName("测试 MySQL 5 版本号获取")
  void testGetMajorVersionMySQL5() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(5);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals(5, dialect.getMajorVersion(), "应该正确获取 MySQL 5 主版本号");
  }

  @Test
  @DisplayName("测试元数据为 null 时版本号默认为 0")
  void testGetMajorVersionWithNullMetadata() {
    MysqlDialect dialect = new MysqlDialect(null, snakeNaming);

    assertEquals(0, dialect.getMajorVersion(), "元数据为 null 时，版本号应该默认为 0");
  }

  @Test
  @DisplayName("测试命名转换器集成")
  void testNamingConversition() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialectWithSnake = new MysqlDialect(metadata, snakeNaming);
    MysqlDialect dialectWithCamel = new MysqlDialect(metadata, camelNaming);

    assertEquals(snakeNaming, dialectWithSnake.namingConversition(),
        "应该返回构造时传入的下划线命名转换器");
    assertEquals(camelNaming, dialectWithCamel.namingConversition(),
        "应该返回构造时传入的驼峰命名转换器");
  }

  // ==================== LIMIT/OFFSET 测试 ====================

  @Test
  @DisplayName("测试仅 LIMIT 语句生成")
  void testLimitOnly() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("LIMIT 10", dialect.limitOffset(10, 0),
        "只有 LIMIT 时应该生成 'LIMIT 10'");
  }

  @Test
  @DisplayName("测试 LIMIT 和 OFFSET 语句生成")
  void testLimitWithOffset() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("LIMIT 20 OFFSET 5", dialect.limitOffset(20, 5),
        "LIMIT 和 OFFSET 都存在时应该生成 'LIMIT 20 OFFSET 5'");
  }

  @Test
  @DisplayName("测试 LIMIT 为 0 时返回空字符串")
  void testLimitZero() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("", dialect.limitOffset(0, 10),
        "LIMIT 为 0 时应该返回空字符串");
  }

  @Test
  @DisplayName("测试 LIMIT 为负数时返回空字符串")
  void testLimitNegative() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("", dialect.limitOffset(-1, 10),
        "LIMIT 为负数时应该返回空字符串");
  }

  @Test
  @DisplayName("测试大数值的 OFFSET")
  void testLargeOffset() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("LIMIT 10 OFFSET 1000000", dialect.limitOffset(10, 1000000),
        "应该支持大数值的 OFFSET");
  }

  // ==================== 表名和字段名引号包裹测试 ====================

  @Test
  @DisplayName("测试表名用反引号包裹")
  void testQuoteTableName() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("`users`", dialect.quoteTableName("users"),
        "MySQL 应该用反引号包裹表名");
    assertEquals("`order_items`", dialect.quoteTableName("order_items"),
        "MySQL 应该用反引号包裹带下划线的表名");
  }

  @Test
  @DisplayName("测试已包含引号的表名不重复包裹")
  void testQuoteTableNameAlreadyQuoted() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("`users`", dialect.quoteTableName("`users`"),
        "已包含反引号的表名不应该重复包裹");
  }

  @Test
  @DisplayName("测试字段名用反引号包裹")
  void testQuoteColumnName() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("`user_name`", dialect.quoteColumnName("user_name"),
        "MySQL 应该用反引号包裹字段名");
    assertEquals("`id`", dialect.quoteColumnName("id"),
        "MySQL 应该用反引号包裹简单字段名");
  }

  @Test
  @DisplayName("测试已包含引号的字段名不重复包裹")
  void testQuoteColumnNameAlreadyQuoted() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("`user_name`", dialect.quoteColumnName("`user_name`"),
        "已包含反引号的字段名不应该重复包裹");
  }

  // ==================== UUID 支持测试 ====================

  @Test
  @DisplayName("测试 MySQL 支持 UUID")
  void testSupportUUID() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertTrue(dialect.supportUUID(), "MySQL 应该支持 UUID");
  }

  @Test
  @DisplayName("测试 MySQL UUID 生成 SQL")
  void testGetSelectUUIDSql() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("SELECT UUID()", dialect.getSelectUUIDSql(),
        "MySQL 应该使用 UUID() 函数生成 UUID");
  }

  @Test
  @DisplayName("测试 MySQL UUID 函数表达式")
  void testGetUuidFunction() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("UUID()", dialect.getUuidFunction(),
        "MySQL UUID 函数表达式应该是 UUID()");
  }

  // ==================== Sequence 不支持测试 ====================

  @Test
  @DisplayName("测试 MySQL 不支持 Sequence")
  void testNotSupportSequence() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertFalse(dialect.supportSequence(), "MySQL 不应该支持 Sequence");
  }

  @Test
  @DisplayName("测试 MySQL 调用 Sequence 方法抛出异常")
  void testGetSequenceNextValSqlThrowsException() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertThrows(UnsupportedOperationException.class,
        () -> dialect.getSequenceNextValSql("test_seq"),
        "MySQL 不支持 Sequence，应该抛出 UnsupportedOperationException");
  }

  // ==================== 当前 Schema 查询测试 ====================

  @Test
  @DisplayName("测试 MySQL 当前 Schema 查询 SQL")
  void testGetCurrentSchemaSql() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("SELECT DATABASE()", dialect.getCurrentSchemaSql(),
        "MySQL 应该使用 DATABASE() 函数查询当前 schema");
  }

  // ==================== 正则表达式支持测试 ====================

  @Test
  @DisplayName("测试 MySQL 正则表达式匹配操作符")
  void testMakeRegexpExpr() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("email REGEXP ?", dialect.makeRegexpExpr("email"),
        "MySQL 应该使用 REGEXP 操作符");
    assertEquals("user_name REGEXP ?", dialect.makeRegexpExpr("user_name"),
        "MySQL 正则表达式应该支持任意列名");
  }

  @Test
  @DisplayName("测试 MySQL 正则表达式不匹配操作符")
  void testMakeNotRegexpExpr() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertEquals("email NOT REGEXP ?", dialect.makeNotRegexpExpr("email"),
        "MySQL 应该使用 NOT REGEXP 操作符");
    assertEquals("user_name NOT REGEXP ?", dialect.makeNotRegexpExpr("user_name"),
        "MySQL 正则表达式不匹配应该支持任意列名");
  }

  // ==================== 高级特性不支持测试 ====================

  @Test
  @DisplayName("测试 MySQL 不支持 GROUP BY ALL")
  void testNotSupportGroupByAll() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertFalse(dialect.supportsGroupByAll(),
        "MySQL 不支持 GROUP BY ALL 语法");
  }

  @Test
  @DisplayName("测试 MySQL 不支持 CTE 物化")
  void testNotSupportMaterializedCTE() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertFalse(dialect.supportsMaterializedCTE(),
        "MySQL 不支持 CTE 物化提示");
  }

  // ==================== 兼容性检测测试 ====================

  @Test
  @DisplayName("测试 MySQL 数据库兼容性检测")
  void testIsCompatibleWithMySQL() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertTrue(dialect.isCompatible(metadata),
        "应该识别 MySQL 数据库为兼容");
  }

  @Test
  @DisplayName("测试 MariaDB 数据库兼容性检测")
  void testIsCompatibleWithMariaDB() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.create(10, "MariaDB");
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertTrue(dialect.isCompatible(metadata),
        "应该识别 MariaDB 为兼容（使用 MySQL 方言）");
  }

  @Test
  @DisplayName("测试 PostgreSQL 数据库不兼容")
  void testIsNotCompatibleWithPostgreSQL() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createPostgreSQL(15);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertFalse(dialect.isCompatible(metadata),
        "PostgreSQL 不应该与 MySQL 方言兼容");
  }

  @Test
  @DisplayName("测试未知数据库不兼容")
  void testIsNotCompatibleWithUnknownDatabase() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.create(1, "OracleDB");
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    assertFalse(dialect.isCompatible(metadata),
        "未知数据库不应该与 MySQL 方言兼容");
  }

  // ==================== 字符串拼接操作测试 ====================

  @Test
  @DisplayName("测试字符串追加拼接表达式")
  void testMakeStringConcat() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    String result = dialect.makeStringConcat("user_name");
    assertEquals("CONCAT(user_name, ?)", result,
        "MySQL 应该使用 CONCAT 函数进行字符串拼接");
  }

  @Test
  @DisplayName("测试字符串前置拼接表达式")
  void testMakeStringPrepend() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    String result = dialect.makeStringPrepend("title");
    assertEquals("CONCAT(?, title)", result,
        "MySQL 应该使用 CONCAT 函数进行字符串前置拼接");
  }

  @Test
  @DisplayName("测试带表别名的字符串拼接")
  void testMakeStringConcatWithTableAlias() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    String result = dialect.makeStringConcat("u.full_name");
    assertEquals("CONCAT(u.full_name, ?)", result,
        "MySQL 应该正确处理带表别名的列名");
  }

  @Test
  @DisplayName("测试带反引号的列名字符串拼接")
  void testMakeStringConcatWithQuotedColumn() {
    DatabaseMetaData metadata = DatabaseMetaDataMocks.createMySQL(8);
    MysqlDialect dialect = new MysqlDialect(metadata, snakeNaming);

    String result = dialect.makeStringConcat("`order`");
    assertEquals("CONCAT(`order`, ?)", result,
        "MySQL 应该正确处理带反引号的列名");
  }
}
