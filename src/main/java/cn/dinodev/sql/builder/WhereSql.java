// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import cn.dinodev.sql.Logic;
import cn.dinodev.sql.Oper;
import cn.dinodev.sql.Range;
import cn.dinodev.sql.SqlBuilder;
import cn.dinodev.sql.utils.NamingUtils;

/**
 * WhereSql 用于构建 SQL 的 WHERE 片段，支持链式动态拼接条件。
 * <p>常用方法包括：where、and、or、in、like、between 等。
 * <p>支持条件判断、空值处理、区间、集合等多种 SQL 表达式。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2022-03-07
 */

public abstract class WhereSql<T extends SqlBuilder> implements SqlBuilder {

  private static final String PMD_LINGUISTIC_NAMING = "PMD.LinguisticNaming";

  /**
   * 默认构造函数。
   * 用于创建 WhereSql 实例。
   */
  public WhereSql() {
  }

  /**
   * 空参数数组
   */
  protected static final Object[] EMPTY_PARAMS = new Object[0];

  /**
   * 表名列表
   */
  protected List<String> tables = new ArrayList<>();

  /**
   * where 表达式列表
   */
  protected List<String> whereColumns = new ArrayList<>();

  /**
   * where 参数列表
   */
  protected List<Object> whereParams = new ArrayList<>();

  /**
   * with 语句子查询
   */
  protected SqlBuilder withSql;

  /**
   * with 语句别名
   */
  protected String withName;

  private T that;

  /**
   * 设置 that 引用
   * @param that 构造器
   */
  protected void setThat(T that) {
    this.that = that;
  }

  /**
   * 设置要查询的表，可以是多个表，如下用法都是正确的：
   * <p>- <code>from("table1, table2"); </code>
   * <p>- <code>from("table1", "table2");</code>
   *
   * @param tables 表名数组
   * @return 当前构建器实例
   */
  public T table(final String... tables) {
    if (tables != null) {
      this.tables.addAll(Arrays.asList(tables));
    }
    return that;
  }

  /**
   * 支持 SQL WITH 语句，并将子查询自动添加到 FROM 中：
   * <p>- <code>with #alias AS ( #subQuery语句 ); select columns from #alias</code>
   *
   * @param subQuery 子查询构建器
   * @param alias 临时查询的别名
   * @return 构建器本身
   */
  public T with(final SqlBuilder subQuery, final String alias) {
    this.withName = alias;
    this.withSql = subQuery;
    this.table(alias);
    return that;
  }

  /**
   * 添加 where 表达式，如下写法都是合法的：
   * <p>- <code>where("status = 1")</code>
   * <p>- <code>where("status = 1 and id = ?")</code>
   *
   * @param expr SQL 条件表达式
   * @return 构建器本身
   */
  public T where(final String expr) {
    return and(expr);
  }

  /**
   * 添加带参数值的 where 表达式：
   * <p>- <code>where("id = ?", id)</code>
   * <p>- <code>where("status = 1 and classId = ? and score > ?", classId, 60)</code>
   * <p>in 表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like 表达式请使用 {@link #like(String, String)}
   *
   * @param expr SQL 条件表达式
   * @param values 参数值（可变参数）
   * @return 构建器本身
   */
  public T where(final String expr, final Object... values) {
    appendWhere(Logic.AND, expr);
    whereParams.addAll(Arrays.asList(values));
    return that;
  }

  /**
   * 添加 where 表达式，例如：
   * <p>- <code>where("col1", Oper.EQ, val)</code>
   * <p>in 表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like 表达式请使用 {@link #like(String, String)}
   *
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  public T where(final String column, final Oper op, final Object value) {
    where(op.makeExpr(column));
    whereParams.add(value);
    return that;
  }

  /**
   * where 表达式，判断值不为null时，表达式才被采用，否则表达式会被丢弃：
   * <p>- <code>whereIfNotNull("type = ?", null);</code> 则会忽略这个表达式，不会根据type字段筛选。
   * <p>- <code>whereIfNotNull("type = ?", 1);</code>
   * <p>in表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like表达式请使用 {@link #like(String, String)}
   *
   * @param expr SQL 条件表达式
   * @param value 参数值
   * @return 构建器本身
   */
  public T whereIfNotNull(final String expr, final Object value) {
    if (value == null) {
      return that;
    }

    where(expr, value);
    return that;
  }

  /**
   * where 表达式，判断值不为 null 时才添加，否则忽略：
   * <p>- <code>whereIfNotNull("type", Oper.EQ, null);</code> 则忽略该表达式。
   * <p>- <code>whereIfNotNull("type", Oper.EQ, 1);</code>
   * <p>in 表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like 表达式请使用 {@link #like(String, String)}
   *
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  public T whereIfNotNull(final String column, final Oper op, final Object value) {
    if (!Objects.isNull(value)) {
      where(column, op, value);
    }
    return that;
  }

  /**
   * 根据条件决定是否添加 where 表达式。
   * <p>- <code>whereIf(false, "type=?", 1);</code> 则会忽略该表达式。
   * <p>- <code>whereIf(true, "type=? or type=?", 1, 2);</code> type=1或2的记录会被筛选。
   * <p>in 表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like 表达式请使用 {@link #like(String, String)}
   *
   * @param cnd 条件（true 时添加表达式）
   * @param expr SQL 条件表达式
   * @param values 参数值（可变参数）
   * @return 构建器本身
   */
  public T whereIf(final boolean cnd, final String expr, final Object... values) {
    if (!cnd) {
      return that;
    }
    return where(expr, values);
  }

