// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import cn.dinodev.sql.SqlBuilder;

/**
 * UPDATE SET 子句接口，提供更新数据相关的方法。
 * <p>
 * 支持设置列值、条件设置、表达式更新等功能。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-12-04
 */
public interface UpdateSetClause<T extends SqlBuilder> extends ClauseSupport<T> {

  /**
   * 获取内部的 UPDATE SET 持有者。
   * 警告：通常不建议直接操作此对象，除非有特殊需求。
   * 
   * @return 内部 UPDATE SET 持有者
   */
  InnerUpdateSetHolder innerUpdateSetHolder();

  /**
   * 设置列值但不添加参数（用于函数调用、表达式等）。
   * <p>
   * 该方法用于设置不需要参数的表达式，如函数调用 "NOW()"、常量值或计算表达式。
   * 表达式应该是完整的赋值语句，如 "status = 0" 或 "updated_at = NOW()"。
   * 
   * @param expression 完整的赋值表达式
   * @return 构建器本身
   */
  default T set(String expression) {
    innerUpdateSetHolder().addSetWithoutParam(expression);
    return self();
  }

  /**
   * 条件设置列值但不添加参数。
   * 
   * @param condition 条件
   * @param expression 完整的赋值表达式
   * @return 构建器本身
   */
  default T setIf(boolean condition, String expression) {
    if (condition) {
      set(expression);
    }
    return self();
  }

  /**
   * 设置列值（使用占位符）。
   * <p>
   * 如果表达式包含 '?'，直接使用该表达式（如 "name = ?"）；
   * 否则自动添加 '= ?'（如传入 "name" 将生成 "name = ?"）。
   * 
   * @param expression 列表达式（可能已包含 '= ?' 或仅列名）
   * @param value 值
   * @return 构建器本身
   */
  default T set(String expression, Object value) {
    innerUpdateSetHolder().addSet(expression, value);
    return self();
  }

  /**
   * 设置列值(使用多个参数)。
   * <p>
   * 用于支持包含多个占位符的表达式。
   * 
   * @param expression 包含占位符的表达式
   * @param params 参数集合
   * @return 构建器本身
   */
  default T set(String expression, Collection<Object> params) {
    innerUpdateSetHolder().addSet(expression, params);
    return self();
  }

  /**
   * 设置列值（使用表达式）。
   * 
   * @param column 列名
   * @param expression 值表达式
   * @param value 参数值
   * @return 构建器本身
   */
  default T set(String column, String expression, Object value) {
    innerUpdateSetHolder().addSetWithExpression(column, expression, value);
    return self();
  }

  /**
   * 条件设置列值。
   * 
   * @param condition 条件
   * @param expression 列表达式
   * @param value 值
   * @return 构建器本身
   */
  default T setIf(boolean condition, String expression, Object value) {
    if (condition) {
      set(expression, value);
    }
    return self();
  }

  /**
   * 值不为 null 时设置列值。
   * 
   * @param expression 列表达式
   * @param value 值
   * @return 构建器本身
   */
  default T setIfNotNull(String expression, Object value) {
    if (value != null) {
      set(expression, value);
    }
    return self();
  }

  /**
   * 条件设置列值（使用表达式）。
   * 
   * @param condition 条件
   * @param column 列名
   * @param expression 值表达式
   * @param value 参数值
   * @return 构建器本身
   */
  default T setIf(boolean condition, String column, String expression, Object value) {
    if (condition) {
      set(column, expression, value);
    }
    return self();
  }

  /**
   * 值不为 null 时设置列值（使用表达式）。
   * 
   * @param column 列名
   * @param expression 值表达式
   * @param value 参数值
   * @return 构建器本身
   */
  default T setIfNotNull(String column, String expression, Object value) {
    if (value != null) {
      set(column, expression, value);
    }
    return self();
  }

  /**
   * 批量设置列值。
   * 
   * @param values 列名-值映射
   * @return 构建器本身
   */
  default T setMap(Map<String, Object> values) {
    if (values != null) {
      values.forEach(this::set);
    }
    return self();
  }

