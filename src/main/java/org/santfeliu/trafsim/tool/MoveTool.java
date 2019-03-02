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
package org.santfeliu.trafsim.tool;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.santfeliu.trafsim.Feature;
import org.santfeliu.trafsim.MapViewer;
import org.santfeliu.trafsim.MapViewer.Selection;
import org.santfeliu.trafsim.Projector;
import org.santfeliu.trafsim.TrafficSimulator;
import org.santfeliu.trafsim.geom.Geometry;
import org.santfeliu.trafsim.geom.LineString;
import org.santfeliu.trafsim.geom.Point;

/**
 *
 * @author realor
 */
public class MoveTool extends Tool implements
  MouseListener, MouseMotionListener, MapViewer.Painter
{
  private Point3d origin;
  private Point3d destination;

  public MoveTool(TrafficSimulator trafficSimulator)
  {
    super(trafficSimulator);
  }

  @Override
  public String getName()
  {
    return "move";
  }

  @Override
  public void start()
  {
    MapViewer mapViewer = getMapViewer();
    mapViewer.addMouseListener(this);
    mapViewer.addMouseMotionListener(this);
    mapViewer.setPainter(this);
    info("info");
  }

  @Override
  public void stop()
  {
    MapViewer mapViewer = getMapViewer();
    mapViewer.removeMouseListener(this);
    mapViewer.removeMouseMotionListener(this);
    mapViewer.setPainter(null);
    origin = null;
    destination = null;
  }

  @Override
  public void mouseClicked(MouseEvent e)
  {
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    if (e.getButton() == MouseEvent.BUTTON1)
    {
      Projector projector = getMapViewer().getProjector();
      origin = new Point3d();
      projector.unproject(e.getPoint(), origin);
    }
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
    if (origin != null && destination != null)
    {
      moveSelection();
      getMapViewer().repaint();
    }
    origin = null;
    destination = null;
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
    Projector projector = getMapViewer().getProjector();
    destination = new Point3d();
    projector.unproject(e.getPoint(), destination);
    getMapViewer().repaint();
  }

  @Override
  public void mouseMoved(MouseEvent e)
  {
  }

  @Override
  public void paint(MapViewer mapViewer, Graphics2D g)
  {
    if (origin == null || destination == null) return;

    Point3d moved1 = new Point3d();
    Point3d moved2 = new Point3d();
    java.awt.Point dp1 = new java.awt.Point();
    java.awt.Point dp2 = new java.awt.Point();
    Projector projector = getProjector();
    Vector3d move = new Vector3d();
    move.sub(destination, origin);

    g.setColor(Color.BLUE);

    Selection selection = mapViewer.getSelection();
    for (Feature feature : selection)
    {
      Geometry geometry = feature.getGeometry();
      if (geometry instanceof Point)
      {
        Point point = (Point)geometry;
        moved1.set(point.getPosition());
        moved1.add(move);
        projector.project(moved1, dp1);
        g.drawOval(dp1.x - 1, dp1.y - 1 , 3, 3);
      }
      else if (geometry instanceof LineString)
      {
        LineString lineString = (LineString)geometry;
        List<Point3d> vertices = lineString.getVertices();
        for (int i = 0; i < vertices.size() - 1; i++)
        {
          moved1.set(vertices.get(i));
          moved2.set(vertices.get(i + 1));
          moved1.add(move);
          moved2.add(move);
          projector.project(moved1, dp1);
          projector.project(moved2, dp2);
          g.drawLine(dp1.x, dp1.y, dp2.x, dp2.y);
        }
      }
    }
  }

  private void moveSelection()
  {
    Vector3d move = new Vector3d();
    move.sub(destination, origin);
    Matrix4d matrix = new Matrix4d();
    matrix.setIdentity();
    matrix.setTranslation(move);

    Selection selection = getMapViewer().getSelection();
    for (Feature feature : selection)
    {
      feature.transform(matrix);
    }
  }
}
