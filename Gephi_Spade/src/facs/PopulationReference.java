/**
* PopulationReference.java
*
* Tweaked into a local form 11/5/2010 ZB
*
* Cytobank (TM) is server and client software for web-based management, analysis,
* and sharing of flow cytometry data.
*
* Copyright (C) 2009 Cytobank, Inc. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
* Cytobank, Inc.
* 659 Oak Grove Avenue #205
* Menlo Park, CA 94025
*
* http://www.cytobank.org
*/
package facs;

// Import the utility package
import java.io.IOException;
import java.util.*;

/**
* <p>
* A simple class that references a population.
* </p>
*
* <p>
* The class basically describes a population. This allows populations to be
* passed around without incurring the overhead costs of actually loading the
* populations. The class basically maintains four pieces of information: the ID
* of the experiment, the filename, the IDs of the applicable gate sets, and the
* ID of the compensation. With the introduction of the different types of
* populations, the class also maintains the type of the population and the
* number of events, but these are optional. Finally, the class maintains an
* index of a channel, which is also optional. Maintaining all this information
* will allow different channels of different types of populations to be mixed
* together in an aggregation of <code>PopulationReference</code> objects.
* </p>
*
* <p>
* The information maintained by the class were what the original
* <code>Population</code> class maintained, but now that it is importing the
* flow file and performing event calculations, we want to avoid passing the
* actual <code>Population</code> object around as much as possible as it is
* quite memory intensive. Thus, it is necessary to create a descriptor class
* like this one so that the populations can be passed around with less strain
* on the memory system.
* </p>
*
* <p>
* The class is immutable.
* </p>
*
* <p>
* Eventually, the class should probably be a part of the descriptor package.
* </p>
*/
public final class PopulationReference {
    /**
* The empty array of gate set IDs
*/
    private static final int[] EMPTY_GATE_SET_ID_ARRAY = new int[0];

    /**
* The default constant flag of the type of the population
*/
    private static final int DEFAULT_TYPE = -1;

    /**
* The default number of events to get from the flow file
*/
    private static final int DEFAULT_EVENT_COUNT = 0;

    /**
* The default index of the channel
*/
    private static final int DEFAULT_CHANNEL = -1;

    /**
* The ID of the experiment underlying the population
*/
    private final int experimentID;

    /**
* The filename of the flow file underlying the population
*/
    private final String filename;

    /**
* The array of IDs of the applicable gate sets
*/
    private int[] gateSetIDs;

    /**
* The ID of the compensation
*/
    private final int compensationID;

    /**
* The constant flag of the type of the population
*/
    private final int type;

    /**
* The number of events to get from the flow file
*/
    private final int eventCount;

    /**
* The index of the channel
*/
    private final int channel;

    /**
* ***********************************************************************
* ***********************************************************************
* Constant parameter names
*
* These are the names used for XML nodes and querysting parameters.
*
* JSP and other java objects should refer to these values rather than hard
* coding a particular String to make it easier to change these values.
*
* ***********************************************************************
* ***********************************************************************
*/

    /**
* The name of this object
*/
    public static final String POP_REF_PARAM_NAME = "populationReference";

    /**
* The name of the filename
*/
    public static final String FILENAME_PARAM_NAME = "filename";

    /**
* The name of the type
*/
    public static final String TYPE_PARAM_NAME = "type";

    /**
* The name of the pop ref channel
*/
    public static final String POP_REF_CHANNEL_PARAM_NAME = "popRefChannel";

    /**
* The name of the gate set id
*/
    public static final String GATE_SET_IDS_PARAM_NAME = "gateSetID";

    /**
* The name of the event count
*/
    public static final String EVENT_COUNT_PARAM_NAME = "eventCount";

    /**
* The name of the compensation ID
*/
    public static final String COMPENSATION_ID_PARAM_NAME = "compensationID";

    /**
* The name of the experiment ID
*/
    public static final String EXPERIMENT_ID_PARAM_NAME = "experimentID";

