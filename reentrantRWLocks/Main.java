package reentrantRWLocks;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.Random;

public class Main {
    public static final int HIGHEST_PRICE = 1000;
    public static void main(String[] args) throws InterruptedException {
        InventoryDatabase inventoryDb = new InventoryDatabase();
        Random random = new Random();
        for (int i = 0; i < 100000; i++) {
            int price = random.nextInt(HIGHEST_PRICE);
            inventoryDb.addItem(price);
        }

        Thread writer = new Thread(() -> {
            while(true) {
                inventoryDb.addItem(random.nextInt(HIGHEST_PRICE));
                inventoryDb.removeItem(random.nextInt(HIGHEST_PRICE));

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    
                }
            }
        });
        writer.setDaemon(true);
        writer.start();

        List<Thread> threads = new ArrayList<Thread>();
        int numOfReaderThreads = 7;
        for (int i = 0; i < numOfReaderThreads; i++) {
            Thread reader = new Thread(() -> {
                for (int j = 0; j < 100000; j++) {
                    int upperBound = random.nextInt(HIGHEST_PRICE);
                    int lowerBound = upperBound > 0 ? random.nextInt(upperBound) : 0;
                    inventoryDb.getItemsWithinRange(lowerBound, upperBound);
                }
            });
            reader.setDaemon(true);
            threads.add(reader);
        }

        long start = System.currentTimeMillis();
        for (Thread thread: threads) {
            thread.start();
        }
        for (Thread thread: threads) {
            thread.join();
        }
        long end = System.currentTimeMillis();
        System.out.println("Total time " + (end - start));
    } 

    private static class InventoryDatabase {
        private static TreeMap<Integer, Integer> priceToCountMap = new TreeMap<>();
        // private ReentrantLock lock = new ReentrantLock();
        private ReentrantReadWriteLock readerWriterLock = new ReentrantReadWriteLock();
        private Lock readerLock = readerWriterLock.readLock();
        private Lock writerLock = readerWriterLock.writeLock();

        public int getItemsWithinRange(int lowerBound, int upperBound) {
            readerLock.lock();
            try {
                Integer fromKey = priceToCountMap.ceilingKey(lowerBound);
                Integer toKey = priceToCountMap.floorKey(upperBound);

                if (fromKey == null || toKey == null) {
                    return 0;
                }
                NavigableMap<Integer, Integer> range = priceToCountMap.subMap(fromKey, true, toKey, true);
                int sum = 0;
                for (int count: range.values()) {
                    sum += count;
                }
                return sum;
            } finally {
                readerLock.unlock();
            }
        }

        public void addItem(int price) {
            writerLock.lock();
            try {
                Integer numberOfItemsForPrice = priceToCountMap.get(price);
                if (numberOfItemsForPrice == null) {
                    priceToCountMap.put(price, 1);
                } else {
                    priceToCountMap.put(price, numberOfItemsForPrice + 1);
                }
            } finally {
                writerLock.unlock();
            }
        }

        public void removeItem(int price) {
            writerLock.lock();
            try {
                Integer numberOfItemsForPrice = priceToCountMap.get(price);
                if (numberOfItemsForPrice == null || numberOfItemsForPrice == 1) {
                    priceToCountMap.remove(price);
                } else {
                    priceToCountMap.put(price, numberOfItemsForPrice - 1);
                }
            } finally {
                writerLock.unlock();
            }
        }
    }

}
