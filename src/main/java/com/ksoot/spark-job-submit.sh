#!/bin/bash
# Define the Spark submit command
SPARK_SUBMIT_CMD="./bin/spark-submit \
    --master k8s://https://127.0.0.1:50456 \
    --deploy-mode cluster \
    --name spark-spring \
    --class com.ksoot.spark.SparkSpringCloudTask \
    --conf spark.kubernetes.container.image=spark-spring-cloud-task:0.0.1 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
    --conf spark.kubernetes.driverEnv.SPARK_USER=spark \
    --conf spark.executor.instances=2 \
    local:///opt/spark/job-apps/spark-spring-cloud-task.jar"

# Execute the Spark submit command
echo "Executing Spark submit command..."
eval $SPARK_SUBMIT_CMD

# Check the exit status of the Spark submit command
if [ $? -eq 0 ]; then
  echo "Spark submit command executed successfully."
else
  echo "Spark submit command failed."
  exit 1
fi