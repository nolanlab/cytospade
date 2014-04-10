package cytospade;

import org.cytoscape.app.swing.AbstractCySwingApp;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.swing.CySwingApplication;

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
        CySwingApplication cytoscapeDesktopService = adapter.getCySwingApplication();
        
        // Initialized internal state keeping
        spadeCxt = new SpadeContext();
        this.adapter = adapter;
        
        // Create menu bar item, along with associated action...
        spadeDrawAction = new SpadeDrawAction(adapter, spadeCxt);
        
        cytoscapeDesktopService.addAction(spadeDrawAction);
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