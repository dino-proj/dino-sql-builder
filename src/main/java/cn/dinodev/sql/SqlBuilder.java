// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql;

/**
 * SQL 构建器接口。
 * <p>
 * 用于生成 SQL 语句及其参数。
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public interface SqlBuilder {
  /**
   * 获取生成的 SQL 语句。
   * @return 生成的 SQL 字符串
   */
  String getSql();

  /**
   * 获取 SQL 语句所需的参数数组。
   * @return SQL 参数数组
   */
  Object[] getParams();

}
