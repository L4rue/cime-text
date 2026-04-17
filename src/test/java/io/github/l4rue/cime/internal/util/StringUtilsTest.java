package io.github.l4rue.cime.internal.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for whitespace-aware string tokenization utilities.
 *
 * @author dingyh
 */
public class StringUtilsTest {

    @Test
    public void shouldReturnEmptyArrayWhenInputIsNull() {
        Assert.assertArrayEquals(new String[0], StringUtils.splitLineWithSpace(null));
    }

    @Test
    public void shouldReturnEmptyArrayWhenInputIsBlank() {
        Assert.assertArrayEquals(new String[0], StringUtils.splitLineWithSpace("  \t  \r\n  "));
    }

    @Test
    public void shouldTreatSpaceAsSeparator() {
        Assert.assertArrayEquals(new String[]{"A", "B", "C"}, StringUtils.splitLineWithSpace("A B C"));
    }

    @Test
    public void shouldTreatTabAsSeparator() {
        Assert.assertArrayEquals(new String[]{"A", "B", "C"}, StringUtils.splitLineWithSpace("A\tB\tC"));
    }

    @Test
    public void shouldTreatMixedWhitespaceAsSeparator() {
        Assert.assertArrayEquals(new String[]{"A", "B", "C"}, StringUtils.splitLineWithSpace("A \t B   C"));
    }

    @Test
    public void shouldCompressConsecutiveSeparators() {
        Assert.assertArrayEquals(new String[]{"A", "B", "C"}, StringUtils.splitLineWithSpace("  A   B\t\tC  "));
    }

    @Test
    public void shouldKeepSingleQuotedToken() {
        String line = "2 发生时间 '2011-11-03 00:00:02.0'";
        Assert.assertArrayEquals(new String[]{"2", "发生时间", "2011-11-03 00:00:02.0"},
                StringUtils.splitLineWithSpace(line));
    }

    @Test
    public void shouldKeepDoubleQuotedToken() {
        String line = "2 发生时间 \"2011-11-03 00:00:02.0\"";
        Assert.assertArrayEquals(new String[]{"2", "发生时间", "2011-11-03 00:00:02.0"},
                StringUtils.splitLineWithSpace(line));
    }

    @Test
    public void shouldTrimOuterWhitespaceAroundQuotedToken() {
        String line = "  'A B'   \t  C  ";
        Assert.assertArrayEquals(new String[]{"A B", "C"}, StringUtils.splitLineWithSpace(line));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSingleQuoteNotClosed() {
        StringUtils.splitLineWithSpace("A 'B C");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenDoubleQuoteNotClosed() {
        StringUtils.splitLineWithSpace("A \"B C");
    }
}
