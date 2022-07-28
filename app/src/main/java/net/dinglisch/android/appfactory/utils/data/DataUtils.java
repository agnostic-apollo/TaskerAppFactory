package net.dinglisch.android.appfactory.utils.data;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Strings;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class DataUtils {

    /** Max safe limit of data size to prevent TransactionTooLargeException when transferring data
     * inside or to other apps via transactions. */
    public static final int TRANSACTION_SIZE_LIMIT_IN_BYTES = 100 * 1024; // 100KB

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String getTruncatedCommandOutput(String text, int maxLength, boolean fromEnd, boolean onNewline, boolean addPrefix) {
        if (text == null) return null;

        String prefix = "(truncated) ";

        if (addPrefix)
            maxLength = maxLength - prefix.length();

        if (maxLength < 0 || text.length() < maxLength) return text;

        if (fromEnd) {
            text = text.substring(0, maxLength);
        } else {
            int cutOffIndex = text.length() - maxLength;

            if (onNewline) {
                int nextNewlineIndex = text.indexOf('\n', cutOffIndex);
                if (nextNewlineIndex != -1 && nextNewlineIndex != text.length() - 1) {
                    cutOffIndex = nextNewlineIndex + 1;
                }
            }
            text = text.substring(cutOffIndex);
        }

        if (addPrefix)
            text = prefix + text;

        return text;
    }



    /**
     * Get the object itself if it is not {@code null}, otherwise default.
     *
     * @param object The {@link Object} to check.
     * @param def The default {@link Object}.
     * @return Returns {@code object} if it is not {@code null}, otherwise returns {@code def}.
     */
    public static <T> T getDefaultIfNull(@Nullable T object, @Nullable T def) {
        return (object == null) ? def : object;
    }

    /**
     * Get the {@link String} itself if it is not {@code null} or empty, otherwise default.
     *
     * @param value The {@link String} to check.
     * @param def The default {@link String}.
     * @return Returns {@code value} if it is not {@code null} or empty, otherwise returns {@code def}.
     */
    public static String getDefaultIfUnset(@Nullable String value, String def) {
        return (value == null || value.isEmpty()) ? def : value;
    }

    /** Check if a string is null or empty. */
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

}
