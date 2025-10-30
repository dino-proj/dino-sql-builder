// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import cn.dinodev.sql.dialect.Dialect;

/**
 * SQL SELECT语句构建器。
 * <p>
 * 用于构建 SELECT SQL 语句的 Builder 类，支持多表、分组、排序、连接等。
 * <br>
 * <b>使用示例：</b>
 * <pre>
 * SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "user")
 *     .column("id", "name", "age")
 *     .where("age > ?", 18)
 *     .orderBy("age desc")
 *     .limit(10);
 * String sql = builder.getSql();
 * Object[] params = builder.getParams();
 * </pre>
 *
 * @author Cody Lu
 * @since 2022-03-07
 */

public final class SelectSqlBuilder extends WhereSql<SelectSqlBuilder> implements SqlBuilderUtils {

  private static final String TABLE_ALIAS_FORMAT = "%s AS %s";
  private static final String TABLE_ALIAS_ON_FORMAT = "%s AS %s ON %s";

  private boolean distinctFlag;

  private final List<String> columnsList = new ArrayList<>();

  private final List<JoinEntity<String>> joins = new ArrayList<>();

  private final List<String> groupBysList = new ArrayList<>();

  private final List<String> havingsList = new ArrayList<>();

  private final List<JoinEntity<SelectSqlBuilder>> unions = new ArrayList<>();

  private final List<String> orderBysList = new ArrayList<>();

  private final List<Object> havingParams = new ArrayList<>();

  private final List<Object> joinParams = new ArrayList<>();

  private int limitValue;

  private long offset;

  private final Dialect dialect;

  /**
   * 私有构造函数，防止直接实例化。
   *
   * @param dialect 数据库方言实例
   */
  private SelectSqlBuilder(final Dialect dialect) {
    this.dialect = dialect;
  }

  /**
   * 私有构造函数，防止直接实例化。
   *
   * @param dialect 数据库方言实例
   * @param table 表名
   */
  private SelectSqlBuilder(final Dialect dialect, final String table) {
    this.dialect = dialect;
    this.tables.add(table);
  }

  /**
   * 私有构造函数，防止直接实例化。
   *
   * @param dialect 数据库方言实例
   * @param table 表名
   * @param alias 表别名
   */
  private SelectSqlBuilder(final Dialect dialect, final String table, final String alias) {
    this.dialect = dialect;
    this.tables.add(TABLE_ALIAS_FORMAT.formatted(table, alias));
  }

  /**
   * 私有构造函数，防止直接实例化。
   *
   * @param subQuery 子查询 SelectSqlBuilder 实例
   * @param alias 子查询别名
   */
  private SelectSqlBuilder(final SelectSqlBuilder subQuery, final String alias) {
    this.dialect = subQuery.dialect;
    this.tables.add(String.format("( %s ) AS %s", subQuery.getSql(), alias));
    this.whereParams.addAll(Arrays.asList(subQuery.getParams()));
  }

  /**
   * 创建 SelectSqlBuilder 实例。
   *
   * @param dialect 数据库方言实例
   * @return 配置好的 SelectSqlBuilder 实例
   */
  public static SelectSqlBuilder create(final Dialect dialect) {
    SelectSqlBuilder builder = new SelectSqlBuilder(dialect);
    builder.initializeBuilder();
    return builder;
  }

  /**
   * 根据表名创建 SelectSqlBuilder 实例。
   * <p>支持的格式：
   * <ul>
   *   <li>"table1"</li>
   *   <li>"table1, table2"</li>
   *   <li>"table1 as t1"</li>
   *   <li>"table1 as t1 join table2 as t2 on t1.id=t2.id"</li>
   * </ul>
   *
   * @param dialect 数据库方言实例
   * @param table 表名
   * @return SelectSqlBuilder 实例
   */
  public static SelectSqlBuilder create(final Dialect dialect, final String table) {
    SelectSqlBuilder builder = new SelectSqlBuilder(dialect, table);
    builder.initializeBuilder();
    return builder;
  }

  /**
   * 根据表名和别名创建 SelectSqlBuilder 实例。
   * <p>生成的 SQL 片段为：table AS alias
   *
   * @param dialect 数据库方言实例
   * @param table 表名
   * @param alias 表别名
   * @return SelectSqlBuilder 实例
   */
  public static SelectSqlBuilder create(final Dialect dialect, final String table, final String alias) {
    SelectSqlBuilder builder = new SelectSqlBuilder(dialect, table, alias);
    builder.initializeBuilder();
    return builder;
  }