  /**
   * 根据条件决定是否添加 where 表达式。
   * <p>- <code>whereIf(false, "type", Oper.EQ, 1);</code> 则忽略该表达式。
   * <p>- <code>whereIf(true, "type", Oper.EQ, 1);</code> type=1的记录会被筛选。
   * <p>in 表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like 表达式请使用 {@link #like(String, String)}
   *
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  public T whereIf(final boolean cnd, final String column, final Oper op, final Object value) {
    if (!cnd) {
      return that;
    }

    return where(column, op, value);
  }

  /**
   * where 表达式，用 AND 连接，如下写法都是合法的：
   * <p>- <code>and("status = 1")</code>
   * <p>- <code>and("status = 1 and id = ?")</code>
   * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
   * @param expr SQL 条件表达式
   * @return 构建器本身
   */
  public T and(final String expr) {
    appendWhere(Logic.AND, expr);
    return that;
  }

  /**
   * 添加带参数值的 AND 连接 where 表达式：
   * <p>- <code>and("id = ?", id)</code>
   * <p>- <code>and("status = 1 and classId = ? and score > ?", classId, 60)</code>
   * <p>in 表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like 表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
   * @param expr SQL 条件表达式
   * @param values 参数值（可变参数）
   * @return 构建器本身
   */
  public T and(final String expr, final Object... values) {
    and(expr);
    whereParams.addAll(Arrays.asList(values));
    return that;
  }

  /**
   * 添加 AND 连接的 where 表达式，例如：
   * <p>- <code>and("col1", Oper.EQ, val)</code>
   * <p>in 表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like 表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  public T and(final String column, final Oper op, final Object value) {
    return and(String.format("%s %s ?", column, op.getOp()), value);
  }

  /**
   * where 表达式，用 AND 连接，根据传入条件，当为false时，则忽略此查询条件
   * <p>- <code>andIf(false, "type=?", 1); 则会忽略这个表达式，不会根据type字段筛选。</code>
   * <p>- <code>andIf(true, "type=? or type=?", 1, 2); type= 1||2的记录会被筛选出来。</code>
   * <p>in表达式请使用 {@link #in(String, Collection, Logic)}
   * <p>like表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
   * @param cnd 判断条件
   * @param expr SQL 条件表达式
   * @param values 参数值
   * @return 构建器本身
   */
  public T andIf(final boolean cnd, final String expr, final Object... values) {
    if (!cnd) {
      return that;
    }

    return and(expr, values);
  }

  /**
   * where 表达式，用 AND 连接，根据传入条件，当为false时，则忽略此查询条件
   * <p>- <code>andIf(false, "type", Oper.EQ, 1); 则会忽略这个表达式，不会根据type字段筛选。</code>
   * <p>- <code>andIf(true, "type", Oper.EQ, 1); type= 1的记录会被筛选出来。</code>
   * <p>in表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
   * @param cnd 判断条件，当为 false 时忽略该表达式
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  public T andIf(final boolean cnd, final String column, final Oper op, final Object value) {
    if (!cnd) {
      return that;
    }

    return and(column, op, value);
  }

  /**
   * AND 连接的 where 表达式，值不为 null 时才添加。
   * <p>- <code>andIfNotNull("type = ?", null);</code> 则忽略该表达式。
   * <p>- <code>andIfNotNull("type = ?", 1);</code>
   * <p>in 表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like 表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
   * @param expr SQL 条件表达式
   * @param value 参数值
   * @return 构建器本身
   */

  public T andIfNotNull(final String expr, final Object value) {
    return andIf(!Objects.isNull(value), expr, value);
  }

  /**
   * where 表达式，用 AND 连接，判断值不为null时，表达式才被采用，否则表达式会被丢弃：
   * <p>- <code>andIfNotNull("type", Oper.EQ, null); 则会忽略这个表达式，不会根据type字段筛选。</code>
   * <p>- <code>andIfNotNull("type", Oper.EQ, 1);</code>
   * <p>in表达式请使用 {@link #in(String, Collection)}
   * <p>like表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  public T andIfNotNull(final String column, final Oper op, final Object value) {
    return andIf(!Objects.isNull(value), column, op, value);
  }

  /**
   * 添加等值（=）条件，AND 连接，例如：
   * <p>- <code>eq("col1", val)</code>
   * <p>in 表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like 表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  public T eq(final String column, final Object value) {
    return and(String.format("%s = ?", column), value);
  }

  /**
   * 根据条件决定是否添加等值（=）AND 表达式。
   * <p>- <code>eqIf(false, "type", 1);</code> 则忽略该表达式。
   * <p>- <code>eqIf(true, "type", 1);</code> type=1的记录会被筛选。
   * <p>in 表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like 表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  public T eqIf(final boolean cnd, final String column, final Object value) {
    return andIf(cnd, column, Oper.EQ, value);
  }

  /**
   * where eq 表达式，用 AND 连接，判断值不为null时，表达式才被采用，否则表达式会被丢弃：
  * <p>- <code>eqIfNotNull("type", null); 则会忽略这个表达式，不会根据type字段筛选。</code>
  * <p>- <code>eqIfNotNull("type", 1);</code>
  * <p>in表达式请使用 {@link #in(String, java.util.Collection)}
  * <p>like表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
  * @param column 字段名
  * @param value eq 参数值
  * @return 当前构建器实例
  */
  public T eqIfNotNull(final String column, final Object value) {
    return andIf(!Objects.isNull(value), column, Oper.EQ, value);
  }

  /**
   * where eq 表达式，用 AND 连接，判断值不为null时，表达式才被采用，否则表达式会被丢弃：
  * <p>- <code>eqIfNotBlank("type", null); 则会忽略这个表达式，不会根据type字段筛选。</code>
  * <p>- <code>eqIfNotBlank("type", "1");</code>
  * <p>in表达式请使用 {@link #in(String, java.util.Collection)}
  * <p>like表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
  * @param column 字段名（列名），用于指定 SQL 条件的目标列
  * @param value 匹配的内容字符串，为空时忽略该条件
  * @return 构建后的 SQL 片段对象（支持链式调用）
   */
  public T eqIfNotBlank(final String column, final String value) {
    return andIf(NamingUtils.isNotBlank(value), column, Oper.EQ, value);
  }

