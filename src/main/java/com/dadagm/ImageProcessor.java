package com.dadagm;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

public class ImageProcessor {
    private I18nManager i18nManager;

    public ImageProcessor() {
        i18nManager = I18nManager.getInstance();
    }

    public void processImages(String romsPath, String coversPath, boolean addSubtitle) {
        System.out.println(i18nManager.getString("welcome.message"));

        File romsDir = new File(romsPath);
        File coversDir = new File(coversPath);

        if (!coversDir.exists() && !coversDir.mkdirs()) {
            String errorMsg = i18nManager.getString("create.covers.folder.error");
            System.err.println(errorMsg);
            return;
        }

        if (romsDir.exists() && romsDir.isDirectory()) {
            processDirectory(romsDir, coversDir, addSubtitle, 0);
        }
    }
    
    private void processDirectory(File sourceDir, File targetDir, boolean addSubtitle, int depth) {
        if (depth > 10) {
            System.out.println("达到最大目录深度，跳过: " + sourceDir.getAbsolutePath());
            return;
        }
        
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            String errorMsg = i18nManager.getString("create.subfolder.error") + targetDir.getAbsolutePath();
            System.err.println(errorMsg);
            return;
        }
        
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isImageFile(file)) {
                    try {
                        String newFileName = file.getName().replaceFirst("[.][^.]+$", "");
                        File imgOutputFile = new File(targetDir, newFileName + ".img");
                        BufferedImage originalImage = ImageIO.read(file);
                        BufferedImage resizedImage = resizeImageWithAspectRatio(originalImage, 168, 100);

                        BufferedImage finalImage = resizedImage;
                        if (addSubtitle) {
                            finalImage = addTextToImage(resizedImage, newFileName);
                        }
                        ImageIO.write(finalImage, "jpeg", imgOutputFile);
                        System.out.println("save img: " + imgOutputFile.getAbsolutePath());
                    } catch (IOException e) {
                        System.err.println("f " + file.getAbsolutePath() + " "
                                + i18nManager.getString("process.error") + " " + e.getMessage());
                    }
                } else if (file.isDirectory()) {
                    File newTargetDir = new File(targetDir, file.getName());
                    processDirectory(file, newTargetDir, addSubtitle, depth + 1);
                }
            }
        }
    }

    public void incrementalUpdateImages(String romsPath, String coversPath, boolean addSubtitle) {
        System.out.println(i18nManager.getString("incremental.welcome.message"));

        File romsDir = new File(romsPath);
        File coversDir = new File(coversPath);

        if (!coversDir.exists() && !coversDir.mkdirs()) {
            String errorMsg = i18nManager.getString("create.covers.folder.error");
            System.err.println(errorMsg);
            return;
        }

        if (romsDir.exists() && romsDir.isDirectory()) {
            incrementalProcessDirectory(romsDir, coversDir, addSubtitle, 0);
        }
    }
    
    private void incrementalProcessDirectory(File sourceDir, File targetDir, boolean addSubtitle, int depth) {
        if (depth > 10) {
            System.out.println("达到最大目录深度，跳过: " + sourceDir.getAbsolutePath());
            return;
        }
        
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            String errorMsg = i18nManager.getString("create.subfolder.error") + targetDir.getAbsolutePath();
            System.err.println(errorMsg);
            return;
        }
        
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isImageFile(file)) {
                    String newFileName = file.getName().replaceFirst("[.][^.]+$", "");
                    File imgOutputFile = new File(targetDir, newFileName + ".img");
                    if (!imgOutputFile.exists()) {
                        try {
                            BufferedImage originalImage = ImageIO.read(file);
                            BufferedImage resizedImage = resizeImageWithAspectRatio(originalImage, 168, 100);

                            BufferedImage finalImage = resizedImage;
                            if (addSubtitle) {
                                finalImage = addTextToImage(resizedImage, newFileName);
                            }
                            ImageIO.write(finalImage, "jpeg", imgOutputFile);
                            System.out.println("img: " + imgOutputFile.getAbsolutePath());
                        } catch (IOException e) {
                            System.err.println("f: " + file.getAbsolutePath() + " error: " + e.getMessage());
                        }
                    }
                } else if (file.isDirectory()) {
                    File newTargetDir = new File(targetDir, file.getName());
                    incrementalProcessDirectory(file, newTargetDir, addSubtitle, depth + 1);
                }
            }
        }
    }

    public void extractBoxFrontFiles(String extractPath) {
        System.out.println(i18nManager.getString("extract.start.message"));

        if (extractPath == null || extractPath.isEmpty()) {
            System.err.println("请选择提取路径");
            return;
        }

        File romsDir = new File(extractPath);

        System.out.println("romsDir>" + romsDir.toPath());

        if (!romsDir.exists() || !romsDir.isDirectory()) {
            System.err.println("提取路径无效");
            return;
        }
        System.out.println("处理zip文件...");
        processZipFiles(romsDir);
        System.out.println("zip文件处理完成");
        
        System.out.println("提取路径: " + romsDir.getAbsolutePath());
        File[] subDirs = romsDir.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                System.out.println("处理子文件夹: " + subDir.getName());
                
                File mediaDir = new File(subDir, "media");
                if (mediaDir.exists() && mediaDir.isDirectory()) {
                    System.out.println("找到media文件夹: " + mediaDir.getAbsolutePath());
                    
                    File[] mediaSubDirs = mediaDir.listFiles(File::isDirectory);
                    if (mediaSubDirs != null) {
                        for (File mediaSubDir : mediaSubDirs) {
                            System.out.println("处理media子文件夹: " + mediaSubDir.getName());
                            
                            File[] boxFrontFiles = mediaSubDir.listFiles(file -> {
                                String name = file.getName().toLowerCase();
                                return name.contains("boxfront") && (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"));
                            });
                            
                            if (boxFrontFiles != null && boxFrontFiles.length > 0) {
                                for (File boxFrontFile : boxFrontFiles) {
                                    try {
                                        BufferedImage originalImage = ImageIO.read(boxFrontFile);
                                        BufferedImage resizedImage = resizeImageWithAspectRatio(originalImage, 168, 100);
                                        
                                        String fileName = mediaSubDir.getName();
                                        String extension = boxFrontFile.getName().substring(boxFrontFile.getName().lastIndexOf('.'));
                                        File outputFile = new File(subDir, fileName + extension);
                                        String formatName = extension.substring(1).toLowerCase();
                                        ImageIO.write(resizedImage, formatName, outputFile);
                                        System.out.println("提取boxFront文件: " + outputFile.getAbsolutePath());
                                    } catch (IOException e) {
                                        System.err.println("提取错误: " + boxFrontFile.getAbsolutePath() + " " + e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println(i18nManager.getString("extract.success.message"));
    }

    private void processZipFiles(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    processZipFiles(file);
                } else if (file.getName().toLowerCase().endsWith(".zip")) {
                    try {
                        unzipFile(file, file.getParentFile());
                    } catch (IOException e) {
                        System.err.println(
                                i18nManager.getString("unzip.error") + file.getAbsolutePath() + " " + e.getMessage());
                    }
                }
            }
        }
    }

    private void unzipFile(File zipFile, File destDir) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile)) {
            java.util.Enumeration<? extends ZipArchiveEntry> entries = zip.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                File entryFile = new File(destDir, entry.getName());

                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    entryFile.getParentFile().mkdirs();
                    try (InputStream in = zip.getInputStream(entry);
                            OutputStream out = new FileOutputStream(entryFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp");
    }

    private BufferedImage resizeImageWithAspectRatio(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        double ratio = Math.min((double) maxWidth / width, (double) maxHeight / height);

        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        Image resultingImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }

    private BufferedImage addTextToImage(BufferedImage image, String text) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = newImage.createGraphics();

        g2d.drawImage(image, 0, 0, null);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font font = new Font("Arial", Font.BOLD, 14);
        g2d.setFont(font);

        FontMetrics metrics = g2d.getFontMetrics();

        int textAreaWidth = (int) (image.getWidth() * 0.85);

        List<String> lines = wrapText(text, font, g2d, textAreaWidth);

        int lineHeight = metrics.getHeight();
        int totalTextHeight = lines.size() * lineHeight;

        int textY = image.getHeight() - totalTextHeight - (lineHeight / 2) - 5; 
        int textX = (image.getWidth() - textAreaWidth) / 2; 

        for (String line : lines) {
            int lineWidth = metrics.stringWidth(line);
            int lineX = (image.getWidth() - lineWidth) / 2; 

            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillRect(lineX - 5, textY - metrics.getAscent() + 2 - 2, lineWidth + 10, lineHeight);

            g2d.setColor(Color.BLACK);
            g2d.drawString(line, lineX, textY);

            textY += lineHeight;
        }

        g2d.dispose();
        return newImage;
    }

    private List<String> wrapText(String text, Font font, Graphics2D g2d, int maxWidth) {
        List<String> lines = new ArrayList<>();
        FontMetrics metrics = g2d.getFontMetrics(font);

        text = text.trim();

        if (metrics.stringWidth(text) <= maxWidth) {
            lines.add(text);
            return lines;
        }

        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (metrics.stringWidth(word) > maxWidth) {
                StringBuilder currentWord = new StringBuilder();
                for (char c : word.toCharArray()) {
                    String testWord = currentWord.toString() + c;
                    if (metrics.stringWidth(testWord) > maxWidth) {
                        if (currentLine.length() > 0) {
                            lines.add(currentLine.toString());
                            currentLine.setLength(0);
                        }
                        lines.add(currentWord.toString());
                        currentWord.setLength(0);
                        currentWord.append(c);
                    } else {
                        currentWord.append(c);
                    }
                }
                if (currentWord.length() > 0) {
                    word = currentWord.toString();
                } else {
                    continue;
                }
            }

            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (metrics.stringWidth(testLine) <= maxWidth) {
                currentLine.append(currentLine.length() == 0 ? "" : " ").append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}