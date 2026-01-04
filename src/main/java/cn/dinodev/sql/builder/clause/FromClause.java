// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import java.util.List;

import cn.dinodev.sql.SqlBuilder;

/**
 * FROM 子句接口，提供数据源选择方法。
 * <p>
 * 用于指定查询的数据源，包括表、带别名的表、子查询等。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-12-02
 */
public interface FromClause<T extends SqlBuilder> extends ClauseSupport<T> {

  /**
   * 获取内部的 FROM 持有者。
   * 警告：通常不建议直接操作此对象，除非有特殊需求。
   * 
   * @return 内部 FROM 持有者
   */
  InnerFromHolder innerFromHolder();

  /**
   * 添加表到 FROM 子句。
   * <p>
   * 示例：
   * <pre>
   * builder.from("user");
   * builder.from("order", "o");  // 带别名
   * </pre>
   * 
   * @param table 表名
   * @return 构建器本身
   */
  default T from(String table) {
    innerFromHolder().addTable(table, null);
    return self();
  }

  /**
   * 添加带别名的表到 FROM 子句。
   * 
   * @param table 表名
   * @param alias 表别名
   * @return 构建器本身
   */
  default T from(String table, String alias) {
    innerFromHolder().addTable(table, alias);
    return self();
  }

  /**
   * 添加子查询到 FROM 子句。
   * <p>
   * 示例：
   * <pre>
   * SelectSqlBuilder subQuery = SelectSqlBuilder.create(dialect, "orders")
   *     .column("customer_id", "SUM(amount) AS total")
   *     .groupBy("customer_id");
   * 
   * builder.fromSubQuery(subQuery, "order_totals");
   * </pre>
   * 
   * @param subQuery 子查询构建器
   * @param alias 子查询别名
   * @return 构建器本身
   */
  default T fromSubQuery(T subQuery, String alias) {
    innerFromHolder().addSubQuery(subQuery, alias);
    return self();
  }

  /**
   * 条件添加表，仅当条件为真时添加。
   * 
   * @param condition 条件
   * @param table 表名
   * @return 构建器本身
   */
  default T fromIf(boolean condition, String table) {
    if (condition) {
      from(table);
    }
    return self();
  }

  /**
   * 条件添加带别名的表，仅当条件为真时添加。
   * 
   * @param condition 条件
   * @param table 表名
   * @param alias 表别名
   * @return 构建器本身
   */
  default T fromIf(boolean condition, String table, String alias) {
    if (condition) {
      from(table, alias);
    }
    return self();
  }

  /**
   * FROM 子句内部持有者，用于管理数据源列表。
   * <p>
   * 该类用于统一管理 FROM 子句的所有数据源，包括：
   * <ul>
   * <li>普通表</li>
   * <li>带别名的表</li>
   * <li>子查询</li>
   * </ul>
   * <p>
   * 注意：此类为包私有，仅供 {@code cn.dinodev.sql.builder.clause} 包内使用。
   * 
   * @author Cody Lu
   * @since 2024-12-02
   */
  class InnerFromHolder {
    private final List<FromEntity> tables = new java.util.ArrayList<>();

    /**
     * 添加表。
     * 
     * @param table 表名
     * @param alias 表别名（可为 null）
     */
    public void addTable(String table, String alias) {
      if (table == null || table.trim().isEmpty()) {
        throw new IllegalArgumentException("Table name cannot be null or empty");
      }
      tables.add(new FromEntity(table, alias, null));
    }

    /**
     * 添加子查询。
     * 
     * @param subQuery 子查询
     * @param alias 子查询别名
     */
    public void addSubQuery(SqlBuilder subQuery, String alias) {
      if (subQuery == null) {
        throw new IllegalArgumentException("SubQuery cannot be null");
      }
      if (alias == null || alias.trim().isEmpty()) {
        throw new IllegalArgumentException("SubQuery alias cannot be null or empty");
      }
      tables.add(new FromEntity(null, alias, subQuery));
    }

    /**
     * 获取所有表。
     * 
     * @return 表实体列表
     */
    public List<FromEntity> getTables() {
      return tables;
    }

    /**
     * 判断是否为空。
     * 
     * @return 如果没有任何表则返回 true
     */
    public boolean isEmpty() {
      return tables.isEmpty();
    }

    /**
     * 获取表数量。
     * 
     * @return 表数量
     */
    public int size() {
      return tables.size();
    }

    /**
     * 构建 FROM 子句 SQL 字符串。
     * 
     * @param sql SQL 字符串构建器
     */
    public void appendSql(StringBuilder sql) {
      if (tables.isEmpty()) {
        return;
      }

      sql.append(" FROM ");
      boolean first = true;
      for (FromEntity entity : tables) {
        if (!first) {
          sql.append(", ");
        }
        first = false;

        if (entity.isSubQuery()) {
          sql.append("( ")
              .append(entity.getSubQuery().getSql())
              .append(" ) AS ")
              .append(entity.getAlias());
        } else {
          sql.append(entity.getTable());
          if (entity.getAlias() != null && !entity.getAlias().isEmpty()) {
            sql.append(" AS ").append(entity.getAlias());
          }
        }
      }
    }

    /**
     * 将所有子查询的参数添加到给定的参数列表中。
     * 
     * @param params 参数列表
     */
    public void appendParams(List<Object> params) {
      for (FromEntity entity : tables) {
        if (entity.isSubQuery()) {
          for (Object param : entity.getSubQuery().getParams()) {
            params.add(param);
          }
        }
      }
    }

    /**
     * 获取参数个数。
     * 
     * @return 参数个数
     */
    public int getParamsCount() {
      int count = 0;
      for (FromEntity entity : tables) {
        if (entity.isSubQuery()) {
          count += entity.getSubQuery().getParamCount();
        }
      }
      return count;
    }

    /**
     * FROM 实体类，表示一个数据源（表或子查询）。
     * 
     * @author Cody Lu
     * @since 2024-12-02
     */
    public static class FromEntity {
      private final String table;
      private final String alias;
      private final SqlBuilder subQuery;

      /**
       * 构造 FromEntity 实例。
       * 
       * @param table 表名（子查询时为 null）
       * @param alias 别名
       * @param subQuery 子查询（表时为 null）
       */
      public FromEntity(String table, String alias, SqlBuilder subQuery) {
        this.table = table;
        this.alias = alias;
        this.subQuery = subQuery;
      }

      /**
       * 获取表名。
       * 
       * @return 表名
       */
      public String getTable() {
        return table;
      }

      /**
       * 获取别名。
       * 
       * @return 别名
       */
      public String getAlias() {
        return alias;
      }

      /**
       * 获取子查询。
       * 
       * @return 子查询
       */
      public SqlBuilder getSubQuery() {
        return subQuery;
      }

      /**
       * 判断是否为子查询。
       * 
       * @return 如果是子查询则返回 true
       */
      public boolean isSubQuery() {
        return subQuery != null;
      }
    }
  }
}
