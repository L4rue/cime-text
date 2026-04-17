# E文本解析实现说明

本文档说明当前项目中 E 文本文件的解析实现，以及从原始文件到 Java 对象的完整处理流程。

## 1. 解析入口

E 文本解析的统一入口是 `com.efile.EFileParse` 接口：

```java
List<ETable> parseFile(File file) throws Exception;
```

当前默认实现类是 `com.efile.impl.DefaultEfileParse`。调用方式可以参考 `com.MainTest`：

```java
DefaultEfileParse parse = new DefaultEfileParse();
List<ETable> list = parse.parseFile(new File("data/LP_K8000_STAdfgYDB_20191105_113002.DT"));
```

如果需要进一步转换成业务对象，再通过 `com.util.BeanUtils.parseBean(...)` 按注解做字段映射。

## 2. 总体解析流程

完整流程如下：

1. 调用 `DefaultEfileParse.parseFile(File file)`。
2. 使用 `EdomSAXReader.read(file)` 读取 E 文本文件。
3. `EdomSAXReader` 根据 `EfileUtils.getCharset(file)` 判断文件编码，并设置到 `InputSource`。
4. `EdomSAXReader` 使用自定义 `DdomSAXParsers` 创建 `EdomXMLReader`。
5. `EdomXMLReader` 按字符流扫描文件，触发 SAX 事件。
6. `EdomSAXContentHandler` 接收 SAX 事件并构建 W3C DOM `Document`。
7. `DefaultEfileParse` 遍历 `Document` 的一级节点，每个一级节点对应一个 E 表。
8. 为每个节点创建 `ETable`，解析表名、日期和文本内容。
9. 根据表体第一行前缀识别表格式，并填充列名、类型、单位、限值和数据行。
10. 返回 `List<ETable>`。
11. 可选：使用 `BeanUtils.parseBean(ETable, Class<E>)` 将 `ETable` 转换为业务对象列表。

简化链路：

```text
File
  -> EdomSAXReader
  -> EdomXMLReader
  -> Document
  -> DefaultEfileParse
  -> ETable
  -> BeanUtils.parseBean
  -> List<业务对象>
```

## 3. 文件读取与 DOM 构建

`EdomSAXReader.read(File file)` 负责把文件转成 DOM：

1. 打开 `FileInputStream`。
2. 创建 `InputSource`。
3. 调用 `EfileUtils.getCharset(file)` 识别编码，默认倾向 GBK，检测到 UTF-8 BOM 或 UTF-8 字节模式时使用 UTF-8。
4. 给 `InputSource` 设置编码和 `systemId`。
5. 获取自定义 `XMLReader`。
6. 创建 `EdomSAXContentHandler`。
7. 执行 `reader.parse(in)`。
8. 从 handler 中取回构建好的 `Document`。

`EdomXMLReader` 是项目自定义的 SAX `XMLReader`，主要逻辑在 `parse(InputSource input)`：

1. 初始化字符流，按 `InputSource` 编码读取文件。
2. 触发 `startDocument()`。
3. 跳过文件开头空白字符。
4. 逐字符读取文件内容。
5. 遇到 `//` 时按注释处理，跳过到换行。
6. 遇到 `<!` 时调用 `parseHeader()` 跳过文件头。
7. 遇到 `<tag ...>` 时调用 `parseStartElement()` 解析开始标签、标签体文本。
8. 遇到 `</tag>` 时调用 `parseEndElement()` 解析结束标签。
9. 触发 `endDocument()`。

`parseStartElement()` 负责读取标签名、标签属性内容和标签体文本，并通过 SAX 事件交给 content handler 构建 DOM。

注意：当前代码中 `parseStartElement()` 里只有在 `attrs.getLength() > 0` 时才调用 `parseAttributes(...)`，但新建的 `StandardAttributes` 初始长度为 0，因此实际可能不会解析标签属性。`DefaultEfileParse` 仍然会读取 `ele.getAttribute("date")`，但如果属性未进入 DOM，`date` 会为空。若后续需要保证日期字段可用，可以把判断改成基于 `elmentTagContent` 是否为空。

## 4. ETable 数据结构

`com.efile.ETable` 是解析后的中间模型：

| 字段 | 含义 |
| --- | --- |
| `tableName` | 表名，来自 DOM 一级节点的标签名 |
| `date` | 日期属性，来自标签的 `date` 属性 |
| `columnNames` | 列名数组 |
| `types` | 类型数组，主要由横表式 `%` 行提供 |
| `unites` | 单位数组，主要由横表式 `$` 行提供 |
| `limitValues` | 限定值数组，主要由横表式 `:` 行提供 |
| `datas` | 数据行列表，每行是一个 `Object[]` |

`DefaultEfileParse.parseFile(...)` 遍历 `Document` 子节点时，每个节点会创建一个 `ETable`：

1. 标签名包含 `::` 时，取 `::` 前面的部分作为表名。
2. 否则直接使用标签名作为表名。
3. 读取 `date` 属性。
4. 读取节点文本内容，交给 `parseTableData(...)` 解析表体。

## 5. 表体格式识别

`parseTableData(ETable table, String content)` 会把表体按换行拆分：

```java
String[] contentArr = content.split("\n");
String headStr = contentArr[0].trim();
```

然后根据第一行前缀选择解析方式：

