// Copyright 2025 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.dinodev.sql.Logic;
import cn.dinodev.sql.Oper;
import cn.dinodev.sql.SqlBuilder;

/**
 * HAVING 子句接口，提供分组后的条件过滤方法。
 * <p>
 * HAVING 子句用于过滤分组后的数据，通常与 GROUP BY 一起使用。
 * 支持链式调用，提供类似 WHERE 的条件表达式。
 * <p>
 * 支持多种使用方式：
 * <ul>
 *   <li>基本聚合条件：havingCountGt(10)</li>
 *   <li>自定义表达式：having("SUM(amount) > 1000")</li>
 *   <li>带参数：having("COUNT(*) > ?", 5)</li>
 *   <li>使用操作符：having("SUM(price)", Oper.GTE, 100)</li>
 *   <li>条件添加：havingIf(condition, "AVG(score) >= 60")</li>
 *   <li>逻辑连接：andHaving(...), orHaving(...)</li>
 * </ul>
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2025-12-01
 */
public interface HavingClause<T extends SqlBuilder> extends ClauseSupport<T> {

  /**
   * 获取内部的 HAVING 持有者。
   * 警告：通常不建议直接操作此对象，除非有特殊需求。
   * 
   * @return 内部 HAVING 持有者
   */
  InnerHavingHolder innerHavingHolder();

  /**
   * 添加 HAVING 条件（带参数）。
   * <p>
   * 支持多种表达式格式：
   * <ul>
   *   <li>聚合函数：having("COUNT(*) > 10")</li>
   *   <li>带参数：having("SUM(amount) > ?", 1000)</li>
   *   <li>复杂表达式：having("AVG(price) BETWEEN ? AND ?", 50, 100)</li>
   *   <li>多个聚合：having("COUNT(*) > 5 AND SUM(total) > 1000")</li>
   * </ul>
   * <p>
   * 示例：
   * <pre>
   * // 基本聚合条件
   * builder.having("COUNT(*) > 10");
   * 
   * // 带参数
   * builder.having("SUM(amount) > ?", 1000);
   * 
   * // 复杂表达式
   * builder.having("AVG(score) >= ? AND MAX(score) <= ?", 60, 100);
   * </pre>
   * 
   * @param expression HAVING 表达式，如 "COUNT(*) &gt; ?"
   * @param params 参数值
   * @return 构建器本身
   */
  default T having(String expression, Object... params) {
    innerHavingHolder().addCondition(Logic.AND, expression, params);
    return self();
  }

  /**
   * 添加 HAVING 条件（使用操作符）。
   * <p>
   * 示例：
   * <pre>
   * // 使用操作符
   * builder.having("COUNT(*)", Oper.GT, 10);
   * builder.having("SUM(amount)", Oper.GTE, 1000);
   * builder.having("AVG(score)", Oper.BETWEEN, new Object[]{60, 100});
   * </pre>
   * 
   * @param aggregateExpression 聚合表达式，如 "COUNT(*)", "SUM(amount)"
   * @param operator 操作符（GT, GTE, LT, LTE, EQ, NE, BETWEEN 等）
   * @param value 参数值（对于 BETWEEN 使用数组或 List）
   * @return 构建器本身
   */
  default T having(String aggregateExpression, Oper operator, Object value) {
    return having(operator.makeExpr(aggregateExpression), value);
  }

  /**
   * 添加 HAVING 条件，使用 AND 连接。
   * <p>
   * 示例：
   * <pre>
   * builder.having("COUNT(*) > 10")
   *        .andHaving("SUM(amount) > ?", 1000)
   *        .andHaving("AVG(price) >= ?", 50);
   * </pre>
   * 
   * @param expression HAVING 表达式
   * @param params 参数值
   * @return 构建器本身
   */
  default T andHaving(String expression, Object... params) {
    return having(expression, params);
  }

  /**
   * 添加 HAVING 条件，使用 OR 连接。
   * <p>
   * 示例：
   * <pre>
   * builder.having("COUNT(*) > 100")
   *        .orHaving("SUM(amount) > ?", 10000);
   * </pre>
   * 
   * @param expression HAVING 表达式
   * @param params 参数值
   * @return 构建器本身
   */
  default T orHaving(String expression, Object... params) {
    innerHavingHolder().addCondition(Logic.OR, expression, params);
    return self();
  }

