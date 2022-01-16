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
    generator => {
        count => 10
    }
}
output {
    opentelemetry => {
        endpoint => "http://localhost:4317"
    }
}
```

## Options

Use `endpoint` to specify an otlp endpoint. The default is `http://localhost:4317`.
Use `endpoint_type` to specify the type of endpoint, either `grpc` or `http`. The default is `grpc`.



## Building

`make gem`

## Running locally

`docker-compose up`

## Notes

**Warning** This plugin depends on OpenTelemetry logging libraries are that are alpha quality.
