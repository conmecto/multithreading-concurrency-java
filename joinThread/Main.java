package joinThread;

import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        List<Long> numList = Arrays.asList(1000000000L, 234L, 3243L, 5432L, 6732L, 34L, 20L);
        List<FactorialThread> threadList = new ArrayList<>();
        
        for (Long num: numList) {
            threadList.add(new FactorialThread(num));
        }

        for (FactorialThread thread: threadList) {
            thread.setDaemon(true);
            thread.start();
        }

        for (FactorialThread thread: threadList) {
            thread.join(2000);
        }

        for (int i = 0; i < numList.size(); i++) {
            FactorialThread thread = threadList.get(i);
            if (thread.isFinished()) {
                System.out.println("Factorial for " + numList.get(i) + " is " + thread.getResult());
            } else {
                System.out.println("Still calculating for " + numList.get(i));
            }
        }
    }    

    private static class FactorialThread extends Thread {
        private long num;
        private BigInteger result = BigInteger.ONE;
        private boolean isFinished = false;
        
        public FactorialThread(long num) {
            this.num = num;
        }

        @Override
        public void run() {
            result = factorial(num);
            isFinished = true;
        }

        public boolean isFinished() {
            return isFinished;
        }

        public BigInteger getResult() {
            return result;
        }

        private BigInteger factorial(long num) {
            BigInteger tempResult = BigInteger.ONE; 
            for (long i = num; i > 0; i--) {
                tempResult = tempResult.multiply(new BigInteger(Long.toString(i))); 
            }
            return tempResult;
        }
    }
}
