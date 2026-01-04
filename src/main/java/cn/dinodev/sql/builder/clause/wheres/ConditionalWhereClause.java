// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.wheres;

import java.util.Objects;

import cn.dinodev.sql.Oper;
import cn.dinodev.sql.SqlBuilder;

/**
 * 条件控制 WHERE 子句接口，提供基于条件判断的查询方法。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-11-23
 */
public interface ConditionalWhereClause<T extends SqlBuilder> extends WhereClauseSupport<T> {

  /**
   * 根据条件决定是否添加 WHERE 表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param expr SQL 条件表达式
   * @param values 参数值（可变参数）
   * @return 构建器本身
   */
  default T whereIf(final boolean cnd, final String expr, final Object... values) {
    if (!cnd) {
      return self();
    }
    return where(expr, values);
  }

  /**
   * 根据条件决定是否添加 WHERE 表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  default T whereIf(final boolean cnd, final String column, final Oper op, final Object value) {
    if (!cnd) {
      return self();
    }
    return where(column, op, value);
  }

  /**
   * 值不为 null 时才添加 WHERE 表达式。
   * 
   * @param expr SQL 条件表达式
   * @param value 参数值
   * @return 构建器本身
   */
  default T whereIfNotNull(final String expr, final Object value) {
    if (value == null) {
      return self();
    }
    return where(expr, value);
  }

  /**
   * 值不为 null 时才添加 WHERE 表达式。
   * 
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  default T whereIfNotNull(final String column, final Oper op, final Object value) {
    if (!Objects.isNull(value)) {
      where(column, op, value);
    }
    return self();
  }

  /**
   * 根据条件决定是否添加 AND 表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param expr SQL 条件表达式
   * @param values 参数值（可变参数）
   * @return 构建器本身
   */
  default T andIf(final boolean cnd, final String expr, final Object... values) {
    if (!cnd) {
      return self();
    }
    return and(expr, values);
  }

  /**
   * 根据条件决定是否添加 AND 表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  default T andIf(final boolean cnd, final String column, final Oper op, final Object value) {
    if (!cnd) {
      return self();
    }
    return and(column, op, value);
  }

  /**
   * 值不为 null 时才添加 AND 表达式。
   * 
   * @param expr SQL 条件表达式
   * @param value 参数值
   * @return 构建器本身
   */
  default T andIfNotNull(final String expr, final Object value) {
    return andIf(!Objects.isNull(value), expr, value);
  }

  /**
   * 值不为 null 时才添加 AND 表达式。
   * 
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  default T andIfNotNull(final String column, final Oper op, final Object value) {
    return andIf(!Objects.isNull(value), column, op, value);
  }

  /**
   * 根据条件决定是否添加 OR 表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param expr SQL 条件表达式
   * @param values 参数值（可变参数）
   * @return 构建器本身
   */
  default T orIf(final boolean cnd, final String expr, final Object... values) {
    if (!cnd) {
      return self();
    }
    return or(expr, values);
  }

  /**
   * 根据条件决定是否添加 OR 表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  default T orIf(final boolean cnd, final String column, final Oper op, final Object value) {
    if (!cnd) {
      return self();
    }
    return or(column, op, value);
  }

  /**
   * 值不为 null 时才添加 OR 表达式。
   * 
   * @param expr SQL 条件表达式
   * @param value 参数值
   * @return 构建器本身
   */
  default T orIfNotNull(final String expr, final Object value) {
    return orIf(!Objects.isNull(value), expr, value);
  }

  /**
   * 值不为 null 时才添加 OR 表达式。
   * 
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  default T orIfNotNull(final String column, final Oper op, final Object value) {
    return orIf(!Objects.isNull(value), column, op, value);
  }
}
