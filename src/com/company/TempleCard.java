package com.company;

import java.awt.image.BufferedImage;

public class TempleCard {
    private String name;
    private StringBuilder stringForm;
    private BufferedImage bufferedImage;


    public TempleCard() {
    }

    public TempleCard(String name, StringBuilder stringForm, BufferedImage bufferedImage) {
        this.name = name;
        this.stringForm = stringForm;
        this.bufferedImage = bufferedImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StringBuilder getStringForm() {
        return stringForm;
    }

    public void setStringForm(StringBuilder stringForm) {
        this.stringForm = stringForm;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }
}
