// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import cn.dinodev.sql.SqlBuilder;
import cn.dinodev.sql.utils.SqlBuilderUtils;

/**
 * INSERT 子句接口，提供插入数据相关的方法。
 * <p>
 * 支持单行插入、批量插入、条件插入等功能。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-12-04
 */
public interface InsertSetClause<T extends SqlBuilder> extends ClauseSupport<T> {

  /**
   * 获取内部的 INSERT SET 持有者。
   * 警告：通常不建议直接操作此对象，除非有特殊需求。
   * 
   * @return 内部 INSERT SET 持有者
   */
  InnerInsertSetHolder innerInsertSetHolder();

  /**
   * 设置列值（使用占位符）。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   */
  default T set(String column, Object value) {
    innerInsertSetHolder().addSet(column, "?", value);
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
    innerInsertSetHolder().addSet(column, expression, value);
    return self();
  }

  /**
   * 条件设置列值。
   * 
   * @param condition 条件
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   */
  default T setIf(boolean condition, String column, Object value) {
    if (condition) {
      set(column, value);
    }
    return self();
  }

  /**
   * 值不为 null 时设置列值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   */
  default T setIfNotNull(String column, Object value) {
    if (value != null) {
      set(column, value);
    }
    return self();
  }

  /**
   * 设置列值但不添加参数（用于函数调用等）。
   * 
   * @param column 列名
   * @param expression 值表达式
   * @return 构建器本身
   */
  default T setExpression(String column, String expression) {
    innerInsertSetHolder().addSetWithoutParam(column, expression);
    return self();
  }

  /**
   * 设置当前时间戳。
   * 
   * @param column 列名
   * @return 构建器本身
   */
  default T setNow(String column) {
    return setExpression(column, "NOW()");
  }

  /**
   * 根据条件设置当前时间戳。
   * 
   * @param condition 条件，为 true 时才设置
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
   * 设置 NULL 值。
   * 
   * @param column 列名
   * @return 构建器本身
   */
  default T setNull(String column) {
    return setExpression(column, "NULL");
  }

  /**
   * 根据条件设置 NULL 值。
   * 
   * @param condition 条件，为 true 时才设置
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
   * 设置默认值。
   * 
   * @param column 列名
   * @return 构建器本身
   */
  default T setDefault(String column) {
    return setExpression(column, "DEFAULT");
  }

  /**
   * 根据条件设置默认值。
   * 
   * @param condition 条件，为 true 时才设置
   * @param column 列名
   * @return 构建器本身
   */
  default T setDefaultIf(boolean condition, String column) {
    if (condition) {
      return setDefault(column);
    }
    return self();
  }

  // ==================== 扩展方法 ====================

  /**
   * 根据条件设置列值，使用自定义表达式。
   *
   * @param condition 条件，为 true 时才设置
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
   * 根据条件设置列值但不添加参数。
   *
   * @param condition 条件，为 true 时才设置
   * @param column 列名
   * @param expression 值表达式
   * @return 构建器本身
   */
  default T setExpressionIf(boolean condition, String column, String expression) {
    if (condition) {
      setExpression(column, expression);
    }
    return self();
  }

  /**
   * 设置 JSON 类型列值。
   * <p>
   * 根据数据库方言自动适配类型转换语法：
   * <ul>
   *   <li><b>MySQL</b>: 使用 ? 占位符</li>
   *   <li><b>PostgreSQL</b>: 使用 ?::json 类型转换</li>
   * </ul>
   *
   * @param column 列名
   * @param value JSON值
   * @return 构建器本身
   */
  default T setJson(String column, Object value) {
    return set(column, dialect().makeJsonTypeCast(), value);
  }

  /**
   * 根据条件设置 JSON 类型列值。
   *
   * @param condition 条件，为 true 时才设置
   * @param column 列名
   * @param value JSON值
   * @return 构建器本身
   */
  default T setJsonIf(boolean condition, String column, Object value) {
    if (condition) {
      return setJson(column, value);
    }
    return self();
  }

  /**
   * 当值不为 null 时设置 JSON 类型列值。
   *
   * @param column 列名
   * @param value JSON值
   * @return 构建器本身
   */
  default T setJsonIfNotNull(String column, Object value) {
    if (value != null) {
      return setJson(column, value);
    }
    return self();
  }

  /**
   * 设置 JSONB 类型列值。
   * <p>
   * JSONB 是 PostgreSQL 特有的二进制 JSON 类型。在 MySQL 中会使用普通 JSON 类型。
   * <ul>
   *   <li><b>MySQL</b>: 使用 ? 占位符（自动映射为 JSON 类型）</li>
   *   <li><b>PostgreSQL</b>: 使用 ?::jsonb 类型转换</li>
   * </ul>
   *
   * @param column 列名
   * @param value JSONB值
   * @return 构建器本身
   */
  default T setJsonb(String column, Object value) {
    return set(column, dialect().makeJsonbTypeCast(), value);
  }

