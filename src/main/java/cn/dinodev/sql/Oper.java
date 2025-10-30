// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql;

import cn.dinodev.sql.utils.NamingUtils;

/**
 * SQL 操作符枚举类型。
 * <p>
 * 用于表示 SQL 查询中的各种操作符，如 =、>、IN、LIKE 等。
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public enum Oper {
  /**
   * 等于
   */
  EQ("=", "%s = ?"),
  /**
   * 大于
   */
  GT(">", "%s > ?"),
  /**
   * 小于
   */
  LT("<", "%s < ?"),
  /**
   * 不等于
   */
  NE("<>", "%s <> ?"),
  /**
   * 大于等于
   */
  GTE(">=", "%s >= ?"),
  /**
   * 小于等于
   */
  LTE("<=", "%s <= ?"),
  /**
   * 模糊匹配
   */
  LIKE("LIKE", "%s LIKE ?"),
  /**
   * 不匹配
   */
  NOT_LIKE("NOT LIKE", "%s NOT LIKE ?"),
  /**
   * 包含
   */
  IN("IN", "%s IN (%s)"),
  /**
   * 不包含
   */
  NOT_IN("NOT IN", "%s NOT IN (%s)"),
  /**
   * 等于空
   */
  IS_NULL("IS NULL", "%s IS NULL"),
  /**
   * 不等于空
   */
  IS_NOT_NULL("IS NOT NULL", "%s IS NOT NULL"),
  /**
   * 范围匹配
   */
  BETWEEN("BETWEEN", "%s BETWEEN ? AND ?"),
  /**
   * 存在
   */
  EXISTS("EXISTS", "EXISTS (%s)");

  private final String op;
  private final String expr;
  private final int paramCount;
  private final int valueCount;

  /**
   * 构造方法。
   *
   * @param op 操作符字符串（如 =、>、IN 等）
   * @param expr SQL 表达式模板
   */
  Oper(String op, String expr) {
    this.op = op;
    this.expr = expr;
    this.paramCount = NamingUtils.countMatches(expr, '%');
    this.valueCount = NamingUtils.countMatches(expr, '?');
  }

  /**
   * 获取操作符字符串。
   * @return 操作符（如 =、>、IN 等）
   */
  public String getOp() {
    return op;
  }

  /**
   * 根据参数生成 SQL 表达式。
   * @param params 表达式参数
   * @return 格式化后的 SQL 表达式
   */
  public String makeExpr(final String... params) {
    if (params.length != this.paramCount) {
      throw new IllegalArgumentException(op + " need " + paramCount + " param(s), actule is " + params.length);
    }
    return String.format(expr, (Object[]) params);
  }

  /**
   * 检查操作符是否包含值占位符。
   * @return 如果包含值占位符返回 true，否则返回 false
   */
  public boolean hasValue() {
    return valueCount > 0;
  }

  @Override
  public String toString() {
    return this.op;
  }
}
