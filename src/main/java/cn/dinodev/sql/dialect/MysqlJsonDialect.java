// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

import cn.dinodev.sql.JsonType;

/**
 * MySQL JSON 操作方言实现（重新设计版本）。
 * <p>
 * 提供 MySQL 专用的 JSON SQL 生成逻辑。
 * <p>
 * <h3>MySQL JSON 特性:</h3>
 * <ul>
 *   <li><b>MySQL 5.7+</b>: 原生支持 JSON 类型，自动验证和优化</li>
 *   <li><b>MySQL 8.0+</b>: 增强 JSON 函数，支持 JSON 表达式索引</li>
 *   <li><b>不支持 JSONB</b>: MySQL 统一使用 JSON 类型，JSONB 参数会被当作 JSON 处理</li>
 * </ul>
 * <p>
 * <h3>MySQL JSON 路径语法:</h3>
 * <ul>
 *   <li><b>$</b>: 根路径</li>
 *   <li><b>$.key</b>: 对象键访问</li>
 *   <li><b>$[0]</b>: 数组索引访问</li>
 *   <li><b>$.users[0].name</b>: 嵌套访问</li>
 * </ul>
 * <p>
 * <h3>常用 JSON 函数:</h3>
 * <ul>
 *   <li><b>JSON_MERGE_PATCH</b>: 合并 JSON 对象</li>
 *   <li><b>JSON_SET</b>: 设置路径值（创建或更新）</li>
 *   <li><b>JSON_REMOVE</b>: 删除路径或键</li>
 *   <li><b>JSON_ARRAY_APPEND</b>: 追加数组元素</li>
 *   <li><b>JSON_ARRAY_INSERT</b>: 插入数组元素</li>
 * </ul>
 *
 * @author Cody Lu
 * @since 2026-01-04
 */
public class MysqlJsonDialect implements JsonDialect {

  /** 单例实例 */
  private static final MysqlJsonDialect INSTANCE = new MysqlJsonDialect();

  /**
   * 私有构造函数，防止外部实例化
   */
  private MysqlJsonDialect() {
  }

  /**
   * 获取单例实例
   * 
   * @return MySQL JSON 方言实例
   */
  public static MysqlJsonDialect getInstance() {
    return INSTANCE;
  }

  @Override
  public String getDialectName() {
    return "mysql";
  }

  /**
   * MySQL JSON 类型转换表达式。
   * <p>
   * MySQL 会自动处理 JSON 类型，直接使用占位符。
   * MySQL 不区分 JSON 和 JSONB，统一使用 JSON 类型。
   * <p>
   * 使用示例：
   * <pre>{@code
   * String cast = dialect.makeTypeCast(JsonType.JSON);   // ?
   * String cast = dialect.makeTypeCast(JsonType.JSONB); // ? (同样)
   * }</pre>
   * 
   * @param type JSON 数据类型（MySQL 忽略此参数）
   * @return JSON 类型转换表达式: ?
   */
  @Override
  public String makeTypeCast(JsonType type) {
    // MySQL 不区分 JSON 和 JSONB，统一返回 ?
    return "?";
  }

  /**
   * MySQL JSON 合并表达式。
   * <p>
   * 使用 JSON_MERGE_PATCH 函数合并 JSON 对象，右侧的值会覆盖左侧相同的键。
   * <p>
   * <b>注意</b>: MySQL 不区分 JSON 和 JSONB，统一使用 JSON 类型。
   * 
   * @param type JSON 数据类型（MySQL 忽略此参数）
   * @param column 列名或表达式
   * @return JSON_MERGE_PATCH(column, ?)
   */
  @Override
  public String makeJsonMerge(JsonType type, String column) {
    return "JSON_MERGE_PATCH(" + column + ", " + makeTypeCast(type) + ")";
  }

  /**
   * MySQL JSON 路径设置表达式。
   * <p>
   * 使用 JSON_SET 函数更新指定路径的值。
   * <p>
   * <b>注意</b>: MySQL 的 JSON_SET 总是创建缺失的路径，createM issing 参数被忽略。
   * 
   * @param type JSON 数据类型（MySQL 忽略此参数）
   * @param column 列名或表达式
   * @param path JSON 路径（MySQL 格式：$.a.b.c）
   * @param createMissing 是否创建缺失的路径（MySQL JSON_SET 总是创建）
   * @return JSON_SET(column, '$.path', ?)
   */
  @Override
  public String makeJsonSetPath(JsonType type, String column, String path, boolean createMissing) {
    return "JSON_SET(" + column + ", '" + path + "', " + makeTypeCast(type) + ")";
  }

