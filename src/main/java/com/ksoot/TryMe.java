package com.ksoot;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class TryMe {

  public static void main(String[] args) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    System.out.println("============================================================");
    System.out.println("Executing spark-submit command... SPARK_HOME: " + System.getenv("SPARK_HOME"));

    final ProcessBuilder processBuilder = new ProcessBuilder();
    File directory = new File("/Users/shubham.rao/spark-3.4.1-bin-hadoop3-scala2.13");
    processBuilder.directory(directory);
    processBuilder.command(
        "bash",
        "-c",
        """
        ./bin/spark-submit \\
            --master k8s://https://127.0.0.1:50456 \\
            --deploy-mode cluster \\
            --name spark-spring \\
            --class com.ksoot.spark.SparkSpringCloudTask \\
            --conf spark.kubernetes.container.image=spark-spring-cloud-task:0.0.1 \\
            --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \\
            --conf spark.kubernetes.driverEnv.SPARK_USER=spark \\
            --conf spark.executor.instances=2 \\
            local:///opt/spark/job-apps/spark-spring-cloud-task.jar
        """);

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
