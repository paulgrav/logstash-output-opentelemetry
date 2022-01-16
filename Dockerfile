FROM logstash:7.16.3
COPY *.gem .
RUN logstash-plugin install --no-verify --local *gem
