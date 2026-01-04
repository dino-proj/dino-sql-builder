// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import cn.dinodev.sql.JsonType;
import cn.dinodev.sql.dialect.JsonDialect;

/**
 * JSON/JSONB 链式操作构建器。
 * <p>
 * 用于构建复杂的 JSON 操作表达式，支持链式调用多个操作。
 * 所有操作会组合成一个 SQL 表达式，在调用 {@link #close()} 时自动应用到构建器。
 * <p>
 * 支持 PostgreSQL JSONB 和 MySQL JSON 两种数据库方言，自动适配不同的 SQL 语法。
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 方式1: 使用 try-with-resources 自动应用
 * try (var ops = builder.json("settings")) {
 *     ops.merge("{\"theme\":\"dark\"}")
 *        .setPath("{notifications,email}", true)
 *        .removeKey("deprecated");
 * } // 自动应用
 * 
 * // 方式2: 使用回调自动应用（推荐）
 * builder.json("settings", ops -> ops
 *     .merge("{\"theme\":\"dark\"}")
 *     .setPath("{notifications,email}", true)
 *     .removeKey("deprecated")
 * ); // 自动应用
 * 
 * // 跨数据库支持
 * // PostgreSQL: 生成 settings || ?::jsonb
 * // MySQL:      生成 JSON_MERGE_PATCH(settings, ?)
 * }</pre>
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2026-01-04
 */
public class JsonOperations<T> implements AutoCloseable {

  private final JsonDialect dialect;
  private final JsonType jsonType;
  private final String column;
  private final List<JsonOperation> operations = new ArrayList<>();
  private final BiConsumer<String, Collection<Object>> applier;
  private boolean applied = false;

  /**
   * 构造 JSON 操作构建器。
   * 
   * @param dialect JSON 操作方言
   * @param jsonType JSON 数据类型
   * @param applier 用于应用 JSON 表达式和参数的回调函数
   * @param column 要操作的列名
   */
  public JsonOperations(final JsonDialect dialect, final JsonType jsonType,
      BiConsumer<String, Collection<Object>> applier, String column) {
    this.dialect = dialect;
    this.jsonType = jsonType;
    this.applier = applier;
    this.column = column;
  }

  /**
   * 设置 JSON 值（完全替换）。
   * 
   * @param value JSON 值
   * @return 当前构建器
   */
  public JsonOperations<T> set(Object value) {
    assert !applied : "JSON 操作已应用，无法继续添加操作";

    operations.add(new SetOperation(value));
    return this;
  }

  /**
   * 合并 JSON 对象。
   * <p>
   * 不同数据库实现：
   * <ul>
   *   <li><b>PostgreSQL</b>: 使用 || 运算符合并</li>
   *   <li><b>MySQL</b>: 使用 JSON_MERGE_PATCH 函数</li>
   * </ul>
   * 新值会覆盖已有的键。
   * 
   * @param value 要合并的 JSON 对象
   * @return 当前构建器
   */
  public JsonOperations<T> merge(Object value) {
    assert !applied : "JSON 操作已应用，无法继续添加操作";

    operations.add(new MergeOperation(value));
    return this;
  }

  /**
   * 更新嵌套路径的值。
   * <p>
   * 不同数据库实现：
   * <ul>
   *   <li><b>PostgreSQL</b>: jsonb_set(column, '{path}', value, true)</li>
   *   <li><b>MySQL</b>: JSON_SET(column, '$.path', value)</li>
   * </ul>
   * 
   * @param path JSON 路径（PostgreSQL 格式），如 "{address,city}" 或 "{users,0,name}"
   * @param value 新值
   * @return 当前构建器
   */
  public JsonOperations<T> setPath(String path, Object value) {
    assert !applied : "JSON 操作已应用，无法继续添加操作";

    operations.add(new SetPathOperation(path, value, true));
    return this;
  }

  /**
   * 更新嵌套路径的值，可指定是否创建缺失路径。
   * <p>
   * 注意：MySQL 的 JSON_SET 总是创建缺失的路径，createMissing 参数仅对 PostgreSQL 有效。
   * 
   * @param path JSON 路径（PostgreSQL 格式）
   * @param value 新值
   * @param createMissing 是否创建缺失的路径（仅 PostgreSQL）
   * @return 当前构建器
   */
  public JsonOperations<T> setPath(String path, Object value, boolean createMissing) {
    assert !applied : "JSON 操作已应用，无法继续添加操作";

    operations.add(new SetPathOperation(path, value, createMissing));
    return this;
  }

