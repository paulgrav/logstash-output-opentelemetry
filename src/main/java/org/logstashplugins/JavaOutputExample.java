package org.logstashplugins;

import co.elastic.logstash.api.*;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporter;
import io.opentelemetry.sdk.logs.LogEmitter;
import io.opentelemetry.sdk.logs.LogProcessor;
import io.opentelemetry.sdk.logs.SdkLogEmitterProvider;
import io.opentelemetry.sdk.logs.export.BatchLogProcessor;
import io.opentelemetry.sdk.resources.Resource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

// class name must match plugin name
@LogstashPlugin(name = "java_output_example")
public class JavaOutputExample implements Output {

    public static final PluginConfigSpec<String> ENDPOINT_CONFIG =
            PluginConfigSpec.stringSetting("endpoint", "http://localhost:4317");

    private final String id;
    private String endpoint;
    private PrintStream printer;
    private final CountDownLatch done = new CountDownLatch(1);
    private volatile boolean stopped = false;
    private LogEmitter logEmitter;

    // all plugins must provide a constructor that accepts id, Configuration, and Context
    public JavaOutputExample(final String id, final Configuration configuration, final Context context) {
        this(id, configuration, context, System.out);
    }

    private Context getContextForEvent(Event event) {
        TraceState ts = TraceState.getDefault();
        TraceFlags tf = TraceFlags.getDefault();
        SpanContext sp = null;
        try {
            sp = SpanContext.create(event.sprintf("%{trace.id}"),event.sprintf("%{span.id}"),tf,ts);
        } catch (IOException e) {
            printer.println("IO Exception");
        }
        return Context.root().with(Span.wrap(sp));
    }

    private Attributes getAttributesForEvent(Event event) {
        Map<String, Object> eventData = event.getData();
        AttributesBuilder a = Attributes.builder();
        Iterator i = eventData.keySet().iterator();
        while (i.hasNext()) {
            String key = (i.next()).toString();
            if( key.equals("@timestamp") || key.equals("message") ) continue;

            a.put(key, (eventData.get(key)).toString());
        }
        return a.build();
    }

    private void emitLog(Event event) {
        String message = null;
        String loglevel = null;
        Attributes attributes = null;
        Context c = null;
        try {
            c = getContextForEvent(event);
            message = event.sprintf("%{message}");
            loglevel = event.sprintf("%{log.level}");
            attributes = getAttributesForEvent(event);
        } catch (IOException e) {
            printer.println("IO Exception");
        }

        logEmitter.logBuilder()
                .setEpoch(event.getEventTimestamp())
                .setSeverityText(loglevel)
                .setBody(message)
                .setAttributes(attributes)
                .setContext(c)
                .emit();
    }

    JavaOutputExample(final String id, final Configuration config, final Context context, OutputStream targetStream) {
        // constructors should validate configuration options
        this.id = id;
        endpoint = config.get(ENDPOINT_CONFIG);
        printer = new PrintStream(targetStream);
        Attributes resourceAttributes = Attributes.builder()
                .put("telemetry.sdk.name","logstash-output-opentelemetry")
                .put("telemetry.sdk.language", "java")
                .put("telemetry.sdk.version","0.0.1")
                .build();
        Resource r = Resource.create(resourceAttributes);
        OtlpGrpcLogExporter exporter = OtlpGrpcLogExporter.builder()
                .setEndpoint(endpoint)
                .build();
//      LogProcessor processor = SimpleLogProcessor.create(exporter);
        LogProcessor batchProcessor = BatchLogProcessor.builder(exporter).build();
        SdkLogEmitterProvider sdkLogEmitterProvider = SdkLogEmitterProvider.builder()
                .setResource(r)

                .addLogProcessor(batchProcessor)
                .build();
        logEmitter = sdkLogEmitterProvider.get("logstash-output-opentelemetry");
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
        done.countDown();
    }

    @Override
    public void awaitStop() throws InterruptedException {
        done.await();
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        // should return a list of all configuration options for this plugin
        return Collections.singletonList(ENDPOINT_CONFIG);
    }

    @Override
    public String getId() {
        return id;
    }
}