  /**
   * 添加不等值（!=）条件，AND 连接，例如：
   * <p>- <code>ne("col1", val)</code>
   * <p>in 表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like 表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  public T ne(final String column, final Object value) {
    return and(String.format("%s != ?", column), value);
  }

  /**
   * 根据条件决定是否添加不等值（!=）AND 表达式。
   * <p>- <code>neIf(false, "type", 1);</code> 则忽略该表达式。
   * <p>- <code>neIf(true, "type", 1);</code> type!=1的记录会被筛选。
   * <p>in 表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like 表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  public T neIf(final boolean cnd, final String column, final Object value) {
    return andIf(cnd, column, Oper.NE, value);
  }

  /**
   * where ne 表达式，用 AND 连接，判断值不为null时，表达式才被采用，否则表达式会被丢弃：
  * <p>- <code>neIfNotNull("type", null); 则会忽略这个表达式，不会根据type字段筛选。</code>
  * <p>- <code>neIfNotNull("type", 1);</code>
  * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
  * @param column 字段名（列名），用于指定 SQL 条件的目标列
  * @param value 匹配的内容字符串，为空时忽略该条件
  * @return 当前 WhereSql 实例，支持链式调用
   */
  public T neIfNotNull(final String column, final Object value) {
    return andIf(!Objects.isNull(value), column, Oper.NE, value);
  }

  /**
   * where ne 表达式，用 AND 连接，判断值不为null时，表达式才被采用，否则表达式会被丢弃：
   * <p>- <code>neIfNotBlank("type", null); 则会忽略这个表达式，不会根据type字段筛选。</code>
   * <p>- <code>neIfNotBlank("type", "1");</code>
   * <p>如果前面没有其他表达式，则 AND 会被忽略
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param value 匹配的内容字符串，为空时忽略该条件
   * @return 构建后的 SQL 片段对象（支持链式调用）
   */
  public T neIfNotBlank(final String column, final String value) {
    return andIf(NamingUtils.isNotBlank(value), column, Oper.NE, value);
  }

  /**
   * where 表达式，用OR连接，如下写法都是合法的：
   * <p>- <code>or("status = 1")</code>
   * <p>- <code>or("status = 1 and id = ?")</code>
   * <p>如果前面没有其他表达式，则 OR 会被忽略
   *
   * @param expr SQL 条件表达式
   * @return 构建器本身
   */
  public T or(final String expr) {
    appendWhere(Logic.OR, expr);
    return that;
  }

  /**
   * 添加带参数值的 OR 连接 where 表达式：
   * <p>- <code>or("id = ?", id)</code>
   * <p>- <code>or("status = 1 and classId = ? and score > ?", classId, 60)</code>
   * <p>in 表达式请使用 {@link #in(String, java.util.Collection)}
   * <p>like 表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 OR 会被忽略
   *
   * @param expr SQL 条件表达式
   * @param values 参数值（可变参数）
   * @return 构建器本身
   */
  public T or(final String expr, final Object... values) {
    or(expr);
    whereParams.addAll(Arrays.asList(values));
    return that;
  }

  /**
   * 添加 OR 连接的 where 表达式，例如：
   * <p>- <code>or("col1", Oper.EQ, val)</code>
   * <p>in 表达式请使用 {@link #in(String, java.util.Collection, Logic)}
   * <p>like 表达式请使用 {@link #like(String, String, Logic)}
   * <p>如果前面没有其他表达式，则 OR 会被忽略
   *
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  public T or(final String column, final Oper op, final Object value) {
    or(op.makeExpr(column));
    whereParams.add(value);
    return that;
  }

  /**
   * where 表达式，用 OR 连接，根据传入条件，当为false时，则忽略此查询条件
   * <p>- <code>orIf(false, "type=?", 1); 则会忽略这个表达式，不会根据type字段筛选。</code>
   * <p>- <code>orIf(true, "type=? or type=?", 1, 2); type= 1||2的记录会被筛选出来。</code>
   * <p>in表达式请使用 {@link #in(String, Collection, Logic)}
   * <p>like表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 OR 会被忽略
   *
   * @param cnd 判断条件，当为 false 时忽略该表达式
   * @param expr SQL 条件表达式
   * @param values 参数值
   * @return 构建器本身
   */
  public T orIf(final boolean cnd, final String expr, final Object... values) {
    if (!cnd) {
      return that;
    }

    return or(expr, values);
  }

  /**
   * where 表达式，用 OR 连接，根据传入条件，当为false时，则忽略此查询条件
   * <p>- <code>orIf(false, "type", “=”, 1); 则会忽略这个表达式，不会根据type字段筛选。</code>
   * <p>- <code>orIf(true, "type", Oper.EQ, 1, 2); type= 1||2的记录会被筛选出来。</code>
   * <p>in表达式请使用 {@link #in(String, Collection)}
   * <p>like表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 OR 会被忽略
   *
   * @param cnd 判断条件，当为 false 时忽略该表达式
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  public T orIf(final boolean cnd, final String column, final Oper op, final Object value) {
    if (!cnd) {
      return that;
    }

    return or(column, op, value);
  }

  /**
   * where 表达式，用 OR 连接，判断值不为null时，表达式才被采用，否则表达式会被丢弃：
   * <p>- <code>orIfNotNull("type = ?", null); 则会忽略这个表达式，不会根据type字段筛选。</code>
   * <p>- <code>orIfNotNull("type = ?", 1);</code>
   * <p>in表达式请使用 {@link #in(String, Collection)}
   * <p>like表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 OR 会被忽略
   *
   * @param expr 条件表达式，如 "type = ?"
   * @param value 参数值
   * @return 构建器本身
   */
  public T orIfNotNull(final String expr, final Object value) {
    return orIf(!Objects.isNull(value), expr, value);
  }