  /**
   * 条件批量设置列值。
   * 
   * @param condition 条件
   * @param values 列名-值映射
   * @return 构建器本身
   */
  default T setMapIf(boolean condition, Map<String, Object> values) {
    if (condition && values != null) {
      values.forEach(this::set);
    }
    return self();
  }

  /**
   * 列值自增 1。
   * 
   * @param column 列名
   * @return 构建器本身
   */
  default T increment(String column) {
    return increment(column, 1);
  }

  /**
   * 列值自增指定值。
   * 
   * @param column 列名
   * @param increment 增量
   * @return 构建器本身
   */
  default T increment(String column, long increment) {
    return set(column + " = " + column + " + " + increment);
  }

  /**
   * 条件列值自增 1。
   * 
   * @param condition 条件
   * @param column 列名
   * @return 构建器本身
   */
  default T incrementIf(boolean condition, String column) {
    if (condition) {
      return increment(column);
    }
    return self();
  }

  /**
   * 条件列值自增指定值。
   * 
   * @param condition 条件
   * @param column 列名
   * @param increment 增量
   * @return 构建器本身
   */
  default T incrementIf(boolean condition, String column, long increment) {
    if (condition) {
      return increment(column, increment);
    }
    return self();
  }

  /**
   * 列值自减 1。
   * 
   * @param column 列名
   * @return 构建器本身
   */
  default T decrement(String column) {
    return decrement(column, 1);
  }

  /**
   * 列值自减指定值。
   * 
   * @param column 列名
   * @param decrement 减量
   * @return 构建器本身
   */
  default T decrement(String column, long decrement) {
    return set(column + " = " + column + " - " + decrement);
  }

  /**
   * 条件列值自减 1。
   * 
   * @param condition 条件
   * @param column 列名
   * @return 构建器本身
   */
  default T decrementIf(boolean condition, String column) {
    if (condition) {
      return decrement(column);
    }
    return self();
  }

  /**
   * 条件列值自减指定值。
   * 
   * @param condition 条件
   * @param column 列名
   * @param decrement 减量
   * @return 构建器本身
   */
  default T decrementIf(boolean condition, String column, long decrement) {
    if (condition) {
      return decrement(column, decrement);
    }
    return self();
  }

  /**
   * 列值乘法运算。
   * 
   * @param column 列名
   * @param multiplier 乘数
   * @return 构建器本身
   */
  default T multiply(String column, double multiplier) {
    return set(column + " = " + column + " * " + multiplier);
  }

  /**
   * 列值除法运算。
   * 
   * @param column 列名
   * @param divisor 除数
   * @return 构建器本身
   */
  default T divide(String column, double divisor) {
    return set(column + " = " + column + " / " + divisor);
  }

  /**
   * 列值取模运算。
   * 
   * @param column 列名
   * @param modulus 模数
   * @return 构建器本身
   */
  default T mod(String column, long modulus) {
    return set(column + " = " + column + " % " + modulus);
  }

  /**
   * 设置为当前时间戳。
   * 
   * @param column 列名
   * @return 构建器本身
   */
  default T setNow(String column) {
    return set(column + " = NOW()");
  }

  /**
   * 条件设置为当前时间戳。
   * 
   * @param condition 条件
   * @param column 列名
   * @return 构建器本身
   */
  default T setNowIf(boolean condition, String column) {
    if (condition) {
      return setNow(column);
    }
    return self();
  }

  /**
   * 设置为 NULL。
   * 
   * @param column 列名
   * @return 构建器本身
   */
  default T setNull(String column) {
    return set(column + " = NULL");
  }

  /**
   * 条件设置为 NULL。
   * 
   * @param condition 条件
   * @param column 列名
   * @return 构建器本身
   */
  default T setNullIf(boolean condition, String column) {
    if (condition) {
      return setNull(column);
    }
    return self();
  }

