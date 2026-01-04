// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JSON 路径抽象类，提供跨数据库的统一路径表示。
 * <p>
 * JsonPath 只负责存储路径结构（对象键和数组索引），不关心具体数据库的路径格式。
 * 路径格式转换由 {@link cn.dinodev.sql.dialect.JsonDialect#formatPath(JsonPath)} 负责。
 * <p>
 * 支持链式构建路径，提供不可变的路径段列表供方言读取。
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 方式1: of() 点分隔（仅对象键）
 * JsonPath.of("address.city")
 * 
 * // 方式2: of() 混合参数（对象键 + 数组索引）
 * JsonPath.of("users", 0, "name")
 * 
 * // 方式3: of() 后继续链式调用（推荐）
 * JsonPath.of("data.items")
 *     .index(5)
 *     .key("tags")
 *     .index(2)
 * 
 * // 方式4: of() 完全链式
 * JsonPath.of()
 *     .key("order")
 *     .key("items")
 *     .index(0)
 *     .key("product")
 * }</pre>
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
public final class JsonPath {

  private final List<Object> segments;

  /**
   * 私有构造函数
   */
  private JsonPath() {
    this.segments = new ArrayList<>();
  }

  /**
   * 私有构造函数（复制）
   */
  private JsonPath(List<Object> segments) {
    this.segments = new ArrayList<>(segments);
  }

  /**
   * 创建空路径。
   * 
   * @return 空的 JsonPath
   */
  public static JsonPath of() {
    return new JsonPath();
  }

  /**
   * 便捷工厂方法：从点分隔路径创建（仅对象键）。
   * <p>
   * 示例：
   * <pre>{@code
   * JsonPath.of("address.city")  // address.city
   * JsonPath.of("user.profile.email")  // user.profile.email
   * }</pre>
   * 
   * @param path 点分隔路径，如 "address.city"
   * @return JsonPath 对象
   */
  public static JsonPath of(String path) {
    if (path == null || path.isEmpty()) {
      return of();
    }

    JsonPath result = of();
    result.addDottedPath(path);
    return result;
  }

  /**
   * 便捷工厂方法：从混合路径创建（对象键 + 数组索引）。
   * <p>
   * 示例：
   * <pre>{@code
   * JsonPath.of("users", 0, "name")  // users[0].name
   * JsonPath.of("data", "items", 5, "tags")  // data.items[5].tags
   * }</pre>
   * 
   * @param parts 路径段，可以是 String（对象键）或 Integer（数组索引）
   * @return JsonPath 对象
   * @throws IllegalArgumentException 如果路径段类型不是 String 或 Integer
   */
  public static JsonPath of(Object... parts) {
    JsonPath result = of();
    for (Object part : parts) {
      if (part instanceof String) {
        result = result.key((String) part);
      } else if (part instanceof Integer) {
        result = result.index((Integer) part);
      } else {
        throw new IllegalArgumentException(
            "Path part must be String or Integer, but got: " +
                (part == null ? "null" : part.getClass().getName()));
      }
    }
    return result;
  }

  private void addDottedPath(String dottedPath) {
    String[] keys = dottedPath.split("\\.");
    for (String key : keys) {
      if (!key.isEmpty()) {
        this.segments.add(key);
      }
    }
  }

  /**
   * 添加对象键（链式调用）。
   * <p>
   * 返回新的 JsonPath 对象，原对象不变（不可变设计）。
   * 
   * @param key 对象键
   * @return 新的 JsonPath 对象
   * @throws IllegalArgumentException 如果 key 为 null 或空字符串
   */
  public JsonPath key(String key) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Key cannot be null or empty");
    }
    this.segments.add(key);
    return this;
  }

  /**
   * 添加数组索引（链式调用）。
   * <p>
   * 返回新的 JsonPath 对象，原对象不变（不可变设计）。
   * 
   * @param index 数组索引
   * @return 新的 JsonPath 对象
   * @throws IllegalArgumentException 如果 index 为负数
   */
  public JsonPath index(int index) {
    if (index < 0) {
      throw new IllegalArgumentException("Index must be non-negative, but got: " + index);
    }
    this.segments.add(index);
    return this;
  }

  /**
   * 获取路径段列表（不可变）。
   * <p>
   * 供 JsonDialect 读取路径结构并转换为特定数据库格式。
   * 
   * @return 不可变的路径段列表
   */
  public List<Object> getSegments() {
    return Collections.unmodifiableList(segments);
  }

  /**
   * 判断是否为空路径（根路径）。
   * 
   * @return 如果路径为空返回 true
   */
  public boolean isEmpty() {
    return segments.isEmpty();
  }

  /**
   * 获取路径段数量。
   * 
   * @return 路径段数量
   */
  public int size() {
    return segments.size();
  }

  /**
   * Copy 当前 JsonPath 对象。
   * 
   * @return 新的 JsonPath 对象，内容相同
   */
  public JsonPath copy() {
    return new JsonPath(this.segments);
  }

}
