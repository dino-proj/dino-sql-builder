// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

import java.sql.DatabaseMetaData;

import cn.dinodev.sql.naming.NamingConversition;
import cn.dinodev.sql.utils.StringUtils;

/**
 * MySQL 数据库方言实现。
 * <p>
 * 提供 MySQL 专用的 SQL 生成与命名转换逻辑。
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public class MysqlDialect implements Dialect {
  /** 
   * 方言名称 
   */
  private static final String dialectName = "mysql";

  /** 
   * 命名转换器实例 
   */
  private final NamingConversition namingConversitionInstance;

  /** 
   * 数据库主版本号 
   */
  private final int majorVersion;

  /**
   * 构造函数，创建MySQL数据库方言实例
   * @param metaData 数据库元数据
   * @param namingConversition 命名转换策略
   */
  public MysqlDialect(DatabaseMetaData metaData, NamingConversition namingConversition) {
    this.namingConversitionInstance = namingConversition;
    int major = 0;
    try {
      major = metaData.getDatabaseMajorVersion();
    } catch (Exception e) {
      // 忽略异常，保持默认版本号0
    }
    this.majorVersion = major;
  }

  @Override
  public NamingConversition namingConversition() {
    return namingConversitionInstance;
  }

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

  @Override
  public String limitOffset(int limit, long offset) {
    if (limit > 0) {
      return offset > 0 ? "LIMIT " + limit + " OFFSET " + offset : "LIMIT " + limit;
    }
    return "";
  }

  @Override
  public String quoteTableName(String name) {
    // MySQL表名、字段名建议用反引号包裹
    return StringUtils.wrapIfMissing(name, '`');
  }

  @Override
  public String quoteColumnName(String columnName) {
    return StringUtils.wrapIfMissing(columnName, '`');
  }

  @Override
  public String getSequenceNextValSql(String sequenceName) {
    // MySQL 不支持 sequence
    throw new UnsupportedOperationException("MySQL does not support sequence.");
  }

  @Override
  public boolean supportSequence() {
    return false;
  }

  @Override
  public boolean supportUUID() {
    return true;
  }

  @Override
  public String getUuidFunction() {
    return "UUID()";
  }

  @Override
  public String getSelectUUIDSql() {
    return "SELECT UUID()";
  }

  @Override
  public String getCurrentSchemaSql() {
    // MySQL 当前 schema 可用 database()
    return "SELECT DATABASE()";
  }

  @Override
  public boolean isCompatible(DatabaseMetaData metaData) {
    try {
      String name = metaData.getDatabaseProductName().toLowerCase();
      return name.contains("mysql") || name.contains("mariadb");
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * MySQL 使用 REGEXP 操作符进行正则表达式匹配
   * <p>
   * 生成形如: column REGEXP ?
   * 
   * @param column 列名
   * @return 正则表达式匹配的 SQL 表达式
   */
  @Override
  public String makeRegexpExpr(String column) {
    return column + " REGEXP ?";
  }

  /**
   * MySQL 使用 NOT REGEXP 操作符进行正则表达式不匹配
   * <p>
   * 生成形如: column NOT REGEXP ?
   * 
   * @param column 列名
   * @return 正则表达式不匹配的 SQL 表达式
   */
  @Override
  public String makeNotRegexpExpr(String column) {
    return column + " NOT REGEXP ?";
  }

  /**
   * 返回 JSON 操作方言实例。
   * <p>
   * 返回 MySQL 专用的 JsonDialect 实现。
   * 
   * @return MySQL JSON 方言实例
   * @since 2026-01-04
   */
  @Override
  public JsonDialect jsonDialect() {
    return MysqlJsonDialect.getInstance();
  }
}
