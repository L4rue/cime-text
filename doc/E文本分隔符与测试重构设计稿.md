# E 文本分隔符与测试重构设计稿

## 一、重构目标

本次重构围绕两个核心目标展开：

1. **统一分隔符规则**：空格和 Tab 都作为分隔符，不做优先级判断。
2. **完善测试体系**：审查并修改测试代码，覆盖分词与解析的全部主要分支。

本设计坚持一个明确原则：

> 当字段内部包含空格时，只有使用单引号或双引号包裹，才允许整体保留；如果未加引号，则直接按空格 / Tab 切开，再由上层字段数校验进入异常或跳过逻辑。

---

## 二、现状与问题

### 2.1 当前调用链

当前解析调用链如下：

```text
DefaultEfileParse.parseFile
  -> parseTableData
    -> parseRowType / parseSingleColType / parseMultiColType
      -> splitString
        -> StringUtils.splitLineWithSpace
```

也就是说，三种表格式：

- 横表式 `@`
- 单列式 `@@`
- 多列式 `@#`

最终都依赖 `StringUtils.splitLineWithSpace` 完成字段切分。

### 2.2 当前设计上的核心矛盾

从项目注释、测试和样例文件来看，项目原始契约一直是：

- 空格可以作为分隔符
- Tab 也可以作为分隔符
- 引号用于保留字段内部空格

但历史上 `StringUtils.isSpace()` 曾经被改成只识别 `\t`、`\n`、`\r` 等空白，导致：

- 空格分隔样例被错误解析
- 分词规则与测试、文档、文件样例不一致

当前工作区中 `isSpace()` 已经恢复为把普通空格 `' '` 也视为空白，这是正确方向，但整体重构还未完成，测试也存在回归。

### 2.3 当前测试的实际问题

现有测试虽然已经覆盖了部分成功路径，但存在以下问题：

1. skip 模式测试被写坏，`shouldSkipMalformedRowWhenConfigured()` 当前实际调用的是默认 `parseFile(file)`，没有显式传入 `ParseOptions.skipMalformedRows()`。
2. `StringUtilsTest` 缺少 null、Tab、混合空白、双引号等关键分支。
3. `DefaultEfileParseTest` 对单列/多列/横表三条分支的 malformed 场景覆盖不完整。

---

## 三、重构范围

### 3.1 生产代码

涉及的主要文件：

- `src/main/java/com/util/StringUtils.java`
- `src/main/java/com/efile/impl/DefaultEfileParse.java`
- 可选：`README.md`（补充规则说明）

### 3.2 测试代码

涉及的主要文件：

- `src/test/java/com/util/StringUtilsTest.java`
- `src/test/java/com/efile/impl/DefaultEfileParseTest.java`
- `src/test/java/com/util/BeanUtilsTest.java`（回归验证，不作为重构主战场）

---

## 四、统一分词规则设计

## 4.1 分词契约

`StringUtils.splitLineWithSpace(String line)` 的新契约定义如下：

### 输入

- 任意一行字段文本
- 上层已经截掉表体 marker，例如 `@`、`#`、`%`、`$`、`:`

### 输出

返回字段数组，规则如下：

1. **空格和 Tab 都作为分隔符**，此外 `\n`、`\r`、`\f` 也作为空白处理。
2. 连续多个空白视为一个分隔区。
3. 被单引号 `'...'` 或双引号 `"..."` 包裹的内容保留为一个字段。
4. 引号本身不进入结果。
5. 未闭合引号抛出 `IllegalArgumentException("存在未闭合的引号")`。
6. `null` 输入返回空数组。
7. 全空白输入返回空数组。

### 设计结论

> 分词器只负责“按规则切分”，不负责猜测业务字段边界。

这意味着：

- 如果日期时间字段使用引号包裹，则整体保留。
- 如果日期时间字段未使用引号包裹，则会被直接切开。
- 被切开后，由上层解析逻辑根据字段数是否匹配决定抛错或跳过。

---

## 4.2 示例

### 示例 1：普通空格分隔

