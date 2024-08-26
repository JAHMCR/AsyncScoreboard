package fr.mrmicky.fastboard.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    private final String SECTION_SYMBOL = "§";
    private final String CHAT_COLOR_SYMBOL = "&";
    private final String FORMAT_SYMBOLS = "[" + SECTION_SYMBOL + CHAT_COLOR_SYMBOL + "]";
    private final String COLOR_CHARACTERS = "[\\da-f]";
    private final String FORMAT_CHARACTERS = "[l-or]";
    private final String COLOR_OR_FORMAT_REGEX_STRING_SINGLE = "(?:" + FORMAT_SYMBOLS + "(?:" + COLOR_CHARACTERS + "|" + FORMAT_CHARACTERS + "))";
    private final String COLOR_OR_FORMAT_REGEX_STRING_MULTIPLE = "(?:" + COLOR_OR_FORMAT_REGEX_STRING_SINGLE + ")+";
    private final String SPLIT_FORMATS_STRING = "(?:^|" + COLOR_OR_FORMAT_REGEX_STRING_MULTIPLE + ")(?:(?!" + COLOR_OR_FORMAT_REGEX_STRING_MULTIPLE + ").)+";

    private final Pattern splitPattern = Pattern.compile(SPLIT_FORMATS_STRING);
    private final Pattern formatPattern = Pattern.compile(COLOR_OR_FORMAT_REGEX_STRING_MULTIPLE);

    public List<String> splitLine(String line, int maxLength) {
        List<String> result = new ArrayList<>(2);

        // Function to assign prefix and suffix
        BiConsumer<String, StringBuilder[]> assignPrefixSuffix = (inputLine, builders) -> {
            Matcher splitMatcher = splitPattern.matcher(inputLine);
            StringBuilder prefix = builders[0];
            StringBuilder suffix = builders[1];

            while (splitMatcher.find()) {
                String component = splitMatcher.group();
                int remainingPrefixLength = maxLength - prefix.length();
                int remainingSuffixLength = maxLength - suffix.length();

                if (suffix.length() > 0) {
                    // Handle suffix
                    if (remainingSuffixLength > 0) {
                        suffix.append(component, 0, Math.min(component.length(), remainingSuffixLength));
                    }
                } else if (remainingPrefixLength >= component.length()) {
                    // Handle prefix
                    prefix.append(component);
                } else {
                    // Split component between prefix and suffix
                    Matcher formatMatcher = formatPattern.matcher(component);
                    String format = formatMatcher.find() ? formatMatcher.group() : "";

                    String content = component.substring(format.length());
                    int remainingContentLength = remainingPrefixLength - format.length();

                    if (remainingContentLength > 0) {
                        int remainingLength = remainingContentLength + remainingSuffixLength;

                        prefix.append(format).append(content, 0, remainingContentLength);
                        suffix.append(format).append(content, remainingContentLength, Math.min(content.length(), remainingLength));
                    } else {
                        suffix.append(component, 0, Math.min(component.length(), remainingSuffixLength));
                    }
                }
            }
        };

        StringBuilder[] builders = {new StringBuilder(), new StringBuilder()};

        if (line == null || line.isEmpty()) {
            builders[0].append(ChatColor.RESET);
        } else if (line.length() <= maxLength) {
            builders[0].append(line);
        } else {
            assignPrefixSuffix.accept(line, builders);
        }

        result.add(builders[0].toString());
        result.add(builders[1].toString());

        return result;
    }
}
