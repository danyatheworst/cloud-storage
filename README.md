# clone repo
git clone https://github.com/danyatheworst/cloud-storage.git

cd ./cloud-storage

# build and run
docker-compose -f compose-local.yml build

docker-compose -f compose-local.yml up
