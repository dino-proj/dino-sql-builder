// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.naming;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 蛇形命名转换器测试类。
 * 
 * <p>测试 {@link SnakeNamingConversition} 的命名转换功能，包括：
 * <ul>
 *   <li>基本驼峰转下划线转换</li>
 *   <li>表名转换</li>
 *   <li>边界情况处理（空值、特殊字符等）</li>
 *   <li>缓存功能验证</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2024-01-03
 */
@DisplayName("蛇形命名转换器测试")
public class SnakeNamingConversitionTest {

  private SnakeNamingConversition converter;

  @BeforeEach
  public void setUp() {
    converter = new SnakeNamingConversition();
  }

  /**
   * 测试基本的驼峰转下划线转换。
   */
  @Test
  @DisplayName("基本驼峰转下划线转换")
  void testBasicConversion() {
    assertEquals("user_name", converter.convertColumnName("userName"),
        "userName 应该转换为 user_name");
    assertEquals("user_id", converter.convertColumnName("userId"),
        "userId 应该转换为 user_id");
    assertEquals("created_at", converter.convertColumnName("createdAt"),
        "createdAt 应该转换为 created_at");
  }

  /**
   * 测试 PascalCase（首字母大写）转换。
   */
  @Test
  @DisplayName("PascalCase转换")
  void testPascalCase() {
    assertEquals("user_name", converter.convertColumnName("UserName"),
        "UserName 应该转换为 user_name");
    assertEquals("order_detail", converter.convertColumnName("OrderDetail"),
        "OrderDetail 应该转换为 order_detail");
  }

  /**
   * 测试单个单词的转换。
   */
  @Test
  @DisplayName("单个单词转换")
  void testSingleWord() {
    assertEquals("user", converter.convertColumnName("user"),
        "单个单词 user 应该保持为 user");
    assertEquals("user", converter.convertColumnName("User"),
        "单个大写单词 User 应该转换为 user");
  }

  /**
   * 测试全大写字段名的转换。
   */
  @Test
  @DisplayName("全大写字段名转换")
  void testAllUpperCase() {
    assertEquals("user", converter.convertColumnName("USER"),
        "USER 应该转换为 user");
    assertEquals("user_name", converter.convertColumnName("USER_NAME"),
        "USER_NAME 应该转换为 user_name");
  }

  /**
   * 测试已经是蛇形格式的输入。
   */
  @Test
  @DisplayName("已是蛇形格式的输入")
  void testAlreadySnakeCase() {
    assertEquals("user_name", converter.convertColumnName("user_name"),
        "已经是蛇形格式的输入应该保持不变");
    assertEquals("user_id", converter.convertColumnName("user_id"),
        "已经是蛇形格式的输入应该保持不变");
  }

  /**
   * 测试以下划线开头的字段名。
   */
  @Test
  @DisplayName("以下划线开头的字段名转换")
  void testLeadingUnderscore() {
    assertEquals("user", converter.convertColumnName("_user"),
        "_user 应该转换为 user");
    assertEquals("user", converter.convertColumnName("_User"),
        "_User 应该转换为 user");
    assertEquals("_user", converter.convertColumnName("__user"),
        "__user 应该转换为 _user");
  }

  /**
   * 测试包含多个连续下划线的字段名。
   */
  @Test
  @DisplayName("多个连续下划线转换")
  void testMultipleUnderscores() {
    assertEquals("user__name", converter.convertColumnName("user__name"),
        "user__name 中的连续下划线应该保持");
  }

  /**
   * 测试表名转换。
   */
  @Test
  @DisplayName("表名转换")
  void testTableNameConversion() {
    assertEquals("user_info", converter.convertTableName("userInfo"),
        "userInfo 表名应该转换为 user_info");
    assertEquals("order_details", converter.convertTableName("orderDetails"),
        "orderDetails 表名应该转换为 order_details");
    assertEquals("user_account", converter.convertTableName("UserAccount"),
        "UserAccount 表名应该转换为 user_account");
  }

  /**
   * 测试表名和列名转换的一致性。
   */
  @Test
  @DisplayName("表名和列名转换一致性")
  void testConsistency() {
    String input = "userAccount";
    assertEquals(converter.convertColumnName(input), converter.convertTableName(input),
        "表名和列名的转换应该保持一致");
  }

  /**
   * 测试缓存功能 - 多次调用相同输入。
   */
  @Test
  @DisplayName("缓存功能验证")
  void testCaching() {
    String input = "userName";
    String result1 = converter.convertColumnName(input);
    String result2 = converter.convertColumnName(input);

    assertEquals("user_name", result1, "第一次转换应该正确");
    assertEquals("user_name", result2, "第二次转换应该正确");
    // 验证缓存是否工作（通过多次调用确保不会抛出异常）
  }

  /**
   * 测试null的处理。
   */
  @Test
  @DisplayName("null值处理")
  void testNull() {
    assertEquals(null, converter.convertColumnName(null),
        "null 输入应该返回 null");
  }

  /**
   * 测试包含数字的字段名转换。
   */
  @Test
  @DisplayName("包含数字的字段名转换")
  void testWithNumbers() {
    assertEquals("user1_name", converter.convertColumnName("user1Name"),
        "user1Name 应该转换为 user1_name");
    assertEquals("address2", converter.convertColumnName("address2"),
        "address2 应该保持为 address2");
  }

  /**
   * 测试复杂场景 - 长字段名。
   */
  @Test
  @DisplayName("复杂长字段名转换")
  void testComplexLongFieldName() {
    assertEquals("user_account_detail_created_at",
        converter.convertColumnName("userAccountDetailCreatedAt"),
        "复杂长字段名应该正确转换");
    assertEquals("user_account_detail_created_at",
        converter.convertColumnName("UserAccountDetailCreatedAt"),
        "PascalCase 的复杂长字段名应该正确转换");
  }

  /**
   * 测试连续大写字母的转换。
   */
  @Test
  @DisplayName("连续大写字母转换")
  void testConsecutiveUpperCase() {
    assertEquals("httpurl", converter.convertColumnName("HTTPUrl"),
        "HTTPUrl 应该转换为 httpurl（连续大写被当作一个单词）");
    assertEquals("xmlparser", converter.convertColumnName("XMLParser"),
        "XMLParser 应该转换为 xmlparser（连续大写被当作一个单词）");
  }

  /**
   * 测试混合格式的输入。
   */
  @Test
  @DisplayName("混合格式输入")
  void testMixedFormat() {
    assertEquals("user_name_id", converter.convertColumnName("userName_id"),
        "混合格式 userName_id 应该转换为 user_name_id");
  }
}
