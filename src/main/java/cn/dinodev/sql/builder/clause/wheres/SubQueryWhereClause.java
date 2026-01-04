// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.wheres;

import cn.dinodev.sql.Logic;
import cn.dinodev.sql.SqlBuilder;

/**
 * 子查询 WHERE 子句接口，提供 EXISTS、NOT EXISTS、ANY、WITH 等子查询操作。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-11-23
 */
public interface SubQueryWhereClause<T extends SqlBuilder> extends WhereClauseSupport<T> {

  /**
   * 添加 ANY 子查询条件，AND 连接。
   * 
   * @param column 列名
   * @param subQuery 子查询构建器
   * @return 构建器本身
   */
  default T any(final String column, final T subQuery) {
    return any(column, subQuery, Logic.AND);
  }

  /**
   * 添加 ANY 子查询条件，支持自定义逻辑符。
   * 
   * @param column 列名
   * @param subQuery 子查询构建器
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T any(final String column, final T subQuery, final Logic logic) {
    appendWhere(logic, String.format("%s = any(%s)", column, subQuery.getSql()));
    innerWhereHolder().addWhereParams(subQuery.getParams());
    return self();
  }

  /**
   * 添加 EXISTS 子查询条件，AND 连接。
   * 
   * @param subQuery 子查询SQL字符串
   * @return 构建器本身
   */
  default T exists(final String subQuery) {
    return exists(subQuery, Logic.AND);
  }

  /**
   * 添加 EXISTS 子查询条件，支持自定义逻辑符。
   * 
   * @param subQuery 子查询SQL字符串
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T exists(final String subQuery, final Logic logic) {
    appendWhere(logic, "EXISTS (" + subQuery + ")");
    return self();
  }

  /**
   * 添加 NOT EXISTS 子查询条件，AND 连接。
   * 
   * @param subQuery 子查询SQL字符串
   * @return 构建器本身
   */
  default T notExists(final String subQuery) {
    return notExists(subQuery, Logic.AND);
  }

  /**
   * 添加 NOT EXISTS 子查询条件，支持自定义逻辑符。
   * 
   * @param subQuery 子查询SQL字符串
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T notExists(final String subQuery, final Logic logic) {
    appendWhere(logic, "NOT EXISTS (" + subQuery + ")");
    return self();
  }
}
