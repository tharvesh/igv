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
package org.broad.igv.ui.util;

//~--- non-JDK imports --------------------------------------------------------

import org.broad.igv.ui.IGVMainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * @author eflakes
 */
public class UIUtilities {

    final private static StringBuffer scratchBuffer = new StringBuffer();

    /**
     * Method description
     *
     * @param selectedDirectory
     * @param selectedFile
     * @param fileFilters
     * @return
     */
    public static FileChooser getFileChooser(File selectedDirectory, File selectedFile,
                                             FileFilter[] fileFilters) {
        return getFileChooser(selectedDirectory, selectedFile, fileFilters, null);
    }

    /**
     * Method description
     *
     * @param selectedDirectory
     * @param selectedFile
     * @param fileFilters
     * @param dialogType
     * @return
     */
    public static FileChooser getFileChooser(File selectedDirectory, File selectedFile,
                                             FileFilter[] fileFilters, Integer dialogType) {

        final FileChooser fileChooser = new FileChooser(selectedDirectory) {

            boolean accepted = false;

            public void approveSelection() {
                accepted = true;
                super.approveSelection();
            }

            @Override
            public void cancelSelection() {
                setSelectedFile(null);
                super.cancelSelection();
            }

            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                JDialog dialog = super.createDialog(parent);
                dialog.setLocation(300, 200);
                dialog.setResizable(false);
                dialog.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        if (!accepted) {
                            setSelectedFile(null);
                        }
                    }
                });
                return dialog;
            }
        };
        fileChooser.setSelectedFile(selectedFile);
        if (dialogType != null) {
            fileChooser.setDialogType(dialogType);
        }


        // Setup FileFilters
        if (fileFilters != null) {
            for (FileFilter fileFilter : fileFilters) {
                fileChooser.addChoosableFileFilter(fileFilter);
            }
        }

        return fileChooser;
    }

    /**
     * Method description
     *
     * @param dialogTitle
     * @param defaultColor
     * @return
     */
    public static Color showColorChooserDialog(String dialogTitle, Color defaultColor) {

        Color color = null;
        JColorChooser chooser = new JColorChooser();
        chooser.setColor(defaultColor);
        while (true) {

            int response = JOptionPane.showConfirmDialog(IGVMainFrame.getInstance(), chooser,
                    dialogTitle, JOptionPane.OK_CANCEL_OPTION);

            if ((response == JOptionPane.CANCEL_OPTION) || (response == JOptionPane.CLOSED_OPTION)) {
                return null;
            }

            color = chooser.getColor();
            if (color == null) {
                continue;
            } else {
                break;
            }
        }
        return color;
    }

    /**
     * Method description
     *
     * @param parent
     * @param message
     * @return
     */
    public static boolean showConfirmationDialog(Component parent, String message) {

        int status = JOptionPane.showConfirmDialog(parent, message, null,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);

        if ((status == JOptionPane.CANCEL_OPTION) || (status == JOptionPane.CLOSED_OPTION)) {
            return false;
        }
        return true;
    }

    /**
     * Method description
     *
     * @param color
     * @return
     */
    public static String getcommaSeparatedRGBString(Color color) {

        if (color != null) {

            scratchBuffer.delete(0, scratchBuffer.length());    // Clear
            int red = color.getRed();
            int green = color.getGreen();
            int blue = color.getBlue();
            scratchBuffer.append(red);
            scratchBuffer.append(",");
            scratchBuffer.append(green);
            scratchBuffer.append(",");
            scratchBuffer.append(blue);
        }
        return scratchBuffer.toString();

    }

    /**
     * Method description
     *
     * @param window
     */
    public static void centerWindow(Window window) {

        Dimension dimension = window.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - dimension.width) / 2;
        int y = (screenSize.height - dimension.height) / 2;
        window.setLocation(x, y);
        window.requestFocus();
    }

    /**
     * A wrapper around invokeOnEventThread.  If the runnable is already in the event dispatching
     * queue it is just run.  Otherwise it is placed in the queue via invokeOnEventThread.
     * <p/>
     * I'm not sure this is strictly neccessary,  but is safe.
     *
     * @param runnable
     */
    public static void invokeOnEventThread(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }

    }
}
