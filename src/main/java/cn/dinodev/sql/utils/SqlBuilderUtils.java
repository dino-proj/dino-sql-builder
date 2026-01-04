// Copyright 2025 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.utils;

import java.util.List;

/**
 * SQL 构建工具接口。
 * <p>
 * 提供 SQL 片段拼接等通用工具方法。
 * 
 * @author Cody Lu
 * @since 2025-10-07
 */
public final class SqlBuilderUtils {

  /**
   * 拼接列表为 SQL 片段。
   *
   * @param sql  用于追加字符串的 StringBuilder
   * @param list 需要拼接的对象列表
   * @param start 列表前缀字符串
   * @param sep 列表项分隔符
   * @param end 列表后缀字符串
   * @return 拼接后的 StringBuilder
   */
  public static StringBuilder appendList(final StringBuilder sql, final List<?> list, final String start,
      final String sep,
      final String end) {
    var first = true;

    for (final Object s : list) {
      if (first) {
        sql.append(start);
      } else {
        sql.append(sep);
      }
      sql.append(s);
      first = false;
    }
    if (end != null && !list.isEmpty()) {
      sql.append(end);
    }
    return sql;
  }

  /**
   * 拼接列表为 SQL 片段（无后缀）。
   *
   * @param sql 用于追加字符串的 StringBuilder
   * @param list 需要拼接的对象列表
   * @param start 列表前缀字符串
   * @param sep 列表项分隔符
   * @return 拼接后的 StringBuilder
   */
  public static StringBuilder appendList(final StringBuilder sql, final List<?> list, final String start,
      final String sep) {
    return appendList(sql, list, start, sep, null);
  }

  /**
   * 生成有 n 个参数的表达式，主要用于 IN/NOT IN ARRAY函数语句。
   * 示例：
   * makeNParamExpr("IN (", 3, ")") 返回 "IN (?, ?, ?)"
   * makeNParamExpr("ARRAY[", 2, "]") 返回 "ARRAY[?, ?]"
   * @param preFix 前缀字符串，如列名和操作符
   * @param nCount 参数个数
   * @param postFix 后缀字符串
   * @return 构造好的 SQL 片段
   */
  public static String makeNParamExpr(final String preFix, final int nCount, final String postFix) {
    final StringBuilder expr = new StringBuilder();
    int idx = 0;
    expr.append(preFix);

    while (idx < nCount) {
      if (idx != 0) {
        expr.append(", ");
      }
      expr.append('?');
      idx++;
    }
    expr.append(postFix);

    return expr.toString();
  }
}
