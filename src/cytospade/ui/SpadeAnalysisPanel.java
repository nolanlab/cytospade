/*
 * SpadeAnalysisPanel.java
 *
 * Created on Dec 5, 2011, 9:24:43 PM
 */
package cytospade.ui;

import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.actions.LoadNetworkTask;
import cytoscape.data.SelectEventListener;
import cytoscape.logger.CyLogger;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.GlobalAppearanceCalculator;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyDependency;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytospade.CytoSpade;
import cytospade.FCSOperations;
import cytospade.MergeOrderOperations;
import cytospade.SpadeContext;
import cytospade.SpadeContext.NormalizationKind;
import cytospade.SpadeController;
import cytospade.VisualMapping;
import cytospade.WorkflowWizard;
import cytospade.WorkflowWizardPanels;
import giny.model.GraphPerspective;
import giny.view.NodeView;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Ketaki
 */
public class SpadeAnalysisPanel extends javax.swing.JPanel {

    private SpadeContext spadeCxt;
    private VisualMapping visualMapping;
    private FCSOperations fcsOperations;
    private ScatterPlotPanel scatterPlot;
    private ReentrantLock panelLock;
    private MergeOrderOperations mergeOrderOps;
    DefaultTableModel TValTableModel = new javax.swing.table.DefaultTableModel(
            new Object[][]{{null, null}},
            new String[]{"Parameter", "T value"});

    /** Creates new form SpadeAnalysisPanel */
    public SpadeAnalysisPanel(SpadeContext spadeCxt) {
        this.spadeCxt = spadeCxt;

        // Find the global_boundaries.table file it exists, and create appropiate visual mapping
        File[] boundaryFiles = spadeCxt.getPath().listFiles(new FilenameFilter() {

            public boolean accept(File f, String name) {
                return (name.matches("global_boundaries.table"));
            }
        });

        if (boundaryFiles.length == 1) {
            this.visualMapping = new VisualMapping(boundaryFiles[0]);
        } else if (boundaryFiles.length == 0) {
            this.visualMapping = new VisualMapping();
        } else {
            JOptionPane.showMessageDialog(null, "Error: Found more than one global_boundaries.table file.");
            return;
        }

        panelLock = new ReentrantLock();

        initComponents();

    }

    /**
     * @author bjornson
     * class to override File's toString method
     */
    public class FileItem {

        private File f;

        public FileItem(File file) {
            f = file;
        }

        @Override
        public String toString() {
            //Return only the file base name
            return f.getName().substring(0, f.getName().indexOf((".fcs")));
        }

        public String getFCSpath() {
            return f.getName();
        }

        public File getFCSFile() {
            return f;
        }
    }

    private javax.swing.DefaultComboBoxModel RangeSelectModel() {
        return new javax.swing.DefaultComboBoxModel(
                visualMapping.globalRangeAvailable()
                ? new SpadeContext.NormalizationKind[]{SpadeContext.NormalizationKind.GLOBAL, SpadeContext.NormalizationKind.LOCAL}
                : new SpadeContext.NormalizationKind[]{SpadeContext.NormalizationKind.LOCAL});
    }

    public void onExit() {
        //Do not save data on exit unless explicitely indicated in the 'Close Spade' dialog
        //this.saveMetadata(true);
    }

    //<editor-fold desc="Node Graph Controls" defaultstate="collapsed">
    /**
     * Handles node selection events
     */
    public class HandleSelect implements SelectEventListener {

        public void onSelectEvent(cytoscape.data.SelectEvent e) {
            updateFCSConsumers();
        }
    }

