/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package com.fernsehheft.enderchest.faststats.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

final class ErrorHelper {
    private static final int MESSAGE_LENGTH = Math.min(1000, Integer.getInteger("faststats.message-length", 500));
    private static final int STACK_TRACE_LENGTH = Math.min(500, Integer.getInteger("faststats.stack-trace-length", 300));
    private static final int STACK_TRACE_LIMIT = Math.min(50, Integer.getInteger("faststats.stack-trace-limit", 15));

    ErrorHelper() {
    }

    public static JsonObject compile(Throwable error, @Nullable List<String> suppress, boolean handled, List<Map.Entry<Pattern, String>> customPatterns) {
        JsonObject report = new JsonObject();
        String message = ErrorHelper.getAnonymizedMessage(error, customPatterns);
        JsonArray stacktrace = new JsonArray();
        String header = message != null ? error.getClass().getName() + ": " + message : error.getClass().getName();
        stacktrace.add(header);
        StackTraceElement[] elements = error.getStackTrace();
        List<String> stack = ErrorHelper.collapseStackTrace(elements);
        ArrayList<String> list = new ArrayList<String>(stack);
        if (suppress != null) {
            list.removeAll(suppress);
        }
        int traces = Math.min(list.size(), STACK_TRACE_LIMIT);
        ErrorHelper.populateTraces(traces, list, elements, stacktrace);
        ErrorHelper.appendCauseChain(error.getCause(), stack, suppress, stacktrace, customPatterns);
        report.addProperty("error", error.getClass().getName());
        if (message != null) {
            report.addProperty("message", message);
        }
        report.add("stack", (JsonElement)stacktrace);
        report.addProperty("handled", Boolean.valueOf(handled));
        return report;
    }

    private static void appendCauseChain(@Nullable Throwable cause, List<String> parentStack, @Nullable List<String> suppress, JsonArray stacktrace, List<Map.Entry<Pattern, String>> customPatterns) {
        ArrayList<String> toSuppress = new ArrayList<String>(parentStack);
        if (suppress != null) {
            toSuppress.addAll(suppress);
        }
        Set visited = Collections.newSetFromMap(new IdentityHashMap());
        while (cause != null && visited.add(cause)) {
            String causeMessage = ErrorHelper.getAnonymizedMessage(cause, customPatterns);
            String header = causeMessage != null ? "Caused by: " + cause.getClass().getName() + ": " + causeMessage : "Caused by: " + cause.getClass().getName();
            stacktrace.add(header);
            StackTraceElement[] causeElements = cause.getStackTrace();
            List<String> causeStack = ErrorHelper.collapseStackTrace(causeElements);
            ArrayList<String> causeList = new ArrayList<String>(causeStack);
            causeList.removeAll(toSuppress);
            int causeTraces = Math.min(causeList.size(), STACK_TRACE_LIMIT);
            ErrorHelper.populateTraces(causeTraces, causeList, causeElements, stacktrace);
            cause = cause.getCause();
        }
    }

    private static void populateTraces(int traces, List<String> list, StackTraceElement[] elements, JsonArray stacktrace) {
        int i;
        for (i = 0; i < traces; ++i) {
            String string = list.get(i);
            if (string.length() <= STACK_TRACE_LENGTH) {
                stacktrace.add("  at " + string);
                continue;
            }
            stacktrace.add("  at " + string.substring(0, STACK_TRACE_LENGTH) + "...");
        }
        if (traces > 0 && traces < list.size()) {
            stacktrace.add("  ... " + (list.size() - traces) + " more");
        } else {
            i = elements.length - list.size();
            if (i > 0) {
                stacktrace.add("  ... " + i + " more");
            }
        }
    }

    private static List<String> collapseStackTrace(StackTraceElement[] trace) {
        List<String> lines = Arrays.stream(trace).map(StackTraceElement::toString).toList();
        return ErrorHelper.collapseRepeatingPattern(lines);
    }

    private static List<String> collapseRepeatingPattern(List<String> lines) {
        List<String> deduplicated = ErrorHelper.collapseConsecutiveDuplicates(lines);
        int n = deduplicated.size();
        for (int cycleLen = 1; cycleLen <= n / 2; ++cycleLen) {
            boolean isPattern = true;
            int repetitions = 0;
            for (int i = 0; i < n; ++i) {
                if (!deduplicated.get(i).equals(deduplicated.get(i % cycleLen))) {
                    isPattern = false;
                    break;
                }
                if (i <= 0 || i % cycleLen != 0) continue;
                ++repetitions;
            }
            if (!isPattern || repetitions < 2) continue;
            return deduplicated.subList(0, cycleLen);
        }
        return deduplicated;
    }

    private static List<String> collapseConsecutiveDuplicates(List<String> lines) {
        if (lines.isEmpty()) {
            return lines;
        }
        ArrayList<String> result = new ArrayList<String>();
        String previous = null;
        for (String line : lines) {
            if (line.equals(previous)) continue;
            result.add(line);
            previous = line;
        }
        return result;
    }

