package io.github.l4rue.cime.mapping;

import io.github.l4rue.cime.annotation.EColumn;
import io.github.l4rue.cime.internal.mapping.ColumnRecord;
import io.github.l4rue.cime.model.EFileDocument;
import io.github.l4rue.cime.model.EHeader;
import io.github.l4rue.cime.model.ETable;
import io.github.l4rue.cime.parse.DefaultEfileParse;
import io.github.l4rue.cime.parse.ParseOptions;
import io.github.l4rue.cime.write.DefaultEfileWrite;
import io.github.l4rue.cime.write.WriteOptions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * Parses an E-language file into a document.
     *
     * @param file source file
     * @return parsed document
     * @throws Exception when parsing fails
     */
    public static EFileDocument parseDocument(File file) throws Exception {
        return new DefaultEfileParse().parseDocument(file);
    }

    /**
     * Parses an E-language file into tables.
     *
     * @param file source file
     * @return parsed tables
     * @throws Exception when parsing fails
     */
    public static List<ETable> parseTables(File file) throws Exception {
        return new DefaultEfileParse().parseFile(file);
    }

    /**
     * Parses an E-language file into tables with parse options.
     *
     * @param file    source file
     * @param options parse options
     * @return parsed tables
     * @throws Exception when parsing fails
     */
    public static List<ETable> parseTables(File file, ParseOptions options) throws Exception {
        return new DefaultEfileParse().parseFile(file, options);
    }

    /**
     * Parses an E-language file and maps the first matching table to beans.
     *
     * @param file   source file
     * @param eClass bean type
     * @param <E>    bean type
     * @return mapped beans
     * @throws Exception when parsing or mapping fails
     */
    public static <E> List<E> parseBeans(File file, Class<E> eClass) throws Exception {
        List<ETable> tables = parseTables(file);
        ETable table = findMatchingTable(tables, eClass);
        return parseBean(table, eClass);
    }

    /**
     * Maps bean rows annotated with {@link EColumn} to an {@link ETable}.
     *
     * @param beans  source bean rows
     * @param eClass bean type
     * @param <E>    bean type
     * @return generated table with table name, column names, and data rows
     * @throws IllegalAccessException when a mapped field cannot be read
     */
    public static <E> ETable toTable(List<E> beans, Class<E> eClass) throws IllegalAccessException {
        if (eClass == null) {
            throw new IllegalArgumentException("eClass must not be null");
        }
        List<E> source = beans == null ? Collections.<E>emptyList() : beans;
        List<Field> mappedFields = new ArrayList<Field>();
        List<String> mappedColumns = new ArrayList<String>();
        Field[] fields = eClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(EColumn.class)) {
                continue;
            }
            EColumn column = field.getAnnotation(EColumn.class);
            mappedFields.add(field);
            mappedColumns.add(column.value());
        }
        if (mappedFields.isEmpty()) {
            throw new IllegalArgumentException("No @EColumn fields found in " + eClass.getName());
        }

        ETable table = new ETable();
        table.setTableName(resolveTableName(eClass));
        table.setColumnNames(mappedColumns.toArray(new String[0]));
        for (E bean : source) {
            if (bean == null) {
                throw new IllegalArgumentException("bean row must not be null");
            }
            Object[] row = new Object[mappedFields.size()];
            for (int i = 0; i < mappedFields.size(); i++) {
                Field field = mappedFields.get(i);
                field.setAccessible(true);
                row[i] = field.get(bean);
            }
            table.getDataRows().add(row);
        }
        return table;
    }

    /**
     * Wraps bean rows as a single-table document.
     *
     * @param beans  source bean rows
     * @param eClass bean type
     * @param <E>    bean type
     * @return document containing one generated table
     * @throws IllegalAccessException when a mapped field cannot be read
     */
    public static <E> EFileDocument toDocument(List<E> beans, Class<E> eClass) throws IllegalAccessException {
        return new EFileDocument(Collections.singletonList(toTable(beans, eClass)));
    }

    /**
     * Wraps tables into a document.
     *
     * @param tables source tables
     * @return document containing the supplied tables
     */
    public static EFileDocument toDocument(List<ETable> tables) {
        return new EFileDocument(tables);
    }

    /**
     * Wraps tables and header into a document.
     *
     * @param tables source tables
     * @param header document header metadata
     * @return document containing tables and header
     */
    public static EFileDocument toDocument(List<ETable> tables, EHeader header) {
        EFileDocument document = new EFileDocument(tables);
        document.setHeader(header);
        return document;
    }

    /**
     * Serializes bean rows as E-language text.
     *
     * @param beans  source bean rows
     * @param eClass bean type
     * @param <E>    bean type
     * @return serialized E-language text
     * @throws IllegalAccessException when a mapped field cannot be read
     */
    public static <E> String toFile(List<E> beans, Class<E> eClass) throws IllegalAccessException {
        ETable table = toTable(beans, eClass);
        return new DefaultEfileWrite().writeToString(Collections.singletonList(table), WriteOptions.defaults());
    }

    /**
     * Writes bean rows directly to an E-language file.
     *
     * @param beans  source bean rows
     * @param eClass bean type
     * @param file   target file
     * @param <E>    bean type
     * @throws IllegalAccessException when a mapped field cannot be read
     * @throws IOException            when target file cannot be written
     */
    public static <E> void toFile(List<E> beans, Class<E> eClass, File file) throws IllegalAccessException, IOException {
        toFile(beans, eClass, file, WriteOptions.defaults());
    }

    /**
     * Writes bean rows directly to an E-language file with options.
     *
     * @param beans   source bean rows
     * @param eClass  bean type
     * @param file    target file
     * @param options write options
     * @param <E>     bean type
     * @throws IllegalAccessException when a mapped field cannot be read
     * @throws IOException            when target file cannot be written
     */
    public static <E> void toFile(List<E> beans, Class<E> eClass, File file, WriteOptions options)
            throws IllegalAccessException, IOException {
        ETable table = toTable(beans, eClass);
        new DefaultEfileWrite().writeFile(Collections.singletonList(table), file, options);
    }

    /**
     * Serializes a document as E-language text.
     *
     * @param document source document
     * @return serialized E-language text
     */
    public static String toFile(EFileDocument document) {
        return new DefaultEfileWrite().writeToString(document, WriteOptions.preserveSourceLayout());
    }

    /**
     * Writes a document to an E-language file.
     *
     * @param document source document
     * @param file     target file
     * @throws IOException when target file cannot be written
     */
    public static void toFile(EFileDocument document, File file) throws IOException {
        new DefaultEfileWrite().writeFile(document, file);
    }

    /**
     * Writes tables to an E-language file.
     *
     * @param tables source tables
     * @param file   target file
     * @throws IOException when target file cannot be written
     */
    public static void toFile(List<ETable> tables, File file) throws IOException {
        new DefaultEfileWrite().writeFile(tables, file);
    }

    private static <E> String resolveTableName(Class<E> eClass) {
        boolean annotationPresent = eClass.isAnnotationPresent(io.github.l4rue.cime.annotation.ETable.class);
        if (annotationPresent) {
            io.github.l4rue.cime.annotation.ETable tableAnnotation = eClass.getAnnotation(io.github.l4rue.cime.annotation.ETable.class);
            return tableAnnotation.value();
        }
        return eClass.getSimpleName();
    }

    private static <E> ETable findMatchingTable(List<ETable> tables, Class<E> eClass) {
        if (tables == null || tables.isEmpty()) {
            throw new IllegalArgumentException("No tables found in file");
        }
        String expected = resolveTableName(eClass);
        for (ETable table : tables) {
            if (table != null && expected.equals(table.getTableName())) {
                return table;
            }
        }
        throw new IllegalArgumentException("No table matched " + expected);
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
