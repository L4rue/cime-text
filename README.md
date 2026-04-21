# cime-text

E 文本（E 语言）解析库，支持横表式（`@`）、单列式（`@@`）、多列式（`@#`）三类表体，并保留文件头、完整标签名和标签属性等元数据。

## 环境要求

- Java 8+
- Maven 3.8+

## 快速使用

```java
import io.github.l4rue.cime.annotation.EColumn;
import io.github.l4rue.cime.annotation.ETable;
import io.github.l4rue.cime.mapping.BeanUtils;

@ETable("StandbyDbInfo")
public static class StandbyDbInfoBean {
    @EColumn("设备ID")
    private String devId;
    @EColumn("厂站名")
    private String dcdName;
    @EColumn("量测名")
    private String measureName;
    @EColumn("时间")
    private String date;
    @EColumn("有功值")
    private String activeValue;
}

List<StandbyDbInfoBean> beans = BeanUtils.parseBeans(
        new File("data/LP_K8000_STAdfgYDB_20191105_113002.DT"),
        StandbyDbInfoBean.class
);
```

默认策略是严格模式，遇到异常行会抛出包含表名和行号的异常。

## 解析文件头和标签属性

如果只关心表数据，可以继续使用 `parseFile(...)/parseBeans(...)`。如果需要读取文件头、完整标签名、`DDMM` 等标签属性，使用 `parseDocument(...)`：

```java
import io.github.l4rue.cime.model.EFileDocument;
import io.github.l4rue.cime.model.ETable;
import io.github.l4rue.cime.parse.DefaultEfileParse;

DefaultEfileParse parser = new DefaultEfileParse();
EFileDocument document = parser.parseDocument(new File("data/多列式.txt"));

String entity = document.getHeader().getAttribute("Entity");
String type = document.getHeader().getAttribute("type");
String dataTime = document.getHeader().getAttribute("dataTime");

ETable table = document.getTables().get(0);
String logicalName = table.getTableName(); // DG
String fullTagName = table.getTagName();   // DG::铁心桥
String date = table.getAttribute("date");  // 2012-04-23
String ddmm = table.getAttribute("DDMM");  // 达梦
```

`ETable.tableName` 仍表示逻辑表名，例如 `DG`，用于兼容业务对象映射；`ETable.tagName` 保留原始完整标签名，例如 `DG::铁心桥`。标签属性按源文件顺序保存在 `ETable.attributes` 中，`date` 同时保留为兼容字段。

## 解析策略配置

新增 `ParseOptions`：

- `ParseOptions.strict()`：默认严格模式，异常数据行直接抛错。
- `ParseOptions.skipMalformedRows()`：跳过异常数据行继续解析。

```java
import io.github.l4rue.cime.model.ETable;
import io.github.l4rue.cime.parse.DefaultEfileParse;
import io.github.l4rue.cime.parse.ParseOptions;

DefaultEfileParse parser = new DefaultEfileParse();
List<ETable> tables = parser.parseFile(new File("data/横表式.txt"), ParseOptions.skipMalformedRows());
```

## ETable 到业务对象映射

使用 `@ETable` 和 `@EColumn` 注解进行表名、列名映射：

```java
import io.github.l4rue.cime.mapping.BeanUtils;

List<BizBean> beans = BeanUtils.parseBean(table, BizBean.class);
```

## 业务对象写回样例（Bean -> 文件）

`BeanUtils` 已封装常用正反向流程，内部会直接 `new DefaultEfileParse()` 和 `new DefaultEfileWrite()`：

- `parseDocument(File)`：文件 -> `EFileDocument`
- `parseTables(File)`：文件 -> `List<ETable>`
- `parseBeans(File, Class<E>)`：文件 -> `List<E>`
- `toTable(List<E>, Class<E>)`：Bean -> `ETable`
- `toDocument(List<E>, Class<E>)` / `toDocument(List<ETable>, EHeader)`：Bean/表 -> `EFileDocument`
- `toFile(List<E>, Class<E>)`：Bean -> E 文本字符串
- `toFile(List<E>, Class<E>, File)`：Bean -> 文件
- `toFile(List<ETable>, File)` / `toFile(EFileDocument, File)`：表/文档 -> 文件

### 1) 从 Bean 直接到文件（最简）

```java
import io.github.l4rue.cime.mapping.BeanUtils;

List<StandbyDbInfoBean> beans = ...;
BeanUtils.toFile(beans, StandbyDbInfoBean.class, new File("out-1.dt"));
```

### 2) 从 Bean 到 ETable，添加部分 attr，然后输出

```java
import io.github.l4rue.cime.write.WriteOptions;

List<StandbyDbInfoBean> beans = ...;
ETable table = BeanUtils.toTable(beans, StandbyDbInfoBean.class);
table.setTagName("StandbyDbInfo::铁心桥");
table.putAttribute("date", "2026-04-21");
table.putAttribute("DDMM", "达梦");

new DefaultEfileWrite().writeFile(
        java.util.Collections.singletonList(table),
        new File("out-2.dt"),
        WriteOptions.multiColumn()
);
```