  /**
   * where 表达式，用 OR 连接，判断值不为null时，表达式才被采用，否则表达式会被丢弃：
   * <p>- <code>orIfNotNull("type", Oper.EQ, null); 则会忽略这个表达式，不会根据type字段筛选。</code>
   * <p>- <code>orIfNotNull("type", Oper.EQ, 1);</code>
   * <p>in表达式请使用 {@link #in(String, Collection)}
   * <p>like表达式请使用 {@link #like(String, String)}
   * <p>如果前面没有其他表达式，则 OR 会被忽略
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  public T orIfNotNull(final String column, final Oper op, final Object value) {
    return orIf(!Objects.isNull(value), column, op, value);
  }

  /**
   * where 表达式中的区间（BETWEEN）语句，参数为起止值：
   * <p>- <code>between("score", 60, 100);</code> 生成的SQL为：score &gt;= 60 AND score &lt;= 100
   * <p>如果start为null则忽略起始条件，end为null则忽略结束条件
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param start 起始值
   * @param end 结束值
   * @return 构造器本身
   */
  public T between(final String column, final Number start, Number end) {
    if (!Objects.isNull(start)) {
      this.and(column, Oper.GTE, start);
    }
    if (!Objects.isNull(end)) {
      this.and(column, Oper.LTE, end);
    }
    return that;
  }

  /**
   * where 表达式中的区间（BETWEEN）语句，参数为Range对象：
   * <p>- <code>between("score", new Range&lt;&gt;(60, 100));</code> 生成的SQL为：score &gt;= 60 AND score &lt;= 100
   * <p>如果begin为null则忽略起始条件，end为null则忽略结束条件
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param range 区间对象
   * @return 构造器本身
   */
  public T between(final String column, final Range<?> range) {
    if (!Objects.isNull(range.getBegin())) {
      this.and(column, Oper.GTE, range.getBegin());
    }
    if (!Objects.isNull(range.getEnd())) {
      this.and(column, Oper.LTE, range.getEnd());
    }
    return that;
  }

  /**
   * where 表达式中的 ANY 函数，其参数为一个子查询，如下：
   * <p>- <code>any("id", "select id from student where classId=1"),生成的sql为：</code>
   * <p>[AND] id = any(subquery sql)
   * <p>默认使用 AND 连接
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param subQuery 子查询构建器
   * @return 构建器本身
   */
  public T any(final String column, final T subQuery) {
    return any(column, subQuery, Logic.AND);
  }

  /**
   * where 表达式中的 ANY 函数，其参数为一个子查询，如下：
   * <p>- <code>any("id", "select id from student where classId=1", "OR"),生成的sql为：</code>
   * <p>[OR] id = any(subquery sql)
   * <p>使用 logic 逻辑符 连接，如果前面没有任何条件表达式，则 logic 会被忽略
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param subQuery 子查询构建器
   * @param logic 逻辑符
   * @return 构建器本身
   */
  public T any(final String column, final T subQuery, final Logic logic) {
    appendWhere(logic, String.format("%s = any(%s)", column, subQuery.getSql()));
    whereParams.addAll(Arrays.asList(subQuery.getParams()));
    return that;
  }

  /**
   * 任意列LIKE %value%：
   * <p>- <code>如果value为blank，则会忽略此条件</code></p>
   * <p>- 如 some(["col1", "col2"], Oper.EQ "a", Logic.AND) 则转化为 AND ( col1 = 'a' OR col2 = 'a')</p>
   * <p>- 如 some(["col1", "col2"], Oper.EQ, "a", Logic.OR) 则转化为 OR ( col1 = 'a' OR col2 = 'a')</p>
   *
   * @param columns 多个列
   * @param op 操作符
   * @param value 关键字
   * @param logic 外围逻辑
   * @return 构建器本身
   */
  public T some(final String[] columns, final Oper op, final Object value, final Logic logic) {
    if (Objects.isNull(value)) {
      if (op == Oper.EQ) {
        appendNColumnExpr(logic, columns, Oper.IS_NULL, null, Logic.OR);
      } else if (op == Oper.NE) {
        appendNColumnExpr(logic, columns, Oper.IS_NOT_NULL, null, Logic.OR);
      }
    } else {
      appendNColumnExpr(logic, columns, op, value, Logic.OR);
    }
    return that;
  }

  /**
   * AND 任意列LIKE %value%:
   * <p>- <code>如果value为blank，则会忽略此条件</code></p>
   * <p>- 如 some(["col1", "col2"], Oper.EQ, "a") 则转化为 AND ( col1 = 'a' OR col2 = 'a')</p>
   *
   * @param columns 多个列
   * @param op 操作符
   * @param value 关键字
   * @return 构建器本身
   */
  public T some(final String[] columns, final Oper op, final Object value) {
    return some(columns, op, value, Logic.AND);
  }

  /**
   * 任意列LIKE %value%：
   * <p>- <code>如果value为blank，则会忽略此条件</code></p>
   * <p>- 如 some(["col1", "col2"], Oper.EQ "a", Logic.AND) 则转化为 AND ( col1 = 'a' OR col2 = 'a')</p>
   * <p>- 如 some(["col1", "col2"], Oper.EQ, "a", Logic.OR) 则转化为 OR ( col1 = 'a' OR col2 = 'a')</p>
   * @param cnd 条件
   * @param columns 多个列
   * @param op 操作符
   * @param value 关键字
   * @param logic 外围逻辑
   * @return 构建器本身
   */
  public T someIf(final boolean cnd, final String[] columns, final Oper op, final Object value, final Logic logic) {
    if (cnd) {
      appendNColumnExpr(logic, columns, op, value, Logic.OR);
    }
    return that;
  }

  /**
   * AND 任意列 [op] [value]:
   * <p>- <code>如果value为blank，则会忽略此条件</code></p>
   * <p>- 如 some(["col1", "col2"], Oper.EQ, "a") 则转化为 AND ( col1 = 'a' OR col2 = 'a')</p>
   * @param cnd 条件
   * @param columns 多个列
   * @param op 操作符
   * @param value 关键字
   * @return 构建器本身
   */
  public T someIf(final boolean cnd, final String[] columns, final Oper op, final String value) {
    if (!cnd) {
      return that;
    }
    return some(columns, op, value, Logic.AND);
  }

