package io.github.l4rue.cime.write;

import io.github.l4rue.cime.model.ETable;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Contract for writing table models to E-language text files.
 *
 * @author dingyh
 */
public interface EFileWrite {

    /**
     * Writes tables to a UTF-8 encoded E-language file with the default options.
     *
     * @param tables source tables
     * @param file   target file
     * @throws IOException when the target file cannot be written
     */
    void writeFile(List<ETable> tables, File file) throws IOException;

    /**
     * Writes tables to a UTF-8 encoded E-language file.
     *
     * @param tables  source tables
     * @param file    target file
     * @param options write options; defaults are used when {@code null}
     * @throws IOException when the target file cannot be written
     */
    void writeFile(List<ETable> tables, File file, WriteOptions options) throws IOException;

    /**
     * Serializes tables to E-language text.
     *
     * @param tables  source tables
     * @param options write options; defaults are used when {@code null}
     * @return serialized E-language text
     */
    String writeToString(List<ETable> tables, WriteOptions options);
}
