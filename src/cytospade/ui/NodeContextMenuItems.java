package cytospade.ui;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import giny.model.Node;
import cytoscape.data.Semantics;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.VisualStyle;
import giny.model.GraphPerspective;
import giny.view.NodeView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
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

        public static CyNode makeNestedNode(Set nodes) {
            return makeNestedNode(nodes, false);
        }

        public static CyNode makeNestedNode(Set nodes, boolean renderNestedView) {
            CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
            CyNetworkView currentView = Cytoscape.getCurrentNetworkView();
            CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();

            Set nestedNodes = new HashSet(nodes);
            Set nestedEdges = new HashSet();
            Set bridgingEdges = new HashSet();

            for (CyNode node : (Set<CyNode>) nestedNodes) {
                // Verify that node itself is not nested
                //if (node.getNestedNetwork() != null) {
                //Boolean isNested = Boolean.getBoolean(nodeAttributes.getAttribute(node.getIdentifier(), "is_nested").toString());
                //if (isNested) {
                //    JOptionPane.showMessageDialog(null, "Cannot create hierarchically nested networks");
                //    return null;
                //}
            }

            for (CyEdge edge : (List<CyEdge>) currentNetwork.edgesList()) {
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

            // Create metanode and add it to the current graph.
            CyNode containerNode = Cytoscape.getCyNode("meta" + MetaID, true);
            currentNetwork.addNode(containerNode);

            // Put meta-node where original network was ...
            {
                double avgX = 0.0, avgY = 0.0;
                for (CyNode node : (Set<CyNode>) nestedNodes) {
                    NodeView nv = currentView.getNodeView(node);
                    double currX = nv.getXPosition();
                    double currY = nv.getYPosition();
                    avgX += currX;
                    avgY += currY;
                }
                avgX /= nestedNodes.size();
                avgY /= nestedNodes.size();
                NodeView containerNodeView = currentView.getNodeView(containerNode);
                containerNodeView.setXPosition(avgX);
                containerNodeView.setYPosition(avgY);

                for (CyNode node : (Set<CyNode>) nestedNodes) {
                    NodeView nv = currentView.getNodeView(node);
                    nodeAttributes.setAttribute(node.getIdentifier(), "OffsetToNNX", nv.getXPosition() - avgX);
                    nodeAttributes.setAttribute(node.getIdentifier(), "OffsetToNNY", nv.getYPosition() - avgY);
                }

                if (nodeAttributes.getUserVisible("OffsetToNNX")) {
                    nodeAttributes.setUserVisible("OffsetToNNX", false);
                }
                if (nodeAttributes.getUserVisible("OffsetToNNY")) {
                    nodeAttributes.setUserVisible("OffsetToNNY", false);
                }
            }

            // Create attributes for meta node
            {
                for (String name : nodeAttributes.getAttributeNames()) {
                    switch (nodeAttributes.getType(name)) {
                        case CyAttributes.TYPE_INTEGER: {
                            int val = 0;
                            boolean touched = false;
                            for (CyNode node : (Set<CyNode>) nestedNodes) {
                                Integer a = nodeAttributes.getIntegerAttribute(node.getIdentifier(), name);
                                if (a != null) {
                                    val += a;
                                    touched = true;
                                }
                            }
                            if (touched) {
                                if (!name.contentEquals("count") && !name.contentEquals("percenttotal")) {
                                    val /= nestedNodes.size();
                                }
                                nodeAttributes.setAttribute(containerNode.getIdentifier(), name, val);
                            }
                            break;
                        }
                        case CyAttributes.TYPE_FLOATING: {
                            double val = 0.;
                            boolean touched = false;
                            for (CyNode node : (Set<CyNode>) nestedNodes) {
                                Double a = nodeAttributes.getDoubleAttribute(node.getIdentifier(), name);
                                if (a != null) {
                                    val += a;
                                    touched = true;
                                }

                            }
                            if (touched) {
                                if (!name.contentEquals("count") && !name.contentEquals("percenttotal")) {
                                    val /= nestedNodes.size();
                                }
                                nodeAttributes.setAttribute(containerNode.getIdentifier(), name, val);
                            }
                            break;
                        }
                    }
                }
            }

            if (renderNestedView) {
                //Set the nested network style. This sets the icon in the newly created node.
                CyNetworkView nestedNetworkView = Cytoscape.createNetworkView(nestedNetwork, " selection");
                String vsName = "default";
                if (currentView != Cytoscape.getNullNetworkView()) {
                    Iterator i = nestedNetwork.nodesIterator();

                    while (i.hasNext()) {
                        Node node = (Node) i.next();
                        nestedNetworkView.getNodeView(node).setOffset(currentView.getNodeView(node).getXPosition(), currentView.getNodeView(node).getYPosition());
                    }

                    nestedNetworkView.fitContent();
                    VisualStyle newVS = currentView.getVisualStyle();

                    if (newVS != null) {
                        vsName = newVS.getName();
                    }
                }

                Cytoscape.getVisualMappingManager().setVisualStyle(vsName);
            }

            //Set the nested network
            containerNode.setNestedNetwork(nestedNetwork);

            // Create edges to replace those that cross the metanode boundary...
            for (CyEdge edge : (Set<CyEdge>) bridgingEdges) {
                CyEdge nEdge = null;
                if (nestedNodes.contains(edge.getTarget())) {
                    nEdge = Cytoscape.getCyEdge(edge.getSource(), containerNode, Semantics.INTERACTION, "pp", true);
                } else if (nestedNodes.contains(edge.getSource())) {
                    nEdge = Cytoscape.getCyEdge(containerNode, edge.getTarget(), Semantics.INTERACTION, "pp", true);
                }
                currentNetwork.addEdge(nEdge);
            }

            // Remove nested nodes and eges
            for (CyNode node : (Set<CyNode>) nestedNodes) {
                nodeAttributes.setAttribute(node.getIdentifier(), "is_nested", true);
                currentNetwork.hideNode(node);
            }

            // Allow user to inspect this variable in the DataPanel
            nodeAttributes.setUserVisible("is_nested", true);

            for (CyEdge edge : (Set<CyEdge>) nestedEdges) {
                currentNetwork.hideEdge(edge);
            }

            // Make sure we reset the main network as the current network...
            Cytoscape.setCurrentNetwork(currentNetwork.getIdentifier());
            Cytoscape.setCurrentNetworkView(currentView.getIdentifier());

            //Set the top window in the Cytoscape desktop to be the current network
            //Nested network view may be on top of the current network
            Cytoscape.getDesktop().setFocus(currentNetwork.getIdentifier());

            ++MetaID;  // Increment global meta node count

            return containerNode;
        }

        public static boolean isNested(CyNode node) {
            CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
            Boolean is_nested = nodeAttributes.getBooleanAttribute(node.getIdentifier(), "is_nested");
            return is_nested != null && is_nested;
        }

        public void actionPerformed(ActionEvent ae) {
            // Make sure this is what the user intended...
            if (JOptionPane.showConfirmDialog(null, "Convert selected nodes into a nested network?") != JOptionPane.OK_OPTION) {
                return;
            }

            CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
            makeNestedNode(currentNetwork.getSelectedNodes());

            // Re-apply visual appearances
            Cytoscape.getVisualMappingManager().applyAppearances();
        }
    }

    public static class UndoNestedNetwork implements ActionListener {

        public static final String LABEL = "Undo Nested Network";

        public static void undoNestedNode(CyNode node) {
            CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
            CyNetworkView currentView = Cytoscape.getCurrentNetworkView();
            CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();

            GraphPerspective nestedNetwork = node.getNestedNetwork();
            if (nestedNetwork == null) {
                return;
            }

            NodeView nv = currentView.getNodeView(node);
            for (CyNode nn : (List<CyNode>) nestedNetwork.nodesList()) {
                if (!MakeNestedNetwork.isNested(nn)) {
                    continue;
                }

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

            // Close the window that opens to display the nested network view

            CyNetworkView nestedView = Cytoscape.getNetworkView(((CyNetwork) nestedNetwork).getIdentifier());
            if (nestedView == null || nestedView == Cytoscape.getNullNetworkView()) {
            } else {
                Cytoscape.destroyNetworkView((CyNetwork) nestedNetwork);
            }

            currentNetwork.removeNode(node.getRootGraphIndex(), true);
        }

        public void actionPerformed(ActionEvent ae) {
            // Make sure this is what the user intended...
            if (JOptionPane.showConfirmDialog(null, "Undo previous nesting operation?") != JOptionPane.OK_OPTION) {
                return;
            }

            CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
            Set selectedNodes = new HashSet(currentNetwork.getSelectedNodes());
            for (CyNode node : (Set<CyNode>) selectedNodes) {
                undoNestedNode(node);
            }

            // Re-apply visual appearances
            Cytoscape.getVisualMappingManager().applyAppearances();
        }
    }
}
