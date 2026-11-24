package at.handoutmanager.backend.common;

public final class StringUtil {

    private StringUtil() {
    }

    // One state for "empty": null. Blank input becomes null; real values stay untouched,
    // surrounding spaces included.
    public static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
