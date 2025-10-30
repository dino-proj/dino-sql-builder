// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.dinodev.sql.SqlBuilder;

/**
 * SQL INSERT语句构建器。
 * <p>
 * 用于构建 INSERT SQL 语句的 Builder 类，支持指定列和值的插入操作。
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public class InsertSqlBuilder implements SqlBuilder, SqlBuilderUtils {

  private final String table;

  /**
   * 需要插入的列名集合。
   */
  protected List<String> setColumns = new ArrayList<>();

  /**
   * 每列对应的值表达式集合。
   */
  protected List<String> valueExps = new ArrayList<>();

  /**
   * 插入语句的参数值集合。
   */
  protected List<Object> setParams = new ArrayList<>();

  /**
   * 构造函数，创建指定表的 INSERT 语句构建器。
   *
   * @param table 表名
   */
  public InsertSqlBuilder(String table) {
    this.table = table;
  }

  /**
   * 设置列值，使用 ? 占位符。
   *
   * @param col 列名
   * @param value 值
   * @return 当前构建器实例
   */
  public InsertSqlBuilder set(String col, Object value) {
    return this.set(col, "?", value);
  }

  /**
   * 设置列值，使用自定义表达式。
   *
   * @param col 列名
   * @param valueExpr 值表达式
   * @param value 参数值
   * @return 当前构建器实例
   */
  public InsertSqlBuilder set(String col, String valueExpr, Object value) {
    this.setColumns.add(col);
    this.valueExps.add(valueExpr);
    this.setParams.add(value);
    return this;
  }

  /**
   * 根据条件设置列值，使用 ? 占位符。
   *
   * @param cnd 条件，为 true 时才设置
   * @param col 列名
   * @param value 值
   * @return 当前构建器实例
   */
  public InsertSqlBuilder setIf(final boolean cnd, String col, Object value) {
    return this.setIf(cnd, col, "?", value);
  }

  /**
   * 根据条件设置列值，使用自定义表达式。
   *
   * @param cnd 条件，为 true 时才设置
   * @param col 列名
   * @param valueExpr 值表达式
   * @param value 参数值
   * @return 当前构建器实例
   */
  public InsertSqlBuilder setIf(final boolean cnd, String col, String valueExpr, Object value) {
    if (cnd) {
      return set(col, valueExpr, value);
    }
    return this;
  }

  /**
   * 当值不为 null 时设置列值。
   *
   * @param col 列名
   * @param value 值
   * @return 当前构建器实例
   */
  public InsertSqlBuilder setIfNotNull(String col, Object value) {
    return setIf(!Objects.isNull(value), col, value);
  }

  /**
   * 设置列值但不添加参数（用于函数调用等）。
   *
   * @param col 列名
   * @param valueExpr 值表达式
   * @return 当前构建器实例
   */
  public InsertSqlBuilder withoutParam(String col, String valueExpr) {
    this.setColumns.add(col);
    this.valueExps.add(valueExpr);
    return this;
  }

  /**
   * 根据条件设置列值但不添加参数。
   *
   * @param cnd 条件，为 true 时才设置
   * @param col 列名
   * @param expr 值表达式
   * @return 当前构建器实例
   */
  public InsertSqlBuilder withoutParamIf(final boolean cnd, String col, String expr) {
    if (cnd) {
      return withoutParam(col, expr);
    }
    return this;
  }

  /**
   * 设置 JSON 类型列值。
   *
   * @param col 列名
   * @param value JSON值
   * @return 当前构建器实例
   */
  public InsertSqlBuilder setJson(String col, Object value) {
    return this.set(col, "?::json", value);
  }

  /**
   * 根据条件设置 JSON 类型列值。
   *
   * @param cnd 条件，为 true 时才设置
   * @param col 列名
   * @param value JSON值
   * @return 当前构建器实例
   */
  public InsertSqlBuilder setJsonIf(final boolean cnd, String col, Object value) {
    if (cnd) {
      return set(col, "?::json", value);
    }
    return this;
  }

  /**
   * 当值不为 null 时设置 JSON 类型列值。
   *
   * @param col 列名
   * @param value JSON值
   * @return 当前构建器实例
   */
  public InsertSqlBuilder setJsonIfNotNull(String col, Object value) {
    return setJsonIf(!Objects.isNull(value), col, value);
  }

  /**
   * 设置 JSONB 类型列值。
   *
   * @param col 列名
   * @param value JSONB值
   * @return 当前构建器实例
   */
  public InsertSqlBuilder setJsonb(String col, Object value) {
    return this.set(col, "?::jsonb", value);
  }

  /**
   * 根据条件设置 JSONB 类型列值。
   *
   * @param cnd 条件，为 true 时才设置
   * @param col 列名
   * @param value JSONB值
   * @return 当前构建器实例
   */
  public InsertSqlBuilder setJsonbIf(final boolean cnd, String col, Object value) {
    if (cnd) {
      return set(col, "?::jsonb", value);
    }
    return this;
  }

  /**
   * 当值不为 null 时设置 JSONB 类型列值。
   *
   * @param col 列名
   * @param value JSONB值
   * @return 当前构建器实例
   */
  public InsertSqlBuilder setJsonbIfNotNull(String col, Object value) {
    return setJsonIf(!Objects.isNull(value), col, value);
  }

  /**
   * 构建最终的 INSERT SQL 语句。
   *
   * @return 构建好的 INSERT SQL 语句字符串
   */
  @Override
  public String getSql() {
    final StringBuilder sql = new StringBuilder(64);
    sql.append("INSERT INTO ").append(table);
    appendList(sql, setColumns, " (", ", ", ") ");
    appendList(sql, valueExps, " VALUES(", ", ", ") ");
    return sql.toString();
  }

  /**
   * 获取所有参数值数组。
   *
   * @return 参数值数组
   */
  @Override
  public Object[] getParams() {
    return setParams.toArray(new Object[0]);
  }

}
