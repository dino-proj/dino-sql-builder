// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import java.util.List;
import java.util.stream.Stream;

import cn.dinodev.sql.MaterializationHint;
import cn.dinodev.sql.SqlBuilder;

/**
 * WITH 子句接口（CTE - Common Table Expression），提供公共表表达式支持。
 * <p>
 * WITH 子句允许定义临时命名结果集，可以在主查询中引用。
 * 适用于简化复杂查询或递归查询。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-12-04
 */
public interface WithClause<T extends SqlBuilder> extends ClauseSupport<T> {

  /**
   * 获取内部的 WITH 持有者。
   * 警告：通常不建议直接操作此对象，除非有特殊需求。
   * 
   * @return 内部 WITH 持有者
   */
  InnerWithHolder innerWithHolder();

  /**
   * 定义 WITH 子句（CTE）。
   * <p>
   * 示例：
   * <pre>
   * SelectSqlBuilder cte = SelectSqlBuilder.create(dialect, "orders")
  *     .columns("customer_id", "SUM(amount) AS total")
   *     .groupBy("customer_id");
   * 
   * SelectSqlBuilder main = SelectSqlBuilder.create(dialect, "customers")
   *     .with("order_totals", cte)
   *     .join("order_totals", "ot", "customers.id = ot.customer_id");
   * </pre>
   * 
   * @param name CTE 名称
   * @param subQuery 子查询构建器
   * @return 构建器本身
   */
  default T with(String name, T subQuery) {
    innerWithHolder().addWith(name, subQuery);
    return self();
  }

  /**
   * 定义带物化提示的 WITH 子句（PostgreSQL 12+）。
   * <p>
   * 示例：
   * <pre>
   * // 强制物化，适用于多次引用的 CTE
   * builder.with("expensive_cte", subQuery, MaterializationHint.MATERIALIZED);
   * 
   * // 禁止物化，适用于简单的 CTE
   * builder.with("simple_cte", subQuery, MaterializationHint.NOT_MATERIALIZED);
   * </pre>
   * 
   * @param name CTE 名称
   * @param subQuery 子查询构建器
   * @param hint 物化提示
   * @return 构建器本身
   */
  default T with(String name, T subQuery, MaterializationHint hint) {
    // 检查是否支持物化提示
    if (hint != MaterializationHint.NONE) {
      // 这里可以添加对方言的检查，如果不支持则抛出异常或忽略提示
      if (!dialect().supportsMaterializedCTE()) {
        throw new UnsupportedOperationException(
            "The current dialect does not support MATERIALIZED/NOT MATERIALIZED hints.");
      }
    }
    innerWithHolder().addWith(name, subQuery, hint);
    return self();
  }

  /**
   * 定义递归 WITH 子句（RECURSIVE CTE）。
   * <p>
   * 用于递归查询，如树形结构遍历。
   * 
   * @param name CTE 名称
   * @param subQuery 子查询构建器
   * @return 构建器本身
   */
  default T withRecursive(String name, T subQuery) {
    innerWithHolder().addRecursiveWith(name, subQuery);
    return self();
  }

  /**
   * 定义带物化提示的递归 WITH 子句（PostgreSQL 12+）。
   * 
   * @param name CTE 名称
   * @param subQuery 子查询构建器
   * @param hint 物化提示
   * @return 构建器本身
   */
  default T withRecursive(String name, T subQuery, MaterializationHint hint) {
    // 检查是否支持物化提示
    if (hint != MaterializationHint.NONE) {
      // 这里可以添加对方言的检查，如果不支持则抛出异常或忽略提示
      if (!dialect().supportsMaterializedCTE()) {
        throw new UnsupportedOperationException(
            "The current dialect does not support MATERIALIZED/NOT MATERIALIZED hints.");
      }
    }

    innerWithHolder().addRecursiveWith(name, subQuery, hint);
    return self();
  }

  /**
   * 条件 WITH，仅当条件为真时添加。
   * 
   * @param condition 条件
   * @param name CTE 名称
   * @param subQuery 子查询构建器
   * @return 构建器本身
   */
  default T withIf(boolean condition, String name, T subQuery) {
    if (condition) {
      with(name, subQuery);
    }
    return self();
  }

  /**
   * 条件递归 WITH，仅当条件为真时添加。
   * 
   * @param condition 条件
   * @param name CTE 名称
   * @param subQuery 子查询构建器
   * @return 构建器本身
   */
  default T withRecursiveIf(boolean condition, String name, T subQuery) {
    if (condition) {
      withRecursive(name, subQuery);
    }
    return self();
  }

  /**
   * WITH 子句内部持有者，用于管理多个 WITH 子句。
   * <p>
   * 该类用于在需要支持多个 CTE 的场景中，统一管理所有 WITH 实体。
   * <p>
   * 注意：此类为包私有，仅供 {@code cn.dinodev.sql.builder.clause} 包内使用。
   * 
   * @author Cody Lu
   * @since 2024-12-04
   */
  class InnerWithHolder {
    private final List<WithEntity> withEntities = new java.util.ArrayList<>();

    /**
     * 添加一个 WITH 实体。
     * 
     * @param entity WITH 实体
     */
    public void add(WithEntity entity) {
      if (entity != null) {
        withEntities.add(entity);
      }
    }

