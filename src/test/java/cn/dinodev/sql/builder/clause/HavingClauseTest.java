// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSql;
import static cn.dinodev.sql.testutil.SqlTestHelper.assertSqlWithParams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.Oper;
import cn.dinodev.sql.builder.SelectSqlBuilder;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

/**
 * HAVING 子句功能测试类。
 * 
 * <p>测试 {@link SelectSqlBuilder} 中 HAVING 相关功能，包括：
 * <ul>
 *   <li>基本 HAVING 条件</li>
 *   <li>聚合函数（COUNT, SUM, AVG, MAX, MIN）的便捷方法</li>
 *   <li>AND/OR 逻辑组合</li>
 *   <li>条件式 HAVING（havingXxxIf）</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2024-12-31
 */
@DisplayName("HavingClause 功能测试")
public class HavingClauseTest {

    private MysqlDialect mysql;

    @BeforeEach
    public void setUp() {
        mysql = new MysqlDialect(null, new CamelNamingConversition());
    }

    @Test
    @DisplayName("基本 HAVING")
    void testBasicHaving() {
        SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
                .columns("user_id", "COUNT(*) AS cnt")
                .groupBy("user_id")
                .having("COUNT(*) > 5");

        assertSql(builder, "基本HAVING",
                "SELECT user_id, COUNT(*) AS cnt FROM orders GROUP BY user_id HAVING COUNT(*) > 5");
    }

    @Test
    @DisplayName("使用操作符的 HAVING")
    void testHavingWithOperator() {
        SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
                .columns("user_id", "SUM(amount) AS total")
                .groupBy("user_id")
                .having("SUM(amount)", Oper.GTE, 1000);

        assertSqlWithParams(builder, "使用操作符的HAVING",
                "SELECT user_id, SUM(amount) AS total FROM orders GROUP BY user_id HAVING SUM(amount) >= ?",
                new Object[] { 1000 });
    }

    @Test
    @DisplayName("多个 HAVING 条件（AND）")
    void testMultipleHaving() {
        SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
                .columns("user_id", "COUNT(*) AS cnt", "SUM(amount) AS total")
                .groupBy("user_id")
                .having("COUNT(*) > ?", 5)
                .andHaving("SUM(amount) > ?", 1000);

        assertSqlWithParams(builder, "多个HAVING条件（AND）",
                "SELECT user_id, COUNT(*) AS cnt, SUM(amount) AS total FROM orders "
                        + "GROUP BY user_id HAVING COUNT(*) > ? AND SUM(amount) > ?",
                new Object[] { 5, 1000 });
    }

    @Test
    @DisplayName("OR HAVING")
    void testOrHaving() {
        SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
                .columns("category", "COUNT(*) AS cnt", "SUM(amount) AS total")
                .groupBy("category")
                .having("COUNT(*) > ?", 100)
                .orHaving("SUM(amount) > ?", 10000);

        assertSqlWithParams(builder, "OR HAVING",
                "SELECT category, COUNT(*) AS cnt, SUM(amount) AS total FROM orders "
                        + "GROUP BY category HAVING COUNT(*) > ? OR SUM(amount) > ?",
                new Object[] { 100, 10000 });
    }

    @Test
    @DisplayName("COUNT 相关便捷方法")
    void testCountFunctions() {
        SelectSqlBuilder builderGt = SelectSqlBuilder.create(mysql, "orders")
                .columns("user_id", "COUNT(*) AS cnt")
                .groupBy("user_id")
                .havingCountGt(10);
        assertSqlWithParams(builderGt, "havingCountGt",
                "SELECT user_id, COUNT(*) AS cnt FROM orders GROUP BY user_id HAVING COUNT(*) > ?",
                new Object[] { 10L });

        SelectSqlBuilder builderGte = SelectSqlBuilder.create(mysql, "orders")
                .columns("user_id", "COUNT(*) AS cnt")
                .groupBy("user_id")
                .havingCountGte(5);
        assertSqlWithParams(builderGte, "havingCountGte",
                "SELECT user_id, COUNT(*) AS cnt FROM orders GROUP BY user_id HAVING COUNT(*) >= ?",
                new Object[] { 5L });

        SelectSqlBuilder builderLt = SelectSqlBuilder.create(mysql, "orders")
                .columns("user_id", "COUNT(*) AS cnt")
                .groupBy("user_id")
                .havingCountLt(100);
        assertSqlWithParams(builderLt, "havingCountLt",
                "SELECT user_id, COUNT(*) AS cnt FROM orders GROUP BY user_id HAVING COUNT(*) < ?",
                new Object[] { 100L });

        SelectSqlBuilder builderEq = SelectSqlBuilder.create(mysql, "orders")
                .columns("user_id", "COUNT(*) AS cnt")
                .groupBy("user_id")
                .havingCountEq(20);
        assertSqlWithParams(builderEq, "havingCountEq",
                "SELECT user_id, COUNT(*) AS cnt FROM orders GROUP BY user_id HAVING COUNT(*) = ?",
                new Object[] { 20L });

        SelectSqlBuilder builderNe = SelectSqlBuilder.create(mysql, "orders")
                .columns("user_id", "COUNT(*) AS cnt")
                .groupBy("user_id")
                .havingCountNe(0);
        assertSqlWithParams(builderNe, "havingCountNe",
                "SELECT user_id, COUNT(*) AS cnt FROM orders GROUP BY user_id HAVING COUNT(*) <> ?",
                new Object[] { 0L });
    }

