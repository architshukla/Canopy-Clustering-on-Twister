# Folders
SRC=src
BIN=bin
DIST=dist
JARFILENAME=canopyclustering_twister.jar
TWISTERJAR=$(TWISTER_HOME)/lib/Twister-0.9.jar

# Folders in SRC
DATAOPERATIONS=DataOperations
CLUSTERING=Clustering
TYPES=types

# Build all
all:	build_dataoperations build_clustering
	if test -d $(BIN); then rm -rf $(BIN); fi
	if test -d $(DIST); then rm -rf $(DIST); fi
	mkdir $(BIN)
	mkdir $(DIST)

	mkdir -p $(BIN)/$(DATAOPERATIONS)
	mv $(SRC)/$(DATAOPERATIONS)/*.class $(BIN)/$(DATAOPERATIONS)/

	mkdir -p $(BIN)/$(CLUSTERING)
	mv $(SRC)/$(CLUSTERING)/*.class $(BIN)/$(CLUSTERING)/

	mkdir -p $(BIN)/$(TYPES)
	mv $(SRC)/$(TYPES)/*.class $(BIN)/$(TYPES)/

	jar -cvf $(JARFILENAME) -C $(BIN) .
	mv $(JARFILENAME) $(DIST)

build_dataoperations:	$(SRC)/$(DATAOPERATIONS)/SplitData.java
	javac $(SRC)/$(DATAOPERATIONS)/SplitData.java

build_clustering:	$(SRC)/$(CLUSTERING)/ClusteringDriver.java \
					$(SRC)/$(CLUSTERIClusteringMapperTask.java \
					$(SRC)/$(CLUSTERIClusteringReducerTask.java \
					$(SRC)/$(CLUSTERING)/ClusteringCombiner.java \
					$(SRC)/$(TYPES)/DataPoint.java \
					$(SRC)/$(TYPES)/DataPointVector.java
	javac -cp $(TWISTERJAR) $(SRC)/$(CLUSTERING)/ClusteringDriver.java \
		$(SRC)/$(CLUSTERING)/ClusteringMapper.java \
		$(SRC)/$(CLUSTERING)/ClusteringReducer.java \
		$(SRC)/$(CLUSTERING)/ClusteringCombiner.java \
		$(SRC)/$(TYPES)/DataPoint.java \
		$(SRC)/$(TYPES)/DataPointVector.java

# Clean
clean:	clean_allcassfiles
	if test -d $(BIN); then rm -rf $(BIN); fi
	if test -d $(DIST); then rm -rf $(DIST); fi

clean_allcassfiles:	clean_dataoperations

clean_dataoperations:
	if test -f $(SRC)/$(DATAOPERATIONS)/*.class; then rm $(SRC)/$(DATAOPERATIONS)/*.class; fi