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

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author realor
 */
public class Console extends javax.swing.JFrame
{
  private final TrafficSimulator trafficSimulator;

  /**
   * Creates new form Console
   */
  public Console(TrafficSimulator trafficSimulator)
  {
    initComponents();
    this.trafficSimulator = trafficSimulator;
    this.setTitle(trafficSimulator.getMessage("dialog.console.title"));
    inputTextArea.putClientProperty("caretWidth", 2);
    initTemplates();
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

    northPanel = new javax.swing.JPanel();
    leftPanel = new javax.swing.JPanel();
    runButton = new javax.swing.JButton();
    clearButton = new javax.swing.JButton();
    rightPanel = new javax.swing.JPanel();
    templateLabel = new javax.swing.JLabel();
    templateComboBox = new javax.swing.JComboBox<>();
    loadButton = new javax.swing.JButton();
    splitPane = new javax.swing.JSplitPane();
    inputScrollPane = new javax.swing.JScrollPane();
    inputTextArea = new javax.swing.JTextArea();
    outputScrollPane = new javax.swing.JScrollPane();
    outputTextArea = new javax.swing.JTextArea();

    setTitle("Console");

    northPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
    northPanel.setLayout(new java.awt.BorderLayout());

    leftPanel.setLayout(new java.awt.BorderLayout());

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/santfeliu/trafsim/resources/TrafficSimulator"); // NOI18N
    runButton.setText(bundle.getString("dialog.console.run")); // NOI18N
    runButton.setFocusable(false);
    runButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    runButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    runButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        runButtonActionPerformed(evt);
      }
    });
    leftPanel.add(runButton, java.awt.BorderLayout.WEST);

    clearButton.setText(bundle.getString("dialog.console.clear")); // NOI18N
    clearButton.setFocusable(false);
    clearButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    clearButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    clearButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        clearButtonActionPerformed(evt);
      }
    });
    leftPanel.add(clearButton, java.awt.BorderLayout.EAST);

    northPanel.add(leftPanel, java.awt.BorderLayout.WEST);

    rightPanel.setLayout(new java.awt.BorderLayout());

    templateLabel.setText(bundle.getString("dialog.console.template")); // NOI18N
    templateLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 1));
    rightPanel.add(templateLabel, java.awt.BorderLayout.WEST);
    rightPanel.add(templateComboBox, java.awt.BorderLayout.CENTER);

    loadButton.setText(bundle.getString("dialog.console.load")); // NOI18N
    loadButton.setFocusable(false);
    loadButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    loadButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    loadButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        loadButtonActionPerformed(evt);
      }
    });
    rightPanel.add(loadButton, java.awt.BorderLayout.EAST);

    northPanel.add(rightPanel, java.awt.BorderLayout.CENTER);

    getContentPane().add(northPanel, java.awt.BorderLayout.NORTH);

    splitPane.setDividerLocation(200);
    splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
    splitPane.setResizeWeight(0.5);

    inputTextArea.setColumns(20);
    inputTextArea.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
    inputTextArea.setRows(5);
    inputScrollPane.setViewportView(inputTextArea);

    splitPane.setLeftComponent(inputScrollPane);

    outputTextArea.setEditable(false);
    outputTextArea.setColumns(20);
    outputTextArea.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
    outputTextArea.setRows(5);
    outputScrollPane.setViewportView(outputTextArea);

    splitPane.setRightComponent(outputScrollPane);

    getContentPane().add(splitPane, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void clearButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_clearButtonActionPerformed
  {//GEN-HEADEREND:event_clearButtonActionPerformed
    inputTextArea.setText("");
    outputTextArea.setText("");
  }//GEN-LAST:event_clearButtonActionPerformed

  private void runButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_runButtonActionPerformed
  {//GEN-HEADEREND:event_runButtonActionPerformed
    try
    {
      String code = inputTextArea.getSelectedText();
      if (code == null) code = inputTextArea.getText();
      ScriptEngineManager manager = new ScriptEngineManager();
      ScriptEngine engine = manager.getEngineByName("nashorn");
      if (engine == null)
        throw new RuntimeException("scripting is not available");
      engine.put("trafsim", trafficSimulator);
      engine.put("mapViewer", trafficSimulator.getMapViewer());
      engine.put("selection", trafficSimulator.getMapViewer().getSelection());
      engine.put("simulation", trafficSimulator.getSimulation());
      Object result = engine.eval(code);
      outputTextArea.setForeground(Color.DARK_GRAY);
      outputTextArea.setText(String.valueOf(result));
      outputTextArea.setCaretPosition(0);
      trafficSimulator.getMapViewer().repaint();
    }
    catch (Exception ex)
    {
      outputTextArea.setForeground(Color.RED);
      outputTextArea.setText(ex.toString());
      outputTextArea.setCaretPosition(0);
    }
  }//GEN-LAST:event_runButtonActionPerformed

  private void loadButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_loadButtonActionPerformed
  {//GEN-HEADEREND:event_loadButtonActionPerformed
    int index = templateComboBox.getSelectedIndex();
    String file = TEMPLATES[index][1];
    InputStream is = getClass().getResourceAsStream(
      "/org/santfeliu/trafsim/resources/templates/" + file);
    try
    {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      StringBuilder buffer = new StringBuilder();
      String line = reader.readLine();
      while (line != null)
      {
        buffer.append(line).append('\n');
        line = reader.readLine();
      }
      inputTextArea.setText(buffer.toString());
      inputTextArea.setCaretPosition(0);
    }
    catch (IOException ex)
    {
      // ignore
    }
  }//GEN-LAST:event_loadButtonActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton clearButton;
  private javax.swing.JScrollPane inputScrollPane;
  private javax.swing.JTextArea inputTextArea;
  private javax.swing.JPanel leftPanel;
  private javax.swing.JButton loadButton;
  private javax.swing.JPanel northPanel;
  private javax.swing.JScrollPane outputScrollPane;
  private javax.swing.JTextArea outputTextArea;
  private javax.swing.JPanel rightPanel;
  private javax.swing.JButton runButton;
  private javax.swing.JSplitPane splitPane;
  private javax.swing.JComboBox<String> templateComboBox;
  private javax.swing.JLabel templateLabel;
  // End of variables declaration//GEN-END:variables

  private static final String[][] TEMPLATES = new String[][]
  {
    {"Process selection", "process_selection.js"},
    {"Process edges", "process_edges.js"},
    {"Process locations", "process_locations.js"},
    {"Process vehicle groups", "process_vehicles.js"},
    {"Select edges by speed", "edges_by_speed.js"},
    {"Total edge distance", "total_edge_distance.js"},
  };

  private void initTemplates()
  {
    DefaultComboBoxModel model = new javax.swing.DefaultComboBoxModel();
    for (String[] template : TEMPLATES)
    {
      model.addElement(template[0]);
    }
    templateComboBox.setModel(model);
  }
}
