package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
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

        String path = "G:/JavaProjects/cardsExample/src/SampleImages";

        //путь к шаблонам
        Path resourcesRankPath = Paths.get("resources", "rank");
        Path resourcesSuitPath = Paths.get("resources", "suit");
        String absoluteResourcesRankPath = resourcesRankPath.toFile().getAbsolutePath();
        String absoluteResourcesSuitPath = resourcesSuitPath.toFile().getAbsolutePath();


        //TODO:сделать метод для генерации мап из путей
        String normalResourcesRankPath = absoluteResourcesRankPath.replace("\\", "/");
        String normalResourcesSuitPath = absoluteResourcesSuitPath.replace("\\", "/");
        String normalSoughtPath = path.replace("\\", "/");

        List<Path> resourcesRankPathList = getImgPathNames(normalResourcesRankPath);
        List<Path> resourcesSuitPathList = getImgPathNames(normalResourcesSuitPath);
        List<Path> pathList = getImgPathNames(normalSoughtPath);

        List<BufferedImage> bufferedImages = getImageListFromPaths(pathList);
        Map<String, Path> templateRankFileMap = getTemplatePathMap(resourcesRankPathList);
        Map<String, Path> templateSuitFileMap = getTemplatePathMap(resourcesSuitPathList);

        levenshteinCompareMethod(templateRankFileMap, templateSuitFileMap, bufferedImages);

    }

    public static List<Path> getImgPathNames(String path) {
        List<Path> pathToFileList = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths.filter(Files::isRegularFile).forEach(pathToFileList::add);
        } catch (IOException e) {
            System.err.println("Wrong path or incorrect access : ");
            e.printStackTrace();
        }
        return pathToFileList;
    }

    public static BufferedImage readImageFile(Path pathToFile) {
        BufferedImage imageSource = null;
        try {
            imageSource = ImageIO.read(pathToFile.toFile());
        } catch (IOException e) {
            System.err.println("Cannot read the image file : ");
            e.printStackTrace();
        }
        return imageSource;
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

    /*private static List<StringBuilder> getStringListFromPath(List<Path> pathList) throws IOException {
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
    }*/
    //TODO:объеденить методы
    private static StringBuilder getStringFromImage(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        BufferedImage symbol = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = symbol.createGraphics();
        g.drawImage(image,0,0, null);

        int height = image.getHeight();
        int width = image.getWidth();
        short whiteBg = -1;
        StringBuilder binaryString = new StringBuilder();
        for (short y = 1; y < height; y++)
            for (short x = 1; x < width; x++) {
                int rgb = symbol.getRGB(x, y);
                binaryString.append(rgb == whiteBg ? " " : "*");
            }
        return binaryString;
    }

    public static void levenshteinCompareMethod(Map<String, Path> templateRankImageMap,
                                                Map<String, Path> templateSuitImageMap,
                                          List<BufferedImage> sampleImageList) throws IOException {
        for (int i = 0; i < sampleImageList.size(); i++) {
            BufferedImage identifyImage = sampleImageList.get(i);
            String[] foundRank = {"?", "?", "?", "?", "?"};
            String[] foundSuits = {"?", "?", "?", "?", "?"};



            for (int cardNum = 0; cardNum < Constants.CARDS_POS_X.length; cardNum++) {
                //обрабатываем ранг
                int min = Integer.MAX_VALUE;
                for (Map.Entry<String, Path> entry : templateRankImageMap.entrySet()) {
                    String rankName = entry.getKey();
                    Path templatePath = entry.getValue();
                    File templateFile = new File(String.valueOf(templatePath));
                    BufferedImage templateImage = readImageFile(templatePath);

                    File cutFileImage = getSubFileFromImage(identifyImage, templateImage,
                            Constants.CARDS_POS_X[cardNum],
                            Constants.RANK_POS_Y);
                    StringBuilder templateStringFile = getStringFromImage(templateFile);
                    StringBuilder cutStringFile = getStringFromImage(cutFileImage);
                    int levenshtein = levensteinMin(cutStringFile.toString(), templateStringFile.toString());
                    if (levenshtein <= min && levenshtein <= 60) {
                        min = levenshtein;
                        foundRank[cardNum] = rankName;
                    }

                }
                //TODO: МАСТИ
                for (Map.Entry<String, Path> entry : templateSuitImageMap.entrySet()) {
                    String suitName = entry.getKey();
                    Path templatePath = entry.getValue();
                    File templateFile = new File(String.valueOf(templatePath));
                    BufferedImage templateImage = readImageFile(templatePath);

                    File cutFileImage = getSubFileFromImage(identifyImage, templateImage,
                            Constants.CARDS_POS_X[cardNum] + Constants.SUIT_OFFSET_X,
                            Constants.SUIT_POS_Y);

                    StringBuilder templateStringFile = getStringFromImage(templateFile);
                    StringBuilder cutStringFile = getStringFromImage(cutFileImage);
                    int levenshtein = levensteinMin(cutStringFile.toString(), templateStringFile.toString());
                    min = 50;
                    if (levenshtein <= min) {
                        foundSuits[cardNum] = suitName;
                    }
                }
            }
            //TODO:сделать нормальный вывод

            for(int j = 0; j<foundRank.length; j++)
            {
                System.out.print(foundRank[j]+""+foundSuits[j]);
            }
            System.out.println();
        }
    }
    public static int levensteinMin( String str1, String str2) {
        int[] Di_1 = new int[str2.length() + 1];
        int[] Di = new int[str2.length() + 1];

        for (int j = 0; j <= str2.length(); j++) {
            Di[j] = j; // (i == 0)
        }

        for (int i = 1; i <= str1.length(); i++) {
            System.arraycopy(Di, 0, Di_1, 0, Di_1.length);

            Di[0] = i; // (j == 0)
            for (int j = 1; j <= str2.length(); j++) {
                int cost = (str1.charAt(i - 1) != str2.charAt(j - 1)) ? 1 : 0;
                Di[j] = min(
                        Di_1[j] + 1,
                        Di[j - 1] + 1,
                        Di_1[j - 1] + cost
                );
            }
        }
        return Di[Di.length - 1];
    }

    private static int min(int n1, int n2, int n3) {
        return Math.min(Math.min(n1, n2), n3);
    }

    public static File getSubFileFromImage(BufferedImage identifyImage,
                                           BufferedImage templateImage,
                                           int offsetX, int positionY) throws IOException {
        BufferedImage cuttedImage = identifyImage.getSubimage(offsetX, positionY,
                templateImage.getWidth(), templateImage.getHeight());

        cuttedImage = ImageService.RGBtoBinarize(cuttedImage);

        File outputStream = new File("compare.png");
        ImageIO.write(cuttedImage, "png", outputStream);
        return outputStream;
    }

}
