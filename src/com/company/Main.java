package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException {

        final String path = "G:/JavaProjects/cardsExample/src/SampleImages";

        //путь к шаблонам
        //TODO:разделить масти и ранги по папкам
        Path resourcesPath = Paths.get("resources");
        String absoluteResourcesPath = resourcesPath.toFile().getAbsolutePath();
        String normalResourcesPath = absoluteResourcesPath.replace("\\", "/");
        String normalSoughtPath = absoluteResourcesPath.replace("\\", "/");

        List<Path> resourcesPathList = getImgPathNames(normalResourcesPath);
        List<Path> pathList = getImgPathNames(normalSoughtPath);

        List<BufferedImage> bufferedImages = getImageListFromPaths(pathList);
        Map<String, Path> templateFileMap = getTemplatePathMap(resourcesPathList);

        List<StringBuilder> sampleStringBuilderList = getStringListFromPath(pathList);


    }

    public static List<Path> getImgPathNames(String path) {
       /* File dir = new File(path);
        List<String> imgNames = new ArrayList<>();
        if (dir.isDirectory()) {
            for (File item : dir.listFiles()) {
                imgNames.add(item.getName());
            }
        }*/
        List<Path> pathToFileList = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths.filter(Files::isRegularFile).forEach(pathToFileList::add);
        } catch (IOException e) {
            System.err.println("Wrong path or incorrect access : ");
            e.printStackTrace();
        }
        return pathToFileList;
    }

    public static List<BufferedImage> getImageListFromPaths(List<Path> pathList) {
        List<BufferedImage> resultList = new ArrayList<>();
        for (Path p:pathList) {
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(p.toFile());
                resultList.add(bufferedImage);
            } catch (IOException e) {
                System.err.println("Cannot read the image file : ");
                e.printStackTrace();
            }
        }
        return resultList;
    }

    public static Map<String, Path> getTemplatePathMap(List<Path> paths) {

        Map<String, Path> resultMap = new HashMap<>();
        for (Path p:paths) {
            //TODO:придумать как делать без создания файла
            File f = new File(p.toString());
            resultMap.put(f.getName().split("\\.")[0] , p);
        }
        return resultMap;
    }

    private static List<StringBuilder> getStringListFromPath(List<Path> pathList) throws IOException {
        List<StringBuilder> resultList = new ArrayList<>();
        for (Path p:pathList) {
            BufferedImage image = ImageIO.read(new File(String.valueOf(p)));
            BufferedImage symbol = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
            Graphics2D g = symbol.createGraphics();
            g.drawImage(image, 0, 0, null);
            int height = image.getHeight();
            int width = image.getWidth();
            short whiteBg = -1;
            StringBuilder binaryString = new StringBuilder();
            for (short y = 1; y < height; y++)
                for (short x = 1; x < width; x++) {
                    int rgb = symbol.getRGB(x, y);
                    binaryString.append(rgb == whiteBg ? " " : "*");
                }
            resultList.add(binaryString);
        }
        return resultList;
    }
}
