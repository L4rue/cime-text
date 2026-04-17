# CIME 包结构迁移清单

## TODOs

- [x] 更新 Maven 坐标到 `io.github.l4rue:cime-text`
- [x] 迁移注解层到 `io.github.l4rue.cime.annotation`
- [x] 迁移公共 parse/model API 到 `io.github.l4rue.cime.parse` 与 `io.github.l4rue.cime.model`
- [x] 迁移映射层到 `io.github.l4rue.cime.mapping` 与 `io.github.l4rue.cime.internal.mapping`
- [x] 迁移 `DefaultEfileParse` 到公开 `io.github.l4rue.cime.parse` 并同步 README 与调用点
- [x] 迁移 `com.edom.*` 与内部 `com.util.*` 到 `io.github.l4rue.cime.internal.*`
- [x] 镜像迁移测试包并修正测试 import
- [ ] 执行全局校验并清理旧包名残留引用

## Final Verification Wave

- [ ] F1 包结构与作用域审查通过
- [ ] F2 Maven 编译与测试通过
- [ ] F3 旧包名残留搜索与 README/API 一致性验证通过
- [ ] F4 独立复核结论为 APPROVE

## 1. 目标基线

### Maven 坐标
- groupId: `io.github.l4rue`
- artifactId: `cime-text`

### 根包
- root package: `io.github.l4rue.cime`

### 目标一级分层
- `io.github.l4rue.cime.parse`
- `io.github.l4rue.cime.build`
- `io.github.l4rue.cime.model`
- `io.github.l4rue.cime.annotation`
- `io.github.l4rue.cime.mapping`
- `io.github.l4rue.cime.io`
- `io.github.l4rue.cime.internal.*`

## 2. 迁移原则

### 公开 API
这些包未来应视为稳定公共边界：
- `parse`
- `build`
- `model`
- `annotation`
- `mapping`
- `io`（如果后续提供统一文件/流入口）

### 内部实现
这些都应收敛到 `internal`，不作为对外品牌暴露：
- 现有 `com.edom.*`
- 现有 `com.util` 中仅服务于解析实现的工具
- 现有 `com.efile.impl.*`

### 命名注意
当前存在一个可接受但需要明确认知的同名情况：
- 模型类：`io.github.l4rue.cime.model.ETable`
- 注解类：`io.github.l4rue.cime.annotation.ETable`

这在 Java 里合法，但重构时要小心 import。

## 3. 主源码迁移清单

### A. 注解层

#### 1) `com.annotation.ETable`
- 旧文件：`src/main/java/com/annotation/ETable.java`
- 新包：`io.github.l4rue.cime.annotation`
- 新全名：`io.github.l4rue.cime.annotation.ETable`
- 公开性：保持公开

#### 2) `com.annotation.EColumn`
- 旧文件：`src/main/java/com/annotation/EColumn.java`
- 新包：`io.github.l4rue.cime.annotation`
- 新全名：`io.github.l4rue.cime.annotation.EColumn`
- 公开性：保持公开

### B. 解析公共 API

#### 3) `com.efile.EFileParse`
- 旧文件：`src/main/java/com/efile/EFileParse.java`
- 新包：`io.github.l4rue.cime.parse`
- 新全名：`io.github.l4rue.cime.parse.EFileParse`
- 公开性：保持公开
- 备注：后续如果决定统一 API 风格，可以再演进为 `CimeParser`，但不是这一步必须做的。

#### 4) `com.efile.ParseOptions`
- 旧文件：`src/main/java/com/efile/ParseOptions.java`
- 新包：`io.github.l4rue.cime.parse`
- 新全名：`io.github.l4rue.cime.parse.ParseOptions`
- 公开性：保持公开

#### 5) `com.efile.MalformedRowPolicy`
- 旧文件：`src/main/java/com/efile/MalformedRowPolicy.java`
- 新包：`io.github.l4rue.cime.parse`
- 新全名：`io.github.l4rue.cime.parse.MalformedRowPolicy`
- 公开性：保持公开

### C. 模型层

#### 6) `com.efile.ETable`
- 旧文件：`src/main/java/com/efile/ETable.java`
- 新包：`io.github.l4rue.cime.model`
- 新全名：`io.github.l4rue.cime.model.ETable`
- 公开性：保持公开
- 说明：这是 parse 和 builder 共享的核心领域模型。

