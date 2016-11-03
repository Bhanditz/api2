FROM postgres:9.4
ADD init-user-db.sh /docker-entrypoint-initdb.d
RUN chmod +x /docker-entrypoint-initdb.d/*.sh
# Add VOLUMEs to allow backup of config, logs and databases
VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]