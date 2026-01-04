# Dino Sql Builder — AI Agent Coding Instructions

## 项目架构与核心组件
- 本项目为 Java SQL 构建工具，基于 Java 17 开发，主入口在 `src/main/java/cn/dinodev/sql/`。
- 主要组件：
  - `SqlBuilder` 接口：定义 SQL 构建器基础接口，提供 `getSql()`, `getParams()` 等核心方法。
  - `builder/`：各类 SQL 构建器（如 `SelectSqlBuilder`, `InsertSqlBuilder`, `UpdateSqlBuilder`, `DeleteSqlBuilder`），实现链式、类型安全 SQL 生成。
  - `builder/clause/`：SQL 子句封装（如 `WhereClause`, `JoinClause`, `GroupByClause`, `OrderByClause`, `LimitOffsetClause` 等），支持复杂查询构建。
  - `dialect/`：数据库方言支持（如 `MysqlDialect`, `PostgreSQLDialect`），通过方言对象适配 SQL 语法差异。
  - `naming/`：命名转换策略（`CamelNamingConversition`, `SnakeNamingConversition`），实现驼峰与下划线命名自动转换。
  - `utils/`：工具类，提供 SQL 构建辅助功能。
- 典型用法见 `README.md` 示例，推荐从 `SelectSqlBuilder.create(dialect, table)` 入手。

## 开发者工作流
- 构建：使用 Maven，主命令为 `mvn clean package`。
- 文档生成：`mvn javadoc:javadoc`，生成在 `target/reports/apidocs/`。
- 代码目录结构遵循标准 Maven Java 项目布局。
- 依赖管理：无第三方依赖，核心为自定义 SQL 构建逻辑。

## 约定与模式
- **链式调用**：所有 SQL 构建器均采用 Fluent API 风格，支持链式调用，提升代码可读性。
- **类型安全**：构建器使用强类型参数，避免 SQL 注入风险，参数统一通过预编译方式绑定。
- **方言适配**：数据库差异通过传入 `Dialect` 实例实现，推荐优先使用已有方言类（`MysqlDialect`、`PostgreSQLDialect`）。
- **命名转换**：字段和表名的驼峰/下划线转换由 `NamingConversition` 接口及实现类自动处理。
- **SQL 输出**：所有 SQL 构建器实现 `SqlBuilder` 接口，通过 `getSql()` 获取 SQL 字符串，通过 `getParams()` 获取参数数组。
- **零依赖原则**：项目核心代码无第三方运行时依赖，仅在测试阶段依赖 JUnit 5。

## 扩展与集成
- **新增 SQL 构建器**：
  - 实现 `SqlBuilder` 接口或继承现有 builder 基类。
  - 参考 `SelectSqlBuilder`, `InsertSqlBuilder` 等现有实现。
  - 推荐在 `src/main/java/cn/dinodev/sql/builder/` 目录下创建。
- **新增数据库方言**：
  - 实现 `Dialect` 接口，定义数据库特定的 SQL 语法规则。
  - 参考 `MysqlDialect` 和 `PostgreSQLDialect` 的实现模式。
  - 推荐在 `src/main/java/cn/dinodev/sql/dialect/` 目录下创建。
- **新增 SQL 子句**：
  - 继承 `ClauseSupport` 基类，实现特定子句逻辑。
  - 推荐在 `src/main/java/cn/dinodev/sql/builder/clause/` 目录下创建。
- **新增命名转换策略**：
  - 实现 `NamingConversition` 接口。
  - 推荐在 `src/main/java/cn/dinodev/sql/naming/` 目录下创建。

## 关键文件/目录
- `src/main/java/cn/dinodev/sql/`：
  - `SqlBuilder.java`：SQL 构建器核心接口
  - `Logic.java`, `Oper.java`, `Range.java`：SQL 操作符和逻辑枚举定义
  - `builder/`：SQL 构建器实现（SELECT、INSERT、UPDATE、DELETE）
  - `builder/clause/`：SQL 子句封装（WHERE、JOIN、GROUP BY、ORDER BY 等）
  - `dialect/`：数据库方言（MySQL、PostgreSQL）
  - `naming/`：命名转换策略（驼峰、下划线）
  - `utils/`：工具类
