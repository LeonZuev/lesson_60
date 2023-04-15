package dithering;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class Main {
    public static void main(String[] args) throws IOException {
        System.setProperty("sun.java2d.uiScale", "1");

        BufferedImage picture = ImageIO.read(new File("res/1.jpeg"));

        picture = ShowWindow.toBufferedImage(picture.getScaledInstance(1080, 840, Image.SCALE_SMOOTH));

        int[] pixels = ((DataBufferInt)picture.getRaster().getDataBuffer()).getData();

        Arrays.setAll(pixels, i -> {
            int color = pixels[i];
            int bwColor = (int) (0.0722 * (color & 0xff) + 0.7152 * (color >> 8 & 0xff) + 0.2126 * (color >> 16 & 0xff));
            return 0xff000000 | bwColor & 0xff | ((bwColor & 0xff) << 8) | ((bwColor & 0xff) << 16);
        });

        picture = dithering(picture);

    /*/*
        int[] noise = new int[pixels.length];
        Arrays.setAll(noise, i -> 128 - (int)(Math.random() * 255));
        Arrays.setAll(pixels, i -> {
            int color = pixels[i];
            int bwColor = color & 0xff;
            int sum = bwColor + noise[i];
            if (sum > 255) {
                sum = 255;
            }
            if (sum < 0) {
                sum = 0;
            }
            return 0xff000000 | sum & 0xff | ((sum & 0xff) << 8) | ((sum & 0xff) << 16);
        });
        Arrays.setAll(pixels, i -> {
            int color = pixels[i];
            int bwColor = color & 0xff;
            return  bwColor > 127 ? 0xffffffff : 0xff000000;
        });
*/
        ShowWindow.showImageWindow(picture);
    }

    /*private static BufferedImage dithering(BufferedImage img) {
        BufferedImage bwOnlyImage = new BufferedImage(img.getWidth(), img.getHeight(), TYPE_INT_RGB);
        int[] destPixels = ((DataBufferInt) bwOnlyImage.getRaster().getDataBuffer()).getData();
        int[] sourcePixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int size = img.getHeight() * img.getWidth();

        int error = 0;

        for (int i = 0; i < size; i++) {
            int pixel = 0xff & sourcePixels[i];
            int value = pixel > error ? 255 : 0;
            error += value - pixel;
            destPixels[i] = 0xff000000 | value << 16 | value << 8 | value;
        }
        return bwOnlyImage;
    }*/
private static BufferedImage dithering(BufferedImage img) {
    BufferedImage bwOnlyImage = new BufferedImage(img.getWidth(), img.getHeight(), TYPE_INT_RGB);
    int[] destPixels = ((DataBufferInt) bwOnlyImage.getRaster().getDataBuffer()).getData();
    int[] sourcePixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();

    int width = img.getWidth();
    int height = img.getHeight();

    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            int index = y * width + x;
            int pixel = 0xff & sourcePixels[index];
            int value = pixel > 127 ? 255 : 0;
            destPixels[index] = 0xff000000 | value << 16 | value << 8 | value;

            int error = pixel - value;
            if (x + 1 < width) {
                sourcePixels[index + 1] += (error ) / 8;
            }
            if (x + 2 < width) {
                sourcePixels[index + 2] += (error ) / 8;
            }
            if (y + 1 < height) {
                if (x > 0) {
                    sourcePixels[index + width - 1] += (error ) / 8;
                }
                sourcePixels[index + width] += (error ) / 8;
                if (x + 1 < width) {
                    sourcePixels[index + width + 1] += (error ) / 8;
                }
            }
            if (y + 2 < height) {
                sourcePixels[index + width * 2] += (error ) / 8;
            }
            // New error diffusion to pixels above and below
            if (y > 0) {
                sourcePixels[index - width] += (error ) / 8;
            }
            if (y > 1) {
                sourcePixels[index - width * 2] += (error ) / 8;
            }
        }
    }

    return bwOnlyImage;
}
}