// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Locale;

import cn.dinodev.sql.naming.NamingConversition;
import cn.dinodev.sql.naming.SnakeNamingConversition;

/**
 * 数据库方言接口。
 * <p>
 * 用于适配不同数据库的 SQL 生成与命名转换逻辑。
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public interface Dialect {

  /**
   * 获取命名转换器
   * @return 命名转换器实例
   */
  NamingConversition namingConversition();

  /**
   * 获取方言名称
   * @return 方言名称字符串
   */
  String getDialectName();

  /**
   * 获取主版本号
   * @return 主版本号
   */
  default int getMajorVersion() {
    return 0;
  }

  /**
   * 字段名加引号（防止关键字冲突）
   * @param columnName 字段名
   * @return 加引号后的字段名
   */
  String quoteColumnName(String columnName);

  /**
   * 获取当前 schema 的 SQL 语句
   * @return 查询当前 schema 的 SQL
   */
  String getCurrentSchemaSql();

  /**
   * 检查当前数据库元数据是否兼容该方言
   * @param metaData 数据库元数据
   * @return 兼容返回 true，否则 false
   */
  boolean isCompatible(DatabaseMetaData metaData);

  /**
   * 生成 LIMIT/OFFSET 语句
   * @param limit 限制条数
   * @param offset 偏移量
   * @return SQL 片段
   */
  String limitOffset(int limit, long offset);

  /**
   * 是否支持 UUID 语句
   * @return 支持返回 true，否则 false
   */
  boolean supportUUID();

  /**
   * 生成查询 UUID 的 SQL 语句
   * @return 查询 UUID 的 SQL 语句
   */
  String getSelectUUIDSql();

  /**
   * 获取 UUID 生成函数表达式（不含 SELECT）。
   * <p>
   * 用于 INSERT/UPDATE 语句中生成 UUID 值。
   * <p>
   * 不同数据库的 UUID 生成函数：
   * <ul>
   *   <li><b>MySQL</b>: UUID()</li>
   *   <li><b>PostgreSQL 13+</b>: gen_random_uuid()</li>
   *   <li><b>PostgreSQL &lt;13</b>: uuid_generate_v4() (需要 uuid-ossp 扩展)</li>
   * </ul>
   * 
   * @return UUID 生成函数表达式
   */
  String getUuidFunction();

  /**
   * 生成查询 sequence 的 SQL 语句
   * @param sequenceName 序列名称
   * @return 查询序列的 SQL 语句
   */
  String getSequenceNextValSql(String sequenceName);

  /**
   * 是否支持 sequence
   * @return 支持返回 true，否则 false
   */
  boolean supportSequence();

  /**
   * 表名加引号（防止关键字冲突）
   * @param name 表名
   * @return 加引号后的表名
   */
  String quoteTableName(String name);

  /**
   * 生成正则表达式匹配的 SQL 表达式。
   * <p>
   * 不同数据库的正则表达式语法：
   * <ul>
   *   <li>MySQL/MariaDB: column REGEXP ?</li>
   *   <li>PostgreSQL: column ~ ?</li>
   *   <li>Oracle: REGEXP_LIKE(column, ?)</li>
   * </ul>
   * 
   * @param column 列名
   * @return 正则表达式匹配的 SQL 表达式
   */
  default String makeRegexpExpr(String column) {
    return column + " REGEXP ?";
  }

  /**
   * 生成正则表达式不匹配的 SQL 表达式。
   * <p>
   * 不同数据库的语法：
   * <ul>
   *   <li>MySQL/MariaDB: column NOT REGEXP ?</li>
   *   <li>PostgreSQL: column !~ ?</li>
   *   <li>Oracle: NOT REGEXP_LIKE(column, ?)</li>
   * </ul>
   * 
   * @param column 列名
   * @return 正则表达式不匹配的 SQL 表达式
   */
  default String makeNotRegexpExpr(String column) {
    return column + " NOT REGEXP ?";
  }

  /**
   * 是否支持 GROUP BY ALL 语法。
   * <p>
   * GROUP BY ALL 会自动将 SELECT 列表中所有非聚合列包含到 GROUP BY 中。
   * <ul>
   *   <li>PostgreSQL 15+: 支持</li>
   *   <li>MySQL: 不支持</li>
   *   <li>Oracle: 不支持</li>
   * </ul>
   * 
   * @return 支持返回 true，否则 false
   */
  default boolean supportsGroupByAll() {
    return false;
  }

  /**
   * 是否支持 CTE 物化提示（MATERIALIZED/NOT MATERIALIZED）。
   * <p>
   * CTE 物化提示用于控制公共表表达式（CTE）是否应该被物化（缓存结果）或内联到主查询中。
   * <ul>
   *   <li>PostgreSQL 12+: 支持</li>
   *   <li>MySQL: 不支持</li>
   *   <li>Oracle: 不支持</li>
   *   <li>SQL Server: 不支持</li>
   * </ul>
   * <p>
   * 示例（PostgreSQL 12+）：
   * <pre>
   * WITH cte_name AS MATERIALIZED (
   *   SELECT * FROM expensive_query
   * )
   * SELECT * FROM cte_name;
   * </pre>
   * 
   * @return 支持返回 true，否则 false
   * @since 2024-12-31
   */
  default boolean supportsMaterializedCTE() {
    return false;
  }

  /**
   * 生成二进制类型转换表达式。
   * <p>
   * 不同数据库的二进制类型转换：
   * <ul>
   *   <li><b>MySQL</b>: ? (直接使用占位符，MySQL 会自动处理 BLOB/VARBINARY)</li>
   *   <li><b>PostgreSQL</b>: ?::bytea (需要显式类型转换)</li>
   * </ul>
   * 
   * @return 二进制类型转换表达式
   * @since 2026-01-04
   */
  default String makeBinaryTypeCast() {
    return "?";
  }

  // ==================== 字符串操作 ====================

  /**
   * 生成字符串拼接表达式（在末尾追加）。
   * <p>
   * 不同数据库的实现：
   * <ul>
   *   <li><b>MySQL</b>: CONCAT(column, ?)</li>
   *   <li><b>PostgreSQL</b>: column || ?</li>
   * </ul>
   * 
   * @param column 列名
   * @return 字符串拼接表达式
   * @since 2026-01-04
   */
  String makeStringConcat(String column);

  /**
   * 生成字符串前置拼接表达式（在开头添加）。
   * <p>
   * 不同数据库的实现：
   * <ul>
   *   <li><b>MySQL</b>: CONCAT(?, column)</li>
   *   <li><b>PostgreSQL</b>: ? || column</li>
   * </ul>
   * 
   * @param column 列名
   * @return 字符串前置拼接表达式
   * @since 2026-01-04
   */
  String makeStringPrepend(String column);

  // ==================== JSON/JSONB 操作 ====================

  /**
   * 获取 JSON 操作方言。
   * <p>
   * 返回处理 JSON/JSONB 操作的方言实例，用于生成数据库特定的 JSON SQL。
   * 
   * @return JSON 操作方言实例
   * @since 2026-01-04
   */
  JsonDialect jsonDialect();

  /**
   * 根据数据库连接自动识别并返回对应的 Dialect 实现
   * @param conn 数据库连接
   * @return 方言实例，无法识别时抛出异常
   * @throws SQLException 获取元数据异常，UnsupportedOperationException 无法识别数据库类型
   */
  static Dialect fromConnection(Connection conn) throws SQLException {
    DatabaseMetaData metaData = conn.getMetaData();
    Dialect dialect = null;

    String name = metaData.getDatabaseProductName().toLowerCase(Locale.ENGLISH);

    if (name.contains("mysql") || name.contains("mariadb")) {
      dialect = new MysqlDialect(metaData, new SnakeNamingConversition());
    } else if (name.contains("postgresql")) {
      dialect = new PostgreSQLDialect(metaData, new SnakeNamingConversition());
    } else {
      throw new UnsupportedOperationException("Couldn't determine DB Dialect for " + name);
    }

    return dialect;
  }

}
