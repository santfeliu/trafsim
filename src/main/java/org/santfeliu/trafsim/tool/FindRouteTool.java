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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.util.List;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.MapViewer;
import org.santfeliu.trafsim.MapViewer.Painter;
import org.santfeliu.trafsim.PaintUtils;
import org.santfeliu.trafsim.RouteFinder;
import org.santfeliu.trafsim.Projector;
import org.santfeliu.trafsim.Route;
import org.santfeliu.trafsim.RouteMeter;
import org.santfeliu.trafsim.TrafficSimulator;
import org.santfeliu.trafsim.geom.LineString;

/**
 *
 * @author realor
 */
public class FindRouteTool extends Tool implements MouseListener, Painter
{
  private final RouteMeter routeMeter = new RouteMeter();
  private RouteFinder routeFinder;
  private Route route;
  private double routeTime;
  private java.awt.Point dp = new java.awt.Point();

  public FindRouteTool(TrafficSimulator trafficSimulator)
  {
    super(trafficSimulator);
  }

  @Override
  public void start()
  {
    routeFinder = new RouteFinder(getSimulation().getRoadGraph(), routeMeter);
    MapViewer mapViewer = getMapViewer();
    mapViewer.addMouseListener(this);
    mapViewer.setPainter(this);
  }

  @Override
  public void stop()
  {
    MapViewer mapViewer = getMapViewer();
    mapViewer.removeMouseListener(this);
    mapViewer.setPainter(null);
    route = null;
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
      Projector projector = mapViewer.getProjector();
      Point3d worldPoint = new Point3d();
      projector.unproject(e.getPoint(), worldPoint);
      if (routeFinder.getOriginNode() == null)
      {
        routeFinder.setOrigin(worldPoint, Double.POSITIVE_INFINITY);
      }
      else
      {
        routeFinder.setDestination(worldPoint, Double.POSITIVE_INFINITY);
        route = routeFinder.getRoute();
        routeTime = routeMeter.getTime(route);
      }
    }
    else
    {
      routeFinder.clear();
      route = null;
    }
    mapViewer.repaint();
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
  public void paint(MapViewer mapViewer, Graphics2D g)
  {
    Stroke stroke = g.getStroke();
    if (route != null && !route.isEmpty())
    {
      g.setColor(new Color(0, 0, 1, 0.5f));
      g.setStroke(new BasicStroke(7,
        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      Projector projector = mapViewer.getProjector();
      LineString lineString = route.getLineString();
      paintLineString(lineString, projector, g);
    }
    g.setStroke(new BasicStroke(1.5f));
    Point3d origin = routeFinder.getOrigin();
    if (origin != null)
    {
      mapViewer.getProjector().project(origin, dp);
      g.setColor(Color.WHITE);
      g.fillOval(dp.x - 5, dp.y - 5, 10, 10);
      g.setColor(Color.BLACK);
      g.drawOval(dp.x - 5, dp.y - 5, 10, 10);
    }
    Point3d destination = routeFinder.getDestination();
    if (destination != null)
    {
      mapViewer.getProjector().project(destination, dp);
      g.setColor(Color.CYAN);
      g.fillOval(dp.x - 5, dp.y - 5, 10, 10);
      g.setColor(Color.BLACK);
      g.drawOval(dp.x - 5, dp.y -5, 10, 10);
      if (route != null && !route.isEmpty())
      {
        drawTexts(g);
      }
    }
    g.setStroke(stroke);
  }

  private void paintLineString(LineString lineString,
    Projector projector, Graphics2D g)
  {
    java.awt.Point dp = new java.awt.Point();
    List<Point3d> vertices = lineString.getVertices();
    Path2D path = new Path2D.Double();
    projector.project(vertices.get(0), dp);
    path.moveTo(dp.x, dp.y);
    for (int i = 1; i < vertices.size(); i++)
    {
      projector.project(vertices.get(i), dp);
      path.lineTo(dp.x, dp.y);
    }
    g.draw(path);
  }

  private void drawTexts(Graphics2D g)
  {
    g.setFont(g.getFont().deriveFont(12.0f));
    DecimalFormat df = new DecimalFormat("#,###,##0.00");
    PaintUtils.drawHaloText(df.format(route.getLength()) + "m",
      dp.x + 10, dp.y + 10, g);
    PaintUtils.drawHaloText(df.format(routeTime * 60) + "min",
      dp.x + 10, dp.y + 24, g);
  }
}