  /**
   * 设置为默认值。
   * <p>
   * 使用 DEFAULT 关键字将列设置为其定义的默认值。
   * 
   * @param column 列名
   * @return 构建器本身
   */
  default T setDefault(String column) {
    return set(column + " = DEFAULT");
  }

  /**
   * 条件设置为默认值。
   * 
   * @param condition 条件
   * @param column 列名
   * @return 构建器本身
   */
  default T setDefaultIf(boolean condition, String column) {
    if (condition) {
      return setDefault(column);
    }
    return self();
  }

  // ==================== JSON/JSONB 操作 ====================

  /**
   * 使用回调方式执行 JSONB 链式操作（PostgreSQL）。
   * <p>
   * 此方法接受一个 Consumer 回调，在回调执行完毕后自动应用操作，无需显式调用 apply()。
   * 这是最推荐的使用方式，代码更简洁。
   * <p>
   * 使用示例：
   * <pre>{@code
   * builder.jsonb("settings", ops -> ops
   *     .merge("{\"theme\":\"dark\"}")
   *     .setPath("{notifications,email}", true)
   *     .removeKey("deprecated")
   *     .stripNulls()
   * ); // 自动应用，无需调用 apply()
   * }</pre>
   * 
   * @param column 要操作的列名
   * @param operations JSONB 操作回调函数
   * @return 构建器本身
   * @see JsonbOperations
   * @see #jsonb(String)
   */
  default T jsonb(String column, Consumer<JsonbOperations<T>> operations) {
    JsonbOperations<T> ops = new JsonbOperations<T>(
        (expr, params) -> {
          if (params == null || params.isEmpty()) {
            set(column + " = " + expr);
          } else {
            set(column + " = " + expr, params);
          }
        }, column);
    operations.accept(ops);
    ops.close(); // 自动应用
    return self();
  }

  /**
   * 条件执行 JSONB 链式操作。
   * 
   * @param condition 条件
   * @param column 要操作的列名
   * @param operations JSONB 操作回调函数
   * @return 构建器本身
   */
  default T jsonbIf(boolean condition, String column, Consumer<JsonbOperations<T>> operations) {
    if (condition) {
      return jsonb(column, operations);
    }
    return self();
  }

  /**
   * 设置 JSON 类型列值（PostgreSQL）。
   * 
   * @param column 列名
   * @param value JSON 值
   * @return 构建器本身
   */
  default T setJson(String column, Object value) {
    return set(column, "?::json", value);
  }

  /**
   * 条件设置 JSON 类型列值。
   * 
   * @param condition 条件
   * @param column 列名
   * @param value JSON 值
   * @return 构建器本身
   */
  default T setJsonIf(boolean condition, String column, Object value) {
    if (condition) {
      return setJson(column, value);
    }
    return self();
  }

  /**
   * 值不为 null 时设置 JSON 类型列值。
   * 
   * @param column 列名
   * @param value JSON 值
   * @return 构建器本身
   */
  default T setJsonIfNotNull(String column, Object value) {
    if (value != null) {
      return setJson(column, value);
    }
    return self();
  }

  /**
   * 设置 JSONB 类型列值（PostgreSQL）。
   * 
   * @param column 列名
   * @param value JSONB 值
   * @return 构建器本身
   */
  default T jsonbSet(String column, Object value) {
    return set(column, "?::jsonb", value);
  }

  /**
   * 条件设置 JSONB 类型列值。
   * 
   * @param condition 条件
   * @param column 列名
   * @param value JSONB 值
   * @return 构建器本身
   */
  default T jsonbSetIf(boolean condition, String column, Object value) {
    if (condition) {
      return jsonbSet(column, value);
    }
    return self();
  }

  /**
   * 值不为 null 时设置 JSONB 类型列值。
   * 
   * @param column 列名
   * @param value JSONB 值
   * @return 构建器本身
   */
  default T jsonbSetIfNotNull(String column, Object value) {
    if (value != null) {
      return jsonbSet(column, value);
    }
    return self();
  }

