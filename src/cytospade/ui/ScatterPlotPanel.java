/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ScatterPlotPanel.java
 *
 * Created on Sep 23, 2011, 11:20:39 AM
 */

package cytospade.ui;

import cytoscape.logger.CyLogger;
import cytospade.FCSOperations;
import cytospade.SpadeContext;
import facs.CanvasSettings;
import facs.Illustration;
import facs.Plot2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

/**
 *
 * @author mlinderm
 */
public class ScatterPlotPanel extends javax.swing.JPanel {

    private FCSOperations fcsOps;
    private int xAxisType, yAxisType;
    private String xAxisParam, yAxisParam;

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

    /** Creates new form ScatterPlotPanel */
    public ScatterPlotPanel(FCSOperations fcsOps) {
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
                    Plot.setIcon(null);
                    CyLogger.getLogger(SpadeAnalysisPanel.class.getName()).error(null, ex);
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
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        xAxisPopup = new javax.swing.JPopupMenu();
        yAxisPopup = new javax.swing.JPopupMenu();
        StyleSelect = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        PlotArea = new javax.swing.JLayeredPane();
        Plot = new javax.swing.JLabel();
        xAxisClickable = new javax.swing.JLabel();
        yAxisClickable = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(360, 400));
        setSize(new java.awt.Dimension(360, 400));

        StyleSelect.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Shaded Contour", "Dot", "Density Dot", "Shadow", "Contour", "Density" }));
        StyleSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StyleSelectActionPerformed(evt);
            }
        });

        jLabel2.setLabelFor(StyleSelect);
        jLabel2.setText("Plot Style");

        Plot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Plot.setBounds(0, 0, 360, 360);
        PlotArea.add(Plot, javax.swing.JLayeredPane.DEFAULT_LAYER);

        xAxisClickable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                xAxisClickableMouseClicked(evt);
            }
        });
        xAxisClickable.setBounds(48, 311, 308, 46);
        PlotArea.add(xAxisClickable, javax.swing.JLayeredPane.DEFAULT_LAYER);

        yAxisClickable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                yAxisClickableMouseClicked(evt);
            }
        });
        yAxisClickable.setBounds(0, 0, 46, 308);
        PlotArea.add(yAxisClickable, javax.swing.JLayeredPane.DEFAULT_LAYER);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(StyleSelect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 260, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(PlotArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 360, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(StyleSelect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(PlotArea, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 360, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void StyleSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StyleSelectActionPerformed
        this.updatePlot();
    }//GEN-LAST:event_StyleSelectActionPerformed

    private void xAxisClickableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xAxisClickableMouseClicked
        xAxisPopup.show(xAxisClickable, 154, 23);
    }//GEN-LAST:event_xAxisClickableMouseClicked

    private void yAxisClickableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yAxisClickableMouseClicked
        yAxisPopup.show(yAxisClickable, 23, 154);
    }//GEN-LAST:event_yAxisClickableMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Plot;
    private javax.swing.JLayeredPane PlotArea;
    private javax.swing.JComboBox StyleSelect;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel xAxisClickable;
    private javax.swing.JPopupMenu xAxisPopup;
    private javax.swing.JLabel yAxisClickable;
    private javax.swing.JPopupMenu yAxisPopup;
    // End of variables declaration//GEN-END:variables

}
