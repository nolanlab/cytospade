/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Scatter.java
 *
 * Created on Sep 17, 2011, 5:08:13 PM
 */
package cytospade;

import cytospade.FCSComputations.Result;
import facs.CanvasSettings;
import facs.Plot2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;

/**
 *
 * @author Ketaki
 */
public class Scatter extends javax.swing.JPanel {

    private String xChanScale;
    private String yChanScale;
    private String xChanParam;
    private String yChanParam;
    private fcsFile inputFcsFile;

    /** Creates new form Scatter */
    public Scatter() {
        initComponents();
        plotStyleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Shaded Contour", "Dot", "Density Dot", "Shadow", "Contour", "Density"}));
        plotStyleComboBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotStyleComboActionPerformed(evt);
            }
        });  
        xTransformComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {}));        
        yTransformComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {}));
        xAttrComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {}));        
        yAttrComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {}));
    }

    private void plotStyleComboActionPerformed(java.awt.event.ActionEvent evt) {
        (new drawScatterThread()).execute();
    }

    public void fcsFileChanged(fcsFile file) {
        inputFcsFile = file;
        //TO DO: Draw Scatter Plot by calling DrawScatterThread()
        // Update plot combo boxes with channels in new FCS files       
        String[] scales = new String[]{"Linear", "Log", "Arcsinh: CyTOF", "Arcsinh: Fluor"};
                
        xTransformComboBox.setModel(new javax.swing.DefaultComboBoxModel(scales));        
        xTransformComboBox.addActionListener(new XactionPerformed());      
        yTransformComboBox.setModel(new javax.swing.DefaultComboBoxModel(scales));
        yTransformComboBox.addActionListener(new YactionPerformed());

        // Build alphabetized channel selector
        String[] names = new String[inputFcsFile.getChannelCount()];
        for (int i = 0; i < inputFcsFile.getChannelCount(); i++) {
            names[i] = SpadeContext.getFCSChannelFormattedName(inputFcsFile, i);
        }
        Arrays.sort(names);     
        
        xAttrComboBox.setModel(new javax.swing.DefaultComboBoxModel(names));
        xAttrComboBox.addActionListener(new XactionPerformed());        
        yAttrComboBox.setModel(new javax.swing.DefaultComboBoxModel(names));
        yAttrComboBox.addActionListener(new YactionPerformed());

        // Initialize plot axes parameters
        xChanScale = xTransformComboBox.getSelectedItem().toString();
        yChanScale = yTransformComboBox.getSelectedItem().toString();
        if (inputFcsFile.getChannelCount() > 0) {
            xChanParam = inputFcsFile.getChannelShortName(0);
            yChanParam = inputFcsFile.getChannelShortName(0);
        }

        //Draw a plot
        (new drawScatterThread()).execute();
    }

    public void nodeSelectionChanged() {
        (new drawScatterThread()).execute();
    }

    public class drawScatterThread extends SwingWorker<Integer, Void> {

        private int eventCount;
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
                //Fear not, compute() makes them not-null if nodes are selected.
                dataAx = null;
                dataAy = null;

                //Open the FCS file         
                if (inputFcsFile == null){
                    return 0;
                }
                FCSComputations fcsDataCompute = new FCSComputations(inputFcsFile);

                Result result = fcsDataCompute.compute(xChanParam, yChanParam);
                if (result.numNodesSelected == 0) {
                    DecimalFormat df = new DecimalFormat();
                    countLbl.setText("Displaying " + df.format(result.numNodesSelected) + " of " + df.format(result.eventCount) + " events");
                    tValLbl.setText("Select some nodes to calculate P-values");
                } else {
                    DecimalFormat df = new DecimalFormat();
                    countLbl.setText("Displaying " + df.format(result.numNodesSelected) + " of " + df.format(result.eventCount) + " events");
                    StringBuilder sb = new StringBuilder(500);
                    for (int i = 0; i < ((result.pValues.size() < 5) ? result.pValues.size() : 5); i++) {
                        sb.append("P-Value for ").append(result.pValues.get(i).name).append(": ").append(result.pValues.get(i).value).append("\n");
                    }
                    tValLbl.setText(sb.toString());
                }

                //Unpack result into local variables
                eventCount = result.eventCount;
                dataAx = result.dataAx;
                dataAy = result.dataAy;
                datax = result.datax;
                datay = result.datay;
                xChanMax = result.xChanMax;
                yChanMax = result.yChanMax;

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
                if (eventCount > 5000) {
                    dotSize = 1;
                } else if (eventCount > 1000) {
                    dotSize = 2;
                } else if (eventCount > 10) {
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
                        facs.Illustration.DEFAULT_POPULATION_TYPE, eventCount, dotSize);
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
    }

    public class XactionPerformed implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String selectedValue = ((JComboBox) e.getSource()).getSelectedItem().toString();
            if (selectedValue.matches("Linear")
                    || selectedValue.matches("Log")
                    || selectedValue.matches("Arcsinh: CyTOF")
                    || selectedValue.matches("Arcsinh: Fluor")) {
                xChanScale = selectedValue;
            } else {
                xChanParam = SpadeContext.getShortNameFromFormattedName(selectedValue);
            }
            (new drawScatterThread()).execute();
        }
    }

    public class YactionPerformed implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String selectedValue = ((JComboBox) e.getSource()).getSelectedItem().toString();
            if (selectedValue.matches("Linear")
                    || selectedValue.matches("Log")
                    || selectedValue.matches("Arcsinh: CyTOF")
                    || selectedValue.matches("Arcsinh: Fluor")) {
                yChanScale = selectedValue;
            } else {
                yChanParam = SpadeContext.getShortNameFromFormattedName(selectedValue);
            }
            (new drawScatterThread()).execute();
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

        plotStyleComboBox = new javax.swing.JComboBox();
        xAxisLbl = new javax.swing.JLabel();
        xAttrLbl = new javax.swing.JLabel();
        xTransLbl = new javax.swing.JLabel();
        xAttrComboBox = new javax.swing.JComboBox();
        xTransformComboBox = new javax.swing.JComboBox();
        yAttrComboBox = new javax.swing.JComboBox();
        yAxisLbl = new javax.swing.JLabel();
        yTransformComboBox = new javax.swing.JComboBox();
        plotArea = new javax.swing.JLayeredPane();
        jLabelPlot = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        tValLbl = new javax.swing.JLabel();
        countLbl = new javax.swing.JLabel();

        plotStyleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        plotStyleComboBox.setMinimumSize(new java.awt.Dimension(0, 18));
        plotStyleComboBox.setPreferredSize(new java.awt.Dimension(165, 20));

        xAxisLbl.setText("X-Axis");

        xAttrLbl.setText("Attribute");

        xTransLbl.setText("Transform");

        xAttrComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        xTransformComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        yAttrComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        yAxisLbl.setText("Y-Axis");

        yTransformComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        plotArea.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        plotArea.setMinimumSize(new java.awt.Dimension(358, 358));

        jLabelPlot.setText("jLabel1");
        jLabelPlot.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabelPlot.setBounds(0, 0, 140, 140);
        plotArea.add(jLabelPlot, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jLabel7.setText("Style");

        tValLbl.setText("t-values of selected nodes");

        countLbl.setText("0 events selected");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(plotStyleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(tValLbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(countLbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(plotArea, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(xAxisLbl)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(xTransLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                                        .addGap(11, 11, 11))
                                    .addComponent(xTransformComboBox, 0, 68, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(yAxisLbl)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(yTransformComboBox, 0, 70, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(yAttrComboBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(xAttrLbl)
                                .addComponent(xAttrComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(45, 45, 45)))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(plotStyleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xTransLbl)
                    .addComponent(xAttrLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xAxisLbl)
                    .addComponent(xTransformComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xAttrComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yAxisLbl)
                    .addComponent(yTransformComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yAttrComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(plotArea, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(countLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tValLbl)
                .addContainerGap(17, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel countLbl;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelPlot;
    private javax.swing.JLayeredPane plotArea;
    private javax.swing.JComboBox plotStyleComboBox;
    private javax.swing.JLabel tValLbl;
    private javax.swing.JComboBox xAttrComboBox;
    private javax.swing.JLabel xAttrLbl;
    private javax.swing.JLabel xAxisLbl;
    private javax.swing.JLabel xTransLbl;
    private javax.swing.JComboBox xTransformComboBox;
    private javax.swing.JComboBox yAttrComboBox;
    private javax.swing.JLabel yAxisLbl;
    private javax.swing.JComboBox yTransformComboBox;
    // End of variables declaration//GEN-END:variables
}
