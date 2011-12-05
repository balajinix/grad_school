package com.xhive.adminclient.tablemodels;

import javax.swing.*;
import java.util.Vector;

public class XhiveComboBoxModel extends DefaultComboBoxModel {

    public XhiveComboBoxModel(Vector items) {
        super(items);
    }

    public void addElement(Object obj) {
        if (getIndexOf(obj) >= 0) {
            removeElement(obj);
            System.out.println("element bestond al");
        }
        insertElementAt(obj, 0);
    }

    public void insertElementAt(Object anObject, int index) {
        super.insertElementAt(anObject, index);
    }
}
