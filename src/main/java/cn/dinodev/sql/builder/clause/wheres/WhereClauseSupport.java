// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.wheres;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import cn.dinodev.sql.Logic;
import cn.dinodev.sql.Oper;
import cn.dinodev.sql.SqlBuilder;
import cn.dinodev.sql.builder.clause.ClauseSupport;

/**
 * WHERE 子句支持接口，提供所有 WHERE 接口需要的底层方法。
 * 这是一个基础接口，定义了访问和修改 WHERE 条件所需的核心方法。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-11-23
 */
public interface WhereClauseSupport<T extends SqlBuilder> extends ClauseSupport<T> {

  /**
   * 获取内部的 WHERE 持有者。
   * 警告：通常不建议直接操作此对象，除非有特殊需求。
   * 
   * @return 内部 WHERE 持有者
   */
  InnerWhereHolder innerWhereHolder();

  /**
   * 将条件语句追加到 WHERE 语句中，如果之前没有语句，则忽略逻辑操作符。
   *
   * @param logicOp 逻辑操作符（AND/OR）
   * @param expr SQL表达式
   */
  default void appendWhere(final Logic logicOp, final String expr) {
    if (innerWhereHolder().isEmpty()) {
      innerWhereHolder().addWhereColumn(expr);
    } else {
      innerWhereHolder().addWhereColumn(String.format("%s (%s)", logicOp.getLogic(), expr));
    }
  }

  /**
   * 将带参数的条件语句追加到 WHERE 语句中。
   *
   * @param logicOp 逻辑操作符（AND/OR）
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   */
  default void appendWhere(final Logic logicOp, final String column, final Oper op, final Object value) {
    appendWhere(logicOp, op.makeExpr(column));
    innerWhereHolder().addWhereParam(value);
  }

  /**
   * 如果是 OR 逻辑表达式，则自动添加 OR 1=1；如果是 AND，则不添加任何内容。
   * 
   * @param logicOp 逻辑操作符（AND/OR）
   */
  default void appendOrTrue(final Logic logicOp) {
    if (logicOp == Logic.OR) {
      appendWhere(Logic.OR, "1=1");
    }
  }

  /**
   * 添加 WHERE 表达式。
   * 
   * @param expr SQL 条件表达式
   * @return 构建器本身
   */
  default T where(final String expr) {
    return and(expr);
  }

  /**
   * 添加带参数值的 WHERE 表达式。
   * 
   * @param expr SQL 条件表达式
   * @param values 参数值（可变参数）
   * @return 构建器本身
   */
  default T where(final String expr, final Object... values) {
    appendWhere(Logic.AND, expr);
    innerWhereHolder().addWhereParams(values);
    return self();
  }

  /**
   * 添加 WHERE 表达式。
   * 
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  default T where(final String column, final Oper op, final Object value) {
    where(op.makeExpr(column));
    innerWhereHolder().addWhereParam(value);
    return self();
  }

  /**
   * 添加 AND 连接的条件表达式。
   * 
   * @param expr SQL 条件表达式
   * @return 构建器本身
   */
  default T and(final String expr) {
    appendWhere(Logic.AND, expr);
    return self();
  }

  /**
   * 添加带参数值的 AND 连接条件表达式。
   * 
   * @param expr SQL 条件表达式
   * @param values 参数值（可变参数）
   * @return 构建器本身
   */
  default T and(final String expr, final Object... values) {
    and(expr);
    innerWhereHolder().addWhereParams(values);
    return self();
  }

  /**
   * 添加 AND 连接的条件表达式。
   * 
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  default T and(final String column, final Oper op, final Object value) {
    return and(String.format("%s %s ?", column, op.getOp()), value);
  }

  /**
   * 添加条件表达式，支持自定义逻辑符。
   * 
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T and(final String column, final Oper op, final Object value, final Logic logic) {
    if (logic == Logic.OR) {
      return or(String.format("%s %s ?", column, op.getOp()), value);
    } else {
      return and(String.format("%s %s ?", column, op.getOp()), value);
    }
  }

  /**
   * 添加 OR 连接的条件表达式。
   * 
   * @param expr SQL 条件表达式
   * @return 构建器本身
   */
  default T or(final String expr) {
    appendWhere(Logic.OR, expr);
    return self();
  }

