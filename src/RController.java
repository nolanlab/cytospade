
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mlinderm
 */
public class RController {

    public RController() {
    }

    public int run(String script, File cwd) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("Rscript", script);
        pb.directory(cwd);
        pb.redirectErrorStream(true);


        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.err.println(line);
        }
        reader.close();

        int exit_status;
        try {
            exit_status = p.waitFor();
            return exit_status;
        } catch (InterruptedException ex) {
            Logger.getLogger(RController.class.getName()).log(Level.SEVERE, null, ex);
            return 1;
        }
    }
}