### D. 映射层

#### 7) `com.util.BeanUtils`
- 旧文件：`src/main/java/com/util/BeanUtils.java`
- 新包：`io.github.l4rue.cime.mapping`
- 新全名：`io.github.l4rue.cime.mapping.BeanUtils`
- 公开性：保持公开
- 说明：它已经是 README 暗示的用户能力之一，不应塞回 internal。

#### 8) `com.util.ColumnRecord`
- 旧文件：`src/main/java/com/util/ColumnRecord.java`
- 新包：`io.github.l4rue.cime.internal.mapping`
- 新全名：`io.github.l4rue.cime.internal.mapping.ColumnRecord`
- 公开性：转内部
- 说明：它只是 `BeanUtils` 的内部辅助类型，不应继续公开。

### E. 解析实现层

#### 9) `com.efile.impl.DefaultEfileParse`
- 旧文件：`src/main/java/com/efile/impl/DefaultEfileParse.java`
- 新包：`io.github.l4rue.cime.parse`
- 新全名：`io.github.l4rue.cime.parse.DefaultEfileParse`
- 公开性：保持公开
- 说明：README 里实际示例就是直接 new 它，它已经是事实上的公开实现类；因此不建议塞进 `internal.parse`。

### F. 内部 DOM / SAX / Reader 支撑层

#### 10) `com.edom.DocumentException`
- 旧文件：`src/main/java/com/edom/DocumentException.java`
- 新包：`io.github.l4rue.cime.internal.document`
- 新全名：`io.github.l4rue.cime.internal.document.DocumentException`

#### 11) `com.edom.DocumentHelper`
- 旧文件：`src/main/java/com/edom/DocumentHelper.java`
- 新包：`io.github.l4rue.cime.internal.document`
- 新全名：`io.github.l4rue.cime.internal.document.DocumentHelper`

#### 12) `com.edom.dom.StandardDocument`
- 新包：`io.github.l4rue.cime.internal.dom`
- 新全名：`io.github.l4rue.cime.internal.dom.StandardDocument`

#### 13) `com.edom.dom.StandardElement`
- 新包：`io.github.l4rue.cime.internal.dom`
- 新全名：`io.github.l4rue.cime.internal.dom.StandardElement`

#### 14) `com.edom.dom.StandardAttr`
- 新包：`io.github.l4rue.cime.internal.dom`
- 新全名：`io.github.l4rue.cime.internal.dom.StandardAttr`

#### 15) `com.edom.dom.StandardNamedNodeMap`
- 新包：`io.github.l4rue.cime.internal.dom`
- 新全名：`io.github.l4rue.cime.internal.dom.StandardNamedNodeMap`

#### 16) `com.edom.dom.StandardAttributes`
- 新包：`io.github.l4rue.cime.internal.dom`
- 新全名：`io.github.l4rue.cime.internal.dom.StandardAttributes`

#### 17) `com.edom.dom.StandardNodeList`
- 新包：`io.github.l4rue.cime.internal.dom`
- 新全名：`io.github.l4rue.cime.internal.dom.StandardNodeList`

#### 18) `com.edom.io.EdomSAXReader`
- 新包：`io.github.l4rue.cime.internal.sax`
- 新全名：`io.github.l4rue.cime.internal.sax.EdomSAXReader`

#### 19) `com.edom.io.EdomSAXContentHandler`
- 新包：`io.github.l4rue.cime.internal.sax`
- 新全名：`io.github.l4rue.cime.internal.sax.EdomSAXContentHandler`

#### 20) `com.edom.parser.EdomXMLReader`
- 新包：`io.github.l4rue.cime.internal.sax`
- 新全名：`io.github.l4rue.cime.internal.sax.EdomXMLReader`

#### 21) `com.edom.parsers.DdomSAXParsers`
- 新包：`io.github.l4rue.cime.internal.sax`
- 新全名：`io.github.l4rue.cime.internal.sax.DdomSAXParsers`

### G. 内部工具层

#### 22) `com.edom.util.ArrayStack`
- 新包：`io.github.l4rue.cime.internal.util`
- 新全名：`io.github.l4rue.cime.internal.util.ArrayStack`

