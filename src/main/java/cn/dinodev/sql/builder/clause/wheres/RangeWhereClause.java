// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.wheres;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import cn.dinodev.sql.Logic;
import cn.dinodev.sql.Oper;
import cn.dinodev.sql.Range;
import cn.dinodev.sql.SqlBuilder;
import cn.dinodev.sql.utils.SqlBuilderUtils;

/**
 * 范围查询 WHERE 子句接口，提供 IN、NOT IN、BETWEEN 等范围操作。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-11-23
 */
public interface RangeWhereClause<T extends SqlBuilder> extends WhereClauseSupport<T> {

  /**
   * 添加 BETWEEN 区间条件。
   * 
   * @param column 列名
   * @param start 起始值
   * @param end 结束值
   * @return 构建器本身
   */
  default T between(final String column, final Number start, final Number end) {
    if (!Objects.isNull(start)) {
      and(column, Oper.GTE, start);
    }
    if (!Objects.isNull(end)) {
      and(column, Oper.LTE, end);
    }
    return self();
  }

  /**
   * 添加 BETWEEN 区间条件，使用 Range 对象。
   * 
   * @param column 列名
   * @param range 区间对象
   * @return 构建器本身
   */
  default T between(final String column, final Range<?> range) {
    if (!Objects.isNull(range.getBegin())) {
      and(column, Oper.GTE, range.getBegin());
    }
    if (!Objects.isNull(range.getEnd())) {
      and(column, Oper.LTE, range.getEnd());
    }
    return self();
  }

  /**
   * 添加 IN 条件，AND 连接。
   * 
   * @param column 列名
   * @param values IN 条件的参数集合
   * @return 构建器本身
   */
  default T in(final String column, final Collection<?> values) {
    return in(column, values, Logic.AND);
  }

  /**
   * 根据条件决定是否添加 IN 表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param values IN 条件的参数集合
   * @return 构建器本身
   */
  default T inIf(final boolean cnd, final String column, final Collection<?> values) {
    return cnd ? in(column, values, Logic.AND) : self();
  }

  /**
   * 添加 IN 条件，支持自定义逻辑符。
   * 
   * @param column 列名
   * @param values IN 条件的参数集合
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T in(final String column, final Collection<?> values, final Logic logic) {
    if (values == null || values.isEmpty()) {
      return self();
    }
    if (values.size() == 1) {
      appendWhere(logic, Oper.EQ.makeExpr(column));
    } else {
      appendWhere(logic, SqlBuilderUtils.makeNParamExprForOper(column, Oper.IN, values.size()));
    }
    innerWhereHolder().addParamsFromCollection(values);
    return self();
  }

  /**
   * 添加 IN 条件，使用数组参数。
   * 
   * @param column 列名
   * @param values IN 条件的参数数组
   * @return 构建器本身
   */
  default T in(final String column, final Object[] values) {
    return in(column, values, Logic.AND);
  }

  /**
   * 根据条件决定是否添加 IN 表达式，使用数组参数。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param values IN 条件的参数数组
   * @return 构建器本身
   */
  default T inIf(final boolean cnd, final String column, final Object[] values) {
    return cnd ? in(column, values, Logic.AND) : self();
  }

  /**
   * 添加 IN 条件，使用数组参数，支持自定义逻辑符。
   * 
   * @param column 列名
   * @param values IN 条件的参数数组
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T in(final String column, final Object[] values, final Logic logic) {
    if (values == null || values.length == 0) {
      return self();
    }
    return in(column, Arrays.asList(values), logic);
  }

  /**
   * 添加 NOT IN 条件，AND 连接。
   * 
   * @param column 列名
   * @param values NOT IN 条件的参数集合
   * @return 构建器本身
   */
  default T notIn(final String column, final Collection<?> values) {
    return notIn(column, values, Logic.AND);
  }

  /**
   * 添加 NOT IN 条件，支持自定义逻辑符。
   * 
   * @param column 列名
   * @param values NOT IN 条件的参数集合
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T notIn(final String column, final Collection<?> values, final Logic logic) {
    if (values == null || values.isEmpty()) {
      return self();
    }
    if (values.size() == 1) {
      appendWhere(logic, Oper.NE.makeExpr(column));
    } else {
      appendWhere(logic, SqlBuilderUtils.makeNParamExprForOper(column, Oper.NOT_IN, values.size()));
    }
    innerWhereHolder().addParamsFromCollection(values);
    return self();
  }

  /**
   * 添加 NOT IN 条件，使用数组参数。
   * 
   * @param column 列名
   * @param values NOT IN 条件的参数数组
   * @return 构建器本身
   */
  default T notIn(final String column, final Object[] values) {
    return notIn(column, values, Logic.AND);
  }

  /**
   * 添加 NOT IN 条件，使用数组参数，支持自定义逻辑符。
   * 
   * @param column 列名
   * @param values NOT IN 条件的参数数组
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T notIn(final String column, final Object[] values, final Logic logic) {
    if (values == null || values.length == 0) {
      return self();
    }
    if (values.length == 1) {
      appendWhere(logic, Oper.NE.makeExpr(column));
    } else {
      appendWhere(logic, SqlBuilderUtils.makeNParamExprForOper(column, Oper.NOT_IN, values.length));
    }
    innerWhereHolder().addWhereParams(values);
    return self();
  }

}
