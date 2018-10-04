
.PHONY: publish clean

FAT_JAR = $(build/libs/Pipeline-all-*.jar)

ifeq ($(FAT_JAR),)
FAT_JAR := build/libs/Pipeline-all-1.0.jar
endif


Pipeline: 
	git clone git@github.com:Alagris/Pipeline.git

Pipeline/pipeline: Pipeline
	cd Pipeline ; \
	git checkout repository ; \
	git reset --hard origin/repository

build/libs/Pipeline-all-%.jar:
	./gradlew fatJar

publish: Pipeline/pipeline $(FAT_JAR)
	cd Pipeline ; \
	FAT_JAR_PATH=$$(echo ../build/libs/Pipeline-all-*.jar) ; \
	echo "FAT_JAR_PATH=$${FAT_JAR_PATH}" ; \
	VERSION=$$(basename $${FAT_JAR_PATH} .jar | awk -F- '{print $$NF}') ; \
	echo "VERSION=$${VERSION}" ; \
	mvn install:install-file -DgroupId=pipeline -DartifactId=pipeline -Dversion=$${VERSION} -Dfile=$${FAT_JAR_PATH} -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=.  -DcreateChecksum=true ; \
	git add . ; \
	git commit -m "Publishing $$VERSION" ; \
	git push origin repository

clean:
	./gradlew clean
	rm -rf Pipeline
	




