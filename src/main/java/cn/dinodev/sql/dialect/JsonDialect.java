// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.dialect;

import cn.dinodev.sql.JsonPath;
import cn.dinodev.sql.JsonType;

/**
 * JSON/JSONB 操作方言接口（重新设计版本）。
 * <p>
 * 定义不同数据库的 JSON 操作 SQL 生成规则，支持 PostgreSQL JSONB 和 MySQL JSON。
 * 所有方法统一使用 {@link JsonType} 参数以提供类型安全和明确性。
 * <p>
 * <b>设计原则：</b>
 * <ul>
 *   <li>所有需要类型信息的方法都接受 {@link JsonType} 参数</li>
 *   <li>方法内部负责生成正确的类型转换（如 ::json, ::jsonb）</li>
 *   <li>调用方无需关心具体的 SQL 语法差异</li>
 *   <li>PostgreSQL 支持 JSON 和 JSONB 两种类型</li>
 *   <li>MySQL 统一使用 JSON 类型（忽略 JSONB）</li>
 * </ul>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>类型转换：JSON/JSONB 类型占位符生成</li>
 *   <li>合并操作：JSON 对象合并</li>
 *   <li>路径操作：设置/删除嵌套路径</li>
 *   <li>键操作：删除指定键</li>
 *   <li>数组操作：追加/前置数组元素</li>
 *   <li>工具方法：路径格式转换、null 清理</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
public interface JsonDialect {

  /**
   * 获取关联的数据库方言名称。
   * 
   * @return 方言名称
   */
  String getDialectName();

  /**
   * 生成指定类型的 JSON 类型转换表达式。
   * <p>
   * 根据 JSON 类型自动生成对应的类型转换表达式：
   * <ul>
   *   <li><b>PostgreSQL + JSON</b>: ?::json</li>
   *   <li><b>PostgreSQL + JSONB</b>: ?::jsonb</li>
   *   <li><b>MySQL</b>: ? (MySQL 不区分 JSON/JSONB，统一为 JSON 类型)</li>
   * </ul>
   * <p>
   * 使用示例：
   * <pre>{@code
   * String jsonCast = dialect.makeTypeCast(JsonType.JSON);   // PostgreSQL: ?::json, MySQL: ?
   * String jsonbCast = dialect.makeTypeCast(JsonType.JSONB); // PostgreSQL: ?::jsonb, MySQL: ?
   * }</pre>
   * 
   * @param type JSON 数据类型
   * @return JSON 类型转换表达式
   * @since 2026-01-04
   */
  String makeTypeCast(JsonType type);

  /**
   * 将 JsonPath 转换为数据库特定的路径字符串格式。
   * <p>
   * 不同数据库实现：
   * <ul>
   *   <li><b>PostgreSQL</b>: {key1,key2,0,key3}</li>
   *   <li><b>MySQL</b>: $.key1.key2[0].key3</li>
   * </ul>
   * <p>
   * 使用示例：
   * <pre>{@code
   * JsonPath path = JsonPath.of("users", 0, "name");
   * String pgPath = postgresDialect.formatPath(path);    // {users,0,name}
   * String mysqlPath = mysqlDialect.formatPath(path);    // $.users[0].name
   * }</pre>
   * 
   * @param path JSON 路径对象
   * @return 数据库特定的路径字符串
   * @since 2026-01-04
   */
  String formatPath(JsonPath path);

  /**
   * 生成 JSON 合并表达式。
   * <p>
   * 不同数据库的实现：
   * <ul>
   *   <li><b>PostgreSQL + JSON</b>: 不支持（需使用 JSONB）</li>
   *   <li><b>PostgreSQL + JSONB</b>: column || ?::jsonb</li>
   *   <li><b>MySQL</b>: JSON_MERGE_PATCH(column, ?)</li>
   * </ul>
   * 
   * @param type JSON 数据类型
   * @param column 列名或表达式
   * @return JSON 合并表达式
   * @throws UnsupportedOperationException 当数据库不支持指定类型的合并操作时
   * @since 2026-01-04
   */
  String makeJsonMerge(JsonType type, String column);

  /**
   * 生成设置 JSON 路径的 SQL 表达式（使用 JsonPath）。
   * <p>
   * 不同数据库的实现：
   * <ul>
   *   <li><b>PostgreSQL + JSON</b>: 不支持（需使用 JSONB）</li>
   *   <li><b>PostgreSQL + JSONB</b>: jsonb_set(column, '{a,b,c}', ?::jsonb, createMissing)</li>
   *   <li><b>MySQL</b>: JSON_SET(column, '$.a.b.c', ?)</li>
   * </ul>
   * <p>
   * <b>注意</b>: PostgreSQL 的 JSON 类型不支持路径设置，必须使用 JSONB。
   * 
   * @param type JSON 数据类型
   * @param column 列名或表达式
   * @param path JSON 路径对象
   * @param createMissing 是否创建缺失的路径（MySQL 总是创建，此参数仅对 PostgreSQL 有效）
   * @return JSON 路径设置表达式
   * @throws UnsupportedOperationException 当数据库不支持指定类型的路径设置操作时
   * @since 2026-01-04
   */
  String makeJsonSetPath(JsonType type, String column, JsonPath path, boolean createMissing);

  /**
   * 生成 JSON 删除键表达式。
   * <p>
   * 不同数据库的实现：
   * <ul>
   *   <li><b>PostgreSQL</b>: column - 'key'</li>
   *   <li><b>MySQL</b>: JSON_REMOVE(column, '$.key')</li>
   * </ul>
   * 
   * @param column 列名或表达式
   * @param key 要删除的键
   * @return JSON 删除键表达式
   */
  default String makeJsonRemoveKey(String column, String key) {
    throw new UnsupportedOperationException(
        getDialectName() + " does not support JSON key removal");
  }

  /**
   * 生成 JSON 删除多个键表达式。
   * <p>
   * 不同数据库的实现：
   * <ul>
   *   <li><b>PostgreSQL</b>: column - ARRAY['key1', 'key2']</li>
   *   <li><b>MySQL</b>: JSON_REMOVE(column, '$.key1', '$.key2')</li>
   * </ul>
   * 
   * @param column 列名或表达式
   * @param keys 要删除的键数组
   * @return JSON 删除多个键表达式
   */
  default String makeJsonRemoveKeys(String column, String[] keys) {
    throw new UnsupportedOperationException(
        getDialectName() + " does not support JSON multiple keys removal");
  }

  /**
   * 生成 JSON 删除路径表达式（使用 JsonPath）。
   * <p>
   * 不同数据库的实现：
   * <ul>
   *   <li><b>PostgreSQL + JSON</b>: 不支持（需使用 JSONB）</li>
   *   <li><b>PostgreSQL + JSONB</b>: column #- '{a,b,c}'</li>
   *   <li><b>MySQL</b>: JSON_REMOVE(column, '$.a.b.c')</li>
   * </ul>
   * 
   * @param type JSON 数据类型
   * @param column 列名或表达式
   * @param path JSON 路径对象
   * @return JSON 删除路径表达式
   * @throws UnsupportedOperationException 当数据库不支持指定类型的路径删除操作时
   * @since 2026-01-04
   */
  String makeJsonRemovePath(JsonType type, String column, JsonPath path);

  /**
   * 生成 JSON 数组追加表达式。
   * <p>
   * 不同数据库的实现：
   * <ul>
   *   <li><b>PostgreSQL + JSONB</b>: column || ?::jsonb</li>
   *   <li><b>MySQL</b>: JSON_ARRAY_APPEND(column, '$', ?)</li>
   * </ul>
   * <p>
   * <b>注意</b>: PostgreSQL 的 JSON 类型不支持 || 操作符，必须使用 JSONB。
   * 
   * @param type JSON 数据类型
   * @param column 列名或表达式
   * @return JSON 数组追加表达式
   * @throws UnsupportedOperationException 当数据库不支持指定类型的数组追加操作时
   */
  String makeJsonArrayAppend(JsonType type, String column);

  /**
   * 生成 JSON 数组前置表达式。
   * <p>
   * 不同数据库的实现：
   * <ul>
   *   <li><b>PostgreSQL + JSONB</b>: ?::jsonb || column</li>
   *   <li><b>MySQL</b>: JSON_ARRAY_INSERT(column, '$[0]', ?)</li>
   * </ul>
   * <p>
   * <b>注意</b>: PostgreSQL 的 JSON 类型不支持 || 操作符，必须使用 JSONB。
   * 
   * @param type JSON 数据类型
   * @param column 列名或表达式
   * @return JSON 数组前置表达式
   * @throws UnsupportedOperationException 当数据库不支持指定类型的数组前置操作时
   */
  String makeJsonArrayPrepend(JsonType type, String column);

  /**
   * 生成 JSON 清理 null 值表达式。
   * <p>
   * 不同数据库的实现：
   * <ul>
   *   <li><b>PostgreSQL + JSON</b>: json_strip_nulls(column)</li>
   *   <li><b>PostgreSQL + JSONB</b>: jsonb_strip_nulls(column)</li>
   *   <li><b>MySQL</b>: 不支持（需要自定义函数）</li>
   * </ul>
   * 
   * @param type JSON 数据类型
   * @param column 列名或表达式
   * @return JSON 清理 null 值表达式
   * @throws UnsupportedOperationException 当数据库不支持 null 清理操作时
   */
  String makeJsonStripNulls(JsonType type, String column);

}
