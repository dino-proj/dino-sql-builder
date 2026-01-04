// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.builder;

import static cn.dinodev.sql.testutil.SqlTestHelper.assertSqlWithParams;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.dialect.PostgreSQLDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;
import cn.dinodev.sql.testutil.DatabaseMetaDataMocks;

/**
 * GROUP BY ALL 功能测试类。
 * 
 * <p>测试 PostgreSQL 17+ 版本中的 GROUP BY ALL 特性支持，包括：
 * <ul>
 *   <li>PostgreSQL 17+ 支持 GROUP BY ALL</li>
 *   <li>PostgreSQL 16 及以下版本不支持</li>
 *   <li>MySQL 不支持 GROUP BY ALL</li>
 *   <li>异常处理验证</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2024-12-31
 */
@DisplayName("GROUP BY ALL功能测试")
public class GroupByAllTest {

  private MysqlDialect mysqlDialect;

  @BeforeEach
  public void setUp() {
    mysqlDialect = new MysqlDialect(null, new CamelNamingConversition());
  }

  @Test
  @DisplayName("测试方言工厂方法和版本特性支持")
  void testDialectFactory() {
    try {
      PostgreSQLDialect pg17 = new PostgreSQLDialect(
          DatabaseMetaDataMocks.createPostgreSQL(17),
          new CamelNamingConversition());
      PostgreSQLDialect pg16 = new PostgreSQLDialect(
          DatabaseMetaDataMocks.createPostgreSQL(16),
          new CamelNamingConversition());
      PostgreSQLDialect pg15 = new PostgreSQLDialect(
          DatabaseMetaDataMocks.createPostgreSQL(15),
          new CamelNamingConversition());
      PostgreSQLDialect pg14 = new PostgreSQLDialect(
          DatabaseMetaDataMocks.createPostgreSQL(14),
          new CamelNamingConversition());

      assertNotNull(pg17);
      assertTrue(pg17.supportsGroupByAll(), "PostgreSQL 17应支持GROUP BY ALL");

      assertNotNull(pg16);
      assertFalse(pg16.supportsGroupByAll(), "PostgreSQL 16不应支持GROUP BY ALL");

      assertNotNull(pg15);
      assertFalse(pg15.supportsGroupByAll(), "PostgreSQL 15不应支持GROUP BY ALL");

      assertNotNull(pg14);
      assertFalse(pg14.supportsGroupByAll(), "PostgreSQL 14不应支持GROUP BY ALL");

      assertFalse(mysqlDialect.supportsGroupByAll(), "MySQL不应支持GROUP BY ALL");
    } catch (Exception e) {
      fail("创建方言失败: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("测试PostgreSQL 17使用GROUP BY ALL")
  void testPostgreSQL17GroupByAll() {
    try {
      PostgreSQLDialect pg17 = new PostgreSQLDialect(
          DatabaseMetaDataMocks.createPostgreSQL(17),
          new CamelNamingConversition());
      SelectSqlBuilder builder = SelectSqlBuilder.create(pg17, "sales")
          .column("region", "product", "COUNT(*) AS count", "SUM(amount) AS total")
          .gt("amount", 100)
          .groupByAll()
          .orderByDesc("total");

      assertSqlWithParams(builder, "PostgreSQL 17 GROUP BY ALL",
          "SELECT region, product, COUNT(*) AS count, SUM(amount) AS total FROM sales WHERE amount > ? GROUP BY ALL ORDER BY total DESC",
          new Object[] { 100 });
    } catch (Exception e) {
      fail("测试失败: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("测试PostgreSQL 14使用GROUP BY ALL应抛出异常")
  void testPostgreSQL14GroupByAllException() {
    assertThrows(UnsupportedOperationException.class, () -> {
      PostgreSQLDialect pg14 = new PostgreSQLDialect(
          DatabaseMetaDataMocks.createPostgreSQL(14),
          new CamelNamingConversition());
      SelectSqlBuilder.create(pg14, "sales")
          .column("region", "product", "COUNT(*) AS count")
          .groupByAll();
    });
  }

  @Test
  @DisplayName("测试MySQL使用GROUP BY ALL应抛出异常")
  void testMySQLGroupByAllException() {
    assertThrows(UnsupportedOperationException.class, () -> {
      SelectSqlBuilder.create(mysqlDialect, "sales")
          .column("region", "product", "COUNT(*) AS count")
          .groupByAll();
    });
  }
}
