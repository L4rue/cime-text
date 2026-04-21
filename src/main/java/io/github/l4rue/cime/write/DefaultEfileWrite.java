package io.github.l4rue.cime.write;

import io.github.l4rue.cime.model.EAttribute;
import io.github.l4rue.cime.model.EFileDocument;
import io.github.l4rue.cime.model.EHeader;
import io.github.l4rue.cime.model.ETable;
import io.github.l4rue.cime.model.ETableLayout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * Default writer that serializes {@link ETable} objects to UTF-8 E-language text.
 *
 * @author dingyh
 */
public class DefaultEfileWrite implements EFileWrite {

    private static final String LINE_SEPARATOR = "\n";
    private static final String SINGLE_COLUMN_HEADER = "@@ 顺序 属性名 属性值";
    private static final String MULTI_COLUMN_HEADER_PREFIX = "@# 顺序 属性名";

    @Override
    public void writeFile(List<ETable> tables, File file) throws IOException {
        writeFile(tables, file, WriteOptions.defaults());
    }

    @Override
    public void writeFile(List<ETable> tables, File file, WriteOptions options) throws IOException {
        if (tables == null) {
            throw new IllegalArgumentException("tables must not be null");
        }
        writeFile(new EFileDocument(tables), file, options == null ? WriteOptions.defaults() : options);
    }

    /**
     * Writes a parsed E-language document to a UTF-8 encoded file.
     *
     * @param document source document
     * @param file     target file
     * @throws IOException when the target file cannot be written
     */
    public void writeFile(EFileDocument document, File file) throws IOException {
        writeFile(document, file, WriteOptions.preserveSourceLayout());
    }