#### 23) `com.edom.util.Ascii`
- 新包：`io.github.l4rue.cime.internal.util`
- 新全名：`io.github.l4rue.cime.internal.util.Ascii`

#### 24) `com.edom.util.CharChunk`
- 新包：`io.github.l4rue.cime.internal.util`
- 新全名：`io.github.l4rue.cime.internal.util.CharChunk`

#### 25) `com.edom.util.ElementStack`
- 新包：`io.github.l4rue.cime.internal.util`
- 新全名：`io.github.l4rue.cime.internal.util.ElementStack`

#### 26) `com.edom.util.StringCache`
- 新包：`io.github.l4rue.cime.internal.util`
- 新全名：`io.github.l4rue.cime.internal.util.StringCache`

#### 27) `com.util.StringUtils`
- 新包：`io.github.l4rue.cime.internal.util`
- 新全名：`io.github.l4rue.cime.internal.util.StringUtils`
- 说明：目前它主要服务解析实现，不建议保留为公共 API。

#### 28) `com.util.Debug`
- 新包：`io.github.l4rue.cime.internal.util`
- 新全名：`io.github.l4rue.cime.internal.util.Debug`

#### 29) `com.util.EfileUtils`
- 新包：`io.github.l4rue.cime.internal.io`
- 新全名：`io.github.l4rue.cime.internal.io.EfileUtils`
- 说明：这个类做文件读取、编码探测、UTF-8 转换，更接近 internal io，而不是 general util。

## 4. 测试代码迁移清单

测试不要统一扔进 `internal.test`，正确做法是镜像主源码包结构。

#### 30) `com.efile.impl.DefaultEfileParseTest`
- 旧文件：`src/test/java/com/efile/impl/DefaultEfileParseTest.java`
- 新包：`io.github.l4rue.cime.parse`
- 新全名：`io.github.l4rue.cime.parse.DefaultEfileParseTest`

#### 31) `com.util.BeanUtilsTest`
- 旧文件：`src/test/java/com/util/BeanUtilsTest.java`
- 新包：`io.github.l4rue.cime.mapping`
- 新全名：`io.github.l4rue.cime.mapping.BeanUtilsTest`

#### 32) `com.util.StringUtilsTest`
- 旧文件：`src/test/java/com/util/StringUtilsTest.java`
- 新包：`io.github.l4rue.cime.internal.util`
- 新全名：`io.github.l4rue.cime.internal.util.StringUtilsTest`

## 5. 尚未存在但应预留的包

虽然 builder 还没实现，但为了后续稳定演进，建议现在就预留命名：

- `io.github.l4rue.cime.build`
  - 未来放：`CimeBuilder` / `EFileBuilder`、`BuildOptions`、文本输出能力

- `io.github.l4rue.cime.io`
  - 如果未来想提供统一读写 facade，可放：`CimeFiles`、`CimeReaders`、`CimeWriters`

## 6. 自主重构时的执行顺序

### Phase 1：先改 Maven 坐标
1. `pom.xml`
   - `groupId -> io.github.l4rue`
   - `artifactId -> cime-text`

### Phase 2：先迁公共 API 包
2. `annotation`
3. `model`
4. `parse`
5. `mapping`

原因：先稳定公开边界，后面 internal 的 import 才好跟着收口。

### Phase 3：再迁解析实现和 internal
6. `DefaultEfileParse`
7. `com.edom.* -> internal.document / internal.dom / internal.sax / internal.util`
8. `com.util` 中非公开类迁入 `internal.*`

### Phase 4：镜像迁测试
9. 测试包随主包同步迁移
10. 修正测试 import

### Phase 5：全局校验
11. 编译
12. 测试
13. 搜索残留旧包名 `com.annotation|com.efile|com.edom|com.util`

## 7. 这次重构里建议同时做的轻量规范化

建议保留原类名，暂不激进改名，先做 package 重构，不要一口气改：
- `EFileParse`
- `DefaultEfileParse`
- `ETable`

因为“改包 + 改类名”叠在一起，风险会明显变高。

