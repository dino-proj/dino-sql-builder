// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql;

/**
 * SQL 逻辑运算符枚举类型。
 * <p>
 * 用于表示 SQL 查询中的 AND/OR 逻辑。
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public enum Logic {
  /**
   * AND
   */
  AND("AND"),
  /**
   * OR
   */
  OR("OR");

  private final String logic;

  /**
   * 构造方法。
   *
   * @param logic 逻辑运算符字符串（AND 或 OR）
   */
  Logic(String logic) {
    this.logic = logic;
  }

  /**
   * 获取逻辑运算符字符串。
   *
   * @return 逻辑运算符字符串
   */
  public String getLogic() {
    return logic;
  }

  @Override
  public String toString() {
    return logic;
  }
}