    /**
* <p>
* A full constructor for <code>PopulationReference</code>.
* </p>
*
* <p>
* This constructor creates a population reference to a population based on
* the flow file with the filename in the <code>String</code> filename in
* the experiment with ID experimentID compensated with the compensation
* with ID compensationID and gated using the gate sets with IDs in the list
* of IDs of the applicable gate sets gateSetIDs. The type of the population
* is set to type and the number of events to get from the flow file is set
* to eventCount. The index of the channel of the population reference is
* set to channel.
* </p>
*
* @param experimentID
* int ID of the experiment underlying the population.
* @param filename
* <code>String</code> filename of the flow file underlying the
* population.
* @param gateSetIDs
* int array of IDs of the applicable gate sets.
* @param compensationID
* int ID of the compensation.
* @param type
* int constant flag of the type of the population.
* @param eventCount
* int number of events to get from the flow file.
* @param channel
* int index of the channel of the population reference.
*/
    private PopulationReference(int experimentID, String filename, int[] gateSetIDs, int compensationID, int type, int eventCount, int channel) {
        // Set the ID of the experiment underlying the population
        this.experimentID = experimentID;

        // Set the filename of the flow file underlying the population
        this.filename = filename;

        if ((gateSetIDs == null) || (gateSetIDs.length <= 0)) {
            // If the array of IDs of the applicable gate sets is null or empty,
            // so allocate an empty array of IDs of the applicable gate sets.
            this.gateSetIDs = new int[0];
        } else {
            // Otherwise, the array of IDs of the applicable gate sets is not
            // null and not empty, so copy it.
            this.gateSetIDs = new int[gateSetIDs.length];

            // Copy the array of IDs of the applicable gate sets
            System.arraycopy(gateSetIDs, 0, this.gateSetIDs, 0, gateSetIDs.length);
        }

        // Set the ID of the compensation
        this.compensationID = compensationID;

        // Set the constant flag of the type of the population
        this.type = type;

        // Set the number of events to get from the flow file
        this.eventCount = eventCount;

        // Set the index of the channel
        this.channel = channel;
    }

    /**
* <p>
* A constructor for <code>PopulationReference</code> that takes a string
* that is assumed to be a querystring formatted PopulationReference. Any
* parameters found on this querystring are translated into parameters.
* Other values are set to their defaults.
*
* @param queryString
* <code>String</code> a queryString formatted
* PopulationReference.
*/
//    public PopulationReference(String queryString) {
//
//        // Check to make sure querystring is a valid query string
//        if (queryString != null) {
//
//            // Look for each parameter and, if it exists, parse it
//
//            // Set the ID of the experiment underlying the population
//            this.experimentID = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, EXPERIMENT_ID_PARAM_NAME)), -1);
//
//            // Set the filename of the flow file underlying the population
//            this.filename = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, FILENAME_PARAM_NAME)), "no file name");
//
//            // Gate set IDs, need to parse out an array of parameters
//            if (queryString.contains(GATE_SET_IDS_PARAM_NAME)) {
//                // Get the array of gate set IDs
//                String[] gateSetIDStrings = JSPlib.getParameterArray(queryString, GATE_SET_IDS_PARAM_NAME + "[0-9]*?");
//
//                if (gateSetIDStrings == null) {
//                    // If the array of gate set IDs is null, then create an
//                    // empty array of gate set IDs.
//                    gateSetIDs = EMPTY_GATE_SET_ID_ARRAY;
//                } else {
//                    // Otherwise, the array of gate set IDs is not null, then
//                    // parse the gate set IDs.
//                    gateSetIDs = new int[gateSetIDStrings.length];
//
//                    // Loop through the array of gate set IDs parsing each
//                    for (int i = 0; i < gateSetIDStrings.length; i++) {
//                        gateSetIDs[i] = JSPlib.checkStatus(gateSetIDStrings[i], Illustration.UNGATED);
//                    }
//                }
//            } else {
//                gateSetIDs = EMPTY_GATE_SET_ID_ARRAY;
//            }
//
//            // Set the ID of the compensation
//            this.compensationID = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, COMPENSATION_ID_PARAM_NAME)), -1);
//
//            // Set the constant flag of the type of the population
//            this.type = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, TYPE_PARAM_NAME)), DEFAULT_TYPE);
//
//            // Set the number of events to get from the flow file
//            this.eventCount = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, EVENT_COUNT_PARAM_NAME)), DEFAULT_EVENT_COUNT);
//
//            // Set the index of the channel
//            this.channel = JSPlib.checkStatus(JSPlib.decode(JSPlib.getParameter(queryString, POP_REF_CHANNEL_PARAM_NAME)), DEFAULT_CHANNEL);
//
//        } else {
//            this.experimentID = -1;
//            this.filename = null;
//            this.compensationID = -1;
//            this.type = DEFAULT_TYPE;
//            this.eventCount = DEFAULT_EVENT_COUNT;
//            this.channel = DEFAULT_CHANNEL;
//            this.gateSetIDs = EMPTY_GATE_SET_ID_ARRAY;
//        }
//    }

