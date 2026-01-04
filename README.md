<div align="center">
  <div><img src="./.assert/intro.svg" style="max-height:300px"  /></div>
</div>

## 🦖 Dino Sql Builder

Dino Sql Builder 是一个轻量级、类型安全的 Java SQL 构建工具，专为提升开发效率而设计。通过流畅的链式 API 和智能的方言系统，让 SQL 构建变得优雅而简单。

[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](./LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)

---

## ✨ 核心特性

- **🔒 类型安全** - 强类型 API 设计，编译期发现错误，避免 SQL 注入
- **⛓️ 链式调用** - 流畅的 Fluent API，代码可读性强，易于维护
- **🎯 功能完整** - 支持 SELECT、INSERT、UPDATE、DELETE 以及复杂子查询
- **🌐 多数据库** - 内置 MySQL、PostgreSQL 方言，轻松扩展其他数据库
- **📐 命名转换** - 自动处理驼峰/下划线命名转换，无需手动适配
- **🪶 零依赖** - 纯 Java 实现，无第三方依赖，轻量集成
- **🧪 高测试覆盖** - 完善的单元测试，代码质量有保障

---

## 📦 安装

### Maven

在你的 `pom.xml` 中添加依赖：

```xml
<dependency>
  <groupId>cn.dinodev</groupId>
  <artifactId>dino-sql-builder</artifactId>
  <version>2.1</version>
</dependency>
```

### Gradle

```gradle
implementation 'cn.dinodev:dino-sql-builder:2.1'
```

---

## 🚀 快速开始

### 1. 初始化方言

方言（Dialect）是数据库适配层，负责处理不同数据库的语法差异和命名转换。

```java
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

// MySQL 方言，使用驼峰命名
Dialect dialect = new MysqlDialect(null, new CamelNamingConversition());

// PostgreSQL 方言，使用下划线命名
Dialect pgDialect = new PostgreSQLDialect(metaData, new SnakeNamingConversition());
```

### 2. 构建简单查询

```java
import cn.dinodev.sql.builder.SelectSqlBuilder;

// 基本 SELECT
SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "users")
    .column("id", "name", "email")
    .eq("status", 1)
    .gt("age", 18)
    .orderByDesc("created_at")
    .limit(10);

String sql = builder.getSql();
// SELECT id, name, email FROM users WHERE status = ? AND (age > ?) ORDER BY created_at DESC LIMIT 10

Object[] params = builder.getParams();
// [1, 18]
```

---

## 📖 使用示例

### SELECT 查询

#### 基本查询

```java
SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "users")
    .column("id", "name", "email")
    .eq("status", 1)
    .like("name", "张")
    .orderByAsc("name")
    .limit(20);
```

#### JOIN 查询

```java
SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "users", "u")
    .column("u.id", "u.name", "o.order_no", "o.amount")
    .leftJoin("orders", "o", "u.id = o.user_id")
    .eq("u.status", 1)
    .gt("o.amount", 100)
    .orderByDesc("o.created_at");
```

#### GROUP BY 和聚合

```java
SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "orders")
    .column("user_id", "COUNT(*) AS order_count", "SUM(amount) AS total")
    .eq("status", "completed")
    .groupBy("user_id")
    .havingCountGt(5)  // HAVING COUNT(*) > 5
    .orderByDesc("total")
    .limit(10);
```

#### 子查询

```java
// 创建子查询
SelectSqlBuilder subQuery = SelectSqlBuilder.create(dialect, "orders")
    .column("user_id")
    .eq("status", "completed")
    .groupBy("user_id")
    .havingCountGt(10);

// 在主查询中使用
SelectSqlBuilder mainQuery = SelectSqlBuilder.create(dialect, "users")
    .column("id", "name")
    .in("id", subQuery);  // WHERE id IN (子查询)
```

#### UNION 查询

