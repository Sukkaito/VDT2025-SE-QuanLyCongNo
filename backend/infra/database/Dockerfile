FROM mariadb:latest AS mariadb-database

# Mock database files
COPY /mysql /var/lib/mysql

# Uncomment and set your database connection details if needed
# ENV MARIADB_DATABASE=mydatabase
# ENV MARIADB_PASSWORD=password
# ENV MARIADB_ROOT_PASSWORD=verysecret
# ENV MARIADB_USER=myuser

VOLUME /var/lib/mysql
EXPOSE 3306