    private void updateFCSConsumers() {
        (new SwingWorker<Integer, Void>() {

            @Override
            protected Integer doInBackground() throws Exception {

                fcsOperations.updateSelectedNodes();

                scatterPlot.updatePlot();

                if (fcsOperations.getSelectedNodesCount() == 0) {
                    panelLock.lock();
                    try {
                        NumberEventsLabel.setText("Displaying all " + fcsOperations.getEventCount() + " events.");
                        TValTableModel.setRowCount(0);
                        TValTableModel.addRow(new Object[]{"Select nodes...", ""});
                    } finally {
                        panelLock.unlock();
                    }
                } else {
                    if (fcsOperations.getSelectedEventCount() >= 2) {
                        List<FCSOperations.AttributeValuePair> stats = fcsOperations.computeTStat();
                        TValTableModel.setRowCount(0);
                        for (int i = 0; i < stats.size(); i++) {
                            TValTableModel.addRow(new Object[]{stats.get(i).attribute, (double) (Math.round(stats.get(i).value * 10)) / 10});
                        }
                    } else {
                        TValTableModel.addRow(new Object[]{"Too few events.", ""});
                    }

                    panelLock.lock();
                    try {
                        NumberEventsLabel.setText(
                                "Displaying "
                                + fcsOperations.getSelectedEventCount()
                                + " of "
                                + fcsOperations.getEventCount()
                                + " events.");
                    } finally {
                        panelLock.unlock();
                    }
                }
                return 0;
            }
        }).execute();
    }

    /**
     * Applies sizes and colors to the network view
     */
    private void updateNodeSizeAndColors() {
        // Skip mapping if no file or coloring attribute is specified
        if ((FilenameSelect1.getSelectedIndex() < 0) || (ColoringSelect1.getSelectedIndex() < 0)) {
            return;
        }
        
        try {
            visualMapping.setCurrentMarkersAndRangeKind(
                    "percenttotal",
                    ColoringSelect1.getSelectedItem().toString(),
                    spadeCxt.getNormalizationKind(),
                    spadeCxt.getSymmetry());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Invalid choice of mapping parameters: " + e);
            return;
        }

        VisualMappingManager cyVMM = Cytoscape.getVisualMappingManager();

        try {

            VisualStyle spadeVS = cyVMM.getCalculatorCatalog().getVisualStyle("SPADEVisualStyle");
            if (spadeVS != null) {
                // Overwrite visual style, only way to get Cytoscape to reliably update
                cyVMM.getCalculatorCatalog().removeVisualStyle("SPADEVisualStyle");
            }
            spadeVS = new VisualStyle("SPADEVisualStyle");

            // Update with new calculators
            GlobalAppearanceCalculator globalAppCalc = new GlobalAppearanceCalculator();
            globalAppCalc.setDefaultNodeSelectionColor(Color.MAGENTA);
            globalAppCalc.setDefaultEdgeSelectionColor(Color.MAGENTA);
            globalAppCalc.setDefaultBackgroundColor(Color.BLACK);
            spadeVS.setGlobalAppearanceCalculator(globalAppCalc);

            NodeAppearanceCalculator nodeAppCalc = new NodeAppearanceCalculator(spadeVS.getDependency());
            nodeAppCalc.setCalculator(visualMapping.createColorCalculator());
            nodeAppCalc.setCalculator(visualMapping.createSizeCalculator());
            spadeVS.setNodeAppearanceCalculator(nodeAppCalc);

            // Set a few defaults now that we have overwritten the calculators
            VisualPropertyType.NODE_SHAPE.setDefault(spadeVS, cytoscape.visual.NodeShape.ELLIPSE);
            VisualPropertyType.NODE_FILL_COLOR.setDefault(spadeVS, Color.LIGHT_GRAY);
            spadeVS.getDependency().set(VisualPropertyDependency.Definition.NODE_SIZE_LOCKED, true);
            VisualPropertyType.NODE_BORDER_COLOR.setDefault(spadeVS, Color.WHITE);
            VisualPropertyType.NODE_LINE_WIDTH.setDefault(spadeVS, 4);
            VisualPropertyType.EDGE_COLOR.setDefault(spadeVS, Color.WHITE);
            VisualPropertyType.EDGE_LINE_WIDTH.setDefault(spadeVS, 2);

            cyVMM.getCalculatorCatalog().addVisualStyle(spadeVS);
            cyVMM.setVisualStyle(spadeVS);
            Cytoscape.getCurrentNetworkView().setVisualStyle(spadeVS.getName());

        } catch (RuntimeException ex) {
            CyLogger.getLogger().error("Error Visual Mapping", ex);
        }
    }

