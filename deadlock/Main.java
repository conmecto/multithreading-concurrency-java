package deadlock;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Intersection intersection = new Intersection();
        Thread thread1 = new Thread(new TrainA(intersection));
        Thread thread2 = new Thread(new TrainB(intersection));

        thread1.start();
        thread2.start();
    }   

    public static class TrainB implements Runnable {
        private Intersection intersection;
        private Random random = new Random();

        public TrainB(Intersection intersection) {
            this.intersection = intersection;
        }

        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(random.nextInt(5));
                } catch(InterruptedException e) {
                    
                }
                intersection.takeRoadB();
            }
        }
    }

    public static class TrainA implements Runnable {
        private Intersection intersection;
        private Random random = new Random();

        public TrainA(Intersection intersection) {
            this.intersection = intersection;
        }

        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(random.nextInt(10));
                } catch(InterruptedException e) {
                    
                }
                intersection.takeRoadA();
            }
        }
    }
    
    public static class Intersection {
        private Object roadA = new Object();
        private Object roadB = new Object();

        public void takeRoadA() {
            synchronized(roadA) {
                System.out.println("Road A is taken by thread " + Thread.currentThread().getName());
                synchronized(roadB) {
                    System.out.println("Passing through road A");
                    try {
                        Thread.sleep(1);
                    } catch(InterruptedException e) {
                        
                    }
                }
            }
        }

        public void takeRoadB() {
            synchronized(roadA) {
                System.out.println("Road A is taken by thread " + Thread.currentThread().getName());
                synchronized(roadB) {
                    System.out.println("Passing through road B");
                    try {
                        Thread.sleep(1);
                    } catch(InterruptedException e) {

                    }
                }
            }
        }
    } 
}
