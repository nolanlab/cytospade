package cytospade;
import cytoscape.logger.CyLogger;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mlinderm
 */
public class WorkflowWizardPanels {

    
    public static class Intro extends WorkflowWizard.PanelDescriptor {

        public static final String IDENTIFIER = "INTRODUCTION_PANEL";

        public Intro(SpadeContext cxt) {
            super(IDENTIFIER);
            
            this.cxt = cxt;
            this.nextID = null;

            setPanelComponent(createPanel());
        }

        @Override
        public Object getNextPanelDescriptor() {
            return nextID;
        }

        @Override
        public void aboutToDisplayPanel() {
            this.getWizard().setTitle("Select Working Directory");
            this.getWizard().setNextButtonEnabled(false);
        }

        @Override
        public void displayingPanel() {
            this.getWizard().setNextButtonEnabled((cxt.getPath() != null) && (cxt.getWorkflowKind() != null));
        }

        private JPanel createPanel() {
            contentPanel = new JPanel();
// <editor-fold defaultstate="collapsed" desc="Generated Code">
            jScrollPane1 = new javax.swing.JScrollPane();
            jTextArea1 = new javax.swing.JTextArea();
            jLabel1 = new javax.swing.JLabel();
            jTextField1 = new javax.swing.JTextField();
            jLabel2 = new javax.swing.JLabel();
            jButton1 = new javax.swing.JButton();

            jTextArea1.setColumns(20);
            jTextArea1.setLineWrap(true);
            jTextArea1.setWrapStyleWord(true);
            jTextArea1.setEditable(false);
            jTextArea1.setRows(5);
            jTextArea1.setText(
                    "The SPADE Cytoscape plugin has two modes:\n"
                    + "1) Processing FCS files to create SPADE trees\n"
                    + "2) Analysis of SPADE trees\n"
                    + "To setup up processing, select the directory containing the FCS files of interest. "
                    + "To begin analysis, select previously generated SPADE output directory."
                    );
            jScrollPane1.setViewportView(jTextArea1);

            jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel1.setText("Welcome to SPADE");

            jTextField1.setText("");
            jTextField1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jTextField1ActionPerformed(evt);
                }
            });

            jLabel2.setText("Directory");

            jButton1.setText("Browse");
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });

            org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(contentPanel);
            contentPanel.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 445, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1)
                    .add(layout.createSequentialGroup()
                        .addContainerGap(30, Short.MAX_VALUE)
                        .add(jLabel2)
                        .add(16, 16, 16)
                        .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 272, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(16, 16, 16)
                        .add(jButton1)
                        .addContainerGap(30, Short.MAX_VALUE)
                        )
                    
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(30, 30, 30)
                    .add(jLabel1)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 146, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(18, 18, 18)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                        .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jButton1)
                        .add(jLabel2))
                    .addContainerGap(30, Short.MAX_VALUE))
            );
            // </editor-fold>
            return contentPanel;
        }

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = jFileChooser.showOpenDialog(jFileChooser);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try {
                    cxt.setPath(jFileChooser.getSelectedFile());
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid directory: " + ex.getMessage());
                    return;
                }
                
                // Fill in Directory Text Field
                jTextField1.setText(cxt.getPath().getPath());

                //If the selected directory is a processed output directory,
                //open the analysis pane immediately
                SpadeContext.WorkflowKind wk = cxt.getWorkflowKind();
                if (wk == SpadeContext.WorkflowKind.ANALYSIS) {
                    getWizard().close(WorkflowWizard.FINISH_RETURN_CODE);
                }

                this.updateNextPanel();
            } else if (returnValue == JFileChooser.CANCEL_OPTION) {
                return;
            } else {
                JOptionPane.showMessageDialog(null, "File selection error");
                return;
            }
        }

        private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {
            try {
                cxt.setPath(new File(jTextField1.getText()));
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, "Invalid directory: " + ex.getMessage());
            }

            this.updateNextPanel();
        }

        private void updateNextPanel() {
            // Set "next" in workflow
            SpadeContext.WorkflowKind wk = cxt.getWorkflowKind();
            if (wk == SpadeContext.WorkflowKind.ANALYSIS) {
                nextID = Intro.FINISH;
                getWizard().setNextButtonEnabled(true);
            } else if (wk == SpadeContext.WorkflowKind.PROCESSING) {
                nextID = ClusterMarkerSelect.IDENTIFIER;
                getWizard().setNextButtonEnabled(true);
            } else {
                // Should not get here
                throw new IllegalArgumentException();
            }
        }

        private SpadeContext cxt;
        private Object nextID;

        private javax.swing.JPanel contentPanel;

        // Variables declaration - do not modify
        private javax.swing.JButton jButton1;
        private javax.swing.JTextField jTextField1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JTextArea jTextArea1;
        // End of variables declaration
    }

    public static class ClusterMarkerSelect extends WorkflowWizard.PanelDescriptor {

        public static final String IDENTIFIER = "CLUSTER_MARKER_SELECT_PANEL";

        public ClusterMarkerSelect(SpadeContext cxt) {
            super(IDENTIFIER);

            this.cxt = cxt;

            setPanelComponent(createPanel());
        }

        @Override
        public Object getBackPanelDescriptor() {
            return Intro.IDENTIFIER;
        }

        @Override
        public Object getNextPanelDescriptor() {
            return PanelCreator.IDENTIFIER;
        }

        @Override
        public void aboutToDisplayPanel() {
            this.getWizard().setTitle("Set Clustering Parameters");       
            jList1.setModel(new javax.swing.AbstractListModel() {
                String[] strings = cxt.getPotentialClusteringMarkers();
                public int getSize() { return strings.length; }
                public Object getElementAt(int i) { return strings[i]; }
            });

            {
                String[] markers = cxt.getSelectedClusteringMarkers();
                List<String> possible_markers = Arrays.asList(cxt.getPotentialClusteringMarkers());
                int[] idxs = new int[markers.length];
                for (int i=0; i<markers.length; i++) {
                    idxs[i] = possible_markers.indexOf(markers[i]);
                }
                jList1.setSelectedIndices(idxs);
            }
        }

        @Override
        public void displayingPanel() {
            getWizard().setNextButtonEnabled((jList1.getSelectedIndex() != -1));
        }

        @Override
        public void aboutToHidePanel() {
            // Collect values from input and update context
            {  // Required to downcast .... 
                Object[] selected = jList1.getSelectedValues();
                String[] markers  = new String[selected.length];
                for (int i=0; i<selected.length; i++)
                    markers[i] = selected[i].toString();
                cxt.setSelectedClusteringMarkers(markers);
            }
            cxt.setArcsinh((Integer)jSpinner1.getValue());
            cxt.setTargetClusters((Integer)jSpinner2.getValue());
            if (jRadioButton1.isSelected())
                cxt.setDownsampleKind(SpadeContext.DownsampleKind.EVENTS);
            else if (jRadioButton2.isSelected())
                cxt.setDownsampleKind(SpadeContext.DownsampleKind.PERCENTILE);
            else if (jRadioButton3.isSelected())
                cxt.setDownsampleKind(SpadeContext.DownsampleKind.PERCENT);
            else  // Set percent as the default
                cxt.setDownsampleKind(SpadeContext.DownsampleKind.PERCENT);

            cxt.setTargetDownsampleEvents((Integer)jSpinner3.getValue());
            cxt.setTargetDownsamplePctile((Integer)jSpinner4.getValue());
            cxt.setTargetDownsamplePercent((Double)jSpinner5.getValue());
        }

        private JPanel createPanel() {
            contentPanel = new JPanel();
            // No idea where the .form file is for this ...
            
            jScrollPane1 = new javax.swing.JScrollPane();
            jList1 = new javax.swing.JList();
            jLabel1 = new javax.swing.JLabel();
            jLabel2 = new javax.swing.JLabel();
            jSpinner1 = new javax.swing.JSpinner();
            jLabel3 = new javax.swing.JLabel();
            jSpinner2 = new javax.swing.JSpinner();
            
            jLabel4 = new javax.swing.JLabel();
            jSpinner3 = new javax.swing.JSpinner();
            jSpinner4 = new javax.swing.JSpinner();
            jSpinner5 = new javax.swing.JSpinner();

            buttonGroup1 = new javax.swing.ButtonGroup();
            jRadioButton1 = new javax.swing.JRadioButton();
            jRadioButton2 = new javax.swing.JRadioButton();
            jRadioButton3 = new javax.swing.JRadioButton();

            jList1.setModel(new javax.swing.AbstractListModel() {
                String[] strings = {};
                public int getSize() { return strings.length; }
                public Object getElementAt(int i) { return strings[i]; }
            });
            jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                    jList1ValueChanged(evt);
                }
            });
            jList1.setToolTipText("Select markers are used to calculate 'distance' between cells from those common to all files");
            jScrollPane1.setViewportView(jList1);

            jLabel1.setText("Select Clustering Markers");

            jLabel2.setText("Arcsinh Cofactor (CyToF=5, Optical=150)");

            jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(cxt.getArcsinh()), Integer.valueOf(0), null, Integer.valueOf(1)));
            jSpinner1.setToolTipText("Set cofactor used in Arcsinh transformation: arcsinh(data/<COFACTOR>)");

            jLabel3.setText("Target Number of Clusters");

            jSpinner2.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(cxt.getTargetClusters()), Integer.valueOf(0), null, Integer.valueOf(1)));
            jSpinner2.setToolTipText("SPADE will automatically stop clustering when number of clusters is within 50% of this target");

            jLabel4.setText("Downsample to Percentile, Number or Percent of Events");
            jRadioButton1.setText("Events");
            buttonGroup1.add(jRadioButton1);
            jRadioButton2.setText("Percentile");
            buttonGroup1.add(jRadioButton2);
            jRadioButton3.setText("Percent");
            buttonGroup1.add(jRadioButton3);
            jRadioButton3.setSelected(true);

            jSpinner3.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(cxt.getTargetDownsampleEvents()), Integer.valueOf(0), null, Integer.valueOf(100)));
            jSpinner3.setToolTipText("Each input file will be downsampled to retain the smaller of this number of events, or total events in file");
            jSpinner4.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(cxt.getTargetDownsamplePctile()), Integer.valueOf(0), Integer.valueOf(100), Integer.valueOf(1)));
            jSpinner4.setToolTipText("Each input file will be downsampled to retain events with density below this percentile");
            jSpinner5.setModel(new javax.swing.SpinnerNumberModel(cxt.getTargetDownsamplePercent(), 0.0d, 100.0d, 1.0d));
            jSpinner5.setToolTipText("Each input file will be downsampled to retain this percent of events");
            

            org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(contentPanel);
            contentPanel.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                .add(layout.createSequentialGroup()
                    .addContainerGap(36, Short.MAX_VALUE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 225, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel1))
                    .add(40, 40, 40)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jLabel2)
                        .add(jSpinner1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel3)
                        .add(jSpinner2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel4)
                        .add(layout.createSequentialGroup()
                            .add(jRadioButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jSpinner3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(layout.createSequentialGroup()
                            .add(jRadioButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jSpinner4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(layout.createSequentialGroup()
                            .add(jRadioButton3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jSpinner5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    )
                    .addContainerGap(36, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(26, 26, 26)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel1)
                        .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE))
                        .add(layout.createSequentialGroup()
                            .add(6, 6, 6)
                            .add(jSpinner1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(18, 18, 18)
                            .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jSpinner2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(18, 18, 18)
                            .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jRadioButton1)
                                .add(jSpinner3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jRadioButton2)
                                .add(jSpinner4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jRadioButton3)
                                .add(jSpinner5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    ))
                    .addContainerGap())
            );

            return contentPanel;
        }

        private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {
            getWizard().setNextButtonEnabled((jList1.getSelectedIndex() != -1));
        }

        private SpadeContext cxt;
        
        private javax.swing.JPanel contentPanel;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JList jList1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JSpinner jSpinner1;
        private javax.swing.JSpinner jSpinner2;
        private javax.swing.JSpinner jSpinner3;
        private javax.swing.JSpinner jSpinner4;
        private javax.swing.JSpinner jSpinner5;
        private javax.swing.ButtonGroup buttonGroup1;
        private javax.swing.JRadioButton jRadioButton1;
        private javax.swing.JRadioButton jRadioButton2;
        private javax.swing.JRadioButton jRadioButton3;
    }

    public static class PanelCreator extends WorkflowWizard.PanelDescriptor {

        public static final String IDENTIFIER = "PANEL_CREATOR_PANEL";

        public PanelCreator(SpadeContext cxt) {
            super(IDENTIFIER);

            this.cxt = cxt;

            setPanelComponent(createPanel());
        }

        @Override
        public Object getBackPanelDescriptor() {
            return ClusterMarkerSelect.IDENTIFIER;
        }

        @Override
        public Object getNextPanelDescriptor() {
            return SummaryAndRun.IDENTIFIER;
        }

        @Override
        public void aboutToDisplayPanel() {
            this.getWizard().setTitle("Create Median and Fold Change Calculation Panels");

            // Fill in previously defined panels
            jList6.setListData(cxt.getAnalysisPanelsNames());

            ListCellRenderer fileRenderer = new javax.swing.ListCellRenderer() {
                public Component getListCellRendererComponent(JList jlist, Object o, int idx, boolean isSelected, boolean bln1) {
                    JLabel label = new JLabel(((File)o).getName());
                    label.setBackground(isSelected ? jlist.getSelectionBackground(): jlist.getBackground());
                    label.setForeground(isSelected ? jlist.getSelectionForeground(): jlist.getForeground());
                    label.setEnabled(jlist.isEnabled());
                    label.setFont(jlist.getFont());
                    label.setOpaque(true);
                    return label;
                }
            };

            // Initialize the panel file list as files not already in panels
            jList3.setModel(new javax.swing.AbstractListModel() {
                File[] files = cxt.getFCSFilesNotInPanel();
                public int getSize() { return files.length; }
                public Object getElementAt(int i) { return files[i]; }
            });

            jList3.setCellRenderer(fileRenderer);
            jList2.setCellRenderer(fileRenderer);
        }

        @Override
        public void nextButtonPressed() {
           // Create "catch-all" panel with any files not currently in a panel
           cxt.addAnalysisPanel("catchall", new SpadeContext.AnalysisPanel(cxt.getFCSFilesNotInPanel()));
        }

        private JPanel createPanel() {
            contentPanel = new JPanel();
            // <editor-fold defaultstate="collapsed" desc="Generated Code">
            jScrollPane5 = new javax.swing.JScrollPane();
            jTextArea1 = new javax.swing.JTextArea();
            jScrollPane6 = new javax.swing.JScrollPane();
            jList5 = new javax.swing.JList();
            jScrollPane2 = new javax.swing.JScrollPane();
            jList2 = new javax.swing.JList();
            jLabel2 = new javax.swing.JLabel();
            jLabel3 = new javax.swing.JLabel();
            jLabel4 = new javax.swing.JLabel();
            jScrollPane3 = new javax.swing.JScrollPane();
            jList3 = new javax.swing.JList();
            jScrollPane4 = new javax.swing.JScrollPane();
            jList4 = new javax.swing.JList();
            jScrollPane7 = new javax.swing.JScrollPane();
            jList6 = new javax.swing.JList();
            jLabel5 = new javax.swing.JLabel();
            jTextField1 = new javax.swing.JTextField();
            jButton1 = new javax.swing.JButton();
            jLabel1 = new javax.swing.JLabel();
            jButton2 = new javax.swing.JButton();

            jTextArea1.setColumns(20);
            jTextArea1.setRows(5);
            jScrollPane5.setViewportView(jTextArea1);

            jList5.setModel(new javax.swing.AbstractListModel() {
                String[] strings = {};
                public int getSize() { return strings.length; }
                public Object getElementAt(int i) { return strings[i]; }
            });
            jScrollPane6.setViewportView(jList5);

            jLabel2.setText("1. Files in Panel");
            jScrollPane3.setViewportView(jList3);
            jList3.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                    jList3ValueChanged(evt);
                }
            });
            jList3.setToolTipText("Select files with common staining panel for computing medians and optionally fold change");
            

            jLabel3.setText("2. Reference Files (Optional)");
            jScrollPane2.setViewportView(jList2);
            jList2.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                    jList2ValueChanged(evt);
                }
            });
            jList2.setToolTipText("Select reference (basal) files for use in fold change analysis. Panel must have multiple files to enable fold change analysis.");

            jLabel4.setText("3. Fold-change Markers");
            jScrollPane4.setViewportView(jList4);
            jList4.setToolTipText("Select markers for fold changes analysis. Reference files must be specified to select fold change parameters");
            
            jLabel1.setText("Panels");
            jList6.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            jScrollPane7.setViewportView(jList6);

            jLabel5.setText("4. Save Panel as:");

            jTextField1.setText("");

            jButton1.setText("Save");
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });

            jButton2.setText("Delete");
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton2ActionPerformed(evt);
                }
            });

            org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(contentPanel);
            contentPanel.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jLabel2)
                        .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 225, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(18, 18, 18)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 225, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel3))
                    .add(18, 18, 18)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 225, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel4))
                    .add(18, 18, 18)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jScrollPane7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 225, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel5)
                        .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 225, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel1)
                        .add(jButton1)
                        .add(jButton2))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .add(jLabel5)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jButton1)
                            .add(35, 35, 35)
                            .add(jLabel1)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jScrollPane7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 265, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(18, 18, 18)
                            .add(jButton2))
                        .add(layout.createSequentialGroup()
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                                .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE))))
                    .addContainerGap())
            );
            // </editor-fold>
            return contentPanel;
        }

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
            if (jTextField1.getText() == null || jTextField1.getText().equals("")) {
                JOptionPane.showMessageDialog(contentPanel, "Please enter a name for the panel.");
                return;
            }
            
            if (cxt.getAnalysisPanel(jTextField1.getText()) != null) {
                JOptionPane.showMessageDialog(contentPanel, "Panel with that name already exists. Please use a different name.");
                return;
            }

            // Record analysis panel
            cxt.addAnalysisPanel(
                jTextField1.getText(),
                new SpadeContext.AnalysisPanel(jList3.getSelectedValues(), null, jList2.getSelectedValues(), jList4.getSelectedValues())
            );
            jList6.setListData(cxt.getAnalysisPanelsNames());

            // Update panel file listing, now that some have been removed
            jList3.setModel(new javax.swing.AbstractListModel() {
                File[] files = cxt.getFCSFilesNotInPanel();
                public int getSize() { return files.length; }
                public Object getElementAt(int i) { return files[i]; }
            });
        }

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
            // Deleting a panel removes it from the list and adds it files
            // back to the potential set of panel files
            if (jList6.getSelectedIndex() >= 0) {
                cxt.removeAnalysisPanel((String)jList6.getSelectedValue());

                // Update panel file listing, now that some have added
                jList3.setModel(new javax.swing.AbstractListModel() {
                    File[] files = cxt.getFCSFilesNotInPanel();
                    public int getSize() { return files.length; }
                    public Object getElementAt(int i) { return files[i]; }
                });

                jList6.setListData(cxt.getAnalysisPanelsNames());
            }
        }
        
        // Selection of panel files changed
        private void jList3ValueChanged(javax.swing.event.ListSelectionEvent evt) {
            final Object[] selected = jList3.getSelectedValues();
            if (selected.length > 1) {
                // Set potential references files equal to panel files (only meaningful, if more than one file)
                jList2.setModel(new javax.swing.AbstractListModel() {
                    Object[] objs = selected;
                    public int getSize() { return objs.length; }
                    public Object getElementAt(int i) { return objs[i]; }
                });
                jList2.setEnabled(true);

                // Set possible reference markers as intersection of those in panel files
                jList4.setModel(new javax.swing.AbstractListModel() {
                    String[] strings = SpadeContext.getCommonMarkers(selected);
                    public int getSize() { return strings.length; }
                    public Object getElementAt(int i) { return strings[i]; }
                });
                jList4.setEnabled(!jList2.isSelectionEmpty());
            } else {
                jList2.setListData(new Object[0]);
                jList2.setEnabled(false);
                jList4.setListData(new Object[0]);
                jList4.setEnabled(false);
            }            
        }

        private void jList2ValueChanged(javax.swing.event.ListSelectionEvent evt) {
            jList4.setEnabled(!jList2.isSelectionEmpty());
        }

        private SpadeContext cxt;

        private javax.swing.JPanel contentPanel;

       // Variables declaration - do not modify
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JLabel jLabel5;
        private javax.swing.JList jList2;
        private javax.swing.JList jList3;
        private javax.swing.JList jList4;
        private javax.swing.JList jList5;
        private javax.swing.JList jList6;
        private javax.swing.JScrollPane jScrollPane2;
        private javax.swing.JScrollPane jScrollPane3;
        private javax.swing.JScrollPane jScrollPane4;
        private javax.swing.JScrollPane jScrollPane5;
        private javax.swing.JScrollPane jScrollPane6;
        private javax.swing.JScrollPane jScrollPane7;
        private javax.swing.JTextArea jTextArea1;
        private javax.swing.JTextField jTextField1;
        // End of variables declaration
    }

    public static class SummaryAndRun extends WorkflowWizard.PanelDescriptor {

        public static final String IDENTIFIER = "SUMMARY_AND_RUN_PANEL";

        public SummaryAndRun(SpadeContext cxt) {
            super(IDENTIFIER);

            this.cxt = cxt;

            setPanelComponent(createPanel());
        }

        @Override
        public Object getBackPanelDescriptor() {
            return PanelCreator.IDENTIFIER;
        }

        @Override
        public Object getNextPanelDescriptor() {
            return WorkflowWizard.PanelDescriptor.FINISH;
        }

        @Override
        public void aboutToDisplayPanel() {
            this.getWizard().setTitle("Summary and Run");
        }

        @Override
        public void displayingPanel() {
            jTextArea1.setText(cxt.getContextAsFormattedString());
        }

        @Override
        public void aboutToHidePanel() {
            ctl = null;
        }


        @Override
        public void nextButtonPressed() {
            try {
                if ((ctl != null) && (ctl.get() == 0)) {
                    // Launch analysis mode at completion by resetting context to
                    // point to "output" directory
                    System.err.println(cxt.getPath().getAbsolutePath() + File.separator + "output");
                    cxt.setPath(new File(cxt.getPath().getAbsolutePath() + File.separator + "output"));
                }
            } catch (IllegalArgumentException ex) {
                CyLogger.getLogger(WorkflowWizardPanels.class.getName()).error(null, ex);
            } catch (InterruptedException ex) {
                CyLogger.getLogger(WorkflowWizardPanels.class.getName()).error(null, ex);
            } catch (ExecutionException ex) {
                CyLogger.getLogger(WorkflowWizardPanels.class.getName()).error(null, ex);
            }
        }


        private JPanel createPanel() {
            contentPanel = new JPanel();
            // <editor-fold defaultstate="collapsed" desc="Generated Code">
            jScrollPane1 = new javax.swing.JScrollPane();
            jTextArea1 = new javax.swing.JTextArea();
            jButton1 = new javax.swing.JButton();
            jButton2 = new javax.swing.JButton();

            jTextArea1.setColumns(20);
            jTextArea1.setRows(5);
            jScrollPane1.setViewportView(jTextArea1);

            jButton1.setText("Run SPADE");
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });
            jButton1.setToolTipText("Run SPADE R package (generating new runSPADE script beforehand)");

            jButton2.setText("Generate runSPADE Script");
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton2ActionPerformed(evt);
                }
            });
            jButton2.setToolTipText("Generate runSPADE script, but do not actually run SPADE");

            org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(contentPanel);
            contentPanel.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(layout.createSequentialGroup()
                            .add(jButton2)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jButton1))
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 332, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(18, 18, 18)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jButton1)
                        .add(jButton2))
                    .addContainerGap(18, Short.MAX_VALUE))
            );

            // </editor-fold>
            return contentPanel;
        }

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
            try {
                cxt.authorRunSpade("runSPADE.R");
                ctl = new SpadeController(cxt.getPath(), "runSPADE.R");
                ctl.exec();
            } catch (IOException ex) {
                CyLogger.getLogger(CytoSpade.class.getName()).error(null, ex);
            }
        }

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
            try {
                cxt.authorRunSpade("runSPADE.R");
                JOptionPane.showMessageDialog(null, "runSPADE.R script file successfully written");
            } catch(IOException ex) {
                JOptionPane.showMessageDialog(null, "Error writing runSPADE.R file: " + ex.getMessage());
            }
        }

        private SpadeContext    cxt;
        private SpadeController ctl = null;

        private javax.swing.JPanel contentPanel;
         // Variables declaration - do not modify
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JTextArea jTextArea1;
        // End of variables declaration
    }

    public static class GeneratePDFs extends WorkflowWizard.PanelDescriptor {

        public static final String IDENTIFIER = "GENERATE_PDFS_PANEL";

        public GeneratePDFs(SpadeContext cxt) {
            super(IDENTIFIER);

            this.cxt = cxt;

            setPanelComponent(createPanel());
        }

        @Override
        public Object getNextPanelDescriptor() {
            return WorkflowWizard.PanelDescriptor.FINISH;
        }

        @Override
        public void aboutToDisplayPanel() {
            this.getWizard().setTitle("Generate PDFs");
        }

        @Override
        public void displayingPanel() {
            
        }

        @Override
        public void aboutToHidePanel() {
            ctl = null;
        }


        @Override
        public void nextButtonPressed() {
            
        }


        private JPanel createPanel() {                   
            contentPanel = new JPanel();
            // <editor-fold defaultstate="collapsed" desc="Generated Code">
            jLabel1 = new javax.swing.JLabel();
            jLabel2 = new javax.swing.JLabel();
            jButton1 = new javax.swing.JButton();
            jTextField1 = new javax.swing.JTextField();

            jLabel1.setText("Node Size Scale Factor");

            jLabel2.setText("Set parameters for PDF Generation");

            jButton1.setText("Generate PDFs");
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });

            jTextField1.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
            jTextField1.setText(Double.toString(cxt.getNodeSizeScaleFactor()));
            jTextField1.setToolTipText("Increase scaling factor to increase node size in PDFs");

            org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(contentPanel);
            contentPanel.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .add(jLabel1)
                            .add(18, 18, 18)
                            .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(jLabel2))
                    .addContainerGap(253, Short.MAX_VALUE))
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(381, Short.MAX_VALUE)
                    .add(jButton1)
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(21, 21, 21)
                    .add(jLabel2)
                    .add(24, 24, 24)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(jLabel1)
                        .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 223, Short.MAX_VALUE)
                    .add(jButton1)
                    .addContainerGap())
            );

            // </editor-fold>
            return contentPanel;
        }

        private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
            try {
                cxt.setNodeSizeScaleFactor(Double.parseDouble(jTextField1.getText()));

                cxt.authorPlotSpade("plotSPADE.R");
                ctl = new SpadeController(cxt.getPath(), "plotSPADE.R");
                ctl.exec();
            } catch (IOException ex) {
                CyLogger.getLogger(CytoSpade.class.getName()).error(null, ex);
            }
        }

        private SpadeContext    cxt;
        private SpadeController ctl = null;

        private javax.swing.JPanel contentPanel;
        // Variables declaration - do not modify
        private javax.swing.JButton jButton1;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JTextField jTextField1;
        // End of variables declaration
    }
}