    /**
* <p>
* Returns the ID of the experiment underlying the population.
* </p>
*
* @return int ID of the experiment underlying the population.
*/
    public int getExperimentID() {
        return experimentID;
    }

    /**
* <p>
* Returns the filename of the flow file underlying the population.
* </p>
*
* @return <code>String</code> filename of the flow file underlying the
* population.
*/
    public String getFilename() {
        return filename;
    }

    /**
* <p>
* Returns the number of applicable gate sets.
* </p>
*
* @return int number of applicable gate sets.
*/
    public int getGateSetCount() {
        return gateSetIDs.length;
    }

    /**
* <p>
* Returns an array of IDs of the applicable gate sets.
* </p>
*
* @return int array of IDs of the applicable gate sets.
*/
    public int[] getGateSetIDs() {
        // Allocate an array to hold all the gate set IDs
        int[] gateSetIDArray = new int[gateSetIDs.length];

        // Copy the array of IDs of the applicable gate sets
        System.arraycopy(gateSetIDs, 0, gateSetIDArray, 0, gateSetIDs.length);

        // Return the array of gate set IDs
        return gateSetIDArray;
    }

    /**
* <p>
* Returns the ID of the compensation.
* </p>
*
* @return int ID of the compensation.
*/
    public int getCompensationID() {
        return compensationID;
    }

    /**
* <p>
* Returns the constant flag of the type of the population.
* </p>
*
* @return int constant flag of the type of the population.
*/
    public int getType() {
        return type;
    }

    /**
* <p>
* Returns the number of events to get from the flow file.
* </p>
*
* @return int number of events to get from the flow file.
*/
    public int getEventCount() {
        return eventCount;
    }

    /**
* <p>
* Returns the index of the channel or -1 if the index of the channel was
* not set.
* </p>
*
* @return int index of the channel or -1 if the index of the channel was
* not set.
*/
    public int getChannel() {
        return channel;
    }

    /**
* <p>
* Returns a string encoding the PopulationReference.
* </p>
*
* <p>
* The encoded parameters do not start with a '?' or a '&' so it is suitable
* to be concatenated into an URL as the only parameters or as additional
* parameters. It was designed this way so the client would have the option
* to use the parameters in either capacity.
* </p>
*
* @return <code>String</code> string encoding the PopulationReference.
*/
//    public String getQueryString() {
//
//        // Create a StringBuffer with which to encode the parameters
//        StringBuffer parameters = new StringBuffer();
//
//        // Encode the experiment ID
//        parameters.append(EXPERIMENT_ID_PARAM_NAME + "=" + JSPlib.encode(experimentID + ""));
//
//        // Encode the filename
//        parameters.append("&" + FILENAME_PARAM_NAME + "=" + JSPlib.encode(filename));
//
//        // Encode the ID of the compensation
//        parameters.append("&" + COMPENSATION_ID_PARAM_NAME + "=" + JSPlib.encode(compensationID + ""));
//
//        if (gateSetIDs != null) {
//            // If the array of gate set IDs is not null, then encode them.
//
//            // Loop through the array of gate set IDs
//            for (int i = 0; i < gateSetIDs.length; i++) {
//                parameters.append("&" + GATE_SET_IDS_PARAM_NAME + "=" + JSPlib.encode(gateSetIDs[i] + ""));
//            }
//        }
//
//        // Encode the type
//        parameters.append("&" + TYPE_PARAM_NAME + "=" + JSPlib.encode(type + ""));
//
//        // Encode the event count
//        parameters.append("&" + EVENT_COUNT_PARAM_NAME + "=" + JSPlib.encode(eventCount + ""));
//
//        // Encode the pop ref channel
//        parameters.append("&" + POP_REF_CHANNEL_PARAM_NAME + "=" + JSPlib.encode(channel + ""));
//
//        // Return the String illustration of the StringBuffer
//        return parameters.toString();
//    }