```text
A B C
```

结果：

```text
["A", "B", "C"]
```

### 示例 2：Tab 分隔

```text
A\tB\tC
```

结果：

```text
["A", "B", "C"]
```

### 示例 3：混合空白分隔

```text
A \t B   C
```

结果：

```text
["A", "B", "C"]
```

### 示例 4：引号保留内部空格

```text
2 发生时间 '2011-11-03 00:00:02.0'
```

结果：

```text
["2", "发生时间", "2011-11-03 00:00:02.0"]
```

### 示例 5：未加引号，直接切开

```text
# 1 时间 2019-11-5 11:25
```

上层去掉 `#` 后，分词结果：

```text
["1", "时间", "2019-11-5", "11:25"]
```

随后进入字段数校验阶段，如果列数不匹配，则：

- 严格模式抛异常
- skip 模式跳过该行

---

## 五、生产代码重构方案

## 5.1 `StringUtils` 重构方案

### 保留接口

保留现有公开方法：

- `splitLineWithSpace(String line)`

这样可以避免扩大 API 变更范围。

### 修改点

#### 1）固定 `isSpace` 契约

最终实现固定为：

```java
return chr == ' ' || chr == '\n' || chr == '\t' || chr == '\r' || chr == '\f';
```

不要保留旧实现作为注释切换项，避免未来再次出现语义漂移。

#### 2）更新注释

原注释“以 space 分割”已经不足以准确表达现有规则，应更新为：

> 按空白字符（空格、Tab 等）分割；引号包裹字段时保留内部空格。

#### 3）保留异常边界

`StringUtils` 只负责以下错误：

- 单引号未闭合
- 双引号未闭合

它不负责判断字段数量是否正确，也不负责判断字段业务含义。

---

## 5.2 `DefaultEfileParse` 重构方案

### 总体原则

不新增“自动猜测是空格还是 Tab”的逻辑，不引入优先级判断。

统一规则为：

> 空格和 Tab 都是等价分隔符。

因此：

- 合法引号字段被整体保留
- 未加引号的带空格字段被切开
- 被切开后由上层字段校验进入 malformed 路线

这正好符合本次设计目标。

---

## 5.3 横表式 `parseRowType`

### 目标行为

- 表头 `@` 行字段数必须大于 0
- `%`、`$`、`:`、`#` 行字段数必须与表头一致

### 关键设计点

如果日期字段未加引号，例如：

```text
# 1 设备A 2019-11-5 11:25 100
```

分词后可能得到 5 列而不是预期 4 列，于是进入：

```text
横表式字段数量不匹配
```

然后：

- 严格模式抛错
- skip 模式跳过

### 结论

不需要在横表式中增加专门“日期字段校验”，字段数校验已经足够承接该类错误输入。

---

## 5.4 单列式 `parseSingleColType`

单列式当前支持两种头部：

- 2 列：属性名 + 属性值
- 3 列：顺序 + 属性名 + 属性值

### 目标行为

- 头部字段数必须为 2 或 3
- 每一行字段数必须与头部一致
- 属性名不能为空
- 记录字段集必须一致

### 关键设计点

如果属性值包含空格但未加引号，例如：

```text
#2 发生时间 2011-11-03 00:00:02.0
```

会被切成：

```text
["2", "发生时间", "2011-11-03", "00:00:02.0"]
```

然后进入：

```text
单列式字段数量不匹配
```

保持现有错误路径即可。

---

## 5.5 多列式 `parseMultiColType`

### 目标行为

- 每个 `#` 行至少需要 3 列
- 所有 `#` 行字段数必须一致
- 第二列列名不能为空
- 至少存在一行有效 `#` 数据行

### 关键设计点

如果日期字段未加引号而内部含空格，则会导致：

- 字段数变多
- 与其他行字段数不一致

最终进入：

```text
多列式字段数量不匹配
```

或：

```text
多列式字段数量不足
```

这符合本次“未加引号则直接切开，再由上层异常处理”的设计目标。

---

## 5.6 异常 / catch 路线设计

