// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder.clause.wheres;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSqlWithParams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.Logic;
import cn.dinodev.sql.builder.SelectSqlBuilder;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

/**
 * 子查询 WHERE 子句测试类。
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("SubQueryWhereClause 测试")
public class SubQueryWhereClauseTest {

    private MysqlDialect mysql;

    @BeforeEach
    public void setUp() {
        mysql = new MysqlDialect(null, new CamelNamingConversition());
    }

    @Test
    @DisplayName("any - 基本用法")
    void testAny() {
        SelectSqlBuilder subQuery = SelectSqlBuilder.create(mysql, "orders")
                .column("user_id")
                .eq("status", "completed");

        SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
                .column("*")
                .any("id", subQuery);

        assertSqlWithParams(builder, "any",
                "SELECT * FROM users WHERE id = any(SELECT user_id FROM orders WHERE status = ?)",
                new Object[] { "completed" });
    }

    @Test
    @DisplayName("any - 带逻辑符")
    void testAnyWithLogic() {
        SelectSqlBuilder subQuery = SelectSqlBuilder.create(mysql, "orders")
                .column("user_id")
                .gt("amount", 1000);

        SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
                .column("*")
                .eq("status", 1)
                .any("id", subQuery, Logic.OR);

        assertSqlWithParams(builder, "any with logic",
                "SELECT * FROM users WHERE status = ? OR (id = any(SELECT user_id FROM orders WHERE amount > ?))",
                new Object[] { 1, 1000 });
    }

    @Test
    @DisplayName("exists - 字符串子查询")
    void testExists() {
        SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
                .column("*")
                .exists("SELECT 1 FROM orders WHERE orders.user_id = users.id AND status = 'completed'");

        assertSqlWithParams(builder, "exists",
                "SELECT * FROM users WHERE EXISTS (SELECT 1 FROM orders WHERE orders.user_id = users.id AND status = 'completed')",
                new Object[] {});
    }

    @Test
    @DisplayName("exists - 带逻辑符")
    void testExistsWithLogic() {
        SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
                .column("*")
                .eq("status", 1)
                .exists("SELECT 1 FROM orders WHERE orders.user_id = users.id", Logic.OR);

        assertSqlWithParams(builder, "exists with logic",
                "SELECT * FROM users WHERE status = ? OR (EXISTS (SELECT 1 FROM orders WHERE orders.user_id = users.id))",
                new Object[] { 1 });
    }

    @Test
    @DisplayName("notExists - 不存在")
    void testNotExists() {
        SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
                .column("*")
                .notExists("SELECT 1 FROM orders WHERE orders.user_id = users.id");

        assertSqlWithParams(builder, "notExists",
                "SELECT * FROM users WHERE NOT EXISTS (SELECT 1 FROM orders WHERE orders.user_id = users.id)",
                new Object[] {});
    }

    @Test
    @DisplayName("notExists - 带逻辑符")
    void testNotExistsWithLogic() {
        SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
                .column("*")
                .eq("status", 1)
                .notExists("SELECT 1 FROM orders WHERE orders.user_id = users.id", Logic.AND);

        assertSqlWithParams(builder, "notExists with logic",
                "SELECT * FROM users WHERE status = ? AND (NOT EXISTS (SELECT 1 FROM orders WHERE orders.user_id = users.id))",
                new Object[] { 1 });
    }

    @Test
    @DisplayName("组合测试 - 多个子查询条件")
    void testCombined() {
        SelectSqlBuilder subQuery1 = SelectSqlBuilder.create(mysql, "orders")
                .column("user_id")
                .eq("status", "completed");

        SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
                .column("*")
                .eq("status", 1)
                .any("id", subQuery1)
                .exists("SELECT 1 FROM user_roles WHERE user_roles.user_id = users.id AND role = 'admin'");

        assertSqlWithParams(builder, "combined",
                "SELECT * FROM users WHERE status = ? AND (id = any(SELECT user_id FROM orders WHERE status = ?)) AND (EXISTS (SELECT 1 FROM user_roles WHERE user_roles.user_id = users.id AND role = 'admin'))",
                new Object[] { 1, "completed" });
    }

    @Test
    @DisplayName("实际场景 - 查找有订单的用户")
    void testUsersWithOrders() {
        SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
                .columns("id", "name", "email")
                .eq("status", 1)
                .exists("SELECT 1 FROM orders WHERE orders.user_id = users.id AND orders.status = 'completed'");

        assertSqlWithParams(builder, "users with orders",
                "SELECT id, name, email FROM users WHERE status = ? AND (EXISTS (SELECT 1 FROM orders WHERE orders.user_id = users.id AND orders.status = 'completed'))",
                new Object[] { 1 });
    }

    @Test
    @DisplayName("实际场景 - 查找没有订单的用户")
    void testUsersWithoutOrders() {
        SelectSqlBuilder builder = SelectSqlBuilder.create(mysql, "users")
                .columns("id", "name", "email")
                .eq("status", 1)
                .notExists("SELECT 1 FROM orders WHERE orders.user_id = users.id");

        assertSqlWithParams(builder, "users without orders",
                "SELECT id, name, email FROM users WHERE status = ? AND (NOT EXISTS (SELECT 1 FROM orders WHERE orders.user_id = users.id))",
                new Object[] { 1 });
    }
}
