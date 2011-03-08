
import java.awt.Component;
import java.io.File;
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

        public Intro(SPADEContext cxt) {
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
            SPADEContext.WorkflowKind wk = cxt.getWorkflowKind();
            if (wk == SPADEContext.WorkflowKind.ANALYSIS) {
                nextID = Intro.FINISH;
                getWizard().setNextButtonEnabled(true);
            } else if (wk == SPADEContext.WorkflowKind.PROCESSING) {
                nextID = ClusterMarkerSelect.IDENTIFIER;
                getWizard().setNextButtonEnabled(true);
            } else {
                // Should not get here
                throw new IllegalArgumentException();
            }
        }

        private SPADEContext cxt;
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

        public ClusterMarkerSelect(SPADEContext cxt) {
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
            cxt.setTargetDownsample((Integer)jSpinner3.getValue());
        }

        private JPanel createPanel() {
            contentPanel = new JPanel();
            // <editor-fold defaultstate="collapsed" desc="Generated Code">
            
            jScrollPane1 = new javax.swing.JScrollPane();
            jList1 = new javax.swing.JList();
            jLabel1 = new javax.swing.JLabel();
            jLabel2 = new javax.swing.JLabel();
            jSpinner1 = new javax.swing.JSpinner();
            jLabel3 = new javax.swing.JLabel();
            jSpinner2 = new javax.swing.JSpinner();
            jLabel4 = new javax.swing.JLabel();
            jSpinner3 = new javax.swing.JSpinner();

           

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
            jScrollPane1.setViewportView(jList1);

            jLabel1.setText("Select Clustering Markers");

            jLabel2.setText("Arcsinh Value (CyToF=5, Optical=150)");

            jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(cxt.getArcsinh()), Integer.valueOf(0), null, Integer.valueOf(1)));

            jLabel3.setText("Target Number of Clusters");

            jSpinner2.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(cxt.getTargetClusters()), Integer.valueOf(0), null, Integer.valueOf(1)));

            jLabel4.setText("Target Number of Downsampled Events");

            jSpinner3.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(cxt.getTargetDownsample()), Integer.valueOf(0), null, Integer.valueOf(100)));

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
                        .add(jSpinner3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
                            .add(jSpinner3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap())
            );
            // </editor-fold>
            return contentPanel;
        }

        private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {
            getWizard().setNextButtonEnabled((jList1.getSelectedIndex() != -1));
        }

        private SPADEContext cxt;
        
        private javax.swing.JPanel contentPanel;
         // Variables declaration - do not modify
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JList jList1;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JSpinner jSpinner1;
        private javax.swing.JSpinner jSpinner2;
        private javax.swing.JSpinner jSpinner3;
        // End of variables declaration
    }

    public static class PanelCreator extends WorkflowWizard.PanelDescriptor {

        public static final String IDENTIFIER = "PANEL_CREATOR_PANEL";

        public PanelCreator(SPADEContext cxt) {
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

            // Initialize the panel file list
            jList3.setModel(new javax.swing.AbstractListModel() {
                File[] files = cxt.getFCSFiles();
                public int getSize() { return files.length; }
                public Object getElementAt(int i) { return files[i]; }
            });
            
            jList3.setCellRenderer(fileRenderer);
            jList2.setCellRenderer(fileRenderer);
        }

        @Override
        public void aboutToHidePanel() {
            // Collect values from input and update context
           
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
            jList3.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                    jList3ValueChanged(evt);
                }
            });
            jScrollPane3.setViewportView(jList3);

            jLabel3.setText("2. Reference Files (Optional)");
            jScrollPane2.setViewportView(jList2);
            jList2.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
                public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                    jList2ValueChanged(evt);
                }
            });

            jLabel4.setText("3. Fold-change Markers");
            jScrollPane4.setViewportView(jList4);

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
            cxt.addAnalysisPanel(
                jTextField1.getText(),
                new SPADEContext.AnalysisPanel(jList3.getSelectedValues(), null, jList2.getSelectedValues(), jList4.getSelectedValues())
            );
            jList6.setListData(cxt.getAnalysisPanelsNames());
        }

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
            // TODO add your handling code here:
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
                    String[] strings = SPADEContext.getCommonMarkers(selected);
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

        private SPADEContext cxt;

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

        public SummaryAndRun(SPADEContext cxt) {
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

            jButton2.setText("Generate runSPADE Script");
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton2ActionPerformed(evt);
                }
            });

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
            // TODO add your handling code here:
        }

        private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
            cxt.authorRunSpade();
            JOptionPane.showMessageDialog(null, "runSPADE.R file successfully written");
        }
      

        private SPADEContext cxt;

        private javax.swing.JPanel contentPanel;
         // Variables declaration - do not modify
        private javax.swing.JButton jButton1;
        private javax.swing.JButton jButton2;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JTextArea jTextArea1;
        // End of variables declaration
    }

}
