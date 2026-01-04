// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.builder.SelectSqlBuilder;
import cn.dinodev.sql.dialect.Dialect;
import cn.dinodev.sql.dialect.PostgreSQLDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

/**
 * WITH 子句基础测试类。
 * 
 * <p>测试 WITH (CTE - Common Table Expression) 的基础功能，包括：
 * <ul>
 *   <li>单个 CTE</li>
 *   <li>多个 CTE</li>
 *   <li>递归 CTE</li>
 *   <li>CTE 与主查询结合</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("WITH子句基础测试")
public class WithClauseTest {

    private Dialect dialect;

    @BeforeEach
    public void setUp() throws Exception {
        dialect = new PostgreSQLDialect(null, new CamelNamingConversition());
    }

    @Test
    @DisplayName("测试单个CTE")
    void testSingleCte() {
        SelectSqlBuilder cte = SelectSqlBuilder.create(dialect, "orders")
                .column("user_id", "SUM(amount) AS total")
                .groupBy("user_id");

        SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "user_totals")
                .with("user_totals", cte)
                .column("user_id", "total")
                .where("total > ?", 1000);

        assertSql(builder, "单个CTE",
                "WITH user_totals AS (\nSELECT user_id, SUM(amount) AS total FROM orders GROUP BY user_id\n)\n" +
                        "SELECT user_id, total FROM user_totals WHERE total > ?");
    }

    @Test
    @DisplayName("测试多个CTE")
    void testMultipleCtes() {
        SelectSqlBuilder cte1 = SelectSqlBuilder.create(dialect, "orders")
                .column("user_id", "COUNT(*) AS order_count")
                .groupBy("user_id");

        SelectSqlBuilder cte2 = SelectSqlBuilder.create(dialect, "payments")
                .column("user_id", "SUM(amount) AS total_paid")
                .groupBy("user_id");

        SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "user_orders", "uo")
                .with("user_orders", cte1)
                .with("user_payments", cte2)
                .column("uo.user_id", "uo.order_count", "up.total_paid")
                .innerJoin("user_payments", "up", "uo.user_id = up.user_id");

        assertSql(builder, "多个CTE",
                "WITH user_orders AS (\nSELECT user_id, COUNT(*) AS order_count FROM orders GROUP BY user_id\n),\n" +
                        "user_payments AS (\nSELECT user_id, SUM(amount) AS total_paid FROM payments GROUP BY user_id\n)\n"
                        +
                        "SELECT uo.user_id, uo.order_count, up.total_paid FROM user_orders AS uo " +
                        "JOIN user_payments AS up ON uo.user_id = up.user_id");
    }

    @Test
    @DisplayName("测试递归CTE")
    void testRecursiveCte() {
        // 递归查询示例：组织层级结构
        // 注：此处仅演示递归CTE的基本结构，实际使用中递归逻辑需要在SQL中定义
        SelectSqlBuilder recursiveBuilder = SelectSqlBuilder.create(dialect, "departments")
                .column("id", "name", "parent_id", "1 AS level")
                .where("parent_id IS NULL");

        SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "org_hierarchy")
                .withRecursive("org_hierarchy", recursiveBuilder)
                .column("*");

        // 验证递归CTE结构
        assertSql(builder, "递归CTE",
                "WITH RECURSIVE org_hierarchy AS (\nSELECT id, name, parent_id, 1 AS level FROM departments WHERE parent_id IS NULL\n)\n"
                        +
                        "SELECT * FROM org_hierarchy");
    }
}