  /**
   * 删除指定键。
   * <p>
   * 不同数据库实现：
   * <ul>
   *   <li><b>PostgreSQL</b>: column - 'key'</li>
   *   <li><b>MySQL</b>: JSON_REMOVE(column, '$.key')</li>
   * </ul>
   * 
   * @param key 要删除的键
   * @return 当前构建器
   */
  public JsonOperations<T> removeKey(String key) {
    assert !applied : "JSON 操作已应用，无法继续添加操作";

    operations.add(new RemoveKeyOperation(key));
    return this;
  }

  /**
   * 删除多个键。
   * 
   * @param keys 要删除的键数组
   * @return 当前构建器
   */
  public JsonOperations<T> removeKeys(String... keys) {
    assert !applied : "JSON 操作已应用，无法继续添加操作";

    operations.add(new RemoveKeysOperation(keys));
    return this;
  }

  /**
   * 删除嵌套路径。
   * <p>
   * 不同数据库实现：
   * <ul>
   *   <li><b>PostgreSQL</b>: column #- '{path}'</li>
   *   <li><b>MySQL</b>: JSON_REMOVE(column, '$.path')</li>
   * </ul>
   * 
   * @param path JSON 路径（PostgreSQL 格式），如 "{address,city}"
   * @return 当前构建器
   */
  public JsonOperations<T> removePath(String path) {
    assert !applied : "JSON 操作已应用，无法继续添加操作";

    operations.add(new RemovePathOperation(path));
    return this;
  }

  /**
   * 向数组末尾追加元素。
   * <p>
   * 使用 || 运算符追加。
   * 
   * @param value 要追加的元素
   * @return 当前构建器
   */
  public JsonOperations<T> appendArray(Object value) {
    assert !applied : "JSON 操作已应用，无法继续添加操作";

    operations.add(new AppendArrayOperation(value));
    return this;
  }

  /**
   * 向数组开头添加元素。
   * 
   * @param value 要添加的元素
   * @return 当前构建器
   */
  public JsonOperations<T> prependArray(Object value) {
    assert !applied : "JSON 操作已应用，无法继续添加操作";

    operations.add(new PrependArrayOperation(value));
    return this;
  }

  /**
   * 更新数组指定索引的元素。
   * 
   * @param index 数组索引（从 0 开始）
   * @param value 新值
   * @return 当前构建器
   */
  public JsonOperations<T> setArrayElement(int index, Object value) {
    assert !applied : "JSON 操作已应用，无法继续添加操作";

    operations.add(new SetPathOperation(dialect.makePath(index), value, true));
    return this;
  }

  /**
   * 删除数组指定索引的元素。
   * 
   * @param index 数组索引（从 0 开始）
   * @return 当前构建器
   */
  public JsonOperations<T> removeArrayElement(int index) {
    assert !applied : "JSON 操作已应用，无法继续添加操作";

    operations.add(new RemovePathOperation(dialect.makePath(index)));
    return this;
  }

  /**
   * 清理所有 null 值。
   * <p>
   * 使用 jsonb_strip_nulls 函数。
   * 
   * @return 当前构建器
   */
  public JsonOperations<T> stripNulls() {
    assert !applied : "JSON 操作已应用，无法继续添加操作";

    operations.add(new StripNullsOperation());
    return this;
  }

  /**
   * 自动应用所有操作(AutoCloseable 接口实现)。
   * <p>
   * 用于 try-with-resources 语句自动应用操作:
   * <pre>{@code
   * try (var ops = builder.json("settings")) {
   *     ops.merge("{\"theme\":\"dark\"}").removeKey("old");
   * } // 自动调用 close() 应用操作
   * }</pre>
   */
  @Override
  public void close() {
    if (applied || operations.isEmpty()) {
      return;
    }

    // 构建完整的 SQL 表达式
    StringBuilder expr = new StringBuilder(column);
    List<Object> params = new ArrayList<>();

    for (JsonOperation op : operations) {
      expr = new StringBuilder(op.buildExpression(expr.toString()));
      params.addAll(op.getParameters());
    }

    // 应用到构建器
    if (params.isEmpty()) {
      applier.accept(expr.toString(), null);
    } else {
      applier.accept(expr.toString(), params);
    }

    applied = true;
  }

