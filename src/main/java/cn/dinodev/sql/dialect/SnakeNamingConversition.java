// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

import java.util.Map;
import java.util.WeakHashMap;

import cn.dinodev.sql.utils.NamingUtils;

/**
 * 蛇形命名转换器。
 * <p>
 * 用于将 Java 驼峰命名转换为数据库蛇形命名格式，例如 userName -&gt; user_name。
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public class SnakeNamingConversition implements NamingConversition {

  private static final Map<String, String> NAMING_CACHE = new WeakHashMap<>(1000);

  /**
   * 默认构造函数。
   * 用于创建 SnakeNamingConversition 实例。
   */
  public SnakeNamingConversition() {
  }

  /**
   * 将字段名转换为蛇形命名。
   *
   * @param colName Java 字段名（驼峰风格）
   * @return 数据库字段名（蛇形风格）
   */
  @Override
  public String convertColumnName(String colName) {
    var val = NAMING_CACHE.get(colName);
    if (val == null) {
      val = NamingUtils.toSnake(colName);
      NAMING_CACHE.put(colName, val);
    }
    return val;
  }

  /**
   * 将表名转换为蛇形命名。
   *
   * @param tableName Java 表名（驼峰风格）
   * @return 数据库表名（蛇形风格）
   */
  @Override
  public String convertTableName(String tableName) {
    return convertColumnName(tableName);
  }

}
