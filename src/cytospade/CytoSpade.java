package cytospade;

import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.cytopanels.CytoPanelImp;
import cytoscape.*;
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


import facs.CanvasSettings;
import facs.Plot2D;
import giny.model.GraphPerspective;
import giny.view.NodeView;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;

import javax.swing.SwingWorker;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;
import org.apache.commons.math.MathException;

import org.apache.commons.math.stat.inference.TTestImpl;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math.linear.RealMatrix;

import java.lang.StringBuffer;

/**
 * Cytoscape plugin that draws scatter plots for SPADE trees
 */
public class CytoSpade extends CytoscapePlugin {

    private SPADEContext spadeCxt;

    /**
     * Creates an action and adds it to the Plugins menu.
     */
    public CytoSpade() {
        // Initialized internal state keeping
        spadeCxt = new SPADEContext();

        //create a new action to respond to menu activation
        SPADEdraw action = new SPADEdraw();
        //set the preferred menu
        action.setPreferredMenu("Plugins");
        //and add it to the menus
        Cytoscape.getDesktop().getCyMenus().addAction(action);
    }

    /**
     * Event to save the user-defined network landscaping when Cytoscape exits.
     */
    @Override
    public void onCytoscapeExit() {
        saveLandscaping(true);
    }

    /**
     * Saves the user-defined network landscaping to a flat file
     * @param closeNetwork - whether or not to close the network after saving it.
     */
    private void saveLandscaping(Boolean closeNetwork) {
        CyNetworkView cnv = Cytoscape.getCurrentNetworkView();
        GraphPerspective network = (GraphPerspective) Cytoscape.getCurrentNetwork();
        Iterator<CyNode> nodesIt;
        if (!network.nodesList().isEmpty()) {
            try {
                FileWriter fstream = new FileWriter(new File(spadeCxt.getPath(), "layout.table").getAbsolutePath());
                BufferedWriter out = new BufferedWriter(fstream);

                nodesIt = network.nodesIterator();

                //This stupid iterator runs backward. So reverse the list first
                double[] xPositions = new double[network.nodesList().size()];
                double[] yPositions = new double[network.nodesList().size()];
                int ii = network.nodesList().size() - 1;
                while (nodesIt.hasNext()) {
                    giny.model.Node cytoNode = (giny.model.Node) nodesIt.next();
                    NodeView nodeView = cnv.getNodeView(cytoNode);
                    if (nodeView == null) {
                        JOptionPane.showMessageDialog(null, "Error: null nodeView");
                    }
                    xPositions[ii] = nodeView.getXPosition();
                    //Multiply by -1 to flip map
                    yPositions[ii] = -1 * nodeView.getYPosition();
                    ii--;
                }

                //Now write the list out
                for (int i = 0; i < network.nodesList().size(); i++) {
                    out.write(xPositions[i] + " ");
                    out.write(yPositions[i] + "\n");
                }

                out.close();
            } catch (IOException ex) {
                Logger.getLogger(CytoSpade.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        }

        if (closeNetwork) {
            //Close the network that the user just left
            Cytoscape.destroyNetwork(Cytoscape.getCurrentNetwork());
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
    private void readLandscaping(File layoutFile) {
        CyNetworkView cnv = Cytoscape.getCurrentNetworkView();
        GraphPerspective network = (GraphPerspective) Cytoscape.getCurrentNetwork();
        Iterator<CyNode> nodesIt;
        try {
            FileReader fstream = new FileReader(layoutFile.getAbsolutePath());
            BufferedReader in = new BufferedReader(fstream);
            String[] line = new String[2];

            cnv = Cytoscape.getCurrentNetworkView();
            network = (GraphPerspective) Cytoscape.getCurrentNetwork();
            nodesIt = network.nodesIterator();

            //This stupid iterator runs backward. So reverse the list first
            double[] xPositions = new double[network.nodesList().size()];
            double[] yPositions = new double[network.nodesList().size()];
            for (int i = network.nodesList().size() - 1; i > -1; i--) {
                try {
                    line = in.readLine().split(" ");
                } catch (IOException ex) {
                    Logger.getLogger(CytoSpade.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(null, "IO error while reading in relandscaped network");
                }
                xPositions[i] = Double.parseDouble(line[0]);
                yPositions[i] = Double.parseDouble(line[1]);
            }

            //Now apply it.
            int ii = 0;
            while (nodesIt.hasNext()) {
                giny.model.Node cytoNode = (giny.model.Node) nodesIt.next();
                NodeView nodeView = cnv.getNodeView(cytoNode);
                if (nodeView != null) {
                    nodeView.setXPosition(xPositions[ii]);
                    //Multiply by -1 to flip the map
                    nodeView.setYPosition(-1 * yPositions[ii]);
                    ii++;
                }

            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(CytoSpade.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets the OS
     */
    public String getOS() {
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

    /**
     * SPADE analysis control panel.
     */
    class SpadePanel extends JPanel {
        private JTextField pValTextBox;

        public SpadePanel(SPADEContext spadeCxt) {
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

            initComponents();
        }

        private void initComponents() {
            // <editor-fold defaultstate="collapsed" desc="GUI Code">
            javax.swing.JButton closeButtonWest = new javax.swing.JButton();
            closeButtonWest.setText("Close");

            javax.swing.JButton drawPlotsButton = new javax.swing.JButton();
            drawPlotsButton.setText("Produce PDFs");
            drawPlotsButton.setToolTipText("Generate PDF tree plots using current Cytoscape layout");

            javax.swing.JButton makePivotTableButton = new javax.swing.JButton();
            makePivotTableButton.setText("Produce Tables");
            makePivotTableButton.setToolTipText("Generate CSV tables for each attributes with columns for all files");

            javax.swing.JLabel FilenameLbl = new javax.swing.JLabel("File");
            filenameComboBox = new javax.swing.JComboBox(spadeCxt.getFCSFiles());
            filenameComboBox.setMaximumRowCount(20);
            filenameComboBox.setRenderer(new javax.swing.ListCellRenderer() {
                // Render FCS files as just File name (no path information, or long extensions)

                public Component getListCellRendererComponent(JList jlist, Object o, int idx, boolean isSelected, boolean bln1) {
                    String name = "";
                    if (o != null) {
                        name = ((File) o).getName();
                        name = name.substring(0, name.lastIndexOf(".density.fcs.cluster.fcs"));
                    }
                    JLabel label = new JLabel(name);

                    label.setBackground(isSelected ? jlist.getSelectionBackground() : jlist.getBackground());
                    label.setForeground(isSelected ? jlist.getSelectionForeground() : jlist.getForeground());
                    label.setEnabled(jlist.isEnabled());
                    label.setFont(jlist.getFont());
                    label.setOpaque(true);

                    return label;
                }
            });
            filenameComboBox.setSelectedIndex(-1);
            filenameComboBox.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    filenameComboBoxActionPerformed(evt);
                }
            });


            javax.swing.JLabel colorscaleLabel = new javax.swing.JLabel("Coloring attribute");
            colorscaleComboBox = new javax.swing.JComboBox();
            colorscaleComboBox.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    colorscaleComboBoxActionPerformed(evt);
                }
            });
            colorscaleComboBox.setToolTipText("Select attribute for coloring nodes");

            javax.swing.JLabel colorrangeLabel = new javax.swing.JLabel("Coloring range");
            colorrangeComboBox = new javax.swing.JComboBox();
            colorrangeComboBox.setModel(new javax.swing.DefaultComboBoxModel(
                    visualMapping.globalRangeAvailable()
                    ? new VisualMapping.RangeKind[]{VisualMapping.RangeKind.GLOBAL, VisualMapping.RangeKind.LOCAL}
                    : new VisualMapping.RangeKind[]{VisualMapping.RangeKind.LOCAL}));
            colorrangeComboBox.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    colorrangeComboBoxActionPerformed(evt);
                }
            });
            colorrangeComboBox.setToolTipText("Global sets colorscale using min/max across all files, local uses min/max of selected file");

            javax.swing.JLabel howtoadjust = new javax.swing.JLabel("Click axis label to change parameter and scale");

            javax.swing.JLabel StyleLbl = new javax.swing.JLabel("Style");
            plotStyleComboBox = new javax.swing.JComboBox();
            plotStyleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Shaded Contour", "Dot", "Density Dot", "Shadow", "Contour", "Density"}));


            jLabelPlot = new javax.swing.JLabel();
            jLabelPlot.setBounds(0, 0, 358, 358);

            countLabel = new javax.swing.JLabel();            
            xAxisClickable = new javax.swing.JLabel();
            xAxisClickable.setBounds(48, 311, 308, 46);

            yAxisClickable = new javax.swing.JLabel();
            yAxisClickable.setBounds(0, 0, 46, 308);
            
            pValTextBox = new javax.swing.JTextField(); 

            plotArea = new javax.swing.JLayeredPane();


            plotStyleComboBox.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    plotStyleComboActionPerformed(evt);
                }
            });