    public static boolean isSameLoader(ClassLoader loader, Throwable error) {
        return ErrorHelper.isSameLoader(loader, error, Collections.newSetFromMap(new IdentityHashMap()));
    }

    private static boolean isSameLoader(ClassLoader loader, @Nullable Throwable error, Set<Throwable> visited) {
        if (error == null || !visited.add(error)) {
            return false;
        }
        StackTraceElement[] stackTrace = error.getStackTrace();
        if (stackTrace == null || stackTrace.length == 0) {
            return ErrorHelper.isSameLoader(loader, error.getCause(), visited);
        }
        int firstNonLibraryIndex = ErrorHelper.findFirstNonLibraryFrameIndex(stackTrace);
        if (firstNonLibraryIndex == -1) {
            return ErrorHelper.isSameLoader(loader, error.getCause(), visited);
        }
        int framesToCheck = Math.min(5, stackTrace.length - firstNonLibraryIndex);
        for (int i = 0; i < framesToCheck; ++i) {
            StackTraceElement frame = stackTrace[firstNonLibraryIndex + i];
            if (ErrorHelper.isLibraryClass(frame.getClassName()) || ErrorHelper.isFromLoader(frame, loader)) continue;
            return ErrorHelper.isSameLoader(loader, error.getCause(), visited);
        }
        return true;
    }

    private static int findFirstNonLibraryFrameIndex(StackTraceElement[] stackTrace) {
        for (int i = 0; i < stackTrace.length; ++i) {
            if (ErrorHelper.isLibraryClass(stackTrace[i].getClassName())) continue;
            return i;
        }
        return -1;
    }

    private static boolean isLibraryClass(String className) {
        return className.startsWith("java.") || className.startsWith("javax.") || className.startsWith("sun.") || className.startsWith("com.sun.") || className.startsWith("jdk.");
    }

    private static boolean isFromLoader(StackTraceElement frame, ClassLoader loader) {
        try {
            Class<?> clazz = Class.forName(frame.getClassName(), false, loader);
            return ErrorHelper.isSameClassLoader(clazz.getClassLoader(), loader);
        }
        catch (Throwable t) {
            return false;
        }
    }

    private static boolean isSameClassLoader(ClassLoader classLoader, ClassLoader loader) {
        ClassLoader current;
        if (classLoader == loader) {
            return true;
        }
        for (current = classLoader; current != null && current != loader; current = current.getParent()) {
        }
        return loader == current;
    }

    private static @Nullable String getAnonymizedMessage(Throwable error, List<Map.Entry<Pattern, String>> customPatterns) {
        String message = error.getMessage();
        if (message == null) {
            return null;
        }
        String truncated = message.length() > MESSAGE_LENGTH ? message.substring(0, MESSAGE_LENGTH) + "..." : message;
        for (Map.Entry<Pattern, String> entry : customPatterns) {
            truncated = entry.getKey().matcher(truncated).replaceAll(entry.getValue());
        }
        return truncated;
    }

    public static Pattern discordWebhookPattern() {
        return Pattern.compile("(https://discord\\.com/api/webhooks/\\d+/)[\\w-]+");
    }

    public static Pattern ipv4Pattern() {
        return Pattern.compile("\\b(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\b");
    }

    public static Pattern ipv6Pattern() {
        return Pattern.compile("(?i)\\b([0-9a-f]{1,4}:){7}[0-9a-f]{1,4}\\b|(?i)\\b([0-9a-f]{1,4}:){1,7}:\\b|(?i)\\b([0-9a-f]{1,4}:){1,6}:[0-9a-f]{1,4}\\b|(?i)\\b([0-9a-f]{1,4}:){1,5}(:[0-9a-f]{1,4}){1,2}\\b|(?i)\\b([0-9a-f]{1,4}:){1,4}(:[0-9a-f]{1,4}){1,3}\\b|(?i)\\b([0-9a-f]{1,4}:){1,3}(:[0-9a-f]{1,4}){1,4}\\b|(?i)\\b([0-9a-f]{1,4}:){1,2}(:[0-9a-f]{1,4}){1,5}\\b|(?i)\\b[0-9a-f]{1,4}:(:[0-9a-f]{1,4}){1,6}\\b|(?i)\\b:(:[0-9a-f]{1,4}){1,7}\\b|(?i)\\b::([0-9a-f]{1,4}:){0,5}[0-9a-f]{1,4}\\b|(?i)\\b::\\b");
    }

    public static Pattern jdbcUrlPattern() {
        return Pattern.compile("(jdbc:[^:]+://[^:]+:(?:\\d+:)?)[^@]+(@)");
    }

    public static Pattern userHomePathPattern() {
        return Pattern.compile("(/home/)[^/\\s]+|(/Users/)[^/\\s]+|((?i)[A-Z]:\\\\Users\\\\)[^\\\\\\s]+");
    }

    public static Optional<Pattern> usernamePattern() {
        return Optional.ofNullable(System.getProperty("user.name")).filter(s -> s.trim().length() > 2).map(Pattern::quote).map(Pattern::compile);
    }
}

