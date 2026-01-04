// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.wheres;

import cn.dinodev.sql.Logic;
import cn.dinodev.sql.Oper;
import cn.dinodev.sql.SqlBuilder;
import cn.dinodev.sql.utils.StringUtils;

/**
 * 模糊匹配 WHERE 子句接口，提供 LIKE、NOT LIKE、前缀、后缀匹配等操作。
 * <p>
 * 支持链式调用，提供条件判断和 AND/OR 逻辑连接。
 * <p>
 * 示例：
 * <pre>{@code
 * builder.like("name", "John")           // name LIKE '%John%'
 *        .startWith("email", "admin")    // email LIKE 'admin%'
 *        .endWith("phone", "8888")       // phone LIKE '%8888'
 *        .notLike("status", "deleted")   // status NOT LIKE '%deleted%'
 *        .likePattern("code", "A_B%")    // code LIKE 'A_B%'
 * }</pre>
 * <p>
 * 注意：当前实现不会自动转义 LIKE 模式中的特殊字符（% 和 _），
 * 如需精确匹配包含这些字符的值，请在调用前手动转义。
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-11-23
 */
public interface LikeWhereClause<T extends SqlBuilder> extends WhereClauseSupport<T> {

  /**
   * 添加 LIKE 条件（%value%），AND 连接。
   * <p>
   * 空值处理：如果 value 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T like(final String column, final String value) {
    return like(column, value, Logic.AND);
  }

  /**
   * 添加 LIKE 条件（%value%），支持自定义逻辑符。
   * <p>
   * 空值处理：如果 value 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T like(final String column, final String value, final Logic logic) {
    if (StringUtils.isBlank(value)) {
      appendOrTrue(logic);
      return self();
    }
    appendWhere(logic, column, Oper.LIKE, "%" + value + "%");
    return self();
  }

  /**
   * 根据条件决定是否添加 LIKE 表达式（%value%）。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T likeIf(final boolean cnd, final String column, final String value) {
    if (cnd) {
      like(column, value);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加 LIKE 表达式（%value%）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T likeIfNotNull(final String column, final String value) {
    if (value != null) {
      like(column, value);
    }
    return self();
  }

  /**
   * 值不为空字符串时才添加 LIKE 表达式（%value%）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T likeIfNotBlank(final String column, final String value) {
    if (StringUtils.isNotBlank(value)) {
      like(column, value);
    }
    return self();
  }

  /**
   * 添加 NOT LIKE 条件（%value%），AND 连接。
   * <p>
   * 空值处理：如果 value 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T notLike(final String column, final String value) {
    return notLike(column, value, Logic.AND);
  }

  /**
   * 添加 NOT LIKE 条件（%value%），支持自定义逻辑符。
   * <p>
   * 空值处理：如果 value 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T notLike(final String column, final String value, final Logic logic) {
    if (StringUtils.isBlank(value)) {
      appendOrTrue(logic);
      return self();
    }
    appendWhere(logic, column, Oper.NOT_LIKE, "%" + value + "%");
    return self();
  }

  /**
   * 根据条件决定是否添加 NOT LIKE 表达式（%value%）。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T notLikeIf(final boolean cnd, final String column, final String value) {
    if (cnd) {
      notLike(column, value);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加 NOT LIKE 表达式（%value%）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T notLikeIfNotNull(final String column, final String value) {
    if (value != null) {
      notLike(column, value);
    }
    return self();
  }

  /**
   * 值不为空字符串时才添加 NOT LIKE 表达式（%value%）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T notLikeIfNotBlank(final String column, final String value) {
    if (StringUtils.isNotBlank(value)) {
      notLike(column, value);
    }
    return self();
  }

  /**
   * 添加前缀匹配 LIKE 条件（value%），AND 连接。
   * <p>
   * 空值处理：如果 value 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T startWith(final String column, final String value) {
    return startWith(column, value, Logic.AND);
  }

  /**
   * 添加前缀匹配 LIKE 条件（value%），支持自定义逻辑符。
   * <p>
   * 空值处理：如果 value 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T startWith(final String column, final String value, final Logic logic) {
    if (StringUtils.isBlank(value)) {
      appendOrTrue(logic);
      return self();
    }
    appendWhere(logic, column, Oper.LIKE, value + "%");
    return self();
  }

  /**
   * 根据条件决定是否添加前缀匹配表达式（value%）。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T startWithIf(final boolean cnd, final String column, final String value) {
    if (cnd) {
      startWith(column, value);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加前缀匹配表达式（value%）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T startWithIfNotNull(final String column, final String value) {
    if (value != null) {
      startWith(column, value);
    }
    return self();
  }

  /**
   * 值不为空字符串时才添加前缀匹配表达式（value%）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T startWithIfNotBlank(final String column, final String value) {
    if (StringUtils.isNotBlank(value)) {
      startWith(column, value);
    }
    return self();
  }

  /**
   * 添加前缀不匹配 NOT LIKE 条件（value%），AND 连接。
   * <p>
   * 空值处理：如果 value 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T notStartWith(final String column, final String value) {
    return notStartWith(column, value, Logic.AND);
  }

  /**
   * 添加前缀不匹配 NOT LIKE 条件（value%），支持自定义逻辑符。
   * <p>
   * 空值处理：如果 value 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T notStartWith(final String column, final String value, final Logic logic) {
    if (StringUtils.isBlank(value)) {
      appendOrTrue(logic);
      return self();
    }
    appendWhere(logic, column, Oper.NOT_LIKE, value + "%");
    return self();
  }

  /**
   * 根据条件决定是否添加前缀不匹配表达式（value%）。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T notStartWithIf(final boolean cnd, final String column, final String value) {
    if (cnd) {
      notStartWith(column, value);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加前缀不匹配表达式（value%）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T notStartWithIfNotNull(final String column, final String value) {
    if (value != null) {
      notStartWith(column, value);
    }
    return self();
  }

  /**
   * 值不为空字符串时才添加前缀不匹配表达式（value%）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T notStartWithIfNotBlank(final String column, final String value) {
    if (StringUtils.isNotBlank(value)) {
      notStartWith(column, value);
    }
    return self();
  }

  /**
   * 添加后缀匹配 LIKE 条件（%value），AND 连接。
   * <p>
   * 空值处理：如果 value 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T endWith(final String column, final String value) {
    return endWith(column, value, Logic.AND);
  }

  /**
   * 添加后缀匹配 LIKE 条件（%value），支持自定义逻辑符。
   * <p>
   * 空值处理：如果 value 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T endWith(final String column, final String value, final Logic logic) {
    if (StringUtils.isBlank(value)) {
      appendOrTrue(logic);
      return self();
    }
    appendWhere(logic, column, Oper.LIKE, "%" + value);
    return self();
  }

  /**
   * 根据条件决定是否添加后缀匹配表达式（%value）。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T endWithIf(final boolean cnd, final String column, final String value) {
    if (cnd) {
      endWith(column, value);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加后缀匹配表达式（%value）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T endWithIfNotNull(final String column, final String value) {
    if (value != null) {
      endWith(column, value);
    }
    return self();
  }

  /**
   * 值不为空字符串时才添加后缀匹配表达式（%value）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T endWithIfNotBlank(final String column, final String value) {
    if (StringUtils.isNotBlank(value)) {
      endWith(column, value);
    }
    return self();
  }

  /**
   * 添加后缀不匹配 NOT LIKE 条件（%value），AND 连接。
   * <p>
   * 空值处理：如果 value 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T notEndWith(final String column, final String value) {
    return notEndWith(column, value, Logic.AND);
  }

  /**
   * 添加后缀不匹配 NOT LIKE 条件（%value），支持自定义逻辑符。
   * <p>
   * 空值处理：如果 value 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T notEndWith(final String column, final String value, final Logic logic) {
    if (StringUtils.isBlank(value)) {
      appendOrTrue(logic);
      return self();
    }
    appendWhere(logic, column, Oper.NOT_LIKE, "%" + value);
    return self();
  }

  /**
   * 根据条件决定是否添加后缀不匹配表达式（%value）。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T notEndWithIf(final boolean cnd, final String column, final String value) {
    if (cnd) {
      notEndWith(column, value);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加后缀不匹配表达式（%value）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T notEndWithIfNotNull(final String column, final String value) {
    if (value != null) {
      notEndWith(column, value);
    }
    return self();
  }

  /**
   * 值不为空字符串时才添加后缀不匹配表达式（%value）。
   * 
   * @param column 列名
   * @param value 匹配值
   * @return 构建器本身
   */
  default T notEndWithIfNotBlank(final String column, final String value) {
    if (StringUtils.isNotBlank(value)) {
      notEndWith(column, value);
    }
    return self();
  }

