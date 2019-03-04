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

import java.awt.BasicStroke;
import org.santfeliu.trafsim.geom.Geometry;
import org.santfeliu.trafsim.geom.LineString;
import org.santfeliu.trafsim.geom.Point;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import org.santfeliu.trafsim.Locations.Location;
import org.santfeliu.trafsim.RoadGraph.Edge;
import org.santfeliu.trafsim.RoadGraph.Node;
import org.santfeliu.trafsim.Vehicles.VehicleGroup;
import org.santfeliu.trafsim.geom.Polygon;
/**
 *
 * @author realor
 */
public class MapViewer extends javax.swing.JPanel
  implements MouseListener, MouseMotionListener, MouseWheelListener
{
  private static final Color NODE_COLOR = new Color(0, 160, 0);
  private static final Color DEAD_END_COLOR = Color.RED;
  private static final Color ORIGINS_COLOR = Color.MAGENTA;
  private static final BasicStroke STROKE1 = new BasicStroke(1f);
  private static final BasicStroke STROKE5 =
    new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
  private static final int PANEL_WIDTH = 230;

  private TrafficSimulator trafficSimulator;
  private final Box window = new Box(-10, -10, 0, 10, 10, 0);
  private java.awt.Point mouseStart;
  private final Projector projector = new Projector();
  private final java.awt.Point dp1 = new java.awt.Point();
  private final java.awt.Point dp2 = new java.awt.Point();
  private final Selection selection = new Selection();
  private Painter painter;
  private boolean edgesVisible = true;
  private boolean nodesVisible = true;
  private boolean baseLayersVisible = true;
  private boolean locationsVisible = true;
  private boolean vehiclesVisible = true;
  private boolean deadEndsVisible = false;
  private boolean originsVisible = false;
  private boolean indicatorsVisible = false;

  /**
   * Creates new form MapViewer
   */
  public MapViewer()
  {
    initComponents();
    initHandlers();
  }

  public TrafficSimulator getTrafficSimulator()
  {
    return trafficSimulator;
  }

  public void setTrafficSimulator(TrafficSimulator trafficSimulator)
  {
    this.trafficSimulator = trafficSimulator;
  }

  public Simulation getSimulation()
  {
    if (trafficSimulator == null) return null;
    return trafficSimulator.getSimulation();
  }

  public Projector getProjector()
  {
    return projector;
  }

  public Selection getSelection()
  {
    return selection;
  }

  public boolean isEdgesVisible()
  {
    return edgesVisible;
  }

  public void setEdgesVisible(boolean edgesVisible)
  {
    if (edgesVisible != this.edgesVisible)
    {
      this.edgesVisible = edgesVisible;
      repaint();
    }
  }

  public boolean isNodesVisible()
  {
    return nodesVisible;
  }

  public void setNodesVisible(boolean nodesVisible)
  {
    if (nodesVisible != this.nodesVisible)
    {
      this.nodesVisible = nodesVisible;
      repaint();
    }
  }

  public boolean isBaseLayersVisible()
  {
    return baseLayersVisible;
  }

  public void setBaseLayersVisible(boolean baseLayersVisible)
  {
    if (baseLayersVisible != this.baseLayersVisible)
    {
      this.baseLayersVisible = baseLayersVisible;
      repaint();
    }
  }

  public boolean isVehiclesVisible()
  {
    return vehiclesVisible;
  }

  public void setVehiclesVisible(boolean vehiclesVisible)
  {
    if (vehiclesVisible != this.vehiclesVisible)
    {
      this.vehiclesVisible = vehiclesVisible;
      repaint();
    }
  }

  public boolean isLocationsVisible()
  {
    return locationsVisible;
  }

  public void setLocationsVisible(boolean locationsVisible)
  {
    if (locationsVisible != this.locationsVisible)
    {
      this.locationsVisible = locationsVisible;
      repaint();
    }
  }

  public boolean isDeadEndsVisible()
  {
    return deadEndsVisible;
  }

  public void setDeadEndsVisible(boolean deadEndsVisible)
  {
    if (deadEndsVisible != this.deadEndsVisible)
    {
      this.deadEndsVisible = deadEndsVisible;
      repaint();
    }
  }

  public boolean isOriginsVisible()
  {
    return originsVisible;
  }

  public void setOriginsVisible(boolean originsVisible)
  {
    if (originsVisible != this.originsVisible)
    {
      this.originsVisible = originsVisible;
      repaint();
    }
  }

  public boolean isIndicatorsVisible()
  {
    return indicatorsVisible;
  }

  public void setIndicatorsVisible(boolean indicatorsVisible)
  {
    this.indicatorsVisible = indicatorsVisible;
    repaint();
  }

  public Painter getPainter()
  {
    return painter;
  }

  public void setPainter(Painter painter)
  {
    this.painter = painter;
    repaint();
  }

  public void zoomFactor(double factor)
  {
    Vector3d v = new Vector3d(
      (window.xmin + window.xmax) / 2,
      (window.ymin + window.ymax) / 2, 0);
    v.negate();
    window.move(v);
    window.scale(factor);
    v.negate();
    window.move(v);
    repaint();
  }

  public void zoomAll()
  {
    Simulation simulation = getSimulation();
    if (simulation == null) return;

    window.reset();
    simulation.getBoundingBox(window);

    if (window.isUndefined())
    {
      window.xmin = -10;
      window.ymin = -10;
      window.zmin = 0;
      window.xmax = 10;
      window.ymax = 10;
      window.zmax = 0;
    }
    else
    {
      double margin = 10;
      if (window.getWidth() != 0)
      {
        margin = 0.1 * window.getWidth();
      }
      else if (window.getHeight() != 0)
      {
        margin = 0.1 * window.getHeight();
      }
      window.xmin -= margin;
      window.ymin -= margin;
      window.xmax += margin;
      window.ymax += margin;
    }
    repaint();
  }

  @Override
  public String toString()
  {
    return getClass().getName();
  }
  
  @Override
  public void paintComponent(Graphics g)
  {
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, getWidth(), getHeight());

    Simulation simulation = getSimulation();
    if (simulation == null) return;

    g.setFont(new Font("Arial", Font.PLAIN, 12));

    projector.setWindow(window);
    projector.setWidth(getWidth());
    projector.setHeight(getHeight());
    projector.update();

    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);

    // layers
    if (baseLayersVisible)
    {
      for (GenericLayer layer : simulation.getGenericLayers())
      {
        paintGenericLayer(g2d, layer);
      }
    }

    // edges
    if (edgesVisible || indicatorsVisible)
    {
      for (Edge edge : simulation.getRoadGraph().getFeatures())
      {
        paintEdge(g2d, edge);
      }
    }

    // nodes
    if (nodesVisible)
    {
      g2d.setColor(NODE_COLOR);
      for (Node node : simulation.getRoadGraph().getNodes())
      {
        paintPoint(g2d, node.getPoint(), 3);
      }
    }

    // dead ends
    if (deadEndsVisible)
    {
      g2d.setColor(DEAD_END_COLOR);
      for (Node node : simulation.getRoadGraph().getNodes())
      {
        if (node.getOutEdges().isEmpty())
        {
          paintPoint(g2d, node.getPoint(), 5);
        }
      }
    }

    // origins
    if (originsVisible)
    {
      g2d.setColor(ORIGINS_COLOR);
      for (Node node : simulation.getRoadGraph().getNodes())
      {
        if (node.getInEdges().isEmpty())
        {
          paintPoint(g2d, node.getPoint(), 5);
        }
      }
    }

    // locations
    if (locationsVisible)
    {
      g.setFont(new Font("Arial", Font.PLAIN, 10));
      for (Feature feature : simulation.getLocations().getFeatures())
      {
        Location location = (Location)feature;
        paintLocation(g2d, location);
      }
    }

    // vehicles
    if (vehiclesVisible)
    {
      for (Feature feature : simulation.getVehicles().getFeatures())
      {
        VehicleGroup vehicleGroup = (VehicleGroup)feature;
        paintVehicleGroup(g2d, vehicleGroup);
      }
    }

    if (painter != null)
    {
      painter.paint(this, g2d);
    }

    if (indicatorsVisible)
    {
      paintIndicators(g2d);
    }

    String title = simulation.getTitle();
    if (title != null)
    {
      g.setColor(Color.BLACK);
      g.setFont(new Font("Arial", Font.PLAIN, 13));
      PaintUtils.drawHaloText(title, 10, 20, g2d);
    }
    String srsName = simulation.getSrsName();
    if (srsName != null)
    {
      g.setColor(Color.BLACK);
      g.setFont(new Font("Arial", Font.PLAIN, 10));
      Rectangle2D bounds = g.getFontMetrics().getStringBounds(srsName, g);
      PaintUtils.drawHaloText(srsName, getWidth() - (int)bounds.getWidth() - 10,
        getHeight() - (int)bounds.getHeight(), g2d);
    }
  }

  protected void paintIndicators(Graphics2D g)
  {
    int width = getWidth();
    int x = width - PANEL_WIDTH - 10;
    Simulation simulation = getSimulation();
    Indicators indicators = simulation.getIndicators();
    g.setFont(new Font("Arial", Font.PLAIN, 12));
    g.setColor(new Color(255, 255, 230));
    g.setStroke(new BasicStroke(1));
    g.fillRoundRect(x, 10, PANEL_WIDTH, 150, 6, 6);
    g.setColor(Color.ORANGE);
    g.drawRoundRect(x, 10, PANEL_WIDTH, 150, 6, 6);
    g.setColor(Color.BLACK);
    DecimalFormat df0 = new DecimalFormat("#,###,##0");
    DecimalFormat df2 = new DecimalFormat("#,###,##0.00");
    DecimalFormat df3 = new DecimalFormat("#,###,##0.000");
    int x2 = x + 10;
    g.drawString(trafficSimulator.getMessage("indicator.journeys") +
      ": " + df0.format(indicators.totalJourneyCount), x2, 30);
    g.drawString(trafficSimulator.getMessage("indicator.routedJourneys") +
      ": " + df0.format(indicators.totalRoutedCount), x2, 50);
    g.drawString(trafficSimulator.getMessage("indicator.unroutedJourneys") +
      ": " + df0.format(indicators.totalUnroutedCount), x2, 70);
    g.drawString(trafficSimulator.getMessage("indicator.totalDistance") +
      ": " + df2.format(indicators.totalDistance / 1000.0) + " km", x2, 90);
    g.drawString(trafficSimulator.getMessage("indicator.totalTime") +
      ": " + df2.format(indicators.totalTime) + " h", x2, 110);
    g.drawString(trafficSimulator.getMessage("indicator.journeyAvgDistance") +
      ": " + df3.format(indicators.journeyAvgDistance) + " m", x2, 130);
    g.drawString(trafficSimulator.getMessage("indicator.journeyAvgTime") +
      ": " + df3.format(60 * indicators.journeyAvgTime) + " min", x2, 150);

    if (selection.size() == 1)
    {
      Feature feature = selection.iterator().next();
      if (feature instanceof Edge)
      {
        Edge edge = ((Edge)feature);
        Edge.Indicators eInd = edge.getIndicators();
        g.setColor(new Color(255, 255, 230));
        g.setStroke(new BasicStroke(1));
        g.fillRoundRect(x, 170, PANEL_WIDTH, 70, 6, 6);
        g.setColor(Color.ORANGE);
        g.drawRoundRect(x, 170, PANEL_WIDTH, 70, 6, 6);
        g.setColor(Color.BLACK);
        g.drawString(trafficSimulator.getMessage("indicator.length") +
          ": " + df2.format(edge.lineString.getLength()) + " m", x2, 190);
        g.drawString(trafficSimulator.getMessage("indicator.vehicleCount") +
          ": " + df0.format(eInd.vehicleCount), x2, 210);
        g.drawString(trafficSimulator.getMessage("indicator.capacity") +
          ": " + df2.format(eInd.getCapacity()) + " vh / min", x2, 230);
      }
      else if (feature instanceof VehicleGroup)
      {
        VehicleGroup vehicleGroup = ((VehicleGroup)feature);
        VehicleGroup.Indicators vInd = vehicleGroup.getIndicators();
        g.setColor(new Color(255, 255, 230));
        g.setStroke(new BasicStroke(1));
        g.fillRoundRect(x, 170, PANEL_WIDTH, 110, 6, 6);
        g.setColor(Color.ORANGE);
        g.drawRoundRect(x, 170, PANEL_WIDTH, 110, 6, 6);
        g.setColor(Color.BLACK);
        g.drawString(trafficSimulator.getMessage("indicator.journeys") +
          ": " + df0.format(vInd.journeyCount), x2, 190);
        g.drawString(trafficSimulator.getMessage("indicator.routedJourneys") +
          ": " + df0.format(vInd.routedCount), x2, 210);
        g.drawString(trafficSimulator.getMessage("indicator.unroutedJourneys") +
          ": " + df0.format(vInd.unroutedCount), x2, 230);
        g.drawString(trafficSimulator.getMessage("indicator.journeyAvgDistance") +
          ": " + df3.format(vInd.getJourneyAvgDistance()) + " m", x2, 250);
        g.drawString(trafficSimulator.getMessage("indicator.journeyAvgTime") +
          ": " +df3.format(60 * vInd.getJourneyAvgTime()) + " min", x2, 270);
      }
    }
  }

  protected void paintGenericLayer(Graphics2D g, GenericLayer layer)
  {
    for (Feature feature : layer.getFeatures())
    {
      Geometry geometry = feature.getGeometry();
      if (geometry.getBoundingBox().overlaps(projector.getWindow()))
      {
        g.setColor(layer.getColor());
        if (geometry instanceof Point)
        {
          paintPoint(g, (Point)geometry, 3);
        }
        else if (geometry instanceof LineString)
        {
          paintLineString(g, (LineString)geometry);
        }
        else if (geometry instanceof Polygon)
        {
          paintPolygon(g, (Polygon)geometry);
        }
      }
    }
  }

  protected void paintEdge(Graphics2D g, Edge edge)
  {
    Simulation simulation = getSimulation();
    Indicators indicators = simulation.getIndicators();
    LineString lineString = edge.getLineString();
    List<Point3d> vertices = lineString.getVertices();
    for (int i = 0; i < vertices.size() - 1; i++)
    {
      Point3d pt1 = vertices.get(i);
      Point3d pt2 = vertices.get(i + 1);
      projector.project(pt1, dp1);
      projector.project(pt2, dp2);
      if (indicatorsVisible)
      {
        Edge.Indicators edgeInd = edge.getIndicators();
        if (edgeInd.vehicleCount > 0)
        {
          g.setStroke(STROKE5);
          float factor = 0.9f * edgeInd.vehicleCount /
            indicators.maxVehiclesPerEdge;
          if (factor > 0.9f) factor = 0.9f;
          g.setColor(new Color(1, 0, 0, factor + 0.1f));
          g.drawLine(dp1.x, dp1.y, dp2.x, dp2.y);
        }
      }
      if (edge.getLanes() == 1)
      {
        g.setStroke(STROKE1);
      }
      else
      {
        g.setStroke(new BasicStroke(edge.getLanes()));
      }
      if (selection.contains(edge))
      {
        g.setColor(Color.BLUE);
        g.drawLine(dp1.x, dp1.y, dp2.x, dp2.y);
        paintArrows(g);
      }
      else if (edgesVisible)
      {
        g.setColor(Color.GREEN);
        g.drawLine(dp1.x, dp1.y, dp2.x, dp2.y);
        paintArrows(g);
      }
    }
    g.setStroke(STROKE1);
  }

  protected void paintArrows(Graphics2D g)
  {
    double distance = dp1.distance(dp2);
    if (distance > 10)
    {
      int xm = (dp1.x + dp2.x) / 2;
      int ym = (dp1.y + dp2.y) / 2;
      Vector2d v = new Vector2d(dp2.x - dp1.x, dp2.y - dp1.y);
      v.normalize();
      Vector2d vc1 = new Vector2d(-v.y, v.x);
      Vector2d vc2 = new Vector2d(v.y, -v.x);
      v.scale(5);
      vc1.scale(3);
      vc2.scale(3);
      g.drawLine((int)(xm + v.x), (int)(ym + v.y),
         (int)(xm + vc1.x), (int)(ym + vc1.y));
      g.drawLine((int)(xm + v.x), (int)(ym + v.y),
         (int)(xm + vc2.x), (int)(ym + vc2.y));
    }
  }

  protected void paintVehicleGroup(Graphics2D g, VehicleGroup vehicleGroup)
  {
    Simulation simulation = getSimulation();
    Indicators indicators = simulation.getIndicators();
    int size;
    //if (selection == vehicleGroup)
    if (selection.contains(vehicleGroup))
    {
      size = 5;
      g.setColor(Color.BLUE);
    }
    else
    {
      size = 3;
      VehicleGroup.Indicators vehicleInd = vehicleGroup.getIndicators();
      if (indicatorsVisible && vehicleInd.journeyCount > 0)
      {
        double journeyAvgTime = vehicleInd.getJourneyAvgTime();
        if (vehicleInd.unroutedCount > 0)
        {
          g.setColor(Color.BLACK);
        }
        else if (journeyAvgTime > indicators.journeyAvgTime)
        {
          g.setColor(Color.RED);
        }
        else
        {
          g.setColor(Color.GREEN);
        }
      }
      else
      {
        g.setColor(Color.GRAY);
      }
    }
    paintPoint(g, vehicleGroup.getPoint(), size);
  }

  protected void paintLocation(Graphics2D g, Location location)
  {
    Point point = location.getPoint();
    projector.project(point.getPosition(), dp1);

    g.setColor(Color.BLACK);
    g.fillOval(dp1.x - 4, dp1.y - 4, 8, 8);
    //if (selection == location)
    if (selection.contains(location))
    {
      g.setColor(Color.BLUE);
    }
    else
    {
      g.setColor(Color.WHITE);
    }
    g.fillOval(dp1.x - 3, dp1.y - 3, 6, 6);
    g.setColor(Color.BLACK);
    if (location.isOrigin())
    {
      g.fillOval(dp1.x - 1, dp1.y - 1, 2, 2);
    }

    String name = location.getName();
    Rectangle2D bounds = g.getFontMetrics().getStringBounds(name, g);
    g.drawString(name, (int)(dp1.x - bounds.getWidth() / 2),
      (int)(dp1.y + bounds.getHeight()));
  }

  protected void paintPoint(Graphics2D g, Point point, int size)
  {
    projector.project(point.getPosition(), dp1);

    g.fillOval(dp1.x - size / 2, dp1.y - size / 2, size, size);
  }

  protected void paintLineString(Graphics2D g, LineString lineString)
  {
    List<Point3d> points = lineString.getVertices();
    for (int i = 0; i < points.size() - 1; i++)
    {
      Point3d pt1 = points.get(i);
      Point3d pt2 = points.get(i + 1);
      projector.project(pt1, dp1);
      projector.project(pt2, dp2);
      g.drawLine(dp1.x, dp1.y, dp2.x, dp2.y);
    }
  }

  protected void paintPolygon(Graphics2D g, Polygon polygon)
  {
    List<Point3d> points = polygon.getVertices();
    for (int i = 0; i < points.size() - 1; i++)
    {
      Point3d pt1 = points.get(i);
      Point3d pt2 = points.get(i + 1);
      projector.project(pt1, dp1);
      projector.project(pt2, dp2);
      g.drawLine(dp1.x, dp1.y, dp2.x, dp2.y);
    }
  }

  private void initHandlers()
  {
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
  }

  /* painter */

  public interface Painter
  {
    public void paint(MapViewer mapViewer, Graphics2D g);
  }

  /* MouseListener */

  @Override
  public void mouseClicked(MouseEvent e)
  {
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    if (e.getButton() != MouseEvent.BUTTON1)
    {
      mouseStart = e.getPoint();
    }
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
    mouseStart = null;
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
  }


  /* MouseMotionListener */
  @Override
  public void mouseDragged(MouseEvent e)
  {
    if (mouseStart != null)
    {
      java.awt.Point mouse = e.getPoint();
      Point3d w1 = new Point3d();
      Point3d w2 = new Point3d();

      projector.unproject(mouseStart, w1);
      projector.unproject(mouse, w2);
      Vector3d vector = new Vector3d();

      vector.sub(w1, w2);

      window.move(vector);
      mouseStart = mouse;
      repaint();
    }
  }

  @Override
  public void mouseMoved(MouseEvent e)
  {
  }

  /* MouseWheelListener */

  @Override
  public void mouseWheelMoved(MouseWheelEvent e)
  {
    double units = e.getPreciseWheelRotation();
    if (units != 0)
    {
      java.awt.Point mouse = e.getPoint();
      Point3d world = new Point3d();
      projector.unproject(mouse, world);

      Vector3d vector = new Vector3d(world);
      vector.negate();
      window.move(vector);
      window.scale(1 + units / 10.0);
      vector.negate();
      window.move(vector);
      repaint();
    }
  }

  public class Selection extends HashSet<Feature>
  {
    public void set(Feature feature)
    {
      super.clear();
      add(feature);
      repaint();
    }

    public void setAll(Collection<Feature> features)
    {
      super.clear();
      addAll(features);
      repaint();
    }

    public void invert(Feature feature)
    {
      if (contains(feature))
      {
        remove(feature);
      }
      else
      {
        add(feature);
      }
      repaint();
    }

    public void invertAll(Collection<Feature> features)
    {
      ArrayList<Feature> copy = new ArrayList<Feature>();
      copy.addAll(features);

      for (Feature feature : copy)
      {
        if (contains(feature))
        {
          remove(feature);
        }
        else
        {
          add(feature);
        }
      }
      repaint();
    }

    @Override
    public boolean add(Feature feature)
    {
      boolean added = super.add(feature);
      repaint();
      return added;
    }

    @Override
    public boolean remove(Object o)
    {
      boolean removed = super.remove(o);
      repaint();
      return removed;
    }

    @Override
    public boolean addAll(Collection<? extends Feature> c)
    {
      boolean added = super.addAll(c);
      return added;
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
      boolean removed = super.removeAll(c);
      repaint();
      return removed;
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
      boolean retained = super.retainAll(c);
      repaint();
      return retained;
    }

    @Override
    public void clear()
    {
      super.clear();
      repaint();
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

    setBackground(new java.awt.Color(255, 255, 255));
    setLayout(new java.awt.BorderLayout());
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables
}