```java
SelectSqlBuilder query1 = SelectSqlBuilder.create(dialect, "users")
    .column("name", "email")
    .eq("type", "admin");

SelectSqlBuilder query2 = SelectSqlBuilder.create(dialect, "customers")
    .column("name", "email")
    .eq("vip", 1);

// UNION（去重）
SelectSqlBuilder unionQuery = query1.union(query2);

// UNION ALL（保留重复）
SelectSqlBuilder unionAllQuery = query1.unionAll(query2);
```

#### 复杂综合查询

```java
SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "products", "p")
    .distinct()
    .column("p.id", "p.name", "p.price", "c.name AS category_name")
    .leftJoin("categories", "c", "p.category_id = c.id")
    .eqIfNotNull("p.status", 1)          // 条件为真才添加
    .gtIfNotNull("p.price", 100)
    .in("p.category_id", Arrays.asList(1, 2, 3))
    .likeIfNotBlank("p.name", "手机")    // 参数非空才添加
    .isNotNull("p.inventory")
    .groupBy("p.id", "c.name")
    .havingCountGt(0)
    .orderByDesc("p.price")
    .orderByAsc("p.name")
    .limitPage(2, 20);  // 第2页，每页20条

String sql = builder.getSql();
Object[] params = builder.getParams();
```

### INSERT 语句

#### 基本插入

```java
InsertSqlBuilder builder = InsertSqlBuilder.create(dialect, "users")
    .set("name", "张三")
    .set("age", 25)
    .set("email", "zhangsan@example.com")
    .setExpression("created_at", "NOW()");  // 数据库函数

String sql = builder.getSql();
// INSERT INTO users (name, age, email, created_at) VALUES (?, ?, ?, NOW())

Object[] params = builder.getParams();
// ["张三", 25, "zhangsan@example.com"]
```

#### 批量插入

```java
InsertSqlBuilder builder = InsertSqlBuilder.create(dialect, "users")
    .columns("name", "age", "email")
    .values("张三", 25, "zhangsan@example.com")
    .values("李四", 30, "lisi@example.com")
    .values("王五", 28, "wangwu@example.com");

String sql = builder.getSql();
// INSERT INTO users (name, age, email) VALUES (?, ?, ?), (?, ?, ?), (?, ?, ?)
```

#### 使用表达式

```java
InsertSqlBuilder builder = InsertSqlBuilder.create(dialect, "users")
    .set("name", "张三")
    .set("status", "UPPER(?)", "active")  // 自定义表达式
    .setExpression("id", "UUID()")
    .setExpression("created_at", "CURRENT_TIMESTAMP");
```

#### 条件插入

```java
String nickname = null;
InsertSqlBuilder builder = InsertSqlBuilder.create(dialect, "users")
    .set("name", "张三")
    .setIfNotNull("nickname", nickname)  // nickname 为 null，不会插入
    .setIfNotBlank("email", "");         // email 为空字符串，不会插入
```

### UPDATE 语句

#### 基本更新

```java
UpdateSqlBuilder builder = UpdateSqlBuilder.create(dialect, "users")
    .set("name", "张三")
    .set("age", 26)
    .setExpression("updated_at", "NOW()")
    .eq("id", 1);

String sql = builder.getSql();
// UPDATE users SET name = ?, age = ?, updated_at = NOW() WHERE id = ?
```

#### 表达式更新

```java
UpdateSqlBuilder builder = UpdateSqlBuilder.create(dialect, "products")
    .increment("view_count")        // view_count = view_count + 1
    .decrement("stock", 5)          // stock = stock - 5
    .eq("id", 100);
```

#### 关联更新（MySQL）

```java
UpdateSqlBuilder builder = UpdateSqlBuilder.create(dialect, "orders", "o")
    .innerJoin("users", "u", "o.user_id = u.id")
    .set("o.status", "vip_processed")
    .eq("u.level", "VIP")
    .eq("o.status", "pending");
```

#### 关联更新（PostgreSQL）

```java
UpdateSqlBuilder builder = UpdateSqlBuilder.create(pgDialect, "orders", "o")
    .from("users", "u")
    .set("o.status", "vip_processed")
    .where("o.user_id = u.id")
    .eq("u.level", "VIP")
    .eq("o.status", "pending");
```