  /**
   * where 表达式中的 IN 语句，其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果数组为空，则会忽略此条件</code>
   * <p>- <code>如果数组长度为1，则用=操作符替代</code>
   * <p>- <code>in("type", typesList);</code>
   * <p>默认使用 AND 连接
   *
  * @param column 字段名
  * @param values NOT IN 条件的参数数组
  * @return 当前构建器实例
  */
  public T in(final String column, final Collection<?> values) {
    return in(column, values, Logic.AND);
  }

  /**
   * where 表达式中的 IN 语句，根据传入条件，当为false时，则忽略此查询条件.
   * 其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果数组为空，则会忽略此条件</code>
   * <p>- <code>如果数组长度为1，则用=操作符替代</code>
   * <p>- <code>inIf(true, "type", typesList);</code>
   * <p>默认使用 AND 连接
   *
   * @param cnd 判断条件，当为 false 时忽略该表达式
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param values IN 条件的参数集合
   * @return 当前构建器实例
   */
  public T inIf(final boolean cnd, final String column, final Collection<?> values) {
    return cnd ? in(column, values, Logic.AND) : that;
  }

  /**
   * where 表达式中的 IN 语句，其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果数组为空，则会忽略此条件</code>
   * <p>- <code>如果数组长度为1，则用=操作符替代</code>
   * <p>- <code>in("type", typesList, "OR");</code>
   * <p>使用 logic 逻辑符 连接，如果前面没有任何条件表达式，则 logic 会被忽略
   *
  * @param column 字段名
  * @param values IN 条件的参数数组
  * @param logic 逻辑运算符（AND/OR），用于连接前后条件
  * @return 当前构建器实例
  */
  public T in(final String column, final Collection<?> values, final Logic logic) {
    if (values == null || values.isEmpty()) {
      return that;
    }
    if (values.size() == 1) {
      appendWhere(logic, column + " = ?");
    } else {
      appendWhere(logic, makeNParamExpr(column, "IN", values.size()));
    }
    whereParams.addAll(values);
    return that;
  }

  /**
   * where 表达式中的 IN 语句，其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果数组为空，则会忽略此条件</code>
   * <p>- <code>如果数组长度为1，则用=操作符替代</code>
   * <p>- <code>in("type", typesArray);</code>
   * <p>默认使用 AND 连接
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param values IN 条件的参数数组
   * @return 当前构建器实例
   */
  public T in(final String column, final Object[] values) {
    return in(column, values, Logic.AND);
  }

  /**
   * where 表达式中的 IN 语句，根据传入条件，当为false时，则忽略此查询条件.
   * 其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果数组为空，则会忽略此条件</code>
   * <p>- <code>如果数组长度为1，则用=操作符替代</code>
   * <p>- <code>inIf(true, "type", typesArray);</code>
   * <p>默认使用 AND 连接
   *
   * @param cnd 判断条件，当为 false 时忽略该表达式
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param values IN 条件的参数数组
   * @return 当前构建器实例
   */
  public T inIf(final boolean cnd, final String column, final Object[] values) {
    return cnd ? in(column, values, Logic.AND) : that;
  }

  /**
   * where 表达式中的 IN 语句，其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果数组为空，则会忽略此条件</code>
   * <p>- <code>如果数组长度为1，则用=操作符替代</code>
   * <p>- <code>in("type", typesArray, "OR");</code>
   * <p>使用 logic 逻辑符 连接，如果前面没有任何条件表达式，则 logic 会被忽略
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param values IN 条件的参数数组
   * @param logic 逻辑运算符（AND/OR），用于连接前后条件
   * @return 当前构建器实例
   */
  public T in(final String column, final Object[] values, final Logic logic) {
    if (values == null || values.length == 0) {
      return that;
    }
    return in(column, Arrays.asList(values), logic);
  }

  /**
   * where 表达式中的 NOT IN 语句，其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果数组为空，则会忽略此条件</code>
   * <p>- <code>如果数组长度为1，则用=操作符替代</code>
   * <p>- <code>not in("type", typesList);</code>
   * <p>默认使用 AND 连接
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param values NOT IN 条件的参数集合
   * @return 当前构建器实例
   */
  public T notIn(final String column, final Collection<?> values) {
    return notIn(column, values, Logic.AND);
  }

  /**
  * where 表达式中的 NOT IN 语句，其参数为数组，按照如下处理逻辑：
  * <p>- <code>如果数组为空，则会忽略此条件</code>
  * <p>- <code>如果数组长度为1，则用=操作符替代</code>
  * <p>- <code>not in("type", typesList, "OR");</code>
  * <p>使用 logic 逻辑符 连接，如果前面没有任何条件表达式，则 logic 会被忽略
  *
  * @param column 字段名
  * @param values NOT IN 条件的参数集合
  * @param logic 逻辑运算符（AND/OR），用于连接前后条件
  * @return 当前构建器实例
  */
  public T notIn(final String column, final Collection<?> values, final Logic logic) {
    if (values == null || values.isEmpty()) {
      return that;
    }
    if (values.size() == 1) {
      appendWhere(logic, Oper.NE.makeExpr(column));
    } else {
      appendWhere(logic, makeNParamExpr(column, "NOT IN", values.size()));
    }
    whereParams.addAll(values);
    return that;
  }

  /**
   * where 表达式中的 NOT IN 语句，其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果数组为空，则会忽略此条件</code>
   * <p>- <code>如果数组长度为1，则用!=操作符替代</code>
   * <p>- <code>notIn("type", typesArray);</code>
   * <p>默认使用 AND 连接
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param values NOT IN 条件的参数数组
   * @return 当前构建器实例
   */
  public T notIn(final String column, final Object[] values) {
    return notIn(column, values, Logic.AND);
  }

