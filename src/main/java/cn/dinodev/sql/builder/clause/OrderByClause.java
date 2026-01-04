// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import java.util.List;

import cn.dinodev.sql.NullsOrder;
import cn.dinodev.sql.SqlBuilder;

/**
 * ORDER BY 子句接口，提供排序相关的方法。
 * <p>
 * 支持链式调用，提供多种排序方式。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-12-01
 */
public interface OrderByClause<T extends SqlBuilder> extends ClauseSupport<T> {

  /**
   * 获取内部的 ORDER BY 持有者。
   * 警告：通常不建议直接操作此对象，除非有特殊需求。
   * 
   * @return 内部 ORDER BY 持有者
   */
  InnerOrderByHolder innerOrderByHolder();

  /**
   * 添加 ORDER BY 子句。
   * <p>
   * 支持多种格式：
   * <ul>
   *   <li>单列：orderBy("name")</li>
   *   <li>带排序：orderBy("name DESC")</li>
   *   <li>多列：orderBy("name", "age DESC")</li>
   *   <li>逗号分隔：orderBy("name, age DESC")</li>
   *   <li>函数表达式：orderBy("UPPER(name) ASC")</li>
   *   <li>计算表达式：orderBy("(price * quantity) DESC")</li>
   *   <li>CASE 表达式：orderBy("CASE WHEN status = 'active' THEN 1 ELSE 2 END ASC")</li>
   * </ul>
   * <p>
   * 示例：
   * <pre>
   * // 基本排序
   * builder.orderBy("age DESC", "name ASC");
   * 
   * // 函数表达式排序
   * builder.orderBy("UPPER(name) ASC");
   * 
   * // 计算表达式排序
   * builder.orderBy("(price * quantity) DESC");
   * 
   * // 复杂 CASE 排序
   * builder.orderBy("CASE WHEN status = 'urgent' THEN 1 ELSE 2 END ASC");
   * </pre>
   * 
   * @param expressions 排序表达式（列名、函数、计算式、CASE 表达式等）
   * @return 构建器本身
   */
  default T orderBy(String... expressions) {
    if (expressions != null) {
      for (String expression : expressions) {
        innerOrderByHolder().addOrderBy(expression);
      }
    }
    return self();
  }

  /**
   * 添加 ORDER BY 子句，指定升序或降序。
   * <p>
   * 支持列名、函数表达式、计算表达式等。
   * <p>
   * 示例：
   * <pre>
   * // 列名排序
   * builder.orderBy("age", false);        // age DESC
   * builder.orderBy("name", true);        // name ASC
   * 
   * // 函数表达式排序
   * builder.orderBy("UPPER(name)", true); // UPPER(name) ASC
   * 
   * // 计算表达式排序
   * builder.orderBy("(price * quantity)", false); // (price * quantity) DESC
   * </pre>
   * 
   * @param column 列名或表达式（列名、函数、计算式等）
   * @param ascending true 为升序（ASC），false 为降序（DESC）
   * @return 构建器本身
   */
  default T orderBy(String column, boolean ascending) {
    return orderBy(column + (ascending ? " ASC" : " DESC"));
  }

  /**
   * 添加升序排序。
   * <p>
   * 支持列名、函数表达式、计算表达式等。
   * <p>
   * 示例：
   * <pre>
   * // 列名排序
   * builder.orderByAsc("name", "age");
   * 
   * // 函数表达式排序
   * builder.orderByAsc("UPPER(name)", "LOWER(email)");
   * 
   * // 计算表达式排序
   * builder.orderByAsc("(price * 0.9)", "stock");
   * 
   * // 混合排序
   * builder.orderByAsc("category", "UPPER(name)");
   * </pre>
   * 
   * @param columns 列名或表达式（列名、函数、计算式等）
   * @return 构建器本身
   */
  default T orderByAsc(String... columns) {
    if (columns != null && columns.length > 0) {
      for (String column : columns) {
        if (column != null && !column.trim().isEmpty()) {
          orderBy(column + " ASC");
        }
      }
    }
    return self();
  }