  /**
   * 根据子查询创建 SelectSqlBuilder 实例。
   *
   * @param subQuery 子查询 SelectSqlBuilder 实例
   * @param alias 子查询的别名
   * @return SelectSqlBuilder 实例
   */
  public static SelectSqlBuilder create(final SelectSqlBuilder subQuery, final String alias) {
    SelectSqlBuilder builder = new SelectSqlBuilder(subQuery, alias);
    builder.initializeBuilder();
    return builder;
  }

  /**
   * 初始化构建器。
   */
  private void initializeBuilder() {
    setThat(this);
  }

  /**
   * 添加查询列信息，可以逐个添加，也可以添加多个，用逗号隔开。
   * <br>用法示例：
   * <pre>
   * builder.column("col1, col2, 'abc' as col3");
   * builder.column("col1").column("col2").column("col3, col4");
   * </pre>
   *
   * @param name 查询列名或表达式
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder column(final String name) {
    columnsList.add(name);
    return this;
  }

  /**
   * 添加多个查询列，每个参数可以是一个列名或多个列名。
   * <br>用法示例：
   * <pre>
   * builder.columns("col1", "col2", "'abc' as col3");
   * builder.columns("col1, col2", "col3");
   * </pre>
   *
   * @param names 查询列名或表达式，可变参数
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder columns(final String... names) {
    columnsList.addAll(Arrays.asList(names));
    return this;
  }

  /**
   * 添加分组表达式。
   * <br>用法示例：
   * <pre>
   * builder.groupBy("col1", "col2");
   * builder.groupBy("col1, col2");
   * </pre>
   *
   * @param expr 分组字段或表达式，可变参数
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder groupBy(final String... expr) {
    groupBysList.addAll(Arrays.asList(expr));
    return this;
  }

  /**
   * 添加 ORDER BY 排序表达式。
   * <br>用法示例：
   * <pre>
   * builder.orderBy("col1 desc", "col2");
   * builder.orderBy("col1, col2");
   * builder.orderBy("col1").orderBy("col2 desc");
   * </pre>
   *
   * @param expr 排序字段或表达式，可变参数
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder orderBy(final String... expr) {
    if (expr != null) {
      orderBysList.addAll(Arrays.asList(expr));
    }
    return this;
  }

  /**
   * 添加 ORDER BY 排序表达式，并指明是否 ASC 升序。
   * <br>用法示例：
   * <pre>
   * builder.orderBy("col1", true); // ORDER BY col1 ASC
   * builder.orderBy("col2", false); // ORDER BY col2 DESC
   * </pre>
   *
   * @param name 排序字段名
   * @param ascending 是否升序，true 为 ASC，false 为 DESC
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder orderBy(final String name, final boolean ascending) {
    if (ascending) {
      orderBysList.add(name + " ASC");
    } else {
      orderBysList.add(name + " DESC");
    }
    return this;
  }

  /**
   * 添加 HAVING 条件表达式。
   * <br>用法示例：
   * <pre>
   * builder.having("cnt > 10");
   * </pre>
   * 多个条件用 AND 连接。
   *
   * @param expr HAVING 条件表达式
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder having(final String expr) {
    havingsList.add(expr);
    return this;
  }

  /**
   * 添加 HAVING 条件表达式，带参数。
   * <br>用法示例：
   * <pre>
   * builder.having("cnt > ? or type = ?", n, type);
   * </pre>
   * 多个条件用 AND 连接。
   *
   * @param expr HAVING 条件表达式
   * @param values 条件参数，可变参数
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder having(final String expr, final Object... values) {
    having(expr);
    if (values != null) {
      havingParams.addAll(Arrays.asList(values));
    }
    return this;
  }

  /**
   * 与另一个查询做 UNION 连接。
   * <br>若需 UNION ALL 请使用 {@link #unionAll(SelectSqlBuilder)}。
   *
   * @param selectSql 另一个 SelectSqlBuilder 实例
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder union(final SelectSqlBuilder selectSql) {
    unions.add(new JoinEntity<>("\nUNION\n", selectSql));
    return this;
  }

  /**
   * 与另一个查询做 UNION ALL 连接。
   * <br>若需 UNION 请使用 {@link #union(SelectSqlBuilder)}。
   *
   * @param selectSql 另一个 SelectSqlBuilder 实例
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder unionAll(final SelectSqlBuilder selectSql) {
    unions.add(new JoinEntity<>("\nUNION ALL\n", selectSql));
    return this;
  }

  /**
   * JOIN 内连接表。
   * <br>用法示例：
   * <pre>
   * builder.join("table2");
   * builder.join("table2 AS t2");
   * builder.join("table2 AS t2 ON t1.id = t2.classId");
   * </pre>
   *
   * @param joinExpr JOIN 表达式
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder join(final String joinExpr) {
    joins.add(new JoinEntity<>("JOIN", joinExpr));
    return this;
  }

  /**
   * JOIN 内连接表，并给表指定别名。
   * <br>用法示例：
   * <pre>
   * builder.join("table2", "t2"); // JOIN table2 AS t2
   * </pre>
   *
   * @param table 表名
   * @param alias 表别名
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder join(final String table, final String alias) {
    return join(String.format(TABLE_ALIAS_FORMAT, table, alias));
  }

  /**
   * JOIN 内连接表，并给表指定别名和连接条件表达式。
   * <br>用法示例：
   * <pre>
   * builder.join("table2", "t2", "t1.id=t2.classId AND t2.status=2");
   * </pre>
   *
   * @param table 表名
   * @param alias 表别名
   * @param onExpr 连接条件表达式
   * @param values 连接参数，可变参数
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder join(final String table, final String alias, final String onExpr, final Object... values) {
    join(String.format(TABLE_ALIAS_ON_FORMAT, table, alias, onExpr));
    if (values != null) {
      joinParams.addAll(Arrays.asList(values));
    }
    return this;
  }

  /**
   * LEFT JOIN 左连接表。
   * <br>用法示例：
   * <pre>
   * builder.leftJoin("table2");
   * builder.leftJoin("table2 AS t2");
   * builder.leftJoin("table2 AS t2 ON t1.id = t2.classId");
   * </pre>
   *
   * @param joinExpr LEFT JOIN 表达式
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder leftJoin(final String joinExpr) {
    joins.add(new JoinEntity<>("LEFT JOIN", joinExpr));
    return this;
  }

  /**
   * LEFT JOIN 左连接表，并给表指定别名。
   * <br>用法示例：
   * <pre>
   * builder.leftJoin("table2", "t2"); // LEFT JOIN table2 AS t2
   * </pre>
   *
   * @param table 表名
   * @param alias 表别名
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder leftJoin(final String table, final String alias) {
    return leftJoin(String.format(TABLE_ALIAS_FORMAT, table, alias));
  }

  /**
   * LEFT JOIN 左连接表，并给表指定别名和连接条件表达式。
   * <br>用法示例：
   * <pre>
   * builder.leftJoin("table2", "t2", "t1.id=t2.classId AND t2.status=2");
   * </pre>
   *
   * @param table 表名
   * @param alias 表别名
   * @param onExpr 连接条件表达式
   * @param values 连接参数，可变参数
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder leftJoin(final String table, final String alias, final String onExpr,
      final Object... values) {
    leftJoin(String.format(TABLE_ALIAS_ON_FORMAT, table, alias, onExpr));
    if (values != null) {
      joinParams.addAll(Arrays.asList(values));
    }
    return this;
  }

  /**
   * RIGHT JOIN 右连接表。
   * <br>用法示例：
   * <pre>
   * builder.rightJoin("table2");
   * builder.rightJoin("table2 AS t2");
   * builder.rightJoin("table2 AS t2 ON t1.id = t2.classId");
   * </pre>
   *
   * @param joinExpr RIGHT JOIN 表达式
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder rightJoin(final String joinExpr) {
    joins.add(new JoinEntity<>("RIGHT JOIN", joinExpr));
    return this;
  }

  /**
   * RIGHT JOIN 右连接表，并给表指定别名。
   * <br>用法示例：
   * <pre>
   * builder.rightJoin("table2", "t2"); // RIGHT JOIN table2 AS t2
   * </pre>
   *
   * @param table 表名
   * @param alias 表别名
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder rightJoin(final String table, final String alias) {
    return rightJoin(String.format(TABLE_ALIAS_FORMAT, table, alias));
  }

  /**
   * RIGHT JOIN 右连接表，并给表指定别名和连接条件表达式。
   * <br>用法示例：
   * <pre>
   * builder.rightJoin("table2", "t2", "t1.id=t2.classId AND t2.status=2");
   * </pre>
   *
   * @param table 表名
   * @param alias 表别名
   * @param onExpr 连接条件表达式
   * @param values 连接参数，可变参数
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder rightJoin(final String table, final String alias, final String onExpr,
      final Object... values) {
    rightJoin(String.format(TABLE_ALIAS_ON_FORMAT, table, alias, onExpr));
    if (values != null) {
      joinParams.addAll(Arrays.asList(values));
    }
    return this;
  }

  /**
   * CROSS JOIN 交叉连接，cross join 不可以加 on。
   * <br>用法示例：
   * <pre>
   * builder.crossJoin("table2"); // CROSS JOIN table2
   * builder.crossJoin("jsonb_array_elements(knowledge)"); // CROSS JOIN jsonb_array_elements(knowledge)
   * </pre>
   *
   * @param joinExpr CROSS JOIN 表达式
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder crossJoin(final String joinExpr) {
    joins.add(new JoinEntity<>("CROSS JOIN", joinExpr));
    return this;
  }

  /**
   * CROSS JOIN 交叉连接，cross join 不可以加 on。
   * <br>用法示例：
   * <pre>
   * builder.crossJoin("table2", "t2"); // CROSS JOIN table2 AS t2
   * builder.crossJoin("jsonb_array_elements(knowledge)", "value"); // CROSS JOIN jsonb_array_elements(knowledge) AS value
   * </pre>
   *
   * @param joinExpr CROSS JOIN 表达式
   * @param alias 别名
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder crossJoin(final String joinExpr, final String alias) {
    return crossJoin(String.format(TABLE_ALIAS_FORMAT, joinExpr, alias));
  }

  /**
   * 声明为 DISTINCT 查询。
   *
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder distinct() {
    this.distinctFlag = true;
    return this;
  }

  /**
   * 使用 LIMIT 限制查询条数。
   * <br>生成的 SQL 语句如：LIMIT [limit]
   *
   * @param limit 限制条数
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder limit(final int limit) {
    return this.limit(limit, 0);
  }

  /**
   * 使用 LIMIT 限制查询条数。
   * <br>生成的 SQL 语句如：LIMIT [offset], [limit]
   * <br>如需使用 OFFSET 关键字，请调用 limitOffset(int, long) 方法。
   *
   * @param limit 限制条数
   * @param offset 偏移量
   * @return 当前 SelectSqlBuilder 实例，便于链式调用
   */
  public SelectSqlBuilder limit(final int limit, final long offset) {
    this.limitValue = limit;
    this.offset = offset;
    return this;
  }

