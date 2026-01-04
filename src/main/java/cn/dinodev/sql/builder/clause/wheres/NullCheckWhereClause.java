// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.wheres;

import cn.dinodev.sql.Logic;
import cn.dinodev.sql.Oper;
import cn.dinodev.sql.SqlBuilder;

/**
 * 空值检查 WHERE 子句接口，提供 IS NULL、IS NOT NULL 等空值判断操作。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-11-23
 */
public interface NullCheckWhereClause<T extends SqlBuilder> extends WhereClauseSupport<T> {

  /**
   * 添加 IS NULL 条件，AND 连接。
   * 
   * @param column 列名
   * @return 构建器本身
   */
  @SuppressWarnings("PMD.LinguisticNaming")
  default T isNull(final String column) {
    return isNull(column, Logic.AND);
  }

  /**
   * 添加 IS NULL 条件，支持自定义逻辑符。
   * 
   * @param column 列名
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  @SuppressWarnings("PMD.LinguisticNaming")
  default T isNull(final String column, final Logic logic) {
    appendWhere(logic, Oper.IS_NULL.makeExpr(column));
    return self();
  }

  /**
   * 根据条件决定是否添加 IS NULL 表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @return 构建器本身
   */
  @SuppressWarnings("PMD.LinguisticNaming")
  default T isNullIf(final boolean cnd, final String column) {
    if (!cnd) {
      return self();
    }
    return isNull(column, Logic.AND);
  }

  /**
   * 根据条件决定是否添加 IS NULL 表达式，支持自定义逻辑符。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  @SuppressWarnings("PMD.LinguisticNaming")
  default T isNullIf(final boolean cnd, final String column, final Logic logic) {
    if (!cnd) {
      return self();
    }
    appendWhere(logic, Oper.IS_NULL.makeExpr(column));
    return self();
  }

  /**
   * 添加 IS NOT NULL 条件，AND 连接。
   * 
   * @param column 列名
   * @return 构建器本身
   */
  @SuppressWarnings("PMD.LinguisticNaming")
  default T isNotNull(final String column) {
    return isNotNull(column, Logic.AND);
  }

  /**
   * 添加 IS NOT NULL 条件，支持自定义逻辑符。
   * 
   * @param column 列名
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  @SuppressWarnings("PMD.LinguisticNaming")
  default T isNotNull(final String column, final Logic logic) {
    appendWhere(logic, Oper.IS_NOT_NULL.makeExpr(column));
    return self();
  }

  /**
   * 根据条件决定是否添加 IS NOT NULL 表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @return 构建器本身
   */
  @SuppressWarnings("PMD.LinguisticNaming")
  default T isNotNullIf(final boolean cnd, final String column) {
    if (!cnd) {
      return self();
    }
    return isNotNull(column, Logic.AND);
  }

  /**
   * 根据条件决定是否添加 IS NOT NULL 表达式，支持自定义逻辑符。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  @SuppressWarnings("PMD.LinguisticNaming")
  default T isNotNullIf(final boolean cnd, final String column, final Logic logic) {
    if (!cnd) {
      return self();
    }
    appendWhere(logic, Oper.IS_NOT_NULL.makeExpr(column));
    return self();
  }
}
