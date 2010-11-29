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
package org.broad.igv.data;

//~--- non-JDK imports --------------------------------------------------------

import org.broad.igv.feature.genome.GenomeManager;
import org.broad.igv.util.collections.FloatArrayList;
import org.broad.igv.util.collections.IntArrayList;
import org.apache.log4j.Logger;
import org.broad.igv.Globals;
import org.broad.igv.event.StatusChangeEvent;
import org.broad.igv.exceptions.ParserException;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.listener.StatusListener;
import org.broad.igv.track.TrackType;
import org.broad.igv.track.WindowFunction;
import org.broad.igv.util.*;
import org.broad.tribble.readers.AsciiLineReader;
import org.broad.igv.util.IGVSeekableStreamFactory;
import org.broad.tribble.util.SeekableStream;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Class description
 *
 * @author Enter your name here...
 * @version Enter version here..., 08/11/11
 */
public class IGVDatasetParser {

    private static Logger log = Logger.getLogger(IGVDatasetParser.class);
    private ResourceLocator dataResourceLocator;
    private int chrColumn;
    private int startColumn;
    private int endColumn;
    private int firstDataColumn;
    private int probeColumn;
    private boolean hasEndLocations;
    private boolean hasCalls;
    private Genome genome;


    public static ArrayList<StatusListener> listeners = null;
    private int startBase = 0;

    /**
     * Constructs ...
     *
     * @param copyNoFile
     * @param genomeId
     */
    public IGVDatasetParser(ResourceLocator copyNoFile, String genomeId) {
        this.dataResourceLocator = copyNoFile;
        this.genome = GenomeManager.getInstance().getGenome(genomeId);
        initParameters();
    }

    private void initParameters() {
        String tmp = (dataResourceLocator.getPath().endsWith(".txt")
                ? dataResourceLocator.getPath().substring(0,
                dataResourceLocator.getPath().length() - 4) : dataResourceLocator.getPath()).toLowerCase();

        if (tmp.endsWith(".igv")) {
            chrColumn = 0;
            startColumn = 1;
            endColumn = 2;
            probeColumn = 3;
            firstDataColumn = 4;
            hasEndLocations = true;
            hasCalls = false;
        } else if (tmp.endsWith(".xcn") || tmp.endsWith("cn") || tmp.endsWith(".snp") || tmp.endsWith(".loh")) {
            probeColumn = 0;
            chrColumn = 1;
            startColumn = 2;
            endColumn = -1;
            firstDataColumn = 3;
            hasEndLocations = false;
            hasCalls = tmp.endsWith(".xcn") || tmp.endsWith(".snp");
        } else {
            throw new ParserException("Unknown file type: ", 0);
        }
    }

    /*
     */

