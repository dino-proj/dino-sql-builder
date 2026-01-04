// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import java.util.List;
import java.util.stream.Stream;

import cn.dinodev.sql.SqlBuilder;

/**
 * UNION 子句接口，提供联合查询方法。
 * <p>
 * 用于合并多个 SELECT 查询的结果集。
 * UNION 会自动去重，UNION ALL 保留重复行。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-12-02
 */
public interface UnionClause<T extends SqlBuilder> extends ClauseSupport<T> {

  /**
   * UNION 类型枚举。
   * 
   * @since 2024-12-31
   */
  enum UnionType {
    /** UNION（去重） */
    UNION("UNION"),
    /** UNION ALL（保留重复） */
    UNION_ALL("UNION ALL");

    private final String sql;

    UnionType(String sql) {
      this.sql = sql;
    }

    /**
     * 获取 SQL 关键字。
     * 
     * @return SQL 关键字
     */
    public String getSql() {
      return sql;
    }
  }

  /**
   * 获取内部的 UNION 持有者。
   * 警告：通常不建议直接操作此对象，除非有特殊需求。
   * 
   * @return 内部 UNION 持有者
   */
  InnerUnionHolder innerUnionHolder();

  /**
   * UNION 联合查询（去重）。
   * <p>
   * 示例：
   * <pre>
   * SelectSqlBuilder query1 = SelectSqlBuilder.create(dialect, "orders")
   *     .column("id", "amount")
   *     .where("status", "=", "completed");
   * 
   * SelectSqlBuilder query2 = SelectSqlBuilder.create(dialect, "refunds")
   *     .column("id", "amount")
   *     .where("status", "=", "approved");
   * 
   * SelectSqlBuilder combined = query1.union(query2);
   * </pre>
   * 
   * @param subQuery 子查询构建器
   * @return 构建器本身
   */
  default T union(T subQuery) {
    innerUnionHolder().addUnion(UnionType.UNION, subQuery);
    return self();
  }

  /**
   * UNION ALL 联合查询（保留重复）。
   * <p>
   * 示例：
   * <pre>
   * SelectSqlBuilder query1 = SelectSqlBuilder.create(dialect, "orders")
   *     .column("id", "amount")
   *     .where("year", "=", 2023);
   * 
   * SelectSqlBuilder query2 = SelectSqlBuilder.create(dialect, "orders")
   *     .column("id", "amount")
   *     .where("year", "=", 2024);
   * 
   * SelectSqlBuilder combined = query1.unionAll(query2);
   * </pre>
   * 
   * @param subQuery 子查询构建器
   * @return 构建器本身
   */
  default T unionAll(T subQuery) {
    innerUnionHolder().addUnion(UnionType.UNION_ALL, subQuery);
    return self();
  }

  /**
   * 条件 UNION，仅当条件为真时执行。
   * 
   * @param condition 条件
   * @param subQuery 子查询构建器
   * @return 构建器本身
   */
  default T unionIf(boolean condition, T subQuery) {
    if (condition) {
      union(subQuery);
    }
    return self();
  }

  /**
   * 条件 UNION ALL，仅当条件为真时执行。
   * 
   * @param condition 条件
   * @param subQuery 子查询构建器
   * @return 构建器本身
   */
  default T unionAllIf(boolean condition, T subQuery) {
    if (condition) {
      unionAll(subQuery);
    }
    return self();
  }

  /**
   * UNION 子句内部持有者，用于管理多个 UNION 子句。
   * <p>
   * 该类用于统一管理所有 UNION 实体。
   * 
   * @author Cody Lu
   * @since 2024-12-31
   */
  public class InnerUnionHolder {
    private final List<UnionEntity> unionEntities = new java.util.ArrayList<>();

    /**
     * 添加一个 UNION 实体。
     * 
     * @param entity UNION 实体
     */
    public void add(UnionEntity entity) {
      if (entity != null) {
        unionEntities.add(entity);
      }
    }

    /**
     * 添加 UNION 子句。
     * 
     * @param type UNION 类型
     * @param subQuery 子查询
     */
    public void addUnion(UnionType type, SqlBuilder subQuery) {
      if (type == null) {
        throw new IllegalArgumentException("Union type cannot be null");
      }
      if (subQuery == null) {
        throw new IllegalArgumentException("SubQuery cannot be null");
      }
      unionEntities.add(new UnionEntity(type, subQuery));
    }

    /**
     * 获取所有 UNION 实体。
     * 
     * @return UNION 实体列表
     */
    public List<UnionEntity> getUnionEntities() {
      return unionEntities;
    }

    /**
     * 判断是否为空。
     * 
     * @return 如果没有任何 UNION 实体则返回 true
     */
    public boolean isEmpty() {
      return unionEntities.isEmpty();
    }

    /**
     * 获取 UNION 实体数量。
     * 
     * @return UNION 实体数量
     */
    public int size() {
      return unionEntities.size();
    }

    /**
     * 构建 UNION 子句 SQL 字符串。
     * <p>
     * 格式：
     * <pre>
     * SELECT ... FROM table1
     * UNION
     * SELECT ... FROM table2
     * UNION ALL
     * SELECT ... FROM table3
     * </pre>
     * 
     * @param sql SQL 字符串构建器
     */
    public void appendSql(StringBuilder sql) {
      if (unionEntities.isEmpty()) {
        return;
      }

      for (UnionEntity entity : unionEntities) {
        sql.append("\n")
            .append(entity.getType().getSql())
            .append("\n")
            .append(entity.getSubQuery().getSql());
      }
    }

    /**
     * 将所有 UNION 实体的参数添加到给定的参数列表中。
     * 
     * @param params 参数列表
     */
    public void appendParams(List<Object> params) {
      for (UnionEntity entity : unionEntities) {
        Stream.of(entity.getSubQuery().getParams()).forEach(params::add);
      }
    }

    /**
     * 获取参数个数。
     * 
     * @return 参数个数
     */
    public int getParamsCount() {
      int count = 0;
      for (UnionEntity entity : unionEntities) {
        count += entity.getSubQuery().getParamCount();
      }
      return count;
    }

    /**
     * UNION 实体类，表示一个 UNION 操作。
     * <p>
     * 包含 UNION 类型和子查询。
     * 
     * @author Cody Lu
     * @since 2024-12-31
     */
    class UnionEntity {
      private final UnionType type;
      private final SqlBuilder subQuery;

      /**
       * 构造 UnionEntity 实例。
       * 
       * @param type UNION 类型
       * @param subQuery 子查询
       */
      public UnionEntity(UnionType type, SqlBuilder subQuery) {
        if (type == null) {
          throw new IllegalArgumentException("Union type cannot be null");
        }
        if (subQuery == null) {
          throw new IllegalArgumentException("SubQuery cannot be null");
        }
        this.type = type;
        this.subQuery = subQuery;
      }

      /**
       * 获取 UNION 类型。
       * 
       * @return UNION 类型
       */
      public UnionType getType() {
        return type;
      }

      /**
       * 获取子查询。
       * 
       * @return 子查询
       */
      public SqlBuilder getSubQuery() {
        return subQuery;
      }

      @Override
      public String toString() {
        return "\n" + type.getSql() + "\n" + subQuery.getSql();
      }
    }
  }
}
