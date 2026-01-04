// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import cn.dinodev.sql.SqlBuilder;
import cn.dinodev.sql.builder.clause.wheres.ComparisonWhereClause;
import cn.dinodev.sql.builder.clause.wheres.ConditionalWhereClause;
import cn.dinodev.sql.builder.clause.wheres.LikeWhereClause;
import cn.dinodev.sql.builder.clause.wheres.MultiColumnWhereClause;
import cn.dinodev.sql.builder.clause.wheres.NullCheckWhereClause;
import cn.dinodev.sql.builder.clause.wheres.RangeWhereClause;
import cn.dinodev.sql.builder.clause.wheres.RegexpWhereClause;
import cn.dinodev.sql.builder.clause.wheres.SubQueryWhereClause;

/**
 * WHERE 子句组合接口，聚合所有 WHERE 相关的查询条件方法。
 * <p>
 * 该接口是一个门面（Facade）接口，通过继承多个功能特定的子接口，
 * 为 SQL 构建器提供完整的 WHERE 子句构建能力。
 * <p>
 * 包含的功能模块：
 * <ul>
 *   <li>{@link ComparisonWhereClause} - 比较操作（=, !=, >, <, >=, <=）</li>
 *   <li>{@link ConditionalWhereClause} - 条件控制（If、IfNotNull）</li>
 *   <li>{@link LikeWhereClause} - 模糊匹配（LIKE、NOT LIKE、前缀、后缀）</li>
 *   <li>{@link NullCheckWhereClause} - 空值检查（IS NULL、IS NOT NULL）</li>
 *   <li>{@link RangeWhereClause} - 范围查询（IN、NOT IN、BETWEEN）</li>
 *   <li>{@link MultiColumnWhereClause} - 多列操作（some、someLike）</li>
 *   <li>{@link SubQueryWhereClause} - 子查询（EXISTS、NOT EXISTS、ANY）</li>
 *   <li>{@link RegexpWhereClause} - 正则表达式匹配（REGEXP、NOT REGEXP）</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 链式调用组合多种 WHERE 条件
 * SelectSqlBuilderV2 builder = SelectSqlBuilderV2.create(dialect, "users")
 *     .eq("status", 1)                    // 比较操作
 *     .gtIfNotNull("age", minAge)         // 条件控制
 *     .like("name", "张")                  // 模糊匹配
 *     .isNotNull("email")                 // 空值检查
 *     .in("role", Arrays.asList(1, 2))    // 范围查询
 *     .some(new String[]{"title", "content"}, Oper.LIKE, keyword)  // 多列操作
 *     .exists("SELECT 1 FROM orders WHERE user_id = users.id")     // 子查询
 *     .regexp("phone", "^1[3-9]\\d{9}$"); // 正则表达式
 * }</pre>
 * <p>
 * 所有子接口提供的方法都支持：
 * <ul>
 *   <li>链式调用，返回构建器本身</li>
 *   <li>条件判断（If 后缀方法）</li>
 *   <li>空值处理（IfNotNull、IfNotBlank 后缀方法）</li>
 *   <li>逻辑连接符（AND/OR）</li>
 * </ul>
 * 
 * @param <T> 具体的 SQL 构建器类型
 * @author Cody Lu
 * @since 2024-11-23
 * @see ComparisonWhereClause 比较操作子接口
 * @see ConditionalWhereClause 条件控制子接口
 * @see LikeWhereClause 模糊匹配子接口
 * @see NullCheckWhereClause 空值检查子接口
 * @see RangeWhereClause 范围查询子接口
 * @see MultiColumnWhereClause 多列操作子接口
 * @see SubQueryWhereClause 子查询子接口
 * @see RegexpWhereClause 正则表达式子接口
 */
public interface WhereClause<T extends SqlBuilder> extends
    ComparisonWhereClause<T>,
    ConditionalWhereClause<T>,
    LikeWhereClause<T>,
    NullCheckWhereClause<T>,
    RangeWhereClause<T>,
    MultiColumnWhereClause<T>,
    SubQueryWhereClause<T>,
    RegexpWhereClause<T> {

  // 该接口是一个组合接口，所有方法由子接口提供
  // 无需额外定义方法
}
