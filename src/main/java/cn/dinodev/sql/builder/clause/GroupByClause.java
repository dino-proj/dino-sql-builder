// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.dinodev.sql.SqlBuilder;

/**
 * GROUP BY 子句接口，提供分组相关的方法。
 * <p>
 * 支持链式调用，提供多种分组方式。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-12-01
 */
public interface GroupByClause<T extends SqlBuilder> extends ClauseSupport<T> {

  /**
   * 获取内部的 GROUP BY 持有者。
   * 警告：通常不建议直接操作此对象，除非有特殊需求。
   * 
   * @return 内部 GROUP BY 持有者
   */
  InnerGroupByHolder innerGroupByHolder();

  /**
   * 添加 GROUP BY 子句。
   * <p>
   * 支持多种格式：
   * <ul>
   *   <li>单列：groupBy("category")</li>
   *   <li>多列：groupBy("category", "status")</li>
   *   <li>逗号分隔：groupBy("category, status")</li>
   *   <li>表达式：groupBy("DATE(created_at)")</li>
   * </ul>
   * 
   * @param expressions 分组表达式
   * @return 构建器本身
   */
  default T groupBy(String... expressions) {
    innerGroupByHolder().addGroupBy(expressions);
    return self();
  }

  /**
   * 条件分组，仅当条件为真时添加分组。
   * 
   * @param condition 条件
   * @param expressions 分组表达式
   * @return 构建器本身
   */
  default T groupByIf(boolean condition, String... expressions) {
    if (condition) {
      groupBy(expressions);
    }
    return self();
  }

  /**
   * 使用 GROUP BY ALL 语法（PostgreSQL 17+）。
   * <p>
   * 自动将 SELECT 列表中所有非聚合列包含到 GROUP BY 中。
   * <p>
   * <b>注意：</b>此语法仅在 PostgreSQL 17+ 版本中可用，其他数据库不支持。
   * 
   * @return 构建器本身
   */
  default T groupByAll() {
    if (!dialect().supportsGroupByAll()) {
      throw new UnsupportedOperationException(
          "GROUP BY ALL is not supported by " + dialect().getDialectName() +
              " dialect. This syntax is only available in PostgreSQL 17+.");
    }
    groupBy("ALL");
    return self();
  }

  /**
   * 条件 GROUP BY ALL，仅当条件为真时使用 GROUP BY ALL 语法。
   * <p>
   * 自动将 SELECT 列表中所有非聚合列包含到 GROUP BY 中。
   * <p>
   * <b>注意：</b>此语法仅在 PostgreSQL 17+ 版本中可用，其他数据库不支持。
   * 
   * @param condition 条件
   * @return 构建器本身
   */
  default T groupByAllIf(boolean condition) {
    if (condition) {
      groupByAll();
    }
    return self();
  }

  /**
   * GROUP BY 子句内部持有者，用于管理分组表达式。
   * <p>
   * 该类用于统一管理所有分组表达式，提供清晰的接口和一致的 SQL 生成方式。
   * 
   * @author Cody Lu
   * @since 2024-12-31
   */
  class InnerGroupByHolder {
    private final List<String> groupByList = new ArrayList<>();

    /**
     * 添加分组表达式。
     * 
     * @param expressions 分组表达式数组
     */
    public void addGroupBy(String... expressions) {
      if (expressions != null && expressions.length > 0) {
        groupByList.addAll(Arrays.asList(expressions));
      }
    }

    /**
     * 获取所有分组表达式。
     * 
     * @return 分组表达式列表
     */
    public List<String> getGroupByList() {
      return groupByList;
    }

    /**
     * 判断是否为空。
     * 
     * @return 如果没有任何分组表达式则返回 true
     */
    public boolean isEmpty() {
      return groupByList.isEmpty();
    }

    /**
     * 获取分组表达式数量。
     * 
     * @return 分组表达式数量
     */
    public int size() {
      return groupByList.size();
    }

    /**
     * 清空所有分组表达式。
     */
    public void clear() {
      groupByList.clear();
    }

    /**
     * 构建 GROUP BY 子句 SQL 字符串。
     * <p>
     * 如果没有分组表达式，则不添加任何内容。
     * 
     * @param sql SQL 字符串构建器
     */
    public void appendSql(StringBuilder sql) {
      if (groupByList.isEmpty()) {
        return;
      }

      sql.append(" GROUP BY ");
      boolean first = true;
      for (String groupBy : groupByList) {
        if (!first) {
          sql.append(", ");
        }
        sql.append(groupBy);
        first = false;
      }
    }
  }
}
