// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.wheres;

import java.util.Objects;

import cn.dinodev.sql.Logic;
import cn.dinodev.sql.Oper;
import cn.dinodev.sql.SqlBuilder;
import cn.dinodev.sql.utils.StringUtils;

/**
 * 多列操作 WHERE 子句接口，提供对多个列的批量条件操作。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-11-23
 */
public interface MultiColumnWhereClause<T extends SqlBuilder> extends WhereClauseSupport<T> {

  /**
   * 任意列满足条件（OR 连接多列），支持自定义外围逻辑符。
   * 
   * @param columns 多个列名
   * @param op 操作符
   * @param value 参数值
   * @param logic 外围逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T some(final String[] columns, final Oper op, final Object value, final Logic logic) {
    if (Objects.isNull(value)) {
      if (op == Oper.EQ) {
        appendNColumnExpr(logic, columns, Oper.IS_NULL, null, Logic.OR);
      } else if (op == Oper.NE) {
        appendNColumnExpr(logic, columns, Oper.IS_NOT_NULL, null, Logic.OR);
      }
    } else {
      appendNColumnExpr(logic, columns, op, value, Logic.OR);
    }
    return self();
  }

  /**
   * 任意列满足条件（OR 连接多列），AND 连接。
   * 
   * @param columns 多个列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  default T some(final String[] columns, final Oper op, final Object value) {
    return some(columns, op, value, Logic.AND);
  }

  /**
   * 根据条件决定是否添加多列表达式，支持自定义外围逻辑符。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param columns 多个列名
   * @param op 操作符
   * @param value 参数值
   * @param logic 外围逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T someIf(final boolean cnd, final String[] columns, final Oper op, final Object value, final Logic logic) {
    if (cnd) {
      appendNColumnExpr(logic, columns, op, value, Logic.OR);
    }
    return self();
  }

  /**
   * 根据条件决定是否添加多列表达式，AND 连接。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param columns 多个列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  default T someIf(final boolean cnd, final String[] columns, final Oper op, final String value) {
    if (!cnd) {
      return self();
    }
    return some(columns, op, value, Logic.AND);
  }

  /**
   * 任意列 LIKE %value%（OR 连接多列），支持自定义外围逻辑符。
   * 
   * @param columns 多个列名
   * @param value 匹配值
   * @param logic 外围逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T someLike(final String[] columns, final String value, final Logic logic) {
    if (StringUtils.isBlank(value)) {
      appendOrTrue(logic);
      return self();
    }
    appendNColumnExpr(logic, columns, Oper.LIKE, "%" + value + "%", Logic.OR);
    return self();
  }

  /**
   * 任意列 LIKE %value%（OR 连接多列），AND 连接。
   * 
   * @param columns 多个列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T someLike(final String[] columns, final String value) {
    return someLike(columns, value, Logic.AND);
  }

  /**
   * 任意列前缀匹配 LIKE value%（OR 连接多列），支持自定义外围逻辑符。
   * 
   * @param columns 多个列名
   * @param value 匹配值
   * @param logic 外围逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T someStartWith(final String[] columns, final String value, final Logic logic) {
    if (StringUtils.isBlank(value)) {
      appendOrTrue(logic);
      return self();
    }
    appendNColumnExpr(logic, columns, Oper.LIKE, value + "%", Logic.OR);
    return self();
  }

  /**
   * 任意列前缀匹配 LIKE value%（OR 连接多列），AND 连接。
   * 
   * @param columns 多个列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T someStartWith(final String[] columns, final String value) {
    return someStartWith(columns, value, Logic.AND);
  }

  /**
   * 任意列后缀匹配 LIKE %value（OR 连接多列），支持自定义外围逻辑符。
   * 
   * @param columns 多个列名
   * @param value 匹配值
   * @param logic 外围逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T someEndWith(final String[] columns, final String value, final Logic logic) {
    if (StringUtils.isBlank(value)) {
      appendOrTrue(logic);
      return self();
    }
    appendNColumnExpr(logic, columns, Oper.LIKE, "%" + value, Logic.OR);
    return self();
  }

  /**
   * 任意列后缀匹配 LIKE %value（OR 连接多列），AND 连接。
   * 
   * @param columns 多个列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T someEndWith(final String[] columns, final String value) {
    return someEndWith(columns, value, Logic.AND);
  }

  /**
   * 添加多列条件表达式，如 (col1 = ? OR col2 = ?)。
   * 
   * @param logicOp 外部逻辑运算符（AND/OR）
   * @param columns 多个列名
   * @param op 操作符
   * @param value 参数值
   * @param innerLogic 内部逻辑运算符（AND/OR）
   */
  default void appendNColumnExpr(final Logic logicOp, final String[] columns, final Oper op, final Object value,
      final Logic innerLogic) {
    StringBuilder exprSb = new StringBuilder();
    exprSb.append("(");
    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        exprSb.append(" ").append(innerLogic.getLogic()).append(" ");
      }
      exprSb.append(op.makeExpr(columns[i]));
    }
    exprSb.append(")");

    appendWhere(logicOp, exprSb.toString());
    if (op.hasValue()) {
      for (int i = 0; i < columns.length; i++) {
        innerWhereHolder().addWhereParam(value);
      }
    }
  }
}
