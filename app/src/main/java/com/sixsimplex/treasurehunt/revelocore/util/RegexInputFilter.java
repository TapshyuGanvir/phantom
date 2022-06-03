package com.sixsimplex.treasurehunt.revelocore.util;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

import java.util.regex.Pattern;

public class RegexInputFilter implements InputFilter {

    private final String CLASS_NAME = RegexInputFilter.class.getSimpleName();
    private Pattern mPattern;
    private EditText mEditText;
    private int length = 0;



    public RegexInputFilter(String pattern, EditText editText) {
        this(Pattern.compile(pattern), editText);
    }

    public RegexInputFilter(Pattern pattern, EditText editText) {
        if (pattern == null) {
            throw new IllegalArgumentException(CLASS_NAME + " requires a regex.");
        }

        mEditText = editText;
        mPattern = pattern;
    }

    public void setLength(int length) {
        this.length = length;

    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dStart, int dEnd) {
        if (length == 0) {
            return checkForFilter(source, start, end, dest, dStart, dEnd);
        } else {
            int keep = length - (dest.length() - (dEnd - dStart));
            if (keep > 0) {
                return checkForFilter(source, start, end, dest, dStart, dEnd);
            } else {
                return "";
            }
        }

       /* if (source.length() == 0) {
            return null;
        } else {
            Matcher matcher = mPattern.matcher(source);
            if (!matcher.matches()) {
                mEditText.setError("Only letters, _ and numbers are allowed.");
                return "";
            } else {
                return null;
            }
        }*/
    }

    private CharSequence checkForFilter(CharSequence source, int start, int end, Spanned dest, int dStart, int dEnd) {
        try {
            return filter1(source, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private CharSequence filter1(CharSequence source, int start, int end) {
        // Only keep characters that are alphanumeric
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            String s = String.valueOf(c);
            if (Character.isLetterOrDigit(c) || s.equalsIgnoreCase("_")) {
                builder.append(c);
            } else {
                mEditText.setError("Only letters, _ and numbers are allowed.");
            }
        }
        // If all characters are valid, return null, otherwise only return the filtered characters
        boolean allCharactersValid = (builder.length() == end - start);
        return allCharactersValid ? null : builder.toString();
    }
}