- `src/test/java/cn/dinodev/sql/`：
  - `AllTestsSuite.java`：完整测试套件入口
  - `builder/`：SQL 构建器测试用例
  - `testutil/`：测试工具类（`SqlTestHelper`, `DatabaseMetaDataMocks`）
- `README.md`：项目说明、快速入门与示例
- `pom.xml`：Maven 配置（Java 17、JUnit 5、JaCoCo）
- `LICENSE`：Apache-2.0 开源许可证

## 测试用例约定
- **测试框架**：使用 JUnit 5 编写单元测试，测试类以 `*Test.java` 命名。
- **测试组织**：
  - 所有测试类位于 `src/test/java/cn/dinodev/sql/builder/` 目录。
  - 使用 `@DisplayName` 注解为测试类和方法提供清晰的中文描述。
  - 测试类应与被测试的构建器对应（如 `SelectSqlBuilderV2Test` 对应 `SelectSqlBuilder`）。
- **测试工具类**：
  - `SqlTestHelper`：提供 `assertSql()` 和 `assertSqlWithParams()` 方法统一验证 SQL 语句和参数。
  - `DatabaseMetaDataMocks`：提供模拟的数据库元数据，用于测试方言功能。
- **测试用例结构**：
  - 使用 `@BeforeEach` 初始化方言和命名转换对象。
  - 每个测试方法聚焦一个特定功能点（如 WHERE 条件、JOIN 操作、GROUP BY 等）。
  - 测试验证包括 SQL 语句正确性和参数绑定正确性。
- **测试覆盖范围**：
  - 基本 CRUD 操作（SELECT、INSERT、UPDATE、DELETE）
  - 复杂查询（JOIN、子查询、聚合函数）
  - 条件构建（WHERE、HAVING）
  - 排序和分页（ORDER BY、LIMIT/OFFSET）
  - 高级特性（WITH 子句、窗口函数等）
  - 多数据库方言适配（MySQL、PostgreSQL 等）
- **运行测试**：
  - 单个测试：`mvn test -Dtest=SelectSqlBuilderV2Test`
  - 全部测试：`mvn test` 或运行 `AllTestsSuite`
  - 覆盖率报告：`mvn clean test jacoco:report`，结果在 `target/site/jacoco/index.html`
- **测试编写规范**：
  - 优先使用 `SqlTestHelper` 工具类进行断言，保持测试代码一致性。
  - 测试数据使用有意义的表名和字段名（如 `users`, `orders` 等）。
  - 复杂场景应拆分为多个独立测试方法，避免单个测试过于臃肿。
  - 测试方法命名应清晰描述测试场景（使用 `@DisplayName` 中文说明）。

## 代码规范
- **Java 版本**：项目基于 Java 17 开发，确保代码兼容 Java 17+。
- **代码风格**：
  - 简洁、类型安全，优先链式 API 设计。
  - 使用有意义的变量名和方法名，避免缩写。
  - 方法保持单一职责，避免过长方法（建议不超过 50 行）。
- **注释规范**：
  - 所有公共类、接口和方法必须包含 Javadoc 注释。
  - Javadoc 应包含功能说明、参数说明（`@param`）、返回值说明（`@return`）和示例代码（`<pre>{@code ...}</pre>`）。
  - 版权声明：所有源文件顶部添加 `// Copyright 2024 dinosdev.cn.` 和 `// SPDX-License-Identifier: Apache-2.0`。
- **异常处理**：优先使用标准异常类，避免捕获后吞没异常。
- **空值处理**：对可能为空的参数进行校验，优先使用 `Objects.requireNonNull()`。

## 其他说明
- **许可证**：本项目采用 Apache-2.0 开源许可证。
- **贡献流程**：Issue/PR 贡献流程见 `README.md`。
- **测试要求**：新增功能时必须同步编写对应的测试用例，确保测试覆盖率不低于 80%。
- **文档同步**：功能变更需同步更新 `README.md` 和相关 Javadoc。

---
如需补充项目约定、开发流程或架构细节，请在 Issue 或 PR 中说明。
