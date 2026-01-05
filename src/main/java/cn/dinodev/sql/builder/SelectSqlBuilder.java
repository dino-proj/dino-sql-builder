// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.dinodev.sql.SqlBuilder;
import cn.dinodev.sql.builder.clause.FromClause;
import cn.dinodev.sql.builder.clause.GroupByClause;
import cn.dinodev.sql.builder.clause.HavingClause;
import cn.dinodev.sql.builder.clause.JoinClause;
import cn.dinodev.sql.builder.clause.LimitOffsetClause;
import cn.dinodev.sql.builder.clause.OrderByClause;
import cn.dinodev.sql.builder.clause.SelectClause;
import cn.dinodev.sql.builder.clause.UnionClause;
import cn.dinodev.sql.builder.clause.WhereClause;
import cn.dinodev.sql.builder.clause.WithClause;
import cn.dinodev.sql.builder.clause.wheres.WhereClauseSupport;
import cn.dinodev.sql.dialect.Dialect;

/**
 * SQL SELECT 语句构建器 V2 版本。
 * <p>
 * 基于 clause 接口实现的新版本 SELECT 构建器，提供更模块化、更易扩展的设计。
 * 实现了所有 SELECT 相关的 clause 接口，支持完整的 SQL SELECT 功能。
 * <br>
 * <b>使用示例：</b>
 * <pre>
 * SelectSqlBuilderV2 builder = SelectSqlBuilderV2.create(dialect, "user")
 *     .columns("id", "name", "age")
 *     .eq("status", 1)
 *     .gt("age", 18)
 *     .orderBy("age", false)
 *     .limit(10);
 * String sql = builder.getSql();
 * Object[] params = builder.getParams();
 * </pre>
 *
 * @author Cody Lu
 * @since 2024-12-04
 */