#### 条件更新

```java
Integer newAge = null;
String newEmail = "newemail@example.com";

UpdateSqlBuilder builder = UpdateSqlBuilder.create(dialect, "users")
    .setIfNotNull("age", newAge)        // newAge 为 null，不会更新
    .setIfNotBlank("email", newEmail)   // newEmail 不为空，会更新
    .eq("id", 1);
```

### DELETE 语句

#### 基本删除

```java
DeleteSqlBuilder builder = DeleteSqlBuilder.create(dialect, "users")
    .eq("id", 1);

String sql = builder.getSql();
// DELETE FROM users WHERE id = ?
```

#### 条件删除

```java
DeleteSqlBuilder builder = DeleteSqlBuilder.create(dialect, "users")
    .eq("status", 0)
    .lt("last_login", "2023-01-01")
    .isNull("email");

String sql = builder.getSql();
// DELETE FROM users WHERE status = ? AND (last_login < ?) AND (email IS NULL)
```

#### 批量删除

```java
DeleteSqlBuilder builder = DeleteSqlBuilder.create(dialect, "logs")
    .in("id", Arrays.asList(1, 2, 3, 4, 5));
```

### WHERE 条件构建

Dino Sql Builder 提供了丰富的条件构建方法，支持各种比较操作符和逻辑组合。

#### 基本条件

```java
builder
    .eq("status", 1)                    // status = ?
    .ne("deleted", 1)                   // deleted != ?
    .gt("age", 18)                      // age > ?
    .gte("score", 60)                   // score >= ?
    .lt("price", 100)                   // price < ?
    .lte("stock", 10)                   // stock <= ?
    .like("name", "张")                 // name LIKE ?  (自动添加 %)
    .isNull("deleted_at")               // deleted_at IS NULL
    .isNotNull("email")                 // email IS NOT NULL
    .in("category_id", Arrays.asList(1, 2, 3))  // category_id IN (?, ?, ?)
    .notIn("status", Arrays.asList(0, -1));     // status NOT IN (?, ?)
```

#### 条件方法（If 系列）

只有在条件满足时才添加到 WHERE 子句：

```java
String name = "张三";
Integer age = null;
String email = "";

builder
    .eqIfNotNull("name", name)      // name 不为 null，添加条件
    .gtIfNotNull("age", age)        // age 为 null，不添加
    .likeIfNotBlank("email", email) // email 为空串，不添加
    .eqIf(status == 1, "type", "VIP");  // 自定义条件判断
```

#### BETWEEN 条件

```java
builder
    .between("age", 18, 65)         // age BETWEEN ? AND ?
    .notBetween("price", 0, 10);    // price NOT BETWEEN ? AND ?
```

#### 自定义条件

```java
builder
    .where("age > ? AND age < ?", 18, 65)
    .where("DATE(created_at) = ?", "2024-01-01")
    .whereIf(needFilter, "status = ?", 1);  // 条件为真才添加
```

#### 逻辑组合（AND/OR）

```java
// 默认是 AND 连接
builder
    .eq("status", 1)
    .gt("age", 18)
    .like("name", "张");
// WHERE status = ? AND (age > ?) AND (name LIKE ?)

// 使用 OR 连接
builder
    .eq("status", 1)
    .or()  // 切换到 OR 模式
    .eq("level", "VIP")
    .like("email", "@vip.com");
// WHERE status = ? OR (level = ?) OR (email LIKE ?)

// 混合使用（需要注意逻辑）
builder
    .eq("status", 1)
    .and()  // 显式指定 AND
    .gt("age", 18)
    .or()   // 切换到 OR
    .eq("level", "VIP");
```

### 高级特性

#### GROUP BY ALL（PostgreSQL 17+）

PostgreSQL 17 引入的便捷语法，自动将 SELECT 中的非聚合列添加到 GROUP BY：

