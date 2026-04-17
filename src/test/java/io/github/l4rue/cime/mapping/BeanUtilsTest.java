package io.github.l4rue.cime.mapping;

import io.github.l4rue.cime.annotation.EColumn;
import io.github.l4rue.cime.parse.DefaultEfileParse;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
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
        io.github.l4rue.cime.model.ETable table = new io.github.l4rue.cime.model.ETable();
        table.setTableName("SampleBean");
        table.setColumnNames(new String[]{"A", "B"});
        table.getDataRows().add(new Object[]{"v1"});
        List<SampleBean> beans = BeanUtils.parseBean(table, SampleBean.class);
        Assert.assertEquals(1, beans.size());
        Assert.assertEquals("v1", beans.get(0).a);
        Assert.assertNull(beans.get(0).b);
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
    }

    @io.github.l4rue.cime.annotation.ETable("SampleBean")
    public static class SampleBean {
        @EColumn("A")
        private String a;
        @EColumn("B")
        private String b;
    }
}
