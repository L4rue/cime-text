# elanguage

E 文本（E 语言）解析库，支持横表式（`@`）、单列式（`@@`）、多列式（`@#`）三类表体。

## 环境要求

- Java 8+
- Maven 3.8+

## 快速使用

```java
DefaultEfileParse parser = new DefaultEfileParse();
List<ETable> tables = parser.parseFile(new File("data/横表式.txt"));
```

默认策略是严格模式，遇到异常行会抛出包含表名和行号的异常。

## 解析策略配置

新增 `ParseOptions`：

- `ParseOptions.strict()`：默认严格模式，异常数据行直接抛错。
- `ParseOptions.skipMalformedRows()`：跳过异常数据行继续解析。

```java
DefaultEfileParse parser = new DefaultEfileParse();
List<ETable> tables = parser.parseFile(new File("data/横表式.txt"), ParseOptions.skipMalformedRows());
```

## ETable 到业务对象映射

使用 `@ETable` 和 `@EColumn` 注解进行表名、列名映射：

```java
List<BizBean> beans = BeanUtils.parseBean(table, BizBean.class);
```

## 测试

仓库已补齐样例驱动测试，覆盖三种表格式、引号切分、`date` 属性解析、异常策略与 Bean 映射。

```bash
mvn test
```

如果本地提示 `JAVA_HOME` 未配置，请先设置到 Java 8 运行环境后再执行。
