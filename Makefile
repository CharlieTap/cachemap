JAVA := /opt/homebrew/opt/openjdk/bin/java
JARFILE := ./cachemap/build/libs/cachemap-all-0.1.jar

# Define the trace output file
TRACE_OUTPUT_CACHEMAP := ./cachemap.trace
TRACE_OUTPUT_CONTROL := ./control.trace

XCTRACE_TEMPLATE := L1CACHESTATS

benchmark_cachemap:
	$(JAVA) -jar $(JARFILE) "cachemap"
benchmark_controlmap:
	$(JAVA) -jar $(JARFILE) "controlmap"
templates:
	xcrun xctrace list templates
# Command to run xctrace with the "CPU Counters" template on Java program launch
run_trace_cachemap:
	-rm -r $(TRACE_OUTPUT_CACHEMAP)
	@echo "Starting xctrace on Java program..."
	xcrun xctrace record --output $(TRACE_OUTPUT_CACHEMAP) --template "$(XCTRACE_TEMPLATE)" --launch -- $(JAVA) -XX:-RestrictContended -jar $(JARFILE) "cachemap"
	@echo "Changing permissions on the trace output..."
	@chmod -R 777 $(TRACE_OUTPUT_CACHEMAP)
run_trace_controlmap:
	-rm -r $(TRACE_OUTPUT_CONTROL) 2>/dev/null
	@echo "Starting xctrace on Java program..."
	xcrun xctrace record --output $(TRACE_OUTPUT_CONTROL) --template "$(XCTRACE_TEMPLATE)" --launch -- $(JAVA) -XX:-RestrictContended -jar $(JARFILE) "controlmap"
	@echo "Changing permissions on the trace output..."
	@chmod -R 777 $(TRACE_OUTPUT_CONTROL)

# Default target
all: run_trace
