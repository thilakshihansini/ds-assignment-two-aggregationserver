# Compiler and flags
JAVAC = javac
JAVA = java
JFLAGS = -d target -cp target

# Project paths
SRC_DIR = src/main/java/org/example
BIN_DIR = target

# Classes
MAIN_CLASS = org.example.AggregationServer
CONTENT_CLASS = org.example.ContentServer
GET_CLIENT_CLASS = org.example.GETClient

# Targets
all: prepare build

# Prepare the target directory
prepare:
	@mkdir -p $(BIN_DIR)

# Compile the Java files
build: prepare
	$(JAVAC) $(JFLAGS) $(SRC_DIR)/*.java

# Run the Aggregation Server
run-aggregation:
	$(JAVA) -cp $(BIN_DIR) $(MAIN_CLASS)

# Run the Content Server
run-content:
	$(JAVA) -cp $(BIN_DIR) $(CONTENT_CLASS)

# Run the GETClient
run-getclient:
	$(JAVA) -cp $(BIN_DIR) $(GET_CLIENT_CLASS)

# Clean the compiled files
clean:
	@rm -rf $(BIN_DIR)/*.class
