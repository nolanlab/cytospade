
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.logger.CyLogger;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.*;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mlinderm
 */
public class VisualMapping {

    public enum RangeKind { LOCAL, GLOBAL }

    public VisualMapping() {
        globalRanges = null;
    }
    public VisualMapping(File globalBoundaryFile) {
        globalRanges = new HashMap();
        readBoundaries(globalBoundaryFile);
    }

    
    public boolean globalRangeAvailable() { 
        return (globalRanges != null) && (globalRanges.size() > 0);
    }


    /**
     * Set markers used in VisualMapping
     * @param sizeMarker Marker to use for size calculator
     * @param colorMarker Marker to use for color calculator
     * @throws IllegalArgumentException If markers are non-numeric
     */
    public void setCurrentMarkersAndRangeKind(String sizeMarker, String colorMarker, RangeKind rangeKind) throws IllegalArgumentException {
        if (!isNumericAttribute(sizeMarker)) {
          throw new IllegalArgumentException("sizeMarker is non-numeric");
        }
        this.sizeMarker = sizeMarker;

        if (!isNumericAttribute(colorMarker)) {
            throw new IllegalArgumentException("colorMarker is non-numeric");
        }
        this.colorMarker = colorMarker;

        this.rangeKind = rangeKind;
    }
    
    public String getCurrentSizeMarker() { return sizeMarker; }
    public String getCurrentColorMarker() { return colorMarker; }


    /**
     * Create a size calculator based on current sizeMarker
     * @return "SPADE Size Calculator" with size continuous mapper
     */
    public Calculator createSizeCalculator() {
        Range rng = getAttributeRange(sizeMarker);
        if (rng.max <= rng.min) {  // Sane defaults if no valid nodes
            rng.min = 0.0;
            rng.max = 100.0;
        }

        VisualPropertyType type = VisualPropertyType.NODE_SIZE;
        final Object defaultObj = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());

        ContinuousMapping cm = new ContinuousMapping(defaultObj, ObjectMapping.NODE_MAPPING);
        cm.setControllingAttributeName(sizeMarker, Cytoscape.getCurrentNetwork(), false);
        Interpolator numToSize = new LinearNumberToNumberInterpolator();
        cm.setInterpolator(numToSize);

        BoundaryRangeValues bv0 = new BoundaryRangeValues(10, 10, 10);
        BoundaryRangeValues bv1 = new BoundaryRangeValues(10, 30, 30);
        BoundaryRangeValues bv2 = new BoundaryRangeValues(150, 150, 150);

        cm.addPoint(0, bv0);
        cm.addPoint(rng.min, bv1);
        cm.addPoint(rng.max, bv2);

        return new BasicCalculator("SPADE Size Calculator", cm, VisualPropertyType.NODE_SIZE);
    }

    /**
     * Create a color calculator based on current colorMarker
     * @return "SPADE Color Calculator" with color continuous mapper
     */
    public Calculator createColorCalculator() {
        Range rng = getAttributeRange(colorMarker);
        if (rng.max <= rng.min) {  // Sane defaults if no valid nodes
            rng.min = 0.0;
            rng.max = 100.0;
        }
        
        VisualPropertyType type = VisualPropertyType.NODE_FILL_COLOR;
        final Object defaultObj = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());

        ContinuousMapping cm = new ContinuousMapping(defaultObj, ObjectMapping.NODE_MAPPING);
        cm.setControllingAttributeName(colorMarker, Cytoscape.getCurrentNetwork(), false);
        Interpolator numToColor = new LinearNumberToColorInterpolator();
        cm.setInterpolator(numToColor);

        //Color underColor = new Color(0,0,0);
        Color[] colors = new Color[7];
        colors[0] = new Color(0,0,153);
        colors[1] = new Color(0,0,255);
        colors[2] = new Color(0,255,255);
        colors[3] = new Color(51,255,0);
        colors[4] = new Color(255,255,0);
        colors[5] = new Color(255,0,51);
        colors[6] = new Color(153,0,0);
        //Color overColor = new Color(255,255,255);

        double step = (rng.max - rng.min) / (colors.length-1);
        for (int i=0; i<colors.length; i++) {
            cm.addPoint(rng.min + step*i, new BoundaryRangeValues(colors[i],colors[i],colors[i]));
        }

        return new BasicCalculator("SPADE Color Calculator", cm, VisualPropertyType.NODE_FILL_COLOR);
    }

    /**
     * Checks if attribute is numeric, i.e. integer or floating point
     * @param attrID Attribute to be checked
     * @return true if numeric
     */
    public static boolean isNumericAttribute(String attrID) {
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        switch(cyNodeAttrs.getType(attrID)) {
            default: return false;
            case CyAttributes.TYPE_FLOATING:
            case CyAttributes.TYPE_INTEGER:
                return true;
        }
    }

    /**
     * Populates a JComboBox will all numeric attributes of current network
     * @param csBox - JComboBox to populate
     */
    public static void populateNumericAttributeComboBox(javax.swing.JComboBox csBox) {
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        for (String name : cyNodeAttrs.getAttributeNames()) {
            if (isNumericAttribute(name)) {
                System.err.println(name);
                csBox.addItem(name);
            }
        }
    }

    private RangeKind rangeKind;
    private HashMap globalRanges;

    /**
     * Markers used for size and color "mapping"
     */
    private String sizeMarker;
    private String colorMarker;

    private static class Range {
        double min;
        double max;
        public Range(double min_a, double max_a) {
            min = min_a;
            max = max_a;
        }
    }

    private Range getAttributeRange(String attrID) {
        Range range;
        if ((rangeKind == RangeKind.GLOBAL) && ((range = (Range)globalRanges.get(attrID)) != null)) {
            return range;
        }
        // Either local, or could not find attribute in global list

        // Initialize min and max prior to scanning the nodes
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        byte attrType = cyNodeAttrs.getType(attrID);

        Iterator<CyNode> it = Cytoscape.getCurrentNetwork().nodesIterator();
        while (it.hasNext()) {
            giny.model.Node node = (giny.model.Node) it.next();
            // Ignore non-numeric nodes
            String nodeID = node.getIdentifier();
            if (cyNodeAttrs.hasAttribute(nodeID, attrID)) {
                Double value;
                if (attrType == CyAttributes.TYPE_INTEGER) {
                    value = cyNodeAttrs.getIntegerAttribute(nodeID, attrID).doubleValue();
                } else if (attrType == CyAttributes.TYPE_FLOATING) {
                    value = cyNodeAttrs.getDoubleAttribute(nodeID, attrID);
                } else {
                    continue;
                }
                min = Math.min(value, min);
                max = Math.max(value, max);
            }
        }
        return new Range(min,max);
    }

     private void readBoundaries(File boundaryFile) {
        try {

            BufferedReader br = new BufferedReader(new FileReader(boundaryFile.getAbsolutePath()));
            String   read;

            while ((read = br.readLine()) != null) {
                // Line: attribute 0% min% max% 100%
                String[] vals = read.split(" ");
                globalRanges.put(vals[0].replaceAll("\"",""), new Range(Double.parseDouble(vals[2]),Double.parseDouble(vals[3])));
            }

        } catch (FileNotFoundException ex) {
            CyLogger.getLogger(CytoSpade.class.getName()).error(null, ex);
            globalRanges = null;
        } catch (IOException ex) {
            CyLogger.getLogger(CytoSpade.class.getName()).error(null, ex);
            globalRanges = null;
        }
        CyLogger.getLogger(CytoSpade.class.getName()).info("Loaded ranges from global_boundaries.table");
    }

}