但有两个点要记在后续演进计划里：
- `BeanUtils` 将来可考虑改成更语义化的 `TableMapper`
- `EFileParse` / `DefaultEfileParse` 将来可考虑演进到 `CimeParser` / `DefaultCimeParser`

建议：这一步只改包，不改公开类名。

## 8. 最终迁移总表

| 旧全名 | 新全名 |
|---|---|
| `com.annotation.ETable` | `io.github.l4rue.cime.annotation.ETable` |
| `com.annotation.EColumn` | `io.github.l4rue.cime.annotation.EColumn` |
| `com.efile.EFileParse` | `io.github.l4rue.cime.parse.EFileParse` |
| `com.efile.ParseOptions` | `io.github.l4rue.cime.parse.ParseOptions` |
| `com.efile.MalformedRowPolicy` | `io.github.l4rue.cime.parse.MalformedRowPolicy` |
| `com.efile.ETable` | `io.github.l4rue.cime.model.ETable` |
| `com.efile.impl.DefaultEfileParse` | `io.github.l4rue.cime.parse.DefaultEfileParse` |
| `com.util.BeanUtils` | `io.github.l4rue.cime.mapping.BeanUtils` |
| `com.util.ColumnRecord` | `io.github.l4rue.cime.internal.mapping.ColumnRecord` |
| `com.edom.DocumentException` | `io.github.l4rue.cime.internal.document.DocumentException` |
| `com.edom.DocumentHelper` | `io.github.l4rue.cime.internal.document.DocumentHelper` |
| `com.edom.dom.StandardDocument` | `io.github.l4rue.cime.internal.dom.StandardDocument` |
| `com.edom.dom.StandardElement` | `io.github.l4rue.cime.internal.dom.StandardElement` |
| `com.edom.dom.StandardAttr` | `io.github.l4rue.cime.internal.dom.StandardAttr` |
| `com.edom.dom.StandardNamedNodeMap` | `io.github.l4rue.cime.internal.dom.StandardNamedNodeMap` |
| `com.edom.dom.StandardAttributes` | `io.github.l4rue.cime.internal.dom.StandardAttributes` |
| `com.edom.dom.StandardNodeList` | `io.github.l4rue.cime.internal.dom.StandardNodeList` |
| `com.edom.io.EdomSAXReader` | `io.github.l4rue.cime.internal.sax.EdomSAXReader` |
| `com.edom.io.EdomSAXContentHandler` | `io.github.l4rue.cime.internal.sax.EdomSAXContentHandler` |
| `com.edom.parser.EdomXMLReader` | `io.github.l4rue.cime.internal.sax.EdomXMLReader` |
| `com.edom.parsers.DdomSAXParsers` | `io.github.l4rue.cime.internal.sax.DdomSAXParsers` |
| `com.edom.util.ArrayStack` | `io.github.l4rue.cime.internal.util.ArrayStack` |
| `com.edom.util.Ascii` | `io.github.l4rue.cime.internal.util.Ascii` |
| `com.edom.util.CharChunk` | `io.github.l4rue.cime.internal.util.CharChunk` |
| `com.edom.util.ElementStack` | `io.github.l4rue.cime.internal.util.ElementStack` |
| `com.edom.util.StringCache` | `io.github.l4rue.cime.internal.util.StringCache` |
| `com.util.StringUtils` | `io.github.l4rue.cime.internal.util.StringUtils` |
| `com.util.Debug` | `io.github.l4rue.cime.internal.util.Debug` |
| `com.util.EfileUtils` | `io.github.l4rue.cime.internal.io.EfileUtils` |
| `com.efile.impl.DefaultEfileParseTest` | `io.github.l4rue.cime.parse.DefaultEfileParseTest` |
| `com.util.BeanUtilsTest` | `io.github.l4rue.cime.mapping.BeanUtilsTest` |
| `com.util.StringUtilsTest` | `io.github.l4rue.cime.internal.util.StringUtilsTest` |

## 9. 一句话结论

这份迁移方案的核心是：
- 对外只暴露 `parse / model / annotation / mapping`
- 把历史实现痕迹 `com.edom.*` 和大部分 `com.util.*` 全部收口进 `internal`
- 把 `DefaultEfileParse` 留在公开 parse 包里
- builder 包现在预留，不强造实现