```java
// 需要 PostgreSQL 17+ 方言
PostgreSQLDialect pg17 = new PostgreSQLDialect(
    DatabaseMetaDataMocks.createPostgreSQL(17),
    new CamelNamingConversition()
);

SelectSqlBuilder builder = SelectSqlBuilder.create(pg17, "sales")
    .column("region", "product", "COUNT(*) AS count", "SUM(amount) AS total")
    .gt("amount", 100)
    .groupByAll()  // 自动 GROUP BY region, product
    .orderByDesc("total");
```

#### WITH 子句（CTE）

```java
// 创建 CTE
SelectSqlBuilder cte = SelectSqlBuilder.create(dialect, "orders")
    .column("user_id", "COUNT(*) AS order_count")
    .eq("status", "completed")
    .groupBy("user_id");

// 使用 CTE
SelectSqlBuilder mainQuery = SelectSqlBuilder.create(dialect, "users", "u")
    .with("user_orders", cte)
    .column("u.name", "uo.order_count")
    .innerJoin("user_orders", "uo", "u.id = uo.user_id")
    .gt("uo.order_count", 10);
```

#### 分页查询

```java
// LIMIT + OFFSET
builder.limit(20).offset(40);  // 跳过40条，取20条

// 直接使用页码
builder.limitPage(3, 20);  // 第3页，每页20条（等同于 LIMIT 20 OFFSET 40）
```

#### COUNT 查询

```java
SelectSqlBuilder builder = SelectSqlBuilder.create(dialect, "users")
    .column("id", "name")
    .eq("status", 1)
    .gt("age", 18);

// 获取 COUNT SQL（自动去除 ORDER BY 和 LIMIT）
String countSql = builder.getCountSql();
// SELECT count(1) AS cnt FROM users WHERE status = ? AND (age > ?)
```

#### 类型转换（Type Cast）

不同数据库有不同的类型转换语法，方言会自动处理：

```java
// MySQL: CAST(column AS type)
builder.column("CAST(price AS DECIMAL(10,2))");

// PostgreSQL: column::type
builder.column("price::NUMERIC(10,2)");

// 使用方言方法（推荐）
String castExpr = dialect.typeCast("price", "DECIMAL(10,2)");
builder.column(castExpr + " AS formatted_price");
```

---

## 🎨 方言系统

方言（Dialect）是适配不同数据库的核心机制，负责处理：

1. **SQL 语法差异** - 如类型转换、LIMIT/OFFSET 语法等
2. **命名转换** - 驼峰与下划线命名自动转换
3. **特性支持** - 如 PostgreSQL 的 RETURNING、MySQL 的 ON DUPLICATE KEY 等

### 内置方言

#### MySQL 方言

```java
import cn.dinodev.sql.dialect.MysqlDialect;
import cn.dinodev.sql.naming.CamelNamingConversition;

Dialect mysql = new MysqlDialect(null, new CamelNamingConversition());
```

**特性：**
- 支持 `LIMIT offset, count` 语法
- 支持 JOIN 更新
- 类型转换使用 `CAST(expr AS type)`

#### PostgreSQL 方言

```java
import cn.dinodev.sql.dialect.PostgreSQLDialect;
import cn.dinodev.sql.naming.SnakeNamingConversition;

Dialect postgres = new PostgreSQLDialect(metaData, new SnakeNamingConversition());
```

**特性：**
- 支持 `LIMIT count OFFSET offset` 语法
- 支持 FROM 子句更新
- 类型转换使用 `expr::type`
- PostgreSQL 17+ 支持 `GROUP BY ALL`

### 命名转换策略

#### 驼峰命名（CamelNamingConversition）

Java 代码中的字段名保持驼峰风格，不做转换：

```java
NamingConversition camel = new CamelNamingConversition();
camel.columnName("userName");  // userName
camel.tableName("userOrders"); // userOrders
```

#### 下划线命名（SnakeNamingConversition）

自动将驼峰转换为下划线风格：

```java
NamingConversition snake = new SnakeNamingConversition();
snake.columnName("userName");  // user_name
snake.tableName("userOrders"); // user_orders
```

---

## 🔧 配置与扩展

### 自定义方言

如需支持其他数据库，实现 `Dialect` 接口：

