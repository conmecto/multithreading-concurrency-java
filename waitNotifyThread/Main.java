package waitNotifyThread;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.StringJoiner;

public class Main {
    private static final int N = 10;
    private static final String FILE_PATH = "./waitNotifyThread/matrices.txt";
    private static final String OUTPUT_FILE = "./waitNotifyThread/matrices_results.txt";
    public static void main(String[] args) throws IOException {
        ThreadSafeQueue queue = new ThreadSafeQueue();
        File inputFile = new File(FILE_PATH);
        File outputFile = new File(OUTPUT_FILE);
        MatricesReaderProducer matrixProducer = new MatricesReaderProducer(new FileReader(inputFile), queue);
        MatricesMultiplierConsumer matrixConsumer = new MatricesMultiplierConsumer(new FileWriter(outputFile), queue);

        matrixProducer.start();
        matrixConsumer.start();
    }

    private static class MatricesMultiplierConsumer extends Thread {
        private FileWriter fileWriter;
        private ThreadSafeQueue queue;

        public MatricesMultiplierConsumer(FileWriter fileWriter, ThreadSafeQueue queue) {
            this.fileWriter = fileWriter;
            this.queue = queue;
        }

        @Override
        public void run() {
            while(true) {
                MatricesPair matricesPair = queue.remove();
                if (matricesPair == null) {
                    System.out.println("No more matrices, consumer is terminating");
                    break;
                }
                float[][] result = multiplyMatrices(matricesPair.matrix1, matricesPair.matrix2);
                try {
                    saveMatrixToFile(result);
                } catch (IOException e) {
                }
            }

            try {
                fileWriter.flush();
                fileWriter.close();
            } catch(IOException e) {}
        }

        private void saveMatrixToFile(float[][] matrix) throws IOException {
            for (int i = 0; i < N; i++) {
                StringJoiner str = new StringJoiner(", ");
                for (int j = 0; j < N; j++) {
                    str.add(String.format("%.2f", matrix[i][j]));
                }
                fileWriter.write(str.toString());
                fileWriter.write('\n');
            }
        }

        private float[][] multiplyMatrices(float[][] m1, float[][] m2) {
            float[][] r = new float[N][N];
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    for (int k = 0; k < N; k++) {
                        r[i][j] += (m1[i][k] * m2[k][j]);
                    }
                }
            }
            return r;
        }
    }

    private static class MatricesReaderProducer extends Thread {
        private Scanner scanner;
        private ThreadSafeQueue queue;

        public MatricesReaderProducer(FileReader fileReader, ThreadSafeQueue queue) {
            this.scanner = new Scanner(fileReader);
            this.queue = queue;
        }

        @Override
        public void run() {
            while(true) {
                float[][] matrix1 = readMatrix();
                float[][] matrix2 = readMatrix();
                if (matrix1 == null || matrix2 == null) {
                    queue.terminate();
                    System.out.println("No more matrix");
                    return;
                }
                MatricesPair matricesPair = new MatricesPair();
                matricesPair.matrix1 = matrix1;
                matricesPair.matrix2 = matrix2;

                queue.add(matricesPair);
            }
        }

        public float[][] readMatrix() {
            float[][] matrix = new float[N][N];
            for (int i = 0; i < N; i++) {
                if (!scanner.hasNext()) {
                    return null;
                }
                String[] line = scanner.nextLine().split(",");
                for (int j = 0; j < N; j++) {
                    matrix[i][j] = Float.valueOf(line[j]);
                }
            }
            scanner.nextLine();
            return matrix;
        }
    }

    private static class ThreadSafeQueue {
        private Queue<MatricesPair> queue = new LinkedList<>();
        private boolean isEmpty = true;
        private boolean isTerminated = false;
        private final int MAX_CAP = 5;

        public synchronized void add(MatricesPair matricesPair) {
            while (queue.size() == MAX_CAP) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            queue.add(matricesPair);
            if (isEmpty) {
                isEmpty = false;
            }
            notify();
        }

        public synchronized MatricesPair remove() {
            MatricesPair matricesPair = null;
            while(isEmpty && !isTerminated) {
                try {
                    wait();    
                } catch(InterruptedException e) {

                }
            }
            if (queue.size() == 1) {
                isEmpty = true;
            }
            if (queue.size() == 0 && isTerminated) {
                return null;
            }
            System.out.println("Queue size " + queue.size());
            matricesPair = queue.remove();
            if (queue.size() == MAX_CAP-1) {
                notifyAll();
            }
            return matricesPair;
        }

        public synchronized void terminate() {
            isTerminated = true;
            notifyAll();
        }
    }

    private static class MatricesPair {
        public float[][] matrix1;
        public float[][] matrix2;
    }
}