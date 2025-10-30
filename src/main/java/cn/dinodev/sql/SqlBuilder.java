// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql;

/**
 *
 * @author Cody Lu
 * @date 2022-03-07 19:13:51
 */

public interface SqlBuilder {
  /**
   * 获取生成的sql语句
   * @return
   */
  String getSql();

  /**
   * 获取sql语句需要的参数数组
   * @return
   */
  Object[] getParams();

}
