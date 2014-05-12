# Variables
PKGPREFIX=$(shell grep 'PKGPREFIX' ccimr.properties | awk '{ print $$3 }')
SRCPREFIX=$(shell grep 'SRCPREFIX' ccimr.properties | awk '{ print $$3 }')/$(PKGPREFIX)

# Folders
BIN=bin
DIST=dist
JARFILENAME=$(shell grep 'JARFILE' ccimr.properties | awk '{ print $$3 }')
TWISTERJAR=$(TWISTER_HOME)/lib/Twister-0.9.jar

# Folders in SRCPREFIX
DATAOPERATIONS=$(shell grep 'DATAOPSDIR' ccimr.properties | awk '{ print $$3 }')
CLUSTERING=$(shell grep 'CLUSTERINGDIR' ccimr.properties | awk '{ print $$3 }')
TYPES=$(shell grep 'TYPESDIR' ccimr.properties | awk '{ print $$3 }')

# Build all
all:	build_dataoperations build_clustering
	if test -d $(BIN); then rm -rf $(BIN); fi
	if test -d $(DIST); then rm -rf $(DIST); fi
	mkdir $(BIN)
	mkdir $(DIST)

	mkdir -p $(BIN)/$(PKGPREFIX)/$(DATAOPERATIONS)
	mv $(SRCPREFIX)/$(DATAOPERATIONS)/*.class $(BIN)/$(PKGPREFIX)/$(DATAOPERATIONS)/

	mkdir -p $(BIN)/$(PKGPREFIX)/$(CLUSTERING)
	mv $(SRCPREFIX)/$(CLUSTERING)/*.class $(BIN)/$(PKGPREFIX)/$(CLUSTERING)/

	mkdir -p $(BIN)/$(PKGPREFIX)/$(TYPES)
	mv $(SRCPREFIX)/$(TYPES)/*.class $(BIN)/$(PKGPREFIX)/$(TYPES)/

	jar -cvf $(JARFILENAME) -C $(BIN) .
	mv $(JARFILENAME) $(DIST)

build_dataoperations:	$(SRCPREFIX)/$(DATAOPERATIONS)/SplitData.java
	javac $(SRCPREFIX)/$(DATAOPERATIONS)/SplitData.java

build_clustering:	$(SRCPREFIX)/$(CLUSTERING)/ClusteringDriver.java \
					$(SRCPREFIX)/$(CLUSTERING)/ClusteringMapper.java \
					$(SRCPREFIX)/$(CLUSTERING)/ClusteringReducer.java \
					$(SRCPREFIX)/$(CLUSTERING)/ClusteringCombiner.java \
					$(SRCPREFIX)/$(TYPES)/DataPoint.java \
					$(SRCPREFIX)/$(TYPES)/DataPointVector.java
	javac -cp $(TWISTERJAR) \
		$(SRCPREFIX)/$(CLUSTERING)/ClusteringDriver.java \
		$(SRCPREFIX)/$(CLUSTERING)/ClusteringMapper.java \
		$(SRCPREFIX)/$(CLUSTERING)/ClusteringReducer.java \
		$(SRCPREFIX)/$(CLUSTERING)/ClusteringCombiner.java \
		$(SRCPREFIX)/$(TYPES)/DataPoint.java \
		$(SRCPREFIX)/$(TYPES)/DataPointVector.java

# Clean
clean:	clean_allclassfiles
	if test -d $(BIN); then rm -rf $(BIN); fi
	if test -d $(DIST); then rm -rf $(DIST); fi

clean_allclassfiles:	clean_dataoperations

clean_dataoperations:
	if test -f $(SRCPREFIX)/$(DATAOPERATIONS)/*.class; then rm $(SRCPREFIX)/$(DATAOPERATIONS)/*.class; fi