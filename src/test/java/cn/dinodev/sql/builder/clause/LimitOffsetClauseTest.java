// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.builder.SelectSqlBuilder;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.dialect.PostgreSQLDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

/**
 * LIMIT/OFFSET 子句测试类。
 * 
 * <p>测试 LIMIT 和 OFFSET 功能，包括：
 * <ul>
 *   <li>基础 LIMIT/OFFSET 语法</li>
 *   <li>PostgreSQL 的 FETCH FIRST ... ROWS ONLY 语法</li>
 *   <li>LIMIT ALL 语法</li>
 *   <li>不同数据库方言的兼容性</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2024-12-31
 */
@DisplayName("LIMIT/OFFSET子句测试")
public class LimitOffsetClauseTest {

        private MysqlDialect mysqlDialect;
        private PostgreSQLDialect postgresDialect;

        @BeforeEach
        public void setUp() throws Exception {
                mysqlDialect = new MysqlDialect(null, new CamelNamingConversition());
                postgresDialect = new PostgreSQLDialect(null, new CamelNamingConversition());
        }

        @Test
        @DisplayName("测试基本LIMIT/OFFSET功能")
        void testBasicLimitOffset() {
                SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .columns("id", "name")
                                .limit(10);

                assertSql(builder1, "基本 LIMIT",
                                "SELECT id, name FROM users LIMIT 10");

                SelectSqlBuilder builder2 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .columns("id", "name")
                                .limit(10)
                                .offset(20);

                assertSql(builder2, "LIMIT + OFFSET",
                                "SELECT id, name FROM users LIMIT 10 OFFSET 20");

                SelectSqlBuilder builder3 = SelectSqlBuilder.create(postgresDialect, "users")
                                .columns("id", "name")
                                .limit(15, 30);

                assertSql(builder3, "limitOffset",
                                "SELECT id, name FROM users LIMIT 15 OFFSET 30");
        }

        @Test
        @DisplayName("测试分页查询")
        void testLimitPage() {
                SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysqlDialect, "products")
                                .columns("id", "name", "price")
                                .limitPage(1, 10);

                assertSql(builder1, "第1页",
                                "SELECT id, name, price FROM products LIMIT 10");

                SelectSqlBuilder builder2 = SelectSqlBuilder.create(mysqlDialect, "products")
                                .columns("id", "name", "price")
                                .limitPage(3, 10);

                assertSql(builder2, "第3页",
                                "SELECT id, name, price FROM products LIMIT 10 OFFSET 20");

                SelectSqlBuilder builder3 = SelectSqlBuilder.create(mysqlDialect, "products")
                                .columns("id", "name")
                                .limitPage(0, 5);

