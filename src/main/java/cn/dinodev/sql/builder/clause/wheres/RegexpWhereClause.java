// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.wheres;

import cn.dinodev.sql.Logic;
import cn.dinodev.sql.SqlBuilder;
import cn.dinodev.sql.utils.StringUtils;

/**
 * 正则表达式 WHERE 子句接口，提供 REGEXP、NOT REGEXP 等正则匹配操作。
 * <p>
 * 支持链式调用，提供条件判断和 AND/OR 逻辑连接。
 * <p>
 * 不同数据库的正则表达式语法会通过 Dialect 自动适配：
 * <ul>
 *   <li>MySQL/MariaDB: 使用 REGEXP 和 NOT REGEXP</li>
 *   <li>PostgreSQL: 使用 ~ 和 !~ （区分大小写）</li>
 * </ul>
 * <p>
 * 示例：
 * <pre>{@code
 * // MySQL: WHERE email REGEXP '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
 * // PostgreSQL: WHERE email ~ '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
 * builder.regexp("email", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
 *        .regexp("phone", "^\\d{11}$")             // 手机号 11 位数字
 *        .notRegexp("code", "^[0-9]+$")           // 排除纯数字编码
 *        .regexpIf(searchName, "name", "^" + searchName)  // 条件匹配
 * }</pre>
 * <p>
 * 注意事项：
 * <ul>
 *   <li>正则表达式语法遵循数据库特定的实现（MySQL 使用 POSIX，PostgreSQL 使用 POSIX ERE）</li>
 *   <li>特殊字符（如 ., +, *, ?, [, ], ^, $, (, ), {, }, |, \）需要根据数据库语法转义</li>
 *   <li>PostgreSQL 的 ~ 操作符区分大小写，如需不区分大小写请使用 ~*</li>
 * </ul>
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-11-29
 */
public interface RegexpWhereClause<T extends SqlBuilder> extends WhereClauseSupport<T> {

  /**
   * 添加正则表达式匹配条件，AND 连接。
   * <p>
   * 空值处理：如果 pattern 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * <p>
   * 示例：
   * <pre>{@code
   * builder.regexp("email", "^[a-zA-Z0-9._%+-]+@gmail\\.com$")  // Gmail 邮箱
   * }</pre>
   * 
   * @param column 列名
   * @param pattern 正则表达式模式
   * @return 构建器本身
   */
  default T regexp(final String column, final String pattern) {
    return regexp(column, pattern, Logic.AND);
  }

  /**
   * 添加正则表达式匹配条件，支持自定义逻辑符。
   * <p>
   * 空值处理：如果 pattern 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param pattern 正则表达式模式
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T regexp(final String column, final String pattern, final Logic logic) {
    if (StringUtils.isBlank(pattern)) {
      appendOrTrue(logic);
      return self();
    }
    appendWhere(logic, dialect().makeRegexpExpr(column));
    innerWhereHolder().addWhereParam(pattern);
    return self();
  }

  /**
   * 根据条件决定是否添加正则表达式匹配。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param pattern 正则表达式模式
   * @return 构建器本身
   */
  default T regexpIf(final boolean cnd, final String column, final String pattern) {
    if (cnd) {
      regexp(column, pattern);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加正则表达式匹配。
   * 
   * @param column 列名
   * @param pattern 正则表达式模式
   * @return 构建器本身
   */
  default T regexpIfNotNull(final String column, final String pattern) {
    if (pattern != null) {
      regexp(column, pattern);
    }
    return self();
  }

  /**
   * 值不为空字符串时才添加正则表达式匹配。
   * 
   * @param column 列名
   * @param pattern 正则表达式模式
   * @return 构建器本身
   */
  default T regexpIfNotBlank(final String column, final String pattern) {
    if (StringUtils.isNotBlank(pattern)) {
      regexp(column, pattern);
    }
    return self();
  }

  /**
   * 添加正则表达式不匹配条件，AND 连接。
   * <p>
   * 空值处理：如果 pattern 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * <p>
   * 示例：
   * <pre>{@code
   * builder.notRegexp("username", "^admin")  // 排除 admin 开头的用户名
   * }</pre>
   * 
   * @param column 列名
   * @param pattern 正则表达式模式
   * @return 构建器本身
   */
  default T notRegexp(final String column, final String pattern) {
    return notRegexp(column, pattern, Logic.AND);
  }

  /**
   * 添加正则表达式不匹配条件，支持自定义逻辑符。
   * <p>
   * 空值处理：如果 pattern 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param pattern 正则表达式模式
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T notRegexp(final String column, final String pattern, final Logic logic) {
    if (StringUtils.isBlank(pattern)) {
      appendOrTrue(logic);
      return self();
    }
    appendWhere(logic, dialect().makeNotRegexpExpr(column));
    innerWhereHolder().addWhereParam(pattern);
    return self();
  }

  /**
   * 根据条件决定是否添加正则表达式不匹配。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param pattern 正则表达式模式
   * @return 构建器本身
   */
  default T notRegexpIf(final boolean cnd, final String column, final String pattern) {
    if (cnd) {
      notRegexp(column, pattern);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加正则表达式不匹配。
   * 
   * @param column 列名
   * @param pattern 正则表达式模式
   * @return 构建器本身
   */
  default T notRegexpIfNotNull(final String column, final String pattern) {
    if (pattern != null) {
      notRegexp(column, pattern);
    }
    return self();
  }

  /**
   * 值不为空字符串时才添加正则表达式不匹配。
   * 
   * @param column 列名
   * @param pattern 正则表达式模式
   * @return 构建器本身
   */
  default T notRegexpIfNotBlank(final String column, final String pattern) {
    if (StringUtils.isNotBlank(pattern)) {
      notRegexp(column, pattern);
    }
    return self();
  }
}
