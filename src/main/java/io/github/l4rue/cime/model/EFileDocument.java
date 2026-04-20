package io.github.l4rue.cime.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a parsed E-language file, including file-level metadata and tables.
 *
 * @author dingyh
 */
public class EFileDocument {

    private EHeader header;
    private List<ETable> tables;

    /**
     * Creates an empty E-language document.
     */
    public EFileDocument() {
        header = new EHeader();
        tables = new ArrayList<ETable>();
    }

    /**
     * Creates a document with the supplied tables and no file-level metadata.
     *
     * @param tables parsed tables
     */
    public EFileDocument(List<ETable> tables) {
        this();
        setTables(tables);
    }

    /**
     * Returns the file-level header metadata.
     *
     * @return header metadata, never {@code null}
     */
    public EHeader getHeader() {
        return header;
    }

    /**
     * Sets the file-level header metadata.
     *
     * @param header header metadata
     */
    public void setHeader(EHeader header) {
        this.header = header == null ? new EHeader() : header;
    }

    /**
     * Returns parsed tables in source order.
     *
     * @return mutable table list
     */
    public List<ETable> getTables() {
        return tables;
    }

    /**
     * Replaces parsed tables.
     *
     * @param tables parsed tables
     */
    public void setTables(List<ETable> tables) {
        this.tables = tables == null ? new ArrayList<ETable>() : tables;
    }
}