### 3) 从 Bean 到 ETable，添加 attr，再到 Document，添加 Header，然后输出

```java
import io.github.l4rue.cime.model.EFileDocument;
import io.github.l4rue.cime.model.EHeader;

List<StandbyDbInfoBean> beans = ...;
ETable table = BeanUtils.toTable(beans, StandbyDbInfoBean.class);
table.setTagName("StandbyDbInfo::铁心桥");
table.putAttribute("date", "2026-04-21");

EFileDocument doc = new EFileDocument(java.util.Collections.singletonList(table));
EHeader header = new EHeader();
header.putAttribute("Entity", "铁心桥");
header.putAttribute("type", "示例");
header.putAttribute("dataTime", "20260421 10:00:00");
doc.setHeader(header);

new DefaultEfileWrite().writeFile(doc, new File("out-3.dt"));
```

### 4) 2 的升级版：多个 Bean 组成 `List<ETable>`

```java
List<StandbyDbInfoBean> standbyBeans = ...;
List<DeviceAlarmBean> alarmBeans = ...;

ETable standbyTable = BeanUtils.toTable(standbyBeans, StandbyDbInfoBean.class);
standbyTable.setTagName("StandbyDbInfo::铁心桥");
standbyTable.putAttribute("date", "2026-04-21");

ETable alarmTable = BeanUtils.toTable(alarmBeans, DeviceAlarmBean.class);
alarmTable.setTagName("DeviceAlarm::铁心桥");
alarmTable.putAttribute("date", "2026-04-21");

List<ETable> tables = java.util.Arrays.asList(standbyTable, alarmTable);
new DefaultEfileWrite().writeFile(tables, new File("out-4.dt"));
```

### 5) 3 的升级版：多个 Bean 组成 `List<ETable>`，再组装 `EFileDocument`

```java
List<ETable> tables = java.util.Arrays.asList(standbyTable, alarmTable);
EFileDocument doc = new EFileDocument(tables);

EHeader header = new EHeader();
header.putAttribute("Entity", "铁心桥");
header.putAttribute("type", "多表样例");
header.putAttribute("dataTime", "20260421 10:00:00");
doc.setHeader(header);

new DefaultEfileWrite().writeFile(doc, new File("out-5.dt"));
```

## ETable 写出到文件

使用 `DefaultEfileWrite` 可以把 `ETable` 写出为 UTF-8 编码的 E 文本文件。旧的 `List<ETable>` 写出入口默认输出横表式（`@`）：

```java
import io.github.l4rue.cime.write.DefaultEfileWrite;

DefaultEfileWrite writer = new DefaultEfileWrite();
writer.writeFile(tables, new File("out.dt"));
```

也可以显式选择单列式（`@@`）或多列式（`@#`）：

```java
import io.github.l4rue.cime.write.WriteOptions;

writer.writeFile(tables, new File("out-single.dt"), WriteOptions.singleColumn());
writer.writeFile(tables, new File("out-multi.dt"), WriteOptions.multiColumn());
```

非横表式没有类型、单位、限值的承载位置，因此当 `types`、`units` 或 `limitValues` 存在时，写成单列式或多列式会自动丢弃这些字段。

## Parse-Write Round-Trip

如果需要解析后尽量按原始结构写回，使用文档级入口。该入口会写回文件头、完整标签名、标签属性，并优先保留解析到的表体格式：

```java
import io.github.l4rue.cime.model.EFileDocument;
import io.github.l4rue.cime.parse.DefaultEfileParse;
import io.github.l4rue.cime.write.DefaultEfileWrite;
import io.github.l4rue.cime.write.WriteOptions;

DefaultEfileParse parser = new DefaultEfileParse();
EFileDocument document = parser.parseDocument(new File("data/多列式.txt"));

DefaultEfileWrite writer = new DefaultEfileWrite();
writer.writeFile(document, new File("out.dt"));

// 等价显式写法：
writer.writeFile(document, new File("out.dt"), WriteOptions.preserveSourceLayout());
```

示例输入：

```text
<! Entity=铁心桥 type=测试2011-11-03 dataTime='20120423 13:30:07' !>
<DG::铁心桥 date='2012-04-23' DDMM='达梦' >
```

写出时会继续保留：

```text
<! Entity=铁心桥 type=测试2011-11-03 dataTime='20120423 13:30:07' !>
<DG::铁心桥 date='2012-04-23' DDMM='达梦'>
```

当前 round-trip 保证结构和元数据不丢失；表体行的空格、表头行显示值等格式细节会按 writer 的规范化格式重新生成。

## 测试

仓库已补齐样例驱动测试，覆盖三种表格式、引号切分、文件头解析、标签属性解析、异常策略、Bean 映射和文档级写回。

```bash
mvn test
```

如果本地提示 `JAVA_HOME` 未配置，请先设置到 Java 8 运行环境后再执行。