    @Test
    @DisplayName("SUM 相关便捷方法")
    void testSumFunctions() {
        SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysql, "orders")
                .columns("user_id", "SUM(amount) AS total")
                .groupBy("user_id")
                .havingSumGt("amount", 1000);
        assertSql(builder1, "havingSumGt",
                "SELECT user_id, SUM(amount) AS total FROM orders GROUP BY user_id HAVING SUM(amount) > ?");

        SelectSqlBuilder builder2 = SelectSqlBuilder.create(mysql, "orders")
                .columns("user_id", "SUM(amount) AS total")
                .groupBy("user_id")
                .havingSumGte("amount", 500);
        assertSql(builder2, "havingSumGte",
                "SELECT user_id, SUM(amount) AS total FROM orders GROUP BY user_id HAVING SUM(amount) >= ?");

        SelectSqlBuilder builder3 = SelectSqlBuilder.create(mysql, "orders")
                .columns("user_id", "SUM(amount) AS total")
                .groupBy("user_id")
                .havingSumLt("amount", 10000);
        assertSql(builder3, "havingSumLt",
                "SELECT user_id, SUM(amount) AS total FROM orders GROUP BY user_id HAVING SUM(amount) < ?");
    }

    @Test
    @DisplayName("AVG 相关便捷方法")
    void testAvgFunctions() {
        SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysql, "students")
                .columns("class_id", "AVG(score) AS avg_score")
                .groupBy("class_id")
                .havingAvgGt("score", 60);
        assertSql(builder1, "havingAvgGt",
                "SELECT class_id, AVG(score) AS avg_score FROM students GROUP BY class_id HAVING AVG(score) > ?");

        SelectSqlBuilder builder2 = SelectSqlBuilder.create(mysql, "students")
                .columns("class_id", "AVG(score) AS avg_score")
                .groupBy("class_id")
                .havingAvgGte("score", 70);
        assertSql(builder2, "havingAvgGte",
                "SELECT class_id, AVG(score) AS avg_score FROM students GROUP BY class_id HAVING AVG(score) >= ?");

        SelectSqlBuilder builder3 = SelectSqlBuilder.create(mysql, "students")
                .columns("class_id", "AVG(score) AS avg_score")
                .groupBy("class_id")
                .havingAvgLte("score", 90);
        assertSql(builder3, "havingAvgLte",
                "SELECT class_id, AVG(score) AS avg_score FROM students GROUP BY class_id HAVING AVG(score) <= ?");
    }

    @Test
    @DisplayName("MAX/MIN 相关便捷方法")
    void testMaxMinFunctions() {
        SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysql, "products")
                .columns("category_id", "MAX(price) AS max_price")
                .groupBy("category_id")
                .havingMaxGt("price", 100);
        assertSql(builder1, "havingMaxGt",
                "SELECT category_id, MAX(price) AS max_price FROM products GROUP BY category_id HAVING MAX(price) > ?");

        SelectSqlBuilder builder2 = SelectSqlBuilder.create(mysql, "products")
                .columns("category_id", "MIN(price) AS min_price")
                .groupBy("category_id")
                .havingMinGte("price", 10);
        assertSql(builder2, "havingMinGte",
                "SELECT category_id, MIN(price) AS min_price FROM products GROUP BY category_id HAVING MIN(price) >= ?");

        SelectSqlBuilder builder3 = SelectSqlBuilder.create(mysql, "products")
                .columns("category_id", "MAX(price) AS max_price", "MIN(price) AS min_price")
                .groupBy("category_id")
                .havingMaxLt("price", 1000)
                .andHaving("MIN(price) > ?", 5);
        assertSqlWithParams(builder3, "组合MAX/MIN",
                "SELECT category_id, MAX(price) AS max_price, MIN(price) AS min_price FROM products "
                        + "GROUP BY category_id HAVING MAX(price) < ? AND MIN(price) > ?",
                new Object[] { 1000, 5 });
    }

    @Test
    @DisplayName("条件 HAVING")
    void testConditionalHaving() {
        boolean needFilter = true;
        boolean skipFilter = false;

        SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "orders")
                .columns("user_id", "COUNT(*) AS cnt", "SUM(amount) AS total")
                .groupBy("user_id")
                .havingCountGtIf(needFilter, 10)
                .havingSumGtIf(skipFilter, "amount", 1000)
                .havingAvgGteIf(needFilter, "amount", 100);

        assertSqlWithParams(builder, "条件HAVING",
                "SELECT user_id, COUNT(*) AS cnt, SUM(amount) AS total FROM orders "
                        + "GROUP BY user_id HAVING COUNT(*) > ? AND AVG(amount) >= ?",
                new Object[] { 10L, 100 });
    }
}
