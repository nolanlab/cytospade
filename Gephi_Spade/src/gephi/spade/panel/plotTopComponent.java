/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gephi.spade.panel;

import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//gephi.spade.panel//plot//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "plotTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "layoutmode", openAtStartup = true)
@ActionID(category = "Window", id = "gephi.spade.panel.plotTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_plotAction",
        preferredID = "plotTopComponent"
)
@Messages({
    "CTL_plotAction=plot",
    "CTL_plotTopComponent=plot Window",
    "HINT_plotTopComponent=This is a plot window"
})
public final class plotTopComponent extends TopComponent {
    private FCSOperations fcsOps;
    private int xAxisType, yAxisType;
    private String xAxisParam, yAxisParam;
    
    public plotTopComponent() {
        initComponents();
        setName(Bundle.CTL_plotTopComponent());
        setToolTipText(Bundle.HINT_plotTopComponent());

    }
    /*
private class xParamChanged implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            xAxisParam = SpadeContext.getShortNameFromFormattedName(e.getActionCommand());
            updatePlot();
        }
    }

    private class yParamChanged implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            yAxisParam = SpadeContext.getShortNameFromFormattedName(e.getActionCommand());
            updatePlot();
        }
    }

   
    private class xScaleChanged implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            xAxisType = getAxisType(e.getActionCommand());
            updatePlot();
        }
    }

    private class yScaleChanged implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            yAxisType = getAxisType(e.getActionCommand());
            updatePlot();
        }
    }

    //Creates new form ScatterPlotPanel 
    public ScatterPlotPanel(FCSOperations fcsOps) {
        JOptionPane.showMessageDialog(null, "scatter panel");
        this.fcsOps = fcsOps;

        initComponents();

        xAxisType = yAxisType   = this.getAxisType("Log");
        xAxisParam = yAxisParam = fcsOps.getChannelShortName(0);

        for (String scales : new String[]{"Linear", "Log", "Arcsinh: CyToF", "Arcsinh: Fluor"}) {
            JMenuItem menuItem;
            menuItem = new JMenuItem(scales);
            menuItem.addActionListener(new xScaleChanged());
            xAxisPopup.add(menuItem);
            menuItem = new JMenuItem(scales);
            menuItem.addActionListener(new yScaleChanged());
            yAxisPopup.add(menuItem);
        }

        xAxisPopup.addSeparator();
        yAxisPopup.addSeparator();

        {  // Build alphabetized channel selector
            String[] names = new String[fcsOps.getChannelCount()];
            for (int i = 0; i < fcsOps.getChannelCount(); i++) {
                names[i] = SpadeContext.getFCSChannelFormattedName(fcsOps.getFCSFile(), i);
            }
            Arrays.sort(names);
            for (int i = 0; i < fcsOps.getChannelCount(); i++) {
                JMenuItem menuItem;
                menuItem = new JMenuItem(names[i]);
                menuItem.addActionListener(new xParamChanged());
                xAxisPopup.add(menuItem);
                menuItem = new JMenuItem(names[i]);
                menuItem.addActionListener(new yParamChanged());
                yAxisPopup.add(menuItem);
            }
        }


    }

    public void updatePlot() {
         (new SwingWorker<Integer,Void>() {
            
            @Override
            protected Integer doInBackground() throws Exception {
                JOptionPane.showMessageDialog(null, "update plot");
                int plot_type = getPlotType((String)StyleSelect.getSelectedItem());
    
                int event_count = fcsOps.getSelectedNodesCount() == 0 ? fcsOps.getEventCount() : fcsOps.getSelectedEventCount();
                int dot_size = 1;
                if (event_count > 5000) {
                    dot_size = 1;
                } else if (event_count > 1000) {
                    dot_size = 2;
                } else if (event_count > 10) {
                    dot_size = 3;
                } else {
                    // Very small numbers of events make contours meaningless
                    // so we automatically switch to dot plots in this scenario
                    plot_type = facs.Illustration.DOT_PLOT;
                    dot_size = 3;
                }

                CanvasSettings cs = CanvasSettings.getCanvasSettings(
                        1, // Horizontal spacing between plots
                        1, // Vertical spacing between plots
                        0, 1, 2,
                        plot_type, facs.Illustration.DEFAULT_COLOR_SET,
                        false, // Black background
                        true,  // Draw annotations
                        true,  // Draw scale labels
                        true,  // Draw scale ticks
                        true,  // Draw axis labels
                        false, // Use long labels
                        300, 1.0d, 1.0d,
                        10.0d, // Note this choices interact with small event check above
                        10.0d,
                        facs.Illustration.DEFAULT_POPULATION_TYPE, event_count, dot_size);
                BufferedImage image;
                try {
                    JOptionPane.showMessageDialog(null, "no exception");
                    image = facs.Plot2D.drawPlot(
                            cs,
                            fcsOps.getSelectedNodesCount() == 0 ? fcsOps.getEvents(xAxisParam) : fcsOps.getSelectedEvents(xAxisParam),
                            fcsOps.getSelectedNodesCount() == 0 ? fcsOps.getEvents(yAxisParam) : fcsOps.getSelectedEvents(yAxisParam),
                            fcsOps.getSelectedNodesCount() == 0 ? null : fcsOps.getEvents(xAxisParam),
                            fcsOps.getSelectedNodesCount() == 0 ? null : fcsOps.getEvents(yAxisParam),
                            xAxisParam,
                            yAxisParam,
                            fcsOps.getEventMax(xAxisParam),
                            fcsOps.getEventMax(yAxisParam),
                            xAxisType,
                            yAxisType
                            );
                    Plot.setIcon(new ImageIcon(image));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "exception");
                    Plot.setIcon(null);
                    //CyLogger.getLogger(SpadeAnalysisPanel.class.getName()).error(null, ex);
                }
                return 0;
            }
             
         }).execute();


        

    }

    private int getPlotType(String type) {
        if (type.matches("Shaded Contour")) {
            return facs.Illustration.SHADED_CONTOUR_PLOT;
        } else if (type.matches("Dot")) {
            return facs.Illustration.DOT_PLOT;
        } else if (type.matches("Density Dot")) {
            return facs.Illustration.DENSITY_DOT_PLOT;
        } else if (type.matches("Shadow")) {
            return facs.Illustration.SHADOW_PLOT;
        } else if (type.matches("Contour")) {
            return facs.Illustration.CONTOUR_PLOT;
        } else if (type.matches("Density")) {
            return facs.Illustration.DENSITY_PLOT;
        } else {
            return facs.Illustration.DOT_PLOT;
        }
    }

    private int getAxisType(String axis) {
        if (axis.matches("Linear")) {
            return Plot2D.LINEAR_DISPLAY;
        } else if (axis.matches("Log")) {
            return Plot2D.LOG_DISPLAY;
        } else if (axis.matches("Arcsinh: CyToF")) {
            return Plot2D.ARCSINH_DISPLAY_CYTOF;
        } else if (axis.matches("Arcsinh: Fluor")) {
            return Plot2D.ARCSINH_DISPLAY_FLUOR;
        } else {
            return Plot2D.LOG_DISPLAY;
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
        
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
