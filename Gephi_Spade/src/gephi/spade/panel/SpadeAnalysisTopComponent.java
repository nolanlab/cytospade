/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gephi.spade.panel;

import facs.VisualMapping;
import java.awt.Container;
import org.gephi.preview.api.*;
import org.gephi.preview.types.*;
import java.awt.Color;
import java.io.FileNotFoundException;
import org.gephi.preview.*;
import java.io.FileReader;
import java.io.Reader;
import org.gephi.graph.api.*;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;
import org.gephi.visualization.apiimpl.ModelImpl;
import java.io.*;
import java.util.*;
import org.gephi.graph.api.*;
import org.gephi.layout.plugin.random.*;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.gephi.io.importer.plugin.file.ImporterBuilderGML;
import org.gephi.io.importer.plugin.file.ImporterGML;
import org.gephi.io.importer.spi.FileImporter;
import javax.swing.*;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.generator.plugin.RandomGraph;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.*;
import org.gephi.data.attributes.api.*;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.project.api.Project;
import org.gephi.visualization.VizController;
import processing.core.PApplet;
import org.gephi.visualization.opengl.*;
import gephi.spade.panel.FCSOperations;
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
        dtd = "-//gephi.spade.panel//SpadeAnalysis//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "SpadeAnalysisTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "rankingmode", openAtStartup = true)
@ActionID(category = "Window", id = "gephi.spade.panel.SpadeAnalysisTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_SpadeAnalysisAction",
        preferredID = "SpadeAnalysisTopComponent"
)
@Messages({
    "CTL_SpadeAnalysisAction=SpadeAnalysis",
    "CTL_SpadeAnalysisTopComponent=SpadeAnalysis Window",
    "HINT_SpadeAnalysisTopComponent=This is a SpadeAnalysis window"
})
public final class SpadeAnalysisTopComponent extends TopComponent {

    private JComboBox<File> comboBox;
    private Object file1;
    private FCSOperations file2;
    private File file3;
    private SpadeContext spadeCxt;
    private VisualMapping visualMapping;
    private FCSOperations fcsOperations;
    private plotLayoutTopComponent plotLayout;
    private ReentrantLock panelLock;
    private javax.swing.JTextField jTextField1;
    DefaultTableModel TValTableModel = new javax.swing.table.DefaultTableModel(
            new Object[][]{{null, null}},
            new String[]{"Parameter", "T value"});
    
    public SpadeAnalysisTopComponent() {
        initComponents();
        setName(Bundle.CTL_SpadeAnalysisTopComponent());
        setToolTipText(Bundle.HINT_SpadeAnalysisTopComponent());
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        
    }
    
public class FileItem {

        private File f;

        public FileItem(File file) {
            f = file;
        }

        @Override
        public String toString() {
            //Return only the file base name
            return f.getName().substring(0, f.getName().indexOf((".fcs")));
        }

        public String getFCSpath() {
            return f.getName();
        }

        public File getFCSFile() {
            return f;
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        List<String> results = new ArrayList<String>();
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setCurrentDirectory(new java.io.File("."));
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            System.out.println("getCurrentDirectory(): "
                +  jFileChooser.getCurrentDirectory());
            System.out.println("getSelectedFile() : "
                +  jFileChooser.getSelectedFile());
        }
        else {
            System.out.println("No Selection ");
        }
        File[] filesInDirectory = jFileChooser.getCurrentDirectory().listFiles();

        //Object[] gmlfiles = file.listFiles();
        javax.swing.JComboBox<File> jComboBox1 = new javax.swing.JComboBox<File>();
        jLabel1 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        PlotContainer = new javax.swing.JScrollPane();
        NumberEventsLabel = new javax.swing.JLabel();
        ColoringSelect1 = new javax.swing.JComboBox();

        jComboBox1.setToolTipText(org.openide.util.NbBundle.getMessage(SpadeAnalysisTopComponent.class, "SpadeAnalysisTopComponent.jComboBox1.toolTipText")); // NOI18N
        for ( File file : filesInDirectory ) {
            if (!file.isDirectory()){
                jComboBox1.addItem(file);
            }

        }

