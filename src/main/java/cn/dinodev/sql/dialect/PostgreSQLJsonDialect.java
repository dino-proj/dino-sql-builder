// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

import cn.dinodev.sql.JsonType;

/**
 * PostgreSQL JSON/JSONB 操作方言实现（重新设计版本）。
 * <p>
 * 提供 PostgreSQL 专用的 JSON/JSONB SQL 生成逻辑，完整支持 JSON 和 JSONB 两种类型。
 * <p>
 * <h3>PostgreSQL JSON vs JSONB:</h3>
 * <ul>
 *   <li><b>JSON</b>: 文本格式，保留原始格式和空格，不支持操作符（||、-、#-）</li>
 *   <li><b>JSONB</b>: 二进制格式，自动去重和排序键，支持索引和操作符，查询性能更好（推荐）</li>
 * </ul>
 * <p>
 * <h3>重要提示:</h3>
 * <ul>
 *   <li>JSON 类型仅支持函数操作（如 json_strip_nulls），不支持操作符</li>
 *   <li>JSONB 类型支持完整功能：操作符（||、-、#-）和函数（jsonb_set、jsonb_strip_nulls 等）</li>
 *   <li>实际项目中强烈推荐使用 JSONB 类型</li>
 * </ul>
 * <p>
 * <h3>JSONB 操作符:</h3>
 * <ul>
 *   <li><b>||</b>: 合并两个 JSONB 对象或追加数组</li>
 *   <li><b>-</b>: 删除键或数组元素</li>
 *   <li><b>#-</b>: 删除指定路径</li>
 * </ul>
 * <p>
 * <h3>JSONB 函数:</h3>
 * <ul>
 *   <li><b>jsonb_set</b>: 更新指定路径的值</li>
 *   <li><b>jsonb_strip_nulls</b>: 递归删除所有 null 值</li>
 * </ul>
 *
 * @author Cody Lu
 * @since 2026-01-04
 */
public class PostgreSQLJsonDialect implements JsonDialect {

  /** 单例实例 */
  private static final PostgreSQLJsonDialect INSTANCE = new PostgreSQLJsonDialect();

  /**
   * 私有构造函数，防止外部实例化
   */
  private PostgreSQLJsonDialect() {
  }

  /**
   * 获取单例实例
   * 
   * @return PostgreSQL JSON 方言实例
   */
  public static PostgreSQLJsonDialect getInstance() {
    return INSTANCE;
  }

  @Override
  public String getDialectName() {
    return "postgresql";
  }

  /**
   * PostgreSQL JSON 类型转换表达式。
   * <p>
   * PostgreSQL 需要显式将参数转换为 JSON 类型。
   * <p>
   * <b>推荐</b>: 使用 {@link #makeTypeCast(JsonType)} 方法，API 更清晰:
   * <pre>{@code
   * String cast = dialect.makeTypeCast(JsonType.JSON); // ?::json
   * }</pre>
   * 
   * @return JSON 类型转换表达式: ?::json
   */
  @Override
  public String makeTypeCast(JsonType type) {
    return switch (type) {
      case JSON -> "?::json";
      case JSONB -> "?::jsonb";
    };
  }

  /**
   * PostgreSQL JSON 合并表达式。
   * <p>
   * 使用 || 运算符合并 JSONB 对象，右侧的值会覆盖左侧相同的键。
   * <p>
   * <b>注意</b>: JSON 类型不支持 || 操作符，必须使用 JSONB。
   * 
   * @param type JSON 数据类型
   * @param column 列名或表达式
   * @return column || ?::jsonb
   * @throws UnsupportedOperationException 当使用 JSON 类型时
   */
  @Override
  public String makeJsonMerge(JsonType type, String column) {
    if (type == JsonType.JSON) {
      throw new UnsupportedOperationException(
          "PostgreSQL JSON type does not support || operator, use JSONB instead");
    }
    return column + " || " + makeTypeCast(type);
  }

  /**
   * PostgreSQL JSON 路径设置表达式。
   * <p>
   * 使用 jsonb_set 函数更新指定路径的值。
   * <p>
   * <b>注意</b>: JSON 类型不支持路径设置，必须使用 JSONB。
   * 
   * @param type JSON 数据类型
   * @param column 列名或表达式
   * @param path JSON 路径（PostgreSQL 格式：{a,b,c}）
   * @param createMissing 是否创建缺失的路径
   * @return jsonb_set(column, '{path}', ?::jsonb, createMissing)
   * @throws UnsupportedOperationException 当使用 JSON 类型时
   */
  @Override
  public String makeJsonSetPath(JsonType type, String column, String path, boolean createMissing) {
    if (type == JsonType.JSON) {
      throw new UnsupportedOperationException(
          "PostgreSQL JSON type does not support path operations, use JSONB instead");
    }
    return "jsonb_set(" + column + ", '" + path + "', " + makeTypeCast(type) + ", " + createMissing + ")";
  }

