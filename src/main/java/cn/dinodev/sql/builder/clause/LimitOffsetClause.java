// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import cn.dinodev.sql.SqlBuilder;

/**
 * LIMIT/OFFSET 子句接口，提供结果集限制和偏移方法。
 * <p>
 * 支持多种 SQL 标准和数据库方言：
 * <ul>
 *   <li>SQL:2008 标准：OFFSET ... ROWS FETCH NEXT ... ROWS ONLY</li>
 *   <li>MySQL/PostgreSQL：LIMIT ... OFFSET ...</li>
 *   <li>基于页码的分页：page, pageSize</li>
 *   <li>条件限制：limitIf, offsetIf</li>
 * </ul>
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-12-01
 */
public interface LimitOffsetClause<T extends SqlBuilder> extends ClauseSupport<T> {

  /**
   * 获取内部的 LIMIT/OFFSET 持有者。
   * 警告：通常不建议直接操作此对象，除非有特殊需求。
   * 
   * @return 内部 LIMIT/OFFSET 持有者
   */
  InnerLimitOffsetHolder innerLimitOffsetHolder();

  /**
   * 设置 LIMIT 限制返回的记录数。
   * <p>
   * 示例：
   * <pre>
   * builder.limit(10);
   * // MySQL/PostgreSQL: SELECT ... LIMIT 10
   * </pre>
   * 
   * @param limit 限制数量，必须 > 0
   * @return 构建器本身
   */
  default T limit(int limit) {
    innerLimitOffsetHolder().setLimit(limit);
    return self();
  }

  /**
   * 设置 OFFSET 偏移量。
   * <p>
   * 示例：
   * <pre>
   * builder.limit(10).offset(20);
   * // MySQL/PostgreSQL: SELECT ... LIMIT 10 OFFSET 20
   * </pre>
   * 
   * @param offset 偏移量，必须 >= 0
   * @return 构建器本身
   */
  default T offset(long offset) {
    innerLimitOffsetHolder().setOffset(offset);
    return self();
  }

  /**
   * 设置 LIMIT 和 OFFSET（用于分页）。
   * <p>
   * 便捷方法，等效于先调用 limit(limit) 再调用 offset(offset)。
   * <p>
   * 示例：
   * <pre>
   * builder.limitOffset(10, 20);
   * // MySQL/PostgreSQL: SELECT ... LIMIT 10 OFFSET 20
   * </pre>
   * 
   * @param limit 限制数量
   * @param offset 偏移量
   * @return 构建器本身
   */
  default T limitOffset(int limit, long offset) {
    limit(limit);
    offset(offset);
    return self();
  }

  /**
   * 分页查询（基于页码和每页数量）。
   * <p>
   * 自动计算偏移量：offset = (page - 1) * pageSize
   * <p>
   * 示例：
   * <pre>
   * // 获取第 3 页，每页 10 条
   * builder.limitPage(3, 10);
   * // 等效于: LIMIT 10 OFFSET 20
   * </pre>
   * 
   * @param page 页码（从 1 开始，小于 1 时自动设为 1）
   * @param pageSize 每页数量
   * @return 构建器本身
   */
  default T limitPage(int page, int pageSize) {
    if (page < 1) {
      page = 1;
    }
    long offset = (long) (page - 1) * pageSize;
    return limitOffset(pageSize, offset);
  }

  /**
   * 条件限制，仅当条件为真时添加 LIMIT。
   * <p>
   * 示例：
   * <pre>
   * boolean needLimit = config.isLimited();
   * builder.limitIf(needLimit, 100);
   * </pre>
   * 
   * @param condition 条件
   * @param limit 限制数量
   * @return 构建器本身
   */
  default T limitIf(boolean condition, int limit) {
    if (condition) {
      limit(limit);
    }
    return self();
  }

  /**
   * 条件偏移，仅当条件为真时添加 OFFSET。
   * <p>
   * 示例：
   * <pre>
   * boolean hasOffset = page > 1;
   * builder.limit(10).offsetIf(hasOffset, 20);
   * </pre>
   * 
   * @param condition 条件
   * @param offset 偏移量
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T offsetIf(boolean condition, long offset) {
    if (condition) {
      offset(offset);
    }
    return self();
  }

  /**
   * 条件分页，仅当条件为真时添加分页限制。
   * <p>
   * 示例：
   * <pre>
   * boolean enablePaging = config.isPagingEnabled();
   * builder.limitPageIf(enablePaging, 2, 10);
   * </pre>
   * 
   * @param condition 条件
   * @param page 页码（从 1 开始）
   * @param pageSize 每页数量
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T limitPageIf(boolean condition, int page, int pageSize) {
    if (condition) {
      limitPage(page, pageSize);
    }
    return self();
  }

  /**
   * 仅获取第一条记录。
   * <p>
   * 等效于 limit(1)。
   * <p>
   * 示例：
   * <pre>
   * builder.limitFirst();
   * // 结果: SELECT ... LIMIT 1
   * </pre>
   * 
   * @return 构建器本身
   */
  default T limitFirst() {
    return limit(1);
  }

