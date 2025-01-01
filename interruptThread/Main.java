package interruptThread;

import java.math.BigInteger;

public class Main {
    public static void main(String[] args) {
        Thread blockingTask = new BlockingTask();
        Thread longComputationTask = new LongComputationTask(new BigInteger("2000000"), new BigInteger("10000000"));

        blockingTask.start();
        blockingTask.interrupt();

        longComputationTask.setDaemon(true);
        longComputationTask.start();
        longComputationTask.interrupt();
    }

    private static class BlockingTask extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(50000);
            } catch (InterruptedException e) {
                System.out.println("Exiting");
            }
        }
    }

    private static class LongComputationTask extends Thread {
        private BigInteger num;
        private BigInteger power;

        public LongComputationTask(BigInteger num, BigInteger power) {
            this.num = num;
            this.power = power;
        }

        @Override
        public void run() {
            System.out.println(num  + "^" + power + "=" + pow(this.num, this.power));            
        }

        private BigInteger pow(BigInteger num, BigInteger power) {
            BigInteger result = BigInteger.ONE;
            for (BigInteger i = BigInteger.ZERO; i.compareTo(power) != 0; i = i.add(BigInteger.ONE)) {
                // if (this.isInterrupted()) {
                //     System.out.println("Interrupted");
                //     return BigInteger.ZERO;
                // }
                result = result.multiply(num);    
            }
            return result;
        }
    }
}