  /**
   * where 表达式中的 IN 语句，其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果数组为空，则会忽略此条件</code>
   * <p>- <code>如果数组长度为1，则用!=操作符替代</code>
   * <p>- <code>notIn("type", typesArray, "OR");</code>
   * <p>使用 logic 逻辑符 连接，如果前面没有任何条件表达式，则 logic 会被忽略
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param values NOT IN 条件的参数数组
   * @param logic 逻辑运算符（AND/OR），用于连接前后条件
   * @return 当前构建器实例
   */
  public T notIn(final String column, final Object[] values, final Logic logic) {
    if (values == null || values.length == 0) {
      return that;
    }
    if (values.length == 1) {
      appendWhere(logic, Oper.NE.makeExpr(column));
    } else {
      appendWhere(logic, makeNParamExpr(column, "NOT IN", values.length));
    }
    whereParams.addAll(Arrays.asList(values));
    return that;
  }

  /**
   * [AND] column IS NULL
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @return 构建器本身
   */
  @SuppressWarnings(PMD_LINGUISTIC_NAMING)
  public T isNull(final String column) {
    return isNull(column, Logic.AND);
  }

  /**
   * [AND|OR] column IS NULL
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param logic 外部逻辑符
   * @return 构建器本身
   */
  @SuppressWarnings(PMD_LINGUISTIC_NAMING)
  public T isNull(final String column, final Logic logic) {
    appendWhere(logic, Oper.IS_NULL.makeExpr(column));
    return that;
  }

  /**
   * 如果条件成立，则为[AND] column IS NULL，否则忽略
   * @param cnd 条件
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @return 构建器本身
   */
  @SuppressWarnings(PMD_LINGUISTIC_NAMING)
  public T isNullIf(final boolean cnd, final String column) {
    if (!cnd) {
      return that;
    }
    return isNull(column, Logic.AND);
  }

  /**
   * 如果条件成立，则为[AND|OR] column IS NULL，否则忽略
   * @param cnd 条件，当为 false 时忽略该表达式
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param logic 外部逻辑符
   * @return 构建器本身
   */
  @SuppressWarnings(PMD_LINGUISTIC_NAMING)
  public T isNullIf(final boolean cnd, final String column, final Logic logic) {
    if (!cnd) {
      return that;
    }
    appendWhere(logic, Oper.IS_NULL.makeExpr(column));
    return that;
  }

  /**
   * [AND] column IS NOT NULL
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @return 构建器本身
   */
  @SuppressWarnings(PMD_LINGUISTIC_NAMING)
  public T isNotNull(final String column) {
    return isNotNull(column, Logic.AND);
  }

  /**
   * [AND|OR] column IS NOT NULL
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param logic 外部逻辑符
   * @return 构建器本身
   */
  @SuppressWarnings(PMD_LINGUISTIC_NAMING)
  public T isNotNull(final String column, final Logic logic) {
    appendWhere(logic, Oper.IS_NOT_NULL.makeExpr(column));
    return that;
  }

  /**
   * 如果条件成立，则为[AND] column IS NOT NULL，否则忽略
   * @param cnd 条件
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @return 构建器本身
   */
  @SuppressWarnings(PMD_LINGUISTIC_NAMING)
  public T isNotNullIf(final boolean cnd, final String column) {
    if (!cnd) {
      return that;
    }
    return isNotNull(column, Logic.AND);
  }

  /**
   * 如果条件成立，则为[AND|OR] column IS NOT NULL，否则忽略
   * @param cnd 条件
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param logic 外部逻辑符
   * @return 构建器本身
   */
  @SuppressWarnings(PMD_LINGUISTIC_NAMING)
  public T isNotNullIf(final boolean cnd, final String column, final Logic logic) {
    if (!cnd) {
      return that;
    }
    appendWhere(logic, Oper.IS_NOT_NULL.makeExpr(column));
    return that;
  }

  /**
   * where 表达式中的 LIKE 语句，其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果value为blank，则会忽略此条件</code>
   * <p>- <code>like("name", "abc");生成的sql为：</code>
   * <p>[AND] name like '%abc%';
   *
   * <p>默认使用 AND 连接
   * <p>部分匹配请使用 {@link #startWith(String, String)} 或 {@link #endWith(String, String)}
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param value 匹配的内容字符串，为空时忽略该条件
   * @return 构建器本身
   */
  public T like(final String column, final String value) {
    return like(column, value, Logic.AND);
  }

  /**
   * where 表达式中的 LIKE 语句，其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果value为blank，则会忽略此条件</code>
   * <p>- <code>like("name", "abc", "OR");生成的sql为：</code>
   * <p>[OR] name like '%abc%';
   *
   * <p>使用 logic 逻辑符 连接，如果前面没有任何条件表达式，则 logic 会被忽略
   * <p>部分匹配请使用 {@link #startWith(String, String)} 或 {@link #endWith(String, String)}
   *
  * @param column 字段名（列名），用于指定 SQL 条件的目标列
  * @param value 匹配的内容字符串，为空时忽略该条件
  * @param logic 逻辑运算符（如 AND、OR），用于连接多个条件
  * @return 当前 WhereSql 实例，支持链式调用
   */
  public T like(final String column, final String value, final Logic logic) {
    if (NamingUtils.isBlank(value)) {
      appendOrTrue(logic);
      return that;
    }
    appendWhere(logic, column + " like ?");
    whereParams.add("%" + value + "%");
    return that;
  }

  /**
   * where 表达式中的 NOT LIKE 语句。
   * <p>- <code>notLike("name", "abc")</code> 生成的SQL为：[AND] name NOT LIKE '%abc%';
   * <p>默认使用 AND 连接
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  public T notLike(final String column, final String value) {
    return notLike(column, value, Logic.AND);
  }

  /**
   * where 表达式中的 NOT LIKE 语句，支持自定义逻辑符。
   * <p>- <code>notLike("name", "abc", Logic.OR)</code> 生成的SQL为：[OR] name NOT LIKE '%abc%';
   * @param column 列名
   * @param value 匹配值
   * @param logic 逻辑符
   * @return 构建器本身
   */
  public T notLike(final String column, final String value, final Logic logic) {
    if (NamingUtils.isBlank(value)) {
      appendOrTrue(logic);
      return that;
    }
    appendWhere(logic, column + " NOT LIKE ?");
    whereParams.add("%" + value + "%");
    return that;
  }

