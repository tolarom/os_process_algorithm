package com.example.processsim;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class IconGenerator {
    public static void main(String[] args) throws Exception {
        int[] sizes = {16, 32, 48, 256};
        
        for (int size : sizes) {
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Background - rounded square
            g2.setColor(new Color(41, 128, 185));
            g2.fillRoundRect(0, 0, size, size, size/5, size/5);
            
            // CPU chip design
            g2.setColor(Color.WHITE);
            int margin = size / 5;
            int chipSize = size - 2 * margin;
            g2.fillRoundRect(margin, margin, chipSize, chipSize, size/10, size/10);
            
            // Inner chip
            g2.setColor(new Color(41, 128, 185));
            int innerMargin = size / 4;
            int innerSize = size - 2 * innerMargin;
            g2.fillRect(innerMargin, innerMargin, innerSize, innerSize);
            
            // CPU pins
            g2.setColor(Color.WHITE);
            int pinWidth = size / 12;
            int pinLen = size / 8;
            int pinCount = 3;
            
            for (int i = 0; i < pinCount; i++) {
                int offset = margin + (chipSize / (pinCount + 1)) * (i + 1) - pinWidth / 2;
                // Top
                g2.fillRect(offset, 0, pinWidth, pinLen);
                // Bottom
                g2.fillRect(offset, size - pinLen, pinWidth, pinLen);
                // Left
                g2.fillRect(0, offset, pinLen, pinWidth);
                // Right
                g2.fillRect(size - pinLen, offset, pinLen, pinWidth);
            }
            
            // "PS" text in center
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, size / 3));
            FontMetrics fm = g2.getFontMetrics();
            String text = "PS";
            int tx = (size - fm.stringWidth(text)) / 2;
            int ty = (size + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, tx, ty);
            
            g2.dispose();
            ImageIO.write(img, "PNG", new File("resources/icon-" + size + ".png"));
            System.out.println("Created icon-" + size + ".png");
        }
        
        System.out.println("\nNow convert to ICO using online tool or ImageMagick:");
        System.out.println("magick resources/icon-256.png resources/icon.ico");
    }
}
