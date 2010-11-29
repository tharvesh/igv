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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.ui.panel;

import org.broad.igv.feature.genome.GenomeManager;
import org.broad.igv.feature.RegionOfInterest;
import org.broad.igv.feature.SequenceManager;
import org.broad.igv.ui.IGVMainFrame;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.util.LongRunningTask;
import org.broad.igv.util.NamedRunnable;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.Collection;

/**
 * @author eflakes
 */
public class RegionOfInterestPanel extends JPanel {

    PopupMenu popup;

    ReferenceFrame frame;

    // There can only be 1 selected region, irrespective of the number of panels
    private static RegionOfInterest selectedRegion = null;

    public RegionOfInterestPanel(ReferenceFrame frame) {

        setToolTipText("Regions of Interest");
        this.frame = frame;
        MouseInputAdapter ma = new ROIMouseAdapater();
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }


    @Override
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);

        // Draw regions of interest?
        drawRegionsOfInterest((Graphics2D) g, getHeight());

        g.setColor(Color.BLACK);
        g.drawRect(0, 0, getWidth(), getHeight());
    }

    Collection<RegionOfInterest> getRegions() {
        return IGVMainFrame.getInstance().getSession().getRegionsOfInterest(frame.getChrName());
    }

    public void drawRegionsOfInterest(final Graphics2D g, int height) {

        ReferenceFrame referenceFrame = frame;
        Collection<RegionOfInterest> regions = getRegions();

        if (FrameManager.isGeneListMode()) {
            g.setColor(RegionOfInterest.getBackgroundColor());
            g.fillRect(0, 0, getWidth(), height);
        }

        if (regions == null || regions.isEmpty()) {
            return;
        }


        for (RegionOfInterest regionOfInterest : regions) {

            Integer regionStart = regionOfInterest.getStart();
            if (regionStart == null) {
                // skip - null starts are bad regions of interest
                continue;
            }

            Integer regionEnd = regionOfInterest.getEnd();
            if (regionEnd == null) {
                regionEnd = regionStart;
            }
            int start = referenceFrame.getScreenPosition(regionStart);
            int end = referenceFrame.getScreenPosition(regionEnd);
            int regionWidth = Math.max(1, end - start);

            g.setColor(regionOfInterest.getBackgroundColor());
            g.fillRect(start, 0, regionWidth, height);

        }
    }


    RegionOfInterest getRegionOfInterest(int px) {

        double pos = frame.getChromosomePosition(px);

        Collection<RegionOfInterest> roiList = getRegions();
        if (roiList != null) {
            for (RegionOfInterest roi : roiList) {
                if (pos > roi.getStart() && pos < roi.getEnd()) {
                    return roi;
                }
            }
        }

        return null;

    }

    protected static JPopupMenu getPopupMenu(final Component parent, final RegionOfInterest roi, final ReferenceFrame frame) {

        //Set<TrackType> loadedTypes = IGVMainFrame.getInstance().getTrackManager().getLoadedTypes();

        JPopupMenu popupMenu = new RegionMenu(roi, frame);

        popupMenu.addSeparator();

        JMenuItem item = new JMenuItem("Zoom");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                frame.jumpTo(roi.getChr(), roi.getStart(), roi.getEnd());

                String locusString = roi.getLocusString();
                IGVMainFrame.getInstance().getSession().getHistory().push(locusString);

            }
        });
        popupMenu.add(item);

        item = new JMenuItem("Edit description...");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {


                String desc = JOptionPane.showInputDialog(parent, "Add or edit region description:", roi.getDescription());
                roi.setDescription(desc);

            }
        });
        popupMenu.add(item);

        item = new JMenuItem("Copy sequence");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                LongRunningTask.submit(new NamedRunnable() {
                    public String getName() {
                        return "Copy sequence";
                    }

                    public void run() {
                        String genomeId = GenomeManager.getInstance().getGenomeId();
                        byte[] seqBytes = SequenceManager.readSequence(genomeId, roi.getChr(), roi.getStart(), roi.getEnd());
                        if (seqBytes == null) {
                            MessageUtils.showMessage("Sequence not available");
                        } else {
                            String sequence = new String(seqBytes);
                            StringSelection stringSelection = new StringSelection(sequence);
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            clipboard.setContents(stringSelection, null);
                        }
                    }
                });


            }
        });

        // Disable copySequence if region exceeds a MB
        if (roi.getEnd() - roi.getStart() > 1000000) {
            item.setEnabled(false);
        }
        popupMenu.add(item);


        popupMenu.add(new JSeparator());

        item = new JMenuItem("Delete");
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                IGVMainFrame.getInstance().getSession().getRegionsOfInterest(frame.getChrName()).remove(roi);
                IGVMainFrame.getInstance().repaintDataAndHeaderPanels();
            }
        });
        popupMenu.add(item);

        return popupMenu;
    }


    public static RegionOfInterest getSelectedRegion() {
        return selectedRegion;
    }

    public static void setSelectedRegion(RegionOfInterest region) {
        selectedRegion = region;
    }

    class ROIMouseAdapater extends MouseInputAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }


        @Override
        public void mouseReleased(MouseEvent e) {
            //showPopup(e);
        }


        @Override
        public void mouseMoved(MouseEvent e) {
            RegionOfInterest roi = getRegionOfInterest(e.getX());
            if (roi != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setToolTipText(roi.getTooltip());
                if (selectedRegion != roi) {
                    selectedRegion = roi;
                    IGVMainFrame.getInstance().repaintDataPanels();
                }

            } else {
                if (selectedRegion != null) {
                    selectedRegion = null;
                    IGVMainFrame.getInstance().repaintDataPanels();
                }
                setToolTipText("");
                setCursor(Cursor.getDefaultCursor());
            }
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
            if (selectedRegion != null) {
                selectedRegion = null;
                IGVMainFrame.getInstance().repaintDataPanels();
            }
        }

        private void showPopup(MouseEvent e) {

            RegionOfInterest roi = getRegionOfInterest(e.getX());
            if (roi != null) {

                getPopupMenu(RegionOfInterestPanel.this, roi, frame).show(e.getComponent(), e.getX(), e.getY());
            }

        }
    }
}