    public static boolean parsableMAGE_TAB(ResourceLocator file) throws IOException {
        AsciiLineReader reader = null;
        try {
            reader = ParsingUtils.openAsciiReader(file);
            String nextLine = null;

            //skip first row
            reader.readLine();

            //check second row for MAGE_TAB identifiers
            if ((nextLine = reader.readLine()) != null && (nextLine.contains("Reporter REF") || nextLine.contains("Composite Element REF") || nextLine.contains("Term Source REF") || nextLine.contains("CompositeElement REF") || nextLine.contains("TermSource REF") || nextLine.contains("Coordinates REF"))) {
                int count = 0;
                // check if this mage_tab data matrix can be parsed by this class
                while ((nextLine = reader.readLine()) != null && count < 5) {
                    nextLine = nextLine.trim();
                    if (nextLine.startsWith("SNP_A") || nextLine.startsWith("CN_")) {
                        return true;
                    }

                    count++;
                }
                return false;
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return false;
    }

    /**
     * Scan the datafile for chromosome breaks.
     *
     * @param dataset
     * @return
     */
    public List<ChromosomeSummary> scan(IGVDataset dataset) {

        int estLineCount = ParsingUtils.estimateLineCount(dataResourceLocator.getPath());
        Map<String, Integer> longestFeatureMap = new HashMap();

        float dataMin = 0;
        float dataMax = 0;
        //long filePosition = 0;
        InputStream is = null;
        AsciiLineReader reader = null;
        String nextLine = null;
        ChromosomeSummary chrSummary = null;
        List<ChromosomeSummary> chrSummaries = new ArrayList();
        String[] headings = null;
        WholeGenomeData wgData = null;
        int nRows = 0;

        boolean logNormalized;
        try {

            int skipColumns = hasCalls ? 2 : 1;

            // BufferedReader reader = ParsingUtils.openBufferedReader(dataResourceLocator);
            is = ParsingUtils.openInputStream(dataResourceLocator);
            reader = new AsciiLineReader(is);

            // Infer datatype from extension.  This can be overriden in the
            // comment section
            if (isCopyNumberFileExt(dataResourceLocator.getPath())) {
                dataset.setTrackType(TrackType.COPY_NUMBER);
                dataset.getTrackProperties().setWindowingFunction(WindowFunction.mean);
            } else if (isLOHFileExt(dataResourceLocator.getPath())) {
                dataset.setTrackType(TrackType.LOH);
                dataset.getTrackProperties().setWindowingFunction(WindowFunction.mean);
            } else {
                dataset.getTrackProperties().setWindowingFunction(WindowFunction.mean);
            }

            // Parse comments, if any
            nextLine = reader.readLine();

            while (nextLine.startsWith("#") || (nextLine.trim().length() == 0)) {
                if (nextLine.length() > 0) {
                    parseComment(nextLine, dataset);
                }
                nextLine = reader.readLine();
            }

            // Parse column headings
            String[] data = nextLine.trim().split("\t");

            headings = getHeadings(data, skipColumns);

            dataset.setDataHeadings(headings);

            // Infer if the data is logNormalized by looking for negative data values.
            // Assume it is not until proven otherwise
            logNormalized = false;

            wgData = new WholeGenomeData(headings);

            int chrRowCount = 0;

            // Update
            int updateCount = 5000;
            int count = 0;
            long lastPosition = 0;
            while ((nextLine = reader.readLine()) != null) {

                if (++count % updateCount == 0) {
                    fireStatusEvent(new StatusChangeEvent("Loaded: " + count + " / " + estLineCount + " (est)"));
                }
                // Distance since last sample

                int nTokens = ParsingUtils.split(nextLine.trim(), tokens, '\t');
                if (nTokens > 0) {
                    String thisChr = genome.getChromosomeAlias(tokens[chrColumn]);
                    if (chrSummary == null || !thisChr.equals(chrSummary.getName())) {
                        // Update whole genome and previous chromosome summary, unless this is
                        // the first chromosome
                        if (chrSummary != null) {
                            updateWholeGenome(chrSummary.getName(), dataset, headings, wgData);
                            chrSummary.setNDataPoints(nRows);
                        }

                        // Shart the next chromosome
                        chrSummary = new ChromosomeSummary(thisChr, lastPosition);
                        chrSummaries.add(chrSummary);
                        nRows = 0;
                        wgData = new WholeGenomeData(headings);
                        chrRowCount = 0;

                    }
                    lastPosition = reader.getPosition();

                    int location = -1;
                    try {
                        location = Integer.parseInt(tokens[startColumn]) - startBase;

                    } catch (NumberFormatException numberFormatException) {
                        log.error("Column " + tokens[startColumn] + " is not a number");
                        throw new ParserException("Column " + (startColumn + 1) +
                                " must contain a numeric value." + " Found: " + tokens[startColumn],
                                reader.getCurrentLineNumber(), nextLine);
                    }

                    int length = 1;
                    if (hasEndLocations) {
                        try {
                            length = Integer.parseInt(tokens[endColumn].trim()) - location + 1;

                        } catch (NumberFormatException numberFormatException) {
                            log.error("Column " + tokens[endColumn] + " is not a number");
                            throw new ParserException("Column " + (endColumn + 1) +
                                    " must contain a numeric value." + " Found: " + tokens[endColumn],
                                    reader.getCurrentLineNumber(), nextLine);
                        }
                    }

                    updateLongestFeature(longestFeatureMap, thisChr, length);

                    if (wgData.locations.size() > 0 && wgData.locations.get(wgData.locations.size() - 1) > location) {
                        throw new ParserException("File is not sorted, .igv and .cn files must be sorted by start position." +
                                " Use igvtools (File > Run igvtools..) to sort the file.", reader.getCurrentLineNumber());
                    }


                    if (nTokens > headings.length * skipColumns + firstDataColumn) {

                        // TODO -- throw error here.  this will cause an index out of bounds exception
                        log.info("Unexpected number of columns.  Expected " + headings.length + firstDataColumn +
                                ". Found " + nTokens + "   (" + nextLine + ")");
                    }

                    wgData.locations.add(location);

                    for (int idx = 0; idx < headings.length; idx++) {
                        int i = firstDataColumn + idx * skipColumns;

                        float copyNo = i < tokens.length ? readFloat(tokens[i]) : Float.NaN;

                        if (!Float.isNaN(copyNo)) {
                            dataMin = Math.min(dataMin, copyNo);
                            dataMax = Math.max(dataMax, copyNo);
                        }
                        if (copyNo < 0) {
                            logNormalized = true;
                        }
                        String heading = headings[idx];
                        wgData.data.get(heading).add(copyNo);
                    }

                    nRows++;

                }
                chrRowCount++;
            }

            dataset.setLongestFeatureMap(longestFeatureMap);

        }
        catch (ParserException pe) {
            throw pe;
        }
        catch (FileNotFoundException e) {
            // DialogUtils.showError("SNP file not found: " + dataSource.getCopyNoFile());
            log.error("File not found: " + dataResourceLocator);
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            log.error("Exception when loading: " + dataResourceLocator.getPath(), e);
            if (nextLine != null && reader.getCurrentLineNumber() != 0) {
                throw new ParserException(e.getMessage(), e, reader.getCurrentLineNumber(), nextLine);
            } else {
                throw new RuntimeException(e);
            }
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("Error closing IGVDataset stream", e);
                }
            }
        }

        // Update last chromosome
        if (chrSummary != null) {
            updateWholeGenome(chrSummary.getName(), dataset, headings, wgData);
            chrSummary.setNDataPoints(nRows);
        }

        dataset.setLogNormalized(logNormalized);
        dataset.setDataMin(dataMin);
        dataset.setDataMax(dataMax);

        return chrSummaries;
    }

