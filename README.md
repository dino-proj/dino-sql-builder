<div align="center">
  <div><img src="./.assert/intro.svg" style="max-height:300px"  /></div>
</div>

## 🦖 Dino Sql Builder

Dino Sql Builder 是一个开源的 Java SQL 构建工具，支持类型安全、链式调用和多数据库方言，帮助开发者优雅地生成 SQL 语句，提升开发效率。

---

## 特性

- 类型安全的 SQL 构建 API
- 支持 SELECT、INSERT、UPDATE、DELETE 等常用语句
- 多数据库方言（MySQL、PostgreSQL 等）
- 命名风格自动转换（驼峰、下划线）
- 轻量、无依赖、易集成

---

## 快速开始

### 依赖引入

在你的 Maven `pom.xml` 中添加依赖：

```xml
<dependency>
  <groupId>cn.dinodev</groupId>
  <artifactId>dino-sql-builder</artifactId>
  <version>2.1</version>
</dependency>
```

### 示例代码

```java
import cn.dinodev.sql.dialect.PostgreSqlDialect;
import cn.dinodev.sql.builder.SelectSqlBuilder;

var dialect = new PostgreSqlDialect();

// 构建 SELECT 语句
String sql = SelectSqlBuilder.create(dialect, "user")
    .where("age > ?", 18)
    .orderBy("id DESC")
    .toSql();
System.out.println(sql); // SELECT * FROM user WHERE age > ? ORDER BY id DESC
```

更多用法见 [src/main/java/cn/dinodev/sql/builder/](src/main/java/cn/dinodev/sql/builder/) 目录。

---

## 贡献

欢迎提交 Issue 或 PR，完善文档、修复 bug 或新增特性。

---


## 相关链接

- [Dino Spring 服务框架](https://dinodev.cn/dino-spring/)

## 📄 License

dino-sql-builder is an open source software licensed as [Apache-2.0](./LICENSE).

## 🫶 Citation
If you find our work useful for your research, please consider citing the paper:

```bibtex
@misc{Dino-spring,
  author = {Cody Lu},
  title = {Dino-spring},
  year = {2023},
  publisher = {GitHub},
  journal = {GitHub Repository},
  howpublished = {\url{https://github.com/dino-proj/dino-sql-builder}}
}