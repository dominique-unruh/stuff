
DIST_NAME = server-0.1.0-SNAPSHOT
DOCKER_TAG = registry.git.rwth-aachen.de/unruh/qis-server/unruh-stuff

docker_build : Dockerfile
	rm -rf "server/target/universal/$(DIST_NAME).zip" tmp  # Removing the ZIP to ensure that we don't get a stale copy due to a wrong DIST_NAME
	sbt --java-home /usr/lib/jvm/java-11-openjdk dist
	mkdir -p tmp/unzip
	cd tmp/unzip && unzip "../../server/target/universal/$(DIST_NAME).zip"
	mv "tmp/unzip/$(DIST_NAME)" tmp/server
	docker build . -t "$(DOCKER_TAG)"

docker_try : docker_build
	mkdir -p tmp/stuff-db
	rm -rf tmp/stuff-db/3unruh@gmail.com
	cp -r ~/r/my-stuff tmp/stuff-db/3unruh@gmail.com
	docker run -it --env APPLICATION_SECRET=4C4SSKB0jaNLXOFgS0ZklP -p 9000:9000  --mount "type=bind,src=$$PWD/tmp/stuff-db,dst=/opt/stuff-db" "$(DOCKER_TAG)" # bash


docker_deploy : docker_build
	docker login registry.git.rwth-aachen.de
	docker push "$(DOCKER_TAG)"
