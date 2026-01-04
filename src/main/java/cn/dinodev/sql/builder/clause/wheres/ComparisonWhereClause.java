// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.wheres;

import java.util.Objects;

import cn.dinodev.sql.Logic;
import cn.dinodev.sql.Oper;
import cn.dinodev.sql.SqlBuilder;
import cn.dinodev.sql.utils.StringUtils;

/**
 * 比较操作 WHERE 子句接口，提供等值、不等值、大于、小于等比较操作。
 * <p>
 * 支持链式调用，提供条件判断和 AND/OR 逻辑连接。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-11-23
 */
public interface ComparisonWhereClause<T extends SqlBuilder> extends WhereClauseSupport<T> {

  /**
   * 添加等值（=）条件，AND 连接。
   * 
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T eq(final String column, final Object value) {
    return and(column, Oper.EQ, value);
  }

  /**
   * 添加等值（=）条件，支持自定义逻辑符。
   * 
   * @param column 列名
   * @param value 参数值
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T eq(final String column, final Object value, final Logic logic) {
    return and(column, Oper.EQ, value, logic);
  }

  /**
   * 根据条件决定是否添加等值（=）表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T eqIf(final boolean cnd, final String column, final Object value) {
    if (cnd) {
      and(column, Oper.EQ, value);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加等值（=）表达式。
   * 
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T eqIfNotNull(final String column, final Object value) {
    if (!Objects.isNull(value)) {
      and(column, Oper.EQ, value);
    }
    return self();
  }

  /**
   * 值不为空字符串时才添加等值（=）表达式。
   * 
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T eqIfNotBlank(final String column, final String value) {
    if (StringUtils.isNotBlank(value)) {
      and(column, Oper.EQ, value);
    }
    return self();
  }

  /**
   * 添加不等值（!=）条件，AND 连接。
   * 
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T ne(final String column, final Object value) {
    return and(column, Oper.NE, value);
  }

  /**
   * 添加不等值（!=）条件，支持自定义逻辑符。
   * 
   * @param column 列名
   * @param value 参数值
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T ne(final String column, final Object value, final Logic logic) {
    return and(column, Oper.NE, value, logic);
  }

  /**
   * 根据条件决定是否添加不等值（!=）表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T neIf(final boolean cnd, final String column, final Object value) {
    if (cnd) {
      and(column, Oper.NE, value);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加不等值（!=）表达式。
   * 
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T neIfNotNull(final String column, final Object value) {
    if (!Objects.isNull(value)) {
      and(column, Oper.NE, value);
    }
    return self();
  }

  /**
   * 值不为空字符串时才添加不等值（!=）表达式。
   * 
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T neIfNotBlank(final String column, final String value) {
    if (StringUtils.isNotBlank(value)) {
      and(column, Oper.NE, value);
    }
    return self();
  }

  /**
   * 添加大于（>）条件，AND 连接。
   * 
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T gt(final String column, final Object value) {
    return and(column, Oper.GT, value);
  }

  /**
   * 添加大于（>）条件，支持自定义逻辑符。
   * 
   * @param column 列名
   * @param value 参数值
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T gt(final String column, final Object value, final Logic logic) {
    return and(column, Oper.GT, value, logic);
  }

  /**
   * 根据条件决定是否添加大于（>）表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T gtIf(final boolean cnd, final String column, final Object value) {
    if (cnd) {
      and(column, Oper.GT, value);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加大于（>）表达式。
   * 
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T gtIfNotNull(final String column, final Object value) {
    if (!Objects.isNull(value)) {
      and(column, Oper.GT, value);
    }
    return self();
  }

  /**
   * 添加小于（&lt;）条件，AND 连接。
   * 
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T lt(final String column, final Object value) {
    return and(column, Oper.LT, value);
  }

  /**
   * 添加小于（&lt;）条件，支持自定义逻辑符。
   * 
   * @param column 列名
   * @param value 参数值
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T lt(final String column, final Object value, final Logic logic) {
    return and(column, Oper.LT, value, logic);
  }

  /**
   * 根据条件决定是否添加小于（&lt;）表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T ltIf(final boolean cnd, final String column, final Object value) {
    if (cnd) {
      and(column, Oper.LT, value);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加小于（&lt;）表达式。
   * 
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T ltIfNotNull(final String column, final Object value) {
    if (!Objects.isNull(value)) {
      and(column, Oper.LT, value);
    }
    return self();
  }

  /**
   * 添加大于等于（>=）条件，AND 连接。
   * 
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T gte(final String column, final Object value) {
    return and(column, Oper.GTE, value);
  }

  /**
   * 添加大于等于（>=）条件，支持自定义逻辑符。
   * 
   * @param column 列名
   * @param value 参数值
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T gte(final String column, final Object value, final Logic logic) {
    return and(column, Oper.GTE, value, logic);
  }

  /**
   * 根据条件决定是否添加大于等于（>=）表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T gteIf(final boolean cnd, final String column, final Object value) {
    if (cnd) {
      and(column, Oper.GTE, value);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加大于等于（>=）表达式。
   * 
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T gteIfNotNull(final String column, final Object value) {
    if (!Objects.isNull(value)) {
      and(column, Oper.GTE, value);
    }
    return self();
  }

  /**
   * 添加小于等于（&lt;=）条件，AND 连接。
   * 
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T lte(final String column, final Object value) {
    return and(column, Oper.LTE, value);
  }

  /**
   * 添加小于等于（&lt;=）条件，支持自定义逻辑符。
   * 
   * @param column 列名
   * @param value 参数值
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T lte(final String column, final Object value, final Logic logic) {
    return and(column, Oper.LTE, value, logic);
  }

  /**
   * 根据条件决定是否添加小于等于（&lt;=）表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T lteIf(final boolean cnd, final String column, final Object value) {
    if (cnd) {
      and(column, Oper.LTE, value);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加小于等于（&lt;=）表达式。
   * 
   * @param column 列名
   * @param value 参数值
   * @return 构建器本身
   */
  default T lteIfNotNull(final String column, final Object value) {
    if (!Objects.isNull(value)) {
      and(column, Oper.LTE, value);
    }
    return self();
  }
}
