// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Locale;

import cn.dinodev.sql.utils.NamingUtils;

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
   * 返回默认的 Dialect 实现
   * @return 默认方言实例
   */
  static Dialect ofDefault() {
    return Default.INST_DEFAULT;
  }

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

  /**
   * 默认数据库方言实现
   */
  class Default implements Dialect {

  private static final Default INST_DEFAULT = new Default();

  /**
   * 默认构造函数。
   * 创建默认数据库方言实现实例。
   */
  public Default() {
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
     * 获取方言名称
     */
    @Override
    public String getDialectName() {
      return "default";
    }

    /**
     * 字段名加引号（防止关键字冲突）
     */
    @Override
    public String quoteColumnName(String columnName) {
      return NamingUtils.wrapIfMissing(columnName, '"');
    }

    /**
     * 获取当前 schema 的 SQL，默认不支持
     */
    @Override
    public String getCurrentSchemaSql() {
      throw new UnsupportedOperationException();
    }

    /**
     * 检查兼容性，默认始终返回 true
     */
    @Override
    public boolean isCompatible(DatabaseMetaData metaData) {
      return true;
    }

    /**
     * 默认不支持 UUID 查询
     */
    @Override
    public String getSelectUUIDSql() {
      throw new UnsupportedOperationException();
    }

    /**
     * 默认不支持 sequence 查询
     */
    @Override
    public String getSequenceNextValSql(String sequenceName) {
      throw new UnsupportedOperationException();
    }

    /**
     * 默认不支持 sequence
     */
    @Override
    public boolean supportSequence() {
      return false;
    }

    /**
     * 表名加引号（防止关键字冲突）
     */
    @Override
    public String quoteTableName(String name) {
      return NamingUtils.wrapIfMissing(name, '"');
    }

    /**
     * 获取默认命名转换器
     */
    @Override
    public NamingConversition namingConversition() {
      return NamingConversition.Nop.INST;
    }

    /**
     * 默认不支持 UUID
     */
    @Override
    public boolean supportUUID() {
      return false;
    }

  }
}