  /**
   * 获取前 N 条记录。
   * <p>
   * 等效于 limit(n)，语义更明确。
   * <p>
   * 示例：
   * <pre>
   * builder.limitTop(5);
   * // 结果: SELECT ... LIMIT 5
   * </pre>
   * 
   * @param n 记录数量
   * @return 构建器本身
   */
  default T limitTop(int n) {
    return limit(n);
  }

  /**
   * 跳过前 N 条记录。
   * <p>
   * 等效于 offset(n)，语义更明确。
   * <p>
   * 示例：
   * <pre>
   * builder.limit(10).skip(20);
   * // 结果: SELECT ... LIMIT 10 OFFSET 20
   * </pre>
   * 
   * @param n 跳过的记录数
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T skip(long n) {
    return offset(n);
  }

  /**
   * 条件跳过记录。
   * <p>
   * 示例：
   * <pre>
   * boolean hasMore = page > 1;
   * builder.limit(10).skipIf(hasMore, 20);
   * </pre>
   * 
   * @param condition 条件
   * @param n 跳过的记录数
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T skipIf(boolean condition, long n) {
    if (condition) {
      offset(n);
    }
    return self();
  }

  /**
   * 使用页码和每页大小进行分页（从 0 开始的页码）。
   * <p>
   * 与 limitPage 的区别：page 从 0 开始计数。
   * <p>
   * 示例：
   * <pre>
   * // 获取第 3 页（从 0 开始），每页 10 条
   * builder.limitPageZeroBased(2, 10);
   * // 等效于: LIMIT 10 OFFSET 20
   * </pre>
   * 
   * @param page 页码（从 0 开始，小于 0 时自动设为 0）
   * @param pageSize 每页数量
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T limitPageZeroBased(int page, int pageSize) {
    if (page < 0) {
      page = 0;
    }
    long offset = (long) page * pageSize;
    return limitOffset(pageSize, offset);
  }

  /**
   * 条件获取第一条记录。
   * <p>
   * 示例：
   * <pre>
   * boolean findOne = searchMode == SearchMode.SINGLE;
   * builder.limitFirstIf(findOne);
   * </pre>
   * 
   * @param condition 条件
   * @return 构建器本身
   * @since 2024-12-31
   */
  default T limitFirstIf(boolean condition) {
    return limitIf(condition, 1);
  }

  /**
   * LIMIT/OFFSET 内部持有者，用于管理分页限制和偏移量。
   * <p>
   * 该类封装了 LIMIT 和 OFFSET 的状态管理，提供了统一的 SQL 生成接口。
   * 
   * @author Cody Lu
   * @since 2024-12-31
   */
  class InnerLimitOffsetHolder {
    private int limitValue;
    private long offsetValue;

    /**
     * 设置 LIMIT 值。
     * 
     * @param limit 限制数量（必须 > 0）
     * @throws IllegalArgumentException 如果 limit <= 0
     */
    public void setLimit(int limit) {
      if (limit <= 0) {
        throw new IllegalArgumentException("limit must be positive, got: " + limit);
      }
      this.limitValue = limit;
    }

    /**
     * 设置 OFFSET 值。
     * 
     * @param offset 偏移量（必须 >= 0）
     * @throws IllegalArgumentException 如果 offset < 0
     */
    public void setOffset(long offset) {
      if (offset < 0) {
        throw new IllegalArgumentException("offset cannot be negative, got: " + offset);
      }
      this.offsetValue = offset;
    }

    /**
     * 获取 LIMIT 值。
     * 
     * @return 限制数量
     */
    public int getLimit() {
      return limitValue;
    }

    /**
     * 获取 OFFSET 值。
     * 
     * @return 偏移量
     */
    public long getOffset() {
      return offsetValue;
    }

    /**
     * 判断是否设置了 LIMIT。
     * 
     * @return 如果设置了 LIMIT 则返回 true
     */
    public boolean hasLimit() {
      return limitValue > 0;
    }

    /**
     * 判断是否设置了 OFFSET。
     * 
     * @return 如果设置了 OFFSET 则返回 true
     */
    public boolean hasOffset() {
      return offsetValue > 0;
    }

    /**
     * 构建 LIMIT/OFFSET 子句 SQL 字符串。
     * <p>
     * 根据数据库方言生成对应的 LIMIT/OFFSET 语法。
     * 
     * @param sql SQL 字符串构建器
     * @param dialect 数据库方言
     */
    public void appendSql(StringBuilder sql, cn.dinodev.sql.dialect.Dialect dialect) {
      if (hasLimit()) {
        sql.append(' ').append(dialect.limitOffset(limitValue, offsetValue));
      }
    }

    /**
     * 重置 LIMIT/OFFSET 状态。
     */
    public void reset() {
      this.limitValue = 0;
      this.offsetValue = 0;
    }
  }
}