  /**
   * PostgreSQL JSON 删除键表达式。
   * <p>
   * 使用 - 运算符删除顶层键。
   * 
   * @param column 列名或表达式
   * @param key 要删除的键
   * @return column - 'key'
   */
  @Override
  public String makeJsonRemoveKey(String column, String key) {
    return column + " - '" + key + "'";
  }

  /**
   * PostgreSQL JSON 删除多个键表达式。
   * <p>
   * 使用 - 运算符配合 ARRAY 删除多个键。
   * 
   * @param column 列名或表达式
   * @param keys 要删除的键数组
   * @return column - ARRAY['key1', 'key2']
   */
  @Override
  public String makeJsonRemoveKeys(String column, String[] keys) {
    if (keys == null || keys.length == 0) {
      return column;
    }
    StringBuilder keysArray = new StringBuilder("ARRAY[");
    for (int i = 0; i < keys.length; i++) {
      if (i > 0)
        keysArray.append(", ");
      keysArray.append("'").append(keys[i]).append("'");
    }
    keysArray.append("]");
    return column + " - " + keysArray.toString();
  }

  /**
   * PostgreSQL JSON 删除路径表达式。
   * <p>
   * 使用 #- 运算符删除指定路径。
   * 
   * @param column 列名或表达式
   * @param path JSON 路径（PostgreSQL 格式：{a,b,c}）
   * @return column #- '{path}'
   */
  @Override
  public String makeJsonRemovePath(String column, String path) {
    return column + " #- '" + path + "'";
  }

  /**
   * PostgreSQL JSON 数组追加表达式。
   * <p>
   * 使用 || 运算符追加到数组末尾。
   * <p>
   * <b>注意</b>: JSON 类型不支持 || 操作符，必须使用 JSONB。
   * 
   * @param type JSON 数据类型
   * @param column 列名或表达式
   * @return column || ?::jsonb
   * @throws UnsupportedOperationException 当使用 JSON 类型时
   */
  @Override
  public String makeJsonArrayAppend(JsonType type, String column) {
    if (type == JsonType.JSON) {
      throw new UnsupportedOperationException(
          "PostgreSQL JSON type does not support || operator, use JSONB instead");
    }
    return column + " || " + makeTypeCast(type);
  }

  /**
   * PostgreSQL JSON 数组前置表达式。
   * <p>
   * 使用 || 运算符添加到数组开头。
   * <p>
   * <b>注意</b>: JSON 类型不支持 || 操作符，必须使用 JSONB。
   * 
   * @param type JSON 数据类型
   * @param column 列名或表达式
   * @return ?::jsonb || column
   * @throws UnsupportedOperationException 当使用 JSON 类型时
   */
  @Override
  public String makeJsonArrayPrepend(JsonType type, String column) {
    if (type == JsonType.JSON) {
      throw new UnsupportedOperationException(
          "PostgreSQL JSON type does not support || operator, use JSONB instead");
    }
    return makeTypeCast(type) + " || " + column;
  }

  /**
   * PostgreSQL JSON 清理 null 值表达式。
   * <p>
   * 使用对应的函数递归删除所有 null 值。
   * <ul>
   *   <li><b>JSON</b>: json_strip_nulls(column)</li>
   *   <li><b>JSONB</b>: jsonb_strip_nulls(column)</li>
   * </ul>
   * 
   * @param type JSON 数据类型
   * @param column 列名或表达式
   * @return json_strip_nulls(column) 或 jsonb_strip_nulls(column)
   */
  @Override
  public String makeJsonStripNulls(JsonType type, String column) {
    return switch (type) {
      case JSON -> "json_strip_nulls(" + column + ")";
      case JSONB -> "jsonb_strip_nulls(" + column + ")";
    };
  }

  /**
   * 构建 PostgreSQL 混合路径（支持键和数组索引混合）。
   * <p>
   * 示例：
   * <ul>
   *   <li>"a", "b", "c" -> "{a,b,c}"</li>
   *   <li>0 -> "{0}"</li>
   *   <li>"users", 0, "name" -> "{users,0,name}"</li>
   *   <li>"data", "items", 2, "tags", 0 -> "{data,items,2,tags,0}"</li>
   * </ul>
   * 
   * @param segments 路径段序列，可以是字符串（键）或整数（数组索引）
   * @return PostgreSQL 路径字符串
   * @throws IllegalArgumentException 如果传入的类型不是 String 或 Integer
   */
  @Override
  public String makePath(Object... segments) {
    if (segments == null || segments.length == 0) {
      return "{}";
    }

    StringBuilder path = new StringBuilder("{");
    for (int i = 0; i < segments.length; i++) {
      if (i > 0) {
        path.append(",");
      }

      Object segment = segments[i];
      if (segment instanceof String) {
        path.append((String) segment);
      } else if (segment instanceof Integer) {
        path.append(segment);
      } else {
        throw new IllegalArgumentException(
            "Path segment must be String or Integer, got: " +
                (segment == null ? "null" : segment.getClass().getName()));
      }
    }
    path.append("}");
    return path.toString();
  }

}
