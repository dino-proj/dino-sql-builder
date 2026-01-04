// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import java.util.ArrayList;
import java.util.List;

import cn.dinodev.sql.SqlBuilder;
import cn.dinodev.sql.builder.clause.FromClause;
import cn.dinodev.sql.builder.clause.JoinClause;
import cn.dinodev.sql.builder.clause.UpdateSetClause;
import cn.dinodev.sql.builder.clause.WhereClause;
import cn.dinodev.sql.builder.clause.WithClause;
import cn.dinodev.sql.builder.clause.wheres.WhereClauseSupport;
import cn.dinodev.sql.dialect.Dialect;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.utils.SqlBuilderUtils;

/**
 * SQL UPDATE语句构建器。
 * <p>
 * 用于构建 UPDATE SQL 语句的 Builder 类，支持 SET、FROM、JOIN、WHERE 等子句。
 * <p>
 * <b>支持的SQL语法：</b>
 * <ul>
 *   <li><b>基础更新：</b>UPDATE table SET col=val WHERE condition</li>
 *   <li><b>关联更新（PostgreSQL）：</b>UPDATE t1 FROM t2 WHERE t1.id=t2.id</li>
 *   <li><b>JOIN更新（MySQL）：</b>UPDATE t1 JOIN t2 ON t1.id=t2.id SET ...</li>
 * </ul>
 *
 * @author Cody Lu
 * @since 2022-03-07
 */
