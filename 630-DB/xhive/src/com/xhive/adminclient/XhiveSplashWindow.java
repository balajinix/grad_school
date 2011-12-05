package com.xhive.adminclient;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;

import javax.swing.JWindow;

import com.xhive.XhiveDriverFactory;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Splash-screen window (used at startup).
 */

public class XhiveSplashWindow extends JWindow {

    public XhiveSplashWindow(int width, int height, Image image) {
        setSize(width, height);
        SplashImage splashImage = new SplashImage(image);
        getContentPane().add(splashImage);
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point centerPoint = graphicsEnvironment.getCenterPoint();
        int left = centerPoint.x - (width / 2);
        int top = centerPoint.y - (height / 2);
        setLocation(left, top);
        splashImage.setSize(width, height);
    }

    /**
     * This method paints a version number on a canvas. All the font processing is for optimization,
     * so that the font only needs to be derived once.
     */
    public static Font paintVersion(Graphics g, Font f) {
        if (f == null) {
            Font baseFont = g.getFont();
            f = baseFont.deriveFont(Font.BOLD, baseFont.getSize() + 2);
        }
        g.setFont(f);
        g.setColor(Color.white);
        g.drawString(XhiveDriverFactory.getXhiveVersion(), 5, f.getSize() + 5);
        return f;
    }

    class SplashImage extends Canvas {

        Image image;
        Font font;

        public SplashImage(Image image) {
            this.image = image;
        }

        @Override
        public void paint(Graphics g) {
            if (g.drawImage(image, 0, 0, this) || font == null) {
                font = paintVersion(g, font);
            }
        }
    }
}
