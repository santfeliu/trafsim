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

/**
 *
 * @author realor
 */
public class EdgeDialog extends javax.swing.JDialog
{
  private boolean accepted = false;


  /**
   * Creates new form EdgeDialog
   */
  public EdgeDialog(java.awt.Frame parent, boolean modal)
  {
    super(parent, modal);
    initComponents();
  }

  public boolean showDialog()
  {
    pack();
    setLocationRelativeTo(getParent());
    setVisible(true);
    return accepted;
  }

  public void setSpeed(int speed)
  {
    speedTextField.setText(String.valueOf(speed));
  }

  public int getSpeed()
  {
    try
    {
      return (new Double(speedTextField.getText())).intValue();
    }
    catch (NumberFormatException ex)
    {
      return 0;
    }
  }

  public void setLanes(int lanes)
  {
    lanesTextField.setText(String.valueOf(lanes));
  }

  public int getLanes()
  {
    try
    {
      return Integer.parseInt(lanesTextField.getText());
    }
    catch (NumberFormatException ex)
    {
      return 1;
    }
  }

  public void setDelay(int delay)
  {
    delayTextField.setText(String.valueOf(delay));
  }

  public int getDelay()
  {
    try
    {
      return Integer.parseInt(delayTextField.getText());
    }
    catch (NumberFormatException ex)
    {
      return 0;
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
    speedLabel = new javax.swing.JLabel();
    speedTextField = new javax.swing.JTextField();
    lanesLabel = new javax.swing.JLabel();
    lanesTextField = new javax.swing.JTextField();
    delayLabel = new javax.swing.JLabel();
    delayTextField = new javax.swing.JTextField();
    southPanel = new javax.swing.JPanel();
    okButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/santfeliu/trafsim/resources/TrafficSimulator"); // NOI18N
    setTitle(bundle.getString("dialog.edge.title")); // NOI18N

    centerPanel.setPreferredSize(new java.awt.Dimension(200, 100));
    centerPanel.setLayout(new java.awt.GridBagLayout());

    speedLabel.setText(bundle.getString("dialog.edge.speed")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    centerPanel.add(speedLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    centerPanel.add(speedTextField, gridBagConstraints);

    lanesLabel.setText(bundle.getString("dialog.edge.lanes")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    centerPanel.add(lanesLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    centerPanel.add(lanesTextField, gridBagConstraints);

    delayLabel.setText(bundle.getString("dialog.edge.delay")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    centerPanel.add(delayLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    centerPanel.add(delayTextField, gridBagConstraints);

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
    accepted = true;
    dispose();
  }//GEN-LAST:event_okButtonActionPerformed

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
  {//GEN-HEADEREND:event_cancelButtonActionPerformed
    dispose();
  }//GEN-LAST:event_cancelButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton cancelButton;
  private javax.swing.JPanel centerPanel;
  private javax.swing.JLabel delayLabel;
  private javax.swing.JTextField delayTextField;
  private javax.swing.JLabel lanesLabel;
  private javax.swing.JTextField lanesTextField;
  private javax.swing.JButton okButton;
  private javax.swing.JPanel southPanel;
  private javax.swing.JLabel speedLabel;
  private javax.swing.JTextField speedTextField;
  // End of variables declaration//GEN-END:variables
}