```java
public class OracleDialect implements Dialect {
    private final NamingConversition naming;
    
    public OracleDialect(DatabaseMetaData metaData, NamingConversition naming) {
        this.naming = naming;
    }
    
    @Override
    public String columnName(String name) {
        return naming.columnName(name);
    }
    
    @Override
    public String tableName(String name) {
        return naming.tableName(name);
    }
    
    @Override
    public String typeCast(String column, String type) {
        return "CAST(" + column + " AS " + type + ")";
    }
    
    // 实现其他接口方法...
}
```

### 自定义命名策略

实现 `NamingConversition` 接口：

```java
public class CustomNamingConversition implements NamingConversition {
    @Override
    public String columnName(String name) {
        // 自定义列名转换逻辑
        return name.toUpperCase();
    }
    
    @Override
    public String tableName(String name) {
        // 自定义表名转换逻辑
        return "tbl_" + name;
    }
}
```

---

## 🧪 测试

项目使用 JUnit 5 + JaCoCo 进行测试和覆盖率统计。

### 运行测试

```bash
# 运行全部测试
mvn test

# 运行指定测试类
mvn test -Dtest=SelectSqlBuilderTest

# 生成覆盖率报告
mvn clean test jacoco:report
```

覆盖率报告生成在 `target/site/jacoco/index.html`。

### 测试工具类

项目提供了测试辅助工具：

```java
import static cn.dinodev.sql.testutil.SqlTestHelper.*;

// 断言 SQL 语句
assertSql(builder, "测试描述", "期望的SQL");

// 断言 SQL 和参数
assertSqlWithParams(builder, "测试描述", "期望的SQL", new Object[]{参数1, 参数2});
```

---

## 📚 API 文档

生成 Javadoc：

```bash
mvn javadoc:javadoc
```

文档生成在 `target/reports/apidocs/`。

---

## 🤝 贡献指南

欢迎各种形式的贡献！

### 贡献方式

1. **报告问题** - 在 [Issues](https://github.com/dino-proj/dino-sql-builder/issues) 中提交 Bug 或功能建议
2. **提交代码** - Fork 项目，提交 Pull Request
3. **完善文档** - 改进 README、Javadoc 或示例代码
4. **分享经验** - 在博客或社区分享使用心得

### 开发流程

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/amazing-feature`
3. 提交代码：`git commit -m 'Add amazing feature'`
4. 推送分支：`git push origin feature/amazing-feature`
5. 提交 Pull Request

### 代码规范

- 遵循 Java 17+ 标准
- 保持代码简洁，方法不超过 50 行
- 添加完整的 Javadoc 注释
- 编写单元测试，覆盖率不低于 80%
- 所有测试必须通过

---

## 📝 更新日志

### v2.1 (2024-01-04)

- ✨ 新增 PostgreSQL 17+ `GROUP BY ALL` 支持
- 🔧 优化 WHERE 条件构建逻辑
- 📖 完善文档和示例
- 🐛 修复已知问题

### v2.0 (2024-12-01)

- 🎉 全新重构的 API 设计
- ✨ 增强的子句支持（WITH、UNION、HAVING 等）
- 🌐 改进的方言系统
- 🧪 完善的测试覆盖

---

## 🔗 相关链接

- [官方文档](https://dinodev.cn/dino-sql-builder/)
- [GitHub 仓库](https://github.com/dino-proj/dino-sql-builder)
- [问题反馈](https://github.com/dino-proj/dino-sql-builder/issues)

---

## 📄 许可证

本项目采用 [Apache-2.0](./LICENSE) 开源许可证。

---

## 🫶 引用

如果本项目对您的研究或工作有帮助，请考虑引用：

```bibtex
@misc{Dino-Sql-Builder,
  author = {Cody Lu},
  title = {Dino-Sql-Builder: A Type-Safe SQL Builder for Java},
  year = {2024},
  publisher = {GitHub},
  journal = {GitHub Repository},
  howpublished = {\url{https://github.com/dino-proj/dino-sql-builder}}
}