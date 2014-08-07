/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cytospade.ui;

import cytospade.ui.NodeContextMenuItems.MakeNestedNetwork;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/**
 *
 * @author mlinderm
 */
//public class NodeContextMenu implements NodeContextMenuListener {
public class NodeContextMenu implements ActionListener {

    public void addNodeContextMenuItems(View<CyNode> nv, JPopupMenu jpm) {
        JMenu spadeMenu = new JMenu("SPADE");

        // Populate menu depending on context...
        //if (nv.isSelected() && nv.getNode().getNestedNetwork() == null) {
        if (nv.isSet(BasicVisualLexicon.NODE_SELECTED) && nv.getVisualProperty(BasicVisualLexicon.NETWORK_DEPTH) == 1) {
            JMenuItem jmi = new JMenuItem(MakeNestedNetwork.LABEL);
            jmi.addActionListener(new MakeNestedNetwork());
            spadeMenu.add(jmi);
        }

        if (nv.isSet(BasicVisualLexicon.NODE_SELECTED) && nv.getVisualProperty(BasicVisualLexicon.NETWORK_DEPTH) > 1) {
            //temporarily out while fixing
            //JMenuItem jmi = new JMenuItem(UndoNestedNetwork.LABEL);
            //jmi.addActionListener(new UndoNestedNetwork());
            //spadeMenu.add(jmi);
        }

        if (spadeMenu.getItemCount() > 0) {
            if (jpm == null) {
                jpm = new JPopupMenu();
            }
            jpm.add(spadeMenu);
        }
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
