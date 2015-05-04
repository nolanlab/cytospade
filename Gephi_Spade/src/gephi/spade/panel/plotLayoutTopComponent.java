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
import gephi.spade.panel.*;
import facs.CanvasSettings;
import facs.Plot2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;


/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//gephi.spade.panel//plotLayout//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "plotLayoutTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "layoutmode", openAtStartup = true)
@ActionID(category = "Window", id = "facs.plotLayoutTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_plotLayoutAction",
        preferredID = "plotLayoutTopComponent"
)
@Messages({
    "CTL_plotLayoutAction=plotLayout",
    "CTL_plotLayoutTopComponent=plotLayout Window",
    "HINT_plotLayoutTopComponent=This is a plotLayout window"
})
public final class plotLayoutTopComponent extends TopComponent {
    
    private static FCSOperations fcsOps;
    private int xAxisType, yAxisType;
    private String xAxisParam, yAxisParam;
    private SpadeContext spadeCxt;
    private static File fcsOperations;
    private ReentrantLock panelLock;
    private Object fcsfile;

    FCSOperations getFCS() {
        return this.fcsOps; //To change body of generated methods, choose Tools | Templates.
    }
    
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

   public void setFCS(Object fcsfile){
       
       this.fcsfile = fcsfile;
       this.fcsOps = (FCSOperations) this.fcsfile;
       this.xAxisType = yAxisType   = this.getAxisType("Log");
       this.xAxisParam = yAxisParam = fcsOps.getChannelShortName(0);
       
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
    
    public plotLayoutTopComponent() {
        //this.fcsOperations = spadeCxt.getFCS();
        //xAxisType = yAxisType   = this.getAxisType("Log");
        //xAxisParam = yAxisParam = fcsOps.getChannelShortName(0);
        
        initComponents();
        setName(Bundle.CTL_plotLayoutTopComponent());
        setToolTipText(Bundle.HINT_plotLayoutTopComponent());
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);

    }
    
public void updatePlot() {
        
         
                int plot_type = getPlotType((String)StyleSelect.getSelectedItem());
                int event_count = 0;
                //int event_count = fcsOps.getSelectedNodesCount() == 0 ? fcsOps.getEventCount() : fcsOps.getSelectedEventCount();\
                if (fcsOps.getSelectedNodesCount() == 0){
                    event_count = fcsOps.getEventCount();
                }else{
                    event_count = fcsOps.getSelectedEventCount();
                }
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
                    //this.setFCS(fcsfile);
                    Plot.setIcon(new ImageIcon(image));
                    
                } catch (IOException ex) {
                    Plot.setIcon(null);
                    
                    //CyLogger.getLogger(SpadeAnalysisPanel.class.getName()).error(null, ex);
                }
                
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

        xAxisPopup = new javax.swing.JPopupMenu();
        yAxisPopup = new javax.swing.JPopupMenu();
        PlotArea = new javax.swing.JLayeredPane();
        Plot = new javax.swing.JLabel();
        xAxisClickable = new javax.swing.JLabel();
        yAxisClickable = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        StyleSelect = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        PlotArea.add(Plot, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout PlotAreaLayout = new javax.swing.GroupLayout(PlotArea);
        PlotArea.setLayout(PlotAreaLayout);
        PlotAreaLayout.setHorizontalGroup(
            PlotAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 226, Short.MAX_VALUE)
        );
        PlotAreaLayout.setVerticalGroup(
            PlotAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(Plot, org.openide.util.NbBundle.getMessage(plotLayoutTopComponent.class, "plotLayoutTopComponent.Plot.text")); // NOI18N
        Plot.setToolTipText(org.openide.util.NbBundle.getMessage(plotLayoutTopComponent.class, "plotLayoutTopComponent.Plot.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(xAxisClickable, org.openide.util.NbBundle.getMessage(plotLayoutTopComponent.class, "plotLayoutTopComponent.xAxisClickable.text")); // NOI18N
        xAxisClickable.setLabelFor(StyleSelect);
        xAxisClickable.setText("Plot Style");
        xAxisClickable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                xAxisClickableMouseClicked(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(yAxisClickable, org.openide.util.NbBundle.getMessage(plotLayoutTopComponent.class, "plotLayoutTopComponent.yAxisClickable.text")); // NOI18N
        yAxisClickable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                yAxisClickableMouseClicked(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(plotLayoutTopComponent.class, "plotLayoutTopComponent.jLabel4.text")); // NOI18N

        StyleSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Shaded Contour", "Dot", "Density Dot", "Shadow", "Contour", "Density" }));
        StyleSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StyleSelectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(plotLayoutTopComponent.class, "plotLayoutTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(plotLayoutTopComponent.class, "plotLayoutTopComponent.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(xAxisClickable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 108, Short.MAX_VALUE)
                        .addComponent(PlotArea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(yAxisClickable)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(50, 50, 50)
                                    .addComponent(Plot))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGap(35, 35, 35)
                                    .addComponent(jLabel4)))
                            .addComponent(jLabel2)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(StyleSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(StyleSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Plot)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(yAxisClickable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(43, 43, 43)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PlotArea)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(xAxisClickable)
                        .addGap(3, 3, 3)
                        .addComponent(jLabel4)
                        .addGap(0, 103, Short.MAX_VALUE)))
                .addContainerGap())
        );

        PlotArea.add(xAxisClickable, javax.swing.JLayeredPane.DEFAULT_LAYER);
        PlotArea.add(yAxisClickable, javax.swing.JLayeredPane.DEFAULT_LAYER);
    }// </editor-fold>//GEN-END:initComponents

    private void StyleSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StyleSelectActionPerformed
        // TODO add your handling code here:
         this.updatePlot();
    }//GEN-LAST:event_StyleSelectActionPerformed

    private void xAxisClickableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xAxisClickableMouseClicked
        // TODO add your handling code here:
        xAxisPopup.show(xAxisClickable, 154, 23);
    }//GEN-LAST:event_xAxisClickableMouseClicked

    private void yAxisClickableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yAxisClickableMouseClicked
        // TODO add your handling code here:
        yAxisPopup.show(yAxisClickable, 23, 154);
    }//GEN-LAST:event_yAxisClickableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Plot;
    private javax.swing.JLayeredPane PlotArea;
    private javax.swing.JComboBox StyleSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel xAxisClickable;
    private javax.swing.JPopupMenu xAxisPopup;
    private javax.swing.JLabel yAxisClickable;
    private javax.swing.JPopupMenu yAxisPopup;
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
