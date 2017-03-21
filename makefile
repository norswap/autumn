#
# Require to run this makefile:
#
# - kotlinc 1.1.1
# - GNU Make
# - curl
# - mkdir, touch, rm, find, grep, xargs
#
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

space :=
space +=
comma := ,

OUT_PROD := out/production/$(PROJECT)
OUT_TEST := out/test/$(PROJECT)

kt_files = $(shell find $1 -name '*.kt')

# ------------------------------------------------------------------------------
# DEPS

maven	= http://central.maven.org/maven2/$1/$2/$3/$2-$(3)$4.jar
jcenter = https://jcenter.bintray.com/$1/$2/$3/$2-$(3)$4.jar

define dep_rule
  lib/$3-$4.jar lib/$3-$4%.jar:
	mkdir -p lib && curl -L $$(call $5,$2,$3,$4,$$*) > $$@
  $(1)_DEPS += lib/$3-$4.jar
  $(1)_DEPS_EXT += lib/$3-$4-sources.jar lib/$3-$4-javadoc.jar
  $(3) := lib/$3-$4.jar
  $3: lib/$3-$4.jar
  $3-ext: lib/$3-$4.jar lib/$3-$4-sources.jar lib/$3-$4-javadoc.jar
  .PHONY: $3
endef

define dependency
  $(eval $(call dep_rule,$(strip $1),$(strip $2),$(strip $3),$(strip $4),$(strip $5)))
endef

$(call dependency, PROD, org/apache/bcel, bcel, 6.0, maven)
$(call dependency, PROD, org/testng, testng, 6.11, jcenter)
$(call dependency, TEST, com/beust, jcommander, 1.64, maven)

DEPS     := $(PROD_DEPS) $(TEST_DEPS)
DEPS_EXT := $(DEPS) $(PROD_DEPS_EXT) $(TEST_DEPS_EXT)
CP_PROD  := "$(subst $(space),$(SEP),$(OUT_PROD) $(PROD_DEPS))"
CP_TEST  := "$(subst $(space),$(SEP),$(CP_PROD) $(OUT_TEST) $(TEST_DEPS))"

deps: $(DEPS)
deps-ext: $(DEPS_EXT)

# ------------------------------------------------------------------------------
# CLEAN

clean:
	rm -rf out

DEPS_REGEX = ".*/\($(subst $(space),\|,$(notdir $(DEPS_EXT)))\)"
# DEPS_REGEX = ".*/($(subst $(space),|,$(notdir $(DEPS_EXT))))"

clean-deps:
# delete all old dependencies
#	find lib ! -regex $(DEPS_REGEX) -type f -exec rm -f {} +
#	find -E lib ! -regex $(DEPS_REGEX) -type f -exec rm -f {} +
	(find lib -type f | grep -v $(DEPS_REGEX) | xargs -I x rm x) || true

nuke: clean
	rm lib/*

# ------------------------------------------------------------------------------
# BUILD & TEST

$(OUT_PROD)/timestamp: $(call kt_files,src)
	mkdir -p $(OUT_PROD)
	kotlinc -cp $(CP_PROD) -d $(OUT_PROD) src
	touch $@

build: $(OUT_PROD)/timestamp

$(OUT_TEST)/timestamp: $(testng) $(jcommander) $(call kt_files,test)
	mkdir -p $(OUT_TEST)
	kotlinc -cp $(CP_TEST) -d $(OUT_TEST) test
	touch $@

test: $(OUT_PROD)/timestamp $(OUT_TEST)/timestamp
	kotlin -cp $(CP_TEST) org.testng.TestNG test/testng.xml -d out/test-output || true
	echo "More details in out/test-output/index.html"

# ------------------------------------------------------------------------------

.PHONY: \
  default \
  deps \
  clean \
  clean-deps \
  build \
  test

#.SILENT:

# ------------------------------------------------------------------------------