public final class SelectSqlBuilder implements SqlBuilder,
    SelectClause<SelectSqlBuilder>,
    FromClause<SelectSqlBuilder>,
    JoinClause<SelectSqlBuilder>,
    WhereClause<SelectSqlBuilder>,
    GroupByClause<SelectSqlBuilder>,
    HavingClause<SelectSqlBuilder>,
    OrderByClause<SelectSqlBuilder>,
    LimitOffsetClause<SelectSqlBuilder>,
    UnionClause<SelectSqlBuilder>,
    WithClause<SelectSqlBuilder> {

  private final Dialect dialect;
  private final FromClause.InnerFromHolder fromHolder = new FromClause.InnerFromHolder();
  private final SelectClause.InnerSelectHolder selectHolder = new SelectClause.InnerSelectHolder();
  private final JoinClause.InnerJoinHolder joinHolder = new JoinClause.InnerJoinHolder();
  private final WhereClauseSupport.InnerWhereHolder whereHolder = new WhereClauseSupport.InnerWhereHolder();
  private final GroupByClause.InnerGroupByHolder groupByHolder = new GroupByClause.InnerGroupByHolder();
  private final HavingClause.InnerHavingHolder havingHolder = new HavingClause.InnerHavingHolder();
  private final OrderByClause.InnerOrderByHolder orderByHolder = new OrderByClause.InnerOrderByHolder();
  private final UnionClause.InnerUnionHolder unionHolder = new UnionClause.InnerUnionHolder();
  private final WithClause.InnerWithHolder withHolder = new WithClause.InnerWithHolder();
  private final LimitOffsetClause.InnerLimitOffsetHolder limitOffsetHolder = new LimitOffsetClause.InnerLimitOffsetHolder();

  /**
   * 私有构造函数，防止直接实例化。
   *
   * @param dialect 数据库方言实例
   */
  private SelectSqlBuilder(final Dialect dialect) {
    this.dialect = dialect;
  }

  /**
   * 私有构造函数，防止直接实例化。
   *
   * @param dialect 数据库方言实例
   * @param table 表名
   */
  private SelectSqlBuilder(final Dialect dialect, final String table) {
    this.dialect = dialect;
    fromHolder.addTable(table, null);
  }

  /**
   * 私有构造函数，防止直接实例化。
   *
   * @param dialect 数据库方言实例
   * @param table 表名
   * @param alias 表别名
   */
  private SelectSqlBuilder(final Dialect dialect, final String table, final String alias) {
    this.dialect = dialect;
    fromHolder.addTable(table, alias);
  }

  /**
   * 私有构造函数，防止直接实例化。
   *
   * @param subQuery 子查询 SelectSqlBuilderV2 实例
   * @param alias 子查询别名
   */
  private SelectSqlBuilder(final SelectSqlBuilder subQuery, final String alias) {
    this.dialect = subQuery.dialect;
    fromHolder.addSubQuery(subQuery, alias);
  }

  /**
   * 创建 SelectSqlBuilderV2 实例。
   *
   * @param dialect 数据库方言实例
   * @return 配置好的 SelectSqlBuilderV2 实例
   */
  public static SelectSqlBuilder create(final Dialect dialect) {
    return new SelectSqlBuilder(dialect);
  }

  /**
   * 根据表名创建 SelectSqlBuilderV2 实例。
   *
   * @param dialect 数据库方言实例
   * @param table 表名
   * @return SelectSqlBuilderV2 实例
   */
  public static SelectSqlBuilder create(final Dialect dialect, final String table) {
    return new SelectSqlBuilder(dialect, table);
  }

  /**
   * 根据表名和别名创建 SelectSqlBuilderV2 实例。
   *
   * @param dialect 数据库方言实例
   * @param table 表名
   * @param alias 表别名
   * @return SelectSqlBuilderV2 实例
   */
  public static SelectSqlBuilder create(final Dialect dialect, final String table, final String alias) {
    return new SelectSqlBuilder(dialect, table, alias);
  }

  /**
   * 根据子查询创建 SelectSqlBuilderV2 实例。
   *
   * @param subQuery 子查询 SelectSqlBuilderV2 实例
   * @param alias 子查询的别名
   * @return SelectSqlBuilderV2 实例
   */
  public static SelectSqlBuilder create(final SelectSqlBuilder subQuery, final String alias) {
    return new SelectSqlBuilder(subQuery, alias);
  }

  // ==================== ClauseSupport 实现 ====================

  @Override
  public SelectSqlBuilder self() {
    return this;
  }

  @Override
  public Dialect dialect() {
    return dialect;
  }

  // ==================== SelectClause 实现 ====================

  @Override
  public SelectClause.InnerSelectHolder innerSelectHolder() {
    return selectHolder;
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

  // ==================== GroupByClause 实现 ====================

  @Override
  public GroupByClause.InnerGroupByHolder innerGroupByHolder() {
    return groupByHolder;
  }

  // ==================== HavingClause 实现 ====================

  @Override
  public HavingClause.InnerHavingHolder innerHavingHolder() {
    return havingHolder;
  }

  // ==================== OrderByClause 实现 ====================

  @Override
  public OrderByClause.InnerOrderByHolder innerOrderByHolder() {
    return orderByHolder;
  }

  // ==================== LimitOffsetClause 实现 ====================

  @Override
  public LimitOffsetClause.InnerLimitOffsetHolder innerLimitOffsetHolder() {
    return limitOffsetHolder;
  }

  // ==================== UnionClause 实现 ====================

  @Override
  public UnionClause.InnerUnionHolder innerUnionHolder() {
    return unionHolder;
  }

  // ==================== WithClause 实现 ====================

  @Override
  public InnerWithHolder innerWithHolder() {
    return withHolder;
  }

  // ==================== SqlBuilder 实现 ====================

  @Override
  public String getSql() {
    return getSql(false);
  }

  /**
   * 获取用于计数的 SQL 语句。
   *
   * @return 计数 SQL 语句字符串
   */
  public String getCountSql() {
    return getSql(true);
  }

  private String getSql(final boolean isCount) {
    final StringBuilder sql = new StringBuilder(64);

    // WITH 子句
    withHolder.appendSql(sql);

    // SELECT 子句
    selectHolder.appendSql(sql, isCount);

    // FROM 子句
    fromHolder.appendSql(sql);

    // JOIN 子句
    joinHolder.appendSql(sql);

    // WHERE 子句
    whereHolder.appendSql(sql);

    // GROUP BY 子句
    groupByHolder.appendSql(sql);

    // HAVING 子句
    havingHolder.appendSql(sql);

    // UNION 子句
    unionHolder.appendSql(sql);

    // 如果是计数查询，不需要 ORDER BY 和 LIMIT
    if (isCount) {
      return sql.toString();
    }

    // ORDER BY 子句
    orderByHolder.appendSql(sql);

    // LIMIT/OFFSET 子句
    limitOffsetHolder.appendSql(sql, dialect);

    return sql.toString();
  }

  @Override
  public Object[] getParams() {
    List<Object> allParams = new ArrayList<>();

    // WITH 参数
    if (!withHolder.isEmpty()) {
      withHolder.appendParams(allParams);
    }

    // FROM 参数
    fromHolder.appendParams(allParams);

    // JOIN 参数
    joinHolder.appendParams(allParams);

    // WHERE 参数
    whereHolder.appendParams(allParams);

    // HAVING 参数
    for (HavingClause.InnerHavingHolder.HavingCondition condition : havingHolder.getConditions()) {
      if (condition.getParams() != null) {
        allParams.addAll(Arrays.asList(condition.getParams()));
      }
    }

    // UNION 参数
    unionHolder.appendParams(allParams);

    return allParams.toArray();
  }

  @Override
  public String toString() {
    return getSql();
  }

  @Override
  public int getParamCount() {
    int count = 0;

    // WITH 参数
    if (!withHolder.isEmpty()) {
      count += withHolder.getParamsCount();
    }

    // FROM 参数
    count += fromHolder.getParamsCount();

    // JOIN 参数
    count += joinHolder.getParamsCount();

    // WHERE 参数
    count += whereHolder.getParamsCount();

    // HAVING 参数
    for (HavingClause.InnerHavingHolder.HavingCondition condition : havingHolder.getConditions()) {
      if (condition.getParams() != null) {
        count += condition.getParams().length;
      }
    }

    // UNION 参数
    count += unionHolder.getParamsCount();

    return count;
  }
}