  /**
   * 更新 JSONB 字段的嵌套路径值（PostgreSQL）。
   * <p>
   * 使用 jsonb_set 函数更新 JSONB 对象中指定路径的值。
   * 路径格式：{key1,key2,key3} 表示嵌套层级。
   * 
   * @param column 列名
   * @param path JSONB 路径，如 "{address,city}" 或 "{users,0,name}"
   * @param value 新值
   * @return 构建器本身
   */
  default T jsonbSetPath(String column, String path, Object value) {
    return set(column, "jsonb_set(" + column + ", '" + path + "', ?::jsonb)", value);
  }

  /**
   * 条件更新 JSONB 字段的嵌套路径值。
   * 
   * @param condition 条件
   * @param column 列名
   * @param path JSONB 路径
   * @param value 新值
   * @return 构建器本身
   */
  default T jsonbSetPathIf(boolean condition, String column, String path, Object value) {
    if (condition) {
      return jsonbSetPath(column, path, value);
    }
    return self();
  }

  /**
   * 值不为 null 时更新 JSONB 字段的嵌套路径值。
   * 
   * @param column 列名
   * @param path JSONB 路径
   * @param value 新值
   * @return 构建器本身
   */
  default T jsonbSetPathIfNotNull(String column, String path, Object value) {
    if (value != null) {
      return jsonbSetPath(column, path, value);
    }
    return self();
  }

  /**
   * 更新 JSONB 字段的嵌套路径值，路径不存在时创建（PostgreSQL）。
   * 
   * @param column 列名
   * @param path JSONB 路径
   * @param value 新值
   * @param createMissing 是否创建缺失的路径，默认 true
   * @return 构建器本身
   */
  default T jsonbSetPath(String column, String path, Object value, boolean createMissing) {
    return set(column, "jsonb_set(" + column + ", '" + path + "', ?::jsonb, " + createMissing + ")", value);
  }

  /**
   * 合并 JSONB 对象（PostgreSQL）。
   * <p>
   * 使用 || 运算符合并两个 JSONB 对象，新值会覆盖已有的键。
   * 
   * @param column 列名
   * @param value 要合并的 JSONB 对象
   * @return 构建器本身
   */
  default T jsonbMerge(String column, Object value) {
    return set(column, column + " || ?::jsonb", value);
  }

  /**
   * 条件合并 JSONB 对象。
   * 
   * @param condition 条件
   * @param column 列名
   * @param value 要合并的 JSONB 对象
   * @return 构建器本身
   */
  default T jsonbMergeIf(boolean condition, String column, Object value) {
    if (condition) {
      return jsonbMerge(column, value);
    }
    return self();
  }

  /**
   * 值不为 null 时合并 JSONB 对象。
   * 
   * @param column 列名
   * @param value 要合并的 JSONB 对象
   * @return 构建器本身
   */
  default T jsonbMergeIfNotNull(String column, Object value) {
    if (value != null) {
      return jsonbMerge(column, value);
    }
    return self();
  }

  /**
   * 删除 JSONB 对象的指定键（PostgreSQL）。
   * <p>
   * 使用 - 运算符删除顶层键。
   * 
   * @param column 列名
   * @param key 要删除的键
   * @return 构建器本身
   */
  default T jsonbRemoveKey(String column, String key) {
    return set(column + " = " + column + " - '" + key + "'");
  }

  /**
   * 条件删除 JSONB 对象的指定键。
   * 
   * @param condition 条件
   * @param column 列名
   * @param key 要删除的键
   * @return 构建器本身
   */
  default T jsonbRemoveKeyIf(boolean condition, String column, String key) {
    if (condition) {
      return jsonbRemoveKey(column, key);
    }
    return self();
  }

  /**
   * 删除 JSONB 对象的多个键（PostgreSQL）。
   * 
   * @param column 列名
   * @param keys 要删除的键数组
   * @return 构建器本身
   */
  default T jsonbRemoveKeys(String column, String... keys) {
    if (keys == null || keys.length == 0) {
      return self();
    }
    StringBuilder keysArray = new StringBuilder("ARRAY[");
    for (int i = 0; i < keys.length; i++) {
      if (i > 0)
        keysArray.append(", ");
      keysArray.append("'").append(keys[i]).append("'");
    }
    keysArray.append("]");
    return set(column + " = " + column + " - " + keysArray.toString());
  }

