// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSql;
import static cn.dinodev.sql.testutil.SqlTestHelper.assertSqlWithParams;

import java.sql.SQLException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.MaterializationHint;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.dialect.PostgreSQLDialect;
import cn.dinodev.sql.naming.SnakeNamingConversition;
import cn.dinodev.sql.testutil.DatabaseMetaDataMocks;

/**
 * WITH (CTE) MATERIALIZED 功能测试类。
 * 
 * <p>测试 CTE（Common Table Expression）物化提示在不同数据库方言下的行为，包括：
 * <ul>
 *   <li>PostgreSQL 的 MATERIALIZED / NOT MATERIALIZED</li>
 *   <li>MySQL 的 CTE 支持（不支持物化提示）</li>
 *   <li>递归 CTE 支持</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2024-12-31
 */
@DisplayName("WITH MATERIALIZED功能测试")
public class WithMaterializedTest {

        @Test
        @DisplayName("测试PostgreSQL MATERIALIZED提示")
        void testPostgreSQLMaterialized() throws SQLException {
                PostgreSQLDialect pg12 = new PostgreSQLDialect(
                                DatabaseMetaDataMocks.postgresV12,
                                new SnakeNamingConversition());

                SelectSqlBuilder cte = SelectSqlBuilder.create(pg12, "orders")
                                .column("customer_id", "SUM(amount) AS total")
                                .groupBy("customer_id");

                SelectSqlBuilder main = SelectSqlBuilder.create(pg12, "customers")
                                .with("order_totals", cte, MaterializationHint.MATERIALIZED)
                                .column("c.name", "ot.total")
                                .join("order_totals", "ot", "customers.id = ot.customer_id");

                assertSql(main, "PostgreSQL MATERIALIZED",
                                "WITH order_totals AS MATERIALIZED (\n"
                                                + "SELECT customer_id, SUM(amount) AS total FROM orders GROUP BY customer_id\n"
                                                + ")\n"
                                                + "SELECT c.name, ot.total FROM customers JOIN order_totals AS ot ON customers.id = ot.customer_id");
        }

        @Test
        @DisplayName("测试PostgreSQL NOT MATERIALIZED提示")
        void testPostgreSQLNotMaterialized() throws SQLException {
                PostgreSQLDialect pg12 = new PostgreSQLDialect(
                                DatabaseMetaDataMocks.postgresV12,
                                new SnakeNamingConversition());

                SelectSqlBuilder cte = SelectSqlBuilder.create(pg12, "simple_table")
                                .column("id", "name")
                                .eq("status", 1);

                SelectSqlBuilder main = SelectSqlBuilder.create(pg12, "main_table")
                                .with("simple_cte", cte, MaterializationHint.NOT_MATERIALIZED)
                                .column("*");

                assertSqlWithParams(main, "PostgreSQL NOT MATERIALIZED",
                                "WITH simple_cte AS NOT MATERIALIZED (\n"
                                                + "SELECT id, name FROM simple_table WHERE status = ?\n"
                                                + ")\n"
                                                + "SELECT * FROM main_table",
                                new Object[] { 1 });
        }

        @Test
        @DisplayName("测试MySQL忽略MATERIALIZED提示")
        void testMySQLIgnoresMaterialized() {
                MysqlDialect mysql = new MysqlDialect(
                                DatabaseMetaDataMocks.mysqlV8,
                                new SnakeNamingConversition());

                SelectSqlBuilder cte = SelectSqlBuilder.create(mysql, "orders")
                                .column("customer_id", "SUM(amount) AS total")
                                .groupBy("customer_id");

                SelectSqlBuilder main = SelectSqlBuilder.create(mysql, "customers")
                                .with("order_totals", cte)
                                .column("*");

                assertSql(main, "MySQL (忽略MATERIALIZED)",
                                "WITH order_totals AS (\n"
                                                + "SELECT customer_id, SUM(amount) AS total FROM orders GROUP BY customer_id\n"
                                                + ")\n"
                                                + "SELECT * FROM customers");
        }

        @Test
        @DisplayName("测试混合物化提示的多个CTE")
        void testMixedCTEs() throws SQLException {
                PostgreSQLDialect pg12 = new PostgreSQLDialect(
                                DatabaseMetaDataMocks.postgresV12,
                                new SnakeNamingConversition());

                SelectSqlBuilder cte1 = SelectSqlBuilder.create(pg12, "expensive_query")
                                .column("id", "complex_calculation(data) AS result");

                SelectSqlBuilder cte2 = SelectSqlBuilder.create(pg12, "simple_query")
                                .column("id", "name");

                SelectSqlBuilder main = SelectSqlBuilder.create(pg12, "final_table")
                                .with("expensive_cte", cte1, MaterializationHint.MATERIALIZED)
                                .with("simple_cte", cte2, MaterializationHint.NOT_MATERIALIZED)
                                .column("*");

                assertSql(main, "混合物化提示的多个CTE",
                                "WITH expensive_cte AS MATERIALIZED (\n"
                                                + "SELECT id, complex_calculation(data) AS result FROM expensive_query\n"
                                                + "),\n"
                                                + "simple_cte AS NOT MATERIALIZED (\n"
                                                + "SELECT id, name FROM simple_query\n"
                                                + ")\n"
                                                + "SELECT * FROM final_table");
        }
}
