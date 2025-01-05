package semaphoreTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;


public class Main {
    public static void main(String[] args) {
        int numOfWorkers = 200;
        List<Task> threads = new ArrayList<>();
        Barrier barrier = new Barrier(numOfWorkers);
        for (int i = 0; i < numOfWorkers; i++) {
            threads.add(new Task(barrier));
        }
        for (Thread thread: threads) {
            thread.start();
        }
    }   

    private static class Barrier {
        private int numOfWorkers;
        private static Semaphore semaphore = new Semaphore(0);
        private static int count = 0;
        private static Lock lock = new ReentrantLock();

        public Barrier(int numOfWorkers) {
            this.numOfWorkers = numOfWorkers;
        }

        public void waitForOthers() throws InterruptedException {
            lock.lock();
            boolean isLastWorker = false;
            try {
                count += 1;
                if (count == numOfWorkers) {
                    isLastWorker = true;
                }
            } finally {
                lock.unlock();
            }

            if (isLastWorker) {
                semaphore.release(numOfWorkers - 1);
            } else {
                semaphore.acquire();
            }
        }
    }

    private static class Task extends Thread {
        private Barrier barrier;

        public Task(Barrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName() + " finished part 1.");
                barrier.waitForOthers();
                System.out.println(Thread.currentThread().getName() + " finished part 2.");
            } catch(InterruptedException e) {

            }
        }
    }
}
