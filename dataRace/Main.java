package dataRace;

public class Main {
    public static void main(String[] args) {
        SharedClass shared = new SharedClass();
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i <= 100000000; i++) {
                shared.increment();
            }
        });
        Thread thread2 = new Thread(() -> {
            for (int i = 0; i <= 100000000; i++) {
                shared.checkDataRace();
            }
        });
        thread1.start();
        thread2.start();
    }   
    
    private static class SharedClass {
        private volatile int x = 0;
        private volatile int y = 0;

        public void increment() {
            x++;
            y++;
        }

        public void checkDataRace() {
            if (y > x) {
                System.out.println("y > x - Data race is detected");
            }
        }
    }
}