  /**
   * 任意列LIKE %value%：
   * <p>- <code>如果value为blank，则会忽略此条件</code></p>
   * <p>- 如 someLike(["col1", "col2", "a", Logic.AND]) 则转化为 AND ( col1 LIKE '%a%' OR col2 LIKE '%a%')</p>
   * <p>- 如 someLike(["col1", "col2", "a", Logic.OR]) 则转化为 OR ( col1 LIKE '%a%' OR col2 LIKE '%a%')</p>
   *
   * @param columns 多个列
   * @param value 关键字
   * @param logic 外围逻辑
   * @return 构建器本身
   */
  public T someLike(final String[] columns, final String value, final Logic logic) {
    if (NamingUtils.isBlank(value)) {
      appendOrTrue(logic);
      return that;
    }
    appendNColumnExpr(logic, columns, Oper.LIKE, "%" + value + "%", Logic.OR);

    return that;
  }

  /**
   * AND 任意列LIKE %value%:
   * <p>- <code>如果value为blank，则会忽略此条件</code></p>
   * <p>如 someLike(["col1", "col2", "a") 则转化为 AND ( col1 LIKE '%a%' OR col2 LIKE '%a%')</p>
   * @param columns 多个列
   * @param value 关键字
   * @return 构建器本身
   */
  public T someLike(final String[] columns, final String value) {
    return someLike(columns, value, Logic.AND);
  }

  /**
   * where 表达式中的 LIKE 语句，其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果value为blank，则会忽略此条件</code>
   * <p>- <code>startWith("name", "abc");生成的sql为：</code>
   * <p>[AND] name like 'abc%';
   *
   * <p>默认使用 AND 连接
   * <p>全匹配或部分匹配请使用 {@link #like(String, String)} 或 {@link #endWith(String, String)}
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param value 匹配的内容字符串，为空时忽略该条件
   * @return 构建器本身
   */
  public T startWith(final String column, final String value) {
    return startWith(column, value, Logic.AND);
  }

  /**
   * where 表达式中的 LIKE 语句，其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果value为blank，则会忽略此条件</code>
   * <p>- <code>startWith("name", "abc", "OR");生成的sql为：</code>
   * <p>[OR] name like 'abc%';
   *
   * <p>使用 logic 逻辑符 连接，如果前面没有任何条件表达式，则 logic 会被忽略
   * <p>全匹配或部分匹配请使用 {@link #like(String, String)} 或 {@link #endWith(String, String)}
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param value 匹配的内容字符串，为空时忽略该条件
   * @param logic 逻辑运算符（如 AND、OR），用于连接多个条件
   * @return 构建器本身
   */
  public T startWith(final String column, final String value, final Logic logic) {
    if (NamingUtils.isBlank(value)) {
      appendOrTrue(logic);
      return that;
    }
    appendWhere(logic, column + " like ?");
    whereParams.add(value + "%");
    return that;
  }

  /**
   * 任意列LIKE value%：
   * <p>- <code>如果value为blank，则会忽略此条件</code></p>
   * <p>- 如 someStartWith(["col1", "col2", "a", Logic.AND]) 则转化为 AND ( col1 LIKE 'a%' OR col2 LIKE 'a%')</p>
   * <p>- 如 someStartWith(["col1", "col2", "a", Logic.OR]) 则转化为 OR ( col1 LIKE 'a%' OR col2 LIKE 'a%')</p>
   * @param columns 多个列
   * @param value 关键字
   * @param logic 外围逻辑
   * @return 构建器本身
   */
  public T someStartWith(final String[] columns, final String value, final Logic logic) {
    if (NamingUtils.isBlank(value)) {
      appendOrTrue(logic);
      return that;
    }

    appendNColumnExpr(logic, columns, Oper.LIKE, value + "%", Logic.OR);

    return that;
  }

  /**
   * AND 任意列LIKE value%:
   * <p>- <code>如果value为blank，则会忽略此条件</code></p>
   * <p>如 someStartWith(["col1", "col2", "a") 则转化为 AND ( col1 LIKE 'a%' OR col2 LIKE 'a%')</p>
   * @param columns 多个列
   * @param value 关键字
   * @return 构建器本身
   */
  public T someStartWith(final String[] columns, final String value) {
    return someStartWith(columns, value, Logic.AND);
  }

  /**
   * where 表达式中的 LIKE 语句，其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果value为blank，则会忽略此条件</code>
   * <p>- <code>endWith("name", "abc"); //生成的sql为：</code>
   * <p>[AND] name like '%abc';
   *
   * <p>默认使用 AND 连接
   * <p>全匹配或部分匹配请使用 {@link #like(String, String)} 或 {@link #startWith(String, String)}
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param value 匹配的内容字符串，为空时忽略该条件
   * @return 构建器本身
   */
  public T endWith(final String column, final String value) {
    return endWith(column, value, Logic.AND);
  }

  /**
   * where 表达式中的 LIKE 语句，其参数为数组，按照如下处理逻辑：
   * <p>- <code>如果value为blank，则会忽略此条件</code>
   * <p>- <code>endWith("name", "abc", "OR"); //生成的sql为：</code>
   * <p>[OR] name like '%abc';
   *
   * <p>使用 logic 逻辑符 连接，如果前面没有任何条件表达式，则 logic 会被忽略
   * <p>全匹配或部分匹配请使用 {@link #like(String, String)} 或 {@link #startWith(String, String)}
   *
   * @param column 字段名（列名），用于指定 SQL 条件的目标列
   * @param value 匹配的内容字符串，为空时忽略该条件
   * @param logic 逻辑运算符（如 AND、OR），用于连接多个条件
   * @return 构建器本身
   */
  public T endWith(final String column, final String value, final Logic logic) {
    if (NamingUtils.isBlank(value)) {
      appendOrTrue(logic);
      return that;
    }
    appendWhere(logic, column + " like ?");
    whereParams.add("%" + value);
    return that;
  }

