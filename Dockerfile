FROM logstash:7.10.1
COPY *.gem .
RUN logstash-plugin install --no-verify --local *gem
