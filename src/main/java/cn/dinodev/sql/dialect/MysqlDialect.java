// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

import java.sql.DatabaseMetaData;

import cn.dinodev.sql.utils.NamingUtils;

/**
 * MySQL 数据库方言实现。
 * <p>
 * 提供 MySQL 专用的 SQL 生成与命名转换逻辑。
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public class MysqlDialect implements Dialect {
  private static final String dialectName = "mysql";
  private final NamingConversition namingConversitionInstance;

  /**
   * 构造函数，创建MySQL数据库方言实例
   * @param metaData 数据库元数据
   * @param namingConversition 命名转换策略
   */
  public MysqlDialect(DatabaseMetaData metaData, NamingConversition namingConversition) {
    this.namingConversitionInstance = namingConversition;
  }

  @Override
  public NamingConversition namingConversition() {
    return namingConversitionInstance;
  }

  @Override
  public String getDialectName() {
    return dialectName;
  }

  @Override
  public String limitOffset(int limit, long offset) {
    if (limit > 0) {
      return offset > 0 ? "LIMIT " + limit + " OFFSET " + offset : "LIMIT " + limit;
    }
    return "";
  }

  @Override
  public String getSelectUUIDSql() {
    return "SELECT UUID()";
  }

  @Override
  public String quoteTableName(String name) {
    // MySQL表名、字段名建议用反引号包裹
    return NamingUtils.wrapIfMissing(name, '`');
  }

  @Override
  public String quoteColumnName(String columnName) {
    return NamingUtils.wrapIfMissing(columnName, '`');
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
}