  /**
   * MySQL JSON 删除键表达式。
   * <p>
   * 使用 JSON_REMOVE 函数删除指定键。
   * 
   * @param column 列名或表达式
   * @param key 要删除的键
   * @return JSON_REMOVE(column, '$.key')
   */
  @Override
  public String makeJsonRemoveKey(String column, String key) {
    return "JSON_REMOVE(" + column + ", '$." + key + "')";
  }

  /**
   * MySQL JSON 删除多个键表达式。
   * <p>
   * 使用 JSON_REMOVE 函数删除多个键。
   * 
   * @param column 列名或表达式
   * @param keys 要删除的键数组
   * @return JSON_REMOVE(column, '$.key1', '$.key2', ...)
   */
  @Override
  public String makeJsonRemoveKeys(String column, String[] keys) {
    if (keys == null || keys.length == 0) {
      return column;
    }
    StringBuilder result = new StringBuilder("JSON_REMOVE(" + column);
    for (String key : keys) {
      result.append(", '$.").append(key).append("'");
    }
    result.append(")");
    return result.toString();
  }

  /**
   * MySQL JSON 删除路径表达式。
   * <p>
   * 使用 JSON_REMOVE 函数删除指定路径。
   * 
   * @param column 列名或表达式
   * @param path JSON 路径（MySQL 格式：$.a.b.c）
   * @return JSON_REMOVE(column, '$.path')
   */
  @Override
  public String makeJsonRemovePath(String column, String path) {
    return "JSON_REMOVE(" + column + ", '" + path + "')";
  }

  /**
   * MySQL JSON 数组追加表达式。
   * <p>
   * 使用 JSON_ARRAY_APPEND 函数追加到数组末尾。
   * 
   * @param type JSON 数据类型（MySQL 忽略此参数）
   * @param column 列名或表达式
   * @return JSON_ARRAY_APPEND(column, '$', ?)
   */
  @Override
  public String makeJsonArrayAppend(JsonType type, String column) {
    return "JSON_ARRAY_APPEND(" + column + ", '$', " + makeTypeCast(type) + ")";
  }

  /**
   * MySQL JSON 数组前置表达式。
   * <p>
   * 使用 JSON_ARRAY_INSERT 函数插入到数组开头。
   * 
   * @param type JSON 数据类型（MySQL 忽略此参数）
   * @param column 列名或表达式
   * @return JSON_ARRAY_INSERT(column, '$[0]', ?)
   */
  @Override
  public String makeJsonArrayPrepend(JsonType type, String column) {
    return "JSON_ARRAY_INSERT(" + column + ", '$[0]', " + makeTypeCast(type) + ")";
  }

  /**
   * MySQL 不支持 JSON strip nulls 操作。
   * <p>
   * MySQL 没有内置函数来删除 JSON 中的 null 值，需要使用自定义函数或应用层处理。
   * 
   * @param type JSON 数据类型
   * @param column 列名或表达式
   * @return 不返回，直接抛出异常
   * @throws UnsupportedOperationException MySQL 不支持此操作
   */
  @Override
  public String makeJsonStripNulls(JsonType type, String column) {
    throw new UnsupportedOperationException(
        "MySQL does not support JSON strip nulls operation natively, " +
            "you need to implement a custom function or handle it in application layer");
  }

  /**
   * 构建 MySQL 混合路径（支持键和数组索引混合）。
   * <p>
   * 示例：
   * <ul>
   *   <li>"a", "b", "c" -> "$.a.b.c"</li>
   *   <li>0 -> "$[0]"</li>
   *   <li>"users", 0, "name" -> "$.users[0].name"</li>
   *   <li>"data", "items", 2, "tags", 0 -> "$.data.items[2].tags[0]"</li>
   * </ul>
   * 
   * @param segments 路径段序列，可以是字符串（键）或整数（数组索引）
   * @return MySQL 路径字符串
   * @throws IllegalArgumentException 如果传入的类型不是 String 或 Integer
   */
  @Override
  public String makePath(Object... segments) {
    if (segments == null || segments.length == 0) {
      return "$";
    }

    StringBuilder path = new StringBuilder("$");
    for (Object segment : segments) {
      if (segment instanceof String) {
        path.append(".").append((String) segment);
      } else if (segment instanceof Integer) {
        path.append("[").append(segment).append("]");
      } else {
        throw new IllegalArgumentException(
            "Path segment must be String or Integer, got: " +
                (segment == null ? "null" : segment.getClass().getName()));
      }
    }
    return path.toString();
  }

}