  /**
   * 条件 HAVING，仅当条件为真时添加。
   * <p>
   * 示例：
   * <pre>
   * builder.havingIf(needFilter, "COUNT(*) > ?", 10)
   *        .havingIf(checkAmount, "SUM(amount) > ?", 1000);
   * </pre>
   * 
   * @param condition 条件
   * @param expression HAVING 表达式
   * @param params 参数值
   * @return 构建器本身
   */
  default T havingIf(boolean condition, String expression, Object... params) {
    if (condition) {
      having(expression, params);
    }
    return self();
  }

  /**
   * 条件 HAVING（使用操作符），仅当条件为真时添加。
   * <p>
   * 示例：
   * <pre>
   * builder.havingIf(needFilter, "COUNT(*)", Oper.GT, 10);
   * </pre>
   * 
   * @param condition 条件
   * @param aggregateExpression 聚合表达式
   * @param operator 操作符
   * @param value 参数值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingIf(boolean condition, String aggregateExpression, Oper operator, Object value) {
    if (condition) {
      having(aggregateExpression, operator, value);
    }
    return self();
  }

  /**
   * 条件 AND HAVING，仅当条件为真时添加。
   * <p>
   * 示例：
   * <pre>
   * builder.andHavingIf(needFilter, "SUM(amount) > ?", 1000);
   * </pre>
   * 
   * @param condition 条件
   * @param expression HAVING 表达式
   * @param params 参数值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T andHavingIf(boolean condition, String expression, Object... params) {
    if (condition) {
      andHaving(expression, params);
    }
    return self();
  }

  /**
   * 条件 OR HAVING，仅当条件为真时添加。
   * <p>
   * 示例：
   * <pre>
   * builder.orHavingIf(needFilter, "MAX(price) < ?", 100);
   * </pre>
   * 
   * @param condition 条件
   * @param expression HAVING 表达式
   * @param params 参数值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T orHavingIf(boolean condition, String expression, Object... params) {
    if (condition) {
      orHaving(expression, params);
    }
    return self();
  }

  // ==================== COUNT 相关便捷方法 ====================

  /**
   * HAVING COUNT(*) 大于指定值。
   * <p>
   * 示例：
   * <pre>
   * builder.groupBy("category")
   *        .havingCountGt(10);  // HAVING COUNT(*) > 10
   * </pre>
   * 
   * @param value 值
   * @return 构建器本身
   */
  default T havingCountGt(long value) {
    return having("COUNT(*)", Oper.GT, value);
  }

  /**
   * HAVING COUNT(*) 大于等于指定值。
   * <p>
   * 示例：
   * <pre>
   * builder.havingCountGte(5);  // HAVING COUNT(*) >= 5
   * </pre>
   * 
   * @param value 值
   * @return 构建器本身
   */
  default T havingCountGte(long value) {
    return having("COUNT(*)", Oper.GTE, value);
  }

  /**
   * HAVING COUNT(*) 小于指定值。
   * <p>
   * 示例：
   * <pre>
   * builder.havingCountLt(100);  // HAVING COUNT(*) < 100
   * </pre>
   * 
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingCountLt(long value) {
    return having("COUNT(*)", Oper.LT, value);
  }

  /**
   * HAVING COUNT(*) 小于等于指定值。
   * <p>
   * 示例：
   * <pre>
   * builder.havingCountLte(50);  // HAVING COUNT(*) <= 50
   * </pre>
   * 
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingCountLte(long value) {
    return having("COUNT(*)", Oper.LTE, value);
  }

  /**
   * HAVING COUNT(*) 等于指定值。
   * <p>
   * 示例：
   * <pre>
   * builder.havingCountEq(10);  // HAVING COUNT(*) = 10
   * </pre>
   * 
   * @param value 值
   * @return 构建器本身
   */
  default T havingCountEq(long value) {
    return having("COUNT(*)", Oper.EQ, value);
  }

