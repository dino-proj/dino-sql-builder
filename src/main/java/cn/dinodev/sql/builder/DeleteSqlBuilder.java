// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import java.util.ArrayList;
import java.util.List;

import cn.dinodev.sql.SqlBuilder;
import cn.dinodev.sql.builder.clause.WhereClause;
import cn.dinodev.sql.builder.clause.WithClause;
import cn.dinodev.sql.builder.clause.wheres.WhereClauseSupport;
import cn.dinodev.sql.dialect.Dialect;

/**
 * SQL DELETE语句构建器。
 * <p>
 * 用于构建 DELETE SQL 语句的 Builder 类，支持 WHERE 条件等子句。
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public final class DeleteSqlBuilder implements SqlBuilder, WhereClause<DeleteSqlBuilder>, WithClause<DeleteSqlBuilder> {

  private final Dialect dialect;
  private final List<String> tables = new ArrayList<>();

  private final WhereClauseSupport.InnerWhereHolder whereHolder = new WhereClauseSupport.InnerWhereHolder();
  private final WithClause.InnerWithHolder withHolder = new WithClause.InnerWithHolder();

  /**
   * 根据表名创建DeleteSqlBuilder实例, 如下写法都是合法的：
   * <p>- <code>"table1"</code>
   * <p>- <code>"table1 as t1"</code>
   *
   * @param table 表名
   * @return DeleteSqlBuilder实例
   */
  public static DeleteSqlBuilder create(final Dialect dialect, final String table) {
    final DeleteSqlBuilder builder = new DeleteSqlBuilder(dialect);
    builder.tables.add(table);
    return builder;
  }

  /**
   * 创建 DeleteSqlBuilder 实例并设置表名和别名。
   * <p>生成的 SQL 片段为：table AS alias。
   *
   * @param table 表名
   * @param alias 表别名
   * @return DeleteSqlBuilder 实例
   */
  public static DeleteSqlBuilder create(final Dialect dialect, final String table, final String alias) {
    final DeleteSqlBuilder builder = new DeleteSqlBuilder(dialect);
    builder.tables.add(table + " AS " + alias);
    return builder;
  }

  /**
   * 私有构造函数，防止直接实例化。
   */
  private DeleteSqlBuilder(final Dialect dialect) {
    this.dialect = dialect;
  }

  // ==================== ClauseSupport 实现 ====================

  @Override
  public DeleteSqlBuilder self() {
    return this;
  }

  @Override
  public Dialect dialect() {
    return dialect;
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

  /**
   * 构建最终的 DELETE SQL 语句。
   *
   * @return 构建好的 DELETE SQL 语句字符串
   */
  @Override
  public String getSql() {
    final StringBuilder sql = new StringBuilder(128);

    // WITH 子句
    withHolder.appendSql(sql);

    // DELETE FROM 子句
    sql.append("DELETE FROM ");
    SqlBuilderUtils.appendList(sql, tables, "", ", ");

    // WHERE 子句
    whereHolder.appendSql(sql);

    return sql.toString();
  }

  /**
   * 获取所有参数值数组。
   *
   * @return 参数值数组
   */
  @Override
  public Object[] getParams() {
    final List<Object> allParams = new ArrayList<>();

    // WITH 参数
    withHolder.appendParams(allParams);

    // WHERE 参数
    whereHolder.appendParams(allParams);

    return allParams.toArray();
  }

  /**
   * 返回当前构建的 SQL 字符串。
   *
   * @return 构建好的 SQL 字符串
   */
  @Override
  public String toString() {
    return this.getSql();
  }

  @Override
  public int getParamCount() {
    int count = 0;

    // WITH 参数
    count += withHolder.getParamsCount();

    // WHERE 参数
    count += whereHolder.getParamsCount();

    return count;
  }

}
