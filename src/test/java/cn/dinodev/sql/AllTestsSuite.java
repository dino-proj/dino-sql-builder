// Copyright 2024 dinosdev.cn.
// SPDX-License-Identifier: Apache-2.0

package cn.dinodev.sql;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Dino SQL Builder 完整测试套件。
 * 
 * <p>这个测试套件包含所有SQL构建器、数据库方言和命名转换器的测试，可以统一运行所有测试用例。
 * 
 * <p>运行方式：
 * <pre>{@code
 * // Maven
 * mvn test -Dtest=AllTestsSuite
 * 
 * // IDE
 * 在IDE中直接运行此类
 * }</pre>
 * 
 * @author Cody Lu
 * @since 2026-01-03
 */
@Suite
@SuiteDisplayName("Dino SQL Builder 完整测试套件")
@SelectPackages({ "cn.dinodev.sql.builder", "cn.dinodev.sql.naming", "cn.dinodev.sql.dialect" })
public class AllTestsSuite {
  // 测试套件类通常为空，通过注解配置
}
