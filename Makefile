LOGSTASH_VERSION := 7.10.2
VERSION := 0.0.1-alpha
ASSETSDIR := assets
LOGSTASH_PATH := $(ASSETSDIR)/logstash-$(LOGSTASH_VERSION)
LOGSTASH_CORE_PATH := $(LOGSTASH_PATH)/logstash-core
LOGSTASH_CORE_JAR := $(LOGSTASH_CORE_PATH)/build/libs/logstash-core-$(LOGSTASH_VERSION).jar
PLUGIN_JAR := build/libs/logstash-output-opentelemetry-$(VERSION).jar
GEM_FILE := logstash-output-opentelemetry-$(VERSION).gem

all: gem

gem: $(GEM_FILE)

$(GEM_FILE): VERSION gradle.properties logstashcorejar
	./gradlew gem

VERSION:
	@echo $(VERSION)
	@echo $(VERSION) > VERSION

$(PLUGIN_JAR): logstashcorejar gradle.properties
	./gradlew assemble

build: $(PLUGIN_JAR)

logstashcorejar: $(LOGSTASH_CORE_JAR)

$(LOGSTASH_CORE_JAR): $(LOGSTASH_PATH)
	cd $(LOGSTASH_PATH); ./gradlew assemble

$(LOGSTASH_PATH): $(ASSETSDIR)/v$(LOGSTASH_VERSION).zip
	unzip -q -d $(ASSETSDIR) $(ASSETSDIR)/v$(LOGSTASH_VERSION).zip
	touch $@

gradle.properties:
	@echo LOGSTASH_VERSION=$(LOGSTASH_VERSION) >> $@
	@echo LOGSTASH_CORE_PATH=$(PWD)/$(LOGSTASH_CORE_PATH) >> $@
	@echo LOGSTASH_JAR=$(LOGSTASH_PATH)/build/libs/logstash-$(LOGSTASH_VERSION).jar >> $@

$(ASSETSDIR)/v$(LOGSTASH_VERSION).zip:
	@mkdir -p $(@D)
	curl -s -L  https://github.com/elastic/logstash/archive/refs/tags/v$(LOGSTASH_VERSION).zip -o $@

clean:
	@rm $(LOGSTASH_CORE_JAR)
	@rm gradle.properties
	@rm $(GEM_FILE)
	@rm $(PLUGIN_JAR)
	@rm VERSION

clean-all: clean
	@rm -rf $(ASSETSDIR)
	@rm -rf build