  @Override
  public String toString() {
    return this.getSql();
  }

  @Override
  public String getSql() {
    return getSql(false);
  }

  private String getSql(boolean isCount) {
    final StringBuilder sql = new StringBuilder(64);

    if (withSql != null) {
      sql.append("WITH ").append(withName).append(" AS (\n").append(withSql.getSql()).append("\n)\n");
    }

    sql.append("SELECT ");
    appendColumn(sql, isCount);

    appendList(sql, tables, " FROM ", ", ");
    appendList(sql, joins, " ", " ");
    appendList(sql, whereColumns, " WHERE ", " ");
    appendList(sql, groupBysList, " GROUP BY ", ", ");
    appendList(sql, havingsList, " HAVING ", " AND ");
    appendList(sql, unions, "  ", " \n ");

    if (isCount) {
      return sql.toString();
    }

    appendList(sql, orderBysList, " ORDER BY ", ", ");

    if (limitValue > 0) {
      sql.append(' ').append(dialect.limitOffset(limitValue, offset));
    }

    return sql.toString();
  }

  private void appendColumn(StringBuilder sql, boolean isCount) {
    if (distinctFlag && !isCount) {
      sql.append("DISTINCT ");
    }
    if (isCount) {
      sql.append("count(1) AS cnt");
    } else if (columnsList.isEmpty()) {
      sql.append('*');
    } else {
      appendList(sql, columnsList, "", ", ");
    }
  }

  /**
   * 获取用于计数的 SQL 语句。
   *
   * @return 计数 SQL 语句字符串
   */
  public String getCountSql() {
    return getSql(true);
  }

  /**
   * 获取 SQL 语句的所有参数数组。
   *
   * @return 参数数组，按 SQL 占位符顺序排列
   */
  @Override
  public Object[] getParams() {
    Stream<Object[]> paramsArr = Stream.of(
        withSql == null ? EMPTY_PARAMS : withSql.getParams(),
        joinParams.toArray(),
        whereParams.toArray(),
        havingParams.toArray());
    paramsArr = Stream.concat(paramsArr, unions.stream().map(v -> v.expr.getParams()));

    return paramsArr.flatMap(Arrays::stream).toArray();
  }

  /**
   * 连接实体类，用于表示 SQL 连接操作。
   */
  private static class JoinEntity<V> {
    private final String op;
    private final V expr;

    /**
     * 构造 JoinEntity 实例。
     *
     * @param op 连接操作类型（如 JOIN、LEFT JOIN）
     * @param expr 连接表达式
     */
    public JoinEntity(final String op, final V expr) {
      this.op = op;
      this.expr = expr;
    }

    @Override
    public String toString() {
      return op + " " + expr.toString();
    }
  }

}
