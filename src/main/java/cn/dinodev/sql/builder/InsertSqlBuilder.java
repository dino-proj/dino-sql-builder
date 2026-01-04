// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import java.util.ArrayList;
import java.util.List;

import cn.dinodev.sql.SqlBuilder;
import cn.dinodev.sql.builder.clause.InsertSetClause;
import cn.dinodev.sql.builder.clause.WithClause;
import cn.dinodev.sql.dialect.Dialect;

/**
 * SQL INSERT语句构建器。
 * <p>
 * 用于构建 INSERT SQL 语句的 Builder 类，支持指定列和值的插入操作。
 * <p>
 * <b>支持的SQL语法：</b>
 * <ul>
 *   <li><b>基础插入：</b>INSERT INTO table (col1, col2) VALUES (?, ?)</li>
 *   <li><b>WITH子句：</b>WITH cte AS (SELECT ...) INSERT INTO table ...</li>
 *   <li><b>表达式插入：</b>INSERT INTO table (created_at) VALUES (NOW())</li>
 * </ul>
 * <p>
 * 示例：
 * <pre>{@code
 * // 基础插入
 * InsertSqlBuilder.create(dialect, "users")
 *     .set("name", "张三")
 *     .set("age", 25)
 *     .setNow("created_at");
 * 
 * // WITH子句插入
 * InsertSqlBuilder.create(dialect, "order_archive")
 *     .with("old_orders", selectBuilder)
 *     .set("order_id", subQuery)
 *     .set("archived_at", "NOW()");
 * }</pre>
 *
 * @author Cody Lu
 * @since 2022-03-07
 */
public final class InsertSqlBuilder
    implements SqlBuilder, InsertSetClause<InsertSqlBuilder>, WithClause<InsertSqlBuilder> {

  private final Dialect dialect;
  private final String table;

  private final InsertSetClause.InnerInsertSetHolder setHolder = new InsertSetClause.InnerInsertSetHolder();
  private final WithClause.InnerWithHolder withHolder = new WithClause.InnerWithHolder();

  /**
   * 私有构造函数，防止直接实例化。
   *
   * @param dialect 数据库方言实例
   * @param table 表名
   */
  private InsertSqlBuilder(final Dialect dialect, final String table) {
    this.dialect = dialect;
    this.table = table;
  }

  /**
   * 根据表名创建INSERT语句构建器。
   * <p>
   * 示例：
   * <pre>{@code
   * InsertSqlBuilder.create(dialect, "users")
   *     .set("name", "张三")
   *     .set("email", "zhangsan@example.com");
   * }</pre>
   *
   * @param dialect 数据库方言实例
   * @param table 表名
   * @return 配置好的InsertSqlBuilder实例
   */
  public static InsertSqlBuilder create(final Dialect dialect, final String table) {
    return new InsertSqlBuilder(dialect, table);
  }

  // ==================== ClauseSupport 实现 ====================

  @Override
  public InsertSqlBuilder self() {
    return this;
  }

  @Override
  public Dialect dialect() {
    return dialect;
  }

  // ==================== InsertClause 实现 ====================

  @Override
  public InsertSetClause.InnerInsertSetHolder innerInsertSetHolder() {
    return setHolder;
  }

  // ==================== WithClause 实现 ====================

  @Override
  public WithClause.InnerWithHolder innerWithHolder() {
    return withHolder;
  }

  // ==================== SqlBuilder 实现 ====================

  @Override
  public String getSql() {
    final StringBuilder sql = new StringBuilder(64);

    // WITH 子句
    withHolder.appendSql(sql);

    // INSERT INTO 子句
    sql.append("INSERT INTO ").append(table);

    // 列名和值
    setHolder.appendSql(sql);

    return sql.toString();
  }

  @Override
  public Object[] getParams() {
    final List<Object> allParams = new ArrayList<>();

    // WITH 参数
    withHolder.appendParams(allParams);

    // INSERT 参数
    setHolder.appendParams(allParams);

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

    // INSERT 参数
    count += setHolder.getParamsCount();

    return count;
  }
}
