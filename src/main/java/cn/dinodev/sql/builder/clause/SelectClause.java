// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import java.util.List;

import cn.dinodev.sql.SqlBuilder;

/**
 * SELECT 列选择子句接口，提供选择列的方法。
 * <p>
 * 用于指定查询返回的列。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-12-02
 */
public interface SelectClause<T extends SqlBuilder> extends ClauseSupport<T> {

  /**
   * 获取内部的 SELECT 持有者。
   * 警告：通常不建议直接操作此对象，除非有特殊需求。
   * 
   * @return 内部 SELECT 持有者
   */
  InnerSelectHolder innerSelectHolder();

  /**
   * 选择多列。
   * <p>
   * 支持多种格式：
   * <ul>
   *   <li>多列：columns("id", "name", "age")</li>
   *   <li>带别名：columns("user_id AS id", "user_name AS name")</li>
   *   <li>表达式：columns("COUNT(*) AS cnt", "SUM(score) AS total")</li>
   *   <li>所有列：columns("*")</li>
   * </ul>
   * 
   * @param columns 列名或表达式
   * @return 构建器本身
   */
  default T columns(CharSequence... columns) {
    innerSelectHolder().addColumns(columns);
    return self();
  }

  /**
   * 条件选择列，仅当条件为真时添加。
   * 
   * @param condition 条件
   * @param columns 列名或表达式
   * @return 构建器本身
   */
  default T columnsIf(boolean condition, String... columns) {
    if (condition) {
      this.columns(columns);
    }
    return self();
  }

  /**
   * 选择单列（快捷方式）。
   * <p>
   * 等价于 columns(column)
   * 
   * @param column 列名或表达式
   * @return 构建器本身
   */
  default T column(CharSequence column) {
    innerSelectHolder().addColumns(column);
    return self();
  }

  /**
   * 选择列（带别名）。
   * 
   * @param column 列名
   * @param alias 别名
   * @return 构建器本身
   */
  default T columnAs(CharSequence column, String alias) {
    innerSelectHolder().addColumns(column + " AS " + alias);
    return self();
  }

  /**
   * 选择所有列（SELECT *）。
   * 
   * @return 构建器本身
   */
  default T selectAll() {
    innerSelectHolder().addColumns("*");
    return self();
  }

  /**
   * 选择聚合函数列。
   * 
   * @param function 聚合函数名（如 COUNT、SUM、AVG 等）
   * @param column 列名
   * @param alias 别名
   * @return 构建器本身
   */
  default T columnAggregate(String function, CharSequence column, String alias) {
    innerSelectHolder().addColumns(function + "(" + column + ") AS " + alias);
    return self();
  }

  /**
   * SELECT COUNT(*)。
   * 
   * @param alias 别名
   * @return 构建器本身
   */
  default T columnCount(String alias) {
    return columnAggregate("COUNT", "1", alias);
  }

  /**
   * SELECT COUNT(DISTINCT column)。
   * 
   * @param column 列名
   * @param alias 别名
   * @return 构建器本身
   */
  default T columnCountDistinct(CharSequence column, String alias) {
    return column("COUNT(DISTINCT " + column + ") AS " + alias);
  }

  /**
   * SELECT SUM(column)。
   * 
   * @param column 列名
   * @param alias 别名
   * @return 构建器本身
   */
  default T columnSum(CharSequence column, String alias) {
    return columnAggregate("SUM", column, alias);
  }

  /**
   * SELECT AVG(column)。
   * 
   * @param column 列名
   * @param alias 别名
   * @return 构建器本身
   */
  default T columnAvg(CharSequence column, String alias) {
    return columnAggregate("AVG", column, alias);
  }

  /**
   * SELECT MAX(column)。
   * 
   * @param column 列名
   * @param alias 别名
   * @return 构建器本身
   */
  default T columnMax(CharSequence column, String alias) {
    return columnAggregate("MAX", column, alias);
  }

  /**
   * SELECT MIN(column)。
   * 
   * @param column 列名
   * @param alias 别名
   * @return 构建器本身
   */
  default T columnMin(CharSequence column, String alias) {
    return columnAggregate("MIN", column, alias);
  }

  /**
   * 启用 DISTINCT，去除重复行。
   * 
   * @return 构建器本身
   */
  default T distinct() {
    innerSelectHolder().setDistinct(true);
    return self();
  }

  /**
   * 条件 DISTINCT，仅当条件为真时启用。
   * 
   * @param condition 条件
   * @return 构建器本身
   */
  default T distinctIf(boolean condition) {
    if (condition) {
      distinct();
    }
    return self();
  }

  /**
   * SELECT 子句内部持有者，用于管理列选择和 DISTINCT 标志。
   * <p>
   * 该类用于统一管理 SELECT 子句的所有元素，包括：
   * <ul>
   * <li>列列表</li>
   * <li>DISTINCT 标志</li>
   * </ul>
   * <p>
   * 注意：此类为包私有，仅供 {@code cn.dinodev.sql.builder.clause} 包内使用。
   * 
   * @author Cody Lu
   * @since 2024-12-02
   */
  class InnerSelectHolder {
    private final List<CharSequence> columns = new java.util.ArrayList<>();
    private boolean distinct = false;

    /**
     * 添加列。
     * 
     * @param cols 列名或表达式
     */
    public void addColumns(CharSequence... cols) {
      if (cols != null && cols.length > 0) {
        for (CharSequence col : cols) {
          if (col != null && !col.isEmpty()) {
            columns.add(col);
          }
        }
      }
    }

    /**
     * 设置 DISTINCT 标志。
     * 
     * @param distinct 是否启用 DISTINCT
     */
    public void setDistinct(boolean distinct) {
      this.distinct = distinct;
    }

    /**
     * 获取 DISTINCT 标志。
     * 
     * @return DISTINCT 标志
     */
    public boolean isDistinct() {
      return distinct;
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
     * 构建 SELECT 子句 SQL 字符串。
     * 
     * @param sql SQL 字符串构建器
     * @param isCount 是否为计数查询
     */
    public void appendSql(StringBuilder sql, boolean isCount) {
      sql.append("SELECT ");

      if (distinct && !isCount) {
        sql.append("DISTINCT ");
      }

      if (isCount) {
        sql.append("count(1) AS cnt");
      } else if (columns.isEmpty()) {
        sql.append('*');
      } else {
        boolean first = true;
        for (CharSequence col : columns) {
          if (!first) {
            sql.append(", ");
          }
          first = false;
          sql.append(col);
        }
      }
    }
  }
}
