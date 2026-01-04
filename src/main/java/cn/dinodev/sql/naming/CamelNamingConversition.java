// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.naming;

import java.util.Map;
import java.util.WeakHashMap;

import cn.dinodev.sql.utils.StringUtils;

/**
 * 驼峰命名转换器。
 * <p>
 * 用于将数据库字段名转换为 Java 驼峰命名格式，例如 user_name -&gt; userName。
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public class CamelNamingConversition implements NamingConversition {

  private final Map<String, String> NAMING_CACHE = new WeakHashMap<>(1000);

  /**
   * 默认构造函数。
   * 用于创建 CamelNamingConversition 实例。
   */
  public CamelNamingConversition() {
  }

  /**
   * 将字段名转换为驼峰命名。
   *
   * @param colName 数据库字段名（蛇形风格）
   * @return Java 字段名（驼峰风格）
   */
  @Override
  public String convertColumnName(String colName) {
    var val = NAMING_CACHE.get(colName);
    if (val == null) {
      val = StringUtils.toCamel(colName);
      NAMING_CACHE.put(colName, val);
    }
    return val;
  }

  /**
   * 将表名转换为驼峰命名。
   *
   * @param tableName 数据库表名（蛇形风格）
   * @return Java 表名（驼峰风格）
   */
  @Override
  public String convertTableName(String tableName) {
    return convertColumnName(tableName);
  }

}
