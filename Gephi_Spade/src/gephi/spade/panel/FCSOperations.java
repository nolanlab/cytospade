/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gephi.spade.panel;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.stat.inference.TTestImpl;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

import org.gephi.graph.api.*;
import org.gephi.project.api.ProjectController;
//import org.gephi.visualization.api.VisualizationController;
import org.openide.util.Lookup;
import org.gephi.visualization.apiimpl.ModelImpl;
//import org.gephi.visualization.opengl.AbstractEngine;



public class FCSOperations {

    private fcsFile fcsInputFile = null;
    //private SpadeContext spadeCxt;
    private Array2DRowRealMatrix eventsInitl = null;

    private int numNodesSelected = 0;
    private static Array2DRowRealMatrix eventsSlctd = null;

    
    public FCSOperations(){}
    
    public FCSOperations(File inputFile) throws FileNotFoundException, IOException {
        this(new fcsFile(inputFile, true));
    }
    
    public FCSOperations(fcsFile inputFile) throws FileNotFoundException, IOException{
        if (inputFile.length() == 0){
            
        }
        fcsInputFile = inputFile;
        eventsInitl = new Array2DRowRealMatrix(fcsInputFile.getCompensatedEventList());
    }

     public fcsFile getFCSFile() {
        return fcsInputFile;
    }

    public String getChannelShortName(int i) {
        return fcsInputFile.getChannelShortName(i);
    }

    public int getChannelCount() {
        return fcsInputFile.getChannelCount();
    }
    
    public int getEventCount () {
        return fcsInputFile.getEventCount();
    }

    public double[] getEvents(String channel) {
        this.updateSelectedNodes();
        return eventsInitl.getDataRef()[fcsInputFile.getChannelIdFromShortName(channel)];
    }

    public double getEventMax(String channel) {
        return fcsInputFile.getChannelRange(fcsInputFile.getChannelIdFromShortName(channel));
    }
    
    public void updateSelectedNodes() {
        //Get the selected nodes
        int[] selectedClust = getSelectedNodes();

        numNodesSelected = selectedClust.length;
        if (numNodesSelected == 0) {
            eventsSlctd = null;
        } else {
            eventsSlctd = populateSelectedEvents(selectedClust);
        }
    }
    
    public int getSelectedNodesCount() {
        //return this.numNodesSelected;
            return 0;
        
    }

    public int getSelectedEventCount() {
        return eventsSlctd == null ? 0 : eventsSlctd.getColumnDimension();
    }

   public double[] getSelectedEvents(String channel) {  
            if (getSelectedNodesCount() > 0){
               return eventsSlctd.getDataRef()[fcsInputFile.getChannelIdFromShortName(channel)];
            }else{ return new double[0]; }
        
    
            /*
        return getSelectedNodesCount() > 0 ?
            eventsSlctd.getDataRef()[fcsInputFile.getChannelIdFromShortName(channel)] :
            new double[0];
            */
    }



    /**
     * Statistical Tests
     */

    public class AttributeValuePair implements Comparable {

        public double value;
        public String attribute;

        public AttributeValuePair(String name, double value) {
            this.attribute = name;
            this.value = value;
        }

