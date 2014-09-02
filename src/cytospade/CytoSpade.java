package cytospade;

import java.io.File;
import org.cytoscape.app.swing.AbstractCySwingApp;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.view.model.CyNetworkView;

/**
 * Cytoscape plugin that draws scatter plots for SPADE trees
 */
public class CytoSpade extends AbstractCySwingApp {

    private final CySwingAppAdapter adapter;
    private SpadeContext spadeCxt;
    private SpadeDrawAction spadeDrawAction;

    /**
     * Creates an action and adds it to the Plugins menu.
     * @param adapter
     */
    public CytoSpade(CySwingAppAdapter adapter) {
        super(adapter);
        //CySwingApplication cytoscapeDesktopService = adapter.getCySwingApplication();
                        
        // Initialized internal state keeping
        spadeCxt = new SpadeContext(adapter);
        this.adapter = adapter;
        // Create menu bar item, along with associated action...
        spadeDrawAction = new SpadeDrawAction(adapter, spadeCxt);
        adapter.getCySwingApplication().addAction(spadeDrawAction);
        //cytoscapeDesktopService.addAction(spadeDrawAction);
    }

    /**
     * Event to save the user-defined network landscaping when Cytoscape exits.
     */
    public void onCytoscapeExit() {
        if (spadeDrawAction != null) {
            spadeDrawAction.onExit();
        }
    }
}