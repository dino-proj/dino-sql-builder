// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql.naming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 命名转换接口测试类。
 * 
 * <p>测试 {@link NamingConversition} 接口及其默认实现和工厂方法，包括：
 * <ul>
 *   <li>工厂方法验证（ofNop、ofSnake、ofCamel）</li>
 *   <li>Nop 实现的功能验证</li>
 *   <li>单例模式验证</li>
 * </ul>
 * 
 * @author Cody Lu
 * @since 2024-01-03
 */
@DisplayName("命名转换接口测试")
public class NamingConversitionTest {

  /**
   * 测试 ofNop 工厂方法。
   */
  @Test
  @DisplayName("ofNop工厂方法")
  void testOfNop() {
    NamingConversition nop = NamingConversition.ofNop();
    assertNotNull(nop, "ofNop 应该返回非null实例");
    assertTrue(nop instanceof NamingConversition.Nop,
        "ofNop 应该返回 Nop 实例");
  }

  /**
   * 测试 ofSnake 工厂方法。
   */
  @Test
  @DisplayName("ofSnake工厂方法")
  void testOfSnake() {
    NamingConversition snake = NamingConversition.ofSnake();
    assertNotNull(snake, "ofSnake 应该返回非null实例");
    assertTrue(snake instanceof SnakeNamingConversition,
        "ofSnake 应该返回 SnakeNamingConversition 实例");
  }

  /**
   * 测试 ofCamel 工厂方法。
   */
  @Test
  @DisplayName("ofCamel工厂方法")
  void testOfCamel() {
    NamingConversition camel = NamingConversition.ofCamel();
    assertNotNull(camel, "ofCamel 应该返回非null实例");
    assertTrue(camel instanceof CamelNamingConversition,
        "ofCamel 应该返回 CamelNamingConversition 实例");
  }

  /**
   * 测试 Nop 实现的列名转换（不转换）。
   */
  @Test
  @DisplayName("Nop列名转换")
  void testNopColumnNameConversion() {
    NamingConversition nop = NamingConversition.ofNop();

    assertEquals("userName", nop.convertColumnName("userName"),
        "Nop 应该保持 userName 不变");
    assertEquals("user_name", nop.convertColumnName("user_name"),
        "Nop 应该保持 user_name 不变");
    assertEquals("UserName", nop.convertColumnName("UserName"),
        "Nop 应该保持 UserName 不变");
  }

  /**
   * 测试 Nop 实现的表名转换（不转换）。
   */
  @Test
  @DisplayName("Nop表名转换")
  void testNopTableNameConversion() {
    NamingConversition nop = NamingConversition.ofNop();

    assertEquals("userInfo", nop.convertTableName("userInfo"),
        "Nop 应该保持 userInfo 不变");
    assertEquals("user_info", nop.convertTableName("user_info"),
        "Nop 应该保持 user_info 不变");
    assertEquals("UserInfo", nop.convertTableName("UserInfo"),
        "Nop 应该保持 UserInfo 不变");
  }

  /**
   * 测试 Nop 实现处理null值。
   */
  @Test
  @DisplayName("Nop处理null值")
  void testNopWithNull() {
    NamingConversition nop = NamingConversition.ofNop();

    assertEquals(null, nop.convertColumnName(null),
        "Nop 应该返回 null");
    assertEquals(null, nop.convertTableName(null),
        "Nop 应该返回 null");
  }

  /**
   * 测试 Nop 实现处理空字符串。
   */
  @Test
  @DisplayName("Nop处理空字符串")
  void testNopWithEmpty() {
    NamingConversition nop = NamingConversition.ofNop();

    assertEquals("", nop.convertColumnName(""),
        "Nop 应该返回空字符串");
    assertEquals("", nop.convertTableName(""),
        "Nop 应该返回空字符串");
  }

  /**
   * 测试 Nop 单例模式。
   */
  @Test
  @DisplayName("Nop单例模式")
  void testNopSingleton() {
    NamingConversition nop1 = NamingConversition.ofNop();
    NamingConversition nop2 = NamingConversition.ofNop();

    assertSame(nop1, nop2, "ofNop 应该返回同一个实例（单例）");
  }

  /**
   * 测试不同工厂方法返回的实例类型不同。
   */
  @Test
  @DisplayName("不同工厂方法返回不同实例类型")
  void testDifferentFactoryMethods() {
    NamingConversition nop = NamingConversition.ofNop();
    NamingConversition snake = NamingConversition.ofSnake();
    NamingConversition camel = NamingConversition.ofCamel();

    // 验证它们是不同类型的实例
    assertTrue(nop instanceof NamingConversition.Nop,
        "nop 应该是 Nop 实例");
    assertTrue(snake instanceof SnakeNamingConversition,
        "snake 应该是 SnakeNamingConversition 实例");
    assertTrue(camel instanceof CamelNamingConversition,
        "camel 应该是 CamelNamingConversition 实例");
  }

  /**
   * 测试不同实现的转换行为。
   */
  @Test
  @DisplayName("不同实现的转换行为对比")
  void testDifferentImplementations() {
    String input = "userName";

    NamingConversition nop = NamingConversition.ofNop();
    NamingConversition snake = NamingConversition.ofSnake();
    NamingConversition camel = NamingConversition.ofCamel();

    // Nop 不转换
    assertEquals("userName", nop.convertColumnName(input),
        "Nop 应该保持原样");

    // Snake 转换为下划线
    assertEquals("user_name", snake.convertColumnName(input),
        "Snake 应该转换为 user_name");

    // Camel 将没有下划线的驼峰转为全小写
    assertEquals("username", camel.convertColumnName(input),
        "Camel 应该转换为 username（因为没有下划线）");
  }

  /**
   * 测试 Nop 实例的直接访问。
   */
  @Test
  @DisplayName("Nop实例直接访问")
  void testNopInstanceDirectAccess() {
    NamingConversition.Nop nop = NamingConversition.Nop.INST;
    assertNotNull(nop, "Nop.INST 应该是非null的");

    assertEquals("test", nop.convertColumnName("test"),
        "直接访问的 Nop 实例应该正常工作");
  }

  /**
   * 测试特殊字符处理。
   */
  @Test
  @DisplayName("Nop特殊字符处理")
  void testNopWithSpecialCharacters() {
    NamingConversition nop = NamingConversition.ofNop();

    assertEquals("user@name", nop.convertColumnName("user@name"),
        "Nop 应该保持特殊字符不变");
    assertEquals("user-name", nop.convertColumnName("user-name"),
        "Nop 应该保持连字符不变");
    assertEquals("user.name", nop.convertColumnName("user.name"),
        "Nop 应该保持点号不变");
  }
}
