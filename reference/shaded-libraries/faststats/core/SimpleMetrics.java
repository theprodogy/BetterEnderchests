/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package com.fernsehheft.enderchest.faststats.core;

import com.fernsehheft.enderchest.faststats.core.ErrorTracker;
import com.fernsehheft.enderchest.faststats.core.Metrics;
import com.fernsehheft.enderchest.faststats.core.SimpleErrorTracker;
import com.fernsehheft.enderchest.faststats.core.Token;
import com.fernsehheft.enderchest.faststats.core.data.Metric;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.zip.GZIPOutputStream;
import org.jetbrains.annotations.Async;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

public abstract class SimpleMetrics
implements Metrics {
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3L)).version(HttpClient.Version.HTTP_1_1).build();
    private @Nullable ScheduledExecutorService executor = null;
    private final Set<Metric<?>> metrics;
    private final Config config;
    @Token
    private final String token;
    private final @Nullable ErrorTracker tracker;
    private final @Nullable Runnable flush;
    private final URI url;
    private final boolean debug;
    private final String SDK_NAME;
    private final String SDK_VERSION;
    private final String BUILD_ID;
    private final String javaVendor;
    private final String javaVersion;
    private final String osArch;
    private final String osName;
    private final String osVersion;
    private final int coreCount;

    @Contract(mutates="io")
    protected SimpleMetrics(Factory<?, ?> factory, Config config) throws IllegalStateException {
        Properties properties = new Properties();
        try (InputStream stream = this.getClass().getResourceAsStream("/META-INF/faststats.properties");){
            if (stream != null) {
                properties.load(stream);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.SDK_NAME = properties.getProperty("name", "unknown");
        this.SDK_VERSION = properties.getProperty("version", "unknown");
        this.BUILD_ID = properties.getProperty("build-id", "unknown");
        this.javaVendor = System.getProperty("java.vendor");
        this.javaVersion = System.getProperty("java.version");
        this.osArch = System.getProperty("os.arch");
        this.osName = System.getProperty("os.name");
        this.osVersion = System.getProperty("os.version");
        this.coreCount = Runtime.getRuntime().availableProcessors();
        if (factory.token == null) {
            throw new IllegalStateException("Token must be specified");
        }
        this.config = config;
        this.metrics = config.additionalMetrics ? Set.copyOf(factory.metrics) : Set.of();
        this.debug = factory.debug || Boolean.getBoolean("faststats.debug") || config.debug();
        this.token = factory.token;
        this.tracker = config.errorTracking ? factory.tracker : null;
        this.flush = factory.flush;
        this.url = factory.url;
    }

    @Contract(mutates="io")
    protected SimpleMetrics(Factory<?, ?> factory, Path config) throws IllegalStateException {
        this(factory, Config.read(config));
    }

    @VisibleForTesting
    protected SimpleMetrics(Config config, Set<Metric<?>> metrics, @Token String token, @Nullable ErrorTracker tracker, @Nullable Runnable flush, URI url, boolean debug) {
        Properties properties = new Properties();
        try (InputStream stream = this.getClass().getResourceAsStream("/META-INF/faststats.properties");){
            if (stream != null) {
                properties.load(stream);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.SDK_NAME = properties.getProperty("name", "unknown");
        this.SDK_VERSION = properties.getProperty("version", "unknown");
        this.BUILD_ID = properties.getProperty("build-id", "unknown");
        this.javaVendor = System.getProperty("java.vendor");
        this.javaVersion = System.getProperty("java.version");
        this.osArch = System.getProperty("os.arch");
        this.osName = System.getProperty("os.name");
        this.osVersion = System.getProperty("os.version");
        this.coreCount = Runtime.getRuntime().availableProcessors();
        if (!token.matches("[a-z0-9]{32}")) {
            throw new IllegalArgumentException("Invalid token '" + token + "', must match '[a-z0-9]{32}'");
        }
        this.metrics = config.additionalMetrics ? Set.copyOf(metrics) : Set.of();
        this.config = config;
        this.debug = debug;
        this.token = token;
        this.tracker = tracker;
        this.flush = flush;
        this.url = url;
    }

    protected String getOnboardingMessage() {
        return "This plugin uses FastStats to collect anonymous usage statistics.\nNo personal or identifying information is ever collected.\nTo opt out, set 'enabled=false' in the metrics configuration file.\nLearn more at: https://faststats.dev/info\n\nSince this is your first start with FastStats, metrics submission will not start\nuntil you restart the server to allow you to opt out if you prefer.";
    }

    protected long getInitialDelay() {
        return TimeUnit.SECONDS.toMillis(Long.getLong("faststats.initial-delay", 30L));
    }

    protected long getPeriod() {
        return TimeUnit.MINUTES.toMillis(30L);
    }

    @Async.Schedule
    @MustBeInvokedByOverriders
    protected void startSubmitting() {
        this.startSubmitting(this.getInitialDelay(), this.getPeriod(), TimeUnit.MILLISECONDS);
    }

    private void startSubmitting(long initialDelay, long period, TimeUnit unit) {
        if (Boolean.getBoolean("faststats.first-run")) {
            this.info("Skipping metrics submission due to first-run flag");
            return;
        }
        if (this.config.firstRun) {
            String[] split;
            int separatorLength = 0;
            for (String s : split = this.getOnboardingMessage().split("\n")) {
                if (s.length() <= separatorLength) continue;
                separatorLength = s.length();
            }
            this.printInfo("-".repeat(separatorLength));
            for (String s : split) {
                this.printInfo(s);
            }
            this.printInfo("-".repeat(separatorLength));
            System.setProperty("faststats.first-run", "true");
            if (!this.config.externallyManaged()) {
                return;
            }
        }
        boolean enabled = Boolean.parseBoolean(System.getProperty("faststats.enabled", "true"));
        if (!this.config.enabled() || !enabled) {
            this.warn("Metrics disabled, not starting submission");
            return;
        }
        if (this.isSubmitting()) {
            this.warn("Metrics already submitting, not starting again");
            return;
        }
        this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "metrics-submitter");
            thread.setDaemon(true);
            return thread;
        });
        this.info("Starting metrics submission");
        this.executor.scheduleAtFixedRate(this::submit, Math.max(0L, initialDelay), Math.max(1000L, period), unit);
    }

    protected boolean isSubmitting() {
        return this.executor != null && !this.executor.isShutdown();
    }

    public boolean submit() {
        try {
            return this.submitNow();
        }
        catch (Throwable t) {
            this.error("Failed to submit metrics", t);
            return false;
        }
    }

    private boolean submitNow() throws IOException {
        String data = this.createData().toString();
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        this.info("Uncompressed data: " + data);
        try (ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
             GZIPOutputStream output = new GZIPOutputStream(byteOutput)) {
            output.write(bytes);
            output.finish();
            byte[] compressed = byteOutput.toByteArray();
            this.info("Compressed size: " + compressed.length + " bytes");
            HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofByteArray(compressed)).header("Content-Encoding", "gzip").header("Content-Type", "application/octet-stream").header("Authorization", "Bearer " + this.getToken()).header("User-Agent", "FastStats Metrics " + this.SDK_NAME + "/" + this.SDK_VERSION).timeout(Duration.ofSeconds(3L)).uri(this.url).build();
            this.info("Sending metrics to: " + String.valueOf(this.url));
            try {
                HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                int statusCode = response.statusCode();
                String body = response.body();
                if (statusCode >= 200 && statusCode < 300) {
                    this.info("Metrics submitted with status code: " + statusCode + " (" + body + ")");
                    this.getErrorTracker().map(SimpleErrorTracker.class::cast).ifPresent(SimpleErrorTracker::clear);
                    if (this.flush != null) this.flush.run();
                    return true;
                }
                if (statusCode >= 300 && statusCode < 400) this.warn("Received redirect response from metrics server: " + statusCode + " (" + body + ")");
                else if (statusCode >= 400 && statusCode < 500) this.error("Submitted invalid request to metrics server: " + statusCode + " (" + body + ")", null);
                else if (statusCode >= 500 && statusCode < 600) this.error("Received server error response from metrics server: " + statusCode + " (" + body + ")", null);
                else this.warn("Received unexpected response from metrics server: " + statusCode + " (" + body + ")");
            } catch (HttpConnectTimeoutException t) {
                this.error("Metrics submission timed out after 3 seconds: " + String.valueOf(this.url), null);
            } catch (ConnectException t) {
                this.error("Failed to connect to metrics server: " + String.valueOf(this.url), null);
            } catch (Throwable t) {
                this.error("Failed to submit metrics", t);
            }
            return false;
        }
    }

    protected JsonObject createData() {
        JsonObject data = new JsonObject();
        JsonObject metrics = new JsonObject();
        metrics.addProperty("core_count", (Number)this.coreCount);
        metrics.addProperty("java_vendor", this.javaVendor);
        metrics.addProperty("java_version", this.javaVersion);
        metrics.addProperty("os_arch", this.osArch);
        metrics.addProperty("os_name", this.osName);
        metrics.addProperty("os_version", this.osVersion);
        try {
            this.appendDefaultData(metrics);
        }
        catch (Throwable t) {
            this.error("Failed to append default data", t);
            this.getErrorTracker().ifPresent(tracker -> tracker.trackError(t));
        }
        this.metrics.forEach(metric -> {
            try {
                metric.getData().ifPresent(element -> metrics.add(metric.getId(), element));
            }
            catch (Throwable t) {
                this.error("Failed to build metric data: " + metric.getId(), t);
                this.getErrorTracker().ifPresent(tracker -> tracker.trackError(t));
            }
        });
        data.addProperty("identifier", this.config.serverId().toString());
        data.add("data", (JsonElement)metrics);
        this.getErrorTracker().map(SimpleErrorTracker.class::cast).map(tracker -> tracker.getData(this.BUILD_ID)).filter(errors -> !errors.isEmpty()).ifPresent(errors -> data.add("errors", (JsonElement)errors));
        return data;
    }

    @Override
    @Token
    public String getToken() {
        return this.token;
    }

    @Override
    public Optional<ErrorTracker> getErrorTracker() {
        return Optional.ofNullable(this.tracker);
    }

    @Override
    public Metrics.Config getConfig() {
        return this.config;
    }

    @Contract(mutates="param1")
    protected abstract void appendDefaultData(JsonObject var1);

    protected void error(String message, @Nullable Throwable throwable) {
        if (this.debug) {
            this.printError("[" + this.getClass().getName() + "]: " + message, throwable);
        }
    }

    protected void warn(String message) {
        if (this.debug) {
            this.printWarning("[" + this.getClass().getName() + "]: " + message);
        }
    }

    protected void info(String message) {
        if (this.debug) {
            this.printInfo("[" + this.getClass().getName() + "]: " + message);
        }
    }

    protected abstract void printError(String var1, @Nullable Throwable var2);

    protected abstract void printInfo(String var1);

    protected abstract void printWarning(String var1);

    @Override
    public void shutdown() {
        this.getErrorTracker().ifPresent(ErrorTracker::detachErrorContext);
        if (this.executor != null) {
            try {
                this.info("Shutting down metrics submission");
                this.executor.shutdown();
                this.submit();
            }
            catch (Throwable t) {
                this.error("Failed to submit metrics on shutdown", t);
            }
            finally {
                this.executor = null;
            }
        }
    }

    public static abstract class Factory<T, F extends Metrics.Factory<T, F>>
    implements Metrics.Factory<T, F> {
        private final Set<Metric<?>> metrics = new HashSet(0);
        private URI url = URI.create("https://metrics.faststats.dev/v1/collect");
        private @Nullable ErrorTracker tracker;
        private @Nullable Runnable flush;
        private @Nullable String token;
        private boolean debug = false;

        @Override
        public F addMetric(Metric<?> metric) throws IllegalArgumentException {
            if (!this.metrics.add(metric)) {
                throw new IllegalArgumentException("Metric already added: " + metric.getId());
            }
            return (F)this;
        }

        @Override
        public F onFlush(Runnable flush) {
            this.flush = flush;
            return (F)this;
        }

        @Override
        public F errorTracker(ErrorTracker tracker) {
            this.tracker = tracker;
            return (F)this;
        }

        @Override
        public F debug(boolean enabled) {
            this.debug = enabled;
            return (F)this;
        }

        @Override
        public F token(@Token String token) throws IllegalArgumentException {
            if (!token.matches("[a-z0-9]{32}")) {
                throw new IllegalArgumentException("Invalid token '" + token + "', must match '[a-z0-9]{32}'");
            }
            this.token = token;
            return (F)this;
        }

        @Override
        public F url(URI url) {
            this.url = url;
            return (F)this;
        }
    }

    public record Config(UUID serverId, boolean additionalMetrics, boolean debug, boolean enabled, boolean errorTracking, boolean firstRun, boolean externallyManaged) implements Metrics.Config
    {
        public static final String DEFAULT_COMMENT = " FastStats (https://faststats.dev) collects anonymous usage statistics for plugin developers.\n# This helps developers understand how their projects are used in the real world.\n#\n# No IP addresses, player data, or personal information is collected.\n# The server ID below is randomly generated and can be regenerated at any time.\n#\n# Enabling metrics has no noticeable performance impact.\n# Keeping metrics enabled is recommended, but you can opt out by setting\n# 'enabled=false' in plugins/faststats/config.properties.\n#\n# If you suspect a plugin is collecting personal data or bypassing the \"enabled\" option,\n# please report it at: https://faststats.dev/abuse\n#\n# For more information, visit: https://faststats.dev/info\n";

        @Contract(mutates="io")
        public static Config read(Path file) throws RuntimeException {
            return Config.read(file, DEFAULT_COMMENT, false, false);
        }

        @Contract(mutates="io")
        public static Config read(Path file, String comment, boolean externallyManaged, boolean externallyEnabled) throws RuntimeException {
            Optional<Properties> properties = Config.readOrEmpty(file);
            boolean firstRun = properties.isEmpty();
            AtomicBoolean saveConfig = new AtomicBoolean(firstRun);
            UUID serverId = properties.map(object -> object.getProperty("serverId")).map(string -> {
                try {
                    String corrected;
                    String trimmed = string.trim();
                    String string2 = corrected = trimmed.length() > 36 ? trimmed.substring(0, 36) : trimmed;
                    if (!corrected.equals(string)) {
                        saveConfig.set(true);
                    }
                    return UUID.fromString(corrected);
                }
                catch (IllegalArgumentException e) {
                    saveConfig.set(true);
                    return UUID.randomUUID();
                }
            }).orElseGet(() -> {
                saveConfig.set(true);
                return UUID.randomUUID();
            });
            BiPredicate<String, Boolean> predicate = (key, defaultValue) -> properties.map(object -> object.getProperty((String)key)).map(Boolean::parseBoolean).orElseGet(() -> {
                saveConfig.set(true);
                return defaultValue;
            });
            boolean enabled = externallyManaged ? externallyEnabled : predicate.test("enabled", true);
            boolean errorTracking = predicate.test("submitErrors", true);
            boolean additionalMetrics = predicate.test("submitAdditionalMetrics", true);
            boolean debug = predicate.test("debug", false);
            if (saveConfig.get()) {
                try {
                    Config.save(file, externallyManaged, comment, serverId, enabled, errorTracking, additionalMetrics, debug);
                }
                catch (IOException e) {
                    throw new RuntimeException("Failed to save metrics config", e);
                }
            }
            return new Config(serverId, additionalMetrics, debug, enabled, errorTracking, firstRun, externallyManaged);
        }

        private static Optional<Properties> readOrEmpty(Path file) throws RuntimeException {
            Optional<Properties> optional;
            block9: {
                if (!Files.isRegularFile(file, new LinkOption[0])) {
                    return Optional.empty();
                }
                BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
                try {
                    Properties properties = new Properties();
                    properties.load(reader);
                    optional = Optional.of(properties);
                    if (reader == null) break block9;
                }
                catch (Throwable throwable) {
                    try {
                        if (reader != null) {
                            try {
                                reader.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (IOException e) {
                        throw new RuntimeException("Failed to read metrics config", e);
                    }
                }
                reader.close();
            }
            return optional;
        }

        private static void save(Path file, boolean externallyManaged, String comment, UUID serverId, boolean enabled, boolean errorTracking, boolean additionalMetrics, boolean debug) throws IOException {
            Files.createDirectories(file.getParent(), new FileAttribute[0]);
            try (OutputStream out = Files.newOutputStream(file, new OpenOption[0]);
                 OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);){
                Properties properties = new Properties();
                properties.setProperty("serverId", serverId.toString());
                if (!externallyManaged) {
                    properties.setProperty("enabled", Boolean.toString(enabled));
                }
                properties.setProperty("submitErrors", Boolean.toString(errorTracking));
                properties.setProperty("submitAdditionalMetrics", Boolean.toString(additionalMetrics));
                properties.setProperty("debug", Boolean.toString(debug));
                properties.store(writer, comment);
            }
        }
    }
}
