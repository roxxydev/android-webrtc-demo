package com.devdroid.webrtcandroid.util;

public class StringUtils {

    /** Validate if String value is not null, not empty, and not a whitespace character. */
    public static boolean isValid(String value) {
        return (value != null && !value.isEmpty() && value.length() >0 && value != " ");
    }
}