    /**
* <p>
* Returns a string encoding all the parameters of the population along with
* the parameters of the illustration.
* </p>
*
* <p>
* TODO: The illustration should not be called for the encoding
* </p>
*
* <p>
* The encoded parameters do not start with a '?' or a '&' so it is suitable
* to be concatenated into an URL as the only parameters or as additional
* parameters. It was designed this way so the client would have the option
* to use the parameters in either capacity.
* </p>
*
* @param illustration
* <code>Illustration</code> object that sets some of the
* parameters.
* @return <code>String</code> string encoding the population and
* illustration.
*/
//    public String getParameters(Illustration illustration) {
//        if (this == null) {
//            // If the population reference is null, then return the empty
//            // string.
//            return "";
//        }
//
//        // Create a new StringBuffer with which we will build the parameters
//        StringBuffer parameters = new StringBuffer();
//
//        // Get the encoded filename of the current population reference
//        String filename = JSPlib.encode(this.getFilename());
//
//        if ((illustration == null) && (filename != null)) {
//            // If the panel set illustration and the filename are not null, then
//            // use a histogram as the link-through.
//
//            parameters.append(EXPERIMENT_ID_PARAM_NAME + "=");
//            parameters.append(this.getExperimentID());
//
//            parameters.append(FILENAME_PARAM_NAME + "=");
//            parameters.append(filename);
//
//            /**
//* Encode the gate set IDs
//*/
//
//            // Get the array of gate set IDs
//            int[] ids = this.getGateSetIDs();
//
//            // Loop through the array of gate set IDs
//            for (int i = 0; i < ids.length; i++) {
//                parameters.append("&" + GATE_SET_IDS_PARAM_NAME + "=");
//                parameters.append(ids[i]);
//            }
//
//            parameters.append("&" + COMPENSATION_ID_PARAM_NAME + "=");
//            parameters.append(this.getCompensationID());
//            // parameters.append("&plotType=histogramX&barePlot=false&axisBins=128&axisWidth=128&aspectRatio=1.0&drawRAllRegions=pc&drawRAllRegions=rg&showSAllRegions=none&labelStyle=power&drawR=-2_al,st,sl&univPlotSettings=true");
//        } else if (illustration != null) {
//            // If the illustration is not null, then get the parameters for the
//            // link-through.
//
//            // Get the channel of the population reference
//            int popRefChannel = this.getChannel();
//
//            int variableChannel = illustration.getVariableChannel();
//
//            if (!illustration.channelsAreVariable()) {
//                parameters.append(illustration.encode(this.getExperimentID(), this.getFilename(), this.getCompensationID(), this.getGateSetIDs(), illustration
//                        .getXChannel(), illustration.getYChannel(), illustration.getZChannel()));
//            } else if (illustration.isPseudo3DPlot()) {
//                if (variableChannel == Illustration.VARY_X) {
//                    // Varying the x channel, rare
//                    parameters.append(illustration.encode(this.getExperimentID(), this.getFilename(), this.getCompensationID(), this.getGateSetIDs(),
//                            popRefChannel, illustration.getYChannel(), illustration.getZChannel()));
//                } else if (variableChannel == Illustration.VARY_Y) {
//                    // Varying the y channel, rare
//                    parameters.append(illustration.encode(this.getExperimentID(), this.getFilename(), this.getCompensationID(), this.getGateSetIDs(),
//                            illustration.getXChannel(), popRefChannel, illustration.getZChannel()));
//                } else {
//                    // Varying the z channel
//                    parameters.append(illustration.encode(this.getExperimentID(), this.getFilename(), this.getCompensationID(), this.getGateSetIDs(),
//                            illustration.getXChannel(), illustration.getYChannel(), popRefChannel));
//                }
//            } else if (illustration.is1DPlot()) {
//                if (variableChannel == Illustration.VARY_X) {
//                    // Varying the x channel, the only option for 1D plots
//                    parameters.append(illustration.encode(this.getExperimentID(), this.getFilename(), this.getCompensationID(), this.getGateSetIDs(),
//                            popRefChannel, illustration.getYChannel(), illustration.getZChannel()));
//                }
//            } else {
//                if (variableChannel == Illustration.VARY_X) {
//                    // Varying the x channel, rare
//                    parameters.append(illustration.encode(this.getExperimentID(), this.getFilename(), this.getCompensationID(), this.getGateSetIDs(),
//                            popRefChannel, illustration.getYChannel(), illustration.getZChannel()));
//                } else {
//                    parameters.append(illustration.encode(this.getExperimentID(), this.getFilename(), this.getCompensationID(), this.getGateSetIDs(),
//                            illustration.getXChannel(), popRefChannel, illustration.getZChannel()));
//                }
//            }
//        }
//
//        // Return the String illustration of the StringBuffer
//        return parameters.toString();
//    }

