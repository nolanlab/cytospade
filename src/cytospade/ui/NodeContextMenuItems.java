/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cytospade.ui;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.view.CyNetworkView;
import giny.model.GraphPerspective;
import giny.view.NodeView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;

/**
 *
 * @author mlinderm
 */
public class NodeContextMenuItems {

    public static class MakeNestedNetwork implements ActionListener {

        public static final String LABEL = "Create Nested Network";
        private static int MetaID = 0;

        public static void makeNestedNode(Set nodes) {
            CyNetwork currentNetwork  = Cytoscape.getCurrentNetwork();
            CyNetworkView currentView = Cytoscape.getCurrentNetworkView();
            CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();

            Set nestedNodes   = new HashSet(nodes);
            Set nestedEdges   = new HashSet();
            Set bridgingEdges = new HashSet();


            for (CyNode node: (Set<CyNode>)nestedNodes) {
                // Verify that node itself is not nested
                if (node.getNestedNetwork() != null) {
                    JOptionPane.showMessageDialog(null, "Cannot create hierarchically nested networks");
                    return;
                }
            }

            for (CyEdge edge: (List<CyEdge>)currentNetwork.edgesList()) {
                boolean src_in = nestedNodes.contains(edge.getSource()),
                        trg_in = nestedNodes.contains(edge.getTarget());
                if (src_in && trg_in) {
                    nestedEdges.add(edge);
                } else if (src_in || trg_in) {
                    nestedEdges.add(edge);
                    bridgingEdges.add(edge);
                }

            }

            String title = currentNetwork.getTitle() + "_meta" + MetaID;
            CyNetwork nestedNetwork = Cytoscape.createNetwork(
                    nestedNodes,
                    nestedEdges,
                    title,
                    currentNetwork,
                    false);

            // Create nested network node and add it to the current graph
            CyNode containerNode = Cytoscape.getCyNode("meta"+MetaID, true);
            containerNode.setNestedNetwork(nestedNetwork);
            currentNetwork.addNode(containerNode);

            // Create edges to replace those that cross the metanode boundary...
            for (CyEdge edge: (Set<CyEdge>)bridgingEdges) {
                CyEdge nEdge = null;
                if (nestedNodes.contains(edge.getTarget())) {
                    nEdge = Cytoscape.getCyEdge(edge.getSource(), containerNode, Semantics.INTERACTION, "pp", true);
                } else if (nestedNodes.contains(edge.getSource())) {
                    nEdge = Cytoscape.getCyEdge(containerNode, edge.getTarget(), Semantics.INTERACTION, "pp", true);
                }
                currentNetwork.addEdge(nEdge);
            }

            // Put meta-node where original network was ...
            {
                double avgX = 0.0, avgY = 0.0;
                for (CyNode node : (Set<CyNode>) nestedNodes) {
                    NodeView nv = currentView.getNodeView(node);
                    avgX += nv.getXPosition();
                    avgY += nv.getYPosition();
                }
                avgX /= nestedNodes.size();
                avgY /= nestedNodes.size();
                NodeView containerNodeView = currentView.getNodeView(containerNode);
                containerNodeView.setXPosition(avgX);
                containerNodeView.setYPosition(avgY);

                for (CyNode node : (Set<CyNode>) nestedNodes) {
                    NodeView nv = currentView.getNodeView(node);
                    nodeAttributes.setAttribute(node.getIdentifier(), "OffsetToNNX", nv.getXPosition()-avgX);
                    nodeAttributes.setAttribute(node.getIdentifier(), "OffsetToNNY", nv.getYPosition()-avgY);
                }

                if (nodeAttributes.getUserVisible("OffsetToNNX"))
                    nodeAttributes.setUserVisible("OffsetToNNX", false);
                if (nodeAttributes.getUserVisible("OffsetToNNY"))
                    nodeAttributes.setUserVisible("OffsetToNNY", false);
            }

            // Create attributes for meta node
            {
                for (String name: nodeAttributes.getAttributeNames()) {
                    switch(nodeAttributes.getType(name)) {
                        case CyAttributes.TYPE_INTEGER: {
                            int val = 0;
                            boolean touched = false;
                            for (CyNode node: (Set<CyNode>) nestedNodes) {
                                Integer a = nodeAttributes.getIntegerAttribute(node.getIdentifier(), name);
                                if (a != null) {
                                    val += a; 
                                    touched = true;
                                }
                            }
                            if (touched) {
                                if (!name.contentEquals("count") && !name.contentEquals("percenttotal"))
                                    val /= nestedNodes.size();
                                nodeAttributes.setAttribute(containerNode.getIdentifier(), name, val);
                            }
                            break;
                        }
                        case CyAttributes.TYPE_FLOATING: {
                            double val = 0.;
                            boolean touched = false;
                            for (CyNode node: (Set<CyNode>) nestedNodes) {
                                Double a = nodeAttributes.getDoubleAttribute(node.getIdentifier(), name);
                                if (a != null) {
                                    val += a;
                                    touched = true;
                                }

                            }
                            if (touched) {
                                if (!name.contentEquals("count") && !name.contentEquals("percenttotal"))
                                    val /= nestedNodes.size();
                                nodeAttributes.setAttribute(containerNode.getIdentifier(), name, val);
                            }
                            break;
                        }
                    }
                }
            }

            // Remove nested nodes and eges
            for (CyNode node: (Set<CyNode>)nestedNodes) {
                nodeAttributes.setAttribute(node.getIdentifier(), "is_nested", true);
                currentNetwork.hideNode(node);
            }
            if (nodeAttributes.getUserVisible("is_nested"))
                nodeAttributes.setUserVisible("is_nested", false);


            for (CyEdge edge: (Set<CyEdge>)nestedEdges)
                currentNetwork.hideEdge(edge);

            // Make sure we reset the main network as the current network...
            Cytoscape.setCurrentNetwork(currentNetwork.getIdentifier());
            Cytoscape.setCurrentNetworkView(currentView.getIdentifier());

            ++MetaID;  // Increment global meta node count
        }