public final class UpdateSqlBuilder implements SqlBuilder, WhereClause<UpdateSqlBuilder>, WithClause<UpdateSqlBuilder>,
    UpdateSetClause<UpdateSqlBuilder>, FromClause<UpdateSqlBuilder>, JoinClause<UpdateSqlBuilder> {

  private final Dialect dialect;
  private final List<String> tables = new ArrayList<>();

  private final UpdateSetClause.InnerUpdateSetHolder setHolder = new UpdateSetClause.InnerUpdateSetHolder();
  private final FromClause.InnerFromHolder fromHolder = new FromClause.InnerFromHolder();
  private final JoinClause.InnerJoinHolder joinHolder = new JoinClause.InnerJoinHolder();
  private final WhereClauseSupport.InnerWhereHolder whereHolder = new WhereClauseSupport.InnerWhereHolder();
  private final WithClause.InnerWithHolder withHolder = new WithClause.InnerWithHolder();

  /**
   * 私有构造函数，防止直接实例化
   * @param dialect 数据库方言实例
   */
  private UpdateSqlBuilder(final Dialect dialect) {
    this.dialect = dialect;
  }

  /**
   * 根据表名创建UPDATE语句构建器
   * <p>支持的格式：
   * <p>- <code>"table1"</code>
   * <p>- <code>"table1 as t1"</code>
   *
   * @param dialect 数据库方言实例
   * @param table 表名
   * @return 配置好的UpdateSqlBuilder实例
   */
  public static UpdateSqlBuilder create(final Dialect dialect, final String table) {
    UpdateSqlBuilder builder = new UpdateSqlBuilder(dialect);
    builder.tables.add(table);
    return builder;
  }

  /**
   * 根据表名和别名创建UPDATE语句构建器
   * <p>生成的sql片段为：table AS alias
   *
   * @param dialect 数据库方言实例
   * @param table 表名
   * @param alias 表别名
   * @return 配置好的UpdateSqlBuilder实例
   */
  public static UpdateSqlBuilder create(final Dialect dialect, final String table, final String alias) {
    UpdateSqlBuilder builder = new UpdateSqlBuilder(dialect);
    builder.tables.add(table + " AS " + alias);
    return builder;
  }

  /**
   * 创建多表UPDATE语句构建器（MySQL风格）。
   * <p>
   * 示例：
   * <pre>
   * UpdateSqlBuilder.createMultiTable(dialect, "orders o", "order_items oi")
   *     .set("o.status", "completed")
   *     .set("oi.shipped", true)
   *     .where("o.id = oi.order_id");
   * // 生成：UPDATE orders o, order_items oi SET o.status = ?, oi.shipped = ? WHERE o.id = oi.order_id
   * </pre>
   * <p>
   * <b>注意：</b>多表UPDATE主要用于MySQL，PostgreSQL应使用FROM子句。
   *
   * @param dialect 数据库方言实例
   * @param tables 表名数组（可包含别名）
   * @return 配置好的UpdateSqlBuilder实例
   */
  public static UpdateSqlBuilder createMultiTable(final Dialect dialect, final String... tables) {
    UpdateSqlBuilder builder = new UpdateSqlBuilder(dialect);
    if (tables != null && tables.length > 0) {
      for (String table : tables) {
        builder.tables.add(table);
      }
    }
    return builder;
  }

  /**
   * 添加额外的表到UPDATE子句（用于多表UPDATE）。
   * <p>
   * 示例：
   * <pre>
   * UpdateSqlBuilder.create(dialect, "orders", "o")
   *     .addTable("order_items", "oi")
   *     .set("o.status", "completed")
   *     .where("o.id = oi.order_id");
   * </pre>
   *
   * @param table 表名
   * @return 构建器本身
   */
  public UpdateSqlBuilder addTable(final String table) {
    this.tables.add(table);
    return this;
  }

  /**
   * 添加带别名的表到UPDATE子句（用于多表UPDATE）。
   *
   * @param table 表名
   * @param alias 表别名
   * @return 构建器本身
   */
  public UpdateSqlBuilder addTable(final String table, final String alias) {
    this.tables.add(table + " AS " + alias);
    return this;
  }

  // ==================== ClauseSupport 实现 ====================

  @Override
  public UpdateSqlBuilder self() {
    return this;
  }

  @Override
  public Dialect dialect() {
    return dialect;
  }

  // ==================== UpdateSetClause 实现 ====================

  @Override
  public UpdateSetClause.InnerUpdateSetHolder innerUpdateSetHolder() {
    return setHolder;
  }

  // ==================== FromClause 实现 ====================

  @Override
  public FromClause.InnerFromHolder innerFromHolder() {
    return fromHolder;
  }

  // ==================== JoinClause 实现 ====================

  @Override
  public JoinClause.InnerJoinHolder innerJoinHolder() {
    return joinHolder;
  }

  // ==================== WhereClauseSupport 实现 ====================

  @Override
  public WhereClauseSupport.InnerWhereHolder innerWhereHolder() {
    return whereHolder;
  }

  // ==================== WithClause 实现 ====================

  @Override
  public InnerWithHolder innerWithHolder() {
    return withHolder;
  }

  // ==================== SqlBuilder 实现 ====================

  @Override
  public String getSql() {
    final StringBuilder sql = new StringBuilder(64);

    // WITH 子句
    withHolder.appendSql(sql);

    // UPDATE 子句
    sql.append("UPDATE ");
    SqlBuilderUtils.appendList(sql, tables, "", ", ");

    // JOIN 子句（MySQL风格：UPDATE table JOIN ...）
    // MySQL支持在UPDATE和SET之间使用JOIN
    if (!joinHolder.isEmpty() && dialect instanceof MysqlDialect) {
      joinHolder.appendSql(sql);
    }

    // SET 子句
    setHolder.appendSql(sql);

    // FROM 子句（PostgreSQL风格：UPDATE table SET ... FROM ...）
    // PostgreSQL支持在SET之后使用FROM
    if (!fromHolder.isEmpty() && !(dialect instanceof MysqlDialect)) {
      fromHolder.appendSql(sql);
    }

    // 如果是PostgreSQL且有JOIN，需要将JOIN转换为FROM或报错
    if (!joinHolder.isEmpty() && !(dialect instanceof MysqlDialect)) {
      throw new UnsupportedOperationException(
          "PostgreSQL does not support JOIN in UPDATE statements. Use FROM clause instead.");
    }

    // WHERE 子句
    whereHolder.appendSql(sql);

    return sql.toString();
  }

  @Override
  public Object[] getParams() {
    final List<Object> allParams = new ArrayList<>();

    // WITH 参数
    withHolder.appendParams(allParams);

    // JOIN 参数（MySQL风格，在SET之前）
    if (dialect instanceof MysqlDialect) {
      joinHolder.appendParams(allParams);
    }

    // SET 参数
    setHolder.appendParams(allParams);

    // FROM 参数（PostgreSQL风格，在SET之后）
    if (!(dialect instanceof MysqlDialect)) {
      fromHolder.appendParams(allParams);
    }

    // WHERE 参数
    whereHolder.appendParams(allParams);

    return allParams.toArray();
  }

  @Override
  public String toString() {
    return this.getSql();
  }

  @Override
  public int getParamCount() {
    int count = 0;

    // WITH 参数
    count += withHolder.getParamsCount();

    // FROM 参数
    count += fromHolder.getParamsCount();

    // JOIN 参数
    count += joinHolder.getParamsCount();

    // SET 参数
    count += setHolder.getParamsCount();

    // WHERE 参数
    count += whereHolder.getParamsCount();

    return count;
  }

}
