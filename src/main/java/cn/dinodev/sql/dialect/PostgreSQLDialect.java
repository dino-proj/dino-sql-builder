// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import cn.dinodev.sql.utils.NamingUtils;

/**
 * PostgreSQL 数据库方言实现
 *
 * @author Cody Lu
 * @date 2022-03-07 19:15:17
 */

public class PostgreSQLDialect implements Dialect {
  private static final String dialectName = "postgresql";
  private final NamingConversition namingConversitionInstance;
  private final String uuidSql;

  /**
   * 构造函数，创建 PostgreSQL 数据库方言实例
   * @param metaData 数据库元数据
   * @param namingConversition 命名转换策略
   * @throws SQLException 获取数据库主版本号异常
   */
  public PostgreSQLDialect(DatabaseMetaData metaData, NamingConversition namingConversition) throws SQLException {
    this.namingConversitionInstance = namingConversition;
    var majorVer = metaData.getDatabaseMajorVersion();
    if (majorVer >= 13) {
      uuidSql = "SELECT gen_random_uuid()";
    } else {
      uuidSql = "SELECT uuid_generate_v4()";
    }

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
   * 生成查询 UUID 的 SQL 语句
   * @return 查询 UUID 的 SQL 语句
   */
  @Override
  public String getSelectUUIDSql() {
    return uuidSql;
  }

  /**
   * 表名加引号（防止关键字冲突）
   * @param name 表名
   * @return 加引号后的表名
   */
  @Override
  public String quoteTableName(String name) {
    return NamingUtils.wrapIfMissing(name, '"');
  }

  /**
   * 字段名加引号（防止关键字冲突）
   * @param columnName 字段名
   * @return 加引号后的字段名
   */
  @Override
  public String quoteColumnName(String columnName) {
    return NamingUtils.wrapIfMissing(columnName, '"');
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
  @Override
  public boolean isCompatible(DatabaseMetaData metaData) {
    try {
      String name = metaData.getDatabaseProductName().toLowerCase();
      return name.contains("postgresql");
    } catch (Exception e) {
      return false;
    }
  }

}