  /**
   * 删除 JSONB 对象的嵌套路径（PostgreSQL）。
   * <p>
   * 使用 #- 运算符删除指定路径的字段。
   * 
   * @param column 列名
   * @param path JSONB 路径，如 "{address,city}"
   * @return 构建器本身
   */
  default T jsonbRemovePath(String column, String path) {
    return set(column + " = " + column + " #- '" + path + "'");
  }

  /**
   * 条件删除 JSONB 对象的嵌套路径。
   * 
   * @param condition 条件
   * @param column 列名
   * @param path JSONB 路径
   * @return 构建器本身
   */
  default T jsonbRemovePathIf(boolean condition, String column, String path) {
    if (condition) {
      return jsonbRemovePath(column, path);
    }
    return self();
  }

  /**
   * 向 JSONB 数组追加元素（PostgreSQL）。
   * <p>
   * 使用 || 运算符向 JSONB 数组末尾追加元素。
   * 
   * @param column 列名
   * @param value 要追加的元素
   * @return 构建器本身
   */
  default T jsonbAppendArray(String column, Object value) {
    return set(column, column + " || ?::jsonb", value);
  }

  /**
   * 条件向 JSONB 数组追加元素。
   * 
   * @param condition 条件
   * @param column 列名
   * @param value 要追加的元素
   * @return 构建器本身
   */
  default T jsonbAppendArrayIf(boolean condition, String column, Object value) {
    if (condition) {
      return jsonbAppendArray(column, value);
    }
    return self();
  }

  /**
   * 向 JSONB 数组开头添加元素（PostgreSQL）。
   * 
   * @param column 列名
   * @param value 要添加的元素
   * @return 构建器本身
   */
  default T jsonbPrependArray(String column, Object value) {
    return set(column, "?::jsonb || " + column, value);
  }

  /**
   * 更新 JSONB 数组指定索引的元素（PostgreSQL）。
   * 
   * @param column 列名
   * @param index 数组索引（从 0 开始）
   * @param value 新值
   * @return 构建器本身
   */
  default T jsonbSetArrayElement(String column, int index, Object value) {
    return jsonbSetPath(column, "{" + index + "}", value);
  }

  /**
   * 条件更新 JSONB 数组指定索引的元素。
   * 
   * @param condition 条件
   * @param column 列名
   * @param index 数组索引
   * @param value 新值
   * @return 构建器本身
   */
  default T jsonbSetArrayElementIf(boolean condition, String column, int index, Object value) {
    if (condition) {
      return jsonbSetArrayElement(column, index, value);
    }
    return self();
  }

  /**
   * 从 JSONB 数组中删除指定索引的元素（PostgreSQL）。
   * 
   * @param column 列名
   * @param index 数组索引（从 0 开始）
   * @return 构建器本身
   */
  default T jsonbRemoveArrayElement(String column, int index) {
    return jsonbRemovePath(column, "{" + index + "}");
  }

  /**
   * 清理 JSONB 对象中的所有 null 值（PostgreSQL）。
   * <p>
   * 使用 jsonb_strip_nulls 函数移除所有值为 null 的字段。
   * 
   * @param column 列名
   * @return 构建器本身
   */
  default T jsonbStripNulls(String column) {
    return set(column + " = jsonb_strip_nulls(" + column + ")");
  }

  /**
   * 条件清理 JSONB 对象中的所有 null 值。
   * 
   * @param condition 条件
   * @param column 列名
   * @return 构建器本身
   */
  default T jsonbStripNullsIf(boolean condition, String column) {
    if (condition) {
      return jsonbStripNulls(column);
    }
    return self();
  }

