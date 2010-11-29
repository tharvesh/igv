/*
 * Copyright (c) 2007-2011 by The Broad Institute, Inc. and the Massachusetts Institute of
 * Technology.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */

/*
 * HeatmapLegendEditor.java
 *
 * Created on June 24, 2008, 8:59 AM
 */
package org.broad.igv.ui.legend;

import org.broad.igv.renderer.ColorScale;
import org.broad.igv.renderer.ContinuousColorScale;
import org.broad.igv.track.TrackType;

import javax.swing.*;
import java.awt.*;

/**
 * @author jrobinso
 */
public class HeatmapLegendEditor extends javax.swing.JDialog {

    private boolean canceled = true;
    private ContinuousColorScale colorScheme;
    private TrackType type;

    /**
     * Creates new form HeatmapLegendEditor
     */
    public HeatmapLegendEditor(java.awt.Frame parent, boolean modal, TrackType type, ColorScale colorScheme) {
        super(parent, modal);
        this.colorScheme = (ContinuousColorScale) colorScheme;
        this.type = type;
        initComponents();
        initValues();
        this.setLocationRelativeTo(parent);
        this.getRootPane().setDefaultButton(okButton);
    }

    private void initValues() {
        doubleGradientCheckbox.setSelected(getColorScheme().isUseDoubleGradient());
        negRangeStart.setText(String.valueOf(getColorScheme().getNegStart()));
        negRangeEnd.setText(String.valueOf(getColorScheme().getMinimum()));
        posRangeStart.setText(String.valueOf(getColorScheme().getPosStart()));
        posRangeEnd.setText(String.valueOf(getColorScheme().getMaximum()));
        minColor.setSelectedColor(getColorScheme().getMinColor());
        maxColor.setSelectedColor(getColorScheme().getMaxColor());

        // Single gradient color schems might have a null mid color.  Default
        // to white in that case, a non-null color is required.
        Color mc = getColorScheme().getMidColor();
        midColor.setSelectedColor(mc == null ? Color.white : mc);

        initDoubleGradientState();

    }

    private void initDoubleGradientState() {
        final boolean doubleGradient = doubleGradientCheckbox.isSelected();
        negRangePanel.setVisible(doubleGradient);
        midColorLabel.setVisible(doubleGradient);
        midColor.setVisible(doubleGradient);
        posRangeLabel.setText(doubleGradient ? "Positive Range " : "Range");
    }

