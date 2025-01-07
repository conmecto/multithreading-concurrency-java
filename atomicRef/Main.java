package atomicRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        AtomicStack<Integer> stack = new AtomicStack<Integer>();
        Random random = new Random();

        for (int i = 0; i < 100000; i++) {
            stack.push(random.nextInt());
        }

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Thread thread = new Thread(() -> {
                while(true) {
                    stack.push(random.nextInt());
                }
            });
            thread.setDaemon(true);
            threads.add(thread);
        }
        for (int i = 0; i < 2; i++) {
            Thread thread = new Thread(() -> {
                while(true) {
                    stack.pop();
                }
            });
            thread.setDaemon(true);
            threads.add(thread);
        }
        for(Thread thread: threads) {
            thread.start();
        }
        Thread.sleep(10000);
        System.out.println("Operations performed in 10sec " + stack.getCounter());
    }    

    private static class AtomicStack<T> {
        private AtomicReference<StackNode<T>> head = new AtomicReference<>();
        private AtomicInteger counter = new AtomicInteger();

        public void push(T value) {
            StackNode<T> newNode = new StackNode<T>(value);
            while(true) {
                System.out.println("Still working push");
                StackNode<T> currentHead = head.get();
                newNode.next = currentHead;
                if (head.compareAndSet(currentHead, newNode)) {
                    break;
                } else {
                    LockSupport.parkNanos(1);
                }
            }
            counter.incrementAndGet();
        }

        public T pop() {
            StackNode<T> currentHead = head.get();
            StackNode<T> newHead;
            while(currentHead != null) {
                System.out.println("Still working pop");
                newHead = currentHead.next;
                if (head.compareAndSet(currentHead, newHead)) {
                    break;
                } else {
                    LockSupport.parkNanos(1);
                    currentHead = head.get();
                }
            }
            counter.incrementAndGet();
            return currentHead != null ? currentHead.value : null;
        }

        public int getCounter() {
            return counter.get();
        }
    } 

    private static class StackNode<T>{
        public T value;
        public StackNode<T> next;

        public StackNode(T value) {
            this.value = value;
            this.next = null;
        }        
    }
}