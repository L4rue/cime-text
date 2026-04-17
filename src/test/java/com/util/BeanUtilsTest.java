package com.util;

import com.annotation.EColumn;
import com.annotation.ETable;
import com.efile.impl.DefaultEfileParse;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class BeanUtilsTest {

    @Test
    public void shouldMapParsedRowsToBean() throws Exception {
        DefaultEfileParse parser = new DefaultEfileParse();
        List<com.efile.ETable> tables = parser.parseFile(new File("data/LP_K8000_STAdfgYDB_20191105_113002.DT"));
        Assert.assertTrue(tables.size() >= 1);
        List<StandbyDbInfoBean> beans = BeanUtils.parseBean(tables.get(0), StandbyDbInfoBean.class);
        Assert.assertEquals(6, beans.size());
        Assert.assertEquals("11681223431138934", beans.get(0).devId);
    }

    @Test
    public void shouldNotFailWhenDataColumnMissing() throws Exception {
        com.efile.ETable table = new com.efile.ETable();
        table.setTableName("SampleBean");
        table.setColumnNames(new String[]{"A", "B"});
        table.getDatas().add(new Object[]{"v1"});
        List<SampleBean> beans = BeanUtils.parseBean(table, SampleBean.class);
        Assert.assertEquals(1, beans.size());
        Assert.assertEquals("v1", beans.get(0).a);
        Assert.assertNull(beans.get(0).b);
    }

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

    @ETable("SampleBean")
    public static class SampleBean {
        @EColumn("A")
        private String a;
        @EColumn("B")
        private String b;
    }
}