当前 `splitString()` 已具备异常包装能力：

```java
try {
    return StringUtils.splitLineWithSpace(str);
} catch (IllegalArgumentException ex) {
    throw parseException(table.getTableName(), lineNo, ex.getMessage());
}
```

因此本次重构明确分为两条错误路径：

### 路径 A：分词器直接抛错

例如：

- 单引号未闭合
- 双引号未闭合

链路：

```text
StringUtils -> IllegalArgumentException -> splitString catch -> parseException
```

### 路径 B：分词成功，但结构非法

例如：

- 未加引号的日期字段被切开
- 导致字段数超出预期

链路：

```text
splitString 正常返回 -> parseRowType / parseSingleColType / parseMultiColType 校验字段数 -> shouldSkipMalformedRow
```

### 最终要求

两条路径都必须通过测试明确覆盖，不能混淆。

---

## 六、测试审查结论

## 6.1 已有覆盖

### `StringUtilsTest`

当前已覆盖：

- 普通空格分隔
- 引号保留内部空格
- 未闭合引号异常

### `DefaultEfileParseTest`

当前已覆盖：

- 横表式成功路径
- 单列式成功路径
- 多列式成功路径
- 首行无 `@` 异常
- malformed 行的异常路径
- malformed 行的 skip 路径（但当前测试实现写坏）

---

## 6.2 现有缺口

### `StringUtilsTest` 缺失

- `null` 输入
- 全空白输入
- Tab 分隔
- 混合空白分隔
- 双引号包裹字段
- 连续分隔符压缩场景

### `DefaultEfileParseTest` 缺失

- `file == null`
- `options == null`
- 表体为空
- 单列式头部字段数非法
- 单列式数据字段数不匹配
- 单列式属性名为空
- 多列式字段数不足
- 多列式字段数不一致
- 多列式列名为空
- 多列式无有效数据行
- 横表式 marker 后无字段
- 横表式字段数不匹配
- 分词异常 wrap
- skip 模式在不同 malformed 类型下的表现
- “未加引号的日期被切开”这一重构核心场景

---

## 七、测试重构设计

## 7.1 `StringUtilsTest` 设计

建议按三组整理：

### A. 基础分词组

- `shouldReturnEmptyArrayWhenInputIsNull`
- `shouldReturnEmptyArrayWhenInputIsBlank`
- `shouldTreatSpaceAsSeparator`
- `shouldTreatTabAsSeparator`
- `shouldTreatMixedWhitespaceAsSeparator`

### B. 引号保留组

- `shouldKeepSingleQuotedToken`
- `shouldKeepDoubleQuotedToken`
- `shouldTrimOuterWhitespaceAroundQuotedToken`

### C. 异常组

- `shouldThrowWhenSingleQuoteNotClosed`
- `shouldThrowWhenDoubleQuoteNotClosed`

---

## 7.2 `DefaultEfileParseTest` 必修项

### 先修正错误测试

把当前写坏的：

```java
List<ETable> tables = parser.parseFile(file);
```

改回：

```java
List<ETable> tables = parser.parseFile(file, ParseOptions.skipMalformedRows());
```

否则 skip 模式根本没有被真正测试。

---

## 7.3 `DefaultEfileParseTest` 测试矩阵

### A. 成功路径

- `shouldParseRowTypeSample`
- `shouldParseSingleColumnSample`
- `shouldParseMultiColumnSample`
- `shouldParseTabSeparatedQuotedDatetimeSample`
- `shouldUseStrictModeWhenOptionsIsNull`

### B. 入口与边界

- `shouldThrowWhenFileIsNull`
- `shouldThrowWhenBodyIsEmpty`
- `shouldThrowWhenHeaderGuideMissing`

### C. 横表式异常 / skip

- `shouldThrowWhenRowTypeDataColumnCountMismatchInStrictMode`
- `shouldSkipRowTypeMalformedDataWhenConfigured`
- `shouldThrowWhenRowTypeMarkerWithoutFields`
- `shouldWrapTokenizerErrorForRowType`

