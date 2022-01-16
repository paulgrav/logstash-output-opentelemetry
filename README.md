# Logstash Java Plugin

[![Java CI with Gradle](https://github.com/paulgrav/logstash-output-opentelemetry/actions/workflows/gradle.yml/badge.svg)](https://github.com/paulgrav/logstash-output-opentelemetry/actions/workflows/gradle.yml)

This is a Java plugin for [Logstash](https://github.com/elastic/logstash).

It is fully free and fully open source. The license is Apache 2.0, meaning you are free to use it however you want.

The documentation for Logstash Java plugins is available [here](https://www.elastic.co/guide/en/logstash/6.7/contributing-java-plugin.html).

## OpenTelemetry

This plugin allows Logstash to output looks to an OpenTelemetry GRPC otlp endpoint. The default endpoint is http://localhost:4317

Fields below are mapped as per the spec: https://opentelemetry.io/docs/reference/specification/logs/data-model/#elastic-common-schema

```
@timestamp >> Timestamp
log.level >> SeverityText
message >> Body
```

All other fields are attached as Attributes.

## Installation

`logstash-plugin install logstash-output-opentelemetry`

## Usage

```
input {
    generator {
        count => 10
        add_field => {
            "log.level" => "WARN"
        }
    }
}
output {
    opentelemetry {
        id => "otelgrpc"
        endpoint => {
            grpc => "http://otel:4317"
        }
    }
    opentelemetry {
        id => "otelhttp"
        endpoint => {
            http => "http://otel:4318/v1/logs"
        }
    }
}

```

## Options

Use `endpoint.grpc` or `endpoint.http` to specify an otlp endpoint. The default is:

```
endpoint => { grpc => "http://localhost:4317" }
```



## Building

`make gem`

## Running locally

`docker-compose up`

## Notes

**Warning** This plugin depends on OpenTelemetry logging libraries are that are alpha quality.
