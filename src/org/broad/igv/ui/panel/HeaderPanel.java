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
 * TrackPanel.java
 *
 * Created on Sep 5, 2007, 4:09:39 PM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.broad.igv.ui.panel;


import com.jidesoft.swing.JideButton;
import org.broad.igv.feature.RegionOfInterest;
import org.broad.igv.track.TrackClickEvent;
import org.broad.igv.track.TrackMenuUtils;
import org.broad.igv.ui.IGVMainFrame;
import org.broad.igv.ui.util.SwitchingLabelUI;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;


/**
 * The drag & drop code was modified from the excellent example of Bryan E. Smith.
 *
 * @author jrobinso
 */
public class HeaderPanel extends JPanel implements Transferable {

    ReferenceFrame frame;
    private JLabel label;
    public static final Color BUTTON_BACKGROUND = new Color(230, 240, 250);
    static DataFlavor dragAndDropPanelDataFlavor;


    public HeaderPanel(ReferenceFrame frame) {
        this.frame = frame;
        init();
    }

    private void init() {


        setBackground(new java.awt.Color(255, 255, 255));
        setMinimumSize(new java.awt.Dimension(700, 0));
        setPreferredSize(new java.awt.Dimension(0, 0));
        setLayout(new java.awt.BorderLayout());

        if (FrameManager.isGeneListMode()) {

            JPanel geneListPanel = new JPanel();
            geneListPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            geneListPanel.setBackground(Color.white);
            geneListPanel.setMinimumSize(new java.awt.Dimension(700, 0));
            geneListPanel.setPreferredSize(new java.awt.Dimension(0, 0));
            geneListPanel.setLayout(new java.awt.BorderLayout());


            label = new JLabel(frame.name);
            label.setForeground(Color.blue);
            label.setUI(new SwitchingLabelUI(10));
            label.setToolTipText(frame.name);
            label.setPreferredSize(new Dimension(500, 80));

            //label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            //label.addMouseListener(mouseAdapter);

            final MouseAdapter nameMouseAdapater = new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() > 0) {
                        frame.reset();
                        IGVMainFrame.getInstance().doRefresh();
                    }
                }

                public void mousePressed(MouseEvent evt) {
                    if (evt.isPopupTrigger()) {
                        getPopupMenu(HeaderPanel.this, frame).show(HeaderPanel.this, evt.getX(), evt.getY());
                    }
                }

