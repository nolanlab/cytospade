package cytospade;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Basic infrastructure for implementing modal wizard.
 * Adapted from: http://java.sun.com/developer/technicalArticles/GUI/swing/wizard/
 * 
 * @author mlinderm
 */
public class WorkflowWizard {

    public static final int FINISH_RETURN_CODE = 0;
    public static final int CANCEL_RETURN_CODE = 1;
    public static final int ERROR_RETURN_CODE = 2;

    public static final String NEXT_BUTTON_ACTION_COMMAND = "NextButtonActionCommand";
    public static final String BACK_BUTTON_ACTION_COMMAND = "BackButtonActionCommand";
    public static final String CANCEL_BUTTON_ACTION_COMMAND = "CancelButtonActionCommand";

    private Model      wizardModel;
    private Controller wizardController;
    private JDialog    wizardDialog;

    private JPanel     cardPanel;
    private CardLayout cardLayout;

    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;

    private int returnCode;

    public WorkflowWizard(Frame desktop) {
        wizardModel = new Model();
        wizardDialog = new JDialog(desktop);
        initWizardComponents();
    }

    public int showModalDialog() {
        wizardDialog.setModal(true);
        wizardDialog.pack();
        wizardDialog.setLocationRelativeTo(wizardDialog.getParent());
        wizardDialog.show();
        return returnCode;
    }

    public void setCurrentPanel(Object id) {
        
        if (id == null)
            close(ERROR_RETURN_CODE);

        PanelDescriptor oldPanelDescriptor = wizardModel.getCurrentPanelDescriptor();
        if (oldPanelDescriptor != null)
            oldPanelDescriptor.aboutToHidePanel();

        wizardModel.setCurrentPanel(id);
        wizardModel.getCurrentPanelDescriptor().aboutToDisplayPanel();

        backButton.setEnabled(wizardModel.getBackButtonEnabled());
        nextButton.setEnabled(wizardModel.getNextButtonEnabled());

        cardLayout.show(cardPanel, id.toString());
        wizardModel.getCurrentPanelDescriptor().displayingPanel();
    }


    public void registerWizardPanel(Object id, PanelDescriptor panel) {
        cardPanel.add(panel.getPanelComponent(), id);
        panel.setWizard(this);
        wizardModel.registerPanel(id, panel);
    }

    public void setNextButtonEnabled(boolean enabled) { nextButton.setEnabled(enabled); }

    public void setTitle(String s) {
        wizardDialog.setTitle(s);
    }
   
    public String getTitle() {
        return wizardDialog.getTitle();
    }


