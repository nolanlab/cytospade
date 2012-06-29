package cytospade;

import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.cytopanels.CytoPanelImp;
import cytoscape.*;
import cytospade.ui.SpadeAnalysisPanel;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.SwingConstants;


/**
 * Cytoscape plugin that draws scatter plots for SPADE trees
 */
public class CytoSpade extends CytoscapePlugin {

    private SpadeContext spadeCxt;
    private SpadeDraw spadeDraw;

    /**
     * Creates an action and adds it to the Plugins menu.
     */
    public CytoSpade() {
        // Initialized internal state keeping
        spadeCxt = new SpadeContext();

        // Create menu bar item, along with associated action...
        spadeDraw = new SpadeDraw();
        spadeDraw.setPreferredMenu("Plugins");
        Cytoscape.getDesktop().getCyMenus().addAction(spadeDraw);
    }

    /**
     * Event to save the user-defined network landscaping when Cytoscape exits.
     */
    @Override
    public void onCytoscapeExit() {

        if (spadeDraw != null) {
            spadeDraw.onExit();
        }
    }

    /**
     * This class gets attached to the menu item.
     */
    public class SpadeDraw extends CytoscapeAction {

        SpadeAnalysisPanel analysisPanel;

        /**
         * Sets the text that should appear on the menu item.
         */
        public SpadeDraw() {
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


            if (spadeCxt.getWorkflowKind() == SpadeContext.WorkflowKind.ANALYSIS) {
                //Create a tab panel for SPADE controls
                CytoPanelImp ctrlPanel = (CytoPanelImp) Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST);                
                analysisPanel = new SpadeAnalysisPanel(spadeCxt);
                ctrlPanel.add("SPADE", analysisPanel);

                //Set the focus on the panel
                Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).setSelectedIndex(
                        Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).getCytoPanelComponentCount() - 1);

                //FIXME this used to make the WEST panel the correct width, now it doesn't
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
            if (analysisPanel != null) {
                analysisPanel.onExit();
            }
        }
    }
}
