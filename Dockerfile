FROM archlinux
RUN useradd -m user -u 1000
RUN <<EOT
    pacman -Sy --noconfirm 
    pacman -S --noconfirm jre11-openjdk-headless
    rm -rf /var/cache/pacman /var/lib/pacman
EOT
COPY tmp/server /opt/stuff-server
USER user
CMD ["/opt/stuff-server/bin/server", "-Dconfig.file=/opt/stuff-server/conf/container.conf"]
# /opt/stuff-server/bin/server -Dconfig.file=/opt/stuff-server/conf/container.conf
