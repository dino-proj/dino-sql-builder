// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * SQL DELETE语句构建器。
 * <p>
 * 用于构建 DELETE SQL 语句的 Builder 类，支持 WHERE 条件等子句。
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public final class DeleteSqlBuilder extends WhereSql<DeleteSqlBuilder> implements SqlBuilderUtils {

  /**
   * 根据表名创建DeleteSqlBuilder实例, 如下写法都是合法的：
   * <p>- <code>"table1"</code>
   * <p>- <code>"table1 as t1"</code>
   *
   * @param table 表名
   * @return DeleteSqlBuilder实例
   */
  public static DeleteSqlBuilder create(final String table) {
    final DeleteSqlBuilder builder = new DeleteSqlBuilder();
    builder.setThat(builder);
    builder.tables.add(table);
    return builder;
  }

  /**
   * 创建 DeleteSqlBuilder 实例并设置表名和别名。
   * <p>生成的 SQL 片段为：table AS alias。
   *
   * @param table 表名
   * @param alias 表别名
   * @return DeleteSqlBuilder 实例
   */
  public static DeleteSqlBuilder create(final String table, final String alias) {
    final DeleteSqlBuilder builder = new DeleteSqlBuilder();
    builder.setThat(builder);
    builder.tables.add(table + " AS " + alias);
    return builder;
  }

  /**
   * 私有构造函数，防止直接实例化。
   */
  private DeleteSqlBuilder() {
    // 私有构造函数，使用静态工厂方法创建实例
  }

  /**
   * 构建最终的 DELETE SQL 语句。
   *
   * @return 构建好的 DELETE SQL 语句字符串
   */
  @Override
  public String getSql() {
    final StringBuilder sql = new StringBuilder(64);
    if (withSql != null) {
      sql.append("WITH ").append(withName).append(" AS (\n").append(withSql.getSql()).append("\n)\n");
    }
    sql.append("DELETE FROM ");
    appendList(sql, tables, " ", ", ");
    appendList(sql, whereColumns, " WHERE ", " ");
    return sql.toString();
  }

  /**
   * 获取所有参数值数组。
   *
   * @return 参数值数组
   */
  @Override
  public Object[] getParams() {
    Stream<Object[]> paramsArr = Stream.of(withSql == null ? EMPTY_PARAMS : withSql.getParams(),
        whereParams.toArray());
    return paramsArr.flatMap(Arrays::stream).toArray();
  }

  /**
   * 返回当前构建的 SQL 字符串。
   *
   * @return 构建好的 SQL 字符串
   */
  @Override
  public String toString() {
    return this.getSql();
  }
}
