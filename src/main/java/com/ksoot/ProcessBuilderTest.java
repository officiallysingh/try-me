package com.ksoot;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ProcessBuilderTest {

  public static void main(String[] args) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    System.out.println("============================================================");
    System.out.println("Executing spark-submit command... SPARK_HOME: " + System.getenv("SPARK_HOME"));

//    final ProcessBuilder processBuilder = new ProcessBuilder("/Users/shubham.rao/Home/workspaces/work_racloop/try-me/src/main/java/com/ksoot/spark-job-submit.sh");
    final ProcessBuilder processBuilder = new ProcessBuilder("./bin/spark-job-submit.sh");
    // Get the environment variables map
    Map<String, String> environment = processBuilder.environment();

    // Set new environment variables or modify existing ones
    environment.put("MASTER_URL", "https://127.0.0.1:50456");
    environment.put("DEPLOY_MODE", "cluster");
    environment.put("JOB_CLASS_NAME", "com.ksoot.spark.SparkSpringCloudTask");
    environment.put("JOB_NAME", "spark-spring-cloud-task");
    environment.put("EXECUTOR_INSTANCES", "2");
    environment.put("SERVICE_ACCOUNT_NAME", "spark");
    environment.put("SPARK_USER", "spark");
//    System.out.println("Env variable > MASTER_URL: " + System.getenv("MASTER_URL"));
//    System.out.println("Env variable > DEPLOY_MODE: " + System.getenv("DEPLOY_MODE"));
//    System.out.println("Env variable > CLASS_NAME: " + System.getenv("CLASS_NAME"));
//    System.out.println("Env variable > NAME: " + System.getenv("NAME"));
    System.out.println("ProcessBuilder environment: " + environment);

    File directory = new File("/Users/shubham.rao/spark-3.4.1-bin-hadoop3-scala2.13");
    processBuilder.directory(directory);
//    processBuilder.command(
//        "bash",
//        "-c",
//        """
//        ./bin/spark-submit \\
//            --master $MASTER_URL \\
//            --deploy-mode cluster \\
//            --name spark-spring \\
//            --class com.ksoot.spark.SparkSpringCloudTask \\
//            --conf spark.kubernetes.container.image=spark-spring-cloud-task:0.0.1 \\
//            --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \\
//            --conf spark.kubernetes.driverEnv.SPARK_USER=spark \\
//            --conf spark.executor.instances=2 \\
//            local:///opt/spark/job-apps/spark-spring-cloud-task.jar
//        """);

    try {
      Process process = processBuilder.inheritIO().start();
      StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
      StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);

      executor.submit(outputGobbler);
      executor.submit(errorGobbler);

      // Wait for the process to complete
      int exitCode = process.waitFor();

      // Get the inputs.waitFor();
      System.out.println("Exited spark-submit command with code: " + exitCode);
      System.out.println("============================================================");
      executor.shutdown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static class StreamGobbler implements Runnable {
    private final InputStream inputStream;
    private final Consumer<String> consumer;

    StreamGobbler(final InputStream inputStream, final Consumer<String> consumer) {
      this.inputStream = inputStream;
      this.consumer = consumer;
    }

    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
    }
  }
}
