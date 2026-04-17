package com.efile.impl;

import com.efile.ETable;
import com.efile.ParseOptions;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class DefaultEfileParseTest {

    @Test
    public void shouldParseRowTypeSample() throws Exception {
        DefaultEfileParse parser = new DefaultEfileParse();
        List<ETable> tables = parser.parseFile(new File("data/横表式.txt"));

        Assert.assertEquals(1, tables.size());
        ETable table = tables.get(0);
        Assert.assertEquals("test", table.getTableName());
        Assert.assertEquals("2012-04-11 11:12", table.getDate());
        Assert.assertArrayEquals(new String[]{"顺序", "单位名称", "发生时间", "次数"}, table.getColumnNames());
        Assert.assertEquals(58, table.getDatas().size());
    }

    @Test
    public void shouldParseSingleColumnSample() throws Exception {
        DefaultEfileParse parser = new DefaultEfileParse();
        List<ETable> tables = parser.parseFile(new File("data/单列式.txt"));

        Assert.assertEquals(1, tables.size());
        ETable table = tables.get(0);
        Assert.assertEquals("DG", table.getTableName());
        Assert.assertEquals("2012-04-23", table.getDate());
        Assert.assertArrayEquals(new String[]{"单位名称", "发生时间", "次数"}, table.getColumnNames());
        Assert.assertEquals(4, table.getDatas().size());
    }

    @Test
    public void shouldParseMultiColumnSample() throws Exception {
        DefaultEfileParse parser = new DefaultEfileParse();
        List<ETable> tables = parser.parseFile(new File("data/多列式.txt"));

        Assert.assertEquals(1, tables.size());
        ETable table = tables.get(0);
        Assert.assertEquals("DG", table.getTableName());
        Assert.assertArrayEquals(new String[]{"单位名称", "发生时间", "次数"}, table.getColumnNames());
        Assert.assertEquals(4, table.getDatas().size());
    }

    @Test
    public void shouldParseTabSeparatedQuotedDatetimeSample() throws Exception {
        DefaultEfileParse parser = new DefaultEfileParse();
        List<ETable> tables = parser.parseFile(new File("data/LP_K8000_STAdfgYDB_20191105_113002.DT"));

        Assert.assertEquals(2, tables.size());
        ETable first = tables.get(0);
        Assert.assertEquals("StandbyDbInfo", first.getTableName());
        Assert.assertArrayEquals(new String[]{"设备ID", "厂站名", "量测名", "时间", "有功值"}, first.getColumnNames());
        Assert.assertEquals(6, first.getDatas().size());
        Assert.assertArrayEquals(new Object[]{"11681223431138934", "八嘎桥站", "哈南.是咯站/500kV.高铁二线/无功值", "2019-11-5 11:25", "-1382333.435"},
                first.getDatas().get(0));
    }

    @Test
    public void shouldUseStrictModeWhenOptionsIsNull() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@ A B C\n"
                        + "# 1 2 3\n"
                        + "# 4 5\n"
                        + "</T>\n",
                null,
                "[line=3]");
    }

    @Test
    public void shouldThrowWhenFileIsNull() throws Exception {
        DefaultEfileParse parser = new DefaultEfileParse();
        try {
            parser.parseFile(null);
            Assert.fail("Expected parser to fail");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("file不能为空"));
        }
    }

    @Test
    public void shouldThrowWhenBodyIsEmpty() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n\n</T>\n", "表体为空");
    }

    @Test
    public void shouldThrowWhenHeaderGuideMissing() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "X A B\n"
                        + "</T>\n",
                "[line=1]");
    }

    @Test
    public void shouldThrowWhenRowTypeDataColumnCountMismatchInStrictMode() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@ A B C\n"
                        + "# 1 2 3\n"
                        + "# 4 5\n"
                        + "# 6 7 8\n"
                        + "</T>\n",
                "横表式字段数量不匹配");
    }

    @Test
    public void shouldSkipRowTypeMalformedDataWhenConfigured() throws Exception {
        ETable table = parseSingleTable("<T date='2026-01-01'>\n"
                        + "@ A B C\n"
                        + "# 1 2 3\n"
                        + "# 4 5\n"
                        + "# 6 7 8\n"
                        + "</T>\n",
                ParseOptions.skipMalformedRows());

        Assert.assertEquals(2, table.getDatas().size());
        Assert.assertArrayEquals(new Object[]{"1", "2", "3"}, table.getDatas().get(0));
        Assert.assertArrayEquals(new Object[]{"6", "7", "8"}, table.getDatas().get(1));
    }

    @Test
    public void shouldThrowWhenRowTypeMarkerWithoutFields() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@ A B C\n"
                        + "%\n"
                        + "</T>\n",
                "横表式引导符后缺少字段");
    }

    @Test
    public void shouldWrapTokenizerErrorForRowType() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@ A B C\n"
                        + "# 1 '2019-11-05 11:25\n"
                        + "</T>\n",
                "存在未闭合的引号");
    }

    @Test
    public void shouldThrowWhenSingleColumnHeaderFieldCountInvalid() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@@ 属性名\n"
                        + "# 属性 值\n"
                        + "</T>\n",
                "单列式表头字段数量必须为2或3");
    }

    @Test
    public void shouldThrowWhenSingleColumnRowFieldCountMismatchInStrictMode() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@@ 顺序 属性名 属性值\n"
                        + "# 1 单位名称\n"
                        + "</T>\n",
                "单列式字段数量不匹配");
    }

    @Test
    public void shouldSkipSingleColumnMalformedRowWhenConfigured() throws Exception {
        ETable table = parseSingleTable("<T date='2026-01-01'>\n"
                        + "@@ 顺序 属性名 属性值\n"
                        + "# 1 单位名称 A站\n"
                        + "# 2 发生时间 '2011-11-03 00:00:02.0'\n"
                        + "# 3 次数 1\n"
                        + "# 1 单位名称\n"
                        + "# 1 单位名称 B站\n"
                        + "# 2 发生时间 '2011-11-03 00:00:03.0'\n"
                        + "# 3 次数 2\n"
                        + "</T>\n",
                ParseOptions.skipMalformedRows());

        Assert.assertArrayEquals(new String[]{"单位名称", "发生时间", "次数"}, table.getColumnNames());
        Assert.assertEquals(2, table.getDatas().size());
        Assert.assertArrayEquals(new Object[]{"A站", "2011-11-03 00:00:02.0", "1"}, table.getDatas().get(0));
        Assert.assertArrayEquals(new Object[]{"B站", "2011-11-03 00:00:03.0", "2"}, table.getDatas().get(1));
    }

    @Test
    public void shouldThrowWhenSingleColumnPropertyNameIsEmpty() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@@ 顺序 属性名 属性值\n"
                        + "# 1 '' 值\n"
                        + "</T>\n",
                "单列式属性名为空");
    }

    @Test
    public void shouldWrapTokenizerErrorForSingleColumn() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@@ 顺序 属性名 属性值\n"
                        + "# 1 发生时间 '2011-11-03 00:00:02.0\n"
                        + "</T>\n",
                "存在未闭合的引号");
    }

    @Test
    public void shouldThrowWhenMultiColumnRowHasTooFewFields() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@# 顺序 属性名 A B\n"
                        + "# 1 单位名称\n"
                        + "</T>\n",
                "多列式字段数量不足");
    }

    @Test
    public void shouldThrowWhenMultiColumnRowCountMismatchInStrictMode() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@# 顺序 属性名 A B\n"
                        + "# 1 单位名称 X Y\n"
                        + "# 2 次数 1\n"
                        + "</T>\n",
                "多列式字段数量不匹配");
    }

    @Test
    public void shouldSkipMultiColumnMalformedRowWhenConfigured() throws Exception {
        ETable table = parseSingleTable("<T date='2026-01-01'>\n"
                        + "@# 顺序 属性名 A B\n"
                        + "# 1 单位名称 X Y\n"
                        + "# 2 发生时间 '2011-11-03 00:00:02.0'\n"
                        + "# 3 次数 1 2\n"
                        + "</T>\n",
                ParseOptions.skipMalformedRows());

        Assert.assertArrayEquals(new String[]{"单位名称", "次数"}, table.getColumnNames());
        Assert.assertEquals(2, table.getDatas().size());
        Assert.assertArrayEquals(new Object[]{"X", "1"}, table.getDatas().get(0));
        Assert.assertArrayEquals(new Object[]{"Y", "2"}, table.getDatas().get(1));
    }

    @Test
    public void shouldThrowWhenMultiColumnColumnNameIsEmpty() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@# 顺序 属性名 A B\n"
                        + "# 1 '' X Y\n"
                        + "</T>\n",
                "多列式列名为空");
    }

    @Test
    public void shouldThrowWhenMultiColumnHasNoValidDataRows() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@# 顺序 属性名 A B\n"
                        + "</T>\n",
                "多列式没有有效的#数据行");
    }

    @Test
    public void shouldWrapTokenizerErrorForMultiColumn() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@# 顺序 属性名 A B\n"
                        + "# 1 发生时间 '2011-11-03 00:00:02.0 X\n"
                        + "</T>\n",
                "存在未闭合的引号");
    }

    @Test
    public void shouldThrowWhenRowTypeDatetimeContainsSpaceWithoutQuotesInStrictMode() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@ 顺序 单位名称 发生时间 次数\n"
                        + "# 1 A站 2019-11-5 11:25 100\n"
                        + "</T>\n",
                "横表式字段数量不匹配");
    }

    @Test
    public void shouldSkipRowTypeDatetimeContainsSpaceWithoutQuotesWhenConfigured() throws Exception {
        ETable table = parseSingleTable("<T date='2026-01-01'>\n"
                        + "@ 顺序 单位名称 发生时间 次数\n"
                        + "# 1 A站 '2019-11-5 11:25' 100\n"
                        + "# 2 B站 2019-11-5 11:25 200\n"
                        + "</T>\n",
                ParseOptions.skipMalformedRows());

        Assert.assertEquals(1, table.getDatas().size());
        Assert.assertArrayEquals(new Object[]{"1", "A站", "2019-11-5 11:25", "100"}, table.getDatas().get(0));
    }

    @Test
    public void shouldThrowWhenSingleColumnDatetimeContainsSpaceWithoutQuotesInStrictMode() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@@ 顺序 属性名 属性值\n"
                        + "# 1 发生时间 2011-11-03 00:00:02.0\n"
                        + "</T>\n",
                "单列式字段数量不匹配");
    }

    @Test
    public void shouldThrowWhenMultiColumnDatetimeContainsSpaceWithoutQuotesInStrictMode() throws Exception {
        assertParseErrorContains("<T date='2026-01-01'>\n"
                        + "@# 顺序 属性名 A B\n"
                        + "# 1 发生时间 2011-11-03 00:00:02.0 2011-11-03 00:00:02.0\n"
                        + "# 2 次数 1 2\n"
                        + "</T>\n",
                "多列式字段数量不匹配");
    }

    private ETable parseSingleTable(String content, ParseOptions options) throws Exception {
        File file = createTempEFile(content);
        try {
            DefaultEfileParse parser = new DefaultEfileParse();
            List<ETable> tables = parser.parseFile(file, options);
            Assert.assertEquals(1, tables.size());
            return tables.get(0);
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    private void assertParseErrorContains(String content, String expectedMessagePart) throws Exception {
        assertParseErrorContains(content, ParseOptions.strict(), expectedMessagePart);
    }

    private void assertParseErrorContains(String content, ParseOptions options, String expectedMessagePart) throws Exception {
        File file = createTempEFile(content);
        try {
            DefaultEfileParse parser = new DefaultEfileParse();
            parser.parseFile(file, options);
            Assert.fail("Expected parser to fail");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue("Expected message to contain: " + expectedMessagePart + ", actual: " + ex.getMessage(),
                    ex.getMessage().contains(expectedMessagePart));
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    private File createTempEFile(String content) throws IOException {
        File file = File.createTempFile("eparser-", ".dt");
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return file;
    }
}
