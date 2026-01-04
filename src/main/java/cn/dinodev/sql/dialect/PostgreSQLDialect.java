// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import cn.dinodev.sql.naming.NamingConversition;
import cn.dinodev.sql.utils.StringUtils;

/**
 * PostgreSQL 数据库方言实现。
 * <p>
 * 提供 PostgreSQL 专用的 SQL 生成与命名转换逻辑。
 * <p>
 * <h3>主要版本特性差异:</h3>
 * <ul>
 *   <li><b>PostgreSQL 10+</b>: 声明式分区、逻辑复制、并行查询、SCRAM-SHA-256认证</li>
 *   <li><b>PostgreSQL 11+</b>: 分区表性能改进、JIT编译、窗口函数性能优化</li>
 *   <li><b>PostgreSQL 12+</b>: 生成列、JSON路径表达式、B-tree索引改进、CTE内联优化</li>
 *   <li><b>PostgreSQL 13+</b>: gen_random_uuid()替代uuid-ossp、增量排序、B-tree去重</li>
 *   <li><b>PostgreSQL 14+</b>: 逻辑复制性能改进、多范围类型、订阅副本并行应用</li>
 *   <li><b>PostgreSQL 15+</b>: MERGE命令、逻辑复制行过滤、改进的分区表</li>
 *   <li><b>PostgreSQL 16+</b>: 增量备份、逻辑复制双向、改进的并行查询、SQL/JSON增强</li>
 *   <li><b>PostgreSQL 17+</b>: 支持GROUP BY ALL语法、MERGE...RETURNING、改进的VACUUM、逻辑复制改进</li>
 *   <li><b>PostgreSQL 18+</b>: 支持uuidv7()内置函数，基于时间戳的UUID v7生成</li>
 * </ul>
 * <p>
 * 使用工厂方法 {@link #of(DatabaseMetaData, NamingConversition)} 自动根据版本创建对应的方言实例。
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public class PostgreSQLDialect implements Dialect {
  /** 
   * 方言名称 
   */
  private static final String dialectName = "postgresql";

  /** 
   * 命名转换器实例 
   */
  private final NamingConversition namingConversitionInstance;

  /** 数据库主版本号 */
  protected final int majorVersion;

  /**
   * 构造函数，创建 PostgreSQL 数据库方言实例
   * @param metaData 数据库元数据
   * @param namingConversition 命名转换策略
   * @throws SQLException 获取数据库主版本号异常
   */
  public PostgreSQLDialect(DatabaseMetaData metaData, NamingConversition namingConversition) throws SQLException {
    this.namingConversitionInstance = namingConversition;
    this.majorVersion = metaData != null ? metaData.getDatabaseMajorVersion() : 0;
  }

  /**
   * 获取命名转换器
   * @return 命名转换器实例
   */
  @Override
  public NamingConversition namingConversition() {
    return namingConversitionInstance;
  }

  /**
   * 获取方言名称
   * @return 方言名称字符串
   */
  @Override
  public String getDialectName() {
    return dialectName;
  }

  /**
   * 获取主版本号
   * @return 主版本号
   */
  @Override
  public int getMajorVersion() {
    return majorVersion;
  }

  /**
   * 生成 LIMIT/OFFSET 语句
   * @param limit 限制条数
   * @param offset 偏移量
   * @return SQL 片段
   */
  @Override
  public String limitOffset(int limit, long offset) {
    if (limit > 0) {
      return offset > 0 ? "LIMIT " + limit + " OFFSET " + offset : "LIMIT " + limit;
    }
    return "";
  }

  /**
   * 表名加引号（防止关键字冲突）
   * @param name 表名
   * @return 加引号后的表名
   */
  @Override
  public String quoteTableName(String name) {
    return StringUtils.wrapIfMissing(name, '"');
  }

  /**
   * 字段名加引号（防止关键字冲突）
   * @param columnName 字段名
   * @return 加引号后的字段名
   */
  @Override
  public String quoteColumnName(String columnName) {
    return StringUtils.wrapIfMissing(columnName, '"');
  }

  /**
   * 生成查询 sequence 的 SQL 语句
   * @param sequenceName 序列名称
   * @return 查询序列的 SQL 语句
   */
  @Override
  public String getSequenceNextValSql(String sequenceName) {
    return "SELECT nextval('" + sequenceName + "')";
  }

  /**
   * 是否支持 sequence
   * @return 支持返回 true，否则 false
   */
  @Override
  public boolean supportSequence() {
    return true;
  }

  /**
   * 是否支持 UUID 语句
   * @return 支持返回 true，否则 false
   */
  @Override
  public boolean supportUUID() {
    return true;
  }

  /**
   * 获取 UUID 生成函数表达式（不含 SELECT）。
   * <p>
   * 根据 PostgreSQL 版本返回相应的 UUID 生成函数：
   * <ul>
   *   <li><b>PostgreSQL 18+</b>: uuidv7() - 基于时间戳的UUID v7，排序性更好</li>
   *   <li><b>PostgreSQL 13-17</b>: gen_random_uuid() - 随机UUID v4</li>
   *   <li><b>PostgreSQL 12 及更早</b>: uuid_generate_v4() - 需要uuid-ossp扩展</li>
   * </ul>
   * 
   * @return UUID 生成函数表达式
   */
  @Override
  public String getUuidFunction() {
    // PostgreSQL 18+ 使用 UUIDv7
    if (majorVersion >= 18) {
      return "uuidv7()";
    }
    // PostgreSQL 13+ 使用内置函数
    if (majorVersion >= 13) {
      return "gen_random_uuid()";
    }
    // PostgreSQL 12及之前使用uuid-ossp扩展
    return "uuid_generate_v4()";
  }

  /**
   * 生成查询 UUID 的 SQL 语句。
   * <p>
   * 通过复用 {@link #getUuidFunction()} 构建完整的 SELECT 语句。
   * <p>
   * <b>版本差异说明:</b>
   * <ul>
   *   <li><b>PostgreSQL 18+</b>: 使用内置的 uuidv7()，基于时间戳的UUID v7</li>
   *   <li><b>PostgreSQL 13-17</b>: 使用内置的 gen_random_uuid()，无需扩展</li>
   *   <li><b>PostgreSQL 9.4-12</b>: 使用 uuid-ossp 扩展的 uuid_generate_v4()</li>
   *   <li><b>PostgreSQL 9.3及更早</b>: 需要手动安装 uuid-ossp 扩展</li>
   * </ul>
   * <p>
   * 注意: 如果使用PostgreSQL 13之前的版本，需要确保已安装uuid-ossp扩展:
   * <pre>{@code
   * CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
   * }</pre>
   * 
   * @return 查询 UUID 的 SQL 语句
   */
  @Override
  public String getSelectUUIDSql() {
    return "SELECT " + getUuidFunction();
  }

  /**
   * 获取当前 schema 的 SQL 语句
   * @return 查询当前 schema 的 SQL
   */
  @Override
  public String getCurrentSchemaSql() {
    // PostgreSQL 当前 schema 可用 current_schema
    return "SELECT current_schema()";
  }

  /**
   * 检查当前数据库元数据是否兼容该方言
   * @param metaData 数据库元数据
   * @return 兼容返回 true，否则 false
   */
  /**
   * PostgreSQL 使用 ~ 操作符进行正则表达式匹配（区分大小写）
   * <p>
   * 生成形如: column ~ ?
   * 
   * @param column 列名
   * @return 正则表达式匹配的 SQL 表达式
   */
  @Override
  public String makeRegexpExpr(String column) {
    return column + " ~ ?";
  }

  /**
   * PostgreSQL 使用 !~ 操作符进行正则表达式不匹配（区分大小写）
   * <p>
   * 生成形如: column !~ ?
   * 
   * @param column 列名
   * @return 正则表达式不匹配的 SQL 表达式
   */
  @Override
  public String makeNotRegexpExpr(String column) {
    return column + " !~ ?";
  }

  /**
   * PostgreSQL 从 17 版本开始支持 GROUP BY ALL 语法。
   * <p>
   * 本方法会检查当前 PostgreSQL 实例的版本号，只有版本 >= 17 时才返回 true。
   * 如果构造时未提供 DatabaseMetaData（metaData 为 null），则默认返回 false。
   * 
   * @return 版本 >= 17 时返回 true，否则返回 false
   */
  @Override
  public boolean supportsGroupByAll() {
    return majorVersion >= 17;
  }

  /**
   * PostgreSQL 从 12 版本开始支持 CTE 物化提示（MATERIALIZED/NOT MATERIALIZED）。
   * <p>
   * 在 PostgreSQL 12 之前，CTE 总是被物化（执行一次并缓存结果）。
   * PostgreSQL 12+ 引入了 MATERIALIZED 和 NOT MATERIALIZED 关键字来显式控制此行为：
   * <ul>
   *   <li><b>MATERIALIZED</b>: 强制物化 CTE，将结果缓存到临时表</li>
   *   <li><b>NOT MATERIALIZED</b>: 禁止物化，将 CTE 内联到主查询中进行优化</li>
   *   <li>不指定: 由优化器根据 CTE 的使用情况自动决定</li>
   * </ul>
   * <p>
   * 使用场景：
   * <ul>
   *   <li>MATERIALIZED: 当 CTE 被多次引用且计算成本高时</li>
   *   <li>NOT MATERIALIZED: 当 CTE 简单且只引用一次，希望优化器进行联合优化时</li>
   * </ul>
   * 
   * @return 版本 >= 12 时返回 true，否则返回 false
   * @since 2024-12-31
   */
  @Override
  public boolean supportsMaterializedCTE() {
    return majorVersion >= 12;
  }

  @Override
  public boolean isCompatible(DatabaseMetaData metaData) {
    try {
      String name = metaData.getDatabaseProductName().toLowerCase();
      return name.contains("postgresql");
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * PostgreSQL 二进制类型转换表达式。
   * <p>
   * PostgreSQL 需要显式将参数转换为 BYTEA 类型。
   * 
   * @return 二进制类型转换表达式: ?::bytea
   * @since 2026-01-04
   */
  @Override
  public String makeBinaryTypeCast() {
    return "?::bytea";
  }

  /**
   * 返回 JSON 操作方言实例。
   * <p>
   * 返回 PostgreSQL 专用的 JsonDialect 实现。
   * 
   * @return PostgreSQL JSON 方言实例
   * @since 2026-01-04
   */
  @Override
  public JsonDialect jsonDialect() {
    return PostgreSQLJsonDialect.getInstance();
  }

}
