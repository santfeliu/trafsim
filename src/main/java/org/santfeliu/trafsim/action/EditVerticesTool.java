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
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.Feature;
import org.santfeliu.trafsim.Finder;
import org.santfeliu.trafsim.Locations;
import org.santfeliu.trafsim.MapViewer;
import org.santfeliu.trafsim.MapViewer.Painter;
import org.santfeliu.trafsim.MapViewer.Selection;
import org.santfeliu.trafsim.PickInfo;
import org.santfeliu.trafsim.Projector;
import org.santfeliu.trafsim.RoadGraph;
import org.santfeliu.trafsim.RoadGraph.Edge;
import org.santfeliu.trafsim.Simulation;
import org.santfeliu.trafsim.TrafficSimulator;
import org.santfeliu.trafsim.Vehicles;
import org.santfeliu.trafsim.geom.Geometry;
import org.santfeliu.trafsim.geom.LineString;
import org.santfeliu.trafsim.geom.Point;

/**
 *
 * @author realor
 */
public class EditVerticesTool extends Tool implements
  MouseListener, MouseMotionListener, Painter
{
  private final PickInfo pick = new PickInfo();
  private final List<PickInfo> picks = new ArrayList<PickInfo>();
  private java.awt.Point lastPoint;
  private boolean dragging;
  private List<Geometry> dragGeometries;

  public EditVerticesTool(TrafficSimulator trafficSimulator)
  {
    super(trafficSimulator);
  }

  @Override
  public String getName()
  {
    return "editVerticesTool";
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
  }

  @Override
  public void mouseClicked(MouseEvent e)
  {
    if (e.isShiftDown() || e.isControlDown()) return;

    MapViewer mapViewer = getMapViewer();
    Projector projector = mapViewer.getProjector();
    double tolerance = SELECT_PIXELS / projector.getScaleX();
    Point3d worldPoint = new Point3d();
    projector.unproject(e.getPoint(), worldPoint);
    Simulation simulation = getSimulation();
    pick.clear();
    if (mapViewer.isLocationsVisible())
    {
      Locations locations = simulation.getLocations();
      Finder.findByPoint(locations.getFeatures(), worldPoint, tolerance, pick);
    }
    if (mapViewer.isVehiclesVisible())
    {
      Vehicles vehicles = simulation.getVehicles();
      Finder.findByPoint(vehicles.getFeatures(), worldPoint, tolerance, pick);
    }
    if (mapViewer.isEdgesVisible())
    {
      RoadGraph roadGraph = simulation.getRoadGraph();
      Finder.findByPoint(roadGraph.getFeatures(), worldPoint, tolerance, pick);
    }
    Selection selection = mapViewer.getSelection();
    selection.clear();
    if (pick.getFeature() != null)
    {
      selection.add(pick.getFeature());
    }
    mapViewer.repaint();
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    if (e.getButton() == MouseEvent.BUTTON1)
    {
      if (e.isShiftDown()) // split or join edges
      {
        if (picks.isEmpty()) // not on vertex
        {
          splitEdge(e.getPoint());
        }
        else // on vertex
        {
          joinEdges();
        }
      }
      else if (e.isControlDown()) // insert or remove vertex
      {
        if (picks.isEmpty()) // not on vertex
        {
          insertVertex(e.getPoint());
        }
        else // on vertex
        {
          removeVertex();
        }
      }
      else
      {
        dragging = !picks.isEmpty();
        if (dragging)
        {
          dragGeometries = copyPickGeometries();
        }
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
    if (dragging)
    {
      List<Feature> features = copyPickFeatures();
      List<Geometry> oldGeometries = dragGeometries;
      List<Geometry> newGeometries = copyPickGeometries();
      getUndoManager().addEdit(new Undo(features, oldGeometries,
        features, newGeometries));
      dragging = false;
      getMapViewer().repaint();
    }
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
    lastPoint = e.getPoint();
    if (dragging)
    {
      for (PickInfo pi : picks)
      {
        Feature feature = pi.getFeature();
        feature.remove();
      }
      for (PickInfo pi : picks)
      {
        Feature feature = pi.getFeature();
        Point3d worldPoint = new Point3d();
        snapNode(lastPoint, worldPoint);
        Geometry geometry = feature.getGeometry();
        if (geometry instanceof Point)
        {
          Point point = (Point)geometry;
          point.getPosition().set(worldPoint);
        }
        else if (geometry instanceof LineString)
        {
          LineString lineString = (LineString)geometry;
          List<Point3d> vertices = lineString.getVertices();
          vertices.get(pi.getIndex()).set(worldPoint);
        }
        feature.add();
      }
      MapViewer mapViewer = getMapViewer();
      mapViewer.repaint();
      trafficSimulator.setModified(true);
    }
  }

  @Override
  public void mouseMoved(MouseEvent e)
  {
    lastPoint = e.getPoint();
    MapViewer mapViewer = getMapViewer();
    if (!mapViewer.getSelection().isEmpty())
    {
      mapViewer.repaint();
    }
  }

  @Override
  public void paint(MapViewer mapViewer, Graphics2D g)
  {
    Projector projector = getProjector();
    java.awt.Point dp = new java.awt.Point();
    Selection selection = mapViewer.getSelection();
    for (Feature feature : selection)
    {
      Geometry geometry = feature.getGeometry();
      if (geometry instanceof LineString)
      {
        LineString lineString = (LineString)geometry;
        List<Point3d> vertices = lineString.getVertices();
        for (int i = 0; i < vertices.size(); i++)
        {
          projector.project(vertices.get(i), dp);
          g.setColor(i == 0 || i == vertices.size() - 1 ?
            Color.BLACK : Color.BLUE);
          g.fillOval(dp.x - 2, dp.y - 2, 5, 5);
        }
      }
    }

    g.setColor(Color.BLUE);
    if (!dragging && lastPoint != null)
    {
      double tolerance = SNAP_PIXELS / projector.getScaleX();
      Point3d worldPoint = new Point3d();
      projector.unproject(lastPoint, worldPoint);

      picks.clear();
      Finder.findVertices(mapViewer.getSelection(),
        worldPoint, tolerance, picks);

      for (PickInfo pi : picks)
      {
        projector.project(pi.getOnFeaturePoint(), dp);
        g.drawOval(dp.x - 4, dp.y - 4, 8, 8);
      }
    }
  }

  private void insertVertex(java.awt.Point dp)
  {
    MapViewer mapViewer = getMapViewer();
    Projector projector = mapViewer.getProjector();
    double tolerance = SNAP_PIXELS / projector.getScaleX();
    Point3d worldPoint = new Point3d();
    projector.unproject(dp, worldPoint);
    pick.clear();
    Finder.findByPoint(mapViewer.getSelection(), worldPoint, tolerance, pick);
    Feature feature = pick.getFeature();
    if (feature != null)
    {
      Geometry geometry = feature.getGeometry();
      if (geometry instanceof LineString)
      {
        Geometry oldGeometry = geometry.duplicate();
        feature.remove();
        LineString lineString = (LineString)geometry;
        List<Point3d> vertices = lineString.getVertices();
        int index = pick.getIndex();
        Point3d point = new Point3d();
        point.set(pick.getOnFeaturePoint());
        vertices.add(index + 1, point);
        feature.add();
        mapViewer.repaint();
        trafficSimulator.setModified(true);
        List<Feature> features = new ArrayList<Feature>();
        features.add(feature);
        getUndoManager().addEdit(new Undo(
          features,
          Collections.singletonList(oldGeometry),
          features,
          Collections.singletonList(lineString.duplicate())));
      }
    }
  }

  private void removeVertex()
  {
    List<Feature> features = copyPickFeatures();
    List<Geometry> oldGeometries = copyPickGeometries();
    for (PickInfo pi : picks)
    {
      Feature feature = pi.getFeature();
      Geometry geometry = feature.getGeometry();
      if (geometry instanceof LineString)
      {
        LineString lineString = (LineString)geometry;
        List<Point3d> vertices = lineString.getVertices();
        if (vertices.size() > 2)
        {
          feature.remove();
          vertices.remove(pi.getIndex());
          feature.add();
        }
      }
    }
    List<Geometry> newGeometries = copyPickGeometries();
    getMapViewer().repaint();
    trafficSimulator.setModified(true);
    getUndoManager().addEdit(new Undo(features, oldGeometries,
      features, newGeometries));
  }

  private void splitEdge(java.awt.Point dp)
  {
    MapViewer mapViewer = getMapViewer();
    Projector projector = mapViewer.getProjector();
    double tolerance = SNAP_PIXELS / projector.getScaleX();
    Point3d worldPoint = new Point3d();
    projector.unproject(dp, worldPoint);
    pick.clear();
    Finder.findByPoint(mapViewer.getSelection(), worldPoint, tolerance, pick);
    Feature feature = pick.getFeature();
    if (feature instanceof Edge)
    {
      Edge edge = (Edge)feature;
      edge.remove();
      Selection selection = mapViewer.getSelection();
      selection.remove(edge);

      LineString lineString = edge.getLineString();
      int index = pick.getIndex();
      List<Point3d> vertices = lineString.getVertices();

      List<Point3d> vertices1 = new ArrayList<Point3d>();
      Point3d splitPoint = new Point3d();
      splitPoint.set(pick.getOnFeaturePoint());
      for (int i = 0; i <= index; i++)
      {
        vertices1.add(new Point3d(vertices.get(i)));
      }
      vertices1.add(splitPoint);

      List<Point3d> vertices2 = new ArrayList<Point3d>();
      splitPoint = new Point3d();
      splitPoint.set(pick.getOnFeaturePoint());
      vertices2.add(splitPoint);
      for (int i = index + 1; i < vertices.size(); i++)
      {
        vertices2.add(new Point3d(vertices.get(i)));
      }
      RoadGraph roadGraph = mapViewer.getSimulation().getRoadGraph();

      Edge edge1 = roadGraph.newEdge(new LineString(vertices1),
        edge.getSpeed(), edge.getLanes());
      edge1.add();

      Edge edge2 = roadGraph.newEdge(new LineString(vertices2),
        edge.getSpeed(), edge.getLanes());
      edge2.add();

      selection.add(edge1);
      selection.add(edge2);

      mapViewer.repaint();
      trafficSimulator.setModified(true);

      List<Feature> newFeatures = new ArrayList<Feature>();
      newFeatures.add(edge1);
      newFeatures.add(edge2);
      List<Geometry> newGeometries = new ArrayList<Geometry>();
      newGeometries.add(edge1.getLineString().duplicate());
      newGeometries.add(edge2.getLineString().duplicate());

      getUndoManager().addEdit(new Undo(
        Collections.singletonList(edge),
        Collections.singletonList(edge.getLineString()),
        newFeatures, newGeometries));
    }
  }

  private void joinEdges()
  {
    if (picks.size() == 2)
    {
      PickInfo pick1 = picks.get(0);
      PickInfo pick2 = picks.get(1);
      if (pick2.getIndex() > pick1.getIndex() && pick1.getIndex() == 0)
      {
        // pick2 -> pick1 : swap
        PickInfo temp = pick2;
        pick2 = pick1;
        pick1 = temp;
      }
      else if (pick1.getIndex() > pick2.getIndex() && pick2.getIndex() == 0)
      {
        // pick1 -> pick2
      }
      else return;

      Edge edge1 = (Edge)pick1.getFeature();
      Edge edge2 = (Edge)pick2.getFeature();
      edge1.remove();
      edge2.remove();
      MapViewer mapViewer = getMapViewer();
      mapViewer.getSelection().remove(edge1);
      mapViewer.getSelection().remove(edge2);

      Edge edge3 = edge1.duplicate();
      List<Point3d> vertices = edge3.getLineString().getVertices();
      vertices.remove(vertices.size() - 1);
      vertices.addAll(edge2.getLineString().duplicate().getVertices());
      edge3.add();
      mapViewer.getSelection().add(edge3);
      mapViewer.repaint();
      trafficSimulator.setModified(true);

      List<Feature> oldFeatures = new ArrayList<Feature>();
      oldFeatures.add(edge1);
      oldFeatures.add(edge2);
      List<Geometry> oldGeometries = new ArrayList<Geometry>();
      oldGeometries.add(edge1.getGeometry());
      oldGeometries.add(edge2.getGeometry());

      getUndoManager().addEdit(new Undo(oldFeatures, oldGeometries,
        Collections.singletonList(edge3),
        Collections.singletonList(edge3.getLineString().duplicate())));
    }
  }

  private List<Feature> copyPickFeatures()
  {
    ArrayList<Feature> features = new ArrayList<Feature>();
    for (PickInfo pi : picks)
    {
      features.add(pi.getFeature());
    }
    return features;
  }

  private List<Geometry> copyPickGeometries()
  {
    ArrayList<Geometry> geometries = new ArrayList<Geometry>();
    for (PickInfo pi : picks)
    {
      Feature feature = pi.getFeature();
      Geometry geometry = feature.getGeometry();
      geometries.add(geometry.duplicate());
    }
    return geometries;
  }

  public class Undo extends BasicUndoableEdit
  {
    private final List<Feature> oldFeatures;
    private final List<Geometry> oldGeometries;
    private final List<Feature> newFeatures;
    private final List<Geometry> newGeometries;

    private Undo(List<Feature> oldFeatures, List<Geometry> oldGeometries,
            List<Feature> newFeatures, List<Geometry> newGeometries)
    {
      this.oldFeatures = oldFeatures;
      this.oldGeometries = oldGeometries;
      this.newFeatures = newFeatures;
      this.newGeometries = newGeometries;
    }

    @Override
    public void undo() throws CannotUndoException
    {
      for (Feature feature : newFeatures)
      {
        feature.remove();
      }
      if (newFeatures != oldFeatures)
      {
        getSelection().removeAll(newFeatures);
      }
      for (int i = 0; i < oldFeatures.size(); i++)
      {
        Feature feature = oldFeatures.get(i);
        feature.remove();
        feature.setGeometry(oldGeometries.get(i));
        feature.add();
      }
      getMapViewer().repaint();
      trafficSimulator.setModified(true);
    }

    @Override
    public void redo() throws CannotRedoException
    {
      for (Feature feature : oldFeatures)
      {
        feature.remove();
      }
      if (newFeatures != oldFeatures)
      {
        getSelection().removeAll(oldFeatures);
      }
      for (int i = 0; i < newFeatures.size(); i++)
      {
        Feature feature = newFeatures.get(i);
        feature.remove();
        feature.setGeometry(newGeometries.get(i));
        feature.add();
      }
      getMapViewer().repaint();
      trafficSimulator.setModified(true);
    }
  }
}
