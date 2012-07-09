package cytospade;
import cytoscape.Cytoscape;
import cytoscape.logger.CyLogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

/**
 *
 * @author mlinderm
 */
public class SpadeController extends SwingWorker<Integer, Void> {

    File   workingDir;
    String script;

    javax.swing.JDialog     outDialog;
    javax.swing.JTextArea   outArea;
    javax.swing.JScrollPane outAreaWrapper;
    javax.swing.JPanel      buttonPanel;
    javax.swing.JSeparator  separator;
    javax.swing.JButton     doneButton;

    public SpadeController(File cwd, String script) {
        this.workingDir = cwd;
        this.script = script;
    }

    public void exec() {
        // Initialize dialog for displaying output
        //
        outDialog = new javax.swing.JDialog(Cytoscape.getDesktop());
        outDialog.getContentPane().setLayout(new java.awt.BorderLayout());
        outDialog.setTitle("SPADE Execution Console");

        outArea = new javax.swing.JTextArea();
        outArea.setColumns(60);
        outArea.setRows(20);
        outArea.setEditable(false);

        outAreaWrapper = new javax.swing.JScrollPane();
        outAreaWrapper.setViewportView(outArea);

        outDialog.getContentPane().add(outAreaWrapper, java.awt.BorderLayout.CENTER);

        buttonPanel = new javax.swing.JPanel();
        separator = new javax.swing.JSeparator();
        doneButton = new javax.swing.JButton();

        // Buttons at the bottom of the panel
        doneButton.setText("Done");
        doneButton.setEnabled(false); // Disable until script completes

        buttonPanel.setLayout(new java.awt.BorderLayout());
        buttonPanel.add(separator, java.awt.BorderLayout.NORTH);
        buttonPanel.add(doneButton, java.awt.BorderLayout.EAST);

        outDialog.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outDialog.dispose();
            }
        });

        this.execute();

        outDialog.setModal(true);
        outDialog.pack();
        outDialog.setLocationRelativeTo(outDialog.getParent());
        outDialog.show();
    }

    protected Integer doInBackground() {
        // Execute script, updating dialog with output

        String executable = getExecutable();
        //TODO this used to prompt the user to find the install path instead.
        if (executable == null) {
            outArea.append("Unable to find R executable. Perhaps try generating and running script manually.\n");
            return 1;
        }

        ProcessBuilder pb = new ProcessBuilder(executable, "-f", script);
        pb.directory(workingDir);
        pb.redirectErrorStream(true);

        try {
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                outArea.append(line+"\n");
            }
            reader.close();
            return p.waitFor();
        } catch (IOException ex) {
            CyLogger.getLogger(SpadeController.class.getName()).error(null, ex);
        } catch (InterruptedException ex) {
            CyLogger.getLogger(SpadeController.class.getName()).error(null, ex);
        }
        return 1;
    }

    @Override
    protected void done() {
        Integer exit_status = 1;
        try {
            exit_status = this.get();
        } catch (InterruptedException ex) {
            CyLogger.getLogger(SpadeController.class.getName()).error(null, ex);
        } catch (ExecutionException ex) {
            CyLogger.getLogger(SpadeController.class.getName()).error(null, ex);
        }
        if (exit_status == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "Successfully executed script.");
        } else {
            javax.swing.JOptionPane.showMessageDialog(null, "Execution failed. See output dialog for clues.");
        }
        doneButton.setEnabled(true);
    }

    private String getExecutable() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) {
            // Need to go into the registry to recover the executable
            try {
                String REGSTR_TOKEN = "REG_SZ";

                Process p = Runtime.getRuntime().exec("REG QUERY \"HKLM\\SOFTWARE\\R-core\\R\" /v InstallPath");
                BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(p.getInputStream()));

                p.waitFor();

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                String result = sb.toString();
                int token = result.indexOf(REGSTR_TOKEN);
                if (token == -1)
                    return null;
                else
                    return '"'+result.substring(token +  REGSTR_TOKEN.length()).trim()+"\\bin\\R\"";
            } catch (Exception ex) {
                CyLogger.getLogger(SpadeController.class.getName()).error(null, ex);
                return null;
            }
        } else
            return "R";
    }


    

}