### D. 单列式异常 / skip

- `shouldThrowWhenSingleColumnHeaderFieldCountInvalid`
- `shouldThrowWhenSingleColumnRowFieldCountMismatchInStrictMode`
- `shouldSkipSingleColumnMalformedRowWhenConfigured`
- `shouldThrowWhenSingleColumnPropertyNameIsEmpty`
- `shouldWrapTokenizerErrorForSingleColumn`

### E. 多列式异常 / skip

- `shouldThrowWhenMultiColumnRowHasTooFewFields`
- `shouldThrowWhenMultiColumnRowCountMismatchInStrictMode`
- `shouldSkipMultiColumnMalformedRowWhenConfigured`
- `shouldThrowWhenMultiColumnColumnNameIsEmpty`
- `shouldThrowWhenMultiColumnHasNoValidDataRows`
- `shouldWrapTokenizerErrorForMultiColumn`

### F. 本次重构核心回归组：未加引号日期被切开

- `shouldThrowWhenRowTypeDatetimeContainsSpaceWithoutQuotesInStrictMode`
- `shouldSkipRowTypeDatetimeContainsSpaceWithoutQuotesWhenConfigured`
- `shouldThrowWhenSingleColumnDatetimeContainsSpaceWithoutQuotesInStrictMode`
- `shouldThrowWhenMultiColumnDatetimeContainsSpaceWithoutQuotesInStrictMode`

这组测试要明确验证：

> 不加引号的带空格字段不会被特殊照顾，而是直接切开，随后由字段数校验进入异常或跳过分支。

---

## 八、测试数据组织建议

### 保留现有样例文件

继续使用：

- `data/横表式.txt`
- `data/单列式.txt`
- `data/多列式.txt`
- `data/LP_K8000_STAdfgYDB_20191105_113002.DT`

### 补充临时数据工厂

建议在测试类中统一使用小型辅助方法：

- `createTempEFile(String content)`
- `parseSingleTable(String content)`
- `assertParseErrorContains(String content, String expectedMessagePart)`

这样可以让测试更聚焦于分支本身，而不是重复样板代码。

---

## 九、实施顺序

### 第 1 步：收口分词规则

只改 `StringUtils`：

- 固定空格 + Tab 都是分隔符
- 固定引号保留字段语义
- 清理历史歧义注释

### 第 2 步：修复测试回归

优先修正 `shouldSkipMalformedRowWhenConfigured()`

### 第 3 步：补齐 `StringUtilsTest`

先让底层分词契约稳定

### 第 4 步：补齐 `DefaultEfileParseTest`

按照：

- 成功路径
- throw 路径
- skip 路径
- 分词异常 wrap 路径

逐层补全

### 第 5 步：回归执行

运行完整测试，确保以下维度全部被覆盖：

- 分词成功
- 分词失败
- 结构校验失败
- skip 策略生效
- 三种表格式主链路稳定

---

## 十、预期收益

本次重构完成后，将获得以下收益：

1. **规则统一**
   - 空格和 Tab 一视同仁
   - 不再存在“到底是空格分隔还是 Tab 分隔”的实现摇摆

2. **错误归因清晰**
   - 未闭合引号：分词错误
   - 未加引号导致字段裂开：结构错误

3. **异常路径稳定**
   - 分词器错误走 catch / wrap 路线
   - 字段数错误走 malformed 策略路线

4. **测试契约完整**
   - 后续再改分词或解析逻辑时，不会轻易破坏另一类格式

5. **行为符合输入约束**
   - 合法引用字段被保留
   - 非法未引用字段不被“猜测性修正”，而是明确报错或跳过

---

## 十一、结论

本次重构不应再尝试“猜测某一行更像空格分隔还是 Tab 分隔”，而应明确建立统一契约：

> 空格和 Tab 都是分隔符；带空格字段必须通过引号保留；否则直接切开，并由上层解析逻辑根据字段数进入 throw / skip 路线。

这是与当前数据形态、解析结构、异常机制和测试目标最一致的方案。
