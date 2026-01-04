// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSql;
import static cn.dinodev.sql.testutil.SqlTestHelper.assertSqlWithParams;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.dialect.Dialect;
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.dialect.PostgreSQLDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;
import cn.dinodev.sql.testutil.DatabaseMetaDataMocks;

/**
 * InsertSqlBuilder 测试类。
 *
 * @author Cody Lu
 * @since 2025-01-03
 */
@DisplayName("InsertSqlBuilder 测试")
class InsertSqlBuilderTest {

    private Dialect mysqlDialect;
    private Dialect postgresDialect;

    @BeforeEach
    void setUp() throws Exception {
        mysqlDialect = new MysqlDialect(DatabaseMetaDataMocks.mysqlV8, new CamelNamingConversition());
        postgresDialect = new PostgreSQLDialect(DatabaseMetaDataMocks.postgresV15, new CamelNamingConversition());
    }

    @Test
    @DisplayName("基础插入 - 单列")
    void testBasicInsertSingleColumn() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三");

        assertSqlWithParams(builder, "基础插入-单列",
                "INSERT INTO users (name) VALUES (?)",
                new Object[] { "张三" });
    }

    @Test
    @DisplayName("基础插入 - 多列")
    void testBasicInsertMultipleColumns() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .set("age", 25)
                .set("email", "zhangsan@example.com");

        assertSqlWithParams(builder, "基础插入-多列",
                "INSERT INTO users (name, age, email) VALUES (?, ?, ?)",
                new Object[] { "张三", 25, "zhangsan@example.com" });
    }

    @Test
    @DisplayName("插入 - 使用自定义表达式")
    void testInsertWithCustomExpression() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .set("status", "UPPER(?)", "active");

        assertSqlWithParams(builder, "自定义表达式",
                "INSERT INTO users (name, status) VALUES (?, UPPER(?))",
                new Object[] { "张三", "active" });
    }

    @Test
    @DisplayName("插入 - 不带参数的表达式")
    void testInsertWithoutParam() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .setExpression("created_at", "NOW()")
                .setExpression("status", "DEFAULT");

        assertSqlWithParams(builder, "不带参数的表达式",
                "INSERT INTO users (name, created_at, status) VALUES (?, NOW(), DEFAULT)",
                new Object[] { "张三" });
    }

    @Test
    @DisplayName("插入 - 使用 InsertClause 接口方法")
    void testInsertWithInterfaceMethods() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .setNow("created_at")
                .setNull("deleted_at")
                .setDefault("status");

        assertSqlWithParams(builder, "InsertClause接口方法",
                "INSERT INTO users (name, created_at, deleted_at, status) VALUES (?, NOW(), NULL, DEFAULT)",
                new Object[] { "张三" });
    }

    @Test
    @DisplayName("插入 - 条件插入（true）")
    void testInsertIfTrue() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .setIf(true, "age", 25)
                .setIf(true, "email", "zhangsan@example.com");

        assertSqlWithParams(builder, "条件插入-true",
                "INSERT INTO users (name, age, email) VALUES (?, ?, ?)",
                new Object[] { "张三", 25, "zhangsan@example.com" });
    }

    @Test
    @DisplayName("插入 - 条件插入（false）")
    void testInsertIfFalse() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .setIf(false, "age", 25)
                .setIf(false, "email", "zhangsan@example.com");

        assertSqlWithParams(builder, "条件插入-false",
                "INSERT INTO users (name) VALUES (?)",
                new Object[] { "张三" });
    }

    @Test
    @DisplayName("插入 - 当值不为null时插入")
    void testInsertIfNotNull() {
        String email = null;

        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .setIfNotNull("age", 25)
                .setIfNotNull("email", email);

        assertSqlWithParams(builder, "非空值插入",
                "INSERT INTO users (name, age) VALUES (?, ?)",
                new Object[] { "张三", 25 });
    }

    @Test
    @DisplayName("插入 - JSON 类型（PostgreSQL）")
    void testInsertJson() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "users")
                .set("name", "张三")
                .setJson("settings", "{\"theme\":\"dark\"}");

        assertSqlWithParams(builder, "JSON类型",
                "INSERT INTO users (name, settings) VALUES (?, ?::json)",
                new Object[] { "张三", "{\"theme\":\"dark\"}" });
    }

    @Test
    @DisplayName("插入 - JSONB 类型（PostgreSQL）")
    void testInsertJsonb() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "users")
                .set("name", "张三")
                .setJsonb("settings", "{\"theme\":\"dark\"}");

        assertSqlWithParams(builder, "JSONB类型",
                "INSERT INTO users (name, settings) VALUES (?, ?::jsonb)",
                new Object[] { "张三", "{\"theme\":\"dark\"}" });
    }

    @Test
    @DisplayName("插入 - 条件JSON插入")
    void testInsertJsonIf() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "users")
                .set("name", "张三")
                .setJsonIf(true, "settings", "{\"theme\":\"dark\"}")
                .setJsonbIf(false, "preferences", "{\"lang\":\"zh\"}");

        assertSqlWithParams(builder, "条件JSON插入",
                "INSERT INTO users (name, settings) VALUES (?, ?::json)",
                new Object[] { "张三", "{\"theme\":\"dark\"}" });
    }

    @Test
    @DisplayName("插入 - JSON非空插入")
    void testInsertJsonIfNotNull() {
        String settings = "{\"theme\":\"dark\"}";
        String preferences = null;

        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "users")
                .set("name", "张三")
                .setJsonIfNotNull("settings", settings)
                .setJsonbIfNotNull("preferences", preferences);

        assertSqlWithParams(builder, "JSON非空插入",
                "INSERT INTO users (name, settings) VALUES (?, ?::json)",
                new Object[] { "张三", "{\"theme\":\"dark\"}" });
    }

    @Test
    @DisplayName("插入 - 带自定义表达式的条件插入")
    void testInsertWithExpressionIf() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .setIf(true, "email", "LOWER(?)", "ZHANGSAN@EXAMPLE.COM")
                .setIf(false, "phone", "CONCAT(?, ?)", "123");

        assertSqlWithParams(builder, "表达式条件插入",
                "INSERT INTO users (name, email) VALUES (?, LOWER(?))",
                new Object[] { "张三", "ZHANGSAN@EXAMPLE.COM" });
    }

    @Test
    @DisplayName("插入 - 条件withoutParam")
    void testInsertWithoutParamIf() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .setExpressionIf(true, "created_at", "NOW()")
                .setExpressionIf(false, "updated_at", "NOW()");

        assertSqlWithParams(builder, "条件withoutParam",
                "INSERT INTO users (name, created_at) VALUES (?, NOW())",
                new Object[] { "张三" });
    }

    @Test
    @DisplayName("插入 - 复杂场景")
    void testComplexInsert() {
        String preferences = "{\"notifications\":true}";

        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "users")
                .set("name", "张三")
                .set("age", 25)
                .setIfNotNull("email", "zhangsan@example.com")
                .setJsonbIfNotNull("preferences", preferences)
                .setNow("created_at")
                .setNull("deleted_at");

        assertSqlWithParams(builder, "复杂场景",
                "INSERT INTO users (name, age, email, preferences, created_at, deleted_at) " +
                        "VALUES (?, ?, ?, ?::jsonb, NOW(), NULL)",
                new Object[] { "张三", 25, "zhangsan@example.com", "{\"notifications\":true}" });
    }

    @Test
    @DisplayName("验证 getParamCount 方法")
    void testGetParamCount() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .set("age", 25)
                .setNow("created_at");

        assertEquals(2, builder.getParamCount(), "参数数量应该为2");
    }

    @Test
    @DisplayName("验证 toString 方法")
    void testToString() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三");

        String sql = builder.toString();
        assertSql(builder, "toString方法", sql);
    }

    @Test
    @DisplayName("插入 - 生成UUID (PostgreSQL 18+)")
    void testSetGenUuidPostgreSQL18() throws Exception {
        // 使用 PostgreSQL 18 方言
        Dialect postgres18Dialect = new PostgreSQLDialect(
                DatabaseMetaDataMocks.postgresV18, new CamelNamingConversition());

        InsertSqlBuilder builder = InsertSqlBuilder.create(postgres18Dialect, "users")
                .set("name", "张三")
                .setGenUuid("id");

        assertSqlWithParams(builder, "生成UUID-PostgreSQL 18",
                "INSERT INTO users (name, id) VALUES (?, uuidv7())",
                new Object[] { "张三" });
    }

    @Test
    @DisplayName("插入 - 空列（边界情况）")
    void testInsertWithNoColumns() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users");

        assertSqlWithParams(builder, "空列边界情况",
                "INSERT INTO users",
                new Object[] {});
    }

    @Test
    @DisplayName("插入 - 条件setNow")
    void testSetNowIf() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .setNowIf(true, "created_at")
                .setNowIf(false, "updated_at");

        assertSqlWithParams(builder, "条件setNow",
                "INSERT INTO users (name, created_at) VALUES (?, NOW())",
                new Object[] { "张三" });
    }

    @Test
    @DisplayName("插入 - 条件setNull")
    void testSetNullIf() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .setNullIf(true, "deleted_at")
                .setNullIf(false, "archived_at");

        assertSqlWithParams(builder, "条件setNull",
                "INSERT INTO users (name, deleted_at) VALUES (?, NULL)",
                new Object[] { "张三" });
    }

    @Test
    @DisplayName("插入 - 条件setDefault")
    void testSetDefaultIf() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .setDefaultIf(true, "status")
                .setDefaultIf(false, "role");

        assertSqlWithParams(builder, "条件setDefault",
                "INSERT INTO users (name, status) VALUES (?, DEFAULT)",
                new Object[] { "张三" });
    }

    @Test
    @DisplayName("插入 - 批量设置Map")
    void testSetMap() {
        java.util.Map<String, Object> values = new java.util.HashMap<>();
        values.put("name", "张三");
        values.put("age", 25);
        values.put("email", "zhangsan@example.com");

        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .setMap(values);

        // Map的遍历顺序不确定，只验证参数个数
        assertEquals(3, builder.getParamCount(), "参数数量应该为3");
    }

    @Test
    @DisplayName("插入 - 条件批量设置Map")
    void testSetMapIf() {
        java.util.Map<String, Object> values1 = new java.util.HashMap<>();
        values1.put("name", "张三");
        values1.put("age", 25);

        java.util.Map<String, Object> values2 = new java.util.HashMap<>();
        values2.put("email", "zhangsan@example.com");

        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .setMapIf(true, values1)
                .setMapIf(false, values2);

        assertEquals(2, builder.getParamCount(), "参数数量应该为2");
    }

    @Test
    @DisplayName("插入 - 批量设置Map（仅非空值）")
    void testSetMapIfNotNull() {
        java.util.Map<String, Object> values = new java.util.HashMap<>();
        values.put("name", "张三");
        values.put("age", 25);
        values.put("email", null);

        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .setMapIfNotNull(values);

        assertEquals(2, builder.getParamCount(), "参数数量应该为2（排除null值）");
    }

    @Test
    @DisplayName("插入 - 数组类型（PostgreSQL）")
    void testSetArray() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "users")
                .set("name", "张三")
                .setArray("tags", new int[] { 1, 2, 3 });

        assertSqlWithParams(builder, "数组类型",
                "INSERT INTO users (name, tags) VALUES (?, ARRAY[?, ?, ?])",
                new Object[] { "张三", 1, 2, 3 });
    }

    @Test
    @DisplayName("插入 - 条件数组")
    void testSetArrayIf() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "users")
                .set("name", "张三")
                .setArrayIf(true, "tags", new int[] { 1, 2, 3 })
                .setArrayIf(false, "roles", new String[] { "admin" });

        assertSqlWithParams(builder, "条件数组",
                "INSERT INTO users (name, tags) VALUES (?, ARRAY[?, ?, ?])",
                new Object[] { "张三", 1, 2, 3 });
    }

    @Test
    @DisplayName("插入 - 数组非空")
    void testSetArrayIfNotNull() {
        int[] tags = new int[] { 1, 2, 3 };
        String[] roles = null;

        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "users")
                .set("name", "张三")
                .setArrayIfNotNull("tags", tags)
                .setArrayIfNotNull("roles", roles);

        assertSqlWithParams(builder, "数组非空",
                "INSERT INTO users (name, tags) VALUES (?, ARRAY[?, ?, ?])",
                new Object[] { "张三", 1, 2, 3 });
    }

    @Test
    @DisplayName("插入 - 数组类型使用List（PostgreSQL）")
    void testSetArrayWithList() {
        java.util.List<Integer> tagList = java.util.Arrays.asList(1, 2, 3);

        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "users")
                .set("name", "张三")
                .setArray("tags", tagList);

        assertSqlWithParams(builder, "数组类型-List",
                "INSERT INTO users (name, tags) VALUES (?, ARRAY[?, ?, ?])",
                new Object[] { "张三", 1, 2, 3 });
    }

    @Test
    @DisplayName("插入 - 数组类型使用Set（PostgreSQL）")
    void testSetArrayWithSet() {
        java.util.Set<String> roles = new java.util.LinkedHashSet<>();
        roles.add("admin");
        roles.add("developer");

        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "users")
                .set("name", "张三")
                .setArray("roles", roles);

        assertSqlWithParams(builder, "数组类型-Set",
                "INSERT INTO users (name, roles) VALUES (?, ARRAY[?, ?])",
                new Object[] { "张三", "admin", "developer" });
    }

    @Test
    @DisplayName("插入 - 空List处理")
    void testSetArrayWithEmptyList() {
        java.util.List<Integer> emptyList = java.util.Collections.emptyList();

        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "users")
                .set("name", "张三")
                .setArray("tags", emptyList);

        assertSqlWithParams(builder, "空List处理",
                "INSERT INTO users (name, tags) VALUES (?, ARRAY[])",
                new Object[] { "张三" });
    }

    @Test
    @DisplayName("插入 - BYTEA类型（PostgreSQL）")
    void testSetBytea() {
        byte[] data = new byte[] { 0x01, 0x02, 0x03 };

        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "files")
                .set("name", "test.bin")
                .setBytea("content", data);

        assertSqlWithParams(builder, "BYTEA类型",
                "INSERT INTO files (name, content) VALUES (?, ?::bytea)",
                new Object[] { "test.bin", data });
    }

    @Test
    @DisplayName("插入 - 条件BYTEA")
    void testSetByteaIf() {
        byte[] data = new byte[] { 0x01, 0x02, 0x03 };

        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "files")
                .set("name", "test.bin")
                .setByteaIf(true, "content", data)
                .setByteaIf(false, "thumbnail", new byte[] { 0x04 });

        assertSqlWithParams(builder, "条件BYTEA",
                "INSERT INTO files (name, content) VALUES (?, ?::bytea)",
                new Object[] { "test.bin", data });
    }

    @Test
    @DisplayName("插入 - BYTEA非空")
    void testSetByteaIfNotNull() {
        byte[] content = new byte[] { 0x01, 0x02, 0x03 };
        byte[] thumbnail = null;

        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "files")
                .set("name", "test.bin")
                .setByteaIfNotNull("content", content)
                .setByteaIfNotNull("thumbnail", thumbnail);

        assertSqlWithParams(builder, "BYTEA非空",
                "INSERT INTO files (name, content) VALUES (?, ?::bytea)",
                new Object[] { "test.bin", content });
    }

    @Test
    @DisplayName("插入 - 生成UUID（PostgreSQL 方言）")
    void testSetGenUuidPostgres() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "users")
                .set("name", "张三")
                .setGenUuid("id");

        assertSqlWithParams(builder, "生成UUID-PostgreSQL",
                "INSERT INTO users (name, id) VALUES (?, gen_random_uuid())",
                new Object[] { "张三" });
    }

    @Test
    @DisplayName("插入 - 条件生成UUID（PostgreSQL 方言）")
    void testSetGenUuidIfPostgres() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "users")
                .set("name", "张三")
                .setGenUuidIf(true, "id")
                .setGenUuidIf(false, "external_id");

        assertSqlWithParams(builder, "条件生成UUID-PostgreSQL",
                "INSERT INTO users (name, id) VALUES (?, gen_random_uuid())",
                new Object[] { "张三" });
    }

    @Test
    @DisplayName("插入 - 生成UUID（MySQL 方言）")
    void testSetGenUuidMysql() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .setGenUuid("id");

        assertSqlWithParams(builder, "生成UUID-MySQL",
                "INSERT INTO users (name, id) VALUES (?, UUID())",
                new Object[] { "张三" });
    }

    @Test
    @DisplayName("插入 - 条件生成UUID（MySQL 方言）")
    void testSetGenUuidIfMysql() {
        InsertSqlBuilder builder = InsertSqlBuilder.create(mysqlDialect, "users")
                .set("name", "张三")
                .setGenUuidIf(true, "id")
                .setGenUuidIf(false, "external_id");

        assertSqlWithParams(builder, "条件生成UUID-MySQL",
                "INSERT INTO users (name, id) VALUES (?, UUID())",
                new Object[] { "张三" });
    }

    @Test
    @DisplayName("插入 - 综合测试：各类新增方法")
    void testComprehensiveNewFeatures() {
        String[] tags = new String[] { "developer", "java" };

        InsertSqlBuilder builder = InsertSqlBuilder.create(postgresDialect, "users")
                .set("name", "张三")
                .set("age", 25)
                .setJsonb("preferences", "{\"theme\":\"dark\"}")
                .setArray("tags", tags)
                .setNowIf(true, "created_at")
                .setNullIf(true, "deleted_at")
                .setDefaultIf(false, "status");

        assertSqlWithParams(builder, "综合测试",
                "INSERT INTO users (name, age, preferences, tags, created_at, deleted_at) " +
                        "VALUES (?, ?, ?::jsonb, ARRAY[?, ?], NOW(), NULL)",
                new Object[] { "张三", 25,
                        "{\"theme\":\"dark\"}", "developer", "java" });
    }
}