        jComboBox1.setSelectedItem(null);
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SpadeAnalysisTopComponent.class, "SpadeAnalysisTopComponent.jLabel1.text")); // NOI18N

        jTextField2.setEditable(false);
        jTextField2.setText(org.openide.util.NbBundle.getMessage(SpadeAnalysisTopComponent.class, "SpadeAnalysisTopComponent.jTextField2.text")); // NOI18N
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(SpadeAnalysisTopComponent.class, "SpadeAnalysisTopComponent.jButton1.text")); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(SpadeAnalysisTopComponent.class, "SpadeAnalysisTopComponent.jButton1.toolTipText")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(NumberEventsLabel, org.openide.util.NbBundle.getMessage(SpadeAnalysisTopComponent.class, "SpadeAnalysisTopComponent.NumberEventsLabel.text")); // NOI18N

        ColoringSelect1.setMaximumRowCount(20);
        ColoringSelect1.setToolTipText("Select attribute for coloring nodes");
        ColoringSelect1.setMinimumSize(null);
        ColoringSelect1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        ColoringSelect1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ColoringSelect1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(NumberEventsLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton1)
                                .addGap(34, 34, 34)
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(ColoringSelect1, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 159, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(105, Short.MAX_VALUE)
                    .addComponent(PlotContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 489, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(42, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NumberEventsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ColoringSelect1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(437, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(206, Short.MAX_VALUE)
                    .addComponent(PlotContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(31, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
        
        comboBox = (JComboBox<File>) evt.getSource();
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        org.gephi.project.api.Workspace workspace = pc.getCurrentWorkspace();
       
        //Generate a random graph
        org.gephi.io.importer.api.Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
       
        RandomGraph randomGraph = new RandomGraph();
        
        //randomGraph.setNumberOfNodes(50);
        //randomGraph.setWiringProbability(0.005);
        randomGraph.generate(container.getLoader());
        
        //Append imported data to GraphAPI
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        NodeDraft nd = Lookup.getDefault().lookup(NodeDraft.class);
        AttributeColumnsController acc = Lookup.getDefault().lookup(AttributeColumnsController.class);
        AttributeController atCon = Lookup.getDefault().lookup(AttributeController.class);
        
        
            try{
                file1 = comboBox.getSelectedItem();
                file3 = new File("C:/f1/a_Ungated.fcs.density.fcs.cluster.fcs");
                importController.process(importController.importFile((File)file1), new DefaultProcessor(), workspace);
                
                //File fcsFile = ((FileItem) comboBox.getSelectedItem()).getFCSFile();
                
                
                
                this.fcsOperations = new FCSOperations((File) file3);
                
                this.plotLayout = new plotLayoutTopComponent();
                
                this.plotLayout.setFCS(this.fcsOperations);
                this.plotLayout.getFCS().updateSelectedNodes();
                this.plotLayout.updatePlot();
                
                this.PlotContainer.setViewportView(this.plotLayout);
                
                this.PlotContainer.setVisible(true);
                //updateFCSConsumers();
               // importController.process(importController.importFile((File)comboBox.getSelectedItem()), new DefaultProcessor(), workspace);
                updateFCSConsumers();
        
        //importController.process(container, new DefaultProcessor(), workspace);
        //importController.process(importController.importFile((File)comboBox.getSelectedItem()), new DefaultProcessor(), workspace);
        
        }catch (Exception fex){}
        AttributeModel model = atCon.getModel(workspace);
        AttributeTable at = model.getNodeTable();
        AttributeColumn[] columns = at.getColumns();
        
        for (AttributeColumn col : columns){
            if (acc.canDeleteColumn(col)){
                String originalTitle = col.getTitle();
                //acc.duplicateColumn(at, col, TOOL_TIP_TEXT_KEY, AttributeType.DYNAMIC_FLOAT);
                AttributeColumn tempCol = acc.duplicateColumn(at, col, "temp", AttributeType.FLOAT);
                acc.deleteAttributeColumn(at, col);
                acc.duplicateColumn(at, tempCol, originalTitle, AttributeType.FLOAT);
                acc.deleteAttributeColumn(at, tempCol);
            }
        }
        
        //AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        
        PreviewModel previewModel = previewController.getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.RED));
        previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 50);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 10f);
        previewModel.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.BLACK);
        previewController.refreshPreview();
        
        
        

            //Refresh the preview and reset the zoom
     
        /*
        
        ImportController ic = Lookup.getDefault().lookup(ImportController.class);
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        FileImporter fi = ic.getFileImporter((File) comboBox.getSelectedItem());
        try{
        ic.importFile((File) comboBox.getSelectedItem());
        }catch(Exception ex){}
        
        /*
        pc.newProject();
        org.gephi.project.api.Workspace ws = pc.getCurrentWorkspace();
        
        try{
            container = (Container) ic.importFile((File) comboBox.getSelectedItem());
            //ic.process((org.gephi.io.importer.api.Container) container, new DefaultProcessor(), ws);
            //Reader reader = new FileReader(file);
        }catch (FileNotFoundException fnfe){}
        
        */
    }//GEN-LAST:event_jComboBox1ActionPerformed
     
    private void updateFCSConsumers() {
        (new SwingWorker<Integer, Void>() {

            @Override
            protected Integer doInBackground() throws Exception {
                fcsOperations.updateSelectedNodes();

                plotLayout.updatePlot();
                
                return 0;
            }
        }).execute();
    }
    /*
     private void updateNodeSizeAndColors() {
        // Skip mapping if no file or coloring attribute is specified
        if ((comboBox.getSelectedIndex() < 0) || (ColoringSelect1.getSelectedIndex() < 0)) {
            return;
        }
        
        try {
            visualMapping.setCurrentMarkersAndRangeKind(
                    "percenttotal",
                    ColoringSelect1.getSelectedItem().toString(),
                    spadeCxt.getNormalizationKind(),
                    spadeCxt.getSymmetry());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Invalid choice of mapping parameters: " + e);
            return;
        }

        //VisualMappingManager cyVMM = Cytoscape.getVisualMappingManager();
        VisualMappingManager cyVMM = spadeCxt.adapter.getVisualMappingManager();

        try {

            //VisualStyle spadeVS = cyVMM.getCalculatorCatalog().getVisualStyle("SPADEVisualStyle");
            VisualStyle spadeVS = spadeCxt.adapter.getVisualStyleFactory().createVisualStyle("SPADEVisualStyle");
            if (spadeVS != null) {
                // Overwrite visual style, only way to get Cytoscape to reliably update
                //cyVMM.getCalculatorCatalog().removeVisualStyle("SPADEVisualStyle");
                cyVMM.removeVisualStyle(cyVMM.getCurrentVisualStyle());
            }
            //spadeVS = new VisualStyle("SPADEVisualStyle");
            cyVMM.setCurrentVisualStyle(spadeVS);
            // Update with new calculators
            //GlobalAppearanceCalculator globalAppCalc = new GlobalAppearanceCalculator();
            //globalAppCalc.setDefaultNodeSelectionColor(Color.MAGENTA);
            cnv.setVisualProperty(BasicVisualLexicon.NODE_SELECTED_PAINT, Color.MAGENTA);
            //globalAppCalc.setDefaultEdgeSelectionColor(Color.MAGENTA);
            cnv.setVisualProperty(BasicVisualLexicon.EDGE_SELECTED_PAINT, Color.MAGENTA);
            //globalAppCalc.setDefaultBackgroundColor(Color.BLACK);
            //spadeVS.setGlobalAppearanceCalculator(globalAppCalc);
            cnv.setVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, Color.BLACK);
            //NodeAppearanceCalculator nodeAppCalc = new NodeAppearanceCalculator(spadeVS.getDependency());
            //nodeAppCalc.setCalculator(visualMapping.createColorCalculator());
            //nodeAppCalc.setCalculator(visualMapping.createSizeCalculator());
            //spadeVS.setNodeAppearanceCalculator(nodeAppCalc);
            cnv.updateView();
            
            // Set a few defaults now that we have overwritten the calculators
            //VisualPropertyType.NODE_SHAPE.setDefault(spadeVS, NodeShapeVisualProperty.ELLIPSE);
            spadeVS.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
            cnv.setViewDefault(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
            //cnv.setViewDefault(BasicVisualLexicon.NODE_SHAPE, spadeVS);
            //VisualPropertyType.NODE_FILL_COLOR.setDefault(spadeVS, Color.LIGHT_GRAY);
            spadeVS.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.LIGHT_GRAY);
            //spadeVS.getDependency().set(VisualPropertyDependency.Definition.NODE_SIZE_LOCKED, true);
            //spadeVS.addVisualPropertyDependency(BasicVisualLexicon);
            //VisualPropertyType.NODE_BORDER_COLOR.setDefault(spadeVS, Color.WHITE);
            spadeVS.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, Color.WHITE);
            //VisualPropertyType.NODE_LINE_WIDTH.setDefault(spadeVS, 4);
            spadeVS.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 4.0);
            //VisualPropertyType.EDGE_COLOR.setDefault(spadeVS, Color.WHITE);
            spadeVS.setDefaultValue(BasicVisualLexicon.EDGE_PAINT, Color.WHITE);
            //VisualPropertyType.EDGE_LINE_WIDTH.setDefault(spadeVS, 2);
            spadeVS.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.LIGHT_GRAY);

            //cyVMM.getCalculatorCatalog().addVisualStyle(spadeVS);
            cyVMM.getAllVisualStyles().add(spadeVS);
            cyVMM.setCurrentVisualStyle(spadeVS);
            //cyVMM.setVisualStyle(spadeVS);
            //Cytoscape.getCurrentNetworkView().setVisualStyle(spadeVS.getName());
            spadeVS.apply(cnv);
            cnv.updateView();

        } catch (RuntimeException ex) {
            //CyLogger.getLogger().error("Error Visual Mapping", ex);
            System.out.println("error line 296...spadeanalysispanel");
        }
    }
*/
    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        ArrayList<Node> selectedNodes = new ArrayList<Node>();
        ModelImpl[] model = Lookup.getDefault().lookup(ModelImpl[].class);
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        org.gephi.project.api.Workspace workspace = pc.getCurrentWorkspace();
        
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        GraphModel gm = gc.getModel(workspace);
        Graph graph = gm.getGraph();
        
        jTextField2.setText(Integer.toString(graph.getNodeCount()));
    }//GEN-LAST:event_jButton1ActionPerformed

    private void ColoringSelect1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ColoringSelect1ActionPerformed
        //updateNodeSizeAndColors();
        //Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
        //cnv.updateView();
        this.plotLayout.updatePlot();
    }//GEN-LAST:event_ColoringSelect1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox ColoringSelect1;
    private javax.swing.JLabel NumberEventsLabel;
    private javax.swing.JScrollPane PlotContainer;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
         
             
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
              

