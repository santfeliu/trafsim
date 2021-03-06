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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author realor
 */
public class VehicleGroupDialog extends javax.swing.JDialog
{
  private int vehicleCount;
  private Movements movements;
  private boolean accepted = false;


  /**
   * Creates new form VehicleGroupDialog
   */
  public VehicleGroupDialog(java.awt.Frame parent, boolean modal)
  {
    super(parent, modal);
    initComponents();
  }

  public boolean showDialog()
  {
    formatMovements();
    countTextField.setText(String.valueOf(vehicleCount));

    pack();
    setLocationRelativeTo(getParent());
    setVisible(true);
    return accepted;
  }

  public void setCount(int count)
  {
    this.vehicleCount = count;
  }

  public int getCount()
  {
    return vehicleCount;
  }

  public void setGroup(String name)
  {
    groupTextField.setText(name);
  }

  public String getGroup()
  {
    return groupTextField.getText();
  }

  public void setMovements(Movements movements)
  {
    this.movements = movements;
  }

  public Movements getMovements()
  {
    return movements;
  }

  private void formatMovements()
  {
    if (movements != null)
    {
      StringBuilder buffer = new StringBuilder();
      List<String> locationNames = new ArrayList<String>(movements.keySet());
      Collections.sort(locationNames);
      for (String locationName : locationNames)
      {
        int journeyCount = movements.get(locationName);
        buffer.append(locationName).append("\t");
        buffer.append(journeyCount).append("\n");
      }
      movementsTextArea.setText(buffer.toString());
    }
  }

  private void parseMovements()
  {
    movements = new Movements();
    String  text = movementsTextArea.getText();
    try
    {
      BufferedReader reader = new BufferedReader(new StringReader(text));
      try
      {
        String line = reader.readLine();
        while (line != null)
        {
          StringTokenizer tokenizer = new StringTokenizer(line, " \t;");
          if (tokenizer.countTokens() >= 2)
          {
            try
            {
              String locationName = tokenizer.nextToken();
              String token = tokenizer.nextToken();
              int journeyCount = Integer.parseInt(token);
              if (journeyCount > 0)
              {
                movements.put(locationName, journeyCount);
              }
            }
            catch (NumberFormatException ex)
            {
              // igonre line
            }
          }
          line = reader.readLine();
        }
      }
      finally
      {
        reader.close();
      }
    }
    catch (IOException ex)
    {
      // ignore
    }
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
    java.awt.GridBagConstraints gridBagConstraints;

    centerPanel = new javax.swing.JPanel();
    countLabel = new javax.swing.JLabel();
    countTextField = new javax.swing.JTextField();
    groupLabel = new javax.swing.JLabel();
    groupTextField = new javax.swing.JTextField();
    movementsLabel = new javax.swing.JLabel();
    scrollPanel = new javax.swing.JScrollPane();
    movementsTextArea = new javax.swing.JTextArea();
    southPanel = new javax.swing.JPanel();
    okButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/santfeliu/trafsim/resources/TrafficSimulator"); // NOI18N
    setTitle(bundle.getString("dialog.vehicleGroup.title")); // NOI18N
    setPreferredSize(new java.awt.Dimension(300, 400));

    centerPanel.setPreferredSize(new java.awt.Dimension(200, 100));
    centerPanel.setLayout(new java.awt.GridBagLayout());

    countLabel.setText(bundle.getString("dialog.vehicleGroup.count")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    centerPanel.add(countLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    centerPanel.add(countTextField, gridBagConstraints);

    groupLabel.setText(bundle.getString("dialog.vehicleGroup.group")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    centerPanel.add(groupLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    centerPanel.add(groupTextField, gridBagConstraints);

    movementsLabel.setText(bundle.getString("dialog.vehicleGroup.movements")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    centerPanel.add(movementsLabel, gridBagConstraints);

    movementsTextArea.setColumns(20);
    movementsTextArea.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
    movementsTextArea.setRows(5);
    movementsTextArea.setTabSize(20);
    scrollPanel.setViewportView(movementsTextArea);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    centerPanel.add(scrollPanel, gridBagConstraints);

    getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

    okButton.setText(bundle.getString("dialog.ok")); // NOI18N
    okButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        okButtonActionPerformed(evt);
      }
    });
    southPanel.add(okButton);

    cancelButton.setText(bundle.getString("dialog.cancel")); // NOI18N
    cancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cancelButtonActionPerformed(evt);
      }
    });
    southPanel.add(cancelButton);

    getContentPane().add(southPanel, java.awt.BorderLayout.PAGE_END);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
  {//GEN-HEADEREND:event_okButtonActionPerformed
    try
    {
      vehicleCount = Integer.parseInt(countTextField.getText());
    }
    catch (NumberFormatException ex)
    {
      vehicleCount = 0;
    }
    parseMovements();

    int movementsVehicleCount = movements.getVehicleCount();
    if (movementsVehicleCount > vehicleCount)
    {
      vehicleCount = movementsVehicleCount;
    }
    dispose();
    accepted = true;
  }//GEN-LAST:event_okButtonActionPerformed

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
  {//GEN-HEADEREND:event_cancelButtonActionPerformed
    dispose();
  }//GEN-LAST:event_cancelButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton cancelButton;
  private javax.swing.JPanel centerPanel;
  private javax.swing.JLabel countLabel;
  private javax.swing.JTextField countTextField;
  private javax.swing.JLabel groupLabel;
  private javax.swing.JTextField groupTextField;
  private javax.swing.JLabel movementsLabel;
  private javax.swing.JTextArea movementsTextArea;
  private javax.swing.JButton okButton;
  private javax.swing.JScrollPane scrollPanel;
  private javax.swing.JPanel southPanel;
  // End of variables declaration//GEN-END:variables
}
