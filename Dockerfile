FROM docker.elastic.co/logstash/logstash:7.10.2
COPY *.gem .
RUN logstash-plugin install --no-verify --local *gem