        public static boolean isNested(CyNode node) {
            CyAttributes  nodeAttributes  = Cytoscape.getNodeAttributes();
            Boolean is_nested = nodeAttributes.getBooleanAttribute(node.getIdentifier(), "is_nested");
            return is_nested != null && is_nested;
        }

        public void actionPerformed(ActionEvent ae) {
            // Make sure this is what the user intended...
            if (JOptionPane.showConfirmDialog(null, "Convert selected nodes into a nested network?") != JOptionPane.OK_OPTION)
                return;

            CyNetwork currentNetwork  = Cytoscape.getCurrentNetwork();
            makeNestedNode(currentNetwork.getSelectedNodes());

            // Re-apply visual appearances
            Cytoscape.getVisualMappingManager().applyAppearances();
        }
        
    }

    public static class UndoNestedNetwork implements ActionListener {

        public static final String LABEL = "Undo Nested Network";

        public static void undoNestedNode(CyNode node) {
            CyNetwork     currentNetwork  = Cytoscape.getCurrentNetwork();
            CyNetworkView currentView     = Cytoscape.getCurrentNetworkView();
            CyAttributes  nodeAttributes  = Cytoscape.getNodeAttributes();

            GraphPerspective nestedNetwork = node.getNestedNetwork();
            if (nestedNetwork == null)
                return;

            NodeView nv = currentView.getNodeView(node);
            for (CyNode nn : (List<CyNode>) nestedNetwork.nodesList()) {
                if (!MakeNestedNetwork.isNested(nn))
                    continue;
                
                currentNetwork.restoreNode(nn);
                nodeAttributes.deleteAttribute(nn.getIdentifier(), "is_nested");

                NodeView nnv = currentView.getNodeView(nn);  // Restore original layout along with nodes
                Double off_x = nodeAttributes.getDoubleAttribute(nn.getIdentifier(), "OffsetToNNX");
                if (off_x != null) {
                    nnv.setXPosition(off_x + nv.getXPosition());
                    nodeAttributes.deleteAttribute(nn.getIdentifier(), "OffsetToNNX");
                }
                Double off_y = nodeAttributes.getDoubleAttribute(nn.getIdentifier(), "OffsetToNNY");
                if (off_y != null) {
                    nnv.setYPosition(off_y + nv.getYPosition());
                    nodeAttributes.deleteAttribute(nn.getIdentifier(), "OffsetToNNY");
                }

            }
            for (CyEdge ne : (List<CyEdge>) nestedNetwork.edgesList()) {
                currentNetwork.restoreEdge(ne);
            }


            // Remove bridging edges we created, along with nested network node...
            for (int idx : currentNetwork.getAdjacentEdgeIndicesArray(node.getRootGraphIndex(), true, true, true)) {
                currentNetwork.removeEdge(idx, true);
            }
            currentNetwork.removeNode(node.getRootGraphIndex(), true);
        }

        public void actionPerformed(ActionEvent ae) {
            // Make sure this is what the user intended...
            if (JOptionPane.showConfirmDialog(null, "Undo previous nesting operation?") != JOptionPane.OK_OPTION)
                return;

            CyNetwork currentNetwork  = Cytoscape.getCurrentNetwork();
            Set selectedNodes = new HashSet(currentNetwork.getSelectedNodes());
            for (CyNode node: (Set<CyNode>)selectedNodes) {
                undoNestedNode(node);
            }

            // Re-apply visual appearances
            Cytoscape.getVisualMappingManager().applyAppearances();
        }
    
    }
}
