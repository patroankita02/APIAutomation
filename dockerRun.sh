#!/bin/bash +x
if [ $# -ne 6 ]; then
  echo "Please provide zookeeper container name, kafka container name, mockContainerName, mock container ECR image name, mock service name and mockServicePort"
  exit 2
fi
nameOfZookeeperContainer=$1 && \
nameOfKafkaContainer=$2 && \
mockContainerName=$3 && \
mockContainerECRImage=$4 && \
mockServiceName=$5 && \
mockServicePort=$6 && \
underLayingEC2HostName=${NEW_RELIC_METADATA_KUBERNETES_NODE_NAME} && \

# Start Zookeeper
echo "Starting Zookeeper ${nameOfZookeeperContainer}" && \
nerdctl run -d \
  -p 2181:2181 \
  --name ${nameOfZookeeperContainer} zookeeper 1>imagepull.log 2>&1 || ( cat imagepull.log && exit 2 ) && \

# Start Kafka
echo "Starting kafka ${nameOfKafkaContainer}" && \
nerdctl run -d \
  -p 29092:29092 \
  -e "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT" \
  -e "KAFKA_ADVERTISED_LISTENERS=INSIDE://localhost:9092,OUTSIDE://${underLayingEC2HostName}:29092" \
  -e "KAFKA_LISTENERS=INSIDE://:9092,OUTSIDE://:29092" \
  -e "KAFKA_INTER_BROKER_LISTENER_NAME=INSIDE" \
  -e "KAFKA_ZOOKEEPER_CONNECT=${underLayingEC2HostName}:2181" \
  -e "KAFKA_CREATE_TOPICS=topic_names" \
--name ${nameOfKafkaContainer} wurstmeister/kafka 1>imagepull.log 2>&1 || ( cat imagepull.log && exit 2 ) && \

# Start Service Mock
if [ -z $mockServicePort ]; then
 echo "Please provide the mockServicePort in the dockerRun.sh script" && \
 exit 3
fi

echo "Starting mock service ${mockContainerName} with image ${mockContainerECRImage} on port ${mockServicePort}" && \
nerdctl run -d -t -p ${mockServicePort}:${mockServicePort} --name ${mockContainerName} ${mockContainerECRImage} sh 1>imagepull.log 2>&1 || ( cat imagepull.log && exit 2 ) && \

# Copy and execute the service script
sed -i "s/will_be_replaced_with_actual_dynamic_ip/${underLayingEC2HostName}/g" startService.sh && \
sed -i "s/will_be_replaced_with_actual_service_name/${mockServiceName}/g" startService.sh && \

echo "Stopping mock service ${mockContainerName} container temporarily" && \
nerdctl stop ${mockContainerName} 1>/dev/null && \
echo "Copying startService.sh inside mock service ${mockContainerName} container" && \
nerdctl cp startService.sh ${mockContainerName}:/etc/service/${mockServiceName}/bin/startService.sh && \
echo "Starting mock service ${mockContainerName} container" && \
nerdctl start ${mockContainerName} 1>/dev/null && \
echo "Starting mock service ${mockServiceName}" && \
nerdctl exec -d ${mockContainerName} sh /etc/service/${mockServiceName}/bin/startService.sh && \
grep -r 'localhost:\|:9092' src/ | awk -F ':' '{print $1}' | sort | uniq | xargs -I %s sed -i "s/localhost:/${underLayingEC2HostName}:/g;s/:9092/:29092/g" %s