    /**
     * 添加普通 WITH 子句。
     * 
     * @param name CTE 名称
     * @param subQuery 子查询
     */
    public void addWith(String name, SqlBuilder subQuery) {
      withEntities.add(new WithEntity(name, subQuery, false, MaterializationHint.NONE));
    }

    /**
     * 添加带物化提示的普通 WITH 子句。
     * 
     * @param name CTE 名称
     * @param subQuery 子查询
     * @param hint 物化提示
     */
    public void addWith(String name, SqlBuilder subQuery, MaterializationHint hint) {
      withEntities.add(new WithEntity(name, subQuery, false, hint));
    }

    /**
     * 添加递归 WITH 子句。
     * 
     * @param name CTE 名称
     * @param subQuery 子查询
     */
    public void addRecursiveWith(String name, SqlBuilder subQuery) {
      withEntities.add(new WithEntity(name, subQuery, true, MaterializationHint.NONE));
    }

    /**
     * 添加带物化提示的递归 WITH 子句。
     * 
     * @param name CTE 名称
     * @param subQuery 子查询
     * @param hint 物化提示
     */
    public void addRecursiveWith(String name, SqlBuilder subQuery, MaterializationHint hint) {
      withEntities.add(new WithEntity(name, subQuery, true, hint));
    }

    /**
     * 获取所有 WITH 实体。
     * 
     * @return WITH 实体列表
     */
    public List<WithEntity> getWithEntities() {
      return withEntities;
    }

    /**
     * 判断是否为空。
     * 
     * @return 如果没有任何 WITH 实体则返回 true
     */
    public boolean isEmpty() {
      return withEntities.isEmpty();
    }

    /**
     * 获取 WITH 实体数量。
     * 
     * @return WITH 实体数量
     */
    public int size() {
      return withEntities.size();
    }

    /**
     * 构建 WITH 子句 SQL 字符串。
     * <p>
     * 注意：如果包含任何递归 CTE，RECURSIVE 关键字会添加在 WITH 之后，
     * 这使得整个 WITH 子句中的所有 CTE 都可以使用递归特性。
     * <p>
     * 支持 MATERIALIZED/NOT MATERIALIZED 提示（PostgreSQL 12+）。
     * 
     * @param sql SQL 字符串构建器
     */
    public void appendSql(StringBuilder sql) {
      if (withEntities.isEmpty()) {
        return;
      }

      // 检查是否存在递归 CTE
      boolean hasRecursive = withEntities.stream().anyMatch(WithEntity::isRecursive);

      sql.append("WITH ");
      if (hasRecursive) {
        sql.append("RECURSIVE ");
      }

      boolean first = true;
      for (WithEntity entity : withEntities) {
        if (!first) {
          sql.append(",\n");
        }
        first = false;

        sql.append(entity.getName()).append(" AS ");

        // 添加物化提示（PostgreSQL 12+）
        if (entity.getMaterializationHint() == MaterializationHint.MATERIALIZED) {
          sql.append("MATERIALIZED ");
        } else if (entity.getMaterializationHint() == MaterializationHint.NOT_MATERIALIZED) {
          sql.append("NOT MATERIALIZED ");
        }

        sql.append("(\n")
            .append(entity.getSubQuery().getSql())
            .append("\n)");
      }
      sql.append("\n");
    }

    /**
     * 将所有 WITH 实体的参数添加到给定的参数列表中。
     * 
     * @param params 参数列表
     */
    public void appendParams(List<Object> params) {
      for (WithEntity entity : withEntities) {
        Stream.of(entity.getSubQuery().getParams()).forEach(params::add);
      }
    }

    /**
     * 获取参数个数
     * return 参数个数
     */
    public int getParamsCount() {
      int count = 0;
      for (WithEntity entity : withEntities) {
        count += entity.getSubQuery().getParamCount();
      }
      return count;
    }

    /**
     * WITH 实体类，表示一个 CTE（公共表表达式）。
     * <p>
     * 包含 CTE 名称、子查询、是否递归的标志以及物化提示。
     * 
     * @author Cody Lu
     * @since 2024-12-04
     */
    class WithEntity {
      private final String name;
      private final SqlBuilder subQuery;
      private final boolean recursive;
      private final MaterializationHint materializationHint;

      /**
       * 构造 WithEntity 实例。
       * 
       * @param name CTE 名称
       * @param subQuery 子查询
       * @param recursive 是否递归
       * @param materializationHint 物化提示
       */
      public WithEntity(String name, SqlBuilder subQuery, boolean recursive, MaterializationHint materializationHint) {
        if (name == null || name.trim().isEmpty()) {
          throw new IllegalArgumentException("CTE name cannot be null or empty");
        }
        if (subQuery == null) {
          throw new IllegalArgumentException("SubQuery cannot be null");
        }
        this.name = name;
        this.subQuery = subQuery;
        this.recursive = recursive;
        this.materializationHint = materializationHint != null ? materializationHint : MaterializationHint.NONE;
      }

      /**
       * 获取 CTE 名称。
       * 
       * @return CTE 名称
       */
      public String getName() {
        return name;
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
       * 判断是否为递归 CTE。
       * 
       * @return 如果是递归 CTE 则返回 true
       */
      public boolean isRecursive() {
        return recursive;
      }

      /**
       * 获取物化提示。
       * 
       * @return 物化提示
       */
      public MaterializationHint getMaterializationHint() {
        return materializationHint;
      }

    }

  }
}