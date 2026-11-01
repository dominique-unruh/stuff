# eclipse-temurin:21-jre-noble (Ubuntu 24.04), pinned by digest. Update via:
#   docker pull eclipse-temurin:21-jre-noble && docker inspect --format '{{index .RepoDigests 0}}' eclipse-temurin:21-jre-noble
FROM eclipse-temurin:21-jre-noble@sha256:0eda5830ed7b669abc246a8dc520ea8a23cab06f9a8d4b6964109e2f2394e46f
COPY tmp/server /opt/stuff-server
# uid 1000 (user "ubuntu" in the base image), must match the owner of the DB volume
USER 1000:1000
CMD ["/opt/stuff-server/bin/server", "-Dconfig.file=/opt/stuff-server/conf/container.conf"]
# /opt/stuff-server/bin/server -Dconfig.file=/opt/stuff-server/conf/container.conf