    // <editor-fold desc="Utility Methods" defaultstate="collapsed">
    /**
     * Saves the user-defined network landscaping to a flat file
     * @param closeNetwork - whether or not to close the network after saving it.
     */
    private void saveMetadata(Boolean closeNetwork) {
        CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
        CyNetworkView currentNetworkView = Cytoscape.getCurrentNetworkView();

        try {
            FileWriter nstream = new FileWriter(new File(spadeCxt.getPath(), "nested.txt").getAbsolutePath());
            BufferedWriter nout = new BufferedWriter(nstream);
            for (CyNode node : (List<CyNode>) currentNetwork.nodesList()) {
                // Write out membership in nested networks
                GraphPerspective nestedNetwork = node.getNestedNetwork();
                if (nestedNetwork != null) {
                    for (CyNode nn : (List<CyNode>) nestedNetwork.nodesList()) {
                        if (!NodeContextMenuItems.MakeNestedNetwork.isNested(nn)) {
                            continue;
                        }

                        int id = Integer.parseInt(nn.getIdentifier());
                        nout.write((id + 1) + " ");
                    }
                    nout.write("\n");

                    // Restore original node structure
                    NodeContextMenuItems.UndoNestedNetwork.undoNestedNode(node);
                }
            }
            nout.close();
        } catch (IOException ex) {
            CyLogger.getLogger().error("Error read layout.table", ex);
            return;
        }

        // Save network layout
        try {
            FileWriter lstream = new FileWriter(new File(spadeCxt.getPath(), "layout.table").getAbsolutePath());
            BufferedWriter lout = new BufferedWriter(lstream);


            int nodeCount = currentNetwork.getNodeCount();
            double[][] pos = new double[nodeCount][2];

            for (CyNode node : (List<CyNode>) currentNetwork.nodesList()) {
                NodeView nodeView = currentNetworkView.getNodeView(node);

                int id;
                try {
                    id = Integer.parseInt(node.getIdentifier());
                } catch (NumberFormatException ex) {
                    continue;
                }
                if (id > nodeCount) {
                    continue;
                }

                if (nodeView != null) {
                    pos[id][0] = nodeView.getXPosition();
                    pos[id][1] = -1.0 * nodeView.getYPosition();
                }
            }

            for (int i = 0; i < nodeCount; i++) {
                lout.write(pos[i][0] + " " + pos[i][1] + "\n");
            }

            lout.close();
        } catch (IOException ex) {
            CyLogger.getLogger().error("Error read layout.table", ex);
            return;
        }

        if (closeNetwork) {
            //Close the network that the user just left
            Cytoscape.destroyNetwork(currentNetwork);
            //This is the only way to clear the nodeAttributes. I don't really
            //know what it does though; found it by trial-and-error:
            Cytoscape.createNewSession();
            //(It's necessary to clear the nodeAttributes for the sake of
            //mapColor's functionality.
        }
    }