                @Override
                public void mouseReleased(MouseEvent evt) {

                    if (evt.isPopupTrigger()) {
                        getPopupMenu(HeaderPanel.this, frame).show(HeaderPanel.this, evt.getX(), evt.getY());
                    }

                }
            };
            label.addMouseListener(nameMouseAdapater);
            setSize(400, 100);
            setVisible(true);


            CytobandPanel cytobandPanel = new CytobandPanel(frame, false);
            cytobandPanel.setBackground(new java.awt.Color(255, 255, 255));
            cytobandPanel.setPreferredSize(new java.awt.Dimension(0, 20));
            cytobandPanel.setRequestFocusEnabled(false);
            cytobandPanel.setLayout(null);
            cytobandPanel.addMouseListener(nameMouseAdapater);

            JPanel topPanel = new JPanel();
            topPanel.setBackground(Color.gray);
            topPanel.setPreferredSize(new Dimension(500, 15));

            final MouseAdapter topMouseAdapter = new MouseAdapter() {

                boolean isDragging = false;
                Point mousePressPoint;

                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() > 1) {
                        IGVMainFrame.getInstance().setDefaultFrame(frame.name);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent mouseEvent) {
                    isDragging = false;
                }

                @Override()
                public void mouseDragged(MouseEvent e) {

                    if (isDragging) {
                        return;
                    }

                    isDragging = true;
                    JComponent c = HeaderPanel.this;
                    TransferHandler handler = c.getTransferHandler();
                    if (handler != null) {
                        handler.exportAsDrag(c, e, TransferHandler.MOVE);
                    }

                }
            };
            topPanel.addMouseListener(topMouseAdapter);
            topPanel.addMouseMotionListener(topMouseAdapter);


            geneListPanel.add(topPanel, BorderLayout.NORTH);

            geneListPanel.add(cytobandPanel, BorderLayout.CENTER);

            geneListPanel.add(label, BorderLayout.SOUTH);
            // geneListPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);

            add(geneListPanel);

            this.setTransferHandler(new DragAndDropTransferHandler());
            // Create the listener to do the work when dropping on this object!
            this.setDropTarget(new DropTarget(this, new HeaderDropTargetListener(this)));


        } else {

            JPanel panel = new JPanel();
            setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            panel.setBackground(new java.awt.Color(255, 255, 255));
            panel.setMinimumSize(new java.awt.Dimension(700, 0));
            panel.setPreferredSize(new java.awt.Dimension(0, 0));
            panel.setLayout(new java.awt.BorderLayout());

            CytobandPanel cytobandPanel = new CytobandPanel(frame);
            cytobandPanel.setBackground(new java.awt.Color(255, 255, 255));
            cytobandPanel.setPreferredSize(new java.awt.Dimension(0, 50));
            cytobandPanel.setRequestFocusEnabled(false);
            cytobandPanel.setLayout(null);
            panel.add(cytobandPanel, java.awt.BorderLayout.NORTH);

            RulerPanel rulerPanel = new RulerPanel(frame);
            rulerPanel.setBackground(new java.awt.Color(255, 255, 255));
            rulerPanel.setLayout(null);
            panel.add(rulerPanel, java.awt.BorderLayout.CENTER);

            RegionOfInterestPanel regionOfInterestPane = new RegionOfInterestPanel(frame);
            regionOfInterestPane.setBackground(new java.awt.Color(255, 255, 255));
            regionOfInterestPane.setMinimumSize(new java.awt.Dimension(0, 13));


            panel.add(regionOfInterestPane, java.awt.BorderLayout.SOUTH);

            add(panel);
        }


    }


    // TODO -- this is a partial copy of the RegionOfInterestPanel method.  Refactor to share


    protected static JPopupMenu getPopupMenu(final HeaderPanel parent, final ReferenceFrame frame) {

        int start = (int) frame.getOrigin();
        int end = (int) frame.getEnd();
        final RegionOfInterest roi = new RegionOfInterest(frame.getChrName(), start, end, "");

        JPopupMenu popupMenu = new RegionMenu(roi, frame);

        TrackMenuUtils.addZoomItems(popupMenu, frame);

        popupMenu.addSeparator();
        JMenuItem item = new JMenuItem("Remove");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.removeFrame(frame);
            }
        });
        popupMenu.add(item);


        return popupMenu;
    }

    private void removeFrame(ReferenceFrame frame) {
        FrameManager.removeFrame(frame);
        java.util.List<ReferenceFrame> remainingFrames = FrameManager.getFrames();
        if (remainingFrames.size() == 1) {
            IGVMainFrame.getInstance().setDefaultFrame(remainingFrames.get(0).name);
        } else {
            IGVMainFrame.getInstance().resetFrames();
        }
    }

    /**
     * <p>Returns (creating, if necessary) the DataFlavor representing RandomDragAndDropPanel</p>
     *
     * @return
     */
    public static DataFlavor getDragAndDropPanelDataFlavor() throws Exception {
        // Lazy load/create the flavor
        if (dragAndDropPanelDataFlavor == null) {
            dragAndDropPanelDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                    ";class=org.broad.igv.ui.panel.HeaderPanel");
        }

        return dragAndDropPanelDataFlavor;
    }


    //private static final Cursor droppableCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    //private static final Cursor notDroppableCursor = Cursor.getDefaultCursor();

    /**
     * <p>Listens for drops and performs the updates.</p>
     * <p>The real magic behind the drop!</p>
     */
    class HeaderDropTargetListener implements DropTargetListener {

        private final HeaderPanel rootPanel;

        /**
         * <p>Two cursors with which we are primarily interested while dragging:</p>
         * <ul>
         * <li>Cursor for droppable condition</li>
         * <li>Cursor for non-droppable consition</li>
         * </ul>
         * <p>After drop, we manually change the cursor back to default, though does this anyhow -- just to be complete.</p>
         */

        public HeaderDropTargetListener(HeaderPanel sheet) {
            this.rootPanel = sheet;
        }

        // Could easily find uses for these, like cursor changes, etc.

        public void dragEnter(DropTargetDragEvent dtde) {
        }

        public void dragOver(DropTargetDragEvent dtde) {
            // if (!this.rootPanel.getCursor().equals(droppableCursor)) {
            //     this.rootPanel.setCursor(droppableCursor);
            // }
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        public void dragExit(DropTargetEvent dte) {
            // this.rootPanel.setCursor(notDroppableCursor);
        }

        /**
         * <p>The user drops the item. Performs the drag and drop calculations and layout.</p>
         *
         * @param dtde
         */
        public void drop(DropTargetDropEvent dtde) {

            // Done with cursors, dropping
            //this.rootPanel.setCursor(Cursor.getDefaultCursor());

            // Just going to grab the expected DataFlavor to make sure
            // we know what is being dropped
            DataFlavor dragAndDropPanelFlavor = null;
            Object transferableObj = null;

            try {
                // Grab expected flavor
                dragAndDropPanelFlavor = HeaderPanel.getDragAndDropPanelDataFlavor();

                Transferable transferable = dtde.getTransferable();

                // What does the Transferable support
                if (transferable.isDataFlavorSupported(dragAndDropPanelFlavor)) {
                    transferableObj = dtde.getTransferable().getTransferData(dragAndDropPanelFlavor);
                }

            } catch (Exception ex) { /* nope, not the place */ }

            // If didn't find an item, bail
            if (transferableObj == null) {
                return;
            }

            // Cast it to the panel. By this point, we have verified it is a HeaderPanel
            HeaderPanel droppedPanel = (HeaderPanel) transferableObj;
            ReferenceFrame droppedFrame = droppedPanel.frame;
            if (droppedFrame == frame) {
                IGVMainFrame.getInstance().resetFrames();
            } else {
                final int dropXLoc = dtde.getLocation().x;
                boolean before = dropXLoc < getWidth() / 2;


                // Find the index for the drop
                java.util.List<ReferenceFrame> panels = FrameManager.getFrames();
                java.util.List<ReferenceFrame> orderedPanels = new ArrayList(panels.size());
                panels.remove(droppedFrame);

                boolean dropAdded = false;


                for (ReferenceFrame frame : panels) {
                    if (HeaderPanel.this.frame == frame) {
                        if (before) {
                            orderedPanels.add(droppedFrame);
                            orderedPanels.add(frame);
                        } else {
                            orderedPanels.add(frame);
                            orderedPanels.add(droppedFrame);
                        }
                        dropAdded = true;
                    } else {
                        orderedPanels.add(frame);
                    }
                }


                // Request relayout contents, or else won't update GUI following drop.
                // Will add back in the order to which we just sorted
                FrameManager.setFrames(orderedPanels);
                IGVMainFrame.getInstance().resetFrames();
            }
        }
    } // HeaderDropTargetListener

    /**
     * <p>One of three methods defined by the Transferable interface.</p>
     * <p>If multiple DataFlavor's are supported, can choose what Object to return.</p>
     * <p>In this case, we only support one: the actual JPanel.</p>
     * <p>Note we could easily support more than one. For example, if supports text and drops to a JTextField, could return the label's text or any arbitrary text.</p>
     *
     * @param flavor
     * @return
     */
    public Object getTransferData(DataFlavor flavor) {

        DataFlavor thisFlavor = null;

        try {
            thisFlavor = getDragAndDropPanelDataFlavor();
        } catch (Exception ex) {
            System.err.println("Problem lazy loading: " + ex.getMessage());
            ex.printStackTrace(System.err);
            return null;
        }

        // For now, assume wants this class... see loadDnD
        if (thisFlavor != null && flavor.equals(thisFlavor)) {
            return this;
        }

        return null;
    }

    /**
     * <p>One of three methods defined by the Transferable interface.</p>
     * <p>Returns supported DataFlavor. Again, we're only supporting this actual Object within the JVM.</p>
     * <p>For more information, see the JavaDoc for DataFlavor.</p>
     *
     * @return
     */
    public DataFlavor[] getTransferDataFlavors() {

        DataFlavor[] flavors = {null};
        try {
            flavors[0] = getDragAndDropPanelDataFlavor();
        } catch (Exception ex) {
            System.err.println("Problem lazy loading: " + ex.getMessage());
            ex.printStackTrace(System.err);
            return null;
        }

        return flavors;
    }

    /**
     * <p>One of three methods defined by the Transferable interface.</p>
     * <p>Determines whether this object supports the DataFlavor. In this case, only one is supported: for this object itself.</p>
     *
     * @param flavor
     * @return True if DataFlavor is supported, otherwise false.
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {

        DataFlavor[] flavors = {null};
        try {
            flavors[0] = getDragAndDropPanelDataFlavor();
        } catch (Exception ex) {
            System.err.println("Problem lazy loading: " + ex.getMessage());
            ex.printStackTrace(System.err);
            return false;
        }

        for (DataFlavor f : flavors) {
            if (f.equals(flavor)) {
                return true;
            }
        }

        return false;
    }

}

