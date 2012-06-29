package cytospade;

import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.VisualStyle;
import cytospade.ui.NodeContextMenuItems;
import giny.model.Node;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author ksheode
 */
public class MergeOrderOperations {

    //Nesting type Incremental
    private ArrayList<ArrayList<Integer>> mergeOrderFileCache = new ArrayList<ArrayList<Integer>>();
    private ArrayList<String> clusterIdentifier = new ArrayList<String>();
    //Nesting type Absolute
    ArrayList<ArrayList<Integer>> absoluteClusterNodes = new ArrayList<ArrayList<Integer>>();
    private String nestedDetailedGraphId;
    private String originalNetworkId;
    private ArrayList<String> tempNodeViews = new ArrayList<String>();
    private int previousMergeOrder = 0;
    private boolean previousRenderNestedView = false;

    public MergeOrderOperations(File mergeOrderFile) throws FileNotFoundException, Exception {
        Scanner myfile = new Scanner(mergeOrderFile);

        //MergeOrder = 0 is no nesting        
        mergeOrderFileCache.add(new ArrayList<Integer>());

        //Cluster 0 does not exist
        clusterIdentifier.add(null);

        // Skip the header
        myfile.nextLine();

        // iterate over rows in the file
        int lineNum = 2;
        while (myfile.hasNextLine()) {
            String[] nodeIds = myfile.nextLine().split(" ");
            if (nodeIds.length != 3) {
                throw new Exception("Merge order file is corrupted. Line number:" + lineNum + " in file:" + mergeOrderFile.getName());
            }
            lineNum++;

            int element0 = Integer.decode(nodeIds[1]);
            int element1 = Integer.decode(nodeIds[2]);

            //Nesting type 1
            ArrayList<Integer> cluster = new ArrayList<Integer>();
            cluster.add(element0);
            cluster.add(element1);
            mergeOrderFileCache.add(cluster);
            clusterIdentifier.add(null);
        }

        //Nesting type 2
        //Create mapping of clusternumber and ALL nodes that are in that cluster
        for (ArrayList<Integer> cluster : mergeOrderFileCache) {
            ArrayList<Integer> nodesToAdd = new ArrayList<Integer>();
            for (Integer node : cluster) {
                if (node < 0) {
                    nodesToAdd.add(-1 * node);
                } else {
                    nodesToAdd.addAll(absoluteClusterNodes.get(node));
                }
            }
            absoluteClusterNodes.add(nodesToAdd);
        }
    }

    /**
     * Gets the maximum merge order possible
     * @return 
     */
    public int getMaxMergeOrder() {
        return mergeOrderFileCache.size() - 1;
    }

    /**
     * Merge SPADE generated clusters to the order of merge specified on the current network
     * @param mergeOrder: Order of merge
     * @param renderNestedView: Display the nested network icon in the container node.
     *                          This slows down processing. 
     * @return: The actual order of merge rendered, -1 if no merge rendered
     */
    public int createNestedGraphIncremental(int mergeOrder, boolean renderNestedView) {

        if (mergeOrder >= mergeOrderFileCache.size()) {
            mergeOrder = mergeOrderFileCache.size() - 1;
        }

        if (mergeOrder < 0) {
            return -1;
        }

        if (previousMergeOrder == mergeOrder) {
            return mergeOrder;
        }
        previousRenderNestedView = renderNestedView;

        //If mergeorder is greater than previous merge, need to merge some more
        for (int i = previousMergeOrder + 1; i <= mergeOrder; i++, previousMergeOrder++) {
            //get nodes to merge
            int firstNode = mergeOrderFileCache.get(i).get(0);
            int secondNode = mergeOrderFileCache.get(i).get(1);

            //convert into Cytoscape node ids
            //if <0, use -1*node, if >0, fetch cluster id from datastructure
            String firstNodeId = String.valueOf(-1 * firstNode);
            String secondNodeId = String.valueOf(-1 * secondNode);
            if (firstNode > 0) {
                firstNodeId = clusterIdentifier.get(firstNode);
            }

            if (secondNode > 0) {
                secondNodeId = clusterIdentifier.get(secondNode);
            }

            //Convert to cytoscape nodes
            CyNode cyNode1 = Cytoscape.getCyNode(firstNodeId);
            CyNode cyNode2 = Cytoscape.getCyNode(secondNodeId);

            if (cyNode1 == null || cyNode2 == null) {
                continue;
            }

            //merge
            Set<CyNode> cluster = new HashSet<CyNode>();
            cluster.add(cyNode1);
            cluster.add(cyNode2);
            CyNode retRootNode = NodeContextMenuItems.MakeNestedNetwork.makeNestedNode(cluster, renderNestedView);
            if (retRootNode != null) {
                clusterIdentifier.add(i, retRootNode.getIdentifier());
            }
        }

        //If mergeorder is lesser than previous merge, need to unmerge some more
        for (int i = previousMergeOrder; i > mergeOrder; i--, previousMergeOrder--) {
            //Get identifier of merged cluster
            String clusterid = clusterIdentifier.get(i);
            if (clusterid == null) {
                continue;
            }

            //Convert to Cynode
            CyNode clusterNode = Cytoscape.getCyNode(clusterid);

            //Update datastructure
            clusterIdentifier.set(i, null);

            //Unmerge
            if (clusterNode == null) {
                continue;
            }

            NodeContextMenuItems.UndoNestedNetwork.undoNestedNode(clusterNode);
        }

        Cytoscape.getVisualMappingManager().applyAppearances();

        return mergeOrder;
    }

