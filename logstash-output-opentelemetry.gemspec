# AUTOGENERATED BY THE GRADLE SCRIPT. EDITS WILL BE OVERWRITTEN.
Gem::Specification.new do |s|
  s.name            = 'logstash-output-opentelemetry'
  s.version         = ::File.read('VERSION').split('\n').first
  s.licenses        = ['Apache-2.0']
  s.summary         = 'OpenTelemetry Output Plugin'
  s.description     = 'A Logstash output plugin that allows logs to be output to OpenTelemetry otlp endpoints.'
  s.authors         = ['Paul Grave']
  s.email           = ['paul@stomer.net']
  s.homepage        = 'https://github.com/paulgrav/logstash-output-opentelemetry'
  s.platform        = 'java'
  s.require_paths = ['lib', 'vendor/jar-dependencies']

  s.files = Dir["lib/**/*","*.gemspec","*.md","CONTRIBUTORS","Gemfile","LICENSE","NOTICE.TXT", "vendor/jar-dependencies/**/*.jar", "vendor/jar-dependencies/**/*.rb", "VERSION", "docs/**/*"]

  # Special flag to let us know this is actually a logstash plugin
  s.metadata = { 'logstash_plugin' => 'true', 'logstash_group' => 'output'}

  # Gem dependencies
  s.add_runtime_dependency "logstash-core-plugin-api", ">= 1.60", "<= 2.99"
  s.add_runtime_dependency 'jar-dependencies'
  s.add_development_dependency 'logstash-devutils'
end