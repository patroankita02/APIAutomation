export LOCAL_IP=$(ifconfig -l | xargs -n1 ipconfig getifaddr)
echo $LOCAL_IP
docker compose -f docker-compose.yml up -d
docker run -d -p 127.0.0.1:7878:7878/tcp --name digital_clinic_catalog_service -i -t 405836331329.dkr.ecr.ap-southeast-1.amazonaws.com/digital-clinic-catalog:stage-1.0-SNAPSHOT-1712832159385 sh;
export dockerId=$(docker ps -aqf "name=digital_clinic_catalog_service");
echo $dockerId;
sed -i -e "s/LOCAL_IP/$LOCAL_IP/g" docker.sh
docker cp docker.sh $dockerId:/etc/service/digital-clinic-catalog/bin/dockerrun.sh;
docker exec -it $dockerId sh /etc/service/digital-clinic-catalog/bin/dockerrun.sh;
sed -i -e "s/$LOCAL_IP/LOCAL_IP/g" docker.sh