package imageProcessingWithThreads;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class Main {
    private static final String SOURCE_FILE_PATH = "./imageProcessingWithThreads/white-flowers.jpg";
    private static final String OUTPUT_FILE_PATH = "./imageProcessingWithThreads/colored-flowers.jpg";

    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();
        BufferedImage originalImage = ImageIO.read(new File(SOURCE_FILE_PATH));
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int numOfThreads = 4;
        reColorImageMultipleThreads(originalImage, resultImage, numOfThreads, width, height);
        File outputFile = new File(OUTPUT_FILE_PATH);
        ImageIO.write(resultImage, "jpg", outputFile);
        long endTime = System.nanoTime();
        long timeTaken = endTime - startTime;
        System.out.println("Time taken: " + timeTaken/1000000 + " ms");
    }

    private static void reColorImageMultipleThreads(BufferedImage originalImage, BufferedImage resultImage, int numOfThreads, int width, int height) {
        List<Thread> threads = new ArrayList<>();
        int blockHeight = height / numOfThreads;
        for (int i = 0; i < numOfThreads; i++) {
            final int threadMultiplier = i;
            Thread thread = new Thread(() -> {
                int leftCorner = 0;
                int topCorner = threadMultiplier * blockHeight;
                reColorImage(originalImage, resultImage, leftCorner, topCorner, width, blockHeight);
            });
            threads.add(thread);
        }
        for (Thread thread: threads) {
            thread.start();
        }
        for (Thread thread: threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {

            }
        }
    }  

    // private static void reColorImageSingleThread(BufferedImage originalImage, BufferedImage resultImage, int width, int height) {
    //     reColorImage(originalImage, resultImage, 0, 0, width, height);
    // }   

    private static void reColorImage(BufferedImage originalImage, BufferedImage resultImage, int leftCorner, int topCorner, int width, int height) {
        for(int x = leftCorner; x < leftCorner + width && x < originalImage.getWidth(); x++) {
            for(int y = topCorner; y < topCorner + height && y < originalImage.getHeight(); y++) {
                pixelColorChange(originalImage, resultImage, x, y);
            }
        }
    }   

    private static void pixelColorChange(BufferedImage originalImage, BufferedImage resultImage, int x, int y) {
        int rgb = originalImage.getRGB(x, y);

        int red = extractRed(rgb);
        int green = extractGreen(rgb);
        int blue = extractBlue(rgb);

        int newRed;
        int newGreen;
        int newBlue;

        if (isGreyScale(red, green, blue)) {
            newRed = Math.min(255, red + 10);
            newGreen = Math.max(0, green - 80);
            newBlue = Math.max(0, blue - 20);
        } else {
            newRed = red;
            newGreen = green;
            newBlue = blue;
        }
        int newRgb = createRGBFromColors(newRed, newGreen, newBlue);
        setRGB(resultImage, x, y, newRgb);
    }

    private static void setRGB(BufferedImage image, int x, int y, int rgb) {
        image.getRaster().setDataElements(x, y, image.getColorModel().getDataElements(rgb, null));
    }

    private static int createRGBFromColors(int red, int green, int blue) {
        int rgb = 0;
        rgb |= blue;
        rgb |= (green << 8);
        rgb |= (red << 16);
        rgb |= 0xFF000000; // For transparency bits of rgb 
        return rgb;
    }

    private static boolean isGreyScale(int red, int green, int blue) {
        return Math.abs(red - green) < 30 && Math.abs(green - blue) < 30 && Math.abs(blue - red) < 30;
    }

    private static int extractBlue(int rgb) {
        return (rgb & 0x000000FF);
    }

    private static int extractGreen(int rgb) {
        return (rgb & 0x0000FF00) >> 8;
    }

    private static int extractRed(int rgb) {
        return (rgb & 0x00FF0000) >> 16;
    }
}