/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pvemu.mapeditor.handler;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.pvemu.mapeditor.action.JMapEditor;
import org.pvemu.mapeditor.common.Compressor;
import org.pvemu.mapeditor.common.Constants;
import org.pvemu.mapeditor.data.MapData;
import org.pvemu.mapeditor.data.db.model.MapHistory;
import org.pvemu.mapeditor.ui.editor.MapEditorUI;

/**
 *
 * @author Vincent Quatrevieux <quatrevieux.vincent@gmail.com>
 */
final public class EditorHandler {
    final private MapData map;
    final private MapEditorUI ui;
    private MapDBHandler data = null;
    private boolean changed = false;

    public EditorHandler(MapData map) {
        changed = true;
        this.map = map;
        ui = new MapEditorUI(this);
        ui.setTitle(getTitle());
        ui.setVisible(true);
    }
    
    public EditorHandler(MapData map, MapDBHandler data){
        this.map = map;
        this.data = data;
        ui = new MapEditorUI(this);
        ui.setTitle(getTitle());
        ui.setVisible(true);
    }

    public MapData getMap() {
        return map;
    }

    public boolean isChanged() {
        return changed;
    }
    
    public String getTitle(){
        return (data == null ? "Sans nom" : data.getFileName()) + " [id:" + map.getInfo().getId() + " date:" + (new SimpleDateFormat("d/M/y H:m:s")).format(new Date(map.getInfo().getLastDate())) + "]" + (changed ? " * " : "");
    }
    
    public void update(){
        changed = true;
        ui.getGrid().repaint();
        ui.setTitle(getTitle());
    }

    public MapEditorUI getUI() {
        return ui;
    }
    
    public void save() throws IOException, ParserConfigurationException, TransformerException, Exception{
        if(!changed)
            return;
        
        if(data == null){
            File file;
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(Constants.JME_EXT);
                }

                @Override
                public String getDescription() {
                    return "Fichier de sauvegarde JMapEditor";
                }
            });
            
            int r = fileChooser.showSaveDialog(ui);
            
            if(r == JFileChooser.CANCEL_OPTION){
                return;
            }
            
            if(r == JFileChooser.APPROVE_OPTION){
                file = fileChooser.getSelectedFile();
                
                if(file.getName().split("\\.").length < 2){
                    file = new File(file.getAbsolutePath() + Constants.JME_EXT);
                }
                
                if(file.isDirectory()){
                    throw new Exception(file.getName() + " is a folder !");
                }
                
                if(file.exists()){
                    r = JOptionPane.showConfirmDialog(JMapEditor.getUI(), "Ce fichier existe déjà. Voulez-vous l'écraser ?");
                    
                    if(r == JOptionPane.NO_OPTION){
                        save(); //show new filechooser
                        return;
                    }
                    
                    if(r == JOptionPane.CANCEL_OPTION){
                        return; //quit saving
                    }
                    
                    if(!file.delete())
                        throw new Exception("Cannot delete the file '" + file.getName() + "' !");
                }
                
                //creating db structure
                data = new MapDBHandler(file.getAbsolutePath());
                data.getInfoDAO().create(map.getInfo());
                data.getHistoryDAO().createTable();
            }
        }
        
        saveHistory();
        
        changed = false;
        ui.setTitle(getTitle());
    }
    
    private void saveHistory() throws SQLException{
        MapHistory history = new MapHistory(
                System.currentTimeMillis(), 
                map.getBackground() == null ? 0 : map.getBackground().getId(), 
                0, 
                Compressor.compressMapData(map)
        );
        
        map.getInfo().setLastDate(history.getDate());
        data.getHistoryDAO().add(history);
        data.getInfoDAO().update(map.getInfo());
    }
    
    static public EditorHandler getCurrentHandler(){
        JInternalFrame frame = JMapEditor.getUI().getDesktopPane().getSelectedFrame();
        
        if(frame == null || !(frame instanceof MapEditorUI)){
            return null;
        }
        
        return ((MapEditorUI)frame).getHandler();
    }
}