  /**
   * 将任意值转换为 JSONB 类型（PostgreSQL）。
   * 
   * @param column 列名
   * @param value 要转换的值
   * @return 构建器本身
   */
  default T jsonbFromValue(String column, Object value) {
    return set(column, "to_jsonb(?)", value);
  }

  /**
   * 条件将任意值转换为 JSONB 类型。
   * 
   * @param condition 条件
   * @param column 列名
   * @param value 要转换的值
   * @return 构建器本身
   */
  default T jsonbFromValueIf(boolean condition, String column, Object value) {
    if (condition) {
      return jsonbFromValue(column, value);
    }
    return self();
  }

  /**
   * 使用 JSON 路径查询更新值（PostgreSQL jsonb_set 高级用法）。
   * <p>
   * 支持复杂的嵌套路径操作，路径中的键名如果包含特殊字符需要用引号包裹。
   * 
   * @param column 列名
   * @param pathElements 路径元素数组，如 ["address", "city"] 或 ["users", "0", "name"]
   * @param value 新值
   * @return 构建器本身
   */
  default T jsonbSetPathElements(String column, String[] pathElements, Object value) {
    StringBuilder path = new StringBuilder("{");
    for (int i = 0; i < pathElements.length; i++) {
      if (i > 0)
        path.append(",");
      path.append(pathElements[i]);
    }
    path.append("}");
    return jsonbSetPath(column, path.toString(), value);
  }

  /**
   * 字符串拼接（使用 CONCAT 函数）。
   * 
   * @param column 列名
   * @param value 要拼接的值
   * @return 构建器本身
   */
  default T concat(String column, Object value) {
    return set(column, "CONCAT(" + column + ", ?)", value);
  }

  /**
   * 在字符串末尾追加内容（使用 || 运算符，PostgreSQL）。
   * 
   * @param column 列名
   * @param value 要追加的值
   * @return 构建器本身
   */
  default T append(String column, Object value) {
    return set(column, column + " || ?", value);
  }

  /**
   * 在字符串开头添加内容（使用 || 运算符，PostgreSQL）。
   * 
   * @param column 列名
   * @param value 要添加的值
   * @return 构建器本身
   */
  default T prepend(String column, Object value) {
    return set(column, "? || " + column, value);
  }

  /**
   * 数组追加元素（PostgreSQL array_append 函数）。
   * 
   * @param column 列名
   * @param value 要追加的元素
   * @return 构建器本身
   */
  default T arrayAppend(String column, Object value) {
    return set(column, "array_append(" + column + ", ?)", value);
  }

  /**
   * 数组移除元素（PostgreSQL array_remove 函数）。
   * 
   * @param column 列名
   * @param value 要移除的元素
   * @return 构建器本身
   */
  default T arrayRemove(String column, Object value) {
    return set(column, "array_remove(" + column + ", ?)", value);
  }

  /**
   * 使用子查询设置列值。
   * 
   * @param column 列名
   * @param subQuery 子查询构建器
   * @return 构建器本身
   */
  default T setSubQuery(String column, SqlBuilder subQuery) {
    String subQuerySql = subQuery.getSql();
    Object[] subQueryParams = subQuery.getParams();

    InnerUpdateSetHolder holder = innerUpdateSetHolder();
    holder.addSetSubQuery(column, subQuerySql, subQueryParams);

    return self();
  }

  /**
   * 条件使用子查询设置列值。
   * 
   * @param condition 条件
   * @param column 列名
   * @param subQuery 子查询构建器
   * @return 构建器本身
   */
  default T setSubQueryIf(boolean condition, String column, SqlBuilder subQuery) {
    if (condition) {
      return setSubQuery(column, subQuery);
    }
    return self();
  }

  /**
   * 设置为 COALESCE 表达式（取第一个非 NULL 值）。
   * 
   * @param column 列名
   * @param defaultValue 默认值
   * @return 构建器本身
   */
  default T setCoalesce(String column, Object defaultValue) {
    return set(column, "COALESCE(" + column + ", ?)", defaultValue);
  }

