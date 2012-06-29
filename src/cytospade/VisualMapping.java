package cytospade;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.logger.CyLogger;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.*;
import cytospade.SpadeContext.NormalizationKind;
import cytospade.SpadeContext.SymmetryType;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.math.stat.descriptive.rank.Percentile;

/**
 *
 * @author mlinderm
 */
public class VisualMapping {

    private NormalizationKind rangeKind;
    private SymmetryType symmetryType;
    private HashMap globalRanges;
    //Markers used for size and color "mapping"
    private String sizeMarker;
    private String colorMarker;

    public VisualMapping() {
        globalRanges = null;
    }

    public VisualMapping(File globalBoundaryFile) {
        globalRanges = new HashMap();
        readBoundaries(globalBoundaryFile);
    }

    private static class Range {
        private double min;
        private double max;

        public Range(double min_a, double max_a) {
            min = min_a;
            max = max_a;
        }

        public void setMax(double Max) {
            max = Max;
        }

        public void setMin(double Min) {
            min = Min;
        }

        public double getMax() {
            if (max < min) {
                return 100.0; //sane default if no valid nodes
            } else {
                return max;
            }
        }

        public double getMin() {
            if (max < min) {
                return 0.0; //sane default if no valid nodes
            } else {
                return min;
            }
        }

    }
    
    public boolean globalRangeAvailable() { 
        return (globalRanges != null) && (globalRanges.size() > 0);
    }

    /**
     * Set markers used in VisualMapping
     * @param sizeMarker Marker to use for size calculator
     * @param colorMarker Marker to use for color calculator
     * @param rangeKind global or local
     * @param symmetryType Asymmetric or symmetric
     * @throws IllegalArgumentException If markers are non-numeric
     */
    public void setCurrentMarkersAndRangeKind(String sizeMarker, String colorMarker, NormalizationKind rangeKind, SymmetryType symmetryType) throws IllegalArgumentException {
        if (!isNumericAttribute(sizeMarker)) {
          throw new IllegalArgumentException("sizeMarker is non-numeric");
        }
        this.sizeMarker = sizeMarker;

        if (!isNumericAttribute(colorMarker)) {
            throw new IllegalArgumentException("colorMarker is non-numeric");
        }
        this.colorMarker = colorMarker;

        this.rangeKind = rangeKind;

        this.symmetryType = symmetryType;
    }
    
    public String getCurrentSizeMarker() { return sizeMarker; }
    public String getCurrentColorMarker() { return colorMarker; }

    /**
     * Create a size calculator based on current sizeMarker
     * @return "SPADE Size Calculator" with size continuous mapper
     */
    public Calculator createSizeCalculator() {
        Range rng = getAttributeRange(sizeMarker);
        double rmin = rng.getMin();
        double rmax = rng.getMax();

        VisualPropertyType type = VisualPropertyType.NODE_SIZE;
        final Object defaultObj = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());

        ContinuousMapping cm = new ContinuousMapping(defaultObj.getClass(), sizeMarker);
        Interpolator numToSize = new LinearNumberToNumberInterpolator();
        cm.setInterpolator(numToSize);

        BoundaryRangeValues bv0 = new BoundaryRangeValues(28, 28, 28);
        BoundaryRangeValues bv1 = new BoundaryRangeValues(72, 72, 72);

        cm.addPoint(rmin, bv0);
        cm.addPoint(rmax, bv1);

        return new BasicCalculator("SPADE Size Calculator", cm, VisualPropertyType.NODE_SIZE);
    }

    /**
     * Create a color calculator based on current colorMarker
     * @return "SPADE Color Calculator" with color continuous mapper
     */
    public Calculator createColorCalculator() {
        Range rng = getAttributeRange(colorMarker);
        double rmin = rng.getMin();
        double rmax = rng.getMax();

        //Handle symmetric ranges
        if (this.symmetryType == SymmetryType.SYMMETRIC) {
            rmax = Math.max(Math.abs(rng.max), Math.abs(rng.min));
            rmin = -1 * rmax;
        }
        
        VisualPropertyType type = VisualPropertyType.NODE_FILL_COLOR;
        final Object defaultObj = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());

        ContinuousMapping cm = new ContinuousMapping(defaultObj.getClass(), colorMarker);
        Interpolator numToColor = new LinearNumberToColorInterpolator();
        cm.setInterpolator(numToColor);

        Color[] colors = new Color[7];
        colors[0] = new Color(0,0,153);
        colors[1] = new Color(0,0,255);
        colors[2] = new Color(0,255,255);
        colors[3] = new Color(51,255,0);
        colors[4] = new Color(255,255,0);
        colors[5] = new Color(255,0,51);
        colors[6] = new Color(153,0,0);

        double step = (rmax - rmin) / (colors.length - 1);
        for (int i=0; i<colors.length; i++) {
            cm.addPoint(rmin + step*i, new BoundaryRangeValues(colors[i],colors[i],colors[i]));
        }

        //TODO try to trigger the VizMapper panel event listener so the graphic there updates.

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
        csBox.removeAllItems();
        CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
        String[] names = cyNodeAttrs.getAttributeNames();
        Arrays.sort(names);
        for (String name : names) {
            if (isNumericAttribute(name) && cyNodeAttrs.getUserVisible(name)) {
                csBox.addItem(name);
            }
        }
    }

    private Range getAttributeRange(String attrID) {
        Range range;
        if ((rangeKind == NormalizationKind.GLOBAL) && ((range = (Range)globalRanges.get(attrID)) != null)) {
            return range;
        }

        // Either local, or could not find attribute in global list
        double[] values = new double[Cytoscape.getCurrentNetwork().getNodeCount()];
        int value_idx = 0;

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
                values[value_idx] = value;
                value_idx++;
            }
        }

        values = Arrays.copyOf(values, value_idx);
        Percentile pctile = new Percentile();
        
        return new Range(
            pctile.evaluate(values, 2.0),  // TODO: Make these values controllable
            pctile.evaluate(values, 98.0)
            );
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