  /**
   * HAVING COUNT(*) 不等于指定值。
   * <p>
   * 示例：
   * <pre>
   * builder.havingCountNe(0);  // HAVING COUNT(*) != 0
   * </pre>
   * 
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingCountNe(long value) {
    return having("COUNT(*)", Oper.NE, value);
  }

  /**
   * HAVING COUNT(column) 大于指定值。
   * <p>
   * 示例：
   * <pre>
   * builder.havingCountGt("email", 10);  // HAVING COUNT(email) > 10
   * </pre>
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingCountGt(String column, long value) {
    return having("COUNT(" + column + ")", Oper.GT, value);
  }

  /**
   * HAVING COUNT(column) 大于等于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingCountGte(String column, long value) {
    return having("COUNT(" + column + ")", Oper.GTE, value);
  }

  // ==================== SUM 相关便捷方法 ====================

  /**
   * HAVING SUM(column) 大于指定值。
   * <p>
   * 示例：
   * <pre>
   * builder.havingSumGt("amount", 1000);  // HAVING SUM(amount) > 1000
   * </pre>
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingSumGt(String column, Number value) {
    return having("SUM(" + column + ")", Oper.GT, value);
  }

  /**
   * HAVING SUM(column) 大于等于指定值。
   * <p>
   * 示例：
   * <pre>
   * builder.havingSumGte("amount", 1000);  // HAVING SUM(amount) >= 1000
   * </pre>
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingSumGte(String column, Number value) {
    return having("SUM(" + column + ")", Oper.GTE, value);
  }

  /**
   * HAVING SUM(column) 小于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingSumLt(String column, Number value) {
    return having("SUM(" + column + ")", Oper.LT, value);
  }

  /**
   * HAVING SUM(column) 小于等于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingSumLte(String column, Number value) {
    return having("SUM(" + column + ")", Oper.LTE, value);
  }

  /**
   * HAVING SUM(column) 等于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingSumEq(String column, Number value) {
    return having("SUM(" + column + ")", Oper.EQ, value);
  }

  // ==================== AVG 相关便捷方法 ====================

  /**
   * HAVING AVG(column) 大于指定值。
   * <p>
   * 示例：
   * <pre>
   * builder.havingAvgGt("score", 60);  // HAVING AVG(score) > 60
   * </pre>
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingAvgGt(String column, Number value) {
    return having("AVG(" + column + ")", Oper.GT, value);
  }

  /**
   * HAVING AVG(column) 大于等于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingAvgGte(String column, Number value) {
    return having("AVG(" + column + ")", Oper.GTE, value);
  }

  /**
   * HAVING AVG(column) 小于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingAvgLt(String column, Number value) {
    return having("AVG(" + column + ")", Oper.LT, value);
  }

  /**
   * HAVING AVG(column) 小于等于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingAvgLte(String column, Number value) {
    return having("AVG(" + column + ")", Oper.LTE, value);
  }

  /**
   * HAVING AVG(column) 等于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingAvgEq(String column, Number value) {
    return having("AVG(" + column + ")", Oper.EQ, value);
  }

  // ==================== MAX 相关便捷方法 ====================

  /**
   * HAVING MAX(column) 大于指定值。
   * <p>
   * 示例：
   * <pre>
   * builder.havingMaxGt("price", 100);  // HAVING MAX(price) > 100
   * </pre>
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingMaxGt(String column, Object value) {
    return having("MAX(" + column + ")", Oper.GT, value);
  }

  /**
   * HAVING MAX(column) 大于等于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingMaxGte(String column, Object value) {
    return having("MAX(" + column + ")", Oper.GTE, value);
  }

  /**
   * HAVING MAX(column) 小于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingMaxLt(String column, Object value) {
    return having("MAX(" + column + ")", Oper.LT, value);
  }

  /**
   * HAVING MAX(column) 小于等于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingMaxLte(String column, Object value) {
    return having("MAX(" + column + ")", Oper.LTE, value);
  }

  /**
   * HAVING MAX(column) 等于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingMaxEq(String column, Object value) {
    return having("MAX(" + column + ")", Oper.EQ, value);
  }

  // ==================== MIN 相关便捷方法 ====================

  /**
   * HAVING MIN(column) 大于指定值。
   * <p>
   * 示例：
   * <pre>
   * builder.havingMinGt("price", 10);  // HAVING MIN(price) > 10
   * </pre>
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingMinGt(String column, Object value) {
    return having("MIN(" + column + ")", Oper.GT, value);
  }

  /**
   * HAVING MIN(column) 大于等于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingMinGte(String column, Object value) {
    return having("MIN(" + column + ")", Oper.GTE, value);
  }

  /**
   * HAVING MIN(column) 小于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingMinLt(String column, Object value) {
    return having("MIN(" + column + ")", Oper.LT, value);
  }

  /**
   * HAVING MIN(column) 小于等于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingMinLte(String column, Object value) {
    return having("MIN(" + column + ")", Oper.LTE, value);
  }

  /**
   * HAVING MIN(column) 等于指定值。
   * 
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingMinEq(String column, Object value) {
    return having("MIN(" + column + ")", Oper.EQ, value);
  }

  // ==================== 条件聚合便捷方法 ====================

  /**
   * 条件 HAVING COUNT(*) 大于指定值。
   * 
   * @param condition 条件
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingCountGtIf(boolean condition, long value) {
    return havingIf(condition, "COUNT(*)", Oper.GT, value);
  }

  /**
   * 条件 HAVING SUM(column) 大于指定值。
   * 
   * @param condition 条件
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingSumGtIf(boolean condition, String column, Number value) {
    return havingIf(condition, "SUM(" + column + ")", Oper.GT, value);
  }

  /**
   * 条件 HAVING AVG(column) 大于等于指定值。
   * 
   * @param condition 条件
   * @param column 列名
   * @param value 值
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T havingAvgGteIf(boolean condition, String column, Number value) {
    return havingIf(condition, "AVG(" + column + ")", Oper.GTE, value);
  }

  /**
   * HAVING 子句内部持有者，用于管理所有 HAVING 条件。
   * <p>
   * 该类用于统一管理所有 HAVING 表达式及其逻辑连接（AND/OR）。
   * 
   * @author Cody Lu
   * @since 2024-12-31
   */
  public class InnerHavingHolder {
    private final List<HavingCondition> conditions = new ArrayList<>();

