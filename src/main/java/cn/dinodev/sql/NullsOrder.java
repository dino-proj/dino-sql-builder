// Copyright 2026 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql;

/**
 * NULLS 位置枚举（PostgreSQL 等数据库支持）。
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
public enum NullsOrder {
  /** 不指定 NULLS 排序 */
  NONE(""),
  /** NULL 值排在前面 */
  NULLS_FIRST("NULLS FIRST"),
  /** NULL 值排在后面 */
  NULLS_LAST("NULLS LAST");

  private final String sql;

  NullsOrder(String sql) {
    this.sql = sql;
  }

  /**
   * 获取 SQL 关键字。
   * 
   * @return SQL 关键字
   */
  public String getSql() {
    return sql;
  }
}