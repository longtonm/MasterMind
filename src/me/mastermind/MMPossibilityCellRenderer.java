/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.mastermind;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.font.TextAttribute;
import javax.swing.*;
import java.text.AttributedString;

/**
 *
 * @author mlongton
 */
public class MMPossibilityCellRenderer implements ListCellRenderer<Possibility> {

    public Component getListCellRendererComponent(JList<? extends Possibility> list, Possibility p, int index, boolean isSelected, boolean cellHasFocus) {
        MMPossibilityListCell theCell = new MMPossibilityListCell(p);
        if (isSelected) {
            theCell.setBackground(list.getSelectionBackground());
        }
        else {
            //theCell.setBackground(list.getBackground());
            theCell.setBackground(UIManager.getDefaults().getColor("List.background"));
            //System.err.println(UIManager.getDefaults().getColor("List.background"));
        }
        theCell.setEnabled(list.isEnabled());
        if (cellHasFocus) {
            theCell.setBorder(UIManager.getDefaults().getBorder("List.focusCellHighlightBorder"));
        }
        return theCell;
    }
}