    /**
     * 添加 HAVING 条件。
     * 
     * @param logic 逻辑连接符（AND 或 OR）
     * @param expression HAVING 表达式
     * @param params 参数值
     */
    public void addCondition(Logic logic, String expression, Object... params) {
      if (expression == null || expression.trim().isEmpty()) {
        throw new IllegalArgumentException("HAVING expression cannot be null or empty");
      }
      conditions.add(new HavingCondition(logic, expression.trim(), params));
    }

    /**
     * 获取所有 HAVING 条件。
     * 
     * @return HAVING 条件列表（不可修改）
     */
    public List<HavingCondition> getConditions() {
      return Collections.unmodifiableList(conditions);
    }

    /**
     * 清空所有 HAVING 条件。
     */
    public void clear() {
      conditions.clear();
    }

    /**
     * 判断是否为空。
     * 
     * @return 如果没有任何 HAVING 条件则返回 true
     */
    public boolean isEmpty() {
      return conditions.isEmpty();
    }

    /**
     * 获取 HAVING 条件数量。
     * 
     * @return HAVING 条件数量
     */
    public int size() {
      return conditions.size();
    }

    /**
     * 构建 HAVING 子句 SQL 字符串。
     * <p>
     * 格式：
     * <pre>
     * HAVING condition1 AND condition2 OR condition3
     * </pre>
     * 
     * @param sql SQL 字符串构建器
     */
    public void appendSql(StringBuilder sql) {
      if (conditions.isEmpty()) {
        return;
      }

      sql.append(" HAVING ");
      for (int i = 0; i < conditions.size(); i++) {
        HavingCondition condition = conditions.get(i);
        if (i > 0) {
          sql.append(" ").append(condition.getLogic().name()).append(" ");
        }
        sql.append(condition.getExpression());
      }
    }

    /**
     * HAVING 条件封装类。
     */
    public static class HavingCondition {
      private final Logic logic;
      private final String expression;
      private final Object[] params;

      public HavingCondition(Logic logic, String expression, Object... params) {
        this.logic = logic;
        this.expression = expression;
        this.params = params;
      }

      public Logic getLogic() {
        return logic;
      }

      public String getExpression() {
        return expression;
      }

      public Object[] getParams() {
        return params;
      }
    }
  }
}