  /**
   * 根据条件设置 JSONB 类型列值。
   *
   * @param condition 条件，为 true 时才设置
   * @param column 列名
   * @param value JSONB值
   * @return 构建器本身
   */
  default T setJsonbIf(boolean condition, String column, Object value) {
    if (condition) {
      return setJsonb(column, value);
    }
    return self();
  }

  /**
   * 当值不为 null 时设置 JSONB 类型列值。
   *
   * @param column 列名
   * @param value JSONB值
   * @return 构建器本身
   */
  default T setJsonbIfNotNull(String column, Object value) {
    if (value != null) {
      return setJsonb(column, value);
    }
    return self();
  }

  /**
   * 批量设置列值。
   * <p>
   * 使用示例：
   * <pre>{@code
   * Map<String, Object> values = new HashMap<>();
   * values.put("name", "张三");
   * values.put("age", 25);
   * builder.setMap(values);
   * }</pre>
   *
   * @param values 列名-值映射
   * @return 构建器本身
   */
  default T setMap(java.util.Map<String, Object> values) {
    if (values != null) {
      values.forEach(this::set);
    }
    return self();
  }

  /**
   * 根据条件批量设置列值。
   *
   * @param condition 条件，为 true 时才设置
   * @param values 列名-值映射
   * @return 构建器本身
   */
  default T setMapIf(boolean condition, java.util.Map<String, Object> values) {
    if (condition && values != null) {
      values.forEach(this::set);
    }
    return self();
  }

  /**
   * 批量设置列值（仅设置非 null 值）。
   *
   * @param values 列名-值映射
   * @return 构建器本身
   */
  default T setMapIfNotNull(java.util.Map<String, Object> values) {
    if (values != null) {
      values.forEach((column, value) -> {
        if (value != null) {
          set(column, value);
        }
      });
    }
    return self();
  }

  /**
   * 设置数组类型列值（PostgreSQL）。
   * <p>
   * 使用 PostgreSQL 的 ARRAY 构造函数，自动推断数组元素类型。
   * 支持 Java 数组和 Collection 类型。
   * <p>
   * 使用示例：
   * <pre>{@code
   * // 使用数组
   * builder.setArray("tags", new int[]{1, 2, 3});
   * builder.setArray("phones", new String[]{"13800138000", "13911112222"});
   * 
   * // 使用 List
   * List<Integer> tagList = Arrays.asList(1, 2, 3);
   * builder.setArray("tags", tagList);
   * 
   * // 使用 Set
   * Set<String> phoneSet = new HashSet<>(Arrays.asList("13800138000", "13911112222"));
   * builder.setArray("phones", phoneSet);
   * 
   * // 生成: ARRAY[?, ?, ...]
   * }</pre>
   *
   * @param column 列名
   * @param value 数组值或 Collection 集合
   * @return 构建器本身
   */
  @SuppressWarnings("unchecked")
  default T setArray(String column, Object value) {
    if (value == null) {
      return self();
    }

    Collection<Object> elements = Collections.emptyList();

    // 处理数组类型
    if (value.getClass().isArray()) {
      elements = Arrays.asList((Object[]) value);

    }
    // 处理 Collection 类型
    else if (value instanceof java.util.Collection) {
      elements = (Collection<Object>) value;
    }
    // 不支持的类型
    else {
      throw new IllegalArgumentException(
          "Value must be an array or Collection, but got: " + value.getClass());
    }

    // 构建 ARRAY[?, ?, ...]
    String expression = SqlBuilderUtils.makeNParamExpr("ARRAY[", elements.size(), "]");

    // 添加列、表达式和所有参数
    innerInsertSetHolder().addSetWithMultipleParams(column, expression, elements);

    return self();
  }

  /**
   * 根据条件设置数组类型列值。
   *
   * @param condition 条件，为 true 时才设置
   * @param column 列名
   * @param value 数组值
   * @return 构建器本身
   */
  default T setArrayIf(boolean condition, String column, Object value) {
    if (condition) {
      return setArray(column, value);
    }
    return self();
  }

  /**
   * 当值不为 null 时设置数组类型列值。
   *
   * @param column 列名
   * @param value 数组值
   * @return 构建器本身
   */
  default T setArrayIfNotNull(String column, Object value) {
    if (value != null) {
      return setArray(column, value);
    }
    return self();
  }

  /**
   * 设置二进制类型列值。
   * <p>
   * 根据数据库方言自动适配二进制类型转换语法：
   * <ul>
   *   <li><b>MySQL</b>: 使用 ? 占位符（自动映射为 BLOB/VARBINARY）</li>
   *   <li><b>PostgreSQL</b>: 使用 ?::bytea 类型转换</li>
   * </ul>
   *
   * @param column 列名
   * @param value 二进制数据（byte[]）
   * @return 构建器本身
   */
  default T setBytea(String column, Object value) {
    return set(column, dialect().makeBinaryTypeCast(), value);
  }

