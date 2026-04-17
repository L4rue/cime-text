package io.github.l4rue.cime.mapping;

import io.github.l4rue.cime.annotation.EColumn;
import io.github.l4rue.cime.internal.mapping.ColumnRecord;
import io.github.l4rue.cime.model.ETable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps parsed {@link ETable} rows to annotated Java beans.
 *
 * @author dingyh
 */
public class BeanUtils {

    /**
     * Maps each parsed {@link ETable} row to a bean instance annotated with {@link EColumn}.
     *
     * @param eTable parsed table to convert
     * @param eClass bean type that receives row values
     * @param <E>    bean type
     * @return list of populated bean instances in row order
     * @throws IllegalAccessException when a mapped field cannot be written
     * @throws InstantiationException when the bean type cannot be instantiated
     */
    public static <E> List<E> parseBean(ETable eTable, Class<E> eClass) throws IllegalAccessException, InstantiationException {
        boolean annotationPresent = eClass.isAnnotationPresent(io.github.l4rue.cime.annotation.ETable.class);
        String expectedTableName = eClass.getSimpleName();
        if (annotationPresent) {
            io.github.l4rue.cime.annotation.ETable tableAnnotation = eClass.getAnnotation(io.github.l4rue.cime.annotation.ETable.class);
            expectedTableName = tableAnnotation.value();
        }

        String tableName = eTable.getTableName();
        if (!expectedTableName.equals(tableName)) {
            throw new RuntimeException(expectedTableName + " does not match table name " + tableName);
        }
        Field[] fields = eClass.getDeclaredFields();
        List<Object[]> datas = eTable.getDataRows();
        String[] columnNames = eTable.getColumnNames();
        if (columnNames == null) {
            columnNames = new String[0];
        }
        if (datas == null) {
            datas = new ArrayList<Object[]>();
        }
        List<ColumnRecord> columnRecords = new ArrayList<>();
        for (Field field : fields) {
            boolean columnAnnotationPresent = field.isAnnotationPresent(EColumn.class);
            if (columnAnnotationPresent) {
                EColumn eColumnAnnotation = field.getAnnotation(EColumn.class);
                String value = eColumnAnnotation.value();
                for (int i = 0; i < columnNames.length; i++) {
                    if (value.equals(columnNames[i])) {
                        ColumnRecord columnRecord = new ColumnRecord();
                        columnRecord.setField(field);
                        columnRecord.setIndex(i);
                        columnRecords.add(columnRecord);
                    }
                }
            }
        }

        List<E> es = new ArrayList<>();

        for (Object[] data : datas) {
            E e = newInstance(eClass);
            for (ColumnRecord columnRecord : columnRecords) {
                Field field = columnRecord.getField();
                field.setAccessible(true);
                int index = columnRecord.getIndex();
                if (data == null || index < 0 || index >= data.length) {
                    continue;
                }
                field.set(e, data[index]);
            }
            es.add(e);
        }
        return es;
    }

    private static <E> E newInstance(Class<E> eClass) throws InstantiationException {
        try {
            return eClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            InstantiationException ex = new InstantiationException("Failed to instantiate " + eClass.getName());
            ex.initCause(e);
            throw ex;
        }
    }
}
