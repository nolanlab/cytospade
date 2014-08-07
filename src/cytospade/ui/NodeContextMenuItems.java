package cytospade.ui;

import cytospade.SpadeContext;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;

/**
 *
 * @author mlinderm
 */
public class NodeContextMenuItems {
    
    private static SpadeContext spadeCxt;
    private static CyNetworkView cnv;
    private static CyApplicationManager cam;
    private static CyNetworkManager netman;
    private static View<CyNode> view;
    
    public static class MakeNestedNetwork implements ActionListener {

        public static final String LABEL = "Create Nested Network";
        private static int MetaID = 0;
        private static VisualStyle vsName;

        public static View<CyNode> makeNestedNode(Set nodes) {
            return makeNestedNode(nodes, false);
        }
        
        public static View<CyNode> makeNestedNode(Set nodes, boolean renderNestedView) {
            /* Cytoscape.getCurrentNetwork() and Cytoscape.getCurrentNetworkView() are
              now part of the CyApplicationManager interface, however their use is
              discouraged in favor of implementing NetworkTaskFactory and
              NetworkViewTaskFactory services (found in the core-task-api bundle). */
            //CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
            netman = spadeCxt.adapter.getCyNetworkManager();
            cnv = cam.getCurrentNetworkView();
            // CyAttributes has been replaced by the concept of CyTable.
            //CyAttributes nodeAttributes = Cytoscape.getNodeAttributes(); //not using attributes at this time

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

            //for (CyEdge edge : (List<CyEdge>) currentNetwork.edgesList()) {
                for (CyEdge edge : (List<CyEdge>) cam.getCurrentNetwork().getEdgeList()){
                boolean src_in = nestedNodes.contains(edge.getSource()),
                        trg_in = nestedNodes.contains(edge.getTarget());
                if (src_in && trg_in) {
                    nestedEdges.add(edge);
                } else if (src_in || trg_in) {
                    nestedEdges.add(edge);
                    bridgingEdges.add(edge);
                }
            }

            //String title = currentNetwork.getTitle() + "_meta" + MetaID;
            String title = cam.getCurrentNetwork().toString() + "_meta" + MetaID;
            CyNetwork myNet = spadeCxt.adapter.getCyNetworkFactory().createNetwork();
            //CyNetwork nestedNetwork = Cytoscape.createNetwork(
            /* nested networks are out temporarily
                    nestedNodes,
                    nestedEdges,
                    title,
                    //currentNetwork,
                    cam.getCurrentNetwork());
                    false);
            */
            // Create metanode and add it to the current graph.
            //CyNode containerNode = Cytoscape.getCyNode("meta" + MetaID, true);
            
            //currentNetwork.addNode(containerNode);
            CyNode newNode = myNet.addNode();
            myNet.getRow(newNode).set(CyNetwork.NAME,"meta" + MetaID);
            // Put meta-node where original network was ...
            
                double avgX = 0.0, avgY = 0.0;
                for (CyNode node : (Set<CyNode>) nestedNodes) {
                    //View<CyNode> nv = currentView.getNodeView(node);
                    View<CyNode> nv = cnv.getNodeView(node);
                    //double currX = nv.getXPosition();
                    double currX = nv.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
                    //double currY = nv.getYPosition();
                    double currY = nv.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
                    avgX += currX;
                    avgY += currY;
                }
                avgX /= nestedNodes.size();
                avgY /= nestedNodes.size();
                
                /* attributes out
                View<CyNode> containerNodeView = currentView.getNodeView(containerNode);
                containerNodeView.setXPosition(avgX);
                containerNodeView.setYPosition(avgY);

                for (CyNode node : (Set<CyNode>) nestedNodes) {
                    View<CyNode> nv = currentView.getNodeView(node);
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
            */
            // Create attributes for meta node
            /*
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
            */ //attributes out
                
            if (renderNestedView) {
                //Set the nested network style. This sets the icon in the newly created node.
                //CyNetworkView nestedNetworkView = Cytoscape.createNetworkView(nestedNetwork, " selection");
                //cnv = cam.getCurrentNetworkView();
                CyNetworkViewFactory cnvf = spadeCxt.adapter.getCyNetworkViewFactory();
                CyNetworkView nestedNetworkView = cnvf.createNetworkView(myNet);
                spadeCxt.adapter.getCyNetworkViewManager().addNetworkView(nestedNetworkView);
                String vsName = "default";
                //if (currentView != Cytoscape.getNullNetworkView()) {
                //if (cnv.getNodeViews() != null)
                    //Iterator i = nestedNetwork.nodesIterator();

                    //while (i.hasNext()) {
                        //Node node = (Node) i.next();
                    for (CyNode node : (Set<CyNode>) nestedNodes) {
                        //nestedNetworkView.getNodeView(node).setOffset(currentView.getNodeView(node).getXPosition(), currentView.getNodeView(node).getYPosition());
                        //not sure how to set offset here
                        //nestedNetworkView.getNodeView(node).setOffset(cnv.getNodeView(node).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION), cnv.getNodeView(node).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));
                    }

                    nestedNetworkView.fitContent();
                    //VisualStyle newVS = currentView.getVisualStyle();
                    VisualStyle newVS = spadeCxt.adapter.getVisualMappingManager().getVisualStyle(cnv);

                    if (newVS != null) {
                        //vsName = newVS.getName();
                        vsName = newVS.getTitle();
                    }
                }

                //Cytoscape.getVisualMappingManager().setVisualStyle(vsName);
                spadeCxt.adapter.getVisualMappingManager().setVisualStyle(vsName, cnv);
            

            //Set the nested network
            /* out for now
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
        */
        return view;
        }

        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
        public static boolean isNested(CyNode node) {
            //CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
            //Boolean is_nested = nodeAttributes.getBooleanAttribute(node.getIdentifier(), "is_nested");
            String is_nested = cam.getCurrentNetwork().getRow(node).get("is_nested", String.class);
            //return is_nested != null && is_nested;
            return (is_nested == "is_nested");
        }

        public void actionPerformed(ActionEvent ae) {
            // Make sure this is what the user intended...
            if (JOptionPane.showConfirmDialog(null, "Convert selected nodes into a nested network?") != JOptionPane.OK_OPTION) {
                return;
            }

            //CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
            CyNetwork currentNetwork = cam.getCurrentNetwork();
            //makeNestedNode(currentNetwork.getSelectedNodes());
            List<CyNode> nodes = CyTableUtil.getNodesInState(currentNetwork,"selected",true);
            //makeNestedNode(currentNetwork.getRow(cnv).get(null, ae)); //not making nested nodes right now

            // Re-apply visual appearances
            //Cytoscape.getVisualMappingManager().applyAppearances();
            cnv.updateView();
        }
    }
    /* currently unnecessary
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

            View<CyNode> nv = currentView.getNodeView(node);
            for (CyNode nn : (List<CyNode>) nestedNetwork.nodesList()) {
                if (!MakeNestedNetwork.isNested(nn)) {
                    continue;
                }

                currentNetwork.restoreNode(nn);
                nodeAttributes.deleteAttribute(nn.getIdentifier(), "is_nested");

                View<CyNode> nnv = currentView.getNodeView(nn);  // Restore original layout along with nodes
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
*/
//}
