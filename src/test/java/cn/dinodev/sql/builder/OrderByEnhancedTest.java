// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSql;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.NullsOrder;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.dialect.PostgreSQLDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

/**
 * ORDER BY 增强功能测试类。
 * 
 * <p>测试 ORDER BY 的 SQL 标准特性，包括：
 * <ul>
 *   <li>NULLS FIRST / NULLS LAST 语法</li>
 *   <li>位置参数排序（序号排序）</li>
 *   <li>CASE WHEN 排序表达式</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2024-12-31
 */
@DisplayName("ORDER BY增强功能测试（SQL标准）")
public class OrderByEnhancedTest {

        private MysqlDialect mysqlDialect;
        private PostgreSQLDialect postgresDialect;

        @BeforeEach
        public void setUp() throws Exception {
                mysqlDialect = new MysqlDialect(null, new CamelNamingConversition());
                postgresDialect = new PostgreSQLDialect(null, new CamelNamingConversition());
        }

        @Test
        @DisplayName("测试NULLS FIRST/LAST排序")
        void testNullsOrder() {
                SelectSqlBuilder builder1 = SelectSqlBuilder.create(postgresDialect, "products")
                                .column("id", "name", "score")
                                .orderByWithNullsOrder("score", false, NullsOrder.NULLS_LAST);

                assertSql(builder1, "NULLS LAST排序",
                                "SELECT id, name, score FROM products ORDER BY score DESC NULLS LAST");

                SelectSqlBuilder builder2 = SelectSqlBuilder.create(postgresDialect, "products")
                                .column("id", "name", "score", "rating")
                                .orderByDescWithNullsOrder("score", NullsOrder.NULLS_LAST)
                                .orderByAscWithNullsOrder("rating", NullsOrder.NULLS_FIRST);

                assertSql(builder2, "多字段NULLS排序",
                                "SELECT id, name, score, rating FROM products ORDER BY score DESC NULLS LAST, rating ASC NULLS FIRST");
        }

        @Test
        @DisplayName("测试按位置编号排序")
        void testOrderByPosition() {
                SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .column("name", "age", "score")
                                .orderByPosition(3, false)
                                .orderByPosition(1, true);

                assertSql(builder1, "按位置编号排序",
                                "SELECT name, age, score FROM users ORDER BY 3 DESC, 1 ASC");

                SelectSqlBuilder builder2 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .column("name", "age", "score", "status")
                                .orderByPositionDesc(3)
                                .orderByPositionAsc(1, 2);

                assertSql(builder2, "多位置编号排序",
                                "SELECT name, age, score, status FROM users ORDER BY 3 DESC, 1 ASC, 2 ASC");
        }

        @Test
        @DisplayName("测试COLLATE排序规则")
        void testOrderByCollate() {
                SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .column("id", "name")
                                .orderByWithCollate("name", true, "utf8mb4_unicode_ci");

                assertSql(builder1, "MySQL COLLATE排序",
                                "SELECT id, name FROM users ORDER BY name COLLATE utf8mb4_unicode_ci ASC");

                SelectSqlBuilder builder2 = SelectSqlBuilder.create(postgresDialect, "users")
                                .column("id", "title")
                                .orderByWithCollate("title", false, "en_US");

                assertSql(builder2, "PostgreSQL COLLATE排序",
                                "SELECT id, title FROM users ORDER BY title COLLATE en_US DESC");
        }

        @Test
        @DisplayName("测试表达式排序")
        void testOrderByExpression() {
                SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .column("id", "name", "email")
                                .orderBy("UPPER(name) ASC");

                assertSql(builder1, "函数表达式排序",
                                "SELECT id, name, email FROM users ORDER BY UPPER(name) ASC");

                SelectSqlBuilder builder2 = SelectSqlBuilder.create(mysqlDialect, "orders")
                                .column("id", "price", "quantity")
                                .orderBy("(price * quantity)", false);

                assertSql(builder2, "计算表达式排序",
                                "SELECT id, price, quantity FROM orders ORDER BY (price * quantity) DESC");

                SelectSqlBuilder builder3 = SelectSqlBuilder.create(mysqlDialect, "tasks")
                                .column("id", "title", "status")
                                .orderBy("CASE WHEN status = 'urgent' THEN 1 WHEN status = 'high' THEN 2 ELSE 3 END ASC");

                assertSql(builder3, "CASE表达式排序",
                                "SELECT id, title, status FROM tasks ORDER BY CASE WHEN status = 'urgent' THEN 1 WHEN status = 'high' THEN 2 ELSE 3 END ASC");
        }

        @Test
        @DisplayName("测试复杂组合排序")
        void testComplexOrderBy() {
                SelectSqlBuilder builder = SelectSqlBuilder.create(postgresDialect, "products")
                                .column("id", "category", "name", "price", "rating", "stock")
                                .orderBy("category")
                                .orderBy("LOWER(name)", true)
                                .orderByWithNullsOrder("price", false, NullsOrder.NULLS_LAST)
                                .orderByDescWithNullsOrder("rating", NullsOrder.NULLS_FIRST);

                assertSql(builder, "复杂组合排序",
                                "SELECT id, category, name, price, rating, stock FROM products ORDER BY category, LOWER(name) ASC, price DESC NULLS LAST, rating DESC NULLS FIRST");
        }

        @Test
        @DisplayName("测试orderByAscIf和orderByDescIf条件排序")
        void testOrderByAscDescIf() {
                boolean sortByName = true;
                SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .column("id", "name", "age")
                                .orderByAscIf(sortByName, "name", "age")
                                .orderByDesc("id");

                assertSql(builder1, "条件排序（true）",
                                "SELECT id, name, age FROM users ORDER BY name ASC, age ASC, id DESC");

                boolean sortByDate = false;
                SelectSqlBuilder builder2 = SelectSqlBuilder.create(mysqlDialect, "orders")
                                .column("id", "amount", "created_at")
                                .orderByDescIf(sortByDate, "created_at")
                                .orderByDesc("amount");

                assertSql(builder2, "条件排序（false）",
                                "SELECT id, amount, created_at FROM orders ORDER BY amount DESC");

                boolean hasScore = true;
                SelectSqlBuilder builder3 = SelectSqlBuilder.create(postgresDialect, "students")
                                .column("id", "name", "score")
                                .orderByAscIfWithNullsOrder(hasScore, NullsOrder.NULLS_LAST, "name")
                                .orderByDescIfWithNullsOrder(hasScore, NullsOrder.NULLS_FIRST, "score");

                assertSql(builder3, "条件NULLS排序（true）",
                                "SELECT id, name, score FROM students ORDER BY name ASC NULLS LAST, score DESC NULLS FIRST");

                hasScore = false;
                SelectSqlBuilder builder4 = SelectSqlBuilder.create(postgresDialect, "students")
                                .column("id", "name", "score")
                                .orderByAscIfWithNullsOrder(hasScore, NullsOrder.NULLS_LAST, "name")
                                .orderByDescIfWithNullsOrder(hasScore, NullsOrder.NULLS_FIRST, "score")
                                .orderBy("id");

                String sql4 = builder4.getSql();
                String expectedSql4 = "SELECT id, name, score FROM students ORDER BY id";
                assertEquals(expectedSql4, sql4, "不应包含NULLS排序");
        }
}