  // ==================== 内部操作接口和实现 ====================

  /**
   * JSON 操作接口。
   */
  private interface JsonOperation {
    /**
     * 构建 SQL 表达式。
     * 
     * @param currentExpr 当前表达式
     * @return 新的表达式
     */
    String buildExpression(String currentExpr);

    /**
     * 获取操作的参数列表。
     * 
     * @return 参数列表
     */
    List<Object> getParameters();
  }

  /**
   * 设置值操作。
   */
  private class SetOperation implements JsonOperation {
    private final Object value;

    SetOperation(Object value) {
      this.value = value;
    }

    @Override
    public String buildExpression(String currentExpr) {
      return dialect.makeTypeCast(jsonType);
    }

    @Override
    public List<Object> getParameters() {
      return Arrays.asList(value);
    }
  }

  /**
   * 合并操作。
   */
  private class MergeOperation implements JsonOperation {
    private final Object value;

    MergeOperation(Object value) {
      this.value = value;
    }

    @Override
    public String buildExpression(String currentExpr) {
      return dialect.makeJsonMerge(jsonType, currentExpr);
    }

    @Override
    public List<Object> getParameters() {
      return Arrays.asList(value);
    }
  }

  /**
   * 设置路径操作。
   */
  private class SetPathOperation implements JsonOperation {
    private final String path;
    private final Object value;
    private final boolean createMissing;

    SetPathOperation(String path, Object value, boolean createMissing) {
      this.path = path;
      this.value = value;
      this.createMissing = createMissing;
    }

    @Override
    public String buildExpression(String currentExpr) {
      return dialect.makeJsonSetPath(jsonType, currentExpr, path, createMissing);
    }

    @Override
    public List<Object> getParameters() {
      return Arrays.asList(value);
    }
  }

  /**
   * 删除键操作。
   */
  private class RemoveKeyOperation implements JsonOperation {
    private final String key;

    RemoveKeyOperation(String key) {
      this.key = key;
    }

    @Override
    public String buildExpression(String currentExpr) {
      return dialect.makeJsonRemoveKey(currentExpr, key);
    }

    @Override
    public List<Object> getParameters() {
      return new ArrayList<>();
    }
  }

  /**
   * 删除多个键操作。
   */
  private class RemoveKeysOperation implements JsonOperation {
    private final String[] keys;

    RemoveKeysOperation(String[] keys) {
      this.keys = keys;
    }

    @Override
    public String buildExpression(String currentExpr) {
      return dialect.makeJsonRemoveKeys(currentExpr, keys);
    }

    @Override
    public List<Object> getParameters() {
      return new ArrayList<>();
    }
  }

  /**
   * 删除路径操作。
   */
  private class RemovePathOperation implements JsonOperation {
    private final String path;

    RemovePathOperation(String path) {
      this.path = path;
    }

    @Override
    public String buildExpression(String currentExpr) {
      return dialect.makeJsonRemovePath(currentExpr, path);
    }

    @Override
    public List<Object> getParameters() {
      return new ArrayList<>();
    }
  }

  /**
   * 数组追加操作。
   */
  private class AppendArrayOperation implements JsonOperation {
    private final Object value;

    AppendArrayOperation(Object value) {
      this.value = value;
    }

    @Override
    public String buildExpression(String currentExpr) {
      return dialect.makeJsonArrayAppend(jsonType, currentExpr);
    }

    @Override
    public List<Object> getParameters() {
      return Arrays.asList(value);
    }
  }

  /**
   * 数组前置操作。
   */
  private class PrependArrayOperation implements JsonOperation {
    private final Object value;

    PrependArrayOperation(Object value) {
      this.value = value;
    }

    @Override
    public String buildExpression(String currentExpr) {
      return dialect.makeJsonArrayPrepend(jsonType, currentExpr);
    }

    @Override
    public List<Object> getParameters() {
      return Arrays.asList(value);
    }
  }

  /**
   * 清理 null 值操作。
   */
  private class StripNullsOperation implements JsonOperation {
    @Override
    public String buildExpression(String currentExpr) {
      return dialect.makeJsonStripNulls(jsonType, currentExpr);
    }

    @Override
    public List<Object> getParameters() {
      return new ArrayList<>();
    }
  }

}
