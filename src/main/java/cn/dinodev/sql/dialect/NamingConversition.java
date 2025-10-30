// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

/**
 * 命名转换接口。
 * <p>
 * 用于将 Java 字段/表名转换为数据库命名风格。
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public interface NamingConversition {

  /**
   * 转换列的名字。
   * @param colName Java 字段名
   * @return 数据库字段名
   */
  String convertColumnName(String colName);

  /**
   * 转换表的名字。
   * @param tableName Java 表名
   * @return 数据库表名
   */
  String convertTableName(String tableName);

  /**
   * 默认命名转换实现，不进行任何转换。
   */
  class Nop implements NamingConversition {

    /**
     * 单例实例。
     */
    public static final Nop INST = new Nop();

    /**
     * 默认构造函数。
     * 创建 Nop 命名转换实现实例。
     */
    public Nop() {
    }

    @Override
    public String convertColumnName(String colName) {
      return colName;
    }

    @Override
    public String convertTableName(String tableName) {
      return tableName;
    }

  }
}