  /**
   * UPDATE SET 子句内部持有者，用于管理多个 SET 操作。
   * <p>
   * 该类用于在 UPDATE 语句中统一管理所有的 SET 表达式和参数。
   * <p>
   * 注意：此类为包私有，仅供 {@code cn.dinodev.sql.builder.clause} 包内使用。
   * 
   * @author Cody Lu
   * @since 2025-01-03
   */
  class InnerUpdateSetHolder {
    private final List<String> setExpressions = new java.util.ArrayList<>();
    private final List<Object> setParams = new java.util.ArrayList<>();

    /**
     * 添加普通的 SET 操作（使用占位符）。
     * <p>
     * 如果表达式包含 '?'，直接使用；否则自动添加 '= ?'。
     * 
     * @param expression 列表达式（可能已包含 '= ?' 或仅列名）
     * @param value 值
     */
    public void addSet(String expression, Object value) {
      // 如果表达式中包含 '?'，说明已经是完整的表达式，直接使用
      if (expression.contains("?")) {
        setExpressions.add(expression);
      } else {
        // 否则，认为是列名，自动添加 '= ?'
        setExpressions.add(expression + " = ?");
      }
      setParams.add(value);
    }

    /**
     * 添加带多个参数的 SET 操作。
     * <p>
     * 用于支持包含多个占位符的表达式。
     * 
     * @param expression 包含占位符的表达式
     * @param params 参数集合
     */
    public void addSet(String expression, java.util.Collection<Object> params) {
      setExpressions.add(expression);
      if (params != null && !params.isEmpty()) {
        setParams.addAll(params);
      }
    }

    /**
     * 添加带表达式的 SET 操作。
     * 
     * @param column 列名
     * @param expression 值表达式
     * @param value 参数值
     */
    public void addSetWithExpression(String column, String expression, Object value) {
      setExpressions.add(column + " = " + expression);
      setParams.add(value);
    }

    /**
     * 添加不带参数的 SET 操作（用于函数调用、表达式等）。
     * 
     * @param expression 完整的表达式（如 "status = 0" 或 "updated_at = NOW()"）
     */
    public void addSetWithoutParam(String expression) {
      setExpressions.add(expression);
    }

    /**
     * 添加使用子查询的 SET 操作。
     * 
     * @param column 列名
     * @param subQuerySql 子查询 SQL
     * @param subQueryParams 子查询参数
     */
    public void addSetSubQuery(String column, String subQuerySql, Object[] subQueryParams) {
      setExpressions.add(column + " = (" + subQuerySql + ")");
      if (subQueryParams != null && subQueryParams.length > 0) {
        for (Object param : subQueryParams) {
          setParams.add(param);
        }
      }
    }

    /**
     * 获取所有 SET 表达式。
     * 
     * @return SET 表达式列表
     */
    public List<String> getSetExpressions() {
      return setExpressions;
    }

    /**
     * 判断是否为空。
     * 
     * @return 如果没有任何 SET 表达式则返回 true
     */
    public boolean isEmpty() {
      return setExpressions.isEmpty();
    }

    /**
     * 获取 SET 表达式数量。
     * 
     * @return SET 表达式数量
     */
    public int size() {
      return setExpressions.size();
    }

    /**
     * 构建 SET 子句 SQL 字符串。
     * 
     * @param sql SQL 字符串构建器
     */
    public void appendSql(StringBuilder sql) {
      if (setExpressions.isEmpty()) {
        return;
      }

      sql.append(" SET ");

      boolean first = true;
      for (String expression : setExpressions) {
        if (!first) {
          sql.append(", ");
        }
        first = false;
        sql.append(expression);
      }
    }

    /**
     * 将所有 SET 参数添加到给定的参数列表中。
     * 
     * @param params 参数列表
     */
    public void appendParams(List<Object> params) {
      params.addAll(setParams);
    }

    /**
     * 获取参数个数。
     * 
     * @return 参数个数
     */
    public int getParamsCount() {
      return setParams.size();
    }
  }
}