| 首行前缀 | 解析方式 | 方法 |
| --- | --- | --- |
| `@@` | 单列式 | `parseSingleColType(...)` |
| `@#` | 多列式 | `parseMultiColType(...)` |
| `@` | 横表式 | `parseRowType(...)` |

所有行内容切分最终都会调用 `StringUtils.splitLineWithSpace(...)`。

## 6. 字段切分规则

`StringUtils.splitLineWithSpace(String line)` 用于把一行 E 文本切分成字段数组。

当前规则：

1. 跳过换行、制表符、回车、换页等空白字符。
2. 支持单引号和双引号包裹字段。
3. 引号包裹的字段会保留内部内容，不把内部内容继续切分。
4. 未被引号包裹的字段按空白边界截取。

示例：

```text
2 发生时间 '2011-11-03 00:00:02.0'
```

会被切成字段数组，便于后续列名和数据行组装。

说明：当前 `StringUtils.isSpace(char chr)` 注释中提到空格，但实际返回值没有包含普通空格 `' '`，只包含 `\n`、`\t`、`\r`、`\f`。因此普通空格是否作为分隔符，取决于该方法的实际实现，后续维护时需要特别注意。

## 7. 三种表格式解析

### 7.1 横表式

横表式由首行 `@` 开头，解析方法是 `parseRowType(...)`。

流程：

1. 解析首行列名，写入 `table.columnNames`。
2. 遍历后续行。
3. `%` 行切分后写入类型数组 `types`。
4. `$` 行切分后写入单位数组 `unites`。
5. `:` 行切分后写入限定值数组 `limitValues`。
6. `#` 行切分后作为一行数据追加到 `table.datas`。

### 7.2 多列式

多列式由首行 `@#` 开头，解析方法是 `parseMultiColType(...)`。

流程：

1. 遍历表体第二行开始的内容。
2. 只处理 `#` 开头的行。
3. 去掉 `#` 后切分字段，并把每一行存入 `porpList`。
4. 每个 `#` 行代表一列的定义和值。
5. 每个定义行的第 2 个字段，即下标 `1`，作为列名。
6. 从每个定义行的第 3 个字段，即下标 `2`，开始按行组装数据。

示意：

```text
@# ...
# 1 列A A1 A2
# 2 列B B1 B2
```

解析结果：

```text
columnNames = ["列A", "列B"]
datas = [
  ["A1", "B1"],
  ["A2", "B2"]
]
```

### 7.3 单列式

单列式由首行 `@@` 开头，解析方法是 `parseSingleColType(...)`。

流程：

1. 解析首行，得到表头定义。
2. 从第二行开始遍历。
3. 只处理 `#` 开头的行。
4. 去掉 `#` 后切分字段。
5. 根据表头字段数量选择属性名和值的位置。
6. 使用 `LinkedHashMap<String, String>` 暂存一条记录中的属性和值，保持字段顺序。
7. 如果再次遇到已经存在的属性名，说明上一条记录结束。
8. 将上一条记录的 values 转成一行数据加入 `table.datas`。
9. 清空 map 后开始收集下一条记录。
10. 循环结束后，把最后一条记录写入 `table.datas`。
11. 使用 map 的 key 顺序作为 `columnNames`。

## 8. 转换为业务对象

`BeanUtils.parseBean(ETable eTable, Class<E> eClass)` 负责把解析后的 `ETable` 转成业务对象。

流程：

1. 判断业务类是否有 `@com.annotation.ETable` 注解。
2. 如果有，使用注解值作为目标表名；否则使用类名作为目标表名。
3. 校验目标表名和 `eTable.getTableName()` 是否一致，不一致则抛出异常。
4. 遍历业务类字段。
5. 找出带有 `@EColumn` 注解的字段。
6. 用 `@EColumn` 的值匹配 `ETable.columnNames`。
7. 记录字段和列下标的关系。
8. 遍历 `ETable.datas`，每一行创建一个业务对象。
9. 按列下标取值，通过反射设置到对应字段。
10. 返回业务对象列表。

## 9. 当前实现特点

1. 解析分为两层：先把 E 文本包装成 DOM，再把 DOM 文本内容转换成 `ETable`。
2. 表格式由表体第一行前缀决定，逻辑集中在 `DefaultEfileParse`。
3. `ETable` 是统一中间结构，便于后续转对象、打印或二次处理。
4. 对象转换依赖注解，业务类和 E 表之间通过表名、列名建立映射。
5. 文件编码识别在 `EfileUtils.getCharset(...)` 中完成，读取时会传给 SAX 输入源。
6. 当前解析器是轻量自定义实现，适合项目中的 E 文本格式，不等同于完整 XML 解析器。

## 10. 维护建议

1. 如果要补强标签属性解析，优先检查 `EdomXMLReader.parseStartElement()` 中 `parseAttributes(...)` 的调用条件。
2. 如果普通空格应作为字段分隔符，需要检查 `StringUtils.isSpace(...)` 是否应包含 `' '`。
3. 如果要支持嵌套标签，需要扩展 `EdomXMLReader.parseStartElement()` 中遇到正文内 `<` 后的处理逻辑。
4. 如果要支持非字符串字段类型，可以在 `BeanUtils.parseBean(...)` 中按字段类型做转换，而不是直接 `field.set(e, data[index])`。
