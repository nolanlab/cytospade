package cytospade;
import cytospade.ui.SpadeAnalysisPanel;
import java.awt.event.ActionEvent;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;

/**
 * This class gets attached to the menu item.
 */
public class SpadeDrawAction extends AbstractCyAction {
    private final CySwingAppAdapter adapter;
    private SpadeContext spadeCxt;
    SpadeAnalysisPanel analysisPanel;

    /**
     * Sets the text that should appear on the menu item.
     * @param adapter
     * @param spadeCxt
     */
    public SpadeDrawAction(CySwingAppAdapter adapter, SpadeContext spadeCxt) {
        super("SPADE");
        this.adapter = adapter;
        this.spadeCxt = spadeCxt;
    }

    /**
     * This method is called when the user selects the menu item.
     * @param ae
     */
    public void actionPerformed(ActionEvent ae) {

        // Create the workflow wizard to walk user through setting up processing/analysis
        JFrame frame = this.adapter.getCySwingApplication().getJFrame();
        WorkflowWizard wf = new WorkflowWizard(frame);

        WorkflowWizard.PanelDescriptor intro = new WorkflowWizardPanels.Intro(spadeCxt);
        wf.registerWizardPanel(WorkflowWizardPanels.Intro.IDENTIFIER, intro);

        WorkflowWizard.PanelDescriptor cluster = new WorkflowWizardPanels.ClusterMarkerSelect(spadeCxt);
        wf.registerWizardPanel(WorkflowWizardPanels.ClusterMarkerSelect.IDENTIFIER, cluster);

        WorkflowWizard.PanelDescriptor panels = new WorkflowWizardPanels.PanelCreator(spadeCxt);
        wf.registerWizardPanel(WorkflowWizardPanels.PanelCreator.IDENTIFIER, panels);

        WorkflowWizard.PanelDescriptor summary = new WorkflowWizardPanels.SummaryAndRun(frame, spadeCxt);
        wf.registerWizardPanel(WorkflowWizardPanels.SummaryAndRun.IDENTIFIER, summary);

        wf.setCurrentPanel(WorkflowWizardPanels.Intro.IDENTIFIER);
        int showModalDialog = wf.showModalDialog();


        if (showModalDialog == WorkflowWizard.CANCEL_RETURN_CODE) {
            return;
        } else if (showModalDialog != WorkflowWizard.FINISH_RETURN_CODE) {
            JOptionPane.showMessageDialog(null, "Error occured in workflow wizard.");
        }


        if (spadeCxt.getWorkflowKind() == SpadeContext.WorkflowKind.ANALYSIS) {
            CytoPanel cytoPanelWest = this.adapter.getCySwingApplication().getCytoPanel(CytoPanelName.WEST);
            
            //Create a tab panel for SPADE controls
            analysisPanel = new SpadeAnalysisPanel(spadeCxt);
                    
            adapter.getCyServiceRegistrar().registerService(analysisPanel, CytoPanelComponent.class, new Properties());
            
//            CytoPanel ctrlPanel = this.adapter.getCySwingApplication().getCytoPanel(CytoPanelName.WEST);
//            ctrlPanel.add("SPADE", analysisPanel);

            //Set the focus on the panel
            if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
                cytoPanelWest.setState(CytoPanelState.DOCK);
            }
            
            int index = cytoPanelWest.indexOfComponent(analysisPanel);
            if (index == -1) {
                return;
            }
            cytoPanelWest.setSelectedIndex(index);
            
            //FIXME this used to make the WEST panel the correct width, now it doesn't
            //This setPrefferedSize(getSize + 1), setPrefferedSize(getSize - 1)
            //is seemingly required to prevent violent behavior of the pack
            //method and to force pack to actually relayout the components
//            Cytoscape.getDesktop().setPreferredSize(new Dimension(
//                    Cytoscape.getDesktop().getSize().width + 1,
//                    Cytoscape.getDesktop().getSize().height + 1));
//            Cytoscape.getDesktop().setPreferredSize(new Dimension(
//                    Cytoscape.getDesktop().getSize().width - 1,
//                    Cytoscape.getDesktop().getSize().height - 1));
//            Cytoscape.getDesktop().pack();
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