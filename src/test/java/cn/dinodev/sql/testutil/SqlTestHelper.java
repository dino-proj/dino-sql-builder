// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.testutil;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cn.dinodev.sql.SqlBuilder;

/**
 * SQL 测试辅助工具类。
 * 提供统一的 SQL 验证方法，用于测试 SQL 构建器生成的 SQL 语句和参数。
 * 
 * <p>使用示例：
 * <pre>{@code
 * SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "users")
 *     .column("id", "name")
 *     .eq("status", 1);
 * 
 * assertSqlWithParams(builder, "基本查询",
 *     "SELECT id, name FROM users WHERE status = ?",
 *     new Object[]{1});
 * }</pre>
 * 
 * @author Cody Lu
 * @since 2024-12-31
 */
public final class SqlTestHelper {

  /**
   * 验证 SQL 语句（不包含参数）。
   * 
   * @param builder SQL 构建器
   * @param title 测试标题，用于输出说明
   * @param expectedSql 期望的 SQL 语句
   */
  public static void assertSql(SqlBuilder builder, String title, String expectedSql) {
    String sql = builder.getSql();
    System.out.println(title + " SQL:");
    System.out.println(sql);
    System.out.println();
    assertEquals(expectedSql, sql, "生成的SQL应该完全匹配预期");
  }

  /**
   * 验证 SQL 语句和参数。
   * 
   * @param builder SQL 构建器
   * @param title 测试标题，用于输出说明
   * @param expectedSql 期望的 SQL 语句
   * @param expectedParams 期望的参数数组
   */
  public static void assertSqlWithParams(SqlBuilder builder, String title, String expectedSql,
      Object[] expectedParams) {
    String sql = builder.getSql();
    Object[] params = builder.getParams();
    System.out.println(title + " SQL:");
    System.out.println(sql);
    System.out.println("Params: " + java.util.Arrays.toString(params));
    System.out.println();
    assertEquals(expectedSql, sql, "生成的SQL应该完全匹配预期");
    assertArrayEquals(expectedParams, params, "参数应该匹配");
  }

  private SqlTestHelper() {
    // 工具类，禁止实例化
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }
}
