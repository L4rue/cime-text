package io.github.l4rue.cime.mapping;

import io.github.l4rue.cime.annotation.EColumn;
import io.github.l4rue.cime.model.EFileDocument;
import io.github.l4rue.cime.model.EHeader;
import io.github.l4rue.cime.model.ETable;
import io.github.l4rue.cime.parse.DefaultEfileParse;
import io.github.l4rue.cime.write.DefaultEfileWrite;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests for bean mapping against parsed E-table data.
 *
 * @author dingyh
 */
public class BeanUtilsTest {

    @Test
    public void shouldMapParsedRowsToBean() throws Exception {
        DefaultEfileParse parser = new DefaultEfileParse();
        List<io.github.l4rue.cime.model.ETable> tables = parser.parseFile(new File("data/LP_K8000_STAdfgYDB_20191105_113002.DT"));
        Assert.assertTrue(tables.size() >= 1);
        List<StandbyDbInfoBean> beans = BeanUtils.parseBean(tables.get(0), StandbyDbInfoBean.class);
        Assert.assertEquals(6, beans.size());
        Assert.assertEquals("11681223431138934", beans.get(0).devId);
    }

    @Test
    public void shouldNotFailWhenDataColumnMissing() throws Exception {
        ETable table = new ETable();
        table.setTableName("SampleBean");
        table.setColumnNames(new String[]{"A", "B"});
        table.getDataRows().add(new Object[]{"v1"});
        List<SampleBean> beans = BeanUtils.parseBean(table, SampleBean.class);
        Assert.assertEquals(1, beans.size());
        Assert.assertEquals("v1", beans.get(0).a);
        Assert.assertNull(beans.get(0).b);
    }

    @Test
    public void shouldMapBeanRowsToTable() throws Exception {
        List<StandbyDbInfoBean> beans = new ArrayList<StandbyDbInfoBean>();
        beans.add(new StandbyDbInfoBean("dev-1", "厂站A", "有功", "2026-04-21 10:00:00", "10.5"));
        beans.add(new StandbyDbInfoBean("dev-2", "厂站B", "无功", "2026-04-21 10:05:00", "11.5"));

        ETable table = BeanUtils.toTable(beans, StandbyDbInfoBean.class);

        Assert.assertEquals("StandbyDbInfo", table.getTableName());
        Assert.assertArrayEquals(new String[]{"设备ID", "厂站名", "量测名", "时间", "有功值"}, table.getColumnNames());
        Assert.assertEquals(2, table.getDataRows().size());
        Assert.assertArrayEquals(new Object[]{"dev-1", "厂站A", "有功", "2026-04-21 10:00:00", "10.5"}, table.getDataRows().get(0));
    }

    @Test
    public void shouldWriteDocumentFromMultipleBeanTables() throws Exception {
        List<StandbyDbInfoBean> standbyBeans = Collections.singletonList(
                new StandbyDbInfoBean("dev-1", "厂站A", "有功", "2026-04-21 10:00:00", "10.5")
        );
        List<DeviceAlarmBean> alarmBeans = Arrays.asList(
                new DeviceAlarmBean("dev-1", "告警A"),
                new DeviceAlarmBean("dev-2", "告警B")
        );

        ETable standbyTable = BeanUtils.toTable(standbyBeans, StandbyDbInfoBean.class);
        standbyTable.setTagName("StandbyDbInfo::站点A");
        standbyTable.putAttribute("date", "2026-04-21");

        ETable alarmTable = BeanUtils.toTable(alarmBeans, DeviceAlarmBean.class);
        alarmTable.setTagName("DeviceAlarm::站点A");
        alarmTable.putAttribute("date", "2026-04-21");

        EFileDocument document = new EFileDocument(Arrays.asList(standbyTable, alarmTable));
        EHeader header = new EHeader();
        header.putAttribute("Entity", "站点A");
        header.putAttribute("type", "样例");
        document.setHeader(header);

        File out = File.createTempFile("bean-doc-", ".dt");
        try {
            DefaultEfileWrite writer = new DefaultEfileWrite();
            writer.writeFile(document, out);
            DefaultEfileParse parser = new DefaultEfileParse();
            EFileDocument parsed = parser.parseDocument(out);

            Assert.assertEquals("站点A", parsed.getHeader().getAttribute("Entity"));
            Assert.assertEquals(2, parsed.getTables().size());
            Assert.assertEquals("StandbyDbInfo", parsed.getTables().get(0).getTableName());
            Assert.assertEquals("DeviceAlarm", parsed.getTables().get(1).getTableName());
        } finally {
            out.delete();
        }
    }

    @Test
    public void shouldWriteBeansDirectlyToFile() throws Exception {
        List<StandbyDbInfoBean> beans = Arrays.asList(
                new StandbyDbInfoBean("dev-1", "厂站A", "有功", "2026-04-21 10:00:00", "10.5"),
                new StandbyDbInfoBean("dev-2", "厂站B", "有功", "2026-04-21 10:05:00", "11.5")
        );
        File out = File.createTempFile("bean-file-", ".dt");
        try {
            BeanUtils.toFile(beans, StandbyDbInfoBean.class, out);
            List<ETable> tables = BeanUtils.parseTables(out);
            Assert.assertEquals(1, tables.size());
            Assert.assertEquals("StandbyDbInfo", tables.get(0).getTableName());
            Assert.assertEquals(2, tables.get(0).getDataRows().size());
        } finally {
            out.delete();
        }
    }

    @Test
    public void shouldParseBeansFromFileFacade() throws Exception {
        List<StandbyDbInfoBean> beans = BeanUtils.parseBeans(
                new File("data/LP_K8000_STAdfgYDB_20191105_113002.DT"),
                StandbyDbInfoBean.class
        );
        Assert.assertEquals(6, beans.size());
        Assert.assertEquals("11681223431138934", beans.get(0).devId);
    }

    @io.github.l4rue.cime.annotation.ETable("StandbyDbInfo")
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

        public StandbyDbInfoBean() {
        }

        public StandbyDbInfoBean(String devId, String dcdName, String measureName, String date, String activeValue) {
            this.devId = devId;
            this.dcdName = dcdName;
            this.measureName = measureName;
            this.date = date;
            this.activeValue = activeValue;
        }
    }

    @io.github.l4rue.cime.annotation.ETable("SampleBean")
    public static class SampleBean {
        @EColumn("A")
        private String a;
        @EColumn("B")
        private String b;
    }

    @io.github.l4rue.cime.annotation.ETable("DeviceAlarm")
    public static class DeviceAlarmBean {
        @EColumn("设备ID")
        private String devId;
        @EColumn("告警名")
        private String alarmName;

        public DeviceAlarmBean() {
        }

        public DeviceAlarmBean(String devId, String alarmName) {
            this.devId = devId;
            this.alarmName = alarmName;
        }
    }
}
