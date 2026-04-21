package io.github.l4rue.cime.write;

import io.github.l4rue.cime.model.EFileDocument;
import io.github.l4rue.cime.model.ETable;
import io.github.l4rue.cime.model.ETableLayout;
import io.github.l4rue.cime.parse.DefaultEfileParse;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

/**
 * Tests for the default E-file writer implementation.
 *
 * @author dingyh
 */
public class DefaultEfileWriteTest {

    @Test
    public void shouldWriteHorizontalLayoutByDefaultAndParseAgain() throws Exception {
        ETable source = createTableWithMetadata();
        File file = createTempEFile();
        try {
            DefaultEfileWrite writer = new DefaultEfileWrite();
            writer.writeFile(Collections.singletonList(source), file);

            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            Assert.assertTrue(content.contains("@ 顺序 发生时间 次数"));
            Assert.assertTrue(content.contains("# 1 '2011-11-03 00:00:02.0' 32"));

            ETable parsed = parseSingleTable(file);
            Assert.assertEquals("DG", parsed.getTableName());
            Assert.assertEquals("2012-04-23 13:34", parsed.getDate());
            Assert.assertArrayEquals(source.getColumnNames(), parsed.getColumnNames());
            Assert.assertArrayEquals(source.getTypes(), parsed.getTypes());
            Assert.assertArrayEquals(source.getUnits(), parsed.getUnits());
            Assert.assertArrayEquals(source.getLimitValues(), parsed.getLimitValues());
            Assert.assertArrayEquals(new Object[]{"1", "2011-11-03 00:00:02.0", "32"}, parsed.getDataRows().get(0));
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    public void shouldUseHorizontalLayoutWhenOptionsIsNull() {
        ETable table = createSimpleTable();
        DefaultEfileWrite writer = new DefaultEfileWrite();

        String content = writer.writeToString(Collections.singletonList(table), null);

        Assert.assertTrue(content.contains("@ 单位名称 发生时间 次数"));
        Assert.assertTrue(content.contains("# 花花电网 '2011-11-03 00:00:02.0' 32"));
    }

    @Test
    public void shouldWriteSingleColumnLayoutAndParseAgain() throws Exception {
        ETable source = createSimpleTable();
        File file = createTempEFile();
        try {
            DefaultEfileWrite writer = new DefaultEfileWrite();
            writer.writeFile(Collections.singletonList(source), file, WriteOptions.singleColumn());

            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            Assert.assertTrue(content.contains("@@ 顺序 属性名 属性值"));
            Assert.assertTrue(content.contains("# 2 发生时间 '2011-11-03 00:00:02.0'"));

            ETable parsed = parseSingleTable(file);
            Assert.assertEquals("DG", parsed.getTableName());
            Assert.assertArrayEquals(source.getColumnNames(), parsed.getColumnNames());
            Assert.assertEquals(2, parsed.getDataRows().size());
            Assert.assertArrayEquals(new Object[]{"花花电网", "2011-11-03 00:00:02.0", "32"}, parsed.getDataRows().get(0));
            Assert.assertArrayEquals(new Object[]{"花花电网2", "2011-11-03 00:00:03.0", "33"}, parsed.getDataRows().get(1));
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    public void shouldWriteMultiColumnLayoutAndParseAgain() throws Exception {
        ETable source = createSimpleTable();
        File file = createTempEFile();
        try {
            DefaultEfileWrite writer = new DefaultEfileWrite();
            writer.writeFile(Collections.singletonList(source), file, WriteOptions.multiColumn());

            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            Assert.assertTrue(content.contains("@# 顺序 属性名 1 2"));
            Assert.assertTrue(content.contains("# 2 发生时间 '2011-11-03 00:00:02.0' '2011-11-03 00:00:03.0'"));

            ETable parsed = parseSingleTable(file);
            Assert.assertEquals("DG", parsed.getTableName());
            Assert.assertArrayEquals(source.getColumnNames(), parsed.getColumnNames());
            Assert.assertEquals(2, parsed.getDataRows().size());
            Assert.assertArrayEquals(new Object[]{"花花电网", "2011-11-03 00:00:02.0", "32"}, parsed.getDataRows().get(0));
            Assert.assertArrayEquals(new Object[]{"花花电网2", "2011-11-03 00:00:03.0", "33"}, parsed.getDataRows().get(1));
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    public void shouldWriteDocumentWithHeaderTagAttributesAndSourceLayout() throws Exception {
        DefaultEfileParse parser = new DefaultEfileParse();
        EFileDocument source = parser.parseDocument(new File("data/多列式.txt"));
        DefaultEfileWrite writer = new DefaultEfileWrite();
        File file = createTempEFile();
        try {
            writer.writeFile(source, file);

            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            Assert.assertTrue(content.contains("<! Entity=铁心桥 type=测试2011-11-03 dataTime='20120423 13:30:07' !>"));
            Assert.assertTrue(content.contains("<DG::铁心桥 date='2012-04-23' DDMM='达梦'>"));
            Assert.assertTrue(content.contains("@# 顺序 属性名"));
            Assert.assertTrue(content.contains("</DG::铁心桥>"));

            EFileDocument parsed = parser.parseDocument(file);
            Assert.assertEquals(source.getHeader().getAttributes(), parsed.getHeader().getAttributes());
            Assert.assertEquals(1, parsed.getTables().size());
            ETable table = parsed.getTables().get(0);
            Assert.assertEquals("DG", table.getTableName());
            Assert.assertEquals("DG::铁心桥", table.getTagName());
            Assert.assertEquals("达梦", table.getAttribute("DDMM"));
            Assert.assertEquals(ETableLayout.MULTI_COLUMN, table.getSourceLayout());
            Assert.assertArrayEquals(source.getTables().get(0).getColumnNames(), table.getColumnNames());
            Assert.assertEquals(source.getTables().get(0).getDataRows().size(), table.getDataRows().size());
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    public void shouldWriteUtf8EncodedFile() throws Exception {
        ETable table = createSimpleTable();
        File file = createTempEFile();
        try {
            DefaultEfileWrite writer = new DefaultEfileWrite();
            writer.writeFile(Collections.singletonList(table), file);

            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

            Assert.assertTrue(content.contains("花花电网"));
            Assert.assertTrue(content.contains("发生时间"));
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    public void shouldWriteNullAndEmptyValuesAsQuotedEmptyTokens() throws Exception {
        ETable table = new ETable();
        table.setTableName("T");
        table.setColumnNames(new String[]{"A", "B"});
        table.getDataRows().add(new Object[]{null, ""});
        File file = createTempEFile();
        try {
            DefaultEfileWrite writer = new DefaultEfileWrite();
            writer.writeFile(Collections.singletonList(table), file);

            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            Assert.assertTrue(content.contains("# '' ''"));

            ETable parsed = parseSingleTable(file);
            Assert.assertArrayEquals(new Object[]{"", ""}, parsed.getDataRows().get(0));
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    public void shouldDropHorizontalMetadataForSingleColumnLayout() throws Exception {
        ETable table = createTableWithMetadata();
        DefaultEfileWrite writer = new DefaultEfileWrite();
        File file = createTempEFile();
        try {
            writer.writeFile(Collections.singletonList(table), file, WriteOptions.singleColumn());
            ETable parsed = parseSingleTable(file);

            Assert.assertNull(parsed.getTypes());
            Assert.assertNull(parsed.getUnits());
            Assert.assertNull(parsed.getLimitValues());
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    public void shouldDropHorizontalMetadataForMultiColumnLayout() throws Exception {
        ETable table = createTableWithMetadata();
        DefaultEfileWrite writer = new DefaultEfileWrite();
        File file = createTempEFile();
        try {
            writer.writeFile(Collections.singletonList(table), file, WriteOptions.multiColumn());
            ETable parsed = parseSingleTable(file);

            Assert.assertNull(parsed.getTypes());
            Assert.assertNull(parsed.getUnits());
            Assert.assertNull(parsed.getLimitValues());
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    private ETable createTableWithMetadata() {
        ETable table = new ETable();
        table.setTableName("DG");
        table.setDate("2012-04-23 13:34");
        table.setColumnNames(new String[]{"顺序", "发生时间", "次数"});
        table.setTypes(new String[]{"int", "string", "int"});
        table.setUnits(new String[]{"", "", "次"});
        table.setLimitValues(new String[]{"", "", "0..100"});
        table.getDataRows().add(new Object[]{"1", "2011-11-03 00:00:02.0", "32"});
        table.getDataRows().add(new Object[]{"2", "2011-11-03 00:00:03.0", "33"});
        return table;
    }

    private ETable createSimpleTable() {
        ETable table = new ETable();
        table.setTableName("DG");
        table.setDate("2012-04-23");
        table.setColumnNames(new String[]{"单位名称", "发生时间", "次数"});
        table.getDataRows().add(new Object[]{"花花电网", "2011-11-03 00:00:02.0", "32"});
        table.getDataRows().add(new Object[]{"花花电网2", "2011-11-03 00:00:03.0", "33"});
        return table;
    }

    private ETable parseSingleTable(File file) throws Exception {
        DefaultEfileParse parser = new DefaultEfileParse();
        List<ETable> tables = parser.parseFile(file);
        Assert.assertEquals(1, tables.size());
        return tables.get(0);
    }

    private File createTempEFile() throws IOException {
        return File.createTempFile("ewriter-", ".dt");
    }
}