  /**
   * 添加带参数值的 OR 连接条件表达式。
   * 
   * @param expr SQL 条件表达式
   * @param values 参数值（可变参数）
   * @return 构建器本身
   */
  default T or(final String expr, final Object... values) {
    or(expr);
    innerWhereHolder().addWhereParams(values);
    return self();
  }

  /**
   * 添加 OR 连接的条件表达式。
   * 
   * @param column 列名
   * @param op 操作符
   * @param value 参数值
   * @return 构建器本身
   */
  default T or(final String column, final Oper op, final Object value) {
    or(op.makeExpr(column));
    innerWhereHolder().addWhereParam(value);
    return self();
  }

  /**
   * WHERE 子句内部持有者，用于管理 WHERE 条件和参数。
   * <p>
   * 该类用于统一管理 WHERE 子句的所有元素，包括：
   * <ul>
   * <li>条件表达式列表</li>
   * <li>参数列表</li>
   * </ul>
   * <p>
   * 注意：此类为包私有，仅供 {@code cn.dinodev.sql.builder.clause} 包内使用。
   * 
   * @author Cody Lu
   * @since 2024-12-02
   */
  class InnerWhereHolder {
    private final List<String> whereColumns = new java.util.ArrayList<>();
    private final List<Object> whereParams = new java.util.ArrayList<>();

    /**
     * 添加 WHERE 条件表达式。
     * 
     * @param expr 条件表达式
     */
    public void addWhereColumn(String expr) {
      if (expr != null && !expr.trim().isEmpty()) {
        whereColumns.add(expr);
      }
    }

    /**
     * 添加 WHERE 参数。
     * 
     * @param param 参数值
     */
    public void addWhereParam(Object param) {
      whereParams.add(param);
    }

    /**
     * 批量添加 WHERE 参数。
     * 
     * @param params 参数数组
     */
    public void addWhereParams(Object... params) {
      if (params != null && params.length > 0) {
        whereParams.addAll(Arrays.asList(params));
      }
    }

    /**
     * 批量添加 WHERE 参数（Collection 版本）。
     * 
     * @param params 参数集合
     */
    public void addParamsFromCollection(Collection<?> params) {
      if (params != null && !params.isEmpty()) {
        whereParams.addAll(params);
      }
    }

    /**
     * 同时添加条件表达式和参数。
     * 
     * @param expr 条件表达式
     * @param params 参数值
     */
    public void addWhereWithParams(String expr, Object... params) {
      addWhereColumn(expr);
      addWhereParams(params);
    }

    /**
     * 判断是否为空。
     * 
     * @return 如果没有任何 WHERE 条件则返回 true
     */
    public boolean isEmpty() {
      return whereColumns.isEmpty();
    }

    /**
     * 获取条件数量。
     * 
     * @return 条件数量
     */
    public int size() {
      return whereColumns.size();
    }

    /**
     * 获取参数数量。
     * 
     * @return 参数数量
     */
    public int getParamsCount() {
      return whereParams.size();
    }

    /**
     * 构建 WHERE 子句 SQL 字符串。
     * 
     * @param sql SQL 字符串构建器
     */
    public void appendSql(StringBuilder sql) {
      if (whereColumns.isEmpty()) {
        return;
      }

      sql.append(" WHERE ");
      boolean first = true;
      for (String column : whereColumns) {
        if (!first) {
          sql.append(" ");
        }
        first = false;
        sql.append(column);
      }
    }

    /**
     * 将 WHERE 参数添加到参数列表。
     * 
     * @param params 参数列表
     */
    public void appendParams(List<Object> params) {
      params.addAll(whereParams);
    }
  }
}
