package net.stomer;

import co.elastic.logstash.api.*;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporter;
import io.opentelemetry.sdk.logs.LogProcessor;
import io.opentelemetry.sdk.logs.SdkLogEmitterProvider;
import io.opentelemetry.sdk.logs.export.BatchLogProcessor;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.resources.Resource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CountDownLatch;

// class name must match plugin name
@LogstashPlugin(name = "opentelemetry")
public class Opentelemetry implements Output {

    public static final PluginConfigSpec<URI> ENDPOINT_CONFIG =
            PluginConfigSpec.uriSetting("endpoint", null, false, true);
    public static final PluginConfigSpec<String> ENDPOINT_TYPE_CONFIG =
            PluginConfigSpec.stringSetting("endpoint_type", "", true, false);
    public static final PluginConfigSpec<String> PROTOCOL_CONFIG =
            PluginConfigSpec.stringSetting("protocol", "", false, false);
    public static final PluginConfigSpec<String> COMPRESSION_CONFIG =
            PluginConfigSpec.stringSetting("compression", "none");
    private enum VALID_PROTOCOL_OPTIONS {grpc, http};
    private final String id;
    private final PrintStream printer;
    private final CountDownLatch done = new CountDownLatch(1);
    private volatile boolean stopped = false;
    private final SdkLogEmitterProvider sdkLogEmitterProvider;

    // all plugins must provide a constructor that accepts id, Configuration, and Context
    public Opentelemetry(final String id, final Configuration configuration, final Context context) {
        this(id, configuration, context, System.out);
    }

    private io.opentelemetry.context.Context getContextForEvent(Event event) {
        TraceState ts = TraceState.getDefault();
        TraceFlags tf = TraceFlags.getDefault();
        SpanContext sp = null;
        try {
            sp = SpanContext.create(
                    event.sprintf("%{trace.id}"),
                    event.sprintf("%{span.id}"),
                    tf,
                    ts
                );
        } catch (IOException e) {
            printer.println("IO Exception");
        }
        return io.opentelemetry.context.Context.root().with(Span.wrap(sp));
    }

    private Attributes getAttributesForEvent(Event event) {
        Map<String, Object> eventData = event.getData();
        AttributesBuilder a = Attributes.builder();
        for (String key : eventData.keySet()) {
            if (key.equals("@timestamp") || key.equals("message")) continue;

            a.put(key, (eventData.get(key)).toString());
        }
        return a.build();
    }

    private void emitLog(Event event) {
        String message = null;
        String logLevel = null;
        Attributes attributes = null;
        io.opentelemetry.context.Context c = null;
        try {
            c = getContextForEvent(event);
            message = event.sprintf("%{message}");
            logLevel = event.sprintf("%{log.level}");
            attributes = getAttributesForEvent(event);
        } catch (IOException e) {
            printer.println("IO Exception");
        }

        sdkLogEmitterProvider.get("logstash-output-opentelemetry").logBuilder()
                .setEpoch(event.getEventTimestamp())
                .setSeverityText(logLevel)
                .setBody(message)
                .setAttributes(attributes)
                .setContext(c)
                .emit();
    }

    private String protocolForConfig(Configuration configuration) {
        String endpointType = configuration.get(ENDPOINT_TYPE_CONFIG);
        String protocol = configuration.get(PROTOCOL_CONFIG);

        if( !protocol.isEmpty() ) {
            for (VALID_PROTOCOL_OPTIONS option : VALID_PROTOCOL_OPTIONS.values()) {
                if(option.name().equals(protocol)) return protocol;
            }
            throw new IllegalArgumentException(String.format("protocol (%s) is not valid.", protocol));
        }

        if( endpointType.isEmpty() ) {
            return VALID_PROTOCOL_OPTIONS.grpc.name();
        }

        for (VALID_PROTOCOL_OPTIONS option : VALID_PROTOCOL_OPTIONS.values()){
            if(option.name().equals(endpointType)) return endpointType;
        }

        throw new IllegalArgumentException(String.format("endpoint_type (%s) is not valid", endpointType));
    }

    private LogExporter logExporterForConfig(Configuration configuration) {
        URI endpoint = configuration.get(ENDPOINT_CONFIG);
        String compression = configuration.get(COMPRESSION_CONFIG);

        if(protocolForConfig(configuration).equals(VALID_PROTOCOL_OPTIONS.http.name())) {
            return OtlpHttpLogExporter.builder()
                    .setEndpoint(endpoint.toString())
                    .setCompression(compression)
                    .build();
        }
        return OtlpGrpcLogExporter.builder()
                .setEndpoint(endpoint.toString())
                .setCompression(compression)
                .build();
    }

    Opentelemetry(final String id, final Configuration config, final Context context, OutputStream targetStream) {
        // constructors should validate configuration options
        this.id = id;

        printer = new PrintStream(targetStream);
        Attributes resourceAttributes;
        resourceAttributes = Attributes.builder()
                .put("telemetry.sdk.name","logstash-output-opentelemetry")
                .put("telemetry.sdk.language", "java")
                .put("telemetry.sdk.version","0.0.1")
                .put("agent.id", id)
                .build();
        Resource r = Resource.create(resourceAttributes);
        LogExporter exporter = logExporterForConfig(config);

        LogProcessor batchProcessor = BatchLogProcessor.builder(exporter).build();
        sdkLogEmitterProvider = SdkLogEmitterProvider.builder()
                .setResource(r)
                .addLogProcessor(batchProcessor)
                .build();
    }

    @Override
    public void output(final Collection<Event> events) {
        Iterator<Event> z = events.iterator();
        while (z.hasNext() && !stopped) {
            emitLog(z.next());
        }
    }

    @Override
    public void stop() {
        stopped = true;
        sdkLogEmitterProvider.forceFlush();
        sdkLogEmitterProvider.shutdown();
        done.countDown();
    }

    @Override
    public void awaitStop() throws InterruptedException {
        done.await();
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return PluginHelper.commonOutputSettings(Arrays.asList(ENDPOINT_TYPE_CONFIG,ENDPOINT_CONFIG,PROTOCOL_CONFIG,COMPRESSION_CONFIG));
    }

    @Override
    public String getId() {
        return id;
    }
}
