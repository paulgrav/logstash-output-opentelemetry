# Logstash Java Plugin

[![Java CI with Gradle](https://github.com/paulgrav/logstash-output-opentelemetry/actions/workflows/gradle.yml/badge.svg)](https://github.com/paulgrav/logstash-output-opentelemetry/actions/workflows/gradle.yml)

This is a Java plugin for [Logstash](https://github.com/elastic/logstash).

It is fully free and fully open source. The license is Apache 2.0, meaning you are free to use it however you want.

The documentation for Logstash Java plugins is available [here](https://www.elastic.co/guide/en/logstash/6.7/contributing-java-plugin.html).

## OpenTelemetry

This plugin allows Logstash to output looks to an OpenTelemetry otlp endpoint.

Fields below are mapped as per the spec: https://opentelemetry.io/docs/reference/specification/logs/data-model/#elastic-common-schema

```
@timestamp >> Timestamp
log.level >> SeverityText
message >> Body
```

All other fields are attached as Attributes.

## Installation

For some reason the following does NOT work.

`logstash-plugin install logstash-output-opentelemetry`

Instead, manually install the Gem:

```
curl -LO https://rubygems.org/downloads/logstash-output-opentelemetry-0.2.5.gem
logstash-plugin install --no-verify --local *gem
```

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
        endpoint => "http://otel:4317"
        protocol => "grpc"
        compression => "none"
    }
}
```

## Options

| Setting | Input Type | Required |
|:--|:--|:--|
| endpoint | [uri](https://www.elastic.co/guide/en/logstash/current/configuration-file-structure.html#uri) | Yes |
| endpoint_type | [string](https://www.elastic.co/guide/en/logstash/7.16/configuration-file-structure.html#string) | No (Deprecated) |
| protocol | [string](https://www.elastic.co/guide/en/logstash/7.16/configuration-file-structure.html#string), one of ["grpc", "http"] | No |
| compression | [string](https://www.elastic.co/guide/en/logstash/7.16/configuration-file-structure.html#string), one of ["gzip", "none"] | No |

`endpoint`

- This is a required setting.
- There is no default value for this setting.
- Value type is [uri](https://www.elastic.co/guide/en/logstash/current/configuration-file-structure.html#uri)

`endpoint_type`

- Deprecated. Replaced with `protocol`.

`protocol`

- Value type is [string](https://www.elastic.co/guide/en/logstash/7.16/configuration-file-structure.html#string)
- Default is: `grpc`

`compression`

- Value type is [string](https://www.elastic.co/guide/en/logstash/7.16/configuration-file-structure.html#string)
- Default is: `none`

## Building

`make gem`

## Running locally

`docker-compose up`

## Notes

**Warning** This plugin depends on OpenTelemetry logging libraries are that are alpha quality.