            closeButtonWest.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    closeButtonWestClicked(evt);
                }
            });

            drawPlotsButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    generatePDFsClicked(evt);
                }
            });

            makePivotTableButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    makePivotTableClicked(ae);
                }
            });

            xAxisPopup = new javax.swing.JPopupMenu();
            yAxisPopup = new javax.swing.JPopupMenu();

            xAxisClickable.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent me) {
                    xAxisPopup.show(xAxisClickable, 154, 23);
                }
            });

            yAxisClickable.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent me) {
                    yAxisPopup.show(yAxisClickable, 23, 154);
                }
            });

            plotArea.add(xAxisClickable);
            plotArea.add(yAxisClickable);
            plotArea.add(jLabelPlot);

            //Platform-dependent width of small comboboxes
            int combowidth = 125;
            if (getOS().equals("windows")) {
                combowidth = 75;
            }

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(howtoadjust)
                        .addComponent(plotArea, 358, 358, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(StyleLbl)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(plotStyleComboBox, 0, 165, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(FilenameLbl)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(filenameComboBox, 0, 165, Short.MAX_VALUE))))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(colorscaleLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(colorscaleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(colorrangeLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(colorrangeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(countLabel)
                        .addComponent(pValTextBox)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(drawPlotsButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(makePivotTableButton))
                        .addComponent(closeButtonWest))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(FilenameLbl)
                        .addComponent(filenameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(colorscaleLabel)
                        .addComponent(colorscaleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(colorrangeLabel)
                        .addComponent(colorrangeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(howtoadjust)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(StyleLbl)
                        .addComponent(plotStyleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(plotArea, 358, 358, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(countLabel)
                    .addComponent(pValTextBox)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(drawPlotsButton)
                        .addComponent(makePivotTableButton))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(closeButtonWest)
                    .addContainerGap(19, Short.MAX_VALUE))
            );

            // </editor-fold>
        }

        /**
         * Handles node selection events
         */
        public class HandleSelect implements SelectEventListener {

            public HandleSelect() {
            }

            public void onSelectEvent(cytoscape.data.SelectEvent e) {
                (new drawScatterThread()).execute();
            }
        }

        public class XactionPerformed implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().matches("Linear")
                        || e.getActionCommand().matches("Log")
                        || e.getActionCommand().matches("Arcsinh: CyTOF")
                        || e.getActionCommand().matches("Arcsinh: Fluor")) {
                    xChanScale = e.getActionCommand();
                } else {
                    xChanParam = SPADEContext.getShortNameFromFormattedName(e.getActionCommand());
                }
                (new drawScatterThread()).execute();
            }
        }

        public class YactionPerformed implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().matches("Linear")
                        || e.getActionCommand().matches("Log")
                        || e.getActionCommand().matches("Arcsinh: CyTOF")
                        || e.getActionCommand().matches("Arcsinh: Fluor")) {
                    yChanScale = e.getActionCommand();
                } else {
                    yChanParam = SPADEContext.getShortNameFromFormattedName(e.getActionCommand());
                }
                (new drawScatterThread()).execute();
            }
        }

        /**
         * When the filenameComboBox is changed, changes the display if the selection
         * isn't null (item 0), applies the landscaping, adds a SelectEvent listener,
         * zooms to the network, draws a plot, adds color scaling attributes to the
         * combo box, and maps node sizes. Eventually will map colors too.
         * @param evt
         */
        private void filenameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {

            CyNetworkView cnv = Cytoscape.getCurrentNetworkView();
            GraphPerspective network = (GraphPerspective) Cytoscape.getCurrentNetwork();

            //Close the current network, saving the X and Y coords for reuse
            //This is a hackerish way to tell if no network is loaded. For some reason,
            //Cytoscape.getCurrentNetwork[View]() always returns something.
            if (!network.nodesList().isEmpty()) {
                saveLandscaping(true);
            }

            //Open the new network, applying the X and Y coords if available
            if (filenameComboBox.getSelectedIndex() >= 0) {
                LoadNetworkTask.loadFile(spadeCxt.getGMLFiles()[filenameComboBox.getSelectedIndex()], true);

                //Find the layout.table file if it exists
                File[] layoutFiles = spadeCxt.getPath().listFiles(new FilenameFilter() {

                    public boolean accept(File f, String name) {
                        return (name.matches("layout.table"));
                    }
                });
                if (layoutFiles.length == 1) {
                    readLandscaping(layoutFiles[0]);
                } else if (layoutFiles.length > 1) {
                    JOptionPane.showMessageDialog(null, "Error: Found more than one layout.table file");
                    return;
                }

                // Add listener for updating dot plot based on user node selection
                Cytoscape.getCurrentNetwork().addSelectEventListener(new HandleSelect());

                //Zoom to the network
                Cytoscape.getCurrentNetworkView().fitContent();

                // Update the parameter combo box
                VisualMapping.populateNumericAttributeComboBox(colorscaleComboBox);

                colorscaleComboBox.setMaximumRowCount(colorscaleComboBox.getItemCount());
                colorscaleComboBox.setSelectedIndex(0);


                // Update plot combo boxes with channels in new FCS files
                xAxisPopup = new javax.swing.JPopupMenu();
                yAxisPopup = new javax.swing.JPopupMenu();
                for (String scales : new String[]{"Linear", "Log", "Arcsinh: CyTOF", "Arcsinh: Fluor"}) {
                    menuItem = new JMenuItem(scales);
                    menuItem.addActionListener(new SpadePanel.XactionPerformed());
                    xAxisPopup.add(menuItem);
                    menuItem = new JMenuItem(scales);
                    menuItem.addActionListener(new SpadePanel.YactionPerformed());
                    yAxisPopup.add(menuItem);
                }

                xAxisPopup.addSeparator();
                yAxisPopup.addSeparator();

                fcsFile FCSInputFile = null;
                try {
                    FCSInputFile = new fcsFile((File) filenameComboBox.getSelectedItem(), true);
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "File not found.");
                    return;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error reading file.");
                    return;
                }

                {  // Build alphabetized channel selector
                    String[] names = new String[FCSInputFile.getChannelCount()];
                    for (int i = 0; i < FCSInputFile.getChannelCount(); i++) {
                        names[i] = SPADEContext.getFCSChannelFormattedName(FCSInputFile, i);
                    }
                    Arrays.sort(names);
                    for (int i = 0; i < FCSInputFile.getChannelCount(); i++) {
                        menuItem = new JMenuItem(names[i]);
                        menuItem.addActionListener(new SpadePanel.XactionPerformed());
                        xAxisPopup.add(menuItem);
                        menuItem = new JMenuItem(names[i]);
                        menuItem.addActionListener(new SpadePanel.YactionPerformed());
                        yAxisPopup.add(menuItem);
                    }
                }

                // Initialize plot axes parameters
                xChanScale = "Log";
                yChanScale = "Log";
                if (FCSInputFile.getChannelCount() > 0) {
                    xChanParam = FCSInputFile.getChannelShortName(0);
                    yChanParam = FCSInputFile.getChannelShortName(0);
                }

                //Draw a plot
                (new drawScatterThread()).execute();

                mapSizeAndColors();

            } else {
                //If the user selected the empty first row, clear the display
                countLabel.setText(null);
                pValTextBox.setText(null);
                jLabelPlot.setIcon(null);
            }

        }

        /*
         * Wraps the plotting function in a worker thread
         */
        public class drawScatterThread extends SwingWorker<Integer, Void> {

            private int COUNT;
            private double[] datax;
            private double[] datay;
            private double[] dataAx;
            private double[] dataAy;
            private double xChanMax;
            private double yChanMax;

            @Override
            protected Integer doInBackground() {
                try {
                    //These need to be null if there are no events selected, otherwise all
                    //events will be in the background and foreground.
                    //Fear not, populateData() makes them not-null if nodes are selected.
                    dataAx = null;
                    dataAy = null;

                    populateData();

                    int xDisplay, yDisplay;

                    if (xChanScale.matches("Linear")) {
                        xDisplay = Plot2D.LINEAR_DISPLAY;
                    } else if (xChanScale.matches("Log")) {
                        xDisplay = Plot2D.LOG_DISPLAY;
                    } else if (xChanScale.matches("Arcsinh: CyTOF")) {
                        xDisplay = Plot2D.ARCSINH_DISPLAY_CYTOF;
                    } else {
                        xDisplay = Plot2D.ARCSINH_DISPLAY_FLUOR;
                    }

                    if (yChanScale.matches("Linear")) {
                        yDisplay = Plot2D.LINEAR_DISPLAY;
                    } else if (yChanScale.matches("Log")) {
                        yDisplay = Plot2D.LOG_DISPLAY;
                    } else if (yChanScale.matches("Arcsinh: CyTOF")) {
                        yDisplay = Plot2D.ARCSINH_DISPLAY_CYTOF;
                    } else {
                        yDisplay = Plot2D.ARCSINH_DISPLAY_FLUOR;
                    }

                    //{ "Shaded Contour", "Dot", "Density Dot", "Shadow", "Contour", "Density" }
                    int plottype = 0;
                    if (plotStyleComboBox.getSelectedIndex() == 0) {
                        plottype = facs.Illustration.SHADED_CONTOUR_PLOT;
                    } else if (plotStyleComboBox.getSelectedIndex() == 1) {
                        plottype = facs.Illustration.DOT_PLOT;
                    } else if (plotStyleComboBox.getSelectedIndex() == 2) {
                        plottype = facs.Illustration.DENSITY_DOT_PLOT;
                    } else if (plotStyleComboBox.getSelectedIndex() == 3) {
                        plottype = facs.Illustration.SHADOW_PLOT;
                    } else if (plotStyleComboBox.getSelectedIndex() == 4) {
                        plottype = facs.Illustration.CONTOUR_PLOT;
                    } else if (plotStyleComboBox.getSelectedIndex() == 5) {
                        plottype = facs.Illustration.DENSITY_PLOT;
                    }

                    // Note that the size is set by axisBins

                    int dotSize;
                    if (COUNT > 5000) {
                        dotSize = 1;
                    } else if (COUNT > 1000) {
                        dotSize = 2;
                    } else if (COUNT > 10) {
                        dotSize = 3;
                    } else {
                        // Very small numbers of events make contours meaningless
                        // so we automatically switch to dot plots in this scenario
                        plottype = facs.Illustration.DOT_PLOT;
                        dotSize = 3;
                    }
                    // TODO: Document these options!
                    CanvasSettings cs = CanvasSettings.getCanvasSettings(
                            10, 10, 0, 1, 2,
                            plottype, facs.Illustration.DEFAULT_COLOR_SET,
                            false, true, true, true, true, false, 300, 1.0d, 1.0d,
                            10.0d, // Note this choices interact with small event check above
                            10.0d,
                            facs.Illustration.DEFAULT_POPULATION_TYPE, COUNT, dotSize);
                    BufferedImage image = facs.Plot2D.drawPlot(
                            cs,
                            datax, datay, dataAx, dataAy,
                            (String) xChanParam, (String) yChanParam,
                            xChanMax, yChanMax,
                            xDisplay, yDisplay);
                    jLabelPlot.setIcon(new ImageIcon(image));
                    return 0;
                } catch (IOException ex) {
                    Logger.getLogger(CytoSpade.class.getName()).log(Level.SEVERE, null, ex);
                    return 1;
                }
            }

            /**
             * Get selected nodes, return null if no nodes are selected
             */
            private int[] getSelectedNodes() {
                int[] selectedClust = null;
                CyNetwork current_network = Cytoscape.getCurrentNetwork();
                if (current_network != null) {
                    Set selectedNodes = current_network.getSelectedNodes();
                    if (selectedNodes.isEmpty()) {
                        //selectedClust = null; //Do nothing, selectedClust is initialized.
                    } else if (selectedNodes.size() > 0) {
                        Object[] nds = (Object[]) selectedNodes.toArray(new Object[1]);
                        selectedClust = new int[nds.length];
                        for (int i = 0; i < nds.length; i++) {
                            selectedClust[i] = Integer.parseInt(nds[i].toString()) + 1; //Plus 1!
                        }
                    }
                }
                return selectedClust;
            }

            /**
             * Populates selectedEvents
             */
            private Array2DRowRealMatrix populateSelectedEvents(
                    Array2DRowRealMatrix events,
                    int[] selectedClust,
                    int clusterColumn
                    ) {
                ArrayList<Integer> columns = new ArrayList<Integer>();

                //Populate selectedEventsInitial                        
                for (int i = 0; i < events.getColumnDimension(); i++) {
                    int cluster = (int) events.getEntry(clusterColumn,i);
                    for (int j = 0; j < selectedClust.length; j++) {
                        if (cluster == selectedClust[j]) {
                            columns.add(i);
                        }
                    }
                }

                int[] rows = new int[events.getRowDimension()];
                for (int i=0; i<rows.length; i++) rows[i] = i;

                return (Array2DRowRealMatrix) events.getSubMatrix(rows, ArrayUtils.toPrimitive(columns.toArray(new Integer[0])));
            }

            public class TTestResult implements Comparable {

                public TTestResult(String name, double pValue) {
                    this.name = name;
                    this.pValue = pValue;
                }

                public int compareTo(Object t) {
                    TTestResult rhs = (TTestResult)t;
                    if (this.pValue < rhs.pValue) return -1;
                    else if(this.pValue > rhs.pValue) return 1;
                    else return 0;
                }

                public double pValue;
                public String name;

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
                    return tTest.tTest(selectedEvents.getDataRef()[attribute], allEvents.getDataRef()[attribute]);
                } catch (IllegalArgumentException ex) {
                    CyLogger.getLogger(SPADEController.class.getName()).error(null, ex);
                } catch (MathException ex) {
                    CyLogger.getLogger(SPADEController.class.getName()).error(null, ex);
                }
                return 1.0;
            }

            /**
             * Populates the data[A]{X,Y} arrays based on the selected file and the
             * selected node(s). (Uses only global variables.)
             */
            private void populateData() {

                //Get the selected nodes; returns null if no nodes selected
                int[] selectedClust = getSelectedNodes();

                //Open the FCS file
                fcsFile FCSInputFile = null;
                try {
                    FCSInputFile = new fcsFile((File) filenameComboBox.getSelectedItem(), true);
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "File not found.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error reading file.");
                }

                //Pull the events list
                double[][] events = FCSInputFile.getCompensatedEventList();
                int        num_events = FCSInputFile.getEventCount();

               
                //Find the columns with the appropriate parameters
                int xChan = FCSInputFile.getChannelIdFromShortName(xChanParam);
                if (xChan < 0)
                    xChan = 0;

                int yChan = FCSInputFile.getChannelIdFromShortName(yChanParam);
                if (yChan < 0)
                    yChan = 0;

                
                //The cluster channel is always the last
                int clustChan = FCSInputFile.getChannelCount() - 1;

                DecimalFormat df = new DecimalFormat();

                if (selectedClust == null) {
                    datax = events[xChan];
                    datay = events[yChan];

                    countLabel.setText("Displaying " + df.format(num_events) + " of " + df.format(num_events) + " events");
                    COUNT = num_events;
                } else {

                    Array2DRowRealMatrix eventsInitl = new Array2DRowRealMatrix(events);
                    Array2DRowRealMatrix eventsSlctd = populateSelectedEvents(
                            eventsInitl,
                            selectedClust,
                            FCSInputFile.getChannelIdFromShortName("cluster")
                            );

                    ArrayList<TTestResult> pValues = new ArrayList<TTestResult>();
                    for (int i=0; i < FCSInputFile.getNumChannels(); i++) {
                        String name = FCSInputFile.getChannelShortName(i);
                        if (name.contentEquals("Time") || name.contentEquals("time")|| name.contentEquals("cluster") || name.contentEquals("density"))
                            continue;

                        pValues.add(new TTestResult(name,tTest(eventsSlctd, eventsInitl, i)));
                    }
                    Collections.sort(pValues);

                    for (int i=0; i < ((pValues.size() < 5) ? pValues.size() : 5); i++)
                        System.out.println("P-Value for "+pValues.get(i).name+": "+pValues.get(i).pValue);
                    System.out.println("");

               
                    dataAx = eventsInitl.getDataRef()[xChan];  // Background events
                    dataAy = eventsInitl.getDataRef()[yChan];
                    datax  = eventsSlctd.getDataRef()[xChan];  // Foreground events
                    datay  = eventsSlctd.getDataRef()[yChan];
                    COUNT  = eventsSlctd.getColumnDimension();
                    countLabel.setText("Displaying " + df.format(COUNT) + " of " + df.format(num_events) + " events");                    
                    StringBuffer sb = new StringBuffer(500);
                    for (int i=0; i < ((pValues.size() < 5) ? pValues.size() : 5); i++) {                        
                        sb = sb.append("P-Value for ").append(pValues.get(i).name).
                                append(": ").append(pValues.get(i).pValue).append("\n");               
                    }
                    pValTextBox.setText(sb.toString());
                }

                xChanMax = FCSInputFile.getChannelRange(xChan);
                yChanMax = FCSInputFile.getChannelRange(yChan);

            }
        }

        /**
         * Applies sizes and colors to the network view
         */
        private void mapSizeAndColors() {
            // Skip mapping if no file is specified
            if ((filenameComboBox.getSelectedIndex() < 0) || (colorscaleComboBox.getSelectedIndex() < 0)) {
                return;
            }

            try {
                visualMapping.setCurrentMarkersAndRangeKind(
                        "percenttotal",
                        colorscaleComboBox.getSelectedItem().toString(),
                        (VisualMapping.RangeKind) colorrangeComboBox.getSelectedItem());
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
                spadeVS.setGlobalAppearanceCalculator(globalAppCalc);

                NodeAppearanceCalculator nodeAppCalc = new NodeAppearanceCalculator();
                nodeAppCalc.setCalculator(visualMapping.createColorCalculator());
                nodeAppCalc.setCalculator(visualMapping.createSizeCalculator());
                spadeVS.setNodeAppearanceCalculator(nodeAppCalc);

                // Set a few defaults now that we have overwritten the calculators
                VisualPropertyType.NODE_SHAPE.setDefault(spadeVS, cytoscape.visual.NodeShape.ELLIPSE);
                VisualPropertyType.NODE_FILL_COLOR.setDefault(spadeVS, Color.LIGHT_GRAY);
                spadeVS.getDependency().set(VisualPropertyDependency.Definition.NODE_SIZE_LOCKED, true);

                cyVMM.getCalculatorCatalog().addVisualStyle(spadeVS);
                cyVMM.setVisualStyle(spadeVS);
                Cytoscape.getCurrentNetworkView().setVisualStyle(spadeVS.getName());

            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(null, "Visual Mapping Error: " + e);
            }
        }

        private void plotStyleComboActionPerformed(java.awt.event.ActionEvent evt) {
            (new drawScatterThread()).execute();
        }

        private void colorrangeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
            mapSizeAndColors();
            Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
        }

        private void colorscaleComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
            mapSizeAndColors();
            Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
        }

        /**
         * Closes the WEST CytoSPADE pane
         * @param evt
         */
        private void closeButtonWestClicked(java.awt.event.ActionEvent evt) {
            int returnvalue = JOptionPane.showConfirmDialog(null, "Close SPADE plug-in?", "Confirm close", JOptionPane.OK_CANCEL_OPTION);
            if (returnvalue == JOptionPane.OK_OPTION) {
                saveLandscaping(true);
                //FIXME This will fail if the user loads another plug-in after loading SPADE
                Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).remove(Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).getCytoPanelComponentCount() - 1);
                return;
            } else {
                return;
            }
        }

        private void generatePDFsClicked(ActionEvent evt) {
            // Save current landscaping before generating PDFs
            saveLandscaping(false);

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
        }

        private void makePivotTableClicked(ActionEvent ae) {
            try {
                spadeCxt.authorMakePivot("pivotSPADE.R");
                SPADEController ctl = new SPADEController(spadeCxt.getPath(), "pivotSPADE.R");
                ctl.exec();
            } catch (IOException ex) {
                CyLogger.getLogger(CytoSpade.class.getName()).error(null, ex);
            }
        }

        private SPADEContext spadeCxt;
        private VisualMapping visualMapping;
        private String xChanScale;
        private String yChanScale;
        private String xChanParam;
        private String yChanParam;
        //West panel controls
        private javax.swing.JComboBox plotStyleComboBox;
        private javax.swing.JComboBox colorscaleComboBox;
        private javax.swing.JComboBox filenameComboBox;
        private javax.swing.JComboBox colorrangeComboBox;
        private javax.swing.JLabel jLabelPlot; // contains the rendered plot
        private javax.swing.JLabel countLabel;
        private javax.swing.JLabel xAxisClickable; // Captures the axis click events
        private javax.swing.JLabel yAxisClickable;
        private javax.swing.JLayeredPane plotArea;
        private javax.swing.JPopupMenu xAxisPopup;
        private javax.swing.JPopupMenu yAxisPopup;
        private javax.swing.JMenuItem menuItem;
    }

    /**
     * This class gets attached to the menu item.
     */
    public class SPADEdraw extends CytoscapeAction {

        /**
         * Sets the text that should appear on the menu item.
         */
        public SPADEdraw() {
            super("SPADE");
        }

        /**
         * This method is called when the user selects the menu item.
         */
        public void actionPerformed(ActionEvent ae) {

            // Create the workflow wizard to walk user through setting up processing/analysis
            WorkflowWizard wf = new WorkflowWizard(Cytoscape.getDesktop());

            WorkflowWizard.PanelDescriptor intro = new WorkflowWizardPanels.Intro(spadeCxt);
            wf.registerWizardPanel(WorkflowWizardPanels.Intro.IDENTIFIER, intro);

            WorkflowWizard.PanelDescriptor cluster = new WorkflowWizardPanels.ClusterMarkerSelect(spadeCxt);
            wf.registerWizardPanel(WorkflowWizardPanels.ClusterMarkerSelect.IDENTIFIER, cluster);

            WorkflowWizard.PanelDescriptor panels = new WorkflowWizardPanels.PanelCreator(spadeCxt);
            wf.registerWizardPanel(WorkflowWizardPanels.PanelCreator.IDENTIFIER, panels);

            WorkflowWizard.PanelDescriptor summary = new WorkflowWizardPanels.SummaryAndRun(spadeCxt);
            wf.registerWizardPanel(WorkflowWizardPanels.SummaryAndRun.IDENTIFIER, summary);

            wf.setCurrentPanel(WorkflowWizardPanels.Intro.IDENTIFIER);
            int showModalDialog = wf.showModalDialog();


            if (showModalDialog == WorkflowWizard.CANCEL_RETURN_CODE) {
                return;
            } else if (showModalDialog != WorkflowWizard.FINISH_RETURN_CODE) {
                JOptionPane.showMessageDialog(null, "Error occured in workflow wizard.");
            }


            if (spadeCxt.getWorkflowKind() == SPADEContext.WorkflowKind.ANALYSIS) {
                //Create a tab panel for SPADE controls
                CytoPanelImp ctrlPanel = (CytoPanelImp) Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST);
                SpadePanel spadePanel = new SpadePanel(spadeCxt);
                ctrlPanel.add("SPADE", spadePanel);

                //Set the focus on the panel
                Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).setSelectedIndex(
                        Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).getCytoPanelComponentCount() - 1);

                //This setPrefferedSize(getSize + 1), setPrefferedSize(getSize - 1)
                //is seemingly required to prevent violent behavior of the pack
                //method and to force pack to actually relayout the components
                Cytoscape.getDesktop().setPreferredSize(new Dimension(
                        Cytoscape.getDesktop().getSize().width + 1,
                        Cytoscape.getDesktop().getSize().height + 1));
                Cytoscape.getDesktop().setPreferredSize(new Dimension(
                        Cytoscape.getDesktop().getSize().width - 1,
                        Cytoscape.getDesktop().getSize().height - 1));
                Cytoscape.getDesktop().pack();
            }

        }
    }
}