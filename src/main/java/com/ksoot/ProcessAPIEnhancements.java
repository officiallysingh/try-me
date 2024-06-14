package com.ksoot;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class ProcessAPIEnhancements {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        infoOfCurrentProcess();
        infoOfLiveProcesses();
        infoOfSpawnProcess();
        infoOfExitCallback();
        infoOfChildProcess();
    }

    private static void infoOfCurrentProcess() {
        ProcessHandle processHandle = ProcessHandle.current();
        ProcessHandle.Info processInfo = processHandle.info();

        System.out.println("PID: " + processHandle.pid());
        System.out.println("Arguments: " + processInfo.arguments());
        System.out.println("Command: " + processInfo.command());
        System.out.println("Instant: " + processInfo.startInstant());
        System.out.println("Total CPU duration: " + processInfo.totalCpuDuration());
        System.out.println("User: " + processInfo.user());
    }

    private static void infoOfSpawnProcess() throws IOException {

        String javaCmd = ProcessUtils.getJavaCmd().getAbsolutePath();
        ProcessBuilder processBuilder = new ProcessBuilder(javaCmd, "-version");
        Process process = processBuilder.inheritIO().start();
        ProcessHandle processHandle = process.toHandle();
        ProcessHandle.Info processInfo = processHandle.info();

        System.out.println("PID: " + processHandle.pid());
        System.out.println("Arguments: " + processInfo.arguments());
        System.out.println("Command: " + processInfo.command());
        System.out.println("Instant: " + processInfo.startInstant());
        System.out.println("Total CPU duration: " + processInfo.totalCpuDuration());
        System.out.println("User: " + processInfo.user());
    }

    private static void infoOfLiveProcesses() {
        Stream<ProcessHandle> liveProcesses = ProcessHandle.allProcesses();
        liveProcesses.filter(ProcessHandle::isAlive)
            .forEach(ph -> {
                System.out.println("PID: " + ph.pid());
                System.out.println("Instance: " + ph.info().startInstant());
                System.out.println("User: " + ph.info().user());
            });
    }

    private static void infoOfChildProcess() throws IOException {
        int childProcessCount = 5;
        for (int i = 0; i < childProcessCount; i++) {
            String javaCmd = ProcessUtils.getJavaCmd()
              .getAbsolutePath();
            ProcessBuilder processBuilder
              = new ProcessBuilder(javaCmd, "-version");
            processBuilder.inheritIO().start();
        }

        Stream<ProcessHandle> children = ProcessHandle.current()
          .children();
        children.filter(ProcessHandle::isAlive)
          .forEach(ph -> System.out.println("PID: "+ph.pid()+", Cmd: "+ph.info()
                  .command()+""));
        Stream<ProcessHandle> descendants = ProcessHandle.current()
          .descendants();
        descendants.filter(ProcessHandle::isAlive)
          .forEach(ph -> System.out.println("PID: "+ph.pid()+", Cmd: "+ph.info()
                  .command()+""));
    }

    private static void infoOfExitCallback() throws IOException, InterruptedException, ExecutionException {
        String javaCmd = ProcessUtils.getJavaCmd()
          .getAbsolutePath();
        ProcessBuilder processBuilder
          = new ProcessBuilder(javaCmd, "-version");
        Process process = processBuilder.inheritIO()
          .start();
        ProcessHandle processHandle = process.toHandle();

        System.out.println("PID: "+processHandle.pid()+" has started");
        CompletableFuture<ProcessHandle> onProcessExit = processHandle.onExit();
        onProcessExit.get();
        System.out.println("Alive: " + processHandle.isAlive());
        onProcessExit.thenAccept(ph -> {
            System.out.println("PID: "+ph.pid()+" has stopped");
        });
    }

}