    /**
* <p>
* Returns whether the population referenced by this population reference is
* the same population as the population referenced by the population
* reference in the <code>PopulationReference</code> object ref.
* </p>
*
* <p>
* The comparison is done by comparing all the fields of the population
* references with the exception of the index of the channel. If all the
* fields except the index of the channel are the same, then the populations
* referenced by the population references are the same.
* </p>
*
* <p>
* This method is different from an equals comparison in that the indices of
* the channels of the two population references are not compared.
* </p>
*
* @param ref
* <code>PopulationReference</code> object to the population
* reference with which to compare.
* @return boolean flag indicating whether the population referenced by this
* population reference is the same population as the population
* referenced by the population reference in the
* <code>PopulationReference</code> object ref.
*/
    public boolean isSamePopulation(PopulationReference ref) {
        if (ref == null) {
            // If the population reference is null, then return false.
            return false;
        }

        /**
* Compare the fields --- Note: We do not perform the comparisons in
* order because some comparisons (like int comparisons) are cheaper and
* if we can figure out that the referenced populations are different
* using those first, we can save quite a bit of computation.
*/

        if (experimentID != ref.experimentID) {
            // If the IDs of the experiments are not equal, then return false.
            return false;
        }

        if (gateSetIDs.length != ref.gateSetIDs.length) {
            // If the numbers of applicable gate sets are not equal, then return
            // false.
            return false;
        }

        if (compensationID != ref.compensationID) {
            // If the IDs of the compensations are not equal, then return false.
            return false;
        }

        if (type != ref.type) {
            // If the constant flags of the types of the populations are not
            // equal, then return false.
            return false;
        }

        if (eventCount != ref.eventCount) {
            // If the numbers of events to get from the flow files are not
            // equal, then return false.
            return false;
        }

        /**
* Compare the IDs of the applicable gate sets --- Note: We have already
* established that the numbers of applicable gate sets are equal at
* this point.
*/

        // Loop through the array of IDs of the applicable gate sets
        for (int i = 0; i < gateSetIDs.length; i++) {
            if (gateSetIDs[i] != ref.gateSetIDs[i]) {
                // If the IDs of the current applicable gate sets are not equal,
                // then return false.
                return false;
            }
        }

        /**
* Compare the filenames of the flow files underlying the populations
*/

        if (((filename == null) && (ref.filename != null)) || ((filename != null) && (ref.filename == null))
                || ((filename != null) && (!filename.equals(ref.filename)))) {
            // If the filenames of the flow files underlying the populations are
            // not equal, then return false.
            return false;
        }

        // At this point, the fields (with the exception of the index of the
        // channel) are the same, so the referenced populations are the same.
        return true;
    }

