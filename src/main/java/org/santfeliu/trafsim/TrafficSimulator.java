/*
 * Traffic Simulator
 *
 * Copyright (C) 2018, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *   http://www.gnu.org/licenses/
 *   and
 *   https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.trafsim;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import org.santfeliu.trafsim.action.DeleteAction;
import org.santfeliu.trafsim.action.Tool;
import org.santfeliu.trafsim.action.DrawEdgeTool;
import org.santfeliu.trafsim.action.DrawLocationTool;
import org.santfeliu.trafsim.action.DrawVehicleGroupTool;
import org.santfeliu.trafsim.action.SelectTool;
import org.santfeliu.trafsim.io.SimulationReader;
import org.santfeliu.trafsim.io.SimulationWriter;
import org.santfeliu.trafsim.action.EditVerticesTool;
import org.santfeliu.trafsim.action.FindRouteTool;
import org.santfeliu.trafsim.action.MoveTool;
import org.santfeliu.trafsim.action.ReverseEdgesAction;
import org.santfeliu.trafsim.action.RouteVehiclesTool;

/**
 *
 * @author realor
 */
public class TrafficSimulator extends javax.swing.JFrame
{
  public static final String APP_NAME = "Traffic Simulator";
  public static final String APP_VERSION = "1.0";
  public static final String FILE_EXTENSION = ".tfs";
  private Simulation simulation;
  private File lastFile;
  private final ResourceBundle resourceBundle;
  private final ReverseEdgesAction reverseEdgesAction;
  private final DeleteAction deleteAction;
  private final SelectTool selectTool;
  private final MoveTool moveTool;
  private final EditVerticesTool editVerticesTool;
  private final DrawEdgeTool drawEdgeTool;
  private final DrawLocationTool drawLocationTool;
  private final DrawVehicleGroupTool drawVehicleGroupTool;
  private final FindRouteTool findRouteTool;
  private final RouteVehiclesTool routeVehiclesTool;
  private Tool currentTool;
  private boolean modified;
  private final SimulatorUndoManager undoManager;
  private Console console;

  /**
   * Creates new form TrafficSimulator
   */
  public TrafficSimulator()
  {
    resourceBundle = ResourceBundle.getBundle(
      "org/santfeliu/trafsim/resources/TrafficSimulator");
    reverseEdgesAction = new ReverseEdgesAction(this);
    deleteAction = new DeleteAction(this);
    selectTool = new SelectTool(this);
    moveTool = new MoveTool(this);
    editVerticesTool = new EditVerticesTool(this);
    drawEdgeTool = new DrawEdgeTool(this);
    drawLocationTool = new DrawLocationTool(this);
    drawVehicleGroupTool = new DrawVehicleGroupTool(this);
    findRouteTool = new FindRouteTool(this);
    routeVehiclesTool = new RouteVehiclesTool(this);
    initComponents();
    setFrameIcons();
    undoManager = new SimulatorUndoManager();
    undoManager.updateMenuItems();
    setSimulation(new Simulation());
    start(selectTool);
  }

  public final Simulation getSimulation()
  {
    return simulation;
  }

  public final void setSimulation(Simulation simulation)
  {
    if (simulation == null)
      throw new RuntimeException("Simulation can not be null");
    this.simulation = simulation;
    mapViewer.getSelection().clear();
  }

  public boolean isModified()
  {
    return modified;
  }

  public void setModified(boolean modified)
  {
    if (this.modified != modified)
    {
      this.modified = modified;
      updateTitle();
    }
  }

  public MapViewer getMapViewer()
  {
    return mapViewer;
  }

  public UndoManager getUndoManager()
  {
    return undoManager;
  }

  public String getMessage(String message)
  {
    String localizedMessage;
    try
    {
      localizedMessage = resourceBundle.getString(message);
    }
    catch (MissingResourceException ex)
    {
      localizedMessage = message;
    }
    return localizedMessage;
  }

  public void showError(Component component, String title, Exception ex)
  {
    JOptionPane.showMessageDialog(component, ex.toString(),
      title, JOptionPane.ERROR_MESSAGE);
  }

  public void start(Tool command)
  {
    if (currentTool != null)
    {
      currentTool.stop();
      toolInfoLabel.setText(null);
    }
    currentTool = command;
    currentTool.start();
    String key = currentTool.getName() + ".name";
    toolNameLabel.setText(getMessage(key) + ":");
  }

  public void info(String message)
  {
    toolInfoLabel.setText(getMessage(message));
  }

  private void updateTitle()
  {
    if (lastFile == null)
    {
      setTitle(APP_NAME);
    }
    else
    {
      String filename = lastFile.getName();
      if (modified) filename += "*";
      setTitle(filename + " - " + APP_NAME);
    }
  }

