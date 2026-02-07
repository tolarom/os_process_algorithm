package com.example.processsim;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IconCreator {
    public static void main(String[] args) throws Exception {
        // Create the icon image
        int size = 256;
        BufferedImage img = createIcon(size);
        
        // Save as PNG first
        ImageIO.write(img, "PNG", new File("resources/icon.png"));
        System.out.println("Created icon.png");
        
        // Create ICO file with multiple sizes
        int[] sizes = {256, 48, 32, 16};
        BufferedImage[] images = new BufferedImage[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            images[i] = createIcon(sizes[i]);
        }
        
        writeIco(images, new File("resources/icon.ico"));
        System.out.println("Created icon.ico");
    }
    
    static BufferedImage createIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Background gradient
        GradientPaint gp = new GradientPaint(0, 0, new Color(52, 152, 219), size, size, new Color(41, 128, 185));
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, size, size, size/4, size/4);
        
        // CPU chip - white square
        g2.setColor(new Color(255, 255, 255, 230));
        int chipMargin = size / 5;
        int chipSize = size - 2 * chipMargin;
        g2.fillRoundRect(chipMargin, chipMargin, chipSize, chipSize, size/8, size/8);
        
        // Inner processor
        g2.setColor(new Color(41, 128, 185));
        int innerMargin = size / 4;
        int innerSize = size / 2;
        g2.fillRoundRect(innerMargin, innerMargin, innerSize, innerSize, size/12, size/12);
        
        // CPU pins on all sides
        g2.setColor(new Color(255, 255, 255, 200));
        int pinWidth = Math.max(2, size / 16);
        int pinLen = size / 6;
        int pinGap = size / 8;
        
        for (int i = 0; i < 3; i++) {
            int offset = chipMargin + pinGap + i * (chipSize - 2 * pinGap) / 2 - pinWidth / 2;
            // Top pins
            g2.fillRoundRect(offset, 2, pinWidth, pinLen, 2, 2);
            // Bottom pins
            g2.fillRoundRect(offset, size - pinLen - 2, pinWidth, pinLen, 2, 2);
            // Left pins
            g2.fillRoundRect(2, offset, pinLen, pinWidth, 2, 2);
            // Right pins
            g2.fillRoundRect(size - pinLen - 2, offset, pinLen, pinWidth, 2, 2);
        }
        
        // "PS" text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, size * 2 / 5));
        FontMetrics fm = g2.getFontMetrics();
        String text = "PS";
        int tx = (size - fm.stringWidth(text)) / 2;
        int ty = size / 2 + fm.getAscent() / 3;
        g2.drawString(text, tx, ty);
        
        g2.dispose();
        return img;
    }
    
    static void writeIco(BufferedImage[] images, File file) throws IOException {
        ByteArrayOutputStream[] pngData = new ByteArrayOutputStream[images.length];
        for (int i = 0; i < images.length; i++) {
            pngData[i] = new ByteArrayOutputStream();
            ImageIO.write(images[i], "PNG", pngData[i]);
        }
        
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            // ICO header
            dos.writeShort(0); // reserved
            dos.writeShort(swapShort((short) 1)); // type: 1 = icon
            dos.writeShort(swapShort((short) images.length)); // image count
            
            int offset = 6 + images.length * 16; // header + directory entries
            
            // Directory entries
            for (int i = 0; i < images.length; i++) {
                int w = images[i].getWidth();
                int h = images[i].getHeight();
                dos.writeByte(w >= 256 ? 0 : w); // width
                dos.writeByte(h >= 256 ? 0 : h); // height
                dos.writeByte(0); // color palette
                dos.writeByte(0); // reserved
                dos.writeShort(swapShort((short) 1)); // color planes
                dos.writeShort(swapShort((short) 32)); // bits per pixel
                dos.writeInt(swapInt(pngData[i].size())); // image size
                dos.writeInt(swapInt(offset)); // offset
                offset += pngData[i].size();
            }
            
            // Image data
            for (ByteArrayOutputStream png : pngData) {
                dos.write(png.toByteArray());
            }
        }
    }
    
    static short swapShort(short v) {
        return (short) (((v & 0xFF) << 8) | ((v >> 8) & 0xFF));
    }
    
    static int swapInt(int v) {
        return ((v & 0xFF) << 24) | ((v & 0xFF00) << 8) | ((v >> 8) & 0xFF00) | ((v >> 24) & 0xFF);
    }
}
