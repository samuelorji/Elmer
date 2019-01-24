#!/bin/bash
echo ":::::: Dumping Elmer Data into mysql"
export MYSQLCONTAINER=$(docker ps  | grep mysql | cut -d" " -f 1)
cat /opt/domains/elmer/data/mysql/schema.sql | docker exec -i $MYSQLCONTAINER mysql -u root --password=admin
cat /opt/domains/elmer/data/mysql/data.sql | docker exec -i $MYSQLCONTAINER mysql -u root --password=admin elmer
cat /opt/domains/elmer/data/mysql/patch.sql | docker exec -i $MYSQLCONTAINER mysql -u root --password=admin elmer

echo ":::::: Done ::::::" 