                assertSql(builder3, "页码<1自动修正",
                                "SELECT id, name FROM products LIMIT 5");
        }

        @Test
        @DisplayName("测试条件限制方法")
        void testConditionalMethods() {
                boolean needLimit = true;
                SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .columns("id", "name")
                                .limitIf(needLimit, 100);

                assertSql(builder1, "limitIf (true)",
                                "SELECT id, name FROM users LIMIT 100");

                needLimit = false;
                SelectSqlBuilder builder2 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .columns("id", "name")
                                .limitIf(needLimit, 100);

                assertSql(builder2, "limitIf (false)",
                                "SELECT id, name FROM users");

                boolean hasOffset = true;
                SelectSqlBuilder builder3 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .columns("id", "name")
                                .limit(10)
                                .offsetIf(hasOffset, 50);

                assertSql(builder3, "offsetIf (true)",
                                "SELECT id, name FROM users LIMIT 10 OFFSET 50");

                boolean enablePaging = true;
                SelectSqlBuilder builder4 = SelectSqlBuilder.create(mysqlDialect, "orders")
                                .columns("id", "amount")
                                .limitPageIf(enablePaging, 2, 20);

                assertSql(builder4, "limitPageIf (true)",
                                "SELECT id, amount FROM orders LIMIT 20 OFFSET 20");

                enablePaging = false;
                SelectSqlBuilder builder5 = SelectSqlBuilder.create(mysqlDialect, "orders")
                                .columns("id", "amount")
                                .limitPageIf(enablePaging, 2, 20);

                assertSql(builder5, "limitPageIf (false)",
                                "SELECT id, amount FROM orders");
        }

        @Test
        @DisplayName("测试skip跳过方法")
        void testSkipMethods() {
                SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .columns("id", "name")
                                .limit(10)
                                .skip(15);

                assertSql(builder1, "skip",
                                "SELECT id, name FROM users LIMIT 10 OFFSET 15");

                boolean shouldSkip = true;
                SelectSqlBuilder builder2 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .columns("id", "name")
                                .limit(10)
                                .skipIf(shouldSkip, 25);

                assertSql(builder2, "skipIf (true)",
                                "SELECT id, name FROM users LIMIT 10 OFFSET 25");

                shouldSkip = false;
                SelectSqlBuilder builder3 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .columns("id", "name")
                                .limit(10)
                                .skipIf(shouldSkip, 25);

                assertSql(builder3, "skipIf (false)",
                                "SELECT id, name FROM users LIMIT 10");
        }

        @Test
        @DisplayName("测试从0开始的分页")
        void testLimitPageZeroBased() {
                SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysqlDialect, "products")
                                .columns("id", "name")
                                .limitPageZeroBased(0, 10);

                assertSql(builder1, "第0页 (0-based)",
                                "SELECT id, name FROM products LIMIT 10");

                SelectSqlBuilder builder2 = SelectSqlBuilder.create(mysqlDialect, "products")
                                .columns("id", "name")
                                .limitPageZeroBased(2, 10);

                assertSql(builder2, "第2页 (0-based)",
                                "SELECT id, name FROM products LIMIT 10 OFFSET 20");

                SelectSqlBuilder builder3 = SelectSqlBuilder.create(mysqlDialect, "products")
                                .columns("id", "name")
                                .limitPageZeroBased(-1, 5);

                assertSql(builder3, "页码<0自动修正",
                                "SELECT id, name FROM products LIMIT 5");
        }

        @Test
        @DisplayName("测试便捷方法和边界情况")
        void testEdgeCases() {
                SelectSqlBuilder builder1 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .columns("id", "name")
                                .limitFirst();

                assertSql(builder1, "limitFirst",
                                "SELECT id, name FROM users LIMIT 1");

                SelectSqlBuilder builder2 = SelectSqlBuilder.create(mysqlDialect, "products")
                                .columns("id", "name", "price")
                                .orderBy("price DESC")
                                .limitTop(5);

                assertSql(builder2, "limitTop",
                                "SELECT id, name, price FROM products ORDER BY price DESC LIMIT 5");

                boolean findOne = true;
                SelectSqlBuilder builder3 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .columns("id", "name")
                                .where("email = ?", "test@example.com")
                                .limitFirstIf(findOne);

                assertSql(builder3, "limitFirstIf (true)",
                                "SELECT id, name FROM users WHERE email = ? LIMIT 1");

                findOne = false;
                SelectSqlBuilder builder4 = SelectSqlBuilder.create(mysqlDialect, "users")
                                .columns("id", "name")
                                .where("email = ?", "test@example.com")
                                .limitFirstIf(findOne);

                assertSql(builder4, "limitFirstIf (false)",
                                "SELECT id, name FROM users WHERE email = ?");

                SelectSqlBuilder builder5 = SelectSqlBuilder.create(postgresDialect, "orders")
                                .columns("id", "customer_id", "amount", "created_at")
                                .where("status = ?", "completed")
                                .orderBy("created_at DESC")
                                .limitPage(2, 25);

                assertSql(builder5, "复杂链式调用",
                                "SELECT id, customer_id, amount, created_at FROM orders WHERE status = ? ORDER BY created_at DESC LIMIT 25 OFFSET 25");
        }
}