  /**
   * 根据条件设置 BYTEA 类型列值。
   *
   * @param condition 条件，为 true 时才设置
   * @param column 列名
   * @param value 二进制数据
   * @return 构建器本身
   */
  default T setByteaIf(boolean condition, String column, Object value) {
    if (condition) {
      return setBytea(column, value);
    }
    return self();
  }

  /**
   * 当值不为 null 时设置 BYTEA 类型列值。
   *
   * @param column 列名
   * @param value 二进制数据
   * @return 构建器本身
   */
  default T setByteaIfNotNull(String column, Object value) {
    if (value != null) {
      return setBytea(column, value);
    }
    return self();
  }

  /**
   * 生成 UUID（使用数据库方言）。
   * <p>
   * 根据当前数据库方言自动选择合适的 UUID 生成函数：
   * <ul>
   *   <li><b>MySQL</b>: UUID()</li>
   *   <li><b>PostgreSQL 13+</b>: gen_random_uuid()</li>
   *   <li><b>PostgreSQL &lt;13</b>: uuid_generate_v4() (需要 uuid-ossp 扩展)</li>
   * </ul>
   *
   * @param column 列名
   * @return 构建器本身
   */
  default T setGenUuid(String column) {
    return setExpression(column, dialect().getUuidFunction());
  }

  /**
   * 根据条件生成 UUID。
   *
   * @param condition 条件，为 true 时才设置
   * @param column 列名
   * @return 构建器本身
   */
  default T setGenUuidIf(boolean condition, String column) {
    if (condition) {
      return setGenUuid(column);
    }
    return self();
  }

  // ==================== 内部 Holder 类 ====================

  /**
   * INSERT SET 子句内部持有者，用于管理插入的列和值。
   * <p>
   * 该类用于在 INSERT 语句中统一管理所有的列名、值表达式和参数。
   * <p>
   * 注意：此类为包私有，仅供 {@code cn.dinodev.sql.builder.clause} 包内使用。
   * 
   * @author Cody Lu
   * @since 2025-01-03
   */
  class InnerInsertSetHolder {
    private final java.util.List<String> columns = new java.util.ArrayList<>();
    private final java.util.List<String> valueExpressions = new java.util.ArrayList<>();
    private final java.util.List<Object> params = new java.util.ArrayList<>();

    /**
     * 创建 INSERT SET 持有者实例。
     */
    public InnerInsertSetHolder() {
      // 默认构造函数
    }

    /**
     * 添加普通的列值设置（使用占位符）。
     * 
     * @param column 列名
     * @param expression 值表达式
     * @param value 值
     */
    public void addSet(String column, String expression, Object value) {
      columns.add(column);
      valueExpressions.add(expression);
      params.add(value);
    }

    /**
     * 添加不带参数的列值设置（用于函数调用、表达式等）。
     * 
     * @param column 列名
     * @param expression 值表达式
     */
    public void addSetWithoutParam(String column, String expression) {
      columns.add(column);
      valueExpressions.add(expression);
    }

    /**
     * 添加带有多个参数的列值设置（用于数组等）。
     * 
     * @param column 列名
     * @param expression 值表达式
     * @param values 多个参数值
     */
    public void addSetWithMultipleParams(String column, String expression, Collection<Object> values) {
      columns.add(column);
      valueExpressions.add(expression);
      params.addAll(values);
    }

    /**
     * 判断是否为空。
     * 
     * @return 如果没有任何列则返回 true
     */
    public boolean isEmpty() {
      return columns.isEmpty();
    }

    /**
     * 获取列数量。
     * 
     * @return 列数量
     */
    public int size() {
      return columns.size();
    }

    /**
     * 构建 INSERT 子句的列名和值部分。
     * 
     * @param sql SQL 字符串构建器
     */
    public void appendSql(StringBuilder sql) {
      if (columns.isEmpty()) {
        return;
      }

      // 列名部分
      sql.append(" (");
      boolean first = true;
      for (String column : columns) {
        if (!first) {
          sql.append(", ");
        }
        first = false;
        sql.append(column);
      }
      sql.append(")");

      // VALUES 部分
      sql.append(" VALUES (");
      first = true;
      for (String expression : valueExpressions) {
        if (!first) {
          sql.append(", ");
        }
        first = false;
        sql.append(expression);
      }
      sql.append(")");
    }

    /**
     * 将所有参数添加到给定的参数列表中。
     * 
     * @param allParams 参数列表
     */
    public void appendParams(java.util.List<Object> allParams) {
      allParams.addAll(params);
    }

    /**
     * 获取参数个数。
     * 
     * @return 参数个数
     */
    public int getParamsCount() {
      return params.size();
    }
  }
}
