package cytospade;
/**
 * fcsFile.java
 * ---
 * <p>Contains the code to read fcs files.</p>
 *
 * <p>Based on the code by Jonathan Irish.</p>
 *
 *
 * Cytobank (TM) is server and client software for web-based management, analysis,
 * and sharing of flow cytometry data.
 *
 * Copyright (C) 2009 Cytobank, Inc.  All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Cytobank, Inc.
 * 659 Oak Grove Avenue #205
 * Menlo Park, CA 94025
 *
 * http://www.cytobank.org
 */
import cytoscape.logger.CyLogger;
import java.io.*;
import java.util.*;

// Use the new I/O for speed
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;

/**
 * fcsFile ---
 * <p>
 * A class to read FCS files based on fastFacsClasses.cs.
 * </p>
 *
 * <p>
 * The class is final and does not implement the
 * <code>java.io.Serializable</code> interface. This is because it should not be
 * subclassed and is highly file-dependent, respectively.
 * </p>
 */
public final class fcsFile {
    // The ENCODING to use for decoding the text data in the file
    // ISO-8859-1 is the standard extension of ASCII

    private static final String ENCODING = "ISO-8859-1";
    private static final Charset charset = Charset.forName(ENCODING);
    // Size of the version string in bytes
    private static final int VERSION_SIZE = 6;
    // Default prefix for FCS files to check whether the file is a FCS file.
    private static final String FCS_PREFIX = "FCS";
    // Default behavior for whether to extract events
    private static final boolean EXTRACTP = false;
    /**
     * Decoder for parsing the text portions
     */
    private CharsetDecoder decoder;
    /**
     * The underlying file
     */
    private File file;
    /**
     * Boolean flag of whether the file is an FCS file.
     */
    private boolean isFCSP;
    /**
     * File Information
     */
    public String version = null; // Version string
    public int textStart = 0;
    public int textEnd = 0;
    public int dataStart = 0;
    public int dataEnd = 0;
    public int analysisStart = 0;
    public int analysisEnd = 0;
    public int supplementalStart = 0;
    public int supplementalEnd = 0;
    public char delimiter = '\\'; // TEXT segment delimiter character
    public String text = null; // The entire TEXT segment
    /**
     * settings ---
     * <p>
     * <code>java.util.Properties</code> object settings contains all the
     * key/value pairs in the TEXT segment.
     * </p>
     *
     * <p>
     * This is a good way to handle all the pairs for hardcore Java people.
     * </p>
     */
    private Properties settings = null;
    /**
     * All the public fields ---
     * <p>
     * These should be self-explanatory.
     * </p>
     *
     * <p>
     * They are initialized since they may never be set if the file is not an
     * FCS file.
     * </p>
     *
     * <p>
     * At some point, we probably want to wean people away from these and use
     * the Properties settings object instead.
     * </p>
     */
    public boolean littleEndianP;
    public int parameters = 0;
    public String sampleName = null;
    public String dataType = null;
    public String cytometer = null;
    public String mode = null;
    public String instrument = null;
    public String expTime = null;
    public String expFile = null;
    public String operatorName = null;
    public String operatingSystem = null;
    public String creatorSoftware = null;
    public String cytometerNumber = null;
    public String experimentDate = null;
    public String experimentName = null;
    public String exportTime = null;
    public String exportUser = null;
    public String GUID = null;
    public String windowExtension = null;
    public String threshold = null;
    public String tubeName = null;
    public String wellId = null;
    public String plateId = null;
    public String plateName = null;
    public String comment = null;
    public String spillString = null;
    public boolean applyCompensation = false;
    public String source = null;
    public String nextData = null;
    public String endsText = null;
    public String bTime = null;
    public double timeStep = 0;
    public boolean[] isLog = null;
    public int lasers = 0;
    public String[] laserASF = null;
    public String[] laserName = null;
    public String[] laserDelay = null;
    public String[] channelName = null;
    public String[] channelShortname = null;
    public String[] channelGain = null;
    public int[] channelBits = null;
    public String[] channelAmp = null;
    public double[] channelRange = null;
    public String[] channelVoltage = null;
    public boolean[] displayLog = null;
    public double[] ampValue = null;
    public int totalEvents = 0;
    protected double[][] eventList = null;

    /**
     * Constructor ---
     * <p>
     * Given the path to a file, the class grabs all the information about the
     * file. Whether events are extracted is determined by the default value in
     * EXTRACTP.
     * </p>
     *
     * <p>
     * Throws <code>FileNotFoundException</code> and <code>IOException</code>.
     * This way, whatever code is calling the class can handle the exception
     * properly.
     * </p>
     *
     * @param path
     *            path to the underlying file.
     * @throws <code>java.io.FileNotFoundException</code> if the file is not
     *         found.
     * @throws <code>java.io.IOException</code> if an IO exception occurred.
     */
    public fcsFile(String path) throws FileNotFoundException, IOException {
        this(path, EXTRACTP);
    }

    /**
     * Constructor ---
     * <p>
     * Given the path to a file, the class grabs all the information about the
     * file. The flag extractEventsP controls whether to extract the data from
     * the file.
     * </p>
     *
     * <p>
     * Set extractEventsP to false to improve speed.
     * </p>
     *
     * <p>
     * Throws <code>FileNotFoundException</code> and <code>IOException</code>.
     * This way, whatever code is calling the class can handle the exception
     * properly.
     * </p>
     *
     * @param path
     *            path to the underlying file.
     * @param extractEventsP
     *            boolean flag for whether to extract events in the underlying
     *            file.
     * @throws <code>java.io.FileNotFoundException</code> if the file is not
     *         found.
     * @throws <code>java.io.IOException</code> if an IO exception occurred.
     */
    public fcsFile(String path, boolean extractEventsP) throws FileNotFoundException, IOException {
        // Create a file using the path and use the other constructor that takes
        // a File.
        this(new File(path), extractEventsP);
    }

    /**
     * Constructor ---
     * <p>
     * Given a File f, the class grabs all the information about the file.
     * Whether events are extracted is determined by the default value in
     * EXTRACTP.
     * </p>
     *
     * <p>
     * Throws <code>FileNotFoundException</code> and <code>IOException</code>.
     * This way, whatever code is calling the class can handle the exception
     * properly.
     * </p>
     *
     * @param file
     *            <code>File</code> object pointing to the underlying file.
     * @throws <code>java.io.FileNotFoundException</code> if the file is not
     *         found.
     * @throws <code>java.io.IOException</code> if an IO exception occurred.
     */
    public fcsFile(File file) throws FileNotFoundException, IOException {
        this(file, EXTRACTP);
    }

    /**
     * Constructor ---
     * <p>
     * Given a File f, the class grabs all the information about the file. The
     * flag extractEventsP controls whether to extract the data from the file.
     * </p>
     *
     * <p>
     * Set extractEventsP to false to improve speed.
     * </p>
     *
     * <p>
     * Throws <code>FileNotFoundException</code> and <code>IOException</code>.
     * This way, whatever code is calling the class can handle the exception
     * properly.
     * </p>
     *
     * @param file
     *            <code>File</code> object pointing to the underlying file.
     * @param extractEventsP
     *            boolean flag for whether to extract events in the underlying
     *            file.
     * @throws <code>java.io.FileNotFoundException</code> if the file is not
     *         found.
     * @throws <code>java.io.IOException</code> if an IO exception occurred.
     */
    public fcsFile(File file, boolean extractEventsP) throws FileNotFoundException, IOException {
        this.file = file;

        // Set isFCSP to false - start by assuming the file is not an FCS file
        isFCSP = false;

        // Read the file and initialize all the fields
        readFile(extractEventsP);
    }