    private boolean updateValues() {

        try {
            double negStart = 0;
            double negEnd = 0;
            double posStart = Double.parseDouble(posRangeStart.getText());
            double posEnd = Double.parseDouble(posRangeEnd.getText());
            negStart = Double.parseDouble(negRangeStart.getText());
            negEnd = Double.parseDouble(negRangeEnd.getText());


            colorScheme = new ContinuousColorScale(
                    Math.max(negStart, negEnd),
                    Math.min(negStart, negEnd),
                    Math.min(posStart, posEnd),
                    Math.max(posStart, posEnd),
                    minColor.getSelectedColor(),
                    midColor.getSelectedColor(),
                    maxColor.getSelectedColor());

            return true;

        } catch (NumberFormatException numberFormatException) {
            JOptionPane.showMessageDialog(this, "Limit fields must be numeric.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        midColorLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        minColorLabel = new javax.swing.JLabel();
        minColor = new com.jidesoft.combobox.ColorComboBox();
        midColor = new com.jidesoft.combobox.ColorComboBox();
        maxColor = new com.jidesoft.combobox.ColorComboBox();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        negRangePanel = new javax.swing.JPanel();
        negRangeLabel = new javax.swing.JLabel();
        negRangeStart = new javax.swing.JTextField();
        negRangeToLabel = new javax.swing.JLabel();
        negRangeEnd = new javax.swing.JTextField();
        doubleGradientCheckbox = new javax.swing.JCheckBox();
        posRangePanel = new javax.swing.JPanel();
        posRangeLabel = new javax.swing.JLabel();
        posRangeStart = new javax.swing.JTextField();
        posRangeToLabel = new javax.swing.JLabel();
        posRangeEnd = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        midColorLabel.setText("Midpoint Color");

        jLabel3.setText("Maximum Color");

        minColorLabel.setText("Minimum Color");

        minColor.setColorValueVisible(false);

        midColor.setColorValueVisible(false);

        maxColor.setColorValueVisible(false);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jPanel1Layout.createSequentialGroup()
                                        .add(jLabel3)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 28, Short.MAX_VALUE)
                                        .add(maxColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(jPanel1Layout.createSequentialGroup()
                                        .add(minColorLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 31, Short.MAX_VALUE)
                                        .add(minColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                                .add(midColorLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 34, Short.MAX_VALUE)
                                .add(midColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jPanel1Layout.createSequentialGroup()
                                        .add(minColorLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(midColorLabel))
                                .add(minColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(jPanel1Layout.createSequentialGroup()
                                .add(39, 39, 39)
                                .add(midColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(12, 12, 12)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                                .add(jLabel3)
                                .add(20, 20, 20))
                        .add(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(maxColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        jPanel1Layout.linkSize(new java.awt.Component[]{midColor, midColorLabel}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jPanel1Layout.linkSize(new java.awt.Component[]{jLabel3, maxColor}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jPanel1Layout.linkSize(new java.awt.Component[]{minColor, minColorLabel}, org.jdesktop.layout.GroupLayout.VERTICAL);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        negRangeLabel.setText("Negative Range: ");

        negRangeStart.setText("-0.1");

        negRangeToLabel.setText("To:");

        negRangeEnd.setText("-1.5");
        negRangeEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                negRangeEndActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout negRangePanelLayout = new org.jdesktop.layout.GroupLayout(negRangePanel);
        negRangePanel.setLayout(negRangePanelLayout);
        negRangePanelLayout.setHorizontalGroup(
                negRangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(negRangePanelLayout.createSequentialGroup()
                        .add(negRangeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(negRangeStart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(negRangeToLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(negRangeEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        negRangePanelLayout.setVerticalGroup(
                negRangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(negRangePanelLayout.createSequentialGroup()
                        .add(negRangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(negRangeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(negRangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(negRangeStart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(negRangeToLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(negRangeEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(8, Short.MAX_VALUE))
        );

        doubleGradientCheckbox.setText("Use Double Gradient");
        doubleGradientCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doubleGradientCheckboxActionPerformed(evt);
            }
        });

        posRangeLabel.setText("Positive Range: ");

        posRangeStart.setText("-0.1");

        posRangeToLabel.setText("To:");

        posRangeEnd.setText("-1.5");
        posRangeEnd.setMaximumSize(new java.awt.Dimension(36, 22));
        posRangeEnd.setMinimumSize(new java.awt.Dimension(36, 22));

        org.jdesktop.layout.GroupLayout posRangePanelLayout = new org.jdesktop.layout.GroupLayout(posRangePanel);
        posRangePanel.setLayout(posRangePanelLayout);
        posRangePanelLayout.setHorizontalGroup(
                posRangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(posRangePanelLayout.createSequentialGroup()
                        .add(posRangeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(posRangeStart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(posRangeToLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(posRangeEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        posRangePanelLayout.setVerticalGroup(
                posRangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(posRangePanelLayout.createSequentialGroup()
                        .add(posRangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(posRangeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(posRangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(posRangeStart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(posRangeToLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(posRangeEnd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(8, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                                .add(35, 35, 35)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(doubleGradientCheckbox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 175, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(negRangePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, posRangePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap(88, Short.MAX_VALUE))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(132, Short.MAX_VALUE)
                        .add(okButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton)
                        .add(132, 132, 132))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                        .add(52, 52, 52)
                        .add(doubleGradientCheckbox)
                        .add(18, 18, 18)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(negRangePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(posRangePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(okButton)
                                .add(cancelButton))
                        .add(34, 34, 34))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public boolean isCanceled() {
        return canceled;
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        canceled = false;
        if (updateValues()) {
            setVisible(false);
        }
//GEN-LAST:event_okButtonActionPerformed
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed

        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void negRangeEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_negRangeEndActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_negRangeEndActionPerformed

    private void doubleGradientCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doubleGradientCheckboxActionPerformed
        initDoubleGradientState();
    }//GEN-LAST:event_doubleGradientCheckboxActionPerformed

    public ContinuousColorScale getColorScheme() {
        return colorScheme;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox doubleGradientCheckbox;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private com.jidesoft.combobox.ColorComboBox maxColor;
    private com.jidesoft.combobox.ColorComboBox midColor;
    private javax.swing.JLabel midColorLabel;
    private com.jidesoft.combobox.ColorComboBox minColor;
    private javax.swing.JLabel minColorLabel;
    private javax.swing.JTextField negRangeEnd;
    private javax.swing.JLabel negRangeLabel;
    private javax.swing.JPanel negRangePanel;
    private javax.swing.JTextField negRangeStart;
    private javax.swing.JLabel negRangeToLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField posRangeEnd;
    private javax.swing.JLabel posRangeLabel;
    private javax.swing.JPanel posRangePanel;
    private javax.swing.JTextField posRangeStart;
    private javax.swing.JLabel posRangeToLabel;
    // End of variables declaration//GEN-END:variables

}
