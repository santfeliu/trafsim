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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.Box;
import org.santfeliu.trafsim.EdgeDialog;
import org.santfeliu.trafsim.Feature;
import org.santfeliu.trafsim.Finder;
import org.santfeliu.trafsim.LocationDialog;
import org.santfeliu.trafsim.Locations;
import org.santfeliu.trafsim.Locations.Location;
import org.santfeliu.trafsim.MapViewer;
import org.santfeliu.trafsim.MapViewer.Painter;
import org.santfeliu.trafsim.MapViewer.Selection;
import org.santfeliu.trafsim.Movements;
import org.santfeliu.trafsim.PickInfo;
import org.santfeliu.trafsim.Projector;
import org.santfeliu.trafsim.RoadGraph;
import org.santfeliu.trafsim.RoadGraph.Edge;
import org.santfeliu.trafsim.Simulation;
import org.santfeliu.trafsim.TrafficSimulator;
import org.santfeliu.trafsim.VehicleGroupDialog;
import org.santfeliu.trafsim.Vehicles;
import org.santfeliu.trafsim.Vehicles.VehicleGroup;

/**
 *
 * @author realor
 */
public class SelectTool extends Tool
  implements MouseListener, MouseMotionListener, Painter
{
  private final PickInfo pick = new PickInfo();
  private Point3d firstCorner;
  private Point3d lastCorner;
  private int mode;
  private static final int SET = 0;
  private static final int ADD = 1;
  private static final int INVERT = 2;

  public SelectTool(TrafficSimulator trafficSimulator)
  {
    super(trafficSimulator);
  }

  @Override
  public String getName()
  {
    return "selectTool";
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
    firstCorner = null;
    lastCorner = null;
  }

  @Override
  public void mouseClicked(MouseEvent e)
  {
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
    setMode(e);
    Feature feature = pick.getFeature();
    if (feature == null)
    {
      updateSelection(Collections.EMPTY_LIST);
    }
    else
    {
      updateSelection(Collections.singletonList(feature));
    }
    if (e.getClickCount() > 1)
    {
      if (feature instanceof Edge)
      {
        Edge edge = (Edge)feature;
        EdgeDialog dialog = new EdgeDialog(trafficSimulator, true);
        dialog.setSpeed(edge.getSpeed());
        dialog.setLanes(edge.getLanes());
        dialog.setDelay(edge.getDelay());
        if (dialog.showDialog())
        {
          List<Edge> edges = getSelectedEdges();
          getUndoManager().addEdit(new UndoEdges(edges,
            dialog.getSpeed(), dialog.getLanes(), dialog.getDelay()));
          changeEdges(edges,
            dialog.getSpeed(), dialog.getLanes(), dialog.getDelay());
        }
      }
      else if (feature instanceof Location)
      {
        Location location = (Location)feature;
        LocationDialog dialog = new LocationDialog(trafficSimulator, true);
        dialog.setLocationName(location.getName());
        dialog.setLocationLabel(location.getLabel());
        dialog.setOrigin(location.isOrigin());
        if (dialog.showDialog())
        {
          getUndoManager().addEdit(new UndoLocation(location,
            dialog.getLocationName(), dialog.getLocationLabel(),
            dialog.isOrigin()));
          location.setName(dialog.getLocationName());
          location.setLabel(dialog.getLocationLabel());
          location.setOrigin(dialog.isOrigin());
          mapViewer.repaint();
          trafficSimulator.setModified(true);
        }
      }
      else if (feature instanceof VehicleGroup)
      {
        VehicleGroup vehicleGroup = (VehicleGroup)feature;
        VehicleGroupDialog dialog =
          new VehicleGroupDialog(trafficSimulator, true);
        dialog.setCount(vehicleGroup.getCount());
        dialog.setGroup(vehicleGroup.getGroup());
        dialog.setMovements(vehicleGroup.getMovements());
        if (dialog.showDialog())
        {
          List<VehicleGroup> vehicleGroups = getSelectedVehicleGroups();
          getUndoManager().addEdit(new UndoVehicleGroups(vehicleGroups,
            dialog.getCount(), dialog.getGroup(), dialog.getMovements()));
          changeVehicleGroups(vehicleGroups,
            dialog.getCount(), dialog.getGroup(), dialog.getMovements());
        }
      }
    }
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    if (e.getButton() == MouseEvent.BUTTON1)
    {
      Projector projector = getMapViewer().getProjector();
      firstCorner = new Point3d();
      projector.unproject(e.getPoint(), firstCorner);
      setMode(e);
    }
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
    if (firstCorner != null)
    {
      if (lastCorner != null)
      {
        Box box = new Box();
        box.extend(firstCorner);
        box.extend(lastCorner);
        MapViewer mapViewer = getMapViewer();
        Simulation simulation = getSimulation();
        HashSet<Feature> selected = new HashSet<Feature>();
        if (mapViewer.isLocationsVisible())
        {
          Locations locations = simulation.getLocations();
          Finder.findByBox(locations.getFeatures(), box, selected);
        }
        if (mapViewer.isVehiclesVisible())
        {
          Vehicles vehicles = simulation.getVehicles();
          Finder.findByBox(vehicles.getFeatures(), box, selected);
        }
        if (mapViewer.isEdgesVisible())
        {
          RoadGraph roadGraph = simulation.getRoadGraph();
          Finder.findByBox(roadGraph.getFeatures(), box, selected);
        }
        updateSelection(selected);
      }
      firstCorner = null;
      lastCorner = null;
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
    if (firstCorner != null)
    {
      lastCorner = new Point3d();
      MapViewer mapViewer = getMapViewer();
      Projector projector = mapViewer.getProjector();
      projector.unproject(e.getPoint(), lastCorner);
      mapViewer.repaint();
    }
  }

  @Override
  public void mouseMoved(MouseEvent e)
  {
  }

  @Override
  public void paint(MapViewer mapViewer, Graphics2D g)
  {
    if (firstCorner != null && lastCorner != null)
    {
      java.awt.Point dp1 = new java.awt.Point();
      java.awt.Point dp2 = new java.awt.Point();
      Projector projector = mapViewer.getProjector();
      projector.project(firstCorner, dp1);
      projector.project(lastCorner, dp2);
      Rectangle rect = new Rectangle(dp1.x, dp1.y, 0, 0);
      rect.add(dp2);
      g.setColor(Color.BLUE);
      g.draw(rect);
    }
  }

  private void setMode(MouseEvent event)
  {
    if (event.isShiftDown())
    {
      mode = ADD;
    }
    else if (event.isControlDown())
    {
      mode = INVERT;
    }
    else
    {
      mode = SET;
    }
  }

  private void updateSelection(Collection<Feature> features)
  {
    Selection selection = getMapViewer().getSelection();
    switch (mode)
    {
      case SET:
        selection.setAll(features);
        break;
      case ADD:
        selection.addAll(features);
        break;
      case INVERT:
        selection.invertAll(features);
        break;
      default:
        break;
    }
  }

  private void changeEdges(List<Edge> edges, int speed, int lanes, int delay)
  {
    for (Edge edge : edges)
    {
      edge.setSpeed(speed);
      edge.setLanes(lanes);
      edge.setDelay(delay);
    }
    getMapViewer().repaint();
    trafficSimulator.setModified(true);
  }

  private void changeVehicleGroups(List<VehicleGroup> vehicleGroups,
    int count, String group, Movements movements)
  {
    for (VehicleGroup vehicleGroup : vehicleGroups)
    {
      vehicleGroup.setCount(count);
      vehicleGroup.setGroup(group);
      vehicleGroup.setMovements(new Movements(movements));
    }
    getMapViewer().repaint();
    trafficSimulator.setModified(true);
  }

  public class UndoEdges extends BasicUndoableEdit
  {
    private final List<Edge> edges;
    private final List values;
    private final int speed;
    private final int lanes;
    private final int delay;

    private UndoEdges(List<Edge> edges, int speed, int lanes, int delay)
    {
      this.edges = edges;
      this.speed = speed;
      this.lanes = lanes;
      this.delay = delay;
      this.values =  new ArrayList(edges.size() * 3);
      for (Edge edge : edges)
      {
        values.add(edge.getSpeed());
        values.add(edge.getLanes());
        values.add(edge.getDelay());
      }
    }

    @Override
    public void undo() throws CannotUndoException
    {
      int i = 0;
      for (Edge edge : edges)
      {
        edge.setSpeed((int)values.get(i));
        edge.setLanes((int)values.get(i + 1));
        edge.setDelay((int)values.get(i + 2));
        i += 3;
      }
      getMapViewer().repaint();
      trafficSimulator.setModified(true);
    }

    @Override
    public void redo() throws CannotRedoException
    {
      changeEdges(edges, speed, lanes, delay);
    }

    @Override
    public void die()
    {
      edges.clear();
      values.clear();
    }
  }

  public class UndoLocation extends BasicUndoableEdit
  {
    private final Location location;
    private final String name;
    private final String label;
    private final boolean origin;
    private final String oldName;
    private final String oldLabel;
    private final boolean oldOrigin;

    private UndoLocation(Location location, String name, String label,
      boolean origin)
    {
      this.location = location;
      this.name = name;
      this.label = label;
      this.origin = origin;
      this.oldName = location.getName();
      this.oldLabel = location.getLabel();
      this.oldOrigin = location.isOrigin();
    }

    @Override
    public void undo() throws CannotUndoException
    {
      location.setName(oldName);
      location.setLabel(oldLabel);
      location.setOrigin(oldOrigin);
      getMapViewer().repaint();
      trafficSimulator.setModified(true);
    }

    @Override
    public void redo() throws CannotRedoException
    {
      location.setName(name);
      location.setLabel(label);
      location.setOrigin(origin);
      getMapViewer().repaint();
      trafficSimulator.setModified(true);
    }
  }

  public class UndoVehicleGroups extends BasicUndoableEdit
  {
    private final List<VehicleGroup> vehicleGroups;
    private final List values;
    private final int count;
    private final String group;
    private final Movements movements;

    private UndoVehicleGroups(List<VehicleGroup> vehicleGroups,
      int count, String group, Movements movements)
    {
      this.vehicleGroups = vehicleGroups;
      this.count = count;
      this.group = group;
      this.movements = movements;
      this.values =  new ArrayList(vehicleGroups.size() * 3);
      for (VehicleGroup vehicleGroup : vehicleGroups)
      {
        values.add(vehicleGroup.getCount());
        values.add(vehicleGroup.getGroup());
        values.add(vehicleGroup.getMovements());
      }
    }

    @Override
    public void undo() throws CannotUndoException
    {
      int i = 0;
      for (VehicleGroup vehicleGroup : vehicleGroups)
      {
        vehicleGroup.setCount((int)values.get(i));
        vehicleGroup.setGroup((String)values.get(i + 1));
        vehicleGroup.setMovements((Movements)values.get(i + 2));
        i += 3;
      }
      getMapViewer().repaint();
      trafficSimulator.setModified(true);
    }

    @Override
    public void redo() throws CannotRedoException
    {
      changeVehicleGroups(vehicleGroups, count, group, movements);
    }

    @Override
    public void die()
    {
      vehicleGroups.clear();
      values.clear();
    }
  }
}
