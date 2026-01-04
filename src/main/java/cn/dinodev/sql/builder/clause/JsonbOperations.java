// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * JSONB 链式操作构建器（PostgreSQL）。
 * <p>
 * 用于构建复杂的 JSONB 操作表达式，支持链式调用多个操作。
 * 所有操作会组合成一个 SQL 表达式，在调用 {@link #apply()} 时应用到构建器。
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 方式2: 使用 try-with-resources 自动应用
 * try (var ops = builder.jsonb("settings")) {
 *     ops.merge("{\"theme\":\"dark\"}")
 *        .setPath("{notifications,email}", true)
 *        .removeKey("deprecated");
 * } // 自动应用
 * 
 * // 方式3: 使用回调自动应用（推荐）
 * builder.jsonb("settings", ops -> ops
 *     .merge("{\"theme\":\"dark\"}")
 *     .setPath("{notifications,email}", true)
 *     .removeKey("deprecated")
 * ); // 自动应用
 * }</pre>
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2026-01-03
 */
public class JsonbOperations<T> implements AutoCloseable {

  private final String column;
  private final List<JsonbOperation> operations = new ArrayList<>();
  private final BiConsumer<String, Collection<Object>> applier;
  private boolean applied = false;

  /**
   * 构造 JSONB 操作构建器。
   * 
   * @param that SQL 构建器实例
   * @param applier 用于应用 JSONB 表达式和参数的回调函数
   * @param column 要操作的列名
   */
  public JsonbOperations(BiConsumer<String, Collection<Object>> applier, String column) {
    this.applier = applier;
    this.column = column;
  }

  /**
   * 设置 JSONB 值。
   * 
   * @param value JSONB 值
   * @return 当前构建器
   */
  public JsonbOperations<T> set(Object value) {
    // 断言 applied 为 false
    assert !applied : "JSONB 操作已应用，无法继续添加操作";

    operations.add(new SetOperation(value));
    return this;
  }

  /**
   * 合并 JSONB 对象。
   * <p>
   * 使用 || 运算符合并，新值会覆盖已有的键。
   * 
   * @param value 要合并的 JSONB 对象
   * @return 当前构建器
   */
  public JsonbOperations<T> merge(Object value) {
    assert !applied : "JSONB 操作已应用，无法继续添加操作";

    operations.add(new MergeOperation(value));
    return this;
  }

  /**
   * 更新嵌套路径的值。
   * <p>
   * 使用 jsonb_set 函数更新指定路径的值。
   * 
   * @param path JSONB 路径，如 "{address,city}" 或 "{users,0,name}"
   * @param value 新值
   * @return 当前构建器
   */
  public JsonbOperations<T> setPath(String path, Object value) {
    assert !applied : "JSONB 操作已应用，无法继续添加操作";

    operations.add(new SetPathOperation(path, value, true));
    return this;
  }

  /**
   * 更新嵌套路径的值，可指定是否创建缺失路径。
   * 
   * @param path JSONB 路径
   * @param value 新值
   * @param createMissing 是否创建缺失的路径
   * @return 当前构建器
   */
  public JsonbOperations<T> setPath(String path, Object value, boolean createMissing) {
    assert !applied : "JSONB 操作已应用，无法继续添加操作";

    operations.add(new SetPathOperation(path, value, createMissing));
    return this;
  }

  /**
   * 删除指定键。
   * <p>
   * 使用 - 运算符删除顶层键。
   * 
   * @param key 要删除的键
   * @return 当前构建器
   */
  public JsonbOperations<T> removeKey(String key) {
    assert !applied : "JSONB 操作已应用，无法继续添加操作";

    operations.add(new RemoveKeyOperation(key));
    return this;
  }

  /**
   * 删除多个键。
   * 
   * @param keys 要删除的键数组
   * @return 当前构建器
   */
  public JsonbOperations<T> removeKeys(String... keys) {
    assert !applied : "JSONB 操作已应用，无法继续添加操作";

    operations.add(new RemoveKeysOperation(keys));
    return this;
  }

