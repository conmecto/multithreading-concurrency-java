package threadPerTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final int TOTAL_TASKS = 10000;
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        performTasks();
        long end = System.currentTimeMillis();
        System.out.println("Time taken for operations " + (end - start));
    }

    private static void performTasks() {
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        try {
            for (int i = 0; i < TOTAL_TASKS; i++) {
                executorService.submit(() -> {
                    blockingTask();
                });
            }
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void blockingTask() {
        System.out.println("Executing a blocking task from thread " + Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }
}
