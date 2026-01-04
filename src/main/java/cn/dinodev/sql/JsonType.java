// Copyright 2026 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql;

/**
 * JSON 数据类型枚举。
 * <p>
 * 用于区分不同的 JSON 存储格式：
 * <ul>
 *   <li><b>JSON</b>: 文本格式，保留原始格式和空格</li>
 *   <li><b>JSONB</b>: 二进制格式 (PostgreSQL 特有)，支持索引和高效查询</li>
 * </ul>
 * <p>
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
public enum JsonType {
  /** 文本格式 JSON */
  JSON,
  /** 二进制格式 JSON (PostgreSQL JSONB) */
  JSONB
}