    /**
     * readFile ---
     * <p>
     * A helper function to read all the fields in the TEXT segment of the FCS
     * file.
     * </p>
     *
     * <p>
     * This helper function should only be called once by the constructor as it
     * is quite expensive.
     * </p>
     *
     * @param extractEventsP
     *            boolean flag indicating whether to extract events in the
     *            underlying file.
     * @throws <code>java.io.FileNotFoundException</code> if the file is not
     *         found.
     * @throws <code>java.io.IOException</code> if an IO exception occurred.
     */
    private void readFile(boolean extractEventsP) throws FileNotFoundException, IOException {
        // Open a file input stream to the file
        FileInputStream fis = new FileInputStream(file);

        // Create a byte array to hold the version
        byte[] versionArray = new byte[VERSION_SIZE];

        // Read the version into the byte array
        int numRead = fis.read(versionArray);

        if (numRead < VERSION_SIZE) {
            // If the number of bytes read is less than the number of bytes in
            // the version string, then the file is too small to be an FCS file.
            isFCSP = false;

            // Close the file input stream
            fis.close();

            // Quit
            return;
        }

        // Decode the version using the default encoding
        version = new String(versionArray);

        // Determine whether the file is an FCS file by whether the version
        // string starts with the FCS_PREFIX
        isFCSP = version.startsWith(FCS_PREFIX);

        if (!isFCSP) {
            // If the file is not an FCS file, then close the file and quit.
            // Close the file input stream
            fis.close();

            // Quit
            return;
        }

        /**
         * At this point, we are pretty sure that the file is an FCS file. So,
         * we parse it.
         */
        /**
         * Get the standard HEADER stuff
         */
        // Skip 4 bytes to get to byte 10
        fis.skip(4);

        // Create a byte array to hold the HEADER
        byte[] headerArray = new byte[48];

        // Read the header into the byte array
        numRead = fis.read(headerArray);

        if (numRead < 48) {
            // If the number of bytes read is less than 48, then the file is too
            // small to be an FCS file.
            isFCSP = false;

            // Close the file input stream
            fis.close();

            // Quit
            return;
        }

        try {
            // Try to parse the TEXT segment start and end and DATA segment
            // start and end
            textStart = Integer.parseInt((new String(headerArray, 0, 8)).trim());
            textEnd = Integer.parseInt((new String(headerArray, 8, 8)).trim());
            dataStart = Integer.parseInt((new String(headerArray, 16, 8)).trim());
            dataEnd = Integer.parseInt((new String(headerArray, 24, 8)).trim());
        } catch (NumberFormatException nfe) {
            // If a NumberFormatException occured, then quit because there's
            // nothing we can do without the TEXT or DATA segment.
            // Close the file input stream
            fis.close();

            return;
        }

        /**
         * Get the ANALYSIS segment limits
         */
        try {
            // Try to parse the analysisStart and analysisEnd
            analysisStart = Integer.parseInt((new String(headerArray, 32, 8)).trim());
            analysisEnd = Integer.parseInt((new String(headerArray, 40, 8)).trim());
        } catch (NumberFormatException nfe) {
            // If a NumberFormatException occured, then set the ANALYSIS start
            // and end to 0 since this segment is optional.
            analysisStart = 0;
            analysisEnd = 0;
        }

        /**
         * Use NIO to read the OTHER and TEXT segments
         */
        // Get the channel for the input file
        FileChannel fc = fis.getChannel();

        // Move the channel's position back to 0
        fc.position(0);

        // Map the TEXT segment to memory
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, textEnd + 1);

        /**
         * Create the character decoder for parsing characters
         */
        decoder = charset.newDecoder();

        /**
         * Get the OTHER segment
         */
        mbb.limit(textStart);
        mbb.position(58);
        CharBuffer other = decoder.decode(mbb.slice());

        /**
         * Get the TEXT segment
         */
        mbb.limit(textEnd + 1);
        mbb.position(textStart);
        text = decoder.decode(mbb.slice()).toString();

        /**
         * Close the file since we have the string version of the TEXT segment
         */
        // Close the file channel
        fc.close();

        // Close the file input stream
        fis.close();

        /**
         * Decode the TEXT segment
         */
        // The first character of the primary TEXT segment contains the
        // delimiter character
        delimiter = text.charAt(0);

        /**
         * Key/Value Pairs
         */
        // Generate all the pairs
        String[] pairs;

        if (delimiter == '\\') {
            // If the delimiter character is a backslash, then we have to escape
            // it in the regular expression.
            pairs = text.split("[\\\\]");
        } else {
            // Otherwise, we can just split it normally by using the character
            // in the regular expression.
            pairs = text.split("[" + Character.toString(delimiter) + "]");
        }

        /**
         * Calculate the number of pairs --- The number of pairs is the length
         * of the pairs array minus 1 divided by 2. The one is due to the empty
         * first element from the Java split above.
         */
        int numPairs = (pairs.length - 1) / 2;

        // Create a mapping for each key and its value
        settings = new Properties();

        // Loop through the TEXT segment we just split to get the keys and
        // values
        // The key is in (i * 2) + 1 to account for the empty first element.
        // The value is in (i * 2) + 2 to account for the empty first element.
        for (int i = 0; i < numPairs; i++) {
            settings.setProperty(pairs[(i * 2) + 1].trim(), pairs[(i * 2) + 2].trim());
        }

        // Go through all the key/value pairs and parse them
        parseSettings();

