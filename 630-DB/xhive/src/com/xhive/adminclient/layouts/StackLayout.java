package com.xhive.adminclient.layouts;

import java.awt.*;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Layout manager that conveniently layouts components as a stack,
 * i.e. it places all components on top of each other allowing
 * row heights to differ.
 *
 */
public class StackLayout implements LayoutManager {
    int verticalGap = 2;

    int max(int a, int b) {
        return (a > b ? a : b);
    }

    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    public Dimension preferredLayoutSize(Container parent) {
        int componentCount = parent.getComponentCount();
        int visibleCount = 0;
        double preferredHeight = 0;

        for (int i = 0; i < componentCount; i++) {
            Component component = parent.getComponent(i);

            if (component.isVisible()) {
                preferredHeight += component.getPreferredSize().getHeight();
                visibleCount++;
            }
        }

        preferredHeight += max(0, (visibleCount - 1) * verticalGap);
        Insets insets = parent.getInsets();
        Dimension preferredSize = new Dimension(getWidth(parent) + insets.left + insets.right,
                                                (int) preferredHeight + insets.top + insets.bottom);

        return preferredSize;
    }

    public int getWidth(Container parent) {
        int componentCount = parent.getComponentCount();
        int width = 0;

        for (int i = 0; i < componentCount; i++) {
            Component component = parent.getComponent(i);

            if (component.isVisible()) {
                width = max(width, (int) component.getPreferredSize().getWidth());
            }
        }

        return width;
    }

    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int componentCount = parent.getComponentCount();
        int currentTop = insets.top;
        int width = (int) parent.getSize().getWidth() - insets.left - insets.right;

        for (int i = 0; i < componentCount; i++) {
            Component component = parent.getComponent(i);

            if (component.isVisible()) {
                int componentHeight = (int) component.getPreferredSize().getHeight();
                component.setBounds(insets.left, currentTop, width, componentHeight);

                currentTop += componentHeight + verticalGap;
            }
        }
    }

    public void removeLayoutComponent(Component comp) {}


    public void addLayoutComponent(String name, Component comp) {}

}
