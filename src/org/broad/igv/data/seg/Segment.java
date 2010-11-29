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

package org.broad.igv.data.seg;

import org.broad.igv.feature.LocusScore;
import org.broad.igv.track.WindowFunction;

/**
 * @author Enter your name here...
 * @version Enter version here..., 09/01/09
 */
public class Segment implements LocusScore {

    private int extendedStart = -1;
    private int extendedEnd = -1;
    private int start;
    private int end;
    private float score;
    private int snpCount;

    public Segment(int start, int end, float score) {
        this(start, end, score, 0);
    }

    public Segment(int start, int end, float score, int snpCount) {
        this.start = start;
        this.end = end;
        if (extendedStart < 0) {
            extendedStart = start;
        }
        if (extendedEnd < 0) {
            extendedEnd = end;
        }
        this.score = score;
        this.snpCount = snpCount;
    }


    //Segment(int start, int end,  float score, int snpCount) {
    //    this(start, start, end, end, score, snpCount);
    //}
    public Segment(int start, int origStart, int end, int origEnd, float score, int snpCount) {
        this.start = start;
        this.end = end;
        this.extendedStart = origStart;
        this.extendedEnd = origEnd;
        this.score = score;
        this.snpCount = snpCount;
    }

    /**
     * Method description
     *
     * @return
     */
    public Segment copy() {
        return new Segment(start, extendedStart, end, extendedEnd, score, snpCount);
    }

    public String getChr() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Method description
     *
     * @return
     */
    public int getStart() {
        return start;
    }

    /**
     * Method description
     *
     * @return
     */
    public int getEnd() {
        return end;
    }

    /**
     * Method description
     *
     * @return
     */
    public float getScore() {
        return score;
    }

    /**
     * Method description
     *
     * @param start
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Method description
     *
     * @param end
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * Method description
     *
     * @param confidence
     */
    public void setConfidence(float confidence) {

        // Ignored for now
    }

    /**
     * Method description
     *
     * @return
     */
    public float getConfidence() {
        return 1.0f;
    }

    /**
     * Method description
     *
     * @param position
     * @param ignored
     * @return
     */
    public String getValueString(double position, WindowFunction ignored) {
        String valueString = "Copy number: " + getScore();
        if (snpCount > 0) {
            valueString += " (" + snpCount + " markers)";
        }
        return valueString;
    }

    /**
     * Method description
     *
     * @param inc
     */
    public void incremenetSnpCount(int inc) {
        snpCount += inc;
    }

    /**
     * Method description
     *
     * @return
     */
    public int getSnpCount() {
        return snpCount;
    }

    /**
     * @return the extendedStart
     */
    public int getExtendedStart() {
        return extendedStart;
    }

    /**
     * @return the extendedEnd
     */
    public int getExtendedEnd() {
        return extendedEnd;
    }

    /**
     * @param extendedStart the extendedStart to set
     */
    public void setExtendedStart(int extendedStart) {
        this.extendedStart = extendedStart;
    }

    /**
     * @param extendedEnd the extendedEnd to set
     */
    public void setExtendedEnd(int extendedEnd) {
        this.extendedEnd = extendedEnd;
    }
}
