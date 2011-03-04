
import javax.swing.JLabel;
import javax.swing.JPanel;

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

        public Intro() {
            super(IDENTIFIER, createPanel());
        }
        
        private static JPanel createPanel() {
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new java.awt.BorderLayout());

            JLabel welcomeTitle = new JLabel();
            welcomeTitle.setText("Welcome to SPADE");
            contentPanel.add(welcomeTitle, java.awt.BorderLayout.NORTH);

            return contentPanel;
        }
    }

}