    /**
     * Writes a parsed E-language document to a UTF-8 encoded file.
     *
     * @param document source document
     * @param file     target file
     * @param options  write options; defaults are used when {@code null}
     * @throws IOException when the target file cannot be written
     */
    public void writeFile(EFileDocument document, File file, WriteOptions options) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        File parent = file.getParentFile();
        if (parent != null) {
            Files.createDirectories(parent.toPath());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(writeToString(document, options));
        }
    }

    @Override
    public String writeToString(List<ETable> tables, WriteOptions options) {
        if (tables == null) {
            throw new IllegalArgumentException("tables must not be null");
        }
        return writeToString(new EFileDocument(tables), options == null ? WriteOptions.defaults() : options);
    }

    /**
     * Serializes a parsed E-language document.
     *
     * @param document source document
     * @param options  write options; defaults are used when {@code null}
     * @return serialized E-language text
     */
    public String writeToString(EFileDocument document, WriteOptions options) {
        if (document == null) {
            throw new IllegalArgumentException("document must not be null");
        }
        List<ETable> tables = document.getTables();
        if (tables == null) {
            throw new IllegalArgumentException("tables must not be null");
        }
        WriteOptions effectiveOptions = options == null ? WriteOptions.preserveSourceLayout() : options;
        StringBuilder sb = new StringBuilder();
        appendHeader(sb, document.getHeader());
        for (int i = 0; i < tables.size(); i++) {
            ETable table = tables.get(i);
            if (table == null) {
                throw new IllegalArgumentException("table must not be null");
            }
            writeTable(sb, table, resolveLayout(table, effectiveOptions));
            if (i + 1 < tables.size()) {
                sb.append(LINE_SEPARATOR);
            }
        }
        return sb.toString();
    }

    private void writeTable(StringBuilder sb, ETable table, TableLayout layout) {
        String tagName = requireTagName(table);
        appendStartTag(sb, tagName, table);
        if (layout == TableLayout.SINGLE_COLUMN) {
            writeSingleColumnTable(sb, table);
        } else if (layout == TableLayout.MULTI_COLUMN) {
            writeMultiColumnTable(sb, table);
        } else {
            writeHorizontalTable(sb, table);
        }
        appendLine(sb, "</" + tagName + ">");
    }

    private void appendHeader(StringBuilder sb, EHeader header) {
        if (header == null || header.isEmpty()) {
            return;
        }
        sb.append("<!");
        appendAttributes(sb, header.getAttributes(), true);
        appendLine(sb, " !>");
    }

    private void appendStartTag(StringBuilder sb, String tagName, ETable table) {
        sb.append('<').append(tagName);
        if (table.getAttributes() != null && !table.getAttributes().isEmpty()) {
            appendAttributes(sb, table.getAttributes(), false);
        } else if (table.getDate() != null && table.getDate().length() > 0) {
            sb.append(" date=").append(formatAttributeValue(table.getDate()));
        }
        appendLine(sb, ">");
    }

    private void appendAttributes(StringBuilder sb, Map<String, EAttribute> attributes, boolean allowBareValues) {
        for (EAttribute attribute : attributes.values()) {
            if (attribute == null || attribute.getName() == null || attribute.getName().length() == 0) {
                throw new IllegalArgumentException("attribute name must not be empty");
            }
            sb.append(' ').append(attribute.getName()).append('=')
                    .append(formatAttributeValue(attribute, allowBareValues));
        }
    }

    private void writeHorizontalTable(StringBuilder sb, ETable table) {
        String[] columns = requireColumns(table);
        appendMarkedLine(sb, "@", columns);
        appendMetadataLine(sb, "%", table.getTypes(), columns.length);
        appendMetadataLine(sb, "$", table.getUnits(), columns.length);
        appendMetadataLine(sb, ":", table.getLimitValues(), columns.length);
        List<Object[]> rows = requireRows(table);
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Object[] row = rows.get(rowIndex);
            requireRowLength(row, columns.length, rowIndex);
            appendMarkedLine(sb, "#", row);
        }
    }

    private void writeSingleColumnTable(StringBuilder sb, ETable table) {
        String[] columns = requireColumns(table);
        List<Object[]> rows = requireRows(table);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("single-column layout requires at least one data row");
        }
        appendLine(sb, SINGLE_COLUMN_HEADER);
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Object[] row = rows.get(rowIndex);
            requireRowLength(row, columns.length, rowIndex);
            for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
                sb.append("# ").append(columnIndex + 1).append(' ')
                        .append(formatToken(columns[columnIndex])).append(' ')
                        .append(formatToken(row[columnIndex]));
                appendLine(sb, "");
            }
        }
    }

    private void writeMultiColumnTable(StringBuilder sb, ETable table) {
        String[] columns = requireColumns(table);
        List<Object[]> rows = requireRows(table);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("multi-column layout requires at least one data row");
        }
        sb.append(MULTI_COLUMN_HEADER_PREFIX);
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Object[] row = rows.get(rowIndex);
            requireRowLength(row, columns.length, rowIndex);
            sb.append(' ').append(rowIndex + 1);
        }
        appendLine(sb, "");
        for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
            sb.append("# ").append(columnIndex + 1).append(' ')
                    .append(formatToken(columns[columnIndex]));
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                sb.append(' ').append(formatToken(rows.get(rowIndex)[columnIndex]));
            }
            appendLine(sb, "");
        }
    }

    private void appendMetadataLine(StringBuilder sb, String marker, String[] values, int expectedLength) {
        if (values == null) {
            return;
        }
        if (values.length != expectedLength) {
            throw new IllegalArgumentException(marker + " metadata field count mismatch, expected "
                    + expectedLength + " but was " + values.length);
        }
        appendMarkedLine(sb, marker, values);
    }

    private void appendMarkedLine(StringBuilder sb, String marker, Object[] values) {
        sb.append(marker);
        for (Object value : values) {
            sb.append(' ').append(formatToken(value));
        }
        appendLine(sb, "");
    }

    private void appendLine(StringBuilder sb, String text) {
        sb.append(text).append(LINE_SEPARATOR);
    }

    private TableLayout resolveLayout(ETable table, WriteOptions options) {
        if (options.isPreserveSourceLayout() && table.getSourceLayout() != null) {
            return toWriteLayout(table.getSourceLayout());
        }
        return options.getLayout();
    }

    private TableLayout toWriteLayout(ETableLayout sourceLayout) {
        if (sourceLayout == ETableLayout.SINGLE_COLUMN) {
            return TableLayout.SINGLE_COLUMN;
        }
        if (sourceLayout == ETableLayout.MULTI_COLUMN) {
            return TableLayout.MULTI_COLUMN;
        }
        return TableLayout.HORIZONTAL;
    }

    private String requireTagName(ETable table) {
        String tagName = table.getTagName();
        if (tagName == null || tagName.trim().length() == 0) {
            tagName = table.getTableName();
        }
        if (tagName == null || tagName.trim().length() == 0) {
            throw new IllegalArgumentException("tagName or tableName must not be empty");
        }
        if (containsInvalidTagNameChar(tagName)) {
            throw new IllegalArgumentException("tagName contains unsupported tag characters: " + tagName);
        }
        return tagName;
    }

    private String[] requireColumns(ETable table) {
        String[] columns = table.getColumnNames();
        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException("columnNames must not be empty");
        }
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] == null || columns[i].length() == 0) {
                throw new IllegalArgumentException("column name must not be empty at index " + i);
            }
        }
        return columns;
    }

    private List<Object[]> requireRows(ETable table) {
        List<Object[]> rows = table.getDataRows();
        if (rows == null) {
            throw new IllegalArgumentException("dataRows must not be null");
        }
        return rows;
    }

    private void requireRowLength(Object[] row, int expectedLength, int rowIndex) {
        if (row == null) {
            throw new IllegalArgumentException("data row must not be null at index " + rowIndex);
        }
        if (row.length != expectedLength) {
            throw new IllegalArgumentException("data row field count mismatch at index " + rowIndex
                    + ", expected " + expectedLength + " but was " + row.length);
        }
    }

    private boolean containsInvalidTagNameChar(String value) {
        for (int i = 0; i < value.length(); i++) {
            char chr = value.charAt(i);
            if (chr == '<' || chr == '>' || chr == '/' || isTokenWhitespace(chr)) {
                return true;
            }
        }
        return false;
    }

    private String formatAttributeValue(String value) {
        if (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("attribute value must not contain line breaks");
        }
        if (value.indexOf('\'') < 0) {
            return "'" + value + "'";
        }
        if (value.indexOf('"') < 0) {
            return "\"" + value + "\"";
        }
        throw new IllegalArgumentException("attribute value cannot contain both single and double quotes");
    }

    private String formatAttributeValue(EAttribute attribute, boolean allowBareValues) {
        String value = attribute.getValue() == null ? "" : attribute.getValue();
        if (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("attribute value must not contain line breaks");
        }
        Character quote = attribute.getQuote();
        if (allowBareValues && quote == null && !requiresQuoting(value)) {
            return value;
        }
        if (quote != null) {
            char quoteChar = quote.charValue();
            if (quoteChar != '\'' && quoteChar != '"') {
                throw new IllegalArgumentException("attribute quote must be single quote or double quote");
            }
            if (value.indexOf(quoteChar) >= 0) {
                throw new IllegalArgumentException("attribute value contains its configured quote character");
            }
            return quoteChar + value + String.valueOf(quoteChar);
        }
        return formatAttributeValue(value);
    }

    private String formatToken(Object value) {
        if (value == null) {
            return "''";
        }
        String text = String.valueOf(value);
        if (text.length() == 0) {
            return "''";
        }
        if (text.indexOf('\r') >= 0 || text.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("field value must not contain line breaks");
        }
        if (text.indexOf('<') >= 0) {
            throw new IllegalArgumentException("field value must not contain '<'");
        }
        if (!requiresQuoting(text)) {
            return text;
        }
        if (text.indexOf('\'') < 0) {
            return "'" + text + "'";
        }
        if (text.indexOf('"') < 0) {
            return "\"" + text + "\"";
        }
        throw new IllegalArgumentException("field value cannot contain both single and double quotes when quoting is required");
    }

    private boolean requiresQuoting(String text) {
        if (text.length() == 0) {
            return true;
        }
        char first = text.charAt(0);
        if (first == '\'' || first == '"') {
            return true;
        }
        for (int i = 0; i < text.length(); i++) {
            if (isTokenWhitespace(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean isTokenWhitespace(char chr) {
        return chr == ' ' || chr == '\n' || chr == '\t' || chr == '\r' || chr == '\f';
    }
}