    /**
* <p>
* Returns the population referenced by this population reference or null if
* the population cannot be created.
* </p>
*
* @return <code>Population</code> object to the population referenced by
* this population reference or null if the population cannot be
* created.
* @throws IOException
*/
//    public Population getPopulation() throws IOException {
//        if ((experimentID <= 0) || (filename == null) || (filename.length() <= 0)) {
//            // If the ID of the experiment is invalid or the filename is null or
//            // empty, then quit.
//            return null;
//        }
//
//        // Get the array of IDs of the applicable gate sets
//        int[] gateSetIDArray = getGateSetIDs();
//
//        if ((type > 0) && (eventCount > 0)) {
//            // If the constant flag of the type of the population is valid and
//            // the number of events to get from the flow file is greater than 0,
//            // then get the partial population.
//            return PartialPopulation.getPopulation(Experiment.getExperimentWithID(experimentID), filename, gateSetIDArray, compensationID, type, eventCount);
//        } else {
//            // Otherwise, get the population.
//            return Population.getPopulation(Experiment.getExperimentWithID(experimentID), filename, gateSetIDArray, compensationID);
//        }
//    }

    /**
* Factory methods
*/

    /**
* <p>
* Returns a population reference to the population based on the input
* parameters.
* </p>
*
* <p>
* This method creates a population reference to a population based on the
* flow file with the filename in the <code>String</code> filename in the
* experiment with ID experimentID that is ungated and uncompensated. The
* index of the channel of the population reference is set to -1.
* </p>
*
* @param experimentID
* int ID of the experiment underlying the population.
* @param filename
* <code>String</code> filename of the flow file underlying the
* population.
*/
//    public static PopulationReference getPopulationReference(int experimentID, String filename) {
//        return getPopulationReference(experimentID, filename, EMPTY_GATE_SET_ID_ARRAY, Compensation.UNCOMPENSATED, DEFAULT_TYPE, DEFAULT_EVENT_COUNT,
//                DEFAULT_CHANNEL);
//    }

    /**
* <p>
* Returns a population reference to the population based on the input
* parameters.
* </p>
*
* <p>
* This method creates a population reference to a population based on the
* flow file with the filename in the <code>String</code> filename in the
* experiment with ID experimentID that is gated using the gate set with ID
* gateSetID and compensated using the compensation with ID compensationID.
* The index of the channel of the population reference is set to -1.
* </p>
*
* @param experimentID
* int ID of the experiment underlying the population.
* @param filename
* <code>String</code> filename of the flow file underlying the
* population.
* @param gateSetID
* int ID of the gate set of the population.
* @param compensationID
* int ID of the compensation.
*/
    public static PopulationReference getPopulationReference(int experimentID, String filename, int gateSetID, int compensationID) {
        return getPopulationReference(experimentID, filename, gateSetID, compensationID, DEFAULT_TYPE, DEFAULT_EVENT_COUNT, DEFAULT_CHANNEL);
    }

    /**
* <p>
* Returns a population reference to the population based on the input
* parameters.
* </p>
*
* <p>
* This method creates a population reference to a population based on the
* flow file with the filename in the <code>String</code> filename in the
* experiment with ID experimentID that is gated using the gate set with ID
* gateSetID and compensated using the compensation with ID compensationID.
* The index of the channel of the population reference is set to -1.
* </p>
*
* @param experimentID
* int ID of the experiment underlying the population.
* @param filename
* <code>String</code> filename of the flow file underlying the
* population.
* @param gateSetID
* int ID of the gate set of the population.
* @param compensationID
* int ID of the compensation.
* @param type
* int constant flag of the type of the population.
* @param eventCount
* int number of events to get from the flow file.
*/
    public static PopulationReference getPopulationReference(int experimentID, String filename, int gateSetID, int compensationID, int type, int eventCount) {
        return getPopulationReference(experimentID, filename, gateSetID, compensationID, type, eventCount, DEFAULT_CHANNEL);
    }

