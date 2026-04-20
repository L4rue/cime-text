package io.github.l4rue.cime.parse;

import io.github.l4rue.cime.model.EFileDocument;
import io.github.l4rue.cime.model.ETable;

import java.io.File;
import java.util.List;

/**
 * Contract for parsing E-language files into table models.
 *
 * @author dingyh
 */
public interface EFileParse {

    /**
     * Parses an E-language file with the default parser options.
     *
     * @param file source file to parse
     * @return parsed tables in source order
     * @throws Exception when parsing fails
     */
    List<ETable> parseFile(File file) throws Exception;

    /**
     * Parses an E-language file with explicit parser options.
     *
     * @param file    source file to parse
     * @param options parse options that control malformed-row handling
     * @return parsed tables in source order
     * @throws Exception when parsing fails
     */
    List<ETable> parseFile(File file, ParseOptions options) throws Exception;

    /**
     * Parses an E-language file with the default parser options and preserves file/table metadata when supported.
     *
     * @param file source file to parse
     * @return parsed document
     * @throws Exception when parsing fails
     */
    default EFileDocument parseDocument(File file) throws Exception {
        return new EFileDocument(parseFile(file));
    }

    /**
     * Parses an E-language file with explicit parser options and preserves file/table metadata when supported.
     *
     * @param file    source file to parse
     * @param options parse options that control malformed-row handling
     * @return parsed document
     * @throws Exception when parsing fails
     */
    default EFileDocument parseDocument(File file, ParseOptions options) throws Exception {
        return new EFileDocument(parseFile(file, options));
    }
}
