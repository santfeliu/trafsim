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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.EdgeDialog;
import org.santfeliu.trafsim.MapViewer;
import org.santfeliu.trafsim.MapViewer.Painter;
import org.santfeliu.trafsim.Projector;
import org.santfeliu.trafsim.RoadGraph;
import org.santfeliu.trafsim.RoadGraph.Edge;
import org.santfeliu.trafsim.TrafficSimulator;
import org.santfeliu.trafsim.geom.LineString;

/**
 *
 * @author realor
 */
public class DrawEdgeTool extends Tool
  implements MouseListener, MouseMotionListener, Painter
{
  private ArrayList<Point3d> vertices;
  private boolean onNode;

  public DrawEdgeTool(TrafficSimulator trafficSimulator)
  {
    super(trafficSimulator);
  }

  @Override
  public String getName()
  {
    return "drawEdgeTool";
  }

  @Override
  public void start()
  {
    vertices = new ArrayList<Point3d>();
    vertices.add(new Point3d());

    MapViewer mapViewer = getMapViewer();
    mapViewer.addMouseListener(this);
    mapViewer.addMouseMotionListener(this);
    mapViewer.setPainter(this);
    mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    info("info");
  }

  @Override
  public void stop()
  {
    MapViewer mapViewer = getMapViewer();
    mapViewer.removeMouseListener(this);
    mapViewer.removeMouseMotionListener(this);
    mapViewer.setPainter(null);
    mapViewer.setCursor(Cursor.getDefaultCursor());
  }

  @Override
  public void mouseClicked(MouseEvent e)
  {
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    MapViewer mapViewer = getMapViewer();

    if (e.getButton() == MouseEvent.BUTTON1)
    {
      if (e.getClickCount() == 1 && (!onNode || vertices.size() == 1))
      {
        // add new vertex
        Point3d world = new Point3d();
        onNode = snapNode(e.getPoint(), world);
        vertices.add(world);
      }
      else
      {
        // close by doble-click or onNode
        if (e.getClickCount() > 1)
        {
          vertices.remove(vertices.size() - 1);
        }
        if (vertices.size() > 1)
        {
          EdgeDialog dialog = new EdgeDialog(null, true);
          dialog.setSpeed(50);
          dialog.setLanes(1);
          dialog.setDelay(0);
          if (dialog.showDialog())
          {
            RoadGraph roadGraph = mapViewer.getSimulation().getRoadGraph();
            Edge edge = roadGraph.newEdge(new LineString(vertices),
              dialog.getSpeed(), dialog.getLanes(), dialog.getDelay());
            edge.add();
            trafficSimulator.setModified(true);
            getUndoManager().addEdit(new Undo(edge));
          }
          vertices = new ArrayList<Point3d>();
          vertices.add(new Point3d());
          onNode = false;
          mapViewer.repaint();
        }
      }
    }
    else
    {
      if (vertices.size() > 1)
      {
        vertices.remove(vertices.size() - 1);
        Point3d world = vertices.get(vertices.size() - 1);
        onNode = snapNode(e.getPoint(), world);
        mapViewer.repaint();
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
  }

  @Override
  public void mouseDragged(MouseEvent e)
  {
  }

  @Override
  public void mouseMoved(MouseEvent e)
  {
    Point3d world = vertices.get(vertices.size() - 1);
    onNode = snapNode(e.getPoint(), world);
    getMapViewer().repaint();
  }

  @Override
  public void paint(MapViewer mapViewer, Graphics2D g)
  {
    g.setColor(Color.MAGENTA);
    Projector projector = mapViewer.getProjector();
    java.awt.Point dp1 = new java.awt.Point();
    java.awt.Point dp2 = new java.awt.Point();
    for (int i = 0; i < vertices.size() - 1; i++)
    {
      Point3d pt1 = vertices.get(i);
      Point3d pt2 = vertices.get(i + 1);
      projector.project(pt1, dp1);
      projector.project(pt2, dp2);
      g.drawLine(dp1.x, dp1.y, dp2.x, dp2.y);
    }
    if (onNode)
    {
      g.setColor(Color.BLACK);
      Point3d pt = vertices.get(vertices.size() - 1);
      projector.project(pt, dp1);
      g.drawOval(dp1.x - 4, dp1.y - 4, 8, 8);
    }
  }

  public class Undo extends BasicUndoableEdit
  {
    private final Edge edge;

    private Undo(Edge edge)
    {
      this.edge = edge;
    }

    @Override
    public void undo() throws CannotUndoException
    {
      edge.remove();
      getSelection().remove(edge);
      trafficSimulator.setModified(true);
      getMapViewer().repaint();
    }

    @Override
    public void redo() throws CannotRedoException
    {
      edge.add();
      trafficSimulator.setModified(true);
      getMapViewer().repaint();
    }
  }
}