  /**
   * 添加降序排序。
   * <p>
   * 支持列名、函数表达式、计算表达式等。
   * <p>
   * 示例：
   * <pre>
   * // 列名排序
   * builder.orderByDesc("created_at", "id");
   * 
   * // 函数表达式排序
   * builder.orderByDesc("LENGTH(description)", "updated_at");
   * 
   * // 计算表达式排序
   * builder.orderByDesc("(price * quantity)", "discount");
   * 
   * // 混合排序
   * builder.orderByDesc("score", "YEAR(created_at)");
   * </pre>
   * 
   * @param columns 列名或表达式（列名、函数、计算式等）
   * @return 构建器本身
   */
  default T orderByDesc(String... columns) {
    if (columns != null && columns.length > 0) {
      for (String column : columns) {
        if (column != null && !column.trim().isEmpty()) {
          orderBy(column + " DESC");
        }
      }
    }
    return self();
  }

  /**
   * 条件排序，仅当条件为真时添加排序。
   * <p>
   * 示例：
   * <pre>
   * // 条件列排序
   * builder.orderByIf(sortByAge, "age DESC");
   * 
   * // 条件表达式排序
   * builder.orderByIf(sortByName, "UPPER(name) ASC");
   * </pre>
   * 
   * @param condition 条件
   * @param expressions 排序表达式（列名、函数、计算式等）
   * @return 构建器本身
   */
  default T orderByIf(boolean condition, String... expressions) {
    if (condition) {
      orderBy(expressions);
    }
    return self();
  }