        /**
         * Extract Events
         */
        if (extractEventsP) {
            // If we are extracting data, then do so.
            extractEvents();
        }
    }

    /**
     * parseSettings ---
     * <p>
     * Uses all the properties in Properties settings to initialize the fields
     * of fcsFile.
     * </p>
     *
     * <p>
     * I pulled it out since it involves a lot of constants and I don't want to
     * mix it with the file reading and parsing code.
     * </p>
     */
    private void parseSettings() {
        if (settings == null) {
            // If settings is null, then quit since there is nothing that can be
            // done here.
            return;
        }

        /**
         * At this point, we know settings is not null.
         */
        if (settings.isEmpty()) {
            // If settings is empty, then quit.
            return;
        }

        /**
         * At this point, we know settings has some mappings, so try to load
         * them.
         */
        if ((settings.getProperty("$BEGINSTEXT") != null) && (settings.getProperty("$ENDSTEXT") != null)) {
            // If the byte offset keywords for the supplemental TEXT segment are
            // not null, then parse the byte offsets.
            try {
                // Try to parse the supplemental start
                supplementalStart = Integer.parseInt(settings.getProperty("$BEGINSTEXT"));
            } catch (NumberFormatException nfe) {
                // If a NumberFormatException occurs, then set the supplemental
                // start to 0.
                supplementalStart = 0;
            }

            try {
                // Try to parse the supplemental end
                supplementalEnd = Integer.parseInt(settings.getProperty("$ENDSTEXT"));
            } catch (NumberFormatException nfe) {
                // If a NumberFormatException occurs, then set the supplemental
                // end to 0.
                supplementalEnd = 0;
            }
        }

        if ((dataStart == 0) && (dataEnd == 0)) {
            // If the begin and the end byte offsets for the DATA segment is 0,
            // then parse the byte offsets from the TEXT segment.
            try {
                // Try to parse the data start
                dataStart = Integer.parseInt(settings.getProperty("$BEGINDATA"));
            } catch (NumberFormatException nfe) {
                // If a NumberFormatException occurs, then set the data start to
                // 0.
                dataStart = 0;
            }

            try {
                // Try to parse the data end
                dataEnd = Integer.parseInt(settings.getProperty("$ENDDATA"));
            } catch (NumberFormatException nfe) {
                // If a NumberFormatException occurs, then set the data end to
                // 0.
                dataEnd = 0;
            }
        }

        if ((analysisStart == 0) && (analysisEnd == 0)) {
            // If the begin and the end byte offsets for the ANALYSIS segment is
            // 0, then parse the byte offsets from the TEXT segment.
            try {
                // Try to parse the analysis start
                analysisStart = Integer.parseInt(settings.getProperty("$BEGINANALYSIS"));
            } catch (NumberFormatException nfe) {
                // If a NumberFormatException occurs, then set the analysis
                // start to 0.
                analysisStart = 0;
            }

            try {
                // Try to parse the analysis end
                analysisEnd = Integer.parseInt(settings.getProperty("$ENDANALYSIS"));
            } catch (NumberFormatException nfe) {
                // If a NumberFormatException occurs, then set the analysis end
                // to 0.
                analysisEnd = 0;
            }
        }

        if (settings.getProperty("$PAR") != null) {
            // If "$PAR" has a mapped value, then try to parse it.
            try {
                // Try to parse the number of parameters
                parameters = Integer.parseInt(settings.getProperty("$PAR"));
            } catch (NumberFormatException nfe) {
                // If a NumberFormatException occurs, then set the number of
                // parameters to 0.
                parameters = 0;
            }
        }

        if (settings.getProperty("$TOT") != null) {
            // If "$TOT" has a mapped value, then try to parse it.
            try {
                // Try to parse the number of events
                totalEvents = Integer.parseInt(settings.getProperty("$TOT"));
            } catch (NumberFormatException nfe2) {
                // If a NumberFormatException occurs, then set the number of
                // events to 0.
                totalEvents = 0;
            }
        }

        // Get the data type
        dataType = settings.getProperty("$DATATYPE");

        // Get the sample name
        sampleName = settings.getProperty("SAMPLE ID");

        if (sampleName == null) {
            // If the sample name is null, then use the filename as the sample
            // name.
            sampleName = file.getName();
        }

        // Initialize whether the byte order is little endian to false
        littleEndianP = false;

        // Get the byte order
        String byteOrder = settings.getProperty("$BYTEORD");

        if ((byteOrder != null) && (byteOrder.length() > 0)) {
            // If the byte order is not null and not empty, then set whether the
            // byte order is little endian.
            littleEndianP = (byteOrder.equals("1,2,3,4") || byteOrder.equals("1,2"));
        }

        // Initialize all the parameter-based arrays
        channelName = new String[parameters];
        channelShortname = new String[parameters];
        channelGain = new String[parameters];
        channelBits = new int[parameters];
        channelAmp = new String[parameters];
        channelRange = new double[parameters];
        channelVoltage = new String[parameters];

        isLog = new boolean[parameters];
        ampValue = new double[parameters];
        displayLog = new boolean[parameters];

        cytometer = settings.getProperty("$CYT");
        mode = settings.getProperty("$MODE");
        instrument = settings.getProperty("$INST");
        expTime = settings.getProperty("$ETIM");
        expFile = settings.getProperty("$FIL");
        operatorName = settings.getProperty("$OP");
        operatingSystem = settings.getProperty("$SYS");
        experimentDate = settings.getProperty("$DATE");
        comment = settings.getProperty("$COM");

        creatorSoftware = settings.getProperty("CREATOR");
        cytometerNumber = settings.getProperty("CYTNUM");
        experimentName = settings.getProperty("EXPERIMENT NAME");
        exportTime = settings.getProperty("EXPORT TIME");
        exportUser = settings.getProperty("EXPORT USER NAME");
        GUID = settings.getProperty("GUID");
        windowExtension = settings.getProperty("WINDOW EXTENSION");
        threshold = settings.getProperty("THRESHOLD");
        spillString = settings.getProperty("SPILL");
        tubeName = settings.getProperty("TUBE NAME");
        wellId = settings.getProperty("WELL ID");
        plateId = settings.getProperty("PLATE ID");
        plateName = settings.getProperty("PLATE NAME");

        // Try to read the $TIMESTEP key, knowing it may be null, so default to
        // 0
        timeStep = 0;

        if (settings.getProperty("$TIMESTEP") != null) {
            // If "$TIMESTEP" has a mapped value, then try to parse it.
            try {
                // Try to parse the time step
                timeStep = Double.parseDouble(settings.getProperty("$TIMESTEP"));
            } catch (NumberFormatException nfe3) {
                // If a NumberFormatException occurs, then set the time step to
                // 0.
                timeStep = 0;
            }
        }

        source = settings.getProperty("$SRC");
        endsText = settings.getProperty("$ENDSTEXT");
        nextData = settings.getProperty("$NEXTDATA");
        bTime = settings.getProperty("$BTIM");

        // Try to read the APPLY COMPENSATION key, knowing it may be null, so
        // default to false
        applyCompensation = false;

        String applyString = settings.getProperty("APPLY COMPENSATION");
        if ((applyString != null) && applyString.equalsIgnoreCase("true")) {
            // If the value of "APPLY COMPENSATION" is "true", then set
            // applyCompensation to true.
            applyCompensation = true;
        }

        lasers = 0;

        // Count the number of lasers
        for (int i = 1; i <= settings.size(); i++) {
            if (settings.getProperty("LASER" + i + "NAME") != null) {
                // If "LASER#NAME" exists, then increment the number of lasers.
                lasers++;
            }
        }

        laserASF = new String[lasers];
        laserDelay = new String[lasers];
        laserName = new String[lasers];

        for (int i = 1; i <= lasers; i++) {
            laserASF[i - 1] = settings.getProperty("LASER" + i + "ASF");
            laserDelay[i - 1] = settings.getProperty("LASER" + i + "DELAY");
            laserName[i - 1] = settings.getProperty("LASER" + i + "NAME");
        }

        for (int i = 1; i <= parameters; i++) {
            channelShortname[i - 1] = settings.getProperty("$P" + i + "N");
            channelName[i - 1] = settings.getProperty("$P" + i + "S");

            if (channelName[i - 1] == null) {
                channelName[i - 1] = channelShortname[i - 1];
            }

            channelGain[i - 1] = settings.getProperty("$P" + i + "G");

            try {
                channelBits[i - 1] = Integer.parseInt(settings.getProperty("$P" + i + "B"));
            } catch (NumberFormatException nfe) {
                if (dataType != null) {
                    if (dataType.equalsIgnoreCase("I")) {
                        // If the data type is "I", then it is binary integer.
                        channelBits[i - 1] = 16;
                    } else if (dataType.equalsIgnoreCase("F")) {
                        // If the data type is "F", then it is floating point.
                        channelBits[i - 1] = 32;
                    } else if (dataType.equalsIgnoreCase("D")) {
                        // If the data type is "D", then it is double precision
                        // floating point
                        channelBits[i - 1] = 64;
                    } else if (dataType.equalsIgnoreCase("A")) {
                        // If the data type is "A", then it is ASCII.
                        channelBits[i - 1] = 8;
                    }
                } else {
                    // Otherwise, set the number of channel bits to 0.
                    channelBits[i - 1] = 0;
                }
            }

            channelAmp[i - 1] = settings.getProperty("$P" + i + "E");

            displayLog[i - 1] = false;

            String displayString = settings.getProperty("P" + i + "DISPLAY");
            if ((displayString != null) && displayString.equalsIgnoreCase("log")) {
                displayLog[i - 1] = true;
            }

            if (channelAmp[i - 1] != null) {
                String[] ampArray = channelAmp[i - 1].split(",");

                try {
                    // Try to parse the amp value
                    ampValue[i - 1] = Double.parseDouble(ampArray[0]);
                } catch (NumberFormatException nfe4) {
                    // If a NumberFormatException occurs, then set the amp value
                    // to 0.
                    ampValue[i - 1] = 0;
                }
            } else {
                // Otherwise, set the amp value to 0 in the event that the
                // channel amp is missing.
                ampValue[i - 1] = 0d;
            }

            if (ampValue[i - 1] > 0) {
                isLog[i - 1] = true;
            } else {
                // Otherwise, set is log to false.
                isLog[i - 1] = false;
            }

            try {
                channelRange[i - 1] = Double.parseDouble(settings.getProperty("$P" + i + "R"));
            } catch (NumberFormatException nfe5) {
                channelRange[i - 1] = 0;
            }
            channelVoltage[i - 1] = settings.getProperty("$P" + i + "V");

            if ((channelVoltage[i - 1] == null) && (cytometer != null) && cytometer.equals("FACSCalibur")) {
                switch (i - 1) {
                    case 2:
                        channelVoltage[i - 1] = settings.getProperty("BD$WORD3");
                        break;
                    case 3:
                        channelVoltage[i - 1] = settings.getProperty("BD$WORD5");
                        break;
                    case 4:
                        channelVoltage[i - 1] = settings.getProperty("BD$WORD7");
                        break;
                    case 5:
                        channelVoltage[i - 1] = settings.getProperty("BD$WORD9");
                        break;
                    case 6:
                        channelVoltage[i - 1] = settings.getProperty("BD$WORD11");
                        break;
                    case 7:
                        channelVoltage[i - 1] = settings.getProperty("BD$WORD55");
                        break;
                    case 8:
                        channelVoltage[i - 1] = settings.getProperty("BD$WORD24");
                        break;
                    default:
                        channelVoltage[i - 1] = "";
                        break;
                }
            }
        }

        /**
         * Calculate the number of events in the flow file based on the number
         * of bits in each event
         */
        // Initialize the number of bits in each event to 0
        int numBitsPerEvent = 0;

        // Loop through all the parameters adding the number of bits in each
        // parameter
        for (int i = 0; i < parameters; i++) {
            numBitsPerEvent += channelBits[i];
        }

        if (numBitsPerEvent > 0) {
            // If the number of bits in each event is greater than 0, then
            // calculate the number of events based on the size of the DATA
            // segment.
            int calculatedNumEvents = (dataEnd - dataStart + 1) * 8 / numBitsPerEvent;

            if (totalEvents > calculatedNumEvents) {
                // If the number of events is greater than the calculated number
                // of events, then update the number of events.
                totalEvents = calculatedNumEvents;
            }
        }
    }

    /**
     * extractEvents ---
     * <p>
     * Extracts the events from the FCS file using NIO.
     * </p>
     *
     * @throws <code>java.io.FileNotFoundException</code> if the file is not
     *         found.
     * @throws <code>java.io.IOException</code> if an IO exception occurred.
     */
    private void extractEvents() throws FileNotFoundException, IOException {
        if ((dataStart >= dataEnd) || (totalEvents <= 0)) {
            // If the byte offset of the start of the DATA segment is greater
            // than or equal to the end of the DATA segment or the number of
            // events is equal to 0, then create an empty array of events.
            eventList = new double[0][parameters];

            return;
        }

        // Open a file input stream to the file
        FileInputStream fis = new FileInputStream(file);

        // Get the channel for the file
        FileChannel fc = fis.getChannel();

        // Map the DATA segment to memory
        MappedByteBuffer data;

        try {
            data = fc.map(FileChannel.MapMode.READ_ONLY, dataStart, dataEnd - dataStart + 1);
        } catch (Throwable t) {
            // Try again with a workaround to see if we can compensate for off-by-one errors that
            // some FCS files have been known to incorporate in the ENDDATA property.
            data = fc.map(FileChannel.MapMode.READ_ONLY, dataStart, dataEnd - dataStart);
        }

        /**
         * We don't need to worry about endian-ness here since ASCII is one
         * byte, and float and double are IEEE standards.
         */
        if (dataType != null) {
            if (dataType.equalsIgnoreCase("I")) {
                // If the data type is "I", then it is binary integer.
                readBinIntData(data);
            } else if (dataType.equalsIgnoreCase("F")) {
                // If the data type is "F", then it is floating point.
                readFloatData(data);
            } else if (dataType.equalsIgnoreCase("D")) {
                // If the data type is "D", then it is double precision floating
                // point
                readDoubleData(data);
            } else if (dataType.equalsIgnoreCase("A")) {
                // If the data type is "A", then it is ASCII.
                readASCIIData(data);
            }
        }

        // Close the file channel
        fc.close();

        // Close the file input stream
        fis.close();
    }

    /**
     * readBinIntData ---
     * <p>
     * Reads binary integers in list mode in the DATA segment and updates
     * eventList.
     * </p>
     *
     * <p>
     * Assumes that the bits for the values are byte-aligned. It needs to be
     * fixed if that is not the case.
     * </p>
     *
     * @param data
     *            <code>ByteBuffer</code> containing the DATA segment of the
     *            underlying file.
     */
    private void readBinIntData(ByteBuffer data) {
        // Allocate the eventList
        eventList = new double[parameters][totalEvents];

        int numBytes, value, range, currByte;

        final int totalEvents = this.totalEvents;
        final int parameters = this.parameters;

        for (int i = 0; i < totalEvents; i++) {
            for (int j = 0; j < parameters; j++) {
                // Calculate the number of bytes used from the number of bits
                // used
                // Round up to the next full byte
                numBytes = (int) Math.ceil(((double) channelBits[j]) / Byte.SIZE);


                // Get the range of the current parameter - will let us build
                // the mask
                range = (int) channelRange[j];

                // Initialize the current value to 0
                value = 0;

                if (littleEndianP) {
                    // If the byte order is little endian, then build the value
                    // to the left.
                    for (int k = 0; k < numBytes; k++) {
                        // Get the next 8 bits masking to make sure Java doesn't
                        // prepend 1's
                        currByte = (data.get() & 0xFF);

                        // Shift the 8 bits into position
                        currByte <<= (8 * k);

                        // Or the 8 bits with the current value
                        value |= currByte;
                    }
                } else {
                    // Otherwise, the byte order is big endian, so build the
                    // value to the right.
                    for (int k = 0; k < numBytes; k++) {
                        // Left shift the previous bits in value to make room
                        value <<= 8;

                        // Grab the next 8 bits masking to make sure Java
                        // doesn't prepend 1's
                        value |= (data.get() & 0xFF);
                    }
                }

                /**
                 * From the FCS specification: --- The remaining bits are
                 * usually unused and set to "0"; however, some file writers
                 * store non-data information in that bit-space. Implementers
                 * must use a bit mask when reading these list mode parameter
                 * values to insure that erroneous values are not read from the
                 * unused bits.
                 */
                // Mask the value based on the range
                value &= (range - 1);

                // Store the value into the array
                eventList[j][i] = value;
            }
        }
    }

    /**
     * readFloatData ---
     * <p>
     * Reads floating point values in list mode in the DATA segment and updates
     * eventList with the integer values of the values.
     * </p>
     *
     * @param data
     *            <code>ByteBuffer</code> containing the DATA segment of the
     *            underlying file.
     */
    private void readFloatData(ByteBuffer data) {
        // Allocate the eventList
        eventList = new double[parameters][totalEvents];

        if (littleEndianP) {
            data.order(ByteOrder.LITTLE_ENDIAN);
        }

        // Convert the byte buffer into a float buffer - doesn't get any easier
        FloatBuffer fb = data.asFloatBuffer();

        final int totalEvents = this.totalEvents;
        final int parameters = this.parameters;

        for (int i = 0; i < totalEvents; i++) {
            for (int j = 0; j < parameters; j++) {
                // Store the value into the array
                eventList[j][i] = fb.get();
            }
        }
    }

    /**
     * readDoubleData ---
     * <p>
     * Reads double precision floating point values in list mode in the DATA
     * segment and updates eventList with the integer values of the values.
     * </p>
     *
     * @param data
     *            <code>ByteBuffer</code> containing the DATA segment of the
     *            underlying file.
     */
    private void readDoubleData(ByteBuffer data) {
        // Allocate the eventList
        eventList = new double[parameters][totalEvents];

        if (littleEndianP) {
            data.order(ByteOrder.LITTLE_ENDIAN);
        }

        // Convert the byte buffer into a double buffer - doesn't get any easier
        DoubleBuffer db = data.asDoubleBuffer();

        final int totalEvents = this.totalEvents;
        final int parameters = this.parameters;

        for (int i = 0; i < totalEvents; i++) {
            for (int j = 0; j < parameters; j++) {
                // Store the value into the array
                eventList[j][i] = db.get();
            }
        }
    }

    /**
     * readASCIIData ---
     * <p>
     * Reads ASCII values in list mode in the DATA segment and updates eventList
     * with the integer values of the values.
     * </p>
     *
     * @param data
     *            <code>ByteBuffer</code> containing the DATA segment of the
     *            underlying file.
     */
    private void readASCIIData(ByteBuffer data) {
        /**
         * Calculate the number of characters in each event of the flow file
         */
        // Initialize the number of characters in each event to 0
        int numCharsPerEvent = 0;

        // Loop through all the parameters adding the number of characters in
        // each parameter
        for (int i = 0; i < parameters; i++) {
            numCharsPerEvent += channelBits[i];
        }

        // Allocate the eventList
        eventList = new double[parameters][totalEvents];

        // Convert the byte buffer into a char buffer - doesn't get any easier
        CharBuffer cb = data.asCharBuffer();

        // Initialize the current character to 0
        int currChar = 0;

        for (int i = 0; i < totalEvents; i++) {
            for (int j = 0; j < parameters; j++) {
                try {
                    // Store the value into the array
                    eventList[j][i] = Integer.parseInt(cb.subSequence(currChar, currChar + channelBits[i]).toString());
                } catch (NumberFormatException nfe) {
                    eventList[j][i] = 0;
                }

                // Increment the current character
                currChar += channelBits[i];
            }
        }
    }

    /**
     * getEventList ---
     * <p>
     * Returns the event list.
     * <p>
     *
     * @return array of double arrays containing the events.
     */
    public double[][] getEventList() {
        if (eventList == null) {
            // If the array of events is null, then try to extract the events
            // from the FCS file.
            try {
                // Try to extract the events
                extractEvents();
            } catch (FileNotFoundException fnfe) {
                // If a FileNotFoundException occurred, then return an empty
                // array of events.
                return new double[0][0];
            } catch (IOException ioe) {
                // If a IOException occurred, then return an empty array of
                // events.
                return new double[0][0];
            }
        }

        // Return the array of events
        return eventList;
    }

    /**
     * getCompensatedEventList ---
     * <p>
     * Returns the event list compensated by the SPILL matrix.
     * <p>
     *
     * @return array of double arrays containing the events.
     */
    public double[][] getCompensatedEventList() {
        double[][] events = this.getEventList();
        if (events.length != this.getNumChannels())
            return events;  // Unable to extract the underlying events

        // Convert the SPILL string to a compensation matrix
        String compString = this.getSpillString();
        if (compString == null)
            return events;  // No compensation, just return the events

        // Split the compensation string into its values
        //
        // The basic structure for SPILL* is:
        // $SPILLOVER/n,string1,string2,...,f1,f2,f3,f4,.../

        String[] compValues = compString.split(",");
        String[] compNames  = null;
        String[] compData   = null;
        int compDataStart   = 0;

        int n = 0;
        try {
            // Try to parse the number of acquisition parameters
            n = Integer.parseInt(compValues[0]);
            if (n <= 0 || n > this.parameters)
                throw new NumberFormatException();
        } catch (NumberFormatException nfe) {
            CyLogger.getLogger().error("Failed to parse parameter count in spill string",nfe);
            return events;
        }

        compNames = Arrays.copyOfRange(compValues, 1, n+1);

        // Match names in spill string to columns in parameter lists
        compDataStart = Arrays.asList(this.channelShortname).indexOf(compNames[0]);
        if (compDataStart < 0) {
            CyLogger.getLogger().error("Failed to match channel "+compNames[0]+" to parameter in file");
            return events;  // Failure match spill string names to channels
        }
        for (int i = 0; i < n; i++) {
            if (!compNames[i].equals(this.channelShortname[compDataStart + i])) {
                CyLogger.getLogger().error("Spill channel are not continguous parameters in file");
                return events;  // Spill string columns not in order
            }
        }

       // Extract actual compensation data
        compData  = Arrays.copyOfRange(compValues, n+1, compValues.length);
        if (compData.length != (n*n))
            return events;

        /**
         * Populate the compensation matrix --- The values are stored in
         * row-major order, i.e., the elements in the first row appear
         * first.
         */
        double[][] matrix = new double[n][n];

        // Loop through the array of compensation values
        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) {
                try {
                    matrix[i][j] = Double.parseDouble(compData[i*n+j]);
                } catch (NumberFormatException nfe) {
                    // Set default value If a NumberFormatException occurred
                    matrix[i][j] = 0.0d;
                }
            }
        }

        // Compute the inverse of the compensation data and then apply
        // to data matrix (which is column major). Specifically compute
        // transpose(inverse(<SPILL MATRIX>)) * data
        RealMatrix comp =
                (new LUDecompositionImpl(new Array2DRowRealMatrix(matrix)))
                .getSolver()
                .getInverse();
        RealMatrix data = new BlockRealMatrix(events);
        data.setSubMatrix(  // Update compensated portion of data matrix
                comp
                .transpose()
                .multiply(data.getSubMatrix(compDataStart, compDataStart+n-1, 0, this.getEventCount()-1))
                .getData(),
                
                compDataStart, 0
                );
        return data.getData();
    }

    /**
     * Accessors
     */
    /**
     * isFCS ---
     * <p>
     * Returns whether the file is an FCS file.
     * </p>
     *
     * <p>
     * Note: All the public fields are valid if and only if the file is an FCS
     * file.
     * </p>
     *
     * @return boolean flag indicating whether the file is an FCS file.
     */
    public boolean isFCS() {
        return isFCSP;
    }

    /**
     * getVersion ---
     * <p>
     * Returns the FCS version string of the underlying FCS file.
     * </p>
     *
     * @return <code>String</code> FCS version string of the underlying FCS
     *         file.
     */
    public String getVersion() {
        return version;
    }

    public int getTextStart() {
        return textStart;
    }

    public int getTextEnd() {
        return textEnd;
    }

    public int getDataStart() {
        return dataStart;
    }

    public int getDataEnd() {
        return dataEnd;
    }

    public int getAnalysisStart() {
        return analysisStart;
    }

    public int getAnalysisEnd() {
        return analysisEnd;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public String getText() {
        return text;
    }

    public String getCytometer() {
        return cytometer;
    }

    /**
     * getChannelHelper ---
     * <p>
     * Returns the channel number of the forward scatter channel if sideScatterP
     * is false or the side scatter channel if sideScatterP is true or -1 if it
     * is not found.
     * </p>
     *
     * <p>
     * For forward scatter, it looks in all the channel names for the channel
     * whose name is "Forward Scatter" or whose channel name or shortname starts
     * with "FS".
     * </p>
     *
     * <p>
     * For side scatter, it looks in all the channel names for the channel whose
     * name is "Side Scatter" or whose channel name or shortname starts with
     * "SS".
     * </p>
     *
     * @return int channel number of the forward scatter channel if sideScatterP
     *         is false or the side scatter channel if sideScatterP is true or
     *         -1 if it is not found.
     */
    private int getChannelHelper(boolean sideScatterP) {
        if ((channelName == null) || (channelShortname == null)) {
            // If the channel name array or the channel shortname array is null,
            // then quit, since they have not been initialized suggesting
            // something went wrong in the TEXT parsing.
            return -1;
        }

        // Loop through all the channels
        for (int i = 0; i < parameters; i++) {
            if (channelName[i] == null) {
                // If either the channel name is null, then there is nothing we
                // can do with the channel, so skip to the next channel.
                continue;
            } else if (sideScatterP) {
                // If sideScatterP, then try to find the side scatter channel.
                if (channelName[i].indexOf("Side Scatter") >= 0) {
                    // If the channel name contains "Side Scatter", then we are
                    // pretty sure the channel is the side scatter channel, so
                    // return the channel number.
                    return i;
                } else if (channelName[i].startsWith("SS") || ((channelShortname[i] != null) && channelName[i].startsWith("SS"))) {
                    // If the channel name starts with "SS" or the channel
                    // shortname starts with "SS", then we less certain the
                    // channel is side scatter, but it probably is, so return
                    // the channel number.
                    // This part can be changed.
                    return i;
                }
            } else {
                // Otherwise, try to find the forward scatter channel.
                if (channelName[i].indexOf("Forward Scatter") >= 0) {
                    // If the channel name contains "Forward Scatter", then we
                    // are pretty sure the channel is the forward scatter
                    // channel, so return the channel number.
                    return i;
                } else if (channelName[i].startsWith("FS") || ((channelShortname[i] != null) && channelName[i].startsWith("FS"))) {
                    // If the channel name starts with "FS" or the channel
                    // shortname starts with "FS", then we less certain the
                    // channel is forward scatter, but it probably is, so return
                    // the channel number.
                    // This part can be changed.
                    return i;
                }
            }
        }

        // At this point, we couldn't determine which channel was Side Scatter,
        // so return -1.
        return -1;
    }

    /**
     * getForwardScatterChannel ---
     * <p>
     * Returns the channel number of the forward scatter channel or -1 if it is
     * not found.
     * </p>
     *
     * @return int channel number of the forward scatter channel or -1 if it is
     *         not found.
     */
    public int getForwardScatterChannel() {
        return getChannelHelper(false);
    }

    /**
     * getSideScatterChannel ---
     * <p>
     * Returns the channel number of the side scatter channel or -1 if it is not
     * found.
     * </p>
     *
     * @return int channel number of the side scatter channel or -1 if it is not
     *         found.
     */
    public int getSideScatterChannel() {
        return getChannelHelper(true);
    }

    /**
     * getChannelShortName ---
     * <p>
     * Returns the $PN of the channel with number channelNumber.
     * </p>
     *
     * @param channelNumber
     *            int number of the channel whose name to return.
     * @return <code>String</code> short name of the channel with number
     *         channelNumber.
     */
    public String getChannelShortName(int channelNumber) {
        if ((channelShortname == null) || (channelNumber < 0) || (channelNumber >= getNumChannels())) {
            // If the channel name array or the channel shortname array is null
            // or the channel number is invalid, then quit, since they have not
            // been initialized suggesting something went wrong in the TEXT
            // parsing.
            return "Error reading this channel!";
        }


        if (channelShortname[channelNumber] == null) {
            // If the channel short name for the channel is also null, then
            // return "Channel #" as the channel name.
            return ("Channel " + channelNumber);
        } else {
            // Otherwise, return the channel short name as the channel name.
            return channelShortname[channelNumber];
        }
    }

    /**
     * getChannelIdFromShortName
     *
     */
    public int getChannelIdFromShortName(String name) {
        for (int i=0; i<channelShortname.length; i++)
            if (channelShortname[i].equals(name))
                return i;
        return -1;
    }

    /**
     * getChannelName ---
     * <p>
     * Returns the name of the channel with number channelNumber.
     * </p>
     *
     * <p>
     * I do this way too often in the code. I don't know why I didn't realize
     * that a method is needed sooner.
     * </p>
     *
     * @param channelNumber
     *            int number of the channel whose name to return.
     * @return <code>String</code> name of the channel with number
     *         channelNumber.
     */
    public String getChannelName(int channelNumber) {
        if ((channelName == null) || (channelShortname == null) || (channelNumber < 0) || (channelNumber >= getNumChannels())) {
            // If the channel name array or the channel shortname array is null
            // or the channel number is invalid, then quit, since they have not
            // been initialized suggesting something went wrong in the TEXT
            // parsing.
            return "Error reading this channel!";
        }

        if (channelName[channelNumber] == null) {
            // If the channel name for the channel is null, then check the
            // channel short name.
            if (channelShortname[channelNumber] == null) {
                // If the channel short name for the channel is also null, then
                // return "Channel #" as the channel name.
                return ("Channel " + channelNumber);
            } else {
                // Otherwise, return the channel short name as the channel name.
                return channelShortname[channelNumber];
            }
        } else {
            // Otherwise, return the channel name as the channel name.
            return channelName[channelNumber];
        }
    }

    /**
     * getChannelCount ---
     * <p>
     * Returns the number of channels in the flow file.
     * </p>
     *
     * @return int number of channels in the flow file.
     */
    public int getChannelCount() {
        return parameters;
    }

    /**
     * getNumChannels ---
     * <p>
     * Returns the number of channels in the flow file.
     * </p>
     *
     * @return int number of channels in the flow file.
     */
    public int getNumChannels() {
        return getChannelCount();
    }

    /**
     * isLog ---
     * <p>
     * Returns a boolean flag indicating whether the channel with channel number
     * channelNumber is stored in log format.
     * </p>
     *
     * @param channelNumber
     *            int number of the channel.
     * @return boolean flag indicating whether the channel with channel number
     *         channelNumber is stored in log format.
     */
    public boolean isLog(int channelNumber) {
        if ((isLog != null) && (channelNumber >= 0) && (channelNumber < isLog.length)) {
            // If the isLog array is not null and the channel number is in the
            // range of the array,
            // then return the value at channelNumber in the isLog array.
            return isLog[channelNumber];
        }

        // Otherwise, return false.
        return false;
    }

    /**
     * getIsLog ---
     * <p>
     * Returns the <code>boolean</code> status of whether the given channel is
     * stored in log format.
     * </p>
     *
     * @param channelNumber
     *            int number of the channel.
     * @return boolean flag indicating whether the channel with channel number
     *         channelNumber is stored in log format.
     */
    public boolean getIsLog(int channelNumber) {
        return isLog(channelNumber);
    }

    /**
     * isDisplayLog ---
     * <p>
     * Returns a boolean flag indicating whether the channel with channel number
     * channelNumber should be displayed in log format.
     * </p>
     *
     * @param channelNumber
     *            int number of the channel.
     * @return boolean flag indicating whether the channel with channel number
     *         channelNumber should be displayed in log format.
     */
    public boolean isDisplayLog(int channelNumber) {
        if ((displayLog != null) && (channelNumber >= 0) && (channelNumber < displayLog.length)) {
            // If the displayLog array is not null and the channel number is in
            // the range of the array,
            // then return the value at channelNumber in the displayLog array.
            return displayLog[channelNumber];
        }

        // Otherwise, return false.
        return false;
    }

    /**
     * getDisplayLog ---
     * <p>
     * Returns the <code>boolean</code> status of whether the given channel
     * should be displayed in log format.
     * </p>
     *
     * @param channelNumber
     *            int number of the channel.
     * @return boolean flag indicating whether the channel with channel number
     *         channelNumber should be displayed in log format.
     */
    public boolean getDisplayLog(int channelNumber) {
        return isDisplayLog(channelNumber);
    }

    /**
     * getAmpValue ---
     * <p>
     * Returns the <code>int</code> log amplifier value.
     * </p>
     *
     * @return <code>double</code> log amplifier value.
     * @param channelNumber
     *            <code>int</code> the number index of the channel in the array
     */
    public double getAmpValue(int channelNumber) {
        if (ampValue != null && channelNumber >= 0 && channelNumber < ampValue.length) {
            return ampValue[channelNumber];
        }
        return 0;
    }

    /**
     * getChannelRange ---
     * <p>
     * Returns the <code>int</code> range of the given channel (before any log
     * amp, as stored in the fcs file).
     * </p>
     *
     * @return <code>int</code> range of the given channel.
     * @param channelNumber
     *            <code>int</code> the number index of the channel in the array
     */
    public double getChannelRange(int channelNumber) {
        if (channelRange != null && channelNumber >= 0 && channelNumber < channelRange.length) {
            return channelRange[channelNumber];
        }
        return 0;
    }

    /**
     * getScaleRange ---
     * <p>
     * Returns the <code>int</code> scale range of the given channel (after any
     * log amp).
     * </p>
     *
     * @return <code>int</code> range of the given channel.
     * @param channelNumber
     *            <code>int</code> the number index of the channel in the array
     */
    public double getScaleRange(int channelNumber) {
        if (getIsLog(channelNumber)) {
            return Math.pow(10, getAmpValue(channelNumber));
        } else {
            return getChannelRange(channelNumber);
        }
    }

    /**
     * getAmpDisplay ---
     * <p>
     * Returns <code>boolean</code> need to log amplify the channel display or
     * not.
     * </p>
     *
     * @return <code>boolean</code> of whether to log amplify or not.
     * @param channelNumber
     *            <code>int</code> the number index of the channel in the array
     */
    public boolean getAmpDisplay(int channelNumber) {
        // If the channel is not log but is being displayed as log (i.e. LSR II
        // data)
        if (!getIsLog(channelNumber) && getDisplayLog(channelNumber)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * getSettings ---
     * <p>
     * Returns the <code>Properties</code> object containing all the key/value
     * pairs containing all the settings of the FCS file.
     * </p>
     *
     * @return <code>Properties</code> object containing all the key/value pairs
     *         containing all the settings of the FCS file.
     */
    public Properties getSettings() {
        return settings;
    }

    /**
     * getEventCount ---
     * <p>
     * Returns the number of events in the flow file.
     * </p>
     *
     * @return int number of events in the flow file.
     */
    public int getEventCount() {
        return totalEvents;
    }

    /**
     * getSpillString ---
     * <p>
     * Returns the spill string of the flow file.
     * </p>
     *
     * @return <code>String</code> spill string of the flow file.
     */
    public String getSpillString() {
        return spillString;
    }

    /**
     * getFile ---
     * <p>
     * Returns a <code>File</code> object corresponding to the underlying file.
     * </p>
     *
     * @return <code>File</code> object corresponding to the underlying file.
     */
    public File getFile() {
        return file;
    }

    /**
     * getPath ---
     * <p>
     * Returns the <code>String</code> path to the underlying file.
     * </p>
     *
     * @return <code>String</code> path to the underlying file.
     */
    public String getPath() {
        return file.getPath();
    }

    /**
     * getName ---
     * <p>
     * Returns the <code>String</code> name of the underlying file.
     * </p>
     *
     * @return <code>String</code> name of the underlying file.
     */
    public String getName() {
        return file.getName();
    }

    /**
     * length ---
     * <p>
     * Returns the length of the underlying file.
     * </p>
     *
     * @return long length of the underlying file.
     */
    public long length() {
        return file.length();
    }

    /**
     * isFCSFile ---
     * <p>
     * A static helper method to determine whether the file in the
     * <code>java.io.File</code> object file is a FCS file.
     * </p>
     *
     * @param file
     *            <code>java.io.File</code> object to the file to test.
     * @return boolean flag indicating whether the file in the
     *         <code>java.io.File</code> object file is a FCS file.
     */
    public static boolean isFCSFile(File file) throws FileNotFoundException, IOException {
        if ((file == null) || (!file.exists()) || (!file.isFile())) {
            // If the file is null, does not exist, or is not a file, then
            // return false.
            return false;
        }

        // Open a file input stream to the file
        FileInputStream fis = new FileInputStream(file);

        // Create a byte array to hold the version
        byte[] versionArray = new byte[VERSION_SIZE];

        // Read the version into the byte array
        int numRead = fis.read(versionArray);

        // Close the file input stream
        fis.close();

        if (numRead < VERSION_SIZE) {
            // If the number of bytes read is less than the number of bytes in
            // the version string, then the file is too small to be an FCS file.
            return false;
        } else {
            // Otherwise, at least 6 bytes were read, so decode it and determine
            // whether the file is an FCS file.
            // Decode the version using the default encoding
            String version = new String(versionArray);

            // Determine whether the file is an FCS file by whether the version
            // string starts with the FCS_PREFIX
            return version.startsWith(FCS_PREFIX);
        }
    }

    /**
     * Testing Code ---
     * <p>
     * The functions below are static functions used to test the fcsFile class.
     * </p>
     *
     * <p>
     * Different testing functions to exercise different parts of the class.
     * </p>
     */
    /**
     * printFCSFile ---
     * <p>
     * Prints all the fields of <code>fcsFile</code> f to standard output - for
     * testing.
     * <p>
     *
     * @param f
     *            <code>fcsFile</code> object to print to standard output.
     */
    public static void printFCSFile(fcsFile f) {
        System.out.println("Version ID -=" + f.version + "=-");
        System.out.println("Text starts at -=" + f.textStart + "=-");
        System.out.println("Text ends at -=" + f.textEnd + "=-");
        System.out.println("Data starts at -=" + f.dataStart + "=-");
        System.out.println("Data ends at -=" + f.dataEnd + "=-");
        System.out.println("Analysis starts at -=" + f.analysisStart + "=-");
        System.out.println("Analysis ends at -=" + f.analysisEnd + "=-");
        System.out.println("Text delimiter -=" + Character.toString(f.delimiter) + "=-");
        System.out.println("Data type -=" + f.dataType + "=-");
        System.out.println("Cytometer -=" + f.cytometer + "=-");
        System.out.println("Lasers -=" + f.lasers + "=-");
        /*
         * System.out.println("LaserName (Count: " + f.laserASF.length + "):");
         * for(int i = 0; i < f.laserASF.length; i++) {
         * System.out.println("\t::" + f.laserASF[i]); }
         *
         * System.out.println("LaserASF (Count: " + f.laserName.length + "):");
         * for(int i = 0; i < f.laserName.length; i++) {
         * System.out.println("\t::" + f.laserName[i]); }
         *
         * System.out.println("LaserDelay (Count: " + f.laserDelay.length +
         * "):"); for(int i = 0; i < f.laserDelay.length; i++) {
         * System.out.println("\t::" + f.laserDelay[i]); }
         *
         * System.out.println("MODE -=" + f.mode + "=-");
         * System.out.println("Instrument -=" + f.instrument + "=-");
         * System.out.println("Experiment Time -=" + f.expTime + "=-");
         * System.out.println("Experiment File Name -=" + f.expFile + "=-");
         * System.out.println("Operator -=" + f.operatorName + "=-");
         * System.out.println("OS -=" + f.operatingSystem + "=-");
         * System.out.println("Date -=" + f.experimentDate + "=-");
         * System.out.println("Creator Software -=" + f.creatorSoftware + "=-");
         * System.out.println("Cytometer Number -=" + f.cytometerNumber + "=-");
         * System.out.println("Experiment Name -=" + f.experimentName + "=-");
         * System.out.println("Export Time -=" + f.exportTime + "=-");
         * System.out.println("Export User -=" + f.exportUser + "=-");
         * System.out.println("GUID -=" + f.GUID + "=-");
         * System.out.println("Window Extension -=" + f.windowExtension + "=-");
         * System.out.println("Threshold -=" + f.threshold + "=-");
         * System.out.println("Spill String -=" + f.spillString + "=-");
         * System.out.println("Tube Name -=" + f.tubeName + "=-");
         * System.out.println("Time Step -=" + f.timeStep + "=-");
         * System.out.println("Source -=" + f.source + "=-");
         * System.out.println("Text Ends -=" + f.endsText + "=-");
         * System.out.println("Next Data -=" + f.nextData + "=-");
         * System.out.println("B Time -=" + f.bTime + "=-");
         * System.out.println("Apply Compensation -=" + f.applyCompensation +
         * "=-"); System.out.println("Sample name -=" + f.sampleName + "=-");
         * System.out.println("Parameters -=" + f.parameters + "=-");
         * System.out.println("Total events -=" + f.totalEvents + "=-");
         *
         * System.out.println("Channel names (Count: " + f.channelName.length +
         * "):"); for(int i = 0; i < f.channelName.length; i++) {
         * System.out.println("\t::" + f.channelName[i]); }
         *
         * System.out.println("Channel short names (Count: " +
         * f.channelShortname.length + "):"); for(int i = 0; i <
         * f.channelShortname.length; i++) { System.out.println("\t::" +
         * f.channelShortname[i]); }
         *
         * System.out.println("Channel gains (Count: " + f.channelGain.length +
         * "):"); for(int i = 0; i < f.channelGain.length; i++) {
         * System.out.println("\t::" + f.channelGain[i]); }
         *
         * System.out.println(
         * "Channel amps (4=log, 0=linear -- aka the number of decades on the plot) (Count: "
         * + f.channelAmp.length + "):"); for(int i = 0; i <
         * f.channelAmp.length; i++) { System.out.println("\t::" +
         * f.channelAmp[i]); }
         *
         * System.out.println("Therefore, are channels log? (Count: " +
         * f.isLog.length + "):"); for(int i = 0; i < f.isLog.length; i++) {
         * System.out.println("\t::" + f.isLog[i]); }
         *
         * System.out.println("Display as log? (Count: " + f.displayLog.length +
         * "):"); for(int i = 0; i < f.displayLog.length; i++) {
         * System.out.println("\t::" + f.displayLog[i]); }
         *
         * System.out.println("Amp values (Count: " + f.ampValue.length + "):");
         * for(int i = 0; i < f.ampValue.length; i++) {
         * System.out.println("\t::" + f.ampValue[i]); }
         *
         * System.out.println("Channel bits (Count: " + f.channelBits.length +
         * "):"); for(int i = 0; i < f.channelBits.length; i++) {
         * System.out.println("\t::" + f.channelBits[i]); }
         *
         * System.out.println(
         * "Channel ranges (maximum number of bins; 1024 on Calibur; 262144 on LSRII) (Count: "
         * + f.channelRange.length + "):"); for(int i = 0; i <
         * f.channelRange.length; i++) { System.out.println("\t::" +
         * f.channelRange[i]); }
         *
         * System.out.println(
         * "Channel voltages (not working for Calibur yet) (Count: " +
         * f.channelVoltage.length + "):"); for(int i = 0; i <
         * f.channelVoltage.length; i++) { System.out.println("\t::" +
         * f.channelVoltage[i]); }
         *
         * System.out.println("All settings key / value pairs:");
         * //f.getSettings().list(System.out);
         *
         * // Get the settings Properties settings = f.getSettings();
         *
         * // Get all the keys Enumeration keys = settings.propertyNames();
         *
         * // Create a list to hold all the keys ArrayList keysList = new
         * ArrayList();
         *
         * String key;
         *
         * // Loop through all the keys getting their values
         * while(keys.hasMoreElements()) { // Get the current key key =
         * (String)keys.nextElement();
         *
         * // Add the current key to the list of keys keysList.add(key); }
         *
         * if(keysList.size() > 1) { // If there are more than one key in the
         * list of keys, then sort it. Collections.sort(keysList); }
         *
         * // Loop through the list of keys for(int i = 0; i < keysList.size();
         * i++) { // Get the current key key = (String)keysList.get(i);
         *
         * // Print the key/value pair System.out.print(key);
         * System.out.print("="); System.out.println(settings.getProperty(key));
         * }
         */

        // System.out.println("The entire text section (unformatted) -=" +
        // f.text + "=-");
        // Print out the first 50 events, if there are any
        f.printEvents(50);
    }

    /**
     * printEvents ---
     * <p>
     * Prints the first numEvents events to standard output for testing.
     * </p>
     *
     * @param numEvents
     *            int number of events to print.
     */
    private void printEvents(int numEvents) {
        if (eventList == null) {
            // If the eventList was not populated, then simply quit.
            return;
        }

        System.out.println("The events (Count: " + numEvents + "):");

        // Print out all the channel short names
        for (int i = 0; i < parameters; i++) {
            System.out.print("\t" + channelShortname[i]);
        }
        System.out.println();

        // Loop through the first numEvents
        for (int i = 0; i < numEvents; i++) {
            System.out.print("Event " + (i + 1) + ":");

            // Loop through all the parameters for that event
            for (int j = 0; j < parameters; j++) {
                // Print out all the parameters for that event
                System.out.print("\t" + eventList[i][j]);
            }

            System.out.println();
        }
    }

    /**
     * main ---
     * <p>
     * A main method to test the class.
     * </p>
     *
     * @param args
     *            <code>String</code> array of arguments at the command prompt.
     */
    public static void main(String[] args) {
        if (args.length <= 0) {
            // If there are no arguments, then exit.
            System.out.println("Usage:");
            System.out.println("---");
            System.out.println(">java fcsFile <path to FCS file>");

            System.exit(0);
        }

        try {
            // Echo the file we are checking
            System.out.println("Checking \"" + args[0] + "\" ...");

            fcsFile f;

            // Read in the file
            if (args.length > 1) {
                // If there are more than one argument, then extract the events.
                f = new fcsFile(args[0], true);
            } else {
                // Otherwise, don't extract the events.
                f = new fcsFile(args[0], false);
            }

            // Test whether the file is an FCS file
            if (f.isFCS()) {
                // If the file is an FCS file, then print out the file is an FCS
                // file.
                System.out.println("\"" + args[0] + "\" is an FCS file.");

                // Print out the information if the file is an FCS file
                printFCSFile(f);
            } else {
                // Otherwise, print out the file is not an FCS file.
                System.out.println("\"" + args[0] + "\" is not an FCS file.");
            }

            /*
             * // Print out whether the file is an FCS file using the static
             * method if(isFCSFile(f.getFile())) { // If the file is an FCS
             * file, then print out the file is an FCS file.
             * System.out.println("\"" + args[0] + "\" is an FCS file."); } else
             * { // Otherwise, print out the file is not an FCS file.
             * System.out.println("\"" + args[0] + "\" is not an FCS file."); }
             */
        } catch (Exception e) {
            // Print out any exceptions
            System.err.println(e.toString());
        }
    }
}