    private void updateLongestFeature(Map<String, Integer> longestFeatureMap, String thisChr, int length) {
        if (longestFeatureMap.containsKey(thisChr)) {
            longestFeatureMap.put(thisChr, Math.max(longestFeatureMap.get(thisChr), length));
        } else {
            longestFeatureMap.put(thisChr, length);
        }
    }

    private float readFloat(String token) {
        float copyNo = Float.NaN;
        try {
            if (token != null) {
                copyNo = Float.parseFloat(token);
            }

        }
        catch (NumberFormatException e) {
            // This is an expected condition.
        }
        return copyNo;

    }

    /**
     * Load data for a single chromosome.
     *
     * @param chrSummary
     * @param columnHeaders
     * @return
     */
    public ChromosomeData loadChromosomeData(
            ChromosomeSummary chrSummary, String[] columnHeaders) {

        // InputStream is = null;
        try {
            int skipColumns = hasCalls ? 2 : 1;

            // Get an estimate of the number of snps (rows).  THIS IS ONLY AN ESTIMATE
            int nRowsEst = chrSummary.getNDataPts();

            SeekableStream is = IGVSeekableStreamFactory.getStreamFor(dataResourceLocator.getPath());
            is.seek(chrSummary.getStartPosition());
            AsciiLineReader reader = new AsciiLineReader(is);


            // Create containers to hold data
            IntArrayList startLocations = new IntArrayList(nRowsEst);
            IntArrayList endLocations = (hasEndLocations ? new IntArrayList(nRowsEst) : null);
            List<String> probes = new ArrayList(nRowsEst);

            Map<String, FloatArrayList> dataMap = new HashMap();
            for (String h : columnHeaders) {
                dataMap.put(h, new FloatArrayList(nRowsEst));
            }

            // Begin loop through rows
            String chromosome = chrSummary.getName();
            boolean chromosomeStarted = false;
            String nextLine = reader.readLine();
            while ((nextLine != null) && (nextLine.trim().length() > 0)) {

                try {
                    int nTokens = ParsingUtils.split(nextLine, tokens, '\t');

                    String thisChromosome = genome.getChromosomeAlias(tokens[chrColumn].trim());
                    if (thisChromosome.equals(chromosome)) {
                        chromosomeStarted = true;

                        // chromosomeData.setMarkerId(nRows, tokens[0]);

                        // The probe.  A new string is created to prevent holding on to the entire row through a substring reference
                        String probe = new String(tokens[probeColumn]);
                        probes.add(probe);

                        int start = Integer.parseInt(tokens[startColumn].trim()) - startBase;
                        if (hasEndLocations) {
                            endLocations.add(Integer.parseInt(tokens[endColumn].trim()));
                        }

                        startLocations.add(start);

                        for (int idx = 0; idx < columnHeaders.length; idx++) {
                            int i = firstDataColumn + idx * skipColumns;
                            float copyNo = i < tokens.length ? readFloat(tokens[i]) : Float.NaN;
                            String heading = columnHeaders[idx];
                            dataMap.get(heading).add(copyNo);
                        }


                    } else if (chromosomeStarted) {
                        break;
                    }

                } catch (NumberFormatException numberFormatException) {

                    // Skip line
                    log.info("Skipping line (NumberFormatException) " + nextLine);
                }

                nextLine = reader.readLine();
            }

            // Loop complete
            ChromosomeData cd = new ChromosomeData(chrSummary.getName());
            cd.setProbes(probes.toArray(new String[]{}));
            cd.setStartLocations(startLocations.toArray());
            if (hasEndLocations) {
                cd.setEndLocations(endLocations.toArray());
            }

            for (String h : columnHeaders) {
                cd.setData(h, dataMap.get(h).toArray());
            }

            return cd;

        } catch (IOException ex) {
            log.error("Error parsing cn file", ex);
            throw new RuntimeException("Error parsing cn file", ex);
        }

    }

    /**
     * Note:  This is an exact copy of the method in GCTDatasetParser.  Refactor to merge these
     * two parsers, or share a common base class.
     *
     * @param comment
     * @param dataset
     */
    private void parseComment(String comment, IGVDataset dataset) {

        String tmp = comment.substring(1, comment.length());
        if (tmp.startsWith("track")) {
            ParsingUtils.parseTrackLine(tmp, dataset.getTrackProperties());

        } else {
            String[] tokens = tmp.split("=");
            if (tokens.length != 2) {
                return;
            }

            String key = tokens[0].trim().toLowerCase();
            if (key.equals("name")) {
                dataset.setName(tokens[1].trim());
            } else if (key.equals("type")) {

                try {
                    dataset.setTrackType(TrackType.valueOf(tokens[1].trim().toUpperCase()));
                } catch (Exception exception) {

                    // Ignore
                }
            } else if (key.equals("coords")) {

                startBase = Integer.parseInt(tokens[1].trim());

            }
        }
    }

    private boolean isCopyNumberFileExt(String filename) {
        String tmp = ((filename.endsWith(".txt") || filename.endsWith(".tab") || filename.endsWith(".xls")
                ? filename.substring(0, filename.length() - 4) : filename)).toLowerCase();
        return tmp.endsWith(".cn") || tmp.endsWith(".xcn") || tmp.endsWith(".snp");
    }

    private boolean isLOHFileExt(String filename) {
        String tmp = (filename.endsWith(".txt") || filename.endsWith(".tab") || filename.endsWith(".xls")
                ? filename.substring(0, filename.length() - 4) : filename);
        return tmp.endsWith(".loh");
    }

    /**
     * Return the sample headings for the copy number file.
     *
     * @param tokens
     * @param skipColumns
     * @return
     */
    public String[] getHeadings(String[] tokens, int skipColumns) {
        return getHeadings(tokens, skipColumns, false);
    }

    /**
     * Return the sample headings for the copy number file.
     *
     * @param tokens
     * @param skipColumns
     * @param removeDuplicates , whether to remove any duplicate headings
     * @return
     */
    public String[] getHeadings(String[] tokens, int skipColumns, boolean removeDuplicates) {

        ArrayList headings = new ArrayList();
        String previousHeading = null;
        for (int i = firstDataColumn; i < tokens.length; i += skipColumns) {
            if (removeDuplicates) {
                if (previousHeading != null && tokens[i].equals(previousHeading) || tokens[i].equals("")) {
                    continue;
                }

                previousHeading = tokens[i];
            }

            headings.add(tokens[i].trim());
        }

        return (String[]) headings.toArray(new String[0]);
    }

    static String[] tokens = new String[10000];


    private void updateWholeGenome(String currentChromosome, IGVDataset dataset, String[] headings,
                                   IGVDatasetParser.WholeGenomeData wgData) {


        if (!genome.getHomeChromosome().equals(Globals.CHR_ALL)) {
            return;
        }

        // Update whole genome data
        int[] locations = wgData.locations.toArray();
        if (locations.length > 0) {
            Map<String, float[]> tmp = new HashMap(wgData.data.size());
            for (String s : wgData.headings) {
                tmp.put(s, wgData.data.get(s).toArray());
            }


            GenomeSummaryData genomeSummary = dataset.getGenomeSummary();
            if (genomeSummary == null) {
                genomeSummary = new GenomeSummaryData(genome, headings);
                dataset.setGenomeSummary(genomeSummary);
            }
            genomeSummary.addData(currentChromosome, locations, tmp);

        }
    }

    class WholeGenomeData {

        String[] headings;
        IntArrayList locations = new IntArrayList(50000);
        Map<String, FloatArrayList> data = new HashMap();

        WholeGenomeData(String[] headings) {
            this.headings = headings;
            for (String h : headings) {
                data.put(h, new FloatArrayList(50000));
            }
        }

        int size() {
            return locations.size();
        }
    }

    public static void addListener(StatusListener s) {
        if (listeners == null) {
            listeners = new ArrayList();
        }

        listeners.add(s);
    }

    public void fireStatusEvent(AWTEvent evt) {
        if (evt instanceof StatusChangeEvent) {
            if (listeners != null) {
                for (StatusListener listener : listeners) {
                    listener.statusChanged((StatusChangeEvent) evt);
                }
            }
        }
    }
}
