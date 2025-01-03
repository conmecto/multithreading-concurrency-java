package lockCounter;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        InventoryCounter counter = new InventoryCounter();
        DecrementingThread thread1 = new DecrementingThread(counter);
        IncrementingThread thread2 = new IncrementingThread(counter);
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        System.out.println("Inventory items " + counter.getItems());
    }

    private static class DecrementingThread extends Thread {
        private InventoryCounter counter;

        public DecrementingThread(InventoryCounter counter) {
            this.counter = counter;
        }

        @Override 
        public void run() {
            for (int i = 0; i < 1000; i++) {
                this.counter.decrement();
            }
        }
    }

    private static class IncrementingThread extends Thread {
        private InventoryCounter counter;

        public IncrementingThread(InventoryCounter counter) {
            this.counter = counter;
        }

        @Override 
        public void run() {
            for (int i = 0; i < 1000; i++) {
                this.counter.increment();
            }
        }
    }

    private static class InventoryCounter {
        private static int items = 0;

        Object lock = new Object();

        public void decrement() {
            synchronized(this.lock) {
                items--;
            }
        }

        public void increment() {
            synchronized(this.lock) {
                items++;
            }
        }

        public int getItems() {
            synchronized(this.lock) {
                return items;
            }
        }
    }
}