  /**
   * 条件升序排序，仅当条件为真时添加排序。
   * <p>
   * 支持列名、函数表达式、计算表达式等。
   * <p>
   * 示例：
   * <pre>
   * // 条件列排序
   * builder.orderByAscIf(sortByName, "name", "age");
   * 
   * // 条件函数表达式排序
   * builder.orderByAscIf(sortByName, "UPPER(name)", "email");
   * </pre>
   * 
   * @param condition 条件
   * @param columns 列名或表达式（列名、函数、计算式等）
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T orderByAscIf(boolean condition, String... columns) {
    if (condition) {
      orderByAsc(columns);
    }
    return self();
  }

  /**
   * 条件降序排序，仅当条件为真时添加排序。
   * <p>
   * 支持列名、函数表达式、计算表达式等。
   * <p>
   * 示例：
   * <pre>
   * // 条件列排序
   * builder.orderByDescIf(sortByDate, "created_at", "updated_at");
   * 
   * // 条件表达式排序
   * builder.orderByDescIf(sortByAmount, "(price * quantity)", "discount");
   * </pre>
   * 
   * @param condition 条件
   * @param columns 列名或表达式（列名、函数、计算式等）
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T orderByDescIf(boolean condition, String... columns) {
    if (condition) {
      orderByDesc(columns);
    }
    return self();
  }

  /**
   * 条件升序排序，带 NULLS 位置控制。
   * <p>
   * 仅当条件为真时添加升序排序并指定 NULLS 位置。
   * <p>
   * 示例：
   * <pre>
   * // 条件排序带 NULLS LAST
   * builder.orderByAscIf(hasScore, "score", NullsOrder.NULLS_LAST);
   * 
   * // 多列条件排序
   * builder.orderByAscIf(sortEnabled, NullsOrder.NULLS_FIRST, "name", "email");
   * </pre>
   * 
   * @param condition 条件
   * @param nullsOrder NULLS 排序位置
   * @param columns 列名
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T orderByAscIfWithNullsOrder(boolean condition, NullsOrder nullsOrder, String... columns) {
    if (condition) {
      for (String column : columns) {
        orderByAscWithNullsOrder(column, nullsOrder);
      }
    }
    return self();
  }

  /**
   * 条件降序排序，带 NULLS 位置控制。
   * <p>
   * 仅当条件为真时添加降序排序并指定 NULLS 位置。
   * <p>
   * 示例：
   * <pre>
   * // 条件排序带 NULLS FIRST
   * builder.orderByDescIf(hasScore, "score", NullsOrder.NULLS_FIRST);
   * 
   * // 多列条件排序
   * builder.orderByDescIf(sortEnabled, NullsOrder.NULLS_LAST, "rating", "views");
   * </pre>
   * 
   * @param condition 条件
   * @param nullsOrder NULLS 排序位置
   * @param columns 列名
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T orderByDescIfWithNullsOrder(boolean condition, NullsOrder nullsOrder, String... columns) {
    if (condition) {
      for (String column : columns) {
        orderByDescWithNullsOrder(column, nullsOrder);
      }
    }
    return self();
  }

  /**
   * 添加 ORDER BY 子句，指定排序方向和 NULLS 位置（SQL 标准扩展）。
   * <p>
   * 示例：
   * <pre>
   * // PostgreSQL、Oracle 等支持
   * builder.orderBy("score", "DESC", OrderByClause.NullsOrder.NULLS_LAST);
   * // 结果: ORDER BY score DESC NULLS LAST
   * </pre>
   * 
   * @param column 列名或表达式
   * @param ascending true 为升序（ASC），false 为降序（DESC）
   * @param nullsOrder NULLS 排序位置
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T orderByWithNullsOrder(String column, boolean ascending, NullsOrder nullsOrder) {
    if (nullsOrder == NullsOrder.NONE) {
      throw new IllegalArgumentException("NullsOrder cannot be NONE");
    }
    innerOrderByHolder().addOrderBy(column, ascending ? "ASC" : "DESC", nullsOrder);
    return self();
  }

  /**
   * 添加升序排序，指定 NULLS 位置。
   * <p>
   * 示例：
   * <pre>
   * builder.orderByAsc("score", NullsOrder.NULLS_LAST);
   * </pre>
   * 
   * @param column 列名或表达式
   * @param nullsOrder NULLS 排序位置
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T orderByAscWithNullsOrder(String column, NullsOrder nullsOrder) {
    return orderByWithNullsOrder(column, true, nullsOrder);
  }

  /**
   * 添加降序排序，指定 NULLS 位置。
   * <p>
   * 示例：
   * <pre>
   * builder.orderByDesc("score", NullsOrder.NULLS_FIRST);
   * </pre>
   * 
   * @param column 列名或表达式
   * @param nullsOrder NULLS 排序位置
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T orderByDescWithNullsOrder(String column, NullsOrder nullsOrder) {
    return orderByWithNullsOrder(column, false, nullsOrder);
  }

  /**
   * 按 SELECT 列表位置编号排序（SQL 标准）。
   * <p>
   * 示例：
   * <pre>
   * builder.select("name", "age", "score")
   *        .orderByPosition(3, false);  // ORDER BY 3 DESC (按 score 降序)
   * </pre>
   * 
   * @param position 列位置（从 1 开始）
   * @param ascending true 为升序（ASC），false 为降序（DESC）
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T orderByPosition(int position, boolean ascending) {
    if (position < 1) {
      throw new IllegalArgumentException("Position must be >= 1, got: " + position);
    }
    return orderBy(String.valueOf(position), ascending);
  }

  /**
   * 按 SELECT 列表位置编号升序排序（SQL 标准）。
   * <p>
   * 示例：
   * <pre>
   * builder.select("name", "age", "score")
   *        .orderByPositionAsc(1, 2);  // ORDER BY 1 ASC, 2 ASC
   * </pre>
   * 
   * @param positions 列位置（从 1 开始）
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T orderByPositionAsc(int... positions) {
    if (positions != null && positions.length > 0) {
      for (int position : positions) {
        orderByPosition(position, true);
      }
    }
    return self();
  }

  /**
   * 按 SELECT 列表位置编号降序排序（SQL 标准）。
   * <p>
   * 示例：
   * <pre>
   * builder.select("name", "age", "score")
   *        .orderByPositionDesc(3, 2);  // ORDER BY 3 DESC, 2 DESC
   * </pre>
   * 
   * @param positions 列位置（从 1 开始）
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T orderByPositionDesc(int... positions) {
    if (positions != null && positions.length > 0) {
      for (int position : positions) {
        orderByPosition(position, false);
      }
    }
    return self();
  }

  /**
   * 按指定排序规则（COLLATE）排序（SQL 标准）。
   * <p>
   * 示例：
   * <pre>
   * // MySQL
   * builder.orderByCollate("name", "utf8mb4_unicode_ci", true);
   * // PostgreSQL
   * builder.orderByCollate("title", "en_US", false);
   * </pre>
   * 
   * @param column 列名
   * @param ascending true 为升序（ASC），false 为降序（DESC）
   * @param collation 排序规则名称
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T orderByWithCollate(String column, boolean ascending, String collation) {
    if (column == null || column.trim().isEmpty()) {
      throw new IllegalArgumentException("Column cannot be null or empty");
    }
    if (collation == null || collation.trim().isEmpty()) {
      throw new IllegalArgumentException("Collation cannot be null or empty");
    }
    String expression = column.trim() + " COLLATE " + collation.trim()
        + (ascending ? " ASC" : " DESC");
    return orderBy(expression);
  }

  /**
   * ORDER BY 子句内部持有者，用于管理多个 ORDER BY 表达式。
   * <p>
   * 该类用于统一管理所有排序表达式。
   * 
   * @author Cody Lu
   * @since 2024-12-31
   */
  public class InnerOrderByHolder {
    private final List<String> orderByExpressions = new java.util.ArrayList<>();

