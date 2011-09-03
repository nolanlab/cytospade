package cytospade;

import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.cytopanels.CytoPanelImp;
import cytoscape.*;
import cytoscape.actions.LoadNetworkTask;
import cytoscape.data.CyAttributes;

import cytoscape.data.SelectEventListener;
import cytoscape.logger.CyLogger;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.GlobalAppearanceCalculator;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyDependency;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytospade.SpadePanel;
import cytospade.ui.NodeContextMenu;
import cytospade.ui.NodeContextMenuItems;


import facs.CanvasSettings;
import facs.Plot2D;
import giny.model.GraphPerspective;
import giny.model.Node;
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
import java.util.Comparator;
import java.util.HashSet;

import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
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
import javax.swing.JTextArea;

/**
 * Cytoscape plugin that draws scatter plots for SPADE trees
 */
public class CytoSpade extends CytoscapePlugin {

    private SPADEContext spadeCxt;
    private SPADEdraw spadeDraw;

    /**
     * Creates an action and adds it to the Plugins menu.
     */
    public CytoSpade() {
        // Initialized internal state keeping
        spadeCxt = new SPADEContext();

        // Create menu bar item, along with associated action...
        spadeDraw = new SPADEdraw();
        spadeDraw.setPreferredMenu("Plugins");
        Cytoscape.getDesktop().getCyMenus().addAction(spadeDraw);
    }

    /**
     * Event to save the user-defined network landscaping when Cytoscape exits.
     */
    @Override
    public void onCytoscapeExit() {
        //saveMetadata(true);
        if (spadeDraw != null) {
            spadeDraw.onExit();
        }

    }

    /**
     * This class gets attached to the menu item.
     */
    public class SPADEdraw extends CytoscapeAction {

        SpadePanel spadePanel;

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
                //        SpadePanel spadePanel = new SpadePanel(spadeCxt);
                spadePanel = new SpadePanel(spadeCxt);
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

        /**
         * This method is called to clean-up on exit
         */
        public void onExit() {
            if (spadePanel != null) {
                spadePanel.onExit();
            }
        }
    }
}