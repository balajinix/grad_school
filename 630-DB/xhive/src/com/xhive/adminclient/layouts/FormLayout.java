package com.xhive.adminclient.layouts;

import java.awt.*;

/**
 * (c) X-Hive Corporation B.V. (www.x-hive.com)
 * Xhive Admin
 *
 * [DESCRIPTION]
 * Layout manager that conveniently layouts components as a form,
 * i.e. it divides the space into two columns, the left column is as
 * wide as the widest component and the right column takes up the rest of
 * the space. Row height is determined per row by the height of the
 * highest component. Components are aligned at the top-left of each
 * cell in the resulting grid. Typically the components in the left
 * column will be labels and components in the right column will be
 * input fields.
 *
 */
// TODO (ADQ) : Isn't there an existing layout to use?
public class FormLayout implements LayoutManager {
    static final int LEFT_COLUMN = 0;
    static final int RIGHT_COLUMN = 1;

    int horizontalGap = 10, verticalGap = 2;

    int max(int a, int b) {
        return (a > b ? a : b);
    }

    public FormLayout() {
        this(10, 2);
    }

    public FormLayout(int horizontalGap, int verticalGap) {
        super();

        this.horizontalGap = horizontalGap;
        this.verticalGap = verticalGap;
    }

    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    public Dimension preferredLayoutSize(Container parent) {
        int componentCount = parent.getComponentCount();
        int visibleCount = 0;
        double preferredHeight = 0, preferredWidth = 0;

        for (int i = 0; i < componentCount; i += 2) {
            Component leftComponent = parent.getComponent(i);
            Component rightComponent = null;

            if (leftComponent.isVisible()) {
                if (i + 1 < componentCount) {
                    rightComponent = parent.getComponent(i + 1);

                    preferredHeight += max((int) leftComponent.getPreferredSize().getHeight(),
                                           (int) rightComponent.getPreferredSize().getHeight());
                } else {
                    preferredHeight += leftComponent.getPreferredSize().getHeight();
                }

                visibleCount++;
            }

            if (rightComponent != null) {
                rightComponent.setVisible(leftComponent.isVisible());
            }
        }

        Insets insets = parent.getInsets();
        preferredHeight += max(0, (visibleCount - 1) * verticalGap) + insets.top + insets.bottom;
        preferredWidth = getColumnWidth(parent, LEFT_COLUMN) + horizontalGap + getColumnWidth(parent, RIGHT_COLUMN) +
                         insets.left + insets.right;

        Dimension preferredSize = new Dimension((int) preferredWidth, (int) preferredHeight);

        return preferredSize;
    }

    public int getColumnWidth(Container parent, int column) {
        int componentCount = parent.getComponentCount();
        int width = 0;

        for (int i = column; i < componentCount; i += 2) {
            Component component = parent.getComponent(i);

            if (component.isVisible() &&
                    (column == LEFT_COLUMN || (column == RIGHT_COLUMN && parent.getComponent(i - 1).isVisible()))) {
                width = max(width, (int) component.getPreferredSize().getWidth());
            }
        }

        return width;
    }

    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int componentCount = parent.getComponentCount();
        int leftColumnWidth = getColumnWidth(parent, LEFT_COLUMN);
        int currentTop = insets.top;
        int startRightColumn = insets.left + leftColumnWidth + horizontalGap;
        int endRightColumn = startRightColumn + insets.right;

        for (int i = 0; i < componentCount; i += 2) {
            Component leftComponent = parent.getComponent(i);
            Component rightComponent = (i + 1 < componentCount ? parent.getComponent(i + 1) : null);

            if (leftComponent.isVisible()) {
                leftComponent.setBounds(insets.left, currentTop, leftColumnWidth, (int) leftComponent.getPreferredSize().getHeight());

                if (rightComponent != null) {
                    rightComponent.setBounds(startRightColumn, currentTop,
                                             (int) parent.getSize().getWidth() - endRightColumn, (int) rightComponent.getPreferredSize().getHeight());
                    currentTop += max((int) leftComponent.getPreferredSize().getHeight(),
                                      (int) rightComponent.getPreferredSize().getHeight()) + verticalGap;
                }
            }

            if (rightComponent != null) {
                rightComponent.setVisible(leftComponent.isVisible());
            }
        }
    }

    public void removeLayoutComponent(Component comp) {}


    public void addLayoutComponent(String name, Component comp) {}

}