    /**
     * 添加 ORDER BY 表达式。
     * 
     * @param expression 排序表达式（如 "name ASC", "age DESC"）
     */
    public void addOrderBy(String expression) {
      if (expression == null || expression.trim().isEmpty()) {
        throw new IllegalArgumentException("Order by expression cannot be null or empty");
      }
      orderByExpressions.add(expression.trim());
    }

    /**
     * 添加 ORDER BY 表达式（结构化方式）。
     * 
     * @param column 列名
     * @param direction 排序方向（如 "ASC" 或 "DESC"）
     */
    public void addOrderBy(String column, String direction) {
      if (column == null || column.trim().isEmpty()) {
        throw new IllegalArgumentException("Column name cannot be null or empty");
      }
      if (direction == null || direction.trim().isEmpty()) {
        throw new IllegalArgumentException("Order direction cannot be null or empty");
      }
      String expression = column.trim() + " " + direction.trim();
      orderByExpressions.add(expression);
    }

    /**
     * 添加 ORDER BY 表达式（带 NULLS 排序）。
     * 
     * @param column 列名
     * @param direction 排序方向（如 "ASC" 或 "DESC"）
     * @param nullsOrder NULLS 排序位置
     */
    public void addOrderBy(String column, String direction, NullsOrder nullsOrder) {
      if (column == null || column.trim().isEmpty()) {
        throw new IllegalArgumentException("Column name cannot be null or empty");
      }
      if (direction == null || direction.trim().isEmpty()) {
        throw new IllegalArgumentException("Order direction cannot be null or empty");
      }
      if (nullsOrder == null) {
        nullsOrder = NullsOrder.NONE;
      }
      StringBuilder sb = new StringBuilder(column.trim());
      sb.append(" ").append(direction.trim());
      if (nullsOrder != NullsOrder.NONE && !nullsOrder.getSql().isEmpty()) {
        sb.append(" ").append(nullsOrder.getSql());
      }
      orderByExpressions.add(sb.toString());
    }

    /**
     * 获取所有 ORDER BY 表达式。
     * 
     * @return ORDER BY 表达式列表（不可修改）
     */
    public List<String> getOrderByExpressions() {
      return java.util.Collections.unmodifiableList(orderByExpressions);
    }

    /**
     * 清空所有 ORDER BY 表达式。
     */
    public void clear() {
      orderByExpressions.clear();
    }

    /**
     * 判断是否为空。
     * 
     * @return 如果没有任何 ORDER BY 表达式则返回 true
     */
    public boolean isEmpty() {
      return orderByExpressions.isEmpty();
    }

    /**
     * 获取 ORDER BY 表达式数量。
     * 
     * @return ORDER BY 表达式数量
     */
    public int size() {
      return orderByExpressions.size();
    }

    /**
     * 构建 ORDER BY 子句 SQL 字符串。
     * <p>
     * 格式：
     * <pre>
     * ORDER BY column1 ASC, column2 DESC, column3 ASC NULLS FIRST
     * </pre>
     * 
     * @param sql SQL 字符串构建器
     */
    public void appendSql(StringBuilder sql) {
      if (orderByExpressions.isEmpty()) {
        return;
      }

      sql.append(" ORDER BY ");
      for (int i = 0; i < orderByExpressions.size(); i++) {
        if (i > 0) {
          sql.append(", ");
        }
        sql.append(orderByExpressions.get(i));
      }
    }

  }
}