  private void exit()
  {
    if (modified && !confirmDiscardChanges("dialog.exit.title")) return;

    System.exit(0);
  }

  private boolean confirmDiscardChanges(String title)
  {
    int result = JOptionPane.showConfirmDialog(this,
      resourceBundle.getString("dialog.unsavedChanges"),
      resourceBundle.getString(title),
      JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    return result == JOptionPane.YES_OPTION;
  }

  public void setIndicatorsVisible(boolean visible)
  {
    indicatorsCheckBoxMenuItem.setSelected(visible);
    mapViewer.setIndicatorsVisible(indicatorsCheckBoxMenuItem.isSelected());
  }

  @Override
  public String toString()
  {
    return getClass().getName();
  }


  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    mapViewer = new org.santfeliu.trafsim.MapViewer();
    statusPanel = new javax.swing.JPanel();
    toolNameLabel = new javax.swing.JLabel();
    toolInfoLabel = new javax.swing.JLabel();
    menuBar = new javax.swing.JMenuBar();
    fileMenu = new javax.swing.JMenu();
    newMenuItem = new javax.swing.JMenuItem();
    openFileMenuItem = new javax.swing.JMenuItem();
    saveMenuItem = new javax.swing.JMenuItem();
    saveAsMenuItem = new javax.swing.JMenuItem();
    fileSeparator1 = new javax.swing.JPopupMenu.Separator();
    importMenuItem = new javax.swing.JMenuItem();
    exportMenuItem = new javax.swing.JMenuItem();
    fileSeparator2 = new javax.swing.JPopupMenu.Separator();
    exitMenuItem = new javax.swing.JMenuItem();
    editMenu = new javax.swing.JMenu();
    undoMenuItem = new javax.swing.JMenuItem();
    redoMenuItem = new javax.swing.JMenuItem();
    editSeparator1 = new javax.swing.JPopupMenu.Separator();
    deleteMenuItem = new javax.swing.JMenuItem();
    edgesMenu = new javax.swing.JMenu();
    reverseEdgeMenuItem = new javax.swing.JMenuItem();
    snapToGridMenuItem = new javax.swing.JMenuItem();
    editSeparator2 = new javax.swing.JPopupMenu.Separator();
    groupsMenuItem = new javax.swing.JMenuItem();
    simulationPropsMenuItem = new javax.swing.JMenuItem();
    viewMenu = new javax.swing.JMenu();
    zoomInMenuItem = new javax.swing.JMenuItem();
    zoomOutMenuItem = new javax.swing.JMenuItem();
    zoomAllMenuItem = new javax.swing.JMenuItem();
    viewSeparator1 = new javax.swing.JPopupMenu.Separator();
    edgesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    nodesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    locationsCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    vehiclesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    baseLayersCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    viewSeparator2 = new javax.swing.JPopupMenu.Separator();
    deadEndsCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    originsCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    indicatorsCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    congestionCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
    viewSeparator3 = new javax.swing.JPopupMenu.Separator();
    consoleMenuItem = new javax.swing.JMenuItem();
    toolsMenu = new javax.swing.JMenu();
    selectMenuItem = new javax.swing.JMenuItem();
    moveMenuItem = new javax.swing.JMenuItem();
    editVerticesMenuItem = new javax.swing.JMenuItem();
    toolsSeparator1 = new javax.swing.JPopupMenu.Separator();
    drawEdgeMenuItem = new javax.swing.JMenuItem();
    drawLocationMenuItem = new javax.swing.JMenuItem();
    drawVehicleGroupMenuItem = new javax.swing.JMenuItem();
    toolsSeparator2 = new javax.swing.JPopupMenu.Separator();
    findRouteMenuItem = new javax.swing.JMenuItem();
    routeVehiclesMenuItem = new javax.swing.JMenuItem();
    helpMenu = new javax.swing.JMenu();
    aboutMenuItem = new javax.swing.JMenuItem();

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    setTitle(APP_NAME);
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        TrafficSimulator.this.windowClosing(evt);
      }
    });

    mapViewer.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    mapViewer.setTrafficSimulator(this);
    getContentPane().add(mapViewer, java.awt.BorderLayout.CENTER);

    statusPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
    statusPanel.setLayout(new java.awt.BorderLayout());

    toolNameLabel.setFont(toolNameLabel.getFont().deriveFont(java.awt.Font.BOLD));
    toolNameLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 4));
    statusPanel.add(toolNameLabel, java.awt.BorderLayout.WEST);
    statusPanel.add(toolInfoLabel, java.awt.BorderLayout.CENTER);

    getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/santfeliu/trafsim/resources/TrafficSimulator"); // NOI18N
    fileMenu.setText(bundle.getString("menu.file")); // NOI18N

    newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
    newMenuItem.setText(bundle.getString("menu.new")); // NOI18N
    newMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        newMenuItemActionPerformed(evt);
      }
    });
    fileMenu.add(newMenuItem);

    openFileMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
    openFileMenuItem.setText(bundle.getString("menu.open")); // NOI18N
    openFileMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        openFileMenuItemActionPerformed(evt);
      }
    });
    fileMenu.add(openFileMenuItem);

    saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
    saveMenuItem.setText(bundle.getString("menu.save")); // NOI18N
    saveMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        saveMenuItemActionPerformed(evt);
      }
    });
    fileMenu.add(saveMenuItem);

    saveAsMenuItem.setText(bundle.getString("menu.saveAs")); // NOI18N
    saveAsMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        saveAsMenuItemActionPerformed(evt);
      }
    });
    fileMenu.add(saveAsMenuItem);
    fileMenu.add(fileSeparator1);

    importMenuItem.setText(bundle.getString("menu.import")); // NOI18N
    importMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        importMenuItemActionPerformed(evt);
      }
    });
    fileMenu.add(importMenuItem);

    exportMenuItem.setText(bundle.getString("menu.export")); // NOI18N
    exportMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        exportMenuItemActionPerformed(evt);
      }
    });
    fileMenu.add(exportMenuItem);
    fileMenu.add(fileSeparator2);

    exitMenuItem.setText(bundle.getString("menu.exit")); // NOI18N
    exitMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        exitMenuItemActionPerformed(evt);
      }
    });
    fileMenu.add(exitMenuItem);

    menuBar.add(fileMenu);

    editMenu.setText(bundle.getString("menu.edit")); // NOI18N

    undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
    undoMenuItem.setText("Undo");
    undoMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        undoMenuItemActionPerformed(evt);
      }
    });
    editMenu.add(undoMenuItem);

    redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
    redoMenuItem.setText("Redo");
    redoMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        redoMenuItemActionPerformed(evt);
      }
    });
    editMenu.add(redoMenuItem);
    editMenu.add(editSeparator1);

    deleteMenuItem.setAction(deleteAction);
    deleteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
    editMenu.add(deleteMenuItem);

    edgesMenu.setText(bundle.getString("menu.edges")); // NOI18N

    reverseEdgeMenuItem.setAction(reverseEdgesAction);
    reverseEdgeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
    edgesMenu.add(reverseEdgeMenuItem);

    snapToGridMenuItem.setText(bundle.getString("menu.snapToGrid")); // NOI18N
    snapToGridMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        snapToGridMenuItemActionPerformed(evt);
      }
    });
    edgesMenu.add(snapToGridMenuItem);

    editMenu.add(edgesMenu);
    editMenu.add(editSeparator2);

    groupsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
    groupsMenuItem.setText(bundle.getString("menu.groups")); // NOI18N
    groupsMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        groupsMenuItemActionPerformed(evt);
      }
    });
    editMenu.add(groupsMenuItem);

    simulationPropsMenuItem.setText(bundle.getString("menu.simulationProperties")); // NOI18N
    simulationPropsMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        simulationPropsMenuItemActionPerformed(evt);
      }
    });
    editMenu.add(simulationPropsMenuItem);

    menuBar.add(editMenu);

    viewMenu.setText(bundle.getString("menu.view")); // NOI18N

    zoomInMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, java.awt.event.InputEvent.CTRL_MASK));
    zoomInMenuItem.setText(bundle.getString("menu.zoomIn")); // NOI18N
    zoomInMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        zoomInMenuItemActionPerformed(evt);
      }
    });
    viewMenu.add(zoomInMenuItem);

    zoomOutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, java.awt.event.InputEvent.CTRL_MASK));
    zoomOutMenuItem.setText(bundle.getString("menu.zoomOut")); // NOI18N
    zoomOutMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        zoomOutMenuItemActionPerformed(evt);
      }
    });
    viewMenu.add(zoomOutMenuItem);

    zoomAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
    zoomAllMenuItem.setText(bundle.getString("menu.zoomAll")); // NOI18N
    zoomAllMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        zoomAllMenuItemActionPerformed(evt);
      }
    });
    viewMenu.add(zoomAllMenuItem);
    viewMenu.add(viewSeparator1);

    edgesCheckBoxMenuItem.setSelected(true);
    edgesCheckBoxMenuItem.setText(bundle.getString("menu.edgesVisible")); // NOI18N
    edgesCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        edgesCheckBoxMenuItemActionPerformed(evt);
      }
    });
    viewMenu.add(edgesCheckBoxMenuItem);

    nodesCheckBoxMenuItem.setSelected(true);
    nodesCheckBoxMenuItem.setText(bundle.getString("menu.nodesVisible")); // NOI18N
    nodesCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        nodesCheckBoxMenuItemActionPerformed(evt);
      }
    });
    viewMenu.add(nodesCheckBoxMenuItem);

    locationsCheckBoxMenuItem.setSelected(true);
    locationsCheckBoxMenuItem.setText(bundle.getString("menu.locationsVisible")); // NOI18N
    locationsCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        locationsCheckBoxMenuItemActionPerformed(evt);
      }
    });
    viewMenu.add(locationsCheckBoxMenuItem);

    vehiclesCheckBoxMenuItem.setSelected(true);
    vehiclesCheckBoxMenuItem.setText(bundle.getString("menu.vehiclesVisible")); // NOI18N
    vehiclesCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        vehiclesCheckBoxMenuItemActionPerformed(evt);
      }
    });
    viewMenu.add(vehiclesCheckBoxMenuItem);

    baseLayersCheckBoxMenuItem.setSelected(true);
    baseLayersCheckBoxMenuItem.setText(bundle.getString("menu.baseLayersVisible")); // NOI18N
    baseLayersCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        baseLayersCheckBoxMenuItemActionPerformed(evt);
      }
    });
    viewMenu.add(baseLayersCheckBoxMenuItem);
    viewMenu.add(viewSeparator2);

    deadEndsCheckBoxMenuItem.setText(bundle.getString("menu.deadEndsVisible")); // NOI18N
    deadEndsCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        deadEndsCheckBoxMenuItemActionPerformed(evt);
      }
    });
    viewMenu.add(deadEndsCheckBoxMenuItem);

    originsCheckBoxMenuItem.setText(bundle.getString("menu.originsVisible")); // NOI18N
    originsCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        originsCheckBoxMenuItemActionPerformed(evt);
      }
    });
    viewMenu.add(originsCheckBoxMenuItem);

    indicatorsCheckBoxMenuItem.setText(bundle.getString("menu.indicatorsVisible")); // NOI18N
    indicatorsCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        indicatorsCheckBoxMenuItemActionPerformed(evt);
      }
    });
    viewMenu.add(indicatorsCheckBoxMenuItem);

    congestionCheckBoxMenuItem.setText(bundle.getString("menu.congestionVisible")); // NOI18N
    congestionCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        congestionCheckBoxMenuItemActionPerformed(evt);
      }
    });
    viewMenu.add(congestionCheckBoxMenuItem);
    viewMenu.add(viewSeparator3);

    consoleMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, 0));
    consoleMenuItem.setText(bundle.getString("menu.console")); // NOI18N
    consoleMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        consoleMenuItemActionPerformed(evt);
      }
    });
    viewMenu.add(consoleMenuItem);

    menuBar.add(viewMenu);

    toolsMenu.setText(bundle.getString("menu.tools")); // NOI18N

    selectMenuItem.setAction(selectTool);
    selectMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, 0));
    toolsMenu.add(selectMenuItem);

    moveMenuItem.setAction(moveTool);
    moveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, 0));
    toolsMenu.add(moveMenuItem);

    editVerticesMenuItem.setAction(editVerticesTool);
    editVerticesMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, 0));
    toolsMenu.add(editVerticesMenuItem);
    toolsMenu.add(toolsSeparator1);

    drawEdgeMenuItem.setAction(drawEdgeTool);
    drawEdgeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, 0));
    toolsMenu.add(drawEdgeMenuItem);

    drawLocationMenuItem.setAction(drawLocationTool);
    drawLocationMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, 0));
    toolsMenu.add(drawLocationMenuItem);

    drawVehicleGroupMenuItem.setAction(drawVehicleGroupTool);
    drawVehicleGroupMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, 0));
    toolsMenu.add(drawVehicleGroupMenuItem);
    toolsMenu.add(toolsSeparator2);

    findRouteMenuItem.setAction(findRouteTool);
    findRouteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, 0));
    toolsMenu.add(findRouteMenuItem);

    routeVehiclesMenuItem.setAction(routeVehiclesTool);
    routeVehiclesMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, 0));
    toolsMenu.add(routeVehiclesMenuItem);

    menuBar.add(toolsMenu);

    helpMenu.setText(bundle.getString("menu.help")); // NOI18N

    aboutMenuItem.setText(bundle.getString("menu.about")); // NOI18N
    aboutMenuItem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        aboutMenuItemActionPerformed(evt);
      }
    });
    helpMenu.add(aboutMenuItem);

    menuBar.add(helpMenu);

    setJMenuBar(menuBar);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_aboutMenuItemActionPerformed
  {//GEN-HEADEREND:event_aboutMenuItemActionPerformed
    JOptionPane.showMessageDialog(this,
      APP_NAME + " " +  APP_VERSION +
      "\nDepartament de Sistemes d'Informació (informatica@santfeliu.cat)\n" +
      "Copyright (C) 2019, Ajuntament de Sant Feliu de Llobregat",
      resourceBundle.getString("dialog.about.title"),
      JOptionPane.INFORMATION_MESSAGE);
  }//GEN-LAST:event_aboutMenuItemActionPerformed

  private void openFileMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openFileMenuItemActionPerformed
  {//GEN-HEADEREND:event_openFileMenuItemActionPerformed
    if (modified && !confirmDiscardChanges("dialog.open.open")) return;

    try
    {
      JFileChooser fileChooser = new JFileChooser();
      File homeDir = new File(System.getProperty("user.home"));
      fileChooser.setCurrentDirectory(homeDir);
      fileChooser.setFileFilter(new SimulationFileFilter());
      int result = fileChooser.showDialog(this,
        resourceBundle.getString("dialog.open.open"));
      if (result == JFileChooser.APPROVE_OPTION)
      {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        File file = fileChooser.getSelectedFile();
        FileInputStream fis = new FileInputStream(file);
        SimulationReader reader = new SimulationReader(fis);
        Simulation sim = reader.read();
        lastFile = file;
        setSimulation(sim);
        mapViewer.zoomAll();
        mapViewer.repaint();
        setCursor(Cursor.getDefaultCursor());
        start(selectTool);
        setModified(false);
        undoManager.discardAllEdits();
      }
    }
    catch (Exception ex)
    {
      setCursor(Cursor.getDefaultCursor());
      showError(this, resourceBundle.getString("dialog.open.title"), ex);
    }
  }//GEN-LAST:event_openFileMenuItemActionPerformed

  private void zoomAllMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_zoomAllMenuItemActionPerformed
  {//GEN-HEADEREND:event_zoomAllMenuItemActionPerformed
    mapViewer.zoomAll();
  }//GEN-LAST:event_zoomAllMenuItemActionPerformed

  private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitMenuItemActionPerformed
  {//GEN-HEADEREND:event_exitMenuItemActionPerformed
    exit();
  }//GEN-LAST:event_exitMenuItemActionPerformed

  private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveAsMenuItemActionPerformed
  {//GEN-HEADEREND:event_saveAsMenuItemActionPerformed
    try
    {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileFilter(new SimulationFileFilter());
      if (lastFile == null)
      {
        File homeDir = new File(System.getProperty("user.home"));
        fileChooser.setCurrentDirectory(homeDir);
        fileChooser.setSelectedFile(
          new File(homeDir, "simulation" + FILE_EXTENSION));
      }
      else
      {
        fileChooser.setSelectedFile(lastFile);
      }
      int result = fileChooser.showDialog(this,
        resourceBundle.getString("dialog.save.save"));
      if (result != JFileChooser.APPROVE_OPTION) return;

      File file = fileChooser.getSelectedFile();
      if (!file.getName().endsWith(FILE_EXTENSION))
      {
        file = new File(file.getName() + FILE_EXTENSION);
      }
      if (file.exists())
      {
        result = JOptionPane.showConfirmDialog(this,
          resourceBundle.getString("dialog.save.overwrite"),
          resourceBundle.getString("dialog.save.title"),
          JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.NO_OPTION) return;
      }

      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      FileOutputStream os = new FileOutputStream(file);
      SimulationWriter writer = new SimulationWriter(os);
      writer.write(simulation);
      setCursor(Cursor.getDefaultCursor());
      lastFile = file;
      setModified(false);
    }
    catch (Exception ex)
    {
      setCursor(Cursor.getDefaultCursor());
      showError(this, resourceBundle.getString("dialog.save.title"), ex);
    }
  }//GEN-LAST:event_saveAsMenuItemActionPerformed

  private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newMenuItemActionPerformed
  {//GEN-HEADEREND:event_newMenuItemActionPerformed
    if (modified && !confirmDiscardChanges("dialog.new.title")) return;

    setSimulation(new Simulation());
    mapViewer.repaint();
    lastFile = null;
    start(selectTool);
    setModified(false);
    undoManager.discardAllEdits();
  }//GEN-LAST:event_newMenuItemActionPerformed

  private void importMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_importMenuItemActionPerformed
  {//GEN-HEADEREND:event_importMenuItemActionPerformed
    ImportDialog dialog = new ImportDialog(this, true);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }//GEN-LAST:event_importMenuItemActionPerformed

  private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveMenuItemActionPerformed
  {//GEN-HEADEREND:event_saveMenuItemActionPerformed
    if (lastFile == null)
    {
      saveAsMenuItemActionPerformed(evt);
    }
    else
    {
      try
      {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        FileOutputStream fos = new FileOutputStream(lastFile);
        SimulationWriter writer = new SimulationWriter(fos);
        writer.write(simulation);
        setCursor(Cursor.getDefaultCursor());
        setModified(false);
      }
      catch (IOException ex)
      {
        setCursor(Cursor.getDefaultCursor());
        showError(this, resourceBundle.getString("dialog.save.title"), ex);
      }
    }
  }//GEN-LAST:event_saveMenuItemActionPerformed

  private void edgesCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_edgesCheckBoxMenuItemActionPerformed
  {//GEN-HEADEREND:event_edgesCheckBoxMenuItemActionPerformed
    mapViewer.setEdgesVisible(edgesCheckBoxMenuItem.isSelected());
  }//GEN-LAST:event_edgesCheckBoxMenuItemActionPerformed

  private void baseLayersCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_baseLayersCheckBoxMenuItemActionPerformed
  {//GEN-HEADEREND:event_baseLayersCheckBoxMenuItemActionPerformed
    mapViewer.setBaseLayersVisible(baseLayersCheckBoxMenuItem.isSelected());
  }//GEN-LAST:event_baseLayersCheckBoxMenuItemActionPerformed

  private void vehiclesCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_vehiclesCheckBoxMenuItemActionPerformed
  {//GEN-HEADEREND:event_vehiclesCheckBoxMenuItemActionPerformed
    mapViewer.setVehiclesVisible(vehiclesCheckBoxMenuItem.isSelected());
  }//GEN-LAST:event_vehiclesCheckBoxMenuItemActionPerformed

  private void locationsCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_locationsCheckBoxMenuItemActionPerformed
  {//GEN-HEADEREND:event_locationsCheckBoxMenuItemActionPerformed
    mapViewer.setLocationsVisible(locationsCheckBoxMenuItem.isSelected());
  }//GEN-LAST:event_locationsCheckBoxMenuItemActionPerformed

  private void nodesCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nodesCheckBoxMenuItemActionPerformed
  {//GEN-HEADEREND:event_nodesCheckBoxMenuItemActionPerformed
    mapViewer.setNodesVisible(nodesCheckBoxMenuItem.isSelected());
  }//GEN-LAST:event_nodesCheckBoxMenuItemActionPerformed

  private void deadEndsCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deadEndsCheckBoxMenuItemActionPerformed
  {//GEN-HEADEREND:event_deadEndsCheckBoxMenuItemActionPerformed
    mapViewer.setDeadEndsVisible(deadEndsCheckBoxMenuItem.isSelected());
  }//GEN-LAST:event_deadEndsCheckBoxMenuItemActionPerformed

  private void zoomInMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_zoomInMenuItemActionPerformed
  {//GEN-HEADEREND:event_zoomInMenuItemActionPerformed
    mapViewer.zoomFactor(0.5);
  }//GEN-LAST:event_zoomInMenuItemActionPerformed

  private void zoomOutMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_zoomOutMenuItemActionPerformed
  {//GEN-HEADEREND:event_zoomOutMenuItemActionPerformed
    mapViewer.zoomFactor(2);
  }//GEN-LAST:event_zoomOutMenuItemActionPerformed

  private void originsCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_originsCheckBoxMenuItemActionPerformed
  {//GEN-HEADEREND:event_originsCheckBoxMenuItemActionPerformed
    mapViewer.setOriginsVisible(originsCheckBoxMenuItem.isSelected());
  }//GEN-LAST:event_originsCheckBoxMenuItemActionPerformed

  private void groupsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_groupsMenuItemActionPerformed
  {//GEN-HEADEREND:event_groupsMenuItemActionPerformed
    GroupsDialog dialog = new GroupsDialog(this, true);
    dialog.setGroups(simulation.getGroups());
    if (dialog.showDialog())
    {
      Map<String, Group> groups = dialog.getGroups();
      simulation.getGroups().clear();
      simulation.getGroups().putAll(groups);

      Distributor distributor = new Distributor();
      Vehicles vehicles = simulation.getVehicles();
      for (Vehicles.VehicleGroup vehicleGroup : vehicles.getFeatures())
      {
        int vehicleCount = vehicleGroup.getCount();
        String groupName = vehicleGroup.getGroup();
        Group group = groups.get(groupName);
        if (group != null && vehicleCount > 0)
        {
          Movements movements = distributor.getMovements(vehicleCount, group);
          vehicleGroup.setMovements(movements);
        }
      }
      setModified(true);
    }
  }//GEN-LAST:event_groupsMenuItemActionPerformed

  private void indicatorsCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_indicatorsCheckBoxMenuItemActionPerformed
  {//GEN-HEADEREND:event_indicatorsCheckBoxMenuItemActionPerformed
    mapViewer.setIndicatorsVisible(indicatorsCheckBoxMenuItem.isSelected());
  }//GEN-LAST:event_indicatorsCheckBoxMenuItemActionPerformed

  private void exportMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exportMenuItemActionPerformed
  {//GEN-HEADEREND:event_exportMenuItemActionPerformed
    ExportDialog dialog = new ExportDialog(this, true);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }//GEN-LAST:event_exportMenuItemActionPerformed

  private void simulationPropsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_simulationPropsMenuItemActionPerformed
  {//GEN-HEADEREND:event_simulationPropsMenuItemActionPerformed
    PropertiesDialog dialog = new PropertiesDialog(this, true);
    dialog.setSimulationTitle(simulation.getTitle());
    dialog.setSrsName(simulation.getSrsName());
    dialog.setDuration(simulation.getDuration());
    dialog.setLocationRelativeTo(this);
    if (dialog.showDialog())
    {
      simulation.setTitle(dialog.getSimulationTitle());
      simulation.setSrsName(dialog.getSrsName());
      simulation.setDuration(dialog.getDuration());
      mapViewer.repaint();
      setModified(true);
    }
  }//GEN-LAST:event_simulationPropsMenuItemActionPerformed

  private void windowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_windowClosing
  {//GEN-HEADEREND:event_windowClosing
    exit();
  }//GEN-LAST:event_windowClosing

  private void snapToGridMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_snapToGridMenuItemActionPerformed
  {//GEN-HEADEREND:event_snapToGridMenuItemActionPerformed
    String title = getMessage("dialog.snapToGrid.title");
    String gridSizeLabel = getMessage("dialog.snapToGrid.gridSize");
    Object value = JOptionPane.showInputDialog(this, gridSizeLabel, title,
      JOptionPane.QUESTION_MESSAGE, null, null, "0.1");
    try
    {
      if (value instanceof String)
      {
        Double gridSize = Double.parseDouble((String)value);
        simulation.getRoadGraph().snapToGrid(gridSize);
        mapViewer.repaint();
      }
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, ex.toString(), title,
        JOptionPane.ERROR_MESSAGE);
    }
  }//GEN-LAST:event_snapToGridMenuItemActionPerformed

  private void undoMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_undoMenuItemActionPerformed
  {//GEN-HEADEREND:event_undoMenuItemActionPerformed
    if (undoManager.canUndo())
    {
      undoManager.undo();
    }
  }//GEN-LAST:event_undoMenuItemActionPerformed

  private void redoMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_redoMenuItemActionPerformed
  {//GEN-HEADEREND:event_redoMenuItemActionPerformed
    if (undoManager.canRedo())
    {
      undoManager.redo();
    }
  }//GEN-LAST:event_redoMenuItemActionPerformed

  private void consoleMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_consoleMenuItemActionPerformed
  {//GEN-HEADEREND:event_consoleMenuItemActionPerformed
    if (console == null)
    {
      console = new Console(this);
      console.setSize(500, 400);
      console.setLocationRelativeTo(this);
    }
    console.setVisible(true);
  }//GEN-LAST:event_consoleMenuItemActionPerformed

  private void congestionCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_congestionCheckBoxMenuItemActionPerformed
  {//GEN-HEADEREND:event_congestionCheckBoxMenuItemActionPerformed
    mapViewer.setCongestionVisible(congestionCheckBoxMenuItem.isSelected());
  }//GEN-LAST:event_congestionCheckBoxMenuItemActionPerformed

  public class SimulatorUndoManager extends UndoManager
  {
    @Override
    public synchronized boolean addEdit(UndoableEdit anEdit)
    {
      boolean added = super.addEdit(anEdit);
      updateMenuItems();
      return added;
    }

    @Override
    public synchronized void redo() throws CannotRedoException
    {
      super.redo();
      updateMenuItems();
    }

    @Override
    public synchronized void undo() throws CannotUndoException
    {
      super.undo();
      updateMenuItems();
    }

    @Override
    public synchronized void discardAllEdits()
    {
      super.discardAllEdits();
      updateMenuItems();
    }

    protected void updateMenuItems()
    {
      undoMenuItem.setEnabled(canUndo());
      redoMenuItem.setEnabled(canRedo());

      undoMenuItem.setText(canUndo() ?
        getUndoPresentationName() : getMessage("menu.undo"));

      redoMenuItem.setText(canRedo() ?
        getRedoPresentationName() : getMessage("menu.redo"));
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
    /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
     */
    try
    {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
      {
        if ("Nimbus".equals(info.getName()))
        {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
      if (System.getProperty("os.name").toLowerCase().contains("windows"))
      {
        UIDefaults uiDefaults = UIManager.getLookAndFeel().getDefaults();
        uiDefaults.put("defaultFont", new Font("Segoe UI", 0, 12));
      }
    }
    catch (Exception ex)
    {
      // ignore
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        TrafficSimulator simulator = new TrafficSimulator();
        simulator.setSize(800, 600);
        simulator.setLocationRelativeTo(null);
        simulator.setVisible(true);
      }
    });
  }

  private void setFrameIcons()
  {
    try
    {
      if (System.getProperty("os.name").contains("Mac"))
      {
        ImageIcon icon = new ImageIcon(getClass().getResource(
          "/org/santfeliu/trafsim/resources/icons/logo_64.png"));
        setMacOSIcon(icon);
      }
      else
      {
        ArrayList<Image> icons = new ArrayList<Image>();
        int logoSizes[] = new int[]{16, 32, 64};
        for (int logoSize : logoSizes)
        {
          ImageIcon icon = new ImageIcon(getClass().getResource(
            "/org/santfeliu/trafsim/resources/icons/logo_" +
            logoSize + ".png"));
          icons.add(icon.getImage());
        }
        setIconImages(icons);
      }
    }
    catch (Exception ex)
    {
      // ignore
    }
  }

  private void setMacOSIcon(ImageIcon icon)
  {
    try
    {
      Class cls = Class.forName("com.apple.eawt.Application");
      Method method = cls.getMethod("getApplication", new Class[0]);
      Object application = method.invoke(null, new Object[0]);
      method = cls.getMethod("setDockIconImage", new Class[]{Image.class});
      method.invoke(application, new Object[]{icon.getImage()});
    }
    catch (Exception ex)
    {
      // ignore
    }
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenuItem aboutMenuItem;
  private javax.swing.JCheckBoxMenuItem baseLayersCheckBoxMenuItem;
  private javax.swing.JCheckBoxMenuItem congestionCheckBoxMenuItem;
  private javax.swing.JMenuItem consoleMenuItem;
  private javax.swing.JCheckBoxMenuItem deadEndsCheckBoxMenuItem;
  private javax.swing.JMenuItem deleteMenuItem;
  private javax.swing.JMenuItem drawEdgeMenuItem;
  private javax.swing.JMenuItem drawLocationMenuItem;
  private javax.swing.JMenuItem drawVehicleGroupMenuItem;
  private javax.swing.JCheckBoxMenuItem edgesCheckBoxMenuItem;
  private javax.swing.JMenu edgesMenu;
  private javax.swing.JMenu editMenu;
  private javax.swing.JPopupMenu.Separator editSeparator1;
  private javax.swing.JPopupMenu.Separator editSeparator2;
  private javax.swing.JMenuItem editVerticesMenuItem;
  private javax.swing.JMenuItem exitMenuItem;
  private javax.swing.JMenuItem exportMenuItem;
  private javax.swing.JMenu fileMenu;
  private javax.swing.JPopupMenu.Separator fileSeparator1;
  private javax.swing.JPopupMenu.Separator fileSeparator2;
  private javax.swing.JMenuItem findRouteMenuItem;
  private javax.swing.JMenuItem groupsMenuItem;
  private javax.swing.JMenu helpMenu;
  private javax.swing.JMenuItem importMenuItem;
  private javax.swing.JCheckBoxMenuItem indicatorsCheckBoxMenuItem;
  private javax.swing.JCheckBoxMenuItem locationsCheckBoxMenuItem;
  private org.santfeliu.trafsim.MapViewer mapViewer;
  private javax.swing.JMenuBar menuBar;
  private javax.swing.JMenuItem moveMenuItem;
  private javax.swing.JMenuItem newMenuItem;
  private javax.swing.JCheckBoxMenuItem nodesCheckBoxMenuItem;
  private javax.swing.JMenuItem openFileMenuItem;
  private javax.swing.JCheckBoxMenuItem originsCheckBoxMenuItem;
  private javax.swing.JMenuItem redoMenuItem;
  private javax.swing.JMenuItem reverseEdgeMenuItem;
  private javax.swing.JMenuItem routeVehiclesMenuItem;
  private javax.swing.JMenuItem saveAsMenuItem;
  private javax.swing.JMenuItem saveMenuItem;
  private javax.swing.JMenuItem selectMenuItem;
  private javax.swing.JMenuItem simulationPropsMenuItem;
  private javax.swing.JMenuItem snapToGridMenuItem;
  private javax.swing.JPanel statusPanel;
  private javax.swing.JLabel toolInfoLabel;
  private javax.swing.JLabel toolNameLabel;
  private javax.swing.JMenu toolsMenu;
  private javax.swing.JPopupMenu.Separator toolsSeparator1;
  private javax.swing.JPopupMenu.Separator toolsSeparator2;
  private javax.swing.JMenuItem undoMenuItem;
  private javax.swing.JCheckBoxMenuItem vehiclesCheckBoxMenuItem;
  private javax.swing.JMenu viewMenu;
  private javax.swing.JPopupMenu.Separator viewSeparator1;
  private javax.swing.JPopupMenu.Separator viewSeparator2;
  private javax.swing.JPopupMenu.Separator viewSeparator3;
  private javax.swing.JMenuItem zoomAllMenuItem;
  private javax.swing.JMenuItem zoomInMenuItem;
  private javax.swing.JMenuItem zoomOutMenuItem;
  // End of variables declaration//GEN-END:variables
}
