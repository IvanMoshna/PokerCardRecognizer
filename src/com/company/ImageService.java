package com.company;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

public class ImageService {

    public static BufferedImage getBinarizedImage(BufferedImage img) {

        int red;
        int newPixel;

        img = getGrayImage(img);

        int threshold = getTreshold(img);

        BufferedImage binarized = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        for(int i=0; i<img.getWidth(); i++) {
            for(int j=0; j<img.getHeight(); j++) {

                red = new Color(img.getRGB(i, j)).getRed();
                int alpha = new Color(img.getRGB(i, j)).getAlpha();
                if(red > threshold) {
                    newPixel = 255;
                }
                else {
                    newPixel = 0;
                }
                newPixel = colorToRGB(alpha, newPixel, newPixel, newPixel);
                binarized.setRGB(i, j, newPixel);

            }
        }
        return binarized;
    }

    private static BufferedImage getGrayImage(BufferedImage img) {
        int alpha, red, green, blue;
        int newPixel;

        BufferedImage lum = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        for(int i=0; i<img.getWidth(); i++) {
            for(int j=0; j<img.getHeight(); j++) {
                //Разбор пикселей на цвета и прозрачность
                alpha = new Color(img.getRGB(i, j)).getAlpha();
                red = new Color(img.getRGB(i, j)).getRed();
                green = new Color(img.getRGB(i, j)).getGreen();
                blue = new Color(img.getRGB(i, j)).getBlue();

                red = (int) (0.21 * red + 0.71 * green + 0.07 * blue);
                //Обратно к оригиналу
                newPixel = colorToRGB(alpha, red, red, red);
                lum.setRGB(i, j, newPixel);
            }
        }
        return lum;
    }

    public static int colorToRGB(int alpha, int red, int green, int blue) {

        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red; newPixel = newPixel << 8;
        newPixel += green; newPixel = newPixel << 8;
        newPixel += blue;

        return newPixel;

    }

    public static int getTreshold(BufferedImage img) {

        int[] histogram = getHistogram(img);
        int total = img.getHeight() * img.getWidth();

        float sum = 0;
        for(int i=0; i<256; i++) sum += i * histogram[i];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        int threshold = 0;

        for(int i=0 ; i<256 ; i++) {
            wB += histogram[i];
            if(wB == 0) continue;
            wF = total - wB;

            if(wF == 0) break;

            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if(varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }

        return threshold;

    }

    public static int[] getHistogram(BufferedImage img) {

        int[] hist = IntStream.range(0, 256).map(i->0).toArray();
        for(int i=0; i<img.getWidth(); i++) {
            for(int j=0; j<img.getHeight(); j++) {
                int red = new Color(img.getRGB (i, j)).getRed();
                hist[red]++;
            }
        }
        return hist;
    }

    private static BufferedImage RGBtoBinarize(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        for(int i=0;i<w;i++)
        {
            for(int j=0;j<h;j++)
            {
                //Get RGB Value
                int val = img.getRGB(i, j);
                //Convert to three separate channels
                int r = (0x00ff0000 & val) >> 16;
                int g = (0x0000ff00 & val) >> 8;
                int b = (0x000000ff & val);
                int m=(r+g+b);
                //(255+255+255)/2 =283 middle of dark and light
                if(m>=383)
                {
                    // for light color it set white
                    img.setRGB(i, j, Color.WHITE.getRGB());
                }
                else{
                    // for dark color it will set black
                    img.setRGB(i, j, 0);
                }
            }
        }
        return img;
    }
}
