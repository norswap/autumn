# ------------------------------------------------------------------------------

default: build
PROJECT := whimsy

# ------------------------------------------------------------------------------
# PATHS

ifeq ($(OS) , Windows_NT)
SEP := ;
else
SEP := :
endif

OUT_PROD := out/production/$(PROJECT)
OUT_TEST := out/test/$(PROJECT)
CP_PROD  := "$(OUT_PROD)"
CP_TEST  := "$(OUT_PROD)$(SEP)$(OUT_TEST)$(SEP)lib/testng.jar$(SEP)lib/jcommander.jar"

kt_files = $(shell find $1 -name '*.kt')

# ------------------------------------------------------------------------------
# DEPS

maven		= http://central.maven.org/maven2/$1/$2/$3/$2-$3.jar
jcenter		= https://jcenter.bintray.com/$1/$2/$3/$2-$3.jar
fetch_dep	= mkdir -p lib && curl -L $(call $1,$2,$3,$4) > $@

lib/testng.jar:
	$(call fetch_dep,jcenter,org/testng,testng,6.9.13.6)

lib/jcommander.jar:
	$(call fetch_dep,maven,com/beust,jcommander,1.48)

offline: lib/testng.jar lib/jcommander.jar

# ------------------------------------------------------------------------------
# CLEAN

clean:
	rm -rf out

clean-deps:
#   leave the jars added by IntelliJ
	find lib ! -name 'kotlin-*.jar' -type f -exec rm -f {} +

# ------------------------------------------------------------------------------
# BUILD & TEST

$(OUT_PROD)/timestamp: $(call java_files, src)
	mkdir -p $(OUT_PROD)
	kotlinc -cp $(CP_PROD) -d $(OUT_PROD) src
	touch $@

build: $(OUT_PROD)/timestamp

$(OUT_TEST)/timestamp: lib/testng.jar lib/jcommander.jar $(call kt_files, test)
	mkdir -p $(OUT_TEST)
	kotlinc -cp $(CP_TEST) -d $(OUT_TEST) test
	touch $@

test: $(OUT_TEST)/timestamp
	kotlin -cp $(CP_TEST) org.testng.TestNG test/testng.xml -d out/test-output || true

# ------------------------------------------------------------------------------

.PHONY: \
  default \
  offline \
  clean \
  clean-deps \
  build \
  test

#.SILENT:

# ------------------------------------------------------------------------------
