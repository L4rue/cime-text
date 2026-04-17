package com.efile;

/**
 * Parse options for E file parser.
 */
public final class ParseOptions {

    public static final ParseOptions DEFAULT = new ParseOptions(MalformedRowPolicy.THROW);

    private final MalformedRowPolicy malformedRowPolicy;

    public ParseOptions() {
        this(MalformedRowPolicy.THROW);
    }

    public ParseOptions(MalformedRowPolicy malformedRowPolicy) {
        this.malformedRowPolicy = malformedRowPolicy == null ? MalformedRowPolicy.THROW : malformedRowPolicy;
    }

    public static ParseOptions strict() {
        return DEFAULT;
    }

    public static ParseOptions skipMalformedRows() {
        return new ParseOptions(MalformedRowPolicy.SKIP_ROW);
    }

    public MalformedRowPolicy getMalformedRowPolicy() {
        return malformedRowPolicy;
    }

    public ParseOptions withMalformedRowPolicy(MalformedRowPolicy policy) {
        return new ParseOptions(policy);
    }
}
