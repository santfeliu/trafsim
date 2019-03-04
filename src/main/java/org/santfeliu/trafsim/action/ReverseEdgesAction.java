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
package org.santfeliu.trafsim.action;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.santfeliu.trafsim.MapViewer;
import org.santfeliu.trafsim.RoadGraph.Edge;
import org.santfeliu.trafsim.TrafficSimulator;

/**
 *
 * @author realor
 */
public class ReverseEdgesAction extends SimulatorAction
{
  public ReverseEdgesAction(TrafficSimulator trafficSimulator)
  {
    super(trafficSimulator);
  }

  @Override
  public String getName()
  {
    return "reverseEdgesAction";
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    List<Edge> edges = getSelectedEdges();
    if (edges.isEmpty()) return;

    reverseEdges(edges);

    UndoManager undoManager = getUndoManager();
    undoManager.addEdit(new Undo(edges));
  }

  private void reverseEdges(List<Edge> edges)
  {
    for (Edge edge : edges)
    {
      edge.reverse();
    }
    MapViewer mapViewer = getMapViewer();
    mapViewer.repaint();
    trafficSimulator.setModified(true);
  }

  public class Undo extends BasicUndoableEdit
  {
    private final List<Edge> edges;

    private Undo(List<Edge> edges)
    {
      this.edges = edges;
    }

    @Override
    public void undo() throws CannotUndoException
    {
      reverseEdges(edges);
    }

    @Override
    public void redo() throws CannotRedoException
    {
      reverseEdges(edges);
    }

    @Override
    public void die()
    {
      edges.clear();
    }
  }
}
