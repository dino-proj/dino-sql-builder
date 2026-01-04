// Copyright 2026 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql;

/**
 * CTE 物化提示（PostgreSQL 12+ 支持）。
 * <p>
 * 用于控制 CTE 的物化行为：
 * <ul>
 * <li>NONE - 不指定，由数据库优化器决定</li>
 * <li>MATERIALIZED - 强制物化 CTE，将结果集缓存</li>
 * <li>NOT_MATERIALIZED - 禁止物化，CTE 将内联到主查询</li>
 * </ul>
 * @author Cody Lu
 * @since 2026-01-04
 */
public enum MaterializationHint {
  /** 不指定物化提示 */
  NONE,
  /** 强制物化（MATERIALIZED） */
  MATERIALIZED,
  /** 禁止物化（NOT MATERIALIZED） */
  NOT_MATERIALIZED
}