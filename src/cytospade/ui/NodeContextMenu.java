/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cytospade.ui;

import cytospade.ui.NodeContextMenuItems.MakeNestedNetwork;
import cytospade.ui.NodeContextMenuItems.UndoNestedNetwork;
import ding.view.NodeContextMenuListener;
import giny.view.NodeView;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author mlinderm
 */
public class NodeContextMenu implements NodeContextMenuListener {

    public void addNodeContextMenuItems(NodeView nv, JPopupMenu jpm) {
        JMenu spadeMenu = new JMenu("SPADE");

        // Populate menu depending on context...
        if (nv.isSelected() && nv.getNode().getNestedNetwork() == null) {
            JMenuItem jmi = new JMenuItem(MakeNestedNetwork.LABEL);
            jmi.addActionListener(new MakeNestedNetwork());
            spadeMenu.add(jmi);
        }

        if (nv.isSelected() && nv.getNode().getNestedNetwork() != null) {
            JMenuItem jmi = new JMenuItem(UndoNestedNetwork.LABEL);
            jmi.addActionListener(new UndoNestedNetwork());
            spadeMenu.add(jmi);
        }

        if (spadeMenu.getItemCount() > 0) {
            if (jpm == null) {
                jpm = new JPopupMenu();
            }
            jpm.add(spadeMenu);
        }
    }

}