  /**
   * 删除嵌套路径。
   * <p>
   * 使用 #- 运算符删除指定路径的字段。
   * 
   * @param path JSONB 路径，如 "{address,city}"
   * @return 当前构建器
   */
  public JsonbOperations<T> removePath(String path) {
    assert !applied : "JSONB 操作已应用，无法继续添加操作";

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
  public JsonbOperations<T> appendArray(Object value) {
    assert !applied : "JSONB 操作已应用，无法继续添加操作";

    operations.add(new AppendArrayOperation(value));
    return this;
  }

  /**
   * 向数组开头添加元素。
   * 
   * @param value 要添加的元素
   * @return 当前构建器
   */
  public JsonbOperations<T> prependArray(Object value) {
    assert !applied : "JSONB 操作已应用，无法继续添加操作";

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
  public JsonbOperations<T> setArrayElement(int index, Object value) {
    assert !applied : "JSONB 操作已应用，无法继续添加操作";

    operations.add(new SetPathOperation("{" + index + "}", value, true));
    return this;
  }

  /**
   * 删除数组指定索引的元素。
   * 
   * @param index 数组索引（从 0 开始）
   * @return 当前构建器
   */
  public JsonbOperations<T> removeArrayElement(int index) {
    assert !applied : "JSONB 操作已应用，无法继续添加操作";

    operations.add(new RemovePathOperation("{" + index + "}"));
    return this;
  }

  /**
   * 清理所有 null 值。
   * <p>
   * 使用 jsonb_strip_nulls 函数。
   * 
   * @return 当前构建器
   */
  public JsonbOperations<T> stripNulls() {
    assert !applied : "JSONB 操作已应用，无法继续添加操作";

    operations.add(new StripNullsOperation());
    return this;
  }

  /**
   * 自动应用所有操作(AutoCloseable 接口实现)。
   * <p>
   * 用于 try-with-resources 语句自动应用操作:
   * <pre>{@code
   * try (var ops = builder.jsonb("settings")) {
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

    for (JsonbOperation op : operations) {
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
   * JSONB 操作接口。
   */
  private interface JsonbOperation {
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
  private static class SetOperation implements JsonbOperation {
    private final Object value;

    SetOperation(Object value) {
      this.value = value;
    }

    @Override
    public String buildExpression(String currentExpr) {
      return "?::jsonb";
    }

    @Override
    public List<Object> getParameters() {
      return Arrays.asList(value);
    }
  }

  /**
   * 合并操作。
   */
  private static class MergeOperation implements JsonbOperation {
    private final Object value;

    MergeOperation(Object value) {
      this.value = value;
    }

    @Override
    public String buildExpression(String currentExpr) {
      return currentExpr + " || ?::jsonb";
    }

    @Override
    public List<Object> getParameters() {
      return Arrays.asList(value);
    }
  }

  /**
   * 设置路径操作。
   */
  private static class SetPathOperation implements JsonbOperation {
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
      return "jsonb_set(" + currentExpr + ", '" + path + "', ?::jsonb, " + createMissing + ")";
    }

    @Override
    public List<Object> getParameters() {
      return Arrays.asList(value);
    }
  }

  /**
   * 删除键操作。
   */
  private static class RemoveKeyOperation implements JsonbOperation {
    private final String key;

    RemoveKeyOperation(String key) {
      this.key = key;
    }

    @Override
    public String buildExpression(String currentExpr) {
      return currentExpr + " - '" + key + "'";
    }

    @Override
    public List<Object> getParameters() {
      return new ArrayList<>();
    }
  }

  /**
   * 删除多个键操作。
   */
  private static class RemoveKeysOperation implements JsonbOperation {
    private final String[] keys;

    RemoveKeysOperation(String[] keys) {
      this.keys = keys;
    }

    @Override
    public String buildExpression(String currentExpr) {
      if (keys == null || keys.length == 0) {
        return currentExpr;
      }
      StringBuilder keysArray = new StringBuilder("ARRAY[");
      for (int i = 0; i < keys.length; i++) {
        if (i > 0)
          keysArray.append(", ");
        keysArray.append("'").append(keys[i]).append("'");
      }
      keysArray.append("]");
      return currentExpr + " - " + keysArray.toString();
    }

    @Override
    public List<Object> getParameters() {
      return new ArrayList<>();
    }
  }

  /**
   * 删除路径操作。
   */
  private static class RemovePathOperation implements JsonbOperation {
    private final String path;

    RemovePathOperation(String path) {
      this.path = path;
    }

    @Override
    public String buildExpression(String currentExpr) {
      return currentExpr + " #- '" + path + "'";
    }

    @Override
    public List<Object> getParameters() {
      return new ArrayList<>();
    }
  }

  /**
   * 数组追加操作。
   */
  private static class AppendArrayOperation implements JsonbOperation {
    private final Object value;

    AppendArrayOperation(Object value) {
      this.value = value;
    }

    @Override
    public String buildExpression(String currentExpr) {
      return currentExpr + " || ?::jsonb";
    }

    @Override
    public List<Object> getParameters() {
      return Arrays.asList(value);
    }
  }

  /**
   * 数组前置操作。
   */
  private static class PrependArrayOperation implements JsonbOperation {
    private final Object value;

    PrependArrayOperation(Object value) {
      this.value = value;
    }

    @Override
    public String buildExpression(String currentExpr) {
      return "?::jsonb || " + currentExpr;
    }

    @Override
    public List<Object> getParameters() {
      return Arrays.asList(value);
    }
  }

  /**
   * 清理 null 值操作。
   */
  private static class StripNullsOperation implements JsonbOperation {
    @Override
    public String buildExpression(String currentExpr) {
      return "jsonb_strip_nulls(" + currentExpr + ")";
    }

    @Override
    public List<Object> getParameters() {
      return new ArrayList<>();
    }
  }
}
