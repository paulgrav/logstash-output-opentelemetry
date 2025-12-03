package net.stomer;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;

final class LogstashLogRecordData implements LogRecordData {

    private final Resource resource;
    private final InstrumentationScopeInfo instrumentationScopeInfo;
    private final long timestampEpochNanos;
    private final long observedTimestampEpochNanos;
    private final SpanContext spanContext;
    private final Severity severity;
    private final String severityText;
    private final Body body;
    private final Attributes attributes;
    private final int totalAttributeCount;
    private final String eventName;

    private LogstashLogRecordData(Builder builder) {
        this.resource = builder.resource;
        this.instrumentationScopeInfo = builder.instrumentationScopeInfo;
        this.timestampEpochNanos = builder.timestampEpochNanos;
        this.observedTimestampEpochNanos = builder.observedTimestampEpochNanos;
        this.spanContext = builder.spanContext;
        this.severity = builder.severity;
        this.severityText = builder.severityText;
        this.body = builder.body;
        this.attributes = builder.attributes;
        this.totalAttributeCount = builder.totalAttributeCount;
        this.eventName = builder.eventName;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public InstrumentationScopeInfo getInstrumentationScopeInfo() {
        return instrumentationScopeInfo;
    }

    @Override
    public long getTimestampEpochNanos() {
        return timestampEpochNanos;
    }

    @Override
    public long getObservedTimestampEpochNanos() {
        return observedTimestampEpochNanos;
    }

    @Override
    public SpanContext getSpanContext() {
        return spanContext;
    }

    @Override
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public String getSeverityText() {
        return severityText;
    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public int getTotalAttributeCount() {
        return totalAttributeCount;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    static Builder builder() {
        return new Builder();
    }

    static final class Builder {
        private Resource resource = Resource.empty();
        private InstrumentationScopeInfo instrumentationScopeInfo = InstrumentationScopeInfo.empty();
        private long timestampEpochNanos;
        private long observedTimestampEpochNanos;
        private SpanContext spanContext = SpanContext.getInvalid();
        private Severity severity = Severity.UNDEFINED_SEVERITY_NUMBER;
        private String severityText = "";
        private Body body = Body.empty();
        private Attributes attributes = Attributes.empty();
        private int totalAttributeCount;
        private String eventName = "";

        Builder setResource(Resource resource) {
            this.resource = resource;
            return this;
        }

        Builder setInstrumentationScopeInfo(InstrumentationScopeInfo instrumentationScopeInfo) {
            this.instrumentationScopeInfo = instrumentationScopeInfo;
            return this;
        }

        Builder setTimestampEpochNanos(long timestampEpochNanos) {
            this.timestampEpochNanos = timestampEpochNanos;
            return this;
        }

        Builder setObservedTimestampEpochNanos(long observedTimestampEpochNanos) {
            this.observedTimestampEpochNanos = observedTimestampEpochNanos;
            return this;
        }

        Builder setSpanContext(SpanContext spanContext) {
            this.spanContext = spanContext;
            return this;
        }

        Builder setSeverity(Severity severity) {
            this.severity = severity;
            return this;
        }

        Builder setSeverityText(String severityText) {
            this.severityText = severityText;
            return this;
        }

        Builder setBody(String body) {
            this.body = Body.string(body == null ? "" : body);
            return this;
        }

        Builder setAttributes(Attributes attributes) {
            this.attributes = attributes;
            return this;
        }

        Builder setTotalAttributeCount(int totalAttributeCount) {
            this.totalAttributeCount = totalAttributeCount;
            return this;
        }

        Builder setEventName(String eventName) {
            this.eventName = eventName;
            return this;
        }

        LogRecordData build() {
            if (observedTimestampEpochNanos == 0) {
                observedTimestampEpochNanos = timestampEpochNanos;
            }
            return new LogstashLogRecordData(this);
        }
    }
}

