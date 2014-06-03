/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pvemu.mapeditor.handler.tool;

import org.pvemu.mapeditor.action.JMapEditor;
import org.pvemu.mapeditor.data.Cell;
import org.pvemu.mapeditor.handler.ToolsHandler;
import org.pvemu.mapeditor.ui.rightmenu.ObjectTab;

/**
 *
 * @author Vincent Quatrevieux <quatrevieux.vincent@gmail.com>
 */
public class SelectTool implements Tool{
    final private ToolsHandler handler;

    public SelectTool(ToolsHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onClick(Cell cell) {
        if(cell.getLayer1() == null)
            return;
        
        handler.setCurrentObject(cell.getLayer1());
        JMapEditor.getUI().getRightMenu().setObjectTab(new ObjectTab(cell.getLayer1()));
    }
    
}