    /**
* <p>
* Returns a population reference to the population based on the input
* parameters.
* </p>
*
* <p>
* This method creates a population reference to a population based on the
* flow file with the filename in the <code>String</code> filename in the
* experiment with ID experimentID that is gated using the gate set with ID
* gateSetID and compensated using the compensation with ID compensationID.
* The index of the channel of the population reference is set to channel.
* </p>
*
* @param experimentID
* int ID of the experiment underlying the population.
* @param filename
* <code>String</code> filename of the flow file underlying the
* population.
* @param gateSetID
* int ID of the gate set of the population.
* @param compensationID
* int ID of the compensation.
* @param channel
* int index of the channel of the population reference.
*/
    public static PopulationReference getPopulationReference(int experimentID, String filename, int gateSetID, int compensationID, int channel) {
        return getPopulationReference(experimentID, filename, gateSetID, compensationID, DEFAULT_TYPE, DEFAULT_EVENT_COUNT, channel);
    }

    /**
* <p>
* Returns a population reference to the population based on the input
* parameters.
* </p>
*
* <p>
* This method creates a population reference to a population based on the
* flow file with the filename in the <code>String</code> filename in the
* experiment with ID experimentID that is gated using the gate set with ID
* gateSetID and compensated using the compensation with ID compensationID.
* The index of the channel of the population reference is set to channel.
* </p>
*
* @param experimentID
* int ID of the experiment underlying the population.
* @param filename
* <code>String</code> filename of the flow file underlying the
* population.
* @param gateSetID
* int ID of the gate set of the population.
* @param compensationID
* int ID of the compensation.
* @param type
* int constant flag of the type of the population.
* @param eventCount
* int number of events to get from the flow file.
* @param channel
* int index of the channel of the population reference.
*/
    public static PopulationReference getPopulationReference(int experimentID, String filename, int gateSetID, int compensationID, int type, int eventCount,
            int channel) {
        if (gateSetID > 0) {
            // If the ID of the gate set is valid, then create an array of gate
            // set IDs using the ID of the gate set.
            int[] ids = new int[1];
            ids[0] = gateSetID;

            // Create the population reference and return it
            return getPopulationReference(experimentID, filename, ids, compensationID, type, eventCount, channel);
        } else {
            // Otherwise, the ID of the gate set is invalid, so use an empty
            // array of gate set IDs.
            return getPopulationReference(experimentID, filename, EMPTY_GATE_SET_ID_ARRAY, compensationID, type, eventCount, channel);
        }
    }

    /**
* <p>
* Returns a population reference to the population based on the input
* parameters.
* </p>
*
* <p>
* This method creates a population reference to a population based on the
* flow file with the filename in the <code>String</code> filename in the
* experiment with ID experimentID that is gated using the gate sets with
* IDs in the array of IDs of the applicable gate sets gateSetIDs and
* compensated using the compensation with ID compensationID. The index of
* the channel of the population reference is set to channel.
* </p>
*
* @param experimentID
* int ID of the experiment underlying the population.
* @param filename
* <code>String</code> filename of the flow file underlying the
* population.
* @param gateSetIDs
* int array of IDs of the applicable gate sets.
* @param compensationID
* int ID of the compensation.
* @param channel
* int index of the channel of the population reference.
*/
    public static PopulationReference getPopulationReference(int experimentID, String filename, int[] gateSetIDs, int compensationID, int channel) {
        return getPopulationReference(experimentID, filename, gateSetIDs, compensationID, DEFAULT_TYPE, DEFAULT_EVENT_COUNT, channel);
    }