    /**
     * Reads and applies the user-defined network landscaping from a flat file
     * @param layoutFile
     */
    private void loadMetadata(File layoutFile) {
        CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
        CyNetworkView currentNetworkView = Cytoscape.getCurrentNetworkView();

        try {
            int curNode = 0, nodeCount = currentNetwork.getNodeCount();
            double[][] pos = new double[currentNetwork.getNodeCount()][2];

            Scanner scanner = new Scanner(layoutFile);
            while (scanner.hasNextLine()) {
                if (curNode >= nodeCount) {
                    break;
                }
                pos[curNode][0] = scanner.nextDouble();
                pos[curNode][1] = -1.0 * scanner.nextDouble();
                curNode++;
            }

            for (CyNode node : (List<CyNode>) currentNetwork.nodesList()) {
                int id;
                try {
                    id = Integer.parseInt(node.getIdentifier());
                } catch (NumberFormatException ex) {
                    continue;
                }
                if (id > nodeCount) {
                    continue;
                }

                NodeView nodeView = currentNetworkView.getNodeView(node);
                nodeView.setXPosition(pos[id][0]);
                nodeView.setYPosition(pos[id][1]);
            }


        } catch (FileNotFoundException ex) {
            CyLogger.getLogger().error("Error read layout.table", ex);
            return;
        }

        // Apply nesting loaded from nested.txt metadata
        try {
            Scanner scanner = new Scanner(new File(spadeCxt.getPath(), "nested.txt"));
            while (scanner.hasNextLine()) {
                Set nodes = new HashSet();
                for (String id : scanner.nextLine().split(" ")) {
                    try {
                        // Convert back to 0-indexed nodes
                        nodes.add(Cytoscape.getCyNode(Integer.toString(Integer.parseInt(id) - 1)));
                    } catch (NumberFormatException ex) {
                        CyLogger.getLogger().error("Invalid entry in nested.txt", ex);
                    }
                }
                // Apply nesting
                NodeContextMenuItems.MakeNestedNetwork.makeNestedNode(nodes);
            }

        } catch (FileNotFoundException ex) {
            CyLogger.getLogger().debug("Error reading nested.txt", ex);
            return;
        }
    }