  /**
   * 任意列LIKE %value：
   * <p>- <code>如果value为blank，则会忽略此条件</code></p>
   * <p>- 如 someEndWith(["col1", "col2", "a", Logic.AND]) 则转化为 AND ( col1 LIKE '%a' OR col2 LIKE '%a')</p>
   * <p>- 如 someEndWith(["col1", "col2", "a", Logic.OR]) 则转化为 OR ( col1 LIKE '%a' OR col2 LIKE '%a')</p>
   * @param columns 多个列
   * @param value 关键字
   * @param logic 外围逻辑
   * @return 构建器本身
   */
  public T someEndWith(final String[] columns, final String value, final Logic logic) {
    if (NamingUtils.isBlank(value)) {
      appendOrTrue(logic);
      return that;
    }

    appendNColumnExpr(logic, columns, Oper.LIKE, "%" + value, Logic.OR);

    return that;
  }

  /**
   * AND 任意列LIKE %value:
   * <p>- <code>如果value为blank，则会忽略此条件</code></p>
   * <p>如 someEndWith(["col1", "col2", "a") 则转化为 AND ( col1 LIKE '%a' OR col2 LIKE '%a')</p>
   * @param columns 多个列
   * @param value 关键字
   * @return 构建器本身
   */
  public T someEndWith(final String[] columns, final String value) {
    return someEndWith(columns, value, Logic.AND);
  }

  /**
  * where 表达式中的 EXISTS 子查询。
  * <p>- <code>exists("select ...")</code> 生成的SQL为：[AND] EXISTS (subQuery)
  * <p>默认使用 AND 连接
  * @param subQuery 子查询SQL字符串
  * @return 构建器本身
  */
  public T exists(final String subQuery) {
    return exists(subQuery, Logic.AND);
  }

  /**
   * where 表达式中的 EXISTS 子查询，支持自定义逻辑符。
   * <p>- <code>exists("select ...", Logic.OR)</code> 生成的SQL为：[OR] EXISTS (subQuery)
   * @param subQuery 子查询SQL字符串
   * @param logic 逻辑符
   * @return 构建器本身
   */
  public T exists(final String subQuery, final Logic logic) {
    appendWhere(logic, "EXISTS (" + subQuery + ")");
    return that;
  }

  /**
   * where 表达式中的 NOT EXISTS 子查询。
   * <p>- <code>notExists("select ...")</code> 生成的SQL为：[AND] NOT EXISTS (subQuery)
   * <p>默认使用 AND 连接
   * @param subQuery 子查询SQL字符串
   * @return 构建器本身
   */
  public T notExists(final String subQuery) {
    return notExists(subQuery, Logic.AND);
  }

  /**
   * where 表达式中的 NOT EXISTS 子查询，支持自定义逻辑符。
   * <p>- <code>notExists("select ...", Logic.OR)</code> 生成的SQL为：[OR] NOT EXISTS (subQuery)
   * @param subQuery 子查询SQL字符串
   * @param logic 逻辑符
   * @return 构建器本身
   */
  public T notExists(final String subQuery, final Logic logic) {
    appendWhere(logic, "NOT EXISTS (" + subQuery + ")");
    return that;
  }

  /**
   * 将条件语句追加到 where 语句中，如果之前没有语句，则忽略逻辑操作符。
   *
   * @param logicOp 逻辑操作符（AND/OR）
   * @param expr SQL表达式
   */
  private void appendWhere(final Logic logicOp, final String expr) {
    if (whereColumns.isEmpty()) {
      whereColumns.add(expr);
    } else {
      whereColumns.add(String.format("%s (%s)", logicOp.getLogic(), expr));
    }
  }

  /**
  * 如果是 OR 逻辑表达式，则自动添加 OR 1=1；如果是 AND，则不添加任何内容。
  * @param logicOp 逻辑操作符（AND/OR）
  */
  private void appendOrTrue(final Logic logicOp) {
    if (logicOp == Logic.OR) {
      appendWhere(Logic.OR, "1=1");
    }
  }

  /**
  * 添加多列条件表达式，如 (col1 = ? OR col2 = ?)。
  * @param logicOp 外部逻辑运算符（AND/OR）
  * @param columns 多个列名
  * @param op 操作符
  * @param value 参数值
  * @param logic 内部逻辑运算符（AND/OR）
  */

  private void appendNColumnExpr(final Logic logicOp, final String[] columns, final Oper op, final Object value,
      final Logic innerLogic) {

    StringBuilder exprSb = new StringBuilder();
    exprSb.append("(");
    for (int i = 0; i < columns.length; i++) {
      if (i > 0)
        exprSb.append(" ").append(innerLogic.getLogic()).append(" ");
      exprSb.append(op.makeExpr(columns[i]));
    }
    exprSb.append(")");

    appendWhere(logicOp, exprSb.toString());
    if (op.hasValue()) {
      for (int i = 0; i < columns.length; i++) {
        whereParams.add(value);
      }
    }
  }

  /**
   * 生成有 n 个参数的表达式，主要用于 IN/NOT IN 语句。
   * @param column 列名
   * @param op 操作符（IN/NOT IN）
   * @param nCount 参数数量
   * @return 构造好的 SQL 片段
   */
  private String makeNParamExpr(final String column, final String op, final int nCount) {
    final StringBuilder expr = new StringBuilder();
    int idx = 0;
    expr.append(column).append(' ').append(op).append(" (");

    while (idx < nCount) {
      if (idx != 0) {
        expr.append(", ");
      }
      expr.append('?');
      idx++;
    }
    expr.append(')');

    return expr.toString();
  }

}