    /**
* <p>
* Returns a population reference to the population based on the input
* parameters.
* </p>
*
* <p>
* This method creates a population reference to a population based on the
* flow file with the filename in the <code>String</code> filename in the
* experiment with ID experimentID that is gated using the gate sets with
* IDs in the array of IDs of the applicable gate sets gateSetIDs and
* compensated using the compensation with ID compensationID. The index of
* the channel of the population reference is set to channel.
* </p>
*
* @param experimentID
* int ID of the experiment underlying the population.
* @param filename
* <code>String</code> filename of the flow file underlying the
* population.
* @param gateSetIDs
* int array of IDs of the applicable gate sets.
* @param compensationID
* int ID of the compensation.
* @param type
* int constant flag of the type of the population.
* @param eventCount
* int number of events to get from the flow file.
* @param channel
* int index of the channel of the population reference.
*/
    public static PopulationReference getPopulationReference(int experimentID, String filename, int[] gateSetIDs, int compensationID, int type, int eventCount,
            int channel) {
        // Create the list of IDs of the applicable gate sets
        ArrayList<Integer> gateSetIDList = new ArrayList<Integer>();

        if ((gateSetIDs != null) && (gateSetIDs.length > 0)) {
            // If the array of IDs of the applicable gate sets is not null and
            // not empty, then check it.

            /**
* The array of IDs of the applicable gate sets is checked here to
* ensure all the IDs are unique and sorted.
*/
            Integer integer;

            // Loop through the array of IDs of the applicable gate sets
            for (int i = 0; i < gateSetIDs.length; i++) {
                if (gateSetIDs[i] > 0) {
                    // If the ID of the current applicable gate set is valid,
                    // then add it to the list of IDs of the applicable gate
                    // sets.

                    // Get the Integer object of the ID of the current
                    // applicable gate set
                    integer = Integer.valueOf(gateSetIDs[i]);

                    if (!gateSetIDList.contains(integer)) {
                        // If the list of IDs of the applicable gate sets does
                        // not contain the ID of the current applicable gate
                        // set, then add it.
                        gateSetIDList.add(integer);
                    }
                }
            }

            if (gateSetIDList.size() > 1) {
                // If there are more than one element in the list of IDs of the
                // applicable gate sets, then sort it.
                Collections.sort(gateSetIDList);
            }
        }

        // Allocate the array of IDs of the applicable gate sets
        int[] gateSetIDArray = new int[gateSetIDList.size()];

        // Loop through the list of IDs of the applicable gate sets
        for (int i = 0; i < gateSetIDList.size(); i++) {
            gateSetIDArray[i] = ((Integer) gateSetIDList.get(i)).intValue();
        }

        // Create the population reference and return it
        return new PopulationReference(experimentID, filename, gateSetIDArray, compensationID, type, eventCount, channel);
    }

    /**
* Testing methods
*/

    /**
* <p>
* Prints the fields of the population reference for testing and debugging.
* </p>
*/
    private void print() {
        System.out.println("Experiment ID: " + getExperimentID());
        System.out.println("Filename: " + getFilename());
        System.out.println("Compensation ID: " + getCompensationID());

        System.out.println("Applicable Gate Set IDs (" + getGateSetCount() + "):");
        System.out.println("---");

        // Get the array of IDs of the applicable gate sets
        int[] idArray = getGateSetIDs();

        // Loop through the array of IDs of the applicable gate sets
        for (int i = 0; i < idArray.length; i++) {
            System.out.println(idArray[i]);
        }

        System.out.println("Type: " + getType());
        System.out.println("Event Count: " + getEventCount());
        System.out.println("Channel: " + getChannel());
    }

    /**
* <p>
* A main method to test the class.
* </p>
*
* @param args
* <code>String</code> array of arguments at the command prompt.
*/
//    public static void main(String[] args) {
//        PopulationReference popRef = getPopulationReference(5, "blah.txt", 1, -1);
//        popRef.print();
//        System.out.println();
//
//        popRef = getPopulationReference(5, "blah.txt");
//        popRef.print();
//        System.out.println();
//
//        popRef = getPopulationReference(5, "blah.txt", -1, 1, 5);
//        popRef.print();
//        System.out.println();
//
//        popRef = getPopulationReference(5, "blah.txt", 1, -1, 5);
//        popRef.print();
//        System.out.println("Is same to self? " + popRef.isSamePopulation(popRef));
//        System.out.println();
//
//        PopulationReference popRef1 = getPopulationReference(5, "blah.txt", 1, -1, 5);
//        popRef1.print();
//        System.out.println("Is same to self? " + popRef1.isSamePopulation(popRef1));
//        System.out.println("Is same? " + popRef.isSamePopulation(popRef1));
//        System.out.println();
//
//        PopulationReference popRef2 = getPopulationReference(5, "blah.txt", 1, -1, 2, 3, 5);
//        popRef2.print();
//        System.out.println("Is same to self? " + popRef2.isSamePopulation(popRef2));
//        System.out.println("Is same? " + popRef.isSamePopulation(popRef2));
//        System.out.println("Is same? " + popRef2.isSamePopulation(popRef1));
//        System.out.println();
//    }
}