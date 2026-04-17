package com.efile;

/**
 * Parse options for E file parser.
 *
 * @author dingyh
 */
public final class ParseOptions {

    /**
     * Shared strict configuration that fails fast on malformed rows.
     */
    public static final ParseOptions DEFAULT = new ParseOptions(MalformedRowPolicy.THROW);

    private final MalformedRowPolicy malformedRowPolicy;

    /**
     * Creates parse options that use {@link MalformedRowPolicy#THROW}.
     */
    public ParseOptions() {
        this(MalformedRowPolicy.THROW);
    }

    /**
     * Creates parse options with the supplied malformed-row policy.
     *
     * @param malformedRowPolicy policy applied when a row cannot be parsed;
     *                           {@code null} falls back to {@link MalformedRowPolicy#THROW}
     */
    public ParseOptions(MalformedRowPolicy malformedRowPolicy) {
        this.malformedRowPolicy = malformedRowPolicy == null ? MalformedRowPolicy.THROW : malformedRowPolicy;
    }

    /**
     * Returns the default strict configuration.
     *
     * @return shared strict parse options instance
     */
    public static ParseOptions strict() {
        return DEFAULT;
    }

    /**
     * Returns options that skip malformed data rows and continue parsing.
     *
     * @return parse options configured with {@link MalformedRowPolicy#SKIP_ROW}
     */
    public static ParseOptions skipMalformedRows() {
        return new ParseOptions(MalformedRowPolicy.SKIP_ROW);
    }

    /**
     * Returns the malformed-row handling policy.
     *
     * @return active malformed-row policy
     */
    public MalformedRowPolicy getMalformedRowPolicy() {
        return malformedRowPolicy;
    }

    /**
     * Creates a copy of these options with a different malformed-row policy.
     *
     * @param policy new malformed-row policy; {@code null} falls back to {@link MalformedRowPolicy#THROW}
     * @return new parse options instance using the requested policy
     */
    public ParseOptions withMalformedRowPolicy(MalformedRowPolicy policy) {
        return new ParseOptions(policy);
    }
}