    /**
     * Gets the OS
     */
    private String getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) {
            return "windows";
        } else if (os.indexOf("mac") >= 0) {
            return "macintosh";
        } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            return "unix";
        } else {
            JOptionPane.showMessageDialog(null, "Invalid OS detection");
            return "error";
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        FileItem[] files = new FileItem[spadeCxt.getFCSFiles().length];
        int i = 0;
        for (File fi : spadeCxt.getFCSFiles()) {
            files[i] = new FileItem(fi);
            i++;
        }
        FilenameSelect1 = new javax.swing.JComboBox(files);
        FilenameLabel1 = new javax.swing.JLabel();
        ColoringSelect1 = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        RangeSelect1 = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        PDFButton = new javax.swing.JButton();
        CloseButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        NumberEventsLabel = new javax.swing.JLabel();
        PlotContainer = new javax.swing.JScrollPane();
        PValTableContainer = new javax.swing.JScrollPane();
        PValTable = new javax.swing.JTable();
        radioSymmetric1 = new javax.swing.JRadioButton();
        radioAsymmetric1 = new javax.swing.JRadioButton();
        mergeOrderSlider = new javax.swing.JSlider();
        jLabel5 = new javax.swing.JLabel();
        mergeOrderDetails = new javax.swing.JButton();
        jCheckShowNestedIcon = new javax.swing.JCheckBox();

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(429, 754));

        jPanel1.setPreferredSize(new java.awt.Dimension(396, 780));

        FilenameSelect1.setMaximumRowCount(20);
        FilenameSelect1.setSelectedIndex(-1);
        FilenameSelect1.setMinimumSize(null);
        FilenameSelect1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FilenameSelect1ActionPerformed(evt);
            }
        });

        FilenameLabel1.setText("File");

        ColoringSelect1.setMaximumRowCount(20);
        ColoringSelect1.setToolTipText("Select attribute for coloring nodes");
        ColoringSelect1.setMinimumSize(null);
        ColoringSelect1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ColoringSelect1ActionPerformed(evt);
            }
        });

        jLabel3.setText("Coloring Attribute");

        RangeSelect1.setMaximumRowCount(20);
        RangeSelect1.setModel(RangeSelectModel());
        RangeSelect1.setToolTipText("Global sets colorscale using min/max across all files, local uses min/max of selected file");
        RangeSelect1.setMinimumSize(null);
        RangeSelect1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RangeSelect1ActionPerformed(evt);
            }
        });

        jLabel4.setText("Coloring Range");

        PDFButton.setText("Produce PDFs");
        PDFButton.setToolTipText("Generate PDF tree plots using current Cytoscape layout");
        PDFButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PDFButtonActionPerformed(evt);
            }
        });

        CloseButton.setText("Close SPADE");
        CloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseButtonActionPerformed(evt);
            }
        });

        jSeparator1.setForeground(new java.awt.Color(0, 0, 0));

        jSeparator2.setForeground(new java.awt.Color(0, 0, 0));

        NumberEventsLabel.setText("Select a file to display network and bi-axial plot");

        PlotContainer.setMaximumSize(new java.awt.Dimension(32767, 400));
        PlotContainer.setMinimumSize(new java.awt.Dimension(370, 430));

        PValTableContainer.setPreferredSize(new java.awt.Dimension(365, 402));

        PValTable.setAutoCreateRowSorter(true);
        PValTable.setModel(TValTableModel);
        PValTable.setCellSelectionEnabled(true);
        PValTableContainer.setViewportView(PValTable);

        buttonGroup1.add(radioSymmetric1);
        radioSymmetric1.setText("Symmetric");
        radioSymmetric1.setToolTipText("Center coloring range about 0");
        radioSymmetric1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSymmetric1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioAsymmetric1);
        radioAsymmetric1.setSelected(true);
        radioAsymmetric1.setText("Asymmetric");
        radioAsymmetric1.setToolTipText("Center coloring range about mean of attribute range");
        radioAsymmetric1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioAsymmetric1ActionPerformed(evt);
            }
        });

        mergeOrderSlider.setMajorTickSpacing(20);
        mergeOrderSlider.setPaintTicks(true);
        mergeOrderSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                mergeOrderSliderStateChanged(evt);
            }
        });

        jLabel5.setText("Merge Order");
        jLabel5.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabel5.setName("mergeOrderLabel"); // NOI18N

        mergeOrderDetails.setLabel("See Merge Details");
        mergeOrderDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeOrderDetailsActionPerformed(evt);
            }
        });

        jCheckShowNestedIcon.setText("Show Merging Details Dynamically [slow]");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(NumberEventsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(24, 24, 24))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(PValTableContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ColoringSelect1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(FilenameLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(FilenameSelect1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(16, 16, 16)
                                .addComponent(RangeSelect1, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(radioSymmetric1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioAsymmetric1))
                            .addComponent(PlotContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(25, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mergeOrderSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jCheckShowNestedIcon)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mergeOrderDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(111, 111, 111)
                                .addComponent(PDFButton, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(CloseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FilenameLabel1)
                    .addComponent(FilenameSelect1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(ColoringSelect1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RangeSelect1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(radioAsymmetric1)
                    .addComponent(radioSymmetric1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(NumberEventsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PlotContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 418, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PValTableContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mergeOrderSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jCheckShowNestedIcon)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(PDFButton)
                            .addComponent(CloseButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(122, 122, 122))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(mergeOrderDetails)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        mergeOrderDetails.getAccessibleContext().setAccessibleName("mergeOrderDetails");

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 790, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void mergeOrderDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergeOrderDetailsActionPerformed
        try {
            if (mergeOrderOps != null) {
                if ("See Merge Details".equals(mergeOrderDetails.getText())) {
                    mergeOrderDetails.setText("Clear Merge Details");
                    mergeOrderSlider.setEnabled(false);
                    mergeOrderOps.showNestedNetworkDetails();

                } else {
                    mergeOrderOps.destroyNestedNetworkDetails();
                    mergeOrderDetails.setText("See Merge Details");
                    mergeOrderSlider.setEnabled(true);
                }
            }
        } catch (Exception ex) {
            CyLogger.getLogger().debug("", ex);
            mergeOrderSlider.setEnabled(true);
        }
    }//GEN-LAST:event_mergeOrderDetailsActionPerformed

    private void mergeOrderSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_mergeOrderSliderStateChanged
        int mergeOrder = mergeOrderSlider.getValue();
        if (mergeOrderOps != null) {
            try {
                mergeOrder = mergeOrderOps.createNestedGraphIncremental(mergeOrder, jCheckShowNestedIcon.isSelected());
                jLabel5.setText("Merge order: " + String.valueOf(mergeOrder) + " of " + mergeOrderOps.getMaxMergeOrder());
            } catch (Exception ex) {
                CyLogger.getLogger().debug("Exception processing merge order: " + mergeOrder, ex);
            }

            mergeOrderSlider.requestFocusInWindow();
        }
    }//GEN-LAST:event_mergeOrderSliderStateChanged

    private void radioAsymmetric1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioAsymmetric1ActionPerformed
        spadeCxt.setSymmetry(SpadeContext.SymmetryType.ASYMMETRIC);
        updateNodeSizeAndColors();
        Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
    }//GEN-LAST:event_radioAsymmetric1ActionPerformed

    private void radioSymmetric1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioSymmetric1ActionPerformed
        spadeCxt.setSymmetry(SpadeContext.SymmetryType.SYMMETRIC);
        updateNodeSizeAndColors();
        Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
    }//GEN-LAST:event_radioSymmetric1ActionPerformed

    private void CloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseButtonActionPerformed
        Object[] options = new String[]{"Save and Close", "Close", "Cancel"};
        int returnvalue = JOptionPane.showOptionDialog(null, "Save network layout before closing?", "Close SPADE", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (returnvalue == 2) {
            //Cancel;
        } else {
            //If Save
            if (returnvalue == 0) {
                saveMetadata(true);
            }
            //Close
            //FIXME This will fail if the user loads another plug-in after loading SPADE
            Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).remove(Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).getCytoPanelComponentCount() - 1);
        }
    }//GEN-LAST:event_CloseButtonActionPerformed

    private void PDFButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PDFButtonActionPerformed
        // Save current landscaping before generating PDFs
        saveMetadata(false);

        // Create the workflow wizard to walk user through setting up PDF generation
        WorkflowWizard wf = new WorkflowWizard(Cytoscape.getDesktop());

        WorkflowWizard.PanelDescriptor genPDFs = new WorkflowWizardPanels.GeneratePDFs(spadeCxt);
        wf.registerWizardPanel(WorkflowWizardPanels.GeneratePDFs.IDENTIFIER, genPDFs);

        wf.setCurrentPanel(WorkflowWizardPanels.GeneratePDFs.IDENTIFIER);
        int showModalDialog = wf.showModalDialog();

        if (showModalDialog == WorkflowWizard.CANCEL_RETURN_CODE) {
            return;
        } else if (showModalDialog != WorkflowWizard.FINISH_RETURN_CODE) {
            JOptionPane.showMessageDialog(null, "Error occured in workflow wizard.");
        }
    }//GEN-LAST:event_PDFButtonActionPerformed

    private void RangeSelect1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RangeSelect1ActionPerformed
        spadeCxt.setNormalizationKind((SpadeContext.NormalizationKind) RangeSelect1.getSelectedItem());
        updateNodeSizeAndColors();
        Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
    }//GEN-LAST:event_RangeSelect1ActionPerformed

    private void ColoringSelect1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ColoringSelect1ActionPerformed
        updateNodeSizeAndColors();
        Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
    }//GEN-LAST:event_ColoringSelect1ActionPerformed

    private void FilenameSelect1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FilenameSelect1ActionPerformed
        GraphPerspective network = (GraphPerspective) Cytoscape.getCurrentNetwork();

        // Close the current network, saving the X and Y coords for reuse
        //   This is a hackerish way to tell if no network is loaded. For some reason,
        //   Cytoscape.getCurrentNetwork[View]() always returns something.
        if (!network.nodesList().isEmpty()) {
            this.saveMetadata(true);
        }

        //Open the new network, applying the X and Y coords if available
        if (FilenameSelect1.getSelectedIndex() >= 0) {
            LoadNetworkTask.loadFile(spadeCxt.getGMLFiles()[FilenameSelect1.getSelectedIndex()], true);

            //Find the layout.table file if it exists
            File[] layoutFiles = spadeCxt.getPath().listFiles(new FilenameFilter() {

                public boolean accept(File f, String name) {
                    return (name.matches("layout.table"));
                }
            });
            if (layoutFiles.length == 1) {
                loadMetadata(layoutFiles[0]);
            } else if (layoutFiles.length > 1) {
                JOptionPane.showMessageDialog(null, "Error: Found more than one layout.table file");
                return;
            }

            // Network Interactions
            Cytoscape.getCurrentNetworkView().addNodeContextMenuListener(new NodeContextMenu());
            Cytoscape.getCurrentNetworkView().fitContent();

            VisualMapping.populateNumericAttributeComboBox(ColoringSelect1);  // Update the parameter combo box
            ColoringSelect1.setSelectedIndex(0);

            RangeSelect1.setSelectedIndex(0);
            spadeCxt.setNormalizationKind((NormalizationKind) RangeSelect1.getSelectedItem());
            spadeCxt.setSymmetry(radioSymmetric1.isSelected() ? SpadeContext.SymmetryType.SYMMETRIC : SpadeContext.SymmetryType.ASYMMETRIC);

            updateNodeSizeAndColors();

            // FCS file interactions
            NumberEventsLabel.setText("Select a file to display network and bi-axial plot");
            TValTableModel.setRowCount(0);
            TValTableModel.addRow(new Object[]{"Select nodes...", ""});

            this.PlotContainer.setViewportView(null);
            this.scatterPlot = null;
            this.fcsOperations = null;

            File[] mergeOrderFile = spadeCxt.getPath().listFiles(new FilenameFilter() {

                public boolean accept(File f, String name) {
                    return (name.matches("merge_order.txt"));
                }
            });

            try {
                if (mergeOrderFile.length == 1) {
                    this.mergeOrderOps = new MergeOrderOperations(mergeOrderFile[0]);
                    mergeOrderSlider.setMaximum(mergeOrderOps.getMaxMergeOrder());
                } else if (mergeOrderFile.length == 0) {
                    jLabel5.setText("merge_order.txt not found");
                    mergeOrderSlider.setEnabled(false);
                    jCheckShowNestedIcon.setEnabled(false);
                    mergeOrderDetails.setEnabled(false);
                } else {
                    JOptionPane.showMessageDialog(null, "Error: Found more than one merge_order.txt file.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error reading Merge Order file: " + ex.getMessage());
            }

            try {
                this.fcsOperations = new FCSOperations(((FileItem) FilenameSelect1.getSelectedItem()).getFCSFile());
                this.scatterPlot = new ScatterPlotPanel(this.fcsOperations);
                this.PlotContainer.setViewportView(this.scatterPlot);
                Cytoscape.getCurrentNetwork().addSelectEventListener(new HandleSelect());
                updateFCSConsumers();
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(null, "FCS file not found: " + ((FileItem) FilenameSelect1.getSelectedItem()).getFCSpath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error reading FCS file: " + FilenameSelect1.getSelectedItem());
            }
        }
    }//GEN-LAST:event_FilenameSelect1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CloseButton;
    private javax.swing.JComboBox ColoringSelect1;
    private javax.swing.JLabel FilenameLabel1;
    private javax.swing.JComboBox FilenameSelect1;
    private javax.swing.JLabel NumberEventsLabel;
    private javax.swing.JButton PDFButton;
    private javax.swing.JTable PValTable;
    private javax.swing.JScrollPane PValTableContainer;
    private javax.swing.JScrollPane PlotContainer;
    private javax.swing.JComboBox RangeSelect1;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox jCheckShowNestedIcon;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton mergeOrderDetails;
    private javax.swing.JSlider mergeOrderSlider;
    private javax.swing.JRadioButton radioAsymmetric1;
    private javax.swing.JRadioButton radioSymmetric1;
    // End of variables declaration//GEN-END:variables
}
