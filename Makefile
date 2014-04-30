# Variables
PKGPREFIX=$(shell grep 'PKGPREFIX' ccimr.properties | awk '{ print $$3 }')
SRCPREFIX=$(shell grep 'SRCPREFIX' ccimr.properties | awk '{ print $$3 }')/$(PKGPREFIX)

# Folders
BIN=bin
DIST=dist
JARFILENAME=$(shell grep 'JARFILE' ccimr.properties | awk '{ print $$3 }')
TWISTERJAR=$(TWISTER_HOME)/lib/Twister-0.9.jar

# Folders in SRCPREFIX
DATAOPERATIONS=$(SRCPREFIX)/$(shell grep 'DATAOPSDIR' ccimr.properties | awk '{ print $$3 }')
CLUSTERING=$(SRCPREFIX)/$(shell grep 'CLUSTERINGDIR' ccimr.properties | awk '{ print $$3 }')
TYPES=$(SRCPREFIX)/$(shell grep 'TYPESDIR' ccimr.properties | awk '{ print $$3 }')

# Build all
all:	build_dataoperations build_clustering
	if test -d $(BIN); then rm -rf $(BIN); fi
	if test -d $(DIST); then rm -rf $(DIST); fi
	mkdir $(BIN)
	mkdir $(DIST)

	mkdir -p $(BIN)/$(DATAOPERATIONS)
	mv $(DATAOPERATIONS)/*.class $(BIN)/$(DATAOPERATIONS)/

	mkdir -p $(BIN)/$(CLUSTERING)
	mv $(CLUSTERING)/*.class $(BIN)/$(CLUSTERING)/

	mkdir -p $(BIN)/$(TYPES)
	mv $(TYPES)/*.class $(BIN)/$(TYPES)/

	jar -cvf $(JARFILENAME) -C $(BIN) .
	mv $(JARFILENAME) $(DIST)

build_dataoperations:	$(DATAOPERATIONS)/SplitData.java
	javac $(DATAOPERATIONS)/SplitData.java

build_clustering:	$(CLUSTERING)/ClusteringDriver.java \
					$(CLUSTERING)/ClusteringMapper.java \
					$(CLUSTERING)/ClusteringReducer.java \
					$(CLUSTERING)/ClusteringCombiner.java \
					$(TYPES)/DataPoint.java \
					$(TYPES)/DataPointVector.java
	javac -cp $(TWISTERJAR) $(CLUSTERING)/ClusteringDriver.java \
		$(CLUSTERING)/ClusteringMapper.java \
		$(CLUSTERING)/ClusteringReducer.java \
		$(CLUSTERING)/ClusteringCombiner.java \
		$(TYPES)/DataPoint.java \
		$(TYPES)/DataPointVector.java

# Clean
clean:	clean_allcassfiles
	if test -d $(BIN); then rm -rf $(BIN); fi
	if test -d $(DIST); then rm -rf $(DIST); fi

clean_allcassfiles:	clean_dataoperations

clean_dataoperations:
	if test -f $(DATAOPERATIONS)/*.class; then rm $(DATAOPERATIONS)/*.class; fi