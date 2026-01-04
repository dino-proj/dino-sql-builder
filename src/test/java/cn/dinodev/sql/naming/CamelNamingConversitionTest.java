// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.naming;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 驼峰命名转换器测试类。
 * 
 * <p>测试 {@link CamelNamingConversition} 的命名转换功能，包括：
 * <ul>
 *   <li>基本下划线转驼峰转换</li>
 *   <li>表名转换</li>
 *   <li>边界情况处理（空值、特殊字符等）</li>
 *   <li>缓存功能验证</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2024-01-03
 */
@DisplayName("驼峰命名转换器测试")
public class CamelNamingConversitionTest {

  private CamelNamingConversition converter;

  @BeforeEach
  public void setUp() {
    converter = new CamelNamingConversition();
  }

  /**
   * 测试基本的下划线转驼峰转换。
   */
  @Test
  @DisplayName("基本下划线转驼峰转换")
  void testBasicConversion() {
    assertEquals("userName", converter.convertColumnName("user_name"),
        "user_name 应该转换为 userName");
    assertEquals("userId", converter.convertColumnName("user_id"),
        "user_id 应该转换为 userId");
    assertEquals("createdAt", converter.convertColumnName("created_at"),
        "created_at 应该转换为 createdAt");
  }

  /**
   * 测试单个单词的转换。
   */
  @Test
  @DisplayName("单个单词转换")
  void testSingleWord() {
    assertEquals("user", converter.convertColumnName("user"),
        "单个单词 user 应该保持为 user");
    assertEquals("name", converter.convertColumnName("name"),
        "单个单词 name 应该保持为 name");
  }

  /**
   * 测试以下划线开头的转换。
   */
  @Test
  @DisplayName("以下划线开头的转换")
  void testUnderscoreStart() {
    assertEquals("_userName", converter.convertColumnName("_user_name"),
        "_user_name 应该转换为 _userName（下划线保留）");
    assertEquals("_user", converter.convertColumnName("_user"),
        "_user 应该转换为 _user（下划线保留）");
  }

  /**
   * 测试多个连续下划线的转换。
   */
  @Test
  @DisplayName("多个连续下划线转换")
  void testMultipleUnderscores() {
    assertEquals("userNameId", converter.convertColumnName("user__name__id"),
        "user__name__id 中的连续下划线应该被正确处理");
  }

  /**
   * 测试全大写字段名的转换。
   */
  @Test
  @DisplayName("全大写字段名转换")
  void testAllUpperCase() {
    assertEquals("userId", converter.convertColumnName("USER_ID"),
        "USER_ID 应该转换为 userId");
    assertEquals("userName", converter.convertColumnName("USER_NAME"),
        "USER_NAME 应该转换为 userName");
  }

  /**
   * 测试表名转换。
   */
  @Test
  @DisplayName("表名转换")
  void testTableNameConversion() {
    assertEquals("userInfo", converter.convertTableName("user_info"),
        "user_info 表名应该转换为 userInfo");
    assertEquals("orderDetails", converter.convertTableName("order_details"),
        "order_details 表名应该转换为 orderDetails");
  }

  /**
   * 测试表名和列名转换的一致性。
   */
  @Test
  @DisplayName("表名和列名转换一致性")
  void testConsistency() {
    String input = "user_account";
    assertEquals(converter.convertColumnName(input), converter.convertTableName(input),
        "表名和列名的转换应该保持一致");
  }

  /**
   * 测试缓存功能 - 多次调用相同输入。
   */
  @Test
  @DisplayName("缓存功能验证")
  void testCaching() {
    String input = "user_name";
    String result1 = converter.convertColumnName(input);
    String result2 = converter.convertColumnName(input);

    assertEquals("userName", result1, "第一次转换应该正确");
    assertEquals("userName", result2, "第二次转换应该正确");
    // 验证缓存是否工作（通过多次调用确保不会抛出异常）
  }

  /**
   * 测试空值和null的处理。
   */
  @Test
  @DisplayName("空值处理")
  void testNullAndEmpty() {
    assertEquals(null, converter.convertColumnName(null),
        "null 输入应该返回 null");
    assertEquals("", converter.convertColumnName(""),
        "空字符串输入应该返回空字符串");
  }

  /**
   * 测试包含数字的字段名转换。
   */
  @Test
  @DisplayName("包含数字的字段名转换")
  void testWithNumbers() {
    assertEquals("user1Name", converter.convertColumnName("user_1_name"),
        "user_1_name 应该转换为 user1Name");
    assertEquals("address2", converter.convertColumnName("address_2"),
        "address_2 应该转换为 address2");
  }

  /**
   * 测试复杂场景 - 长字段名。
   */
  @Test
  @DisplayName("复杂长字段名转换")
  void testComplexLongFieldName() {
    assertEquals("userAccountDetailCreatedAt",
        converter.convertColumnName("user_account_detail_created_at"),
        "复杂长字段名应该正确转换");
  }

  /**
   * 测试已经是驼峰格式的输入。
   */
  @Test
  @DisplayName("已是驼峰格式的输入")
  void testAlreadyCamelCase() {
    assertEquals("username", converter.convertColumnName("userName"),
        "驼峰格式的输入会被转换为全小写（因为没有下划线）");
    assertEquals("userid", converter.convertColumnName("userId"),
        "驼峰格式的输入会被转换为全小写（因为没有下划线）");
  }
}
