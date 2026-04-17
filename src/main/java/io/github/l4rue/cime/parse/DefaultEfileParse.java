package io.github.l4rue.cime.parse;

import io.github.l4rue.cime.internal.sax.EdomSAXReader;
import io.github.l4rue.cime.internal.util.StringUtils;
import io.github.l4rue.cime.model.ETable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation that parses E-language files into {@link ETable} objects.
 *
 * @author dingyh
 */
public class DefaultEfileParse implements EFileParse {

    @Override
    public List<ETable> parseFile(File file) throws Exception {
        return parseFile(file, ParseOptions.strict());
    }

    @Override
    public List<ETable> parseFile(File file, ParseOptions options) throws Exception {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        ParseOptions effectiveOptions = options == null ? ParseOptions.strict() : options;
        EdomSAXReader reader = new EdomSAXReader();
        Document doc = reader.read(file);
        List<ETable> tableList = new ArrayList<ETable>();

        NodeList nodeList = doc.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (!(node instanceof Element)) {
                continue;
            }
            Element ele = (Element) node;
            ETable table = new ETable();
            String tagName = ele.getTagName();
            if (tagName.contains("::")) {
                table.setTableName(tagName.split("::")[0]);
            } else {
                table.setTableName(tagName);
            }
            String date = ele.getAttribute("date");
            table.setDate(date == null ? "" : date);
            parseTableData(table, ele.getTextContent(), effectiveOptions);
            tableList.add(table);
        }
        return tableList;
    }

    private void parseTableData(ETable table, String content, ParseOptions options) {
        String tableName = table.getTableName();
        if (content == null) {
            throw parseException(tableName, 0, "table body is empty");
        }
        String[] contentArr = content.split("\\r?\\n", -1);
        int headLineIndex = findFirstNonBlankLine(contentArr);
        if (headLineIndex < 0) {
            throw parseException(tableName, 0, "table body is empty");
        }
        String headStr = contentArr[headLineIndex].trim();
        if (headStr.startsWith("@@")) {
            parseSingleColType(table, contentArr, headLineIndex, options);
        } else if (headStr.startsWith("@#")) {
            parseMultiColType(table, contentArr, headLineIndex, options);
        } else if (headStr.startsWith("@")) {
            parseRowType(table, contentArr, headLineIndex, options);
        } else {
            throw parseException(tableName, headLineIndex + 1, "first line must start with @, @@, or @#");
        }
    }

    /**
     * Parses the single-column table layout.
     *
     * @@
     */
    private void parseSingleColType(ETable table, String[] contentArr, int headLineIndex, ParseOptions options) {
        String headStr = contentArr[headLineIndex].trim();
        String[] headerArr = splitString(headStr.substring("@@".length()), table, headLineIndex + 1);
        if (headerArr.length != 2 && headerArr.length != 3) {
            throw parseException(table.getTableName(), headLineIndex + 1, "single-column header must contain 2 or 3 fields");
        }
        Map<String, String> propMap = new LinkedHashMap<String, String>();
        String[] lockedColumns = null;
        int recordStartLine = -1;

        for (int i = headLineIndex + 1; i < contentArr.length; i++) {
            String linestr = contentArr[i].trim();
            if (linestr.length() == 0 || !linestr.startsWith("#")) {
                continue;
            }
            int lineNo = i + 1;
            if (linestr.length() == 1) {
                if (shouldSkipMalformedRow(table.getTableName(), lineNo, "single-column data row is empty", options)) {
                    continue;
                }
            }
            String[] lineArr = splitString(linestr.substring(1), table, lineNo);
            if (lineArr.length != headerArr.length) {
                if (shouldSkipMalformedRow(table.getTableName(), lineNo,
                        "single-column field count mismatch, expected " + headerArr.length + " but was " + lineArr.length, options)) {
                    continue;
                }
            }
            int nameIndex = headerArr.length == 2 ? 0 : 1;
            int valueIndex = nameIndex + 1;
            String propName = lineArr[nameIndex];
            if (propName == null || propName.trim().length() == 0) {
                if (shouldSkipMalformedRow(table.getTableName(), lineNo, "single-column property name is empty", options)) {
                    continue;
                }
            }
            if (propMap.size() == 0) {
                recordStartLine = lineNo;
            }
            if (propMap.containsKey(propName)) {
                lockedColumns = appendSingleRecord(table, propMap, lockedColumns, recordStartLine, options);
                propMap.clear();
                recordStartLine = lineNo;
            }
            propMap.put(propName, lineArr[valueIndex]);
        }

        if (propMap.size() > 0) {
            appendSingleRecord(table, propMap, lockedColumns, recordStartLine, options);
        } else if (table.getColumnNames() == null) {
            table.setColumnNames(new String[0]);
        }
    }

    /**
     * Parses the multi-column table layout.
     */
    private void parseMultiColType(ETable table, String[] contentArr, int headLineIndex, ParseOptions options) {
        List<String[]> propList = new ArrayList<String[]>();
        int expectedLength = -1;
        for (int i = headLineIndex + 1; i < contentArr.length; i++) {
            String linestr = contentArr[i].trim();
            if (linestr.length() == 0 || !linestr.startsWith("#")) {
                continue;
            }
            int lineNo = i + 1;
            if (linestr.length() == 1) {
                if (shouldSkipMalformedRow(table.getTableName(), lineNo, "multi-column data row is empty", options)) {
                    continue;
                }
            }
            String[] lineArr = splitString(linestr.substring(1), table, lineNo);
            if (lineArr.length < 3) {
                if (shouldSkipMalformedRow(table.getTableName(), lineNo, "multi-column row must contain at least 3 fields", options)) {
                    continue;
                }
            }
            if (lineArr[1] == null || lineArr[1].trim().length() == 0) {
                if (shouldSkipMalformedRow(table.getTableName(), lineNo, "multi-column column name is empty", options)) {
                    continue;
                }
            }
            if (expectedLength == -1) {
                expectedLength = lineArr.length;
            } else if (lineArr.length != expectedLength) {
                if (shouldSkipMalformedRow(table.getTableName(), lineNo,
                        "multi-column field count mismatch, expected " + expectedLength + " but was " + lineArr.length, options)) {
                    continue;
                }
            }
            propList.add(lineArr);
        }
        if (propList.size() == 0) {
            throw parseException(table.getTableName(), headLineIndex + 1, "multi-column section does not contain a valid # data row");
        }
        String[] columnNames = new String[propList.size()];
        for (int col = 0; col < columnNames.length; col++) {
            columnNames[col] = propList.get(col)[1];
        }
        table.setColumnNames(columnNames);
        for (int row = 2; row < expectedLength; row++) {
            String[] data = new String[columnNames.length];
            for (int col = 0; col < data.length; col++) {
                data[col] = propList.get(col)[row];
            }
            table.getDataRows().add(data);
        }
    }

    /**
     * Parses the row-based table layout.
     */
    private void parseRowType(ETable table, String[] contentArr, int headLineIndex, ParseOptions options) {
        String headStr = contentArr[headLineIndex].trim();
        String[] headerArr = splitString(headStr.substring("@".length()), table, headLineIndex + 1);
        if (headerArr.length == 0) {
            throw parseException(table.getTableName(), headLineIndex + 1, "row-based header must not be empty");
        }
        String[] columnNames = copyArray(headerArr);
        table.setColumnNames(columnNames);
        for (int i = headLineIndex + 1; i < contentArr.length; i++) {
            String linestr = contentArr[i].trim();
            if (linestr.length() == 0) {
                continue;
            }
            int lineNo = i + 1;
            char marker = linestr.charAt(0);
            if (marker != '%' && marker != '$' && marker != ':' && marker != '#') {
                continue;
            }
            if (linestr.length() == 1) {
                if (marker == '#') {
                    if (shouldSkipMalformedRow(table.getTableName(), lineNo, "row-based data row is empty", options)) {
                        continue;
                    }
                }
                throw parseException(table.getTableName(), lineNo, "row-based marker must be followed by fields");
            }
            String[] lineArr = splitString(linestr.substring(1), table, lineNo);
            if (lineArr.length != columnNames.length) {
                if (marker == '#') {
                    if (shouldSkipMalformedRow(table.getTableName(), lineNo,
                            "row-based field count mismatch, expected " + columnNames.length + " but was " + lineArr.length, options)) {
                        continue;
                    }
                }
                throw parseException(table.getTableName(), lineNo,
                        "row-based field count mismatch, expected " + columnNames.length + " but was " + lineArr.length);
            }
            if (marker == '%') {
                table.setTypes(copyArray(lineArr));
            } else if (marker == '$') {
                table.setUnits(copyArray(lineArr));
            } else if (marker == ':') {
                table.setLimitValues(copyArray(lineArr));
            } else {
                table.getDataRows().add(copyArray(lineArr));
            }
        }
    }

    private String[] appendSingleRecord(ETable table, Map<String, String> propMap, String[] lockedColumns,
                                        int lineNo, ParseOptions options) {
        if (propMap.size() == 0) {
            return lockedColumns;
        }
        String[] recordColumns = toArray(propMap.keySet());
        if (lockedColumns == null) {
            lockedColumns = copyArray(recordColumns);
            table.setColumnNames(copyArray(recordColumns));
        } else if (!sameColumns(lockedColumns, recordColumns)) {
            if (shouldSkipMalformedRow(table.getTableName(), lineNo, "single-column record fields do not match the first record", options)) {
                return lockedColumns;
            }
        }
        String[] data = new String[table.getColumnNames().length];
        for (int i = 0; i < table.getColumnNames().length; i++) {
            data[i] = propMap.get(table.getColumnNames()[i]);
        }
        table.getDataRows().add(data);
        return lockedColumns;
    }

    private boolean shouldSkipMalformedRow(String tableName, int lineNo, String reason, ParseOptions options) {
        if (options.getMalformedRowPolicy() == MalformedRowPolicy.SKIP_ROW) {
            return true;
        }
        throw parseException(tableName, lineNo, reason);
    }

    private RuntimeException parseException(String tableName, int lineNo, String reason) {
        StringBuilder sb = new StringBuilder();
        sb.append("E-file parsing failed");
        if (tableName != null && tableName.trim().length() > 0) {
            sb.append(" [table=").append(tableName).append("]");
        }
        if (lineNo > 0) {
            sb.append(" [line=").append(lineNo).append("]");
        }
        sb.append(" ").append(reason);
        return new IllegalArgumentException(sb.toString());
    }

    private int findFirstNonBlankLine(String[] contentArr) {
        for (int i = 0; i < contentArr.length; i++) {
            if (contentArr[i] != null && contentArr[i].trim().length() > 0) {
                return i;
            }
        }
        return -1;
    }

    private String[] splitString(String str, ETable table, int lineNo) {
        try {
            return StringUtils.splitLineWithSpace(str);
        } catch (IllegalArgumentException ex) {
            throw parseException(table.getTableName(), lineNo, ex.getMessage());
        }
    }

    private String[] copyArray(String[] source) {
        String[] target = new String[source.length];
        System.arraycopy(source, 0, target, 0, source.length);
        return target;
    }

    private String[] toArray(Iterable<String> values) {
        List<String> list = new ArrayList<String>();
        for (String value : values) {
            list.add(value);
        }
        return list.toArray(new String[list.size()]);
    }

    private boolean sameColumns(String[] left, String[] right) {
        if (left.length != right.length) {
            return false;
        }
        for (int i = 0; i < left.length; i++) {
            if (!left[i].equals(right[i])) {
                return false;
            }
        }
        return true;
    }
}
