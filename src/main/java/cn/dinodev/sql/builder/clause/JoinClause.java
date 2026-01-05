// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import java.util.List;

import cn.dinodev.sql.SqlBuilder;

/**
 * JOIN 子句接口，提供表连接相关的方法。
 * <p>
 * 支持 INNER JOIN、LEFT JOIN、RIGHT JOIN、FULL JOIN 等多种连接方式。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-12-01
 */
public interface JoinClause<T extends SqlBuilder> extends ClauseSupport<T> {

  /**
   * JOIN 类型枚举。
   * 
   * @since 2024-12-31
   */
  enum JoinType {
    /** INNER JOIN（内连接） */
    INNER("JOIN"),
    /** LEFT JOIN（左连接） */
    LEFT("LEFT JOIN"),
    /** RIGHT JOIN（右连接） */
    RIGHT("RIGHT JOIN"),
    /** FULL JOIN（全连接） */
    FULL("FULL JOIN"),
    /** CROSS JOIN（交叉连接） */
    CROSS("CROSS JOIN");

    private final String sql;

    JoinType(String sql) {
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
   * 获取内部的 JOIN 持有者。
   * 警告：通常不建议直接操作此对象，除非有特殊需求。
   * 
   * @return 内部 JOIN 持有者
   */
  InnerJoinHolder innerJoinHolder();

  /**
   * INNER JOIN 连接表。
   * <p>
   * 示例：
   * <pre>
   * SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "users")
  *     .columns("users.name", "orders.total")
   *     .innerJoin("orders", "users.id = orders.user_id");
   * </pre>
   * 
   * @param table 表名
   * @param on 连接条件
   * @return 构建器本身
   */
  default T innerJoin(String table, String on) {
    innerJoinHolder().addJoin(JoinType.INNER, table, on);
    return self();
  }

  /**
   * LEFT JOIN 连接表。
   * <p>
   * 示例：
   * <pre>
   * SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "users")
  *     .columns("users.name", "orders.total")
   *     .leftJoin("orders", "users.id = orders.user_id");
   * </pre>
   * 
   * @param table 表名
   * @param on 连接条件
   * @return 构建器本身
   */
  default T leftJoin(String table, String on) {
    innerJoinHolder().addJoin(JoinType.LEFT, table, on);
    return self();
  }

  /**
   * RIGHT JOIN 连接表。
   * <p>
   * 示例：
   * <pre>
   * SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "orders")
  *     .columns("orders.total", "users.name")
   *     .rightJoin("users", "orders.user_id = users.id");
   * </pre>
   * 
   * @param table 表名
   * @param on 连接条件
   * @return 构建器本身
   */
  default T rightJoin(String table, String on) {
    innerJoinHolder().addJoin(JoinType.RIGHT, table, on);
    return self();
  }

  /**
   * FULL JOIN 连接表。
   * <p>
   * 示例：
   * <pre>
   * SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "orders")
  *     .columns("orders.id", "users.name")
   *     .fullJoin("users", "orders.user_id = users.id");
   * </pre>
   * 
   * @param table 表名
   * @param on 连接条件
   * @return 构建器本身
   */
  default T fullJoin(String table, String on) {
    innerJoinHolder().addJoin(JoinType.FULL, table, on);
    return self();
  }

  /**
   * CROSS JOIN 交叉连接表。
   * <p>
   * 示例：
   * <pre>
   * SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "colors")
  *     .columns("colors.name", "sizes.name")
   *     .crossJoin("sizes");
   * </pre>
   * 
   * @param table 表名
   * @return 构建器本身
   */
  default T crossJoin(String table) {
    innerJoinHolder().addJoin(JoinType.CROSS, table, null);
    return self();
  }

  /**
   * JOIN 连接表（默认为 INNER JOIN）。
   * 
   * @param table 表名
   * @param on 连接条件
   * @return 构建器本身
   */
  default T join(String table, String on) {
    return innerJoin(table, on);
  }

  /**
   * INNER JOIN 带别名。
   * 
   * @param table 表名
   * @param alias 表别名
   * @param on 连接条件
   * @return 构建器本身
   */
  default T join(final String table, final String alias, final String on) {
    return innerJoin(table, alias, on);
  }

  /**
   * INNER JOIN 带别名。
   * 
   * @param table 表名
   * @param alias 表别名
   * @param on 连接条件
   * @return 构建器本身
   */
  default T innerJoin(String table, String alias, String on) {
    return innerJoin(table + " AS " + alias, on);
  }

  /**
   * LEFT JOIN 带别名。
   * 
   * @param table 表名
   * @param alias 表别名
   * @param on 连接条件
   * @return 构建器本身
   */
  default T leftJoin(String table, String alias, String on) {
    return leftJoin(table + " AS " + alias, on);
  }

  /**
   * RIGHT JOIN 带别名。
   * 
   * @param table 表名
   * @param alias 表别名
   * @param on 连接条件
   * @return 构建器本身
   */
  default T rightJoin(String table, String alias, String on) {
    return rightJoin(table + " AS " + alias, on);
  }

  /**
   * 条件 JOIN，仅当条件为真时添加。
   * 
   * @param condition 条件
   * @param table 表名
   * @param on 连接条件
   * @return 构建器本身
   */
  default T joinIf(boolean condition, String table, String on) {
    if (condition) {
      join(table, on);
    }
    return self();
  }

  /**
   * 条件 LEFT JOIN，仅当条件为真时添加。
   * 
   * @param condition 条件
   * @param table 表名
   * @param on 连接条件
   * @return 构建器本身
   */
  default T leftJoinIf(boolean condition, String table, String on) {
    if (condition) {
      leftJoin(table, on);
    }
    return self();
  }

  /**
   * INNER JOIN 使用 USING 子句。
   * <p>
   * 当两个表有相同列名时可以使用 USING 简化连接条件。
   * 例如：INNER JOIN orders USING (user_id)
   * 
   * @param table 表名
   * @param columns 共同列名
   * @return 构建器本身
   */
  default T innerJoinUsing(String table, String... columns) {
    return innerJoin(table, "USING (" + String.join(", ", columns) + ")");
  }

  /**
   * LEFT JOIN 使用 USING 子句。
   * 
   * @param table 表名
   * @param columns 共同列名
   * @return 构建器本身
   */
  default T leftJoinUsing(String table, String... columns) {
    return leftJoin(table, "USING (" + String.join(", ", columns) + ")");
  }

  /**
   * RIGHT JOIN 使用 USING 子句。
   * 
   * @param table 表名
   * @param columns 共同列名
   * @return 构建器本身
   */
  default T rightJoinUsing(String table, String... columns) {
    return rightJoin(table, "USING (" + String.join(", ", columns) + ")");
  }

  /**
   * NATURAL JOIN 自然连接。
   * <p>
   * 自动根据相同列名进行连接。
   * 
   * @param table 表名
   * @return 构建器本身
   */
  default T naturalJoin(String table) {
    return innerJoin("NATURAL " + table, "");
  }

  /**
   * NATURAL LEFT JOIN 自然左连接。
   * 
   * @param table 表名
   * @return 构建器本身
   */
  default T naturalLeftJoin(String table) {
    return leftJoin("NATURAL " + table, "");
  }

  /**
   * JOIN 子句内部持有者，用于管理多个 JOIN 子句。
   * <p>
   * 该类用于统一管理所有 JOIN 实体，支持参数化查询。
   * 
   * @author Cody Lu
   * @since 2024-12-31
   */
  public class InnerJoinHolder {
    private final List<JoinEntity> joinEntities = new java.util.ArrayList<>();
    private final List<Object> params = new java.util.ArrayList<>();

    /**
     * 添加一个 JOIN 实体。
     * 
     * @param entity JOIN 实体
     */
    public void add(JoinEntity entity) {
      if (entity != null) {
        joinEntities.add(entity);
      }
    }

    /**
     * 添加 JOIN 子句。
     * 
     * @param type JOIN 类型
     * @param table 表名或表达式
     * @param on 连接条件
     */
    public void addJoin(JoinType type, String table, String on) {
      if (type == null) {
        throw new IllegalArgumentException("Join type cannot be null");
      }
      if (table == null || table.trim().isEmpty()) {
        throw new IllegalArgumentException("Table name cannot be null or empty");
      }
      joinEntities.add(new JoinEntity(type, table, on));
    }

    /**
     * 添加 JOIN 子句（带参数）。
     * 
     * @param type JOIN 类型
     * @param table 表名或表达式
     * @param on 连接条件
     * @param params 参数
     */
    public void addJoin(JoinType type, String table, String on, Object... params) {
      addJoin(type, table, on);
      if (params != null && params.length > 0) {
        for (Object param : params) {
          this.params.add(param);
        }
      }
    }

    /**
     * 添加参数。
     * 
     * @param params 参数
     */
    public void addParams(Object... params) {
      if (params != null && params.length > 0) {
        for (Object param : params) {
          this.params.add(param);
        }
      }
    }

    /**
     * 获取所有 JOIN 实体。
     * 
     * @return JOIN 实体列表
     */
    public List<JoinEntity> getJoinEntities() {
      return joinEntities;
    }

    /**
     * 获取所有参数。
     * 
     * @return 参数列表
     */
    public List<Object> getParams() {
      return params;
    }

    /**
     * 判断是否为空。
     * 
     * @return 如果没有任何 JOIN 实体则返回 true
     */
    public boolean isEmpty() {
      return joinEntities.isEmpty();
    }

    /**
     * 获取 JOIN 实体数量。
     * 
     * @return JOIN 实体数量
     */
    public int size() {
      return joinEntities.size();
    }

    /**
     * 获取参数个数。
     * 
     * @return 参数个数
     */
    public int getParamsCount() {
      return params.size();
    }

    /**
     * 构建 JOIN 子句 SQL 字符串。
     * <p>
     * 格式：
     * <pre>
     * SELECT ... FROM table1
     * JOIN table2 ON table1.id = table2.id
     * LEFT JOIN table3 ON table1.id = table3.id
     * </pre>
     * 
     * @param sql SQL 字符串构建器
     */
    public void appendSql(StringBuilder sql) {
      if (joinEntities.isEmpty()) {
        return;
      }

      for (JoinEntity entity : joinEntities) {
        sql.append(" ")
            .append(entity.getType().getSql())
            .append(" ")
            .append(entity.getTable());

        if (entity.getOnCondition() != null && !entity.getOnCondition().trim().isEmpty()) {
          sql.append(" ON ").append(entity.getOnCondition());
        }
      }
    }

    /**
     * 将所有参数添加到给定的参数列表中。
     * 
     * @param targetParams 目标参数列表
     */
    public void appendParams(List<Object> targetParams) {
      targetParams.addAll(params);
    }

    /**
     * JOIN 实体类，表示一个 JOIN 操作。
     * <p>
     * 包含 JOIN 类型、表名和连接条件。
     * 
     * @author Cody Lu
     * @since 2024-12-31
     */
    class JoinEntity {
      private final JoinType type;
      private final String table;
      private final String onCondition;

      /**
       * 构造 JoinEntity 实例。
       * 
       * @param type JOIN 类型
       * @param table 表名或表达式
       * @param onCondition 连接条件（可为 null，如 CROSS JOIN）
       */
      public JoinEntity(JoinType type, String table, String onCondition) {
        if (type == null) {
          throw new IllegalArgumentException("Join type cannot be null");
        }
        if (table == null || table.trim().isEmpty()) {
          throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        this.type = type;
        this.table = table;
        this.onCondition = onCondition;
      }

      /**
       * 获取 JOIN 类型。
       * 
       * @return JOIN 类型
       */
      public JoinType getType() {
        return type;
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
       * 获取连接条件。
       * 
       * @return 连接条件
       */
      public String getOnCondition() {
        return onCondition;
      }

      @Override
      public String toString() {
        if (onCondition == null || onCondition.trim().isEmpty()) {
          return type.getSql() + " " + table;
        }
        return type.getSql() + " " + table + " ON " + onCondition;
      }
    }
  }
}
