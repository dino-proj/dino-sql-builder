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
 * UNION 子句测试类。
 * 
 * <p>测试 UNION 相关功能，包括：
 * <ul>
 *   <li>UNION</li>
 *   <li>UNION ALL</li>
 *   <li>多个 UNION</li>
 *   <li>UNION 与 ORDER BY</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2026-01-04
 */
@DisplayName("UNION子句测试")
public class UnionClauseTest {

    private Dialect dialect;

    @BeforeEach
    public void setUp() throws Exception {
        dialect = new PostgreSQLDialect(null, new CamelNamingConversition());
    }

    @Test
    @DisplayName("测试基础UNION")
    void testBasicUnion() {
        SelectSqlBuilder builder1 = SelectSqlBuilder.create(dialect, "users")
                .column("id", "name")
                .where("age > ?", 18);

        SelectSqlBuilder builder2 = SelectSqlBuilder.create(dialect, "customers")
                .column("id", "name")
                .where("status = ?", 1);

        builder1.union(builder2);

        assertSql(builder1, "基础UNION",
                "SELECT id, name FROM users WHERE age > ?\nUNION\nSELECT id, name FROM customers WHERE status = ?");
    }

    @Test
    @DisplayName("测试UNION ALL")
    void testUnionAll() {
        SelectSqlBuilder builder1 = SelectSqlBuilder.create(dialect, "users")
                .column("id", "name");

        SelectSqlBuilder builder2 = SelectSqlBuilder.create(dialect, "customers")
                .column("id", "name");

        builder1.unionAll(builder2);

        assertSql(builder1, "UNION ALL",
                "SELECT id, name FROM users\nUNION ALL\nSELECT id, name FROM customers");
    }

    @Test
    @DisplayName("测试多个UNION")
    void testMultipleUnions() {
        SelectSqlBuilder builder1 = SelectSqlBuilder.create(dialect, "users")
                .column("id", "name");

        SelectSqlBuilder builder2 = SelectSqlBuilder.create(dialect, "customers")
                .column("id", "name");

        SelectSqlBuilder builder3 = SelectSqlBuilder.create(dialect, "vendors")
                .column("id", "name");

        builder1.union(builder2).union(builder3);

        assertSql(builder1, "多个UNION",
                "SELECT id, name FROM users\nUNION\nSELECT id, name FROM customers\nUNION\nSELECT id, name FROM vendors");
    }

    @Test
    @DisplayName("测试UNION与ORDER BY")
    void testUnionWithOrderBy() {
        SelectSqlBuilder builder1 = SelectSqlBuilder.create(dialect, "users")
                .column("id", "name");

        SelectSqlBuilder builder2 = SelectSqlBuilder.create(dialect, "customers")
                .column("id", "name");

        builder1.union(builder2).orderBy("name", true);

        assertSql(builder1, "UNION与ORDER BY",
                "SELECT id, name FROM users\nUNION\nSELECT id, name FROM customers ORDER BY name ASC");
    }
}