    private void initWizardComponents() {
        wizardController = new Controller(this);

        wizardDialog.getContentPane().setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        JSeparator separator = new JSeparator();
        Box buttonBox = new Box(BoxLayout.X_AXIS);

        cardPanel = new JPanel();
        cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        backButton = new JButton();
        nextButton = new JButton();
        cancelButton = new JButton();

        backButton.setText("Back");
        nextButton.setText("Next");
        cancelButton.setText("Cancel");

        backButton.setActionCommand(BACK_BUTTON_ACTION_COMMAND);
        nextButton.setActionCommand(NEXT_BUTTON_ACTION_COMMAND);
        cancelButton.setActionCommand(CANCEL_BUTTON_ACTION_COMMAND);

        backButton.addActionListener(wizardController);
        nextButton.addActionListener(wizardController);
        cancelButton.addActionListener(wizardController);

        //  Create the buttons with a separator above them, then place them
        //  on the east side of the panel with a small amount of space between
        //  the back and the next button, and a larger amount of space between
        //  the next button and the cancel button.

        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(separator, BorderLayout.NORTH);

        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        buttonBox.add(backButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(nextButton);
        buttonBox.add(Box.createHorizontalStrut(30));
        buttonBox.add(cancelButton);

        buttonPanel.add(buttonBox, java.awt.BorderLayout.EAST);

        wizardDialog.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
        wizardDialog.getContentPane().add(cardPanel, java.awt.BorderLayout.CENTER);
    }

    private Model getModel() {
        return wizardModel;
    }

    private Controller getController() {
        return wizardController;
    }


    public void close(int code) {
        returnCode = code;
        wizardDialog.dispose();
    }

    /**
     * Parent class for all wizard panels
     */
    public static class PanelDescriptor {
        private static final String DEFAULT_PANEL_IDENTIFIER = "defaultPanelIdentifier";
        public static final FinishIdentifier FINISH = new FinishIdentifier();

        private WorkflowWizard wizard;
        private Component targetPanel;
        private Object panelIdentifier;


        public PanelDescriptor() {
            panelIdentifier = DEFAULT_PANEL_IDENTIFIER;
            targetPanel = new JPanel();
        }

        public PanelDescriptor(Object id) {
            panelIdentifier = id;
            targetPanel = new JPanel();
        }

        public PanelDescriptor(Object id, Component panel) {
            panelIdentifier = id;
            targetPanel = panel;
        }

        public void setPanelComponent(Component panel) {
            targetPanel = panel;
        }

        public final Component getPanelComponent() {
            return targetPanel;
        }

        public Object getNextPanelDescriptor() {
            return null;
        }

        public Object getBackPanelDescriptor() {
            return null;
        }

        public void setWizard(WorkflowWizard aThis) {
            wizard = aThis;
        }

        public final WorkflowWizard getWizard() {
            return wizard;
        }

        /*
         * Callbacks invoked by wizard
         */
        public void aboutToHidePanel() {}

        public void aboutToDisplayPanel() {}

        public void displayingPanel() {}

        public void nextButtonPressed() {}

        
        static class FinishIdentifier {
            public static final String ID = "FINISH";
        }
    }


    private static class Controller implements ActionListener {
        private WorkflowWizard wizard;

        public Controller(WorkflowWizard ww) {
            wizard = ww;
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getActionCommand().equals(WorkflowWizard.CANCEL_BUTTON_ACTION_COMMAND))
                cancelButtonPressed();
            else if (evt.getActionCommand().equals(WorkflowWizard.BACK_BUTTON_ACTION_COMMAND))
                backButtonPressed();
            else if (evt.getActionCommand().equals(WorkflowWizard.NEXT_BUTTON_ACTION_COMMAND))
                nextButtonPressed();
        }

        private void cancelButtonPressed() {
            wizard.close(WorkflowWizard.CANCEL_RETURN_CODE);
        }

        private void nextButtonPressed() {

            Model model = wizard.getModel();
            PanelDescriptor descriptor = model.getCurrentPanelDescriptor();

            descriptor.nextButtonPressed();

            //  If it is a finishable panel, close down the dialog. Otherwise,
            //  get the ID that the current panel identifies as the next panel,
            //  and display it.

            Object nextPanelDescriptor = descriptor.getNextPanelDescriptor();
            if (nextPanelDescriptor instanceof PanelDescriptor.FinishIdentifier) {
                wizard.close(WorkflowWizard.FINISH_RETURN_CODE);
            } else {
                wizard.setCurrentPanel(nextPanelDescriptor);
            }

        }

        private void backButtonPressed() {

            Model model = wizard.getModel();
            PanelDescriptor descriptor = model.getCurrentPanelDescriptor();

            //  Get the descriptor that the current panel identifies as the previous
            //  panel, and display it.

            Object backPanelDescriptor = descriptor.getBackPanelDescriptor();
            wizard.setCurrentPanel(backPanelDescriptor);
        }
    }

    private static class Model {

        private PanelDescriptor currentPanel;

        private HashMap panelHashmap;


        /**
         * Default constructor.
         */
        public Model() {
            panelHashmap = new HashMap();
        }

        /**
         * Returns the currently displayed WizardPanelDescriptor.
         * @return The currently displayed WizardPanelDescriptor
         */
        PanelDescriptor getCurrentPanelDescriptor() {
            return currentPanel;
        }

        /**
         * Registers the WizardPanelDescriptor in the model using the Object-identifier specified.
         * @param id Object-based identifier
         * @param descriptor WizardPanelDescriptor that describes the panel
         */
        void registerPanel(Object id, PanelDescriptor descriptor) {
            panelHashmap.put(id, descriptor);
        }

        boolean setCurrentPanel(Object id) {
            PanelDescriptor nextPanel = (PanelDescriptor) panelHashmap.get(id);

            if (nextPanel == null) {
                throw new IllegalArgumentException();
            }

            PanelDescriptor oldPanel = currentPanel;
            currentPanel = nextPanel;

            return true;
        }

        boolean getBackButtonEnabled() { return currentPanel.getBackPanelDescriptor() != null; }
        boolean getNextButtonEnabled() { return currentPanel.getNextPanelDescriptor() != null; }
    }
}