  /**
   * 添加自定义 LIKE 模式条件，AND 连接。
   * <p>
   * 该方法允许完全自定义 LIKE 模式，包括通配符的位置。
   * 使用 % 表示任意字符序列，使用 _ 表示单个字符。
   * <p>
   * 示例：
   * <pre>{@code
   * builder.likePattern("code", "A_B%")  // code LIKE 'A_B%'
   * builder.likePattern("name", "%John%Doe%")  // name LIKE '%John%Doe%'
   * }</pre>
   * <p>
   * 空值处理：如果 pattern 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param pattern LIKE 模式（可包含 % 和 _ 通配符）
   * @return 构建器本身
   */
  default T likePattern(final String column, final String pattern) {
    return likePattern(column, pattern, Logic.AND);
  }

  /**
   * 添加自定义 LIKE 模式条件，支持自定义逻辑符。
   * <p>
   * 该方法允许完全自定义 LIKE 模式，包括通配符的位置。
   * 使用 % 表示任意字符序列，使用 _ 表示单个字符。
   * <p>
   * 空值处理：如果 pattern 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param pattern LIKE 模式（可包含 % 和 _ 通配符）
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T likePattern(final String column, final String pattern, final Logic logic) {
    if (StringUtils.isBlank(pattern)) {
      appendOrTrue(logic);
      return self();
    }
    appendWhere(logic, column, Oper.LIKE, pattern);
    return self();
  }

  /**
   * 根据条件决定是否添加自定义 LIKE 模式表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param pattern LIKE 模式
   * @return 构建器本身
   */
  default T likePatternIf(final boolean cnd, final String column, final String pattern) {
    if (cnd) {
      likePattern(column, pattern);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加自定义 LIKE 模式表达式。
   * 
   * @param column 列名
   * @param pattern LIKE 模式
   * @return 构建器本身
   */
  default T likePatternIfNotNull(final String column, final String pattern) {
    if (pattern != null) {
      likePattern(column, pattern);
    }
    return self();
  }

  /**
   * 值不为空字符串时才添加自定义 LIKE 模式表达式。
   * 
   * @param column 列名
   * @param pattern LIKE 模式
   * @return 构建器本身
   */
  default T likePatternIfNotBlank(final String column, final String pattern) {
    if (StringUtils.isNotBlank(pattern)) {
      likePattern(column, pattern);
    }
    return self();
  }

  /**
   * 添加自定义 NOT LIKE 模式条件，AND 连接。
   * <p>
   * 该方法允许完全自定义 NOT LIKE 模式，包括通配符的位置。
   * <p>
   * 空值处理：如果 pattern 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param pattern LIKE 模式（可包含 % 和 _ 通配符）
   * @return 构建器本身
   */
  default T notLikePattern(final String column, final String pattern) {
    return notLikePattern(column, pattern, Logic.AND);
  }

  /**
   * 添加自定义 NOT LIKE 模式条件，支持自定义逻辑符。
   * <p>
   * 该方法允许完全自定义 NOT LIKE 模式，包括通配符的位置。
   * <p>
   * 空值处理：如果 pattern 为 null 或空字符串，将添加 OR 1=1 条件（始终为真）。
   * 
   * @param column 列名
   * @param pattern LIKE 模式（可包含 % 和 _ 通配符）
   * @param logic 逻辑运算符（AND/OR）
   * @return 构建器本身
   */
  default T notLikePattern(final String column, final String pattern, final Logic logic) {
    if (StringUtils.isBlank(pattern)) {
      appendOrTrue(logic);
      return self();
    }
    appendWhere(logic, column, Oper.NOT_LIKE, pattern);
    return self();
  }

  /**
   * 根据条件决定是否添加自定义 NOT LIKE 模式表达式。
   * 
   * @param cnd 条件（true 时添加表达式）
   * @param column 列名
   * @param pattern LIKE 模式
   * @return 构建器本身
   */
  default T notLikePatternIf(final boolean cnd, final String column, final String pattern) {
    if (cnd) {
      notLikePattern(column, pattern);
    }
    return self();
  }

  /**
   * 值不为 null 时才添加自定义 NOT LIKE 模式表达式。
   * 
   * @param column 列名
   * @param pattern LIKE 模式
   * @return 构建器本身
   */
  default T notLikePatternIfNotNull(final String column, final String pattern) {
    if (pattern != null) {
      notLikePattern(column, pattern);
    }
    return self();
  }

  /**
   * 值不为空字符串时才添加自定义 NOT LIKE 模式表达式。
   * 
   * @param column 列名
   * @param pattern LIKE 模式
   * @return 构建器本身
   */
  default T notLikePatternIfNotBlank(final String column, final String pattern) {
    if (StringUtils.isNotBlank(pattern)) {
      notLikePattern(column, pattern);
    }
    return self();
  }
}