    /**
     * Create a new window with the nested networks visible within the node
     * This can take a while.
     */
    public void showNestedNetworkDetails() {
        //Undo all nesting
        int previous = previousMergeOrder;
        boolean previousView = previousRenderNestedView;
        createNestedGraphIncremental(0, previousRenderNestedView);

        //Make a copy of the current network
        CyNetworkView currentView = Cytoscape.getCurrentNetworkView();
        CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
        originalNetworkId = currentNetwork.getIdentifier();

        String title = currentNetwork.getTitle() + "_Merge" + Integer.toString(previous);
        CyNetwork nestedNetwork = Cytoscape.createNetwork(title, true);
        nestedNetwork.appendNetwork(currentNetwork);
        CyNetworkView nestedView = Cytoscape.createNetworkView(nestedNetwork, title);
        nestedDetailedGraphId = nestedNetwork.getIdentifier();

        String vsName = "default";

        //Set the network view style
        if (currentView != Cytoscape.getNullNetworkView()) {
            Iterator i = nestedNetwork.nodesIterator();

            while (i.hasNext()) {
                Node node = (Node) i.next();
                nestedView.getNodeView(node).setOffset(currentView.getNodeView(node).getXPosition(), currentView.getNodeView(node).getYPosition());
            }
            nestedView.fitContent();

            VisualStyle newVS = currentView.getVisualStyle();

            if (newVS != null) {
                vsName = newVS.getName();
            }
        }

        Cytoscape.getVisualMappingManager().setVisualStyle(vsName);

        // Create the clustered graph with nested network views
        createNestedGraphAbsolute(previous, true);

        Cytoscape.getVisualMappingManager().applyAppearances();

        //Reset the current network to what was earlier
        Cytoscape.setCurrentNetwork(currentNetwork.getIdentifier());
        Cytoscape.setCurrentNetworkView(currentView.getTitle());
        Cytoscape.getDesktop().setFocus(currentNetwork.getIdentifier());

        //Restore the cluster of the network
        //Reset internal datastructures as they are still tracking the copied network
        previousMergeOrder = 0;
        int numClusters = clusterIdentifier.size();
        clusterIdentifier.clear();
        for (int i = 0; i < numClusters; i++) {
            clusterIdentifier.add(null);
        }
        createNestedGraphIncremental(previous, previousView);
        Cytoscape.getDesktop().setFocus(nestedNetwork.getIdentifier());
    }

    /**
     * Merge SPADE generated clusters to the order of merge specified on the current network
     * The nested network contains non-hierarchical nesting of nodes.
     * Slower than createNestedGraphIncremental.
     * @param mergeOrder: Order of merge
     * @param renderNestedView: Display the nested network icon in the container node.
     *                          This slows down processing. The nested network icon contains
     *                          non-hierarchical nesting of nodes. 
     * @return: The actual order of merge rendered, -1 if no merge rendered
     */
    private void createNestedGraphAbsolute(int mergeOrder, boolean renderNestedView) {

        //Cluster nodes using the absolute node list
        for (int clusterNum = mergeOrder; clusterNum >= 0; clusterNum--) {
            Set<CyNode> nodes = new HashSet<CyNode>();
            for (int nodeId : absoluteClusterNodes.get(clusterNum)) {
                //Convert to cytoscape nodes
                CyNode cyNode = Cytoscape.getCyNode(Integer.toString(nodeId));
                if (cyNode == null || NodeContextMenuItems.MakeNestedNetwork.isNested(cyNode)) {
                    continue;
                }
                nodes.add(cyNode);
            }

            if (!nodes.isEmpty()) {
                CyNode retRootNode = NodeContextMenuItems.MakeNestedNetwork.makeNestedNode(nodes, renderNestedView);
                if (retRootNode != null) {
                    tempNodeViews.add(0, ((CyNetwork) retRootNode.getNestedNetwork()).getIdentifier());
                }
            }
        }
    }

    /**
     * Shuts down all the windows opened by showNestedNetworkDetails
     */
    public void destroyNestedNetworkDetails() {
        //Set the detailed network as the current network.
        //Required as many operation in various places are performed exclusively on the current network.
        CyNetwork network = Cytoscape.getNetwork(nestedDetailedGraphId);
        Cytoscape.setCurrentNetwork(nestedDetailedGraphId);
        Cytoscape.setCurrentNetworkView(nestedDetailedGraphId);

        //Delete all the background windows
        if (tempNodeViews.size() > 0) {
            for (int i = 0; i < tempNodeViews.size(); i++) {
                Cytoscape.destroyNetworkView(tempNodeViews.get(i));
            }
        }

        //Destroy the detailed network
        Cytoscape.destroyNetworkView(network);
        Cytoscape.destroyNetwork(network);

        //Restore the current network to the 'undetailed' network
        Cytoscape.setCurrentNetwork(originalNetworkId);
        Cytoscape.setCurrentNetworkView(originalNetworkId);
        Cytoscape.getDesktop().setFocus(originalNetworkId);
    }
}