        public int compareTo(Object t) {
            AttributeValuePair rhs = (AttributeValuePair) t;
            if (Math.abs(this.value) < Math.abs(rhs.value)) {
                return -1;
            } else if (Math.abs(this.value) > Math.abs(rhs.value)) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public List<AttributeValuePair> computeTStat() {
        ArrayList<AttributeValuePair> stats = new ArrayList<AttributeValuePair>();
        for (int i = 0; i < fcsInputFile.getNumChannels(); i++) {
            String name = fcsInputFile.getChannelShortName(i);
            if (name.contentEquals("Time") ||
                name.contentEquals("time") ||
                name.contentEquals("cluster") ||
                name.contentEquals("density")) {
                continue;
            }
            stats.add(new AttributeValuePair(name, tTest(eventsSlctd, eventsInitl, i)));
        }
        Collections.sort(stats);
        Collections.reverse(stats);
        return stats;
    }

    /**
     * Get selected nodes
     */
    private int[] getSelectedNodes() {
        //ArrayList<CyNode> selectedNodes = new ArrayList<CyNode>();
        
        //ArrayList<Node> selectedNodes = new ArrayList<Node>();
        //CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
        //CyNetwork currentNetwork = spadeCxt.adapter.getCyApplicationManager().getCurrentNetwork();
        
        //for (CyNode node : (Set<CyNode>) currentNetwork.getSelectedNodes()) {
        //for (CyNode node : (Set<CyNode>) currentNetwork.getNodeList()) {
        //CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
        //for (CyNode node : (Set<CyNode>) currentNetwork.getSelectedNodes()) {
        //for (CyNode node : (List<CyNode>) CyTableUtil.getNodesInState(currentNetwork,"selected",true)) {
        //AbstractEngine engine = VizController.getInstance().getEngine();
        //VisualizationController vc = Lookup.getDefault().lookup(VisualizationController.class);
        
        /*
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        Graph graph = graphController.getModel().getGraphVisible();
        
        
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        org.gephi.project.api.Workspace workspace = pc.getCurrentWorkspace();
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        GraphModel gm = gc.getModel(workspace);
        Graph graph = gm.getGraph();
        */
        ArrayList<Node> selectedNodes = new ArrayList<Node>();
        ModelImpl[] model = Lookup.getDefault().lookup(ModelImpl[].class);
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        org.gephi.project.api.Workspace workspace = pc.getCurrentWorkspace();
        
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        GraphModel gm = gc.getModel(workspace);
        Graph graph = gm.getGraph();
        
        //ModelImpl[] selectedNodeModels = engine.getSelectedObjects(AbstractEngine.CLASS_NODE);
        NodeIterable ni = graph.getNodes();
        Node[] nodes = ni.toArray();
        /*
        for (int i = 0; i < selectedNodeModels.length; i++) {
            Node node = ((NodeData) selectedNodeModels[i].getObj()).getNode(graph.getView().getViewId());
            if (node != null) {
                selectedNodes.add(node);
            }
        }
        */
       
        /*
        for (Node node : (List<Node>) CyTableUtil.getNodesInState(currentNetwork,"selected",true)) {
//            GraphPerspective nestedNetwork = node.getNestedNetwork();
//            if (nestedNetwork == null) {
                selectedNodes.add(node);
//            } else {
//                selectedNodes.addAll(nestedNetwork.nodesList());
//            }
        }
*/
        //int[] selectedNodes_i = new int[selectedNodes.size()];
        
        //int[] selectedNodes_i = new int[nodes.length];
        int[] selectedNodes_i = new int[188];
        /*
        for (int j = 0; j < selectedNodes.size(); j++) {
            
            //selectedNodes_i[i] = Integer.parseInt(selectedNodes.get(i).getIdentifier) + 1;
            selectedNodes_i[j] = nodes[j].getId() + 1;
            //selectedNodes_i[j] = Integer.parseInt(currentNetwork.getRow(selectedNodes.get(j)).get("id", String.class)) + 1;
        }
        */
        for (int j = 0; j < 188; j++) {
            
            //selectedNodes_i[i] = Integer.parseInt(selectedNodes.get(i).getIdentifier) + 1;
            selectedNodes_i[j] = nodes[j].getId() + 1;
            
            //selectedNodes_i[j] = Integer.parseInt(currentNetwork.getRow(selectedNodes.get(j)).get("id", String.class)) + 1;
        } 
        
        int count = 0;
        
        for (int k = 0; k < selectedNodes_i.length; k++){
            if (selectedNodes_i[k] > 1){
                count++;
            }
        }
        
        return selectedNodes_i;
    }

    /**
     * Populates selectedEvents
     */
    private Array2DRowRealMatrix populateSelectedEvents(int[] selectedClust) {
        int clusterColumn = fcsInputFile.getChannelIdFromShortName("cluster");
        ArrayList<Integer> columns = new ArrayList<Integer>();

        //Populate selectedEventsInitial                        
        for (int i = 0; i < eventsInitl.getColumnDimension(); i++) {
            int cluster = (int) eventsInitl.getEntry(clusterColumn, i);
            for (int j = 0; j < selectedClust.length; j++) {
                if (cluster == selectedClust[j]) {
                    columns.add(i);
                }
            }
        }

        int[] rows = new int[eventsInitl.getRowDimension()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }

        return (Array2DRowRealMatrix) eventsInitl.getSubMatrix(
                    rows,
                    ArrayUtils.toPrimitive(columns.toArray(new Integer[0]))
                );
    }



    public class nameValuePair implements Comparable {

        public double value;
        public String name;

        public nameValuePair(String name, double value) {
            this.name = name;
            this.value = value;
        }

        public int compareTo(Object t) {
            nameValuePair rhs = (nameValuePair) t;
            if (this.value < rhs.value) {
                return -1;
            } else if (this.value > rhs.value) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * T-distribution between selected nodes and all nodes
     */
    private double tTest(
            Array2DRowRealMatrix selectedEvents,
            Array2DRowRealMatrix allEvents,
            int attribute) {

        TTestImpl tTest = new TTestImpl();
        try {
            return tTest.t(selectedEvents.getDataRef()[attribute], allEvents.getDataRef()[attribute]);
        } catch (IllegalArgumentException ex) {
            //CyLogger.getLogger(FCSOperations.class.getName()).error(null, ex);
        }
        return 0.0;
    }

}
