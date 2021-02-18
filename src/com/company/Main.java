package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException {

        Scanner in = new Scanner(System.in);
        System.out.print("Enter name of directory: ");
        String path = in.nextLine();
        in.close();

        //путь к шаблонам
        Path resourcesRankPath = Paths.get("resources", "rank");
        Path resourcesSuitPath = Paths.get("resources", "suit");
        String absoluteResourcesRankPath = resourcesRankPath.toFile().getAbsolutePath();
        String absoluteResourcesSuitPath = resourcesSuitPath.toFile().getAbsolutePath();
        String normalSoughtPath = path.replace("\\", "/");
        List<Path> pathList = getImgPathNames(normalSoughtPath);
        //List<BufferedImage> bufferedImages = getImageListFromPaths(pathList);
       /* Map<String, Path> templateRankFileMap = generateMapFromPathList(absoluteResourcesRankPath);
        Map<String, Path> templateSuitFileMap = generateMapFromPathList(absoluteResourcesSuitPath);*/

        List<TempleCard> templeCardRankList = templeCardList(absoluteResourcesRankPath);
        List<TempleCard> templeCardSuitList = templeCardList(absoluteResourcesSuitPath);

        //levenshteinCompareMethod(templateRankFileMap, templateSuitFileMap, pathList);
        levenshteinCompareMethod(templeCardRankList, templeCardSuitList, pathList);
    }

    public static Map<String, Path> generateMapFromPathList(String pathDir) {
        String normalDirPath = pathDir.replace("\\", "/");
        List<Path> pathList = getImgPathNames(normalDirPath);
        Map<String, Path> resultMap = getTemplatePathMap(pathList);
        return resultMap;
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

    public static List<TempleCard> templeCardList(String path) throws IOException {
        List<TempleCard> resultList = new ArrayList<>();
        List<Path> pathToFileList = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths.filter(Files::isRegularFile).forEach(pathToFileList::add);
        } catch (IOException e) {
            System.err.println("Wrong path or incorrect access : ");
            e.printStackTrace();
        }
        for (Path p: pathToFileList) {
            resultList.add(new TempleCard(p.getFileName().toString().split("\\.")[0],
                    getStringFromImage(ImageIO.read(p.toFile())),
                    ImageIO.read(p.toFile())));
        }
        return resultList;
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
            resultMap.put(p.getFileName().toString().split("\\.")[0] , p);
        }
        return resultMap;
    }

    private static StringBuilder getStringFromImage(BufferedImage image) {
        BufferedImage symbol = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = symbol.createGraphics();
        g.drawImage(image,0,0, null);

        int height = image.getHeight();
        int width = image.getWidth();
        short whiteBg = -1;
        StringBuilder binaryString = new StringBuilder();
        for (int y = 1; y < height; y++) {
            for (int x = 1; x < width; x++) {
                int rgb = symbol.getRGB(x, y);
                binaryString.append(rgb == whiteBg ? " " : "*");
            }
        }
        return binaryString;
    }

    public static void levenshteinCompareMethod(/*Map<String, Path> templateRankImageMap,
                                                Map<String, Path> templateSuitImageMap,*/
                                          /*List<BufferedImage> sampleImageList*/
                                                List<TempleCard> templateRankCardList,
                                                List<TempleCard> templateSuitCardList,
                                                List<Path> samplePathList) throws IOException {

        int notFoundCardsCount = 0;
        int foundCardsCount = 0;
        for (int i = 0; i < samplePathList.size(); i++) {
            BufferedImage identifyImage = ImageIO.read(samplePathList.get(i).toFile());
            String[] foundRank = {"?", "?", "?", "?", "?"};
            String[] foundSuits = {"?", "?", "?", "?", "?"};

            for (int cardNum = 0; cardNum < Constants.CARDS_POS_X.length; cardNum++) {
                int min = Integer.MAX_VALUE;
                //for (Map.Entry<String, Path> entry : templateRankImageMap.entrySet()) {
                for (TempleCard card:templateRankCardList) {
                    String rankName = card.getName();
                    //Path templatePath = entry.getValue();
                    BufferedImage templateImage = card.getBufferedImage();

                    BufferedImage cutFileImage = getSubFileFromImage(identifyImage, templateImage,
                            Constants.CARDS_POS_X[cardNum],
                            Constants.RANK_POS_Y);
                    StringBuilder templateStringFile = card.getStringForm();
                    StringBuilder cutStringFile = getStringFromImage(cutFileImage);
                    int levenshtein = levensteinMin(cutStringFile.toString(), templateStringFile.toString());
                    if (levenshtein <= min && levenshtein <= 60) {
                        min = levenshtein;
                        foundRank[cardNum] = rankName;
                    }
                }
                //for (Map.Entry<String, Path> entry : templateSuitImageMap.entrySet()) {
                for (TempleCard card: templateSuitCardList) {


                    String suitName = card.getName();
                    //Path templatePath = entry.getValue();
                    BufferedImage templateImage = card.getBufferedImage();

                    BufferedImage cutFileImage = getSubFileFromImage(identifyImage, templateImage,
                            Constants.CARDS_POS_X[cardNum] + Constants.SUIT_OFFSET_X,
                            Constants.SUIT_POS_Y);
                    StringBuilder templateStringFile = card.getStringForm();
                    StringBuilder cutStringFile = getStringFromImage(cutFileImage);
                    int levenshtein = levensteinMin(cutStringFile.toString(), templateStringFile.toString());
                    min = 70;
                    if (levenshtein <= min) {
                        foundSuits[cardNum] = suitName;
                    }
                }
            }

            System.out.print(samplePathList.get(i).getFileName() + " - ");
            for(int j = 0; j<foundRank.length; j++)
            {
                String s = foundRank[j]+foundSuits[j];
                String str = s.replace("??", "");
                System.out.print(str);
                if(str.contains("?")) {
                    notFoundCardsCount++;
                } else {
                    foundCardsCount++;
                }
            }
            System.out.println();
        }
        System.out.println("Identified = " + foundCardsCount);
        System.out.println("Unidentified = " + notFoundCardsCount);

    }
    public static int levensteinMin( String str1, String str2) {
        int[] Di_1 = new int[str2.length() + 1];
        int[] Di = new int[str2.length() + 1];

        for (int j = 0; j <= str2.length(); j++) {
            Di[j] = j;
        }

        for (int i = 1; i <= str1.length(); i++) {
            System.arraycopy(Di, 0, Di_1, 0, Di_1.length);

            Di[0] = i;
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

    public static BufferedImage getSubFileFromImage(BufferedImage identifyImage,
                                           BufferedImage templateImage,
                                           int offsetX, int positionY) {
        BufferedImage cutImage = identifyImage.getSubimage(offsetX, positionY,
                templateImage.getWidth(), templateImage.getHeight());

        cutImage = RGBtoBinarize(cutImage);
        return cutImage;
    }

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
                if(m>=355)
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
