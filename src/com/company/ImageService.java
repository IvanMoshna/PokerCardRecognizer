package com.company;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

public class ImageService {

    public static BufferedImage RGBtoBinarize(BufferedImage img) {
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
                //383
                if(m>=360)
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
