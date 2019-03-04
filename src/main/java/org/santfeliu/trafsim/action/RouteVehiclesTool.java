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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.Group;
import org.santfeliu.trafsim.Group.Journey;
import org.santfeliu.trafsim.Indicators;
import org.santfeliu.trafsim.Locations;
import org.santfeliu.trafsim.Locations.Location;
import org.santfeliu.trafsim.MapViewer;
import org.santfeliu.trafsim.MapViewer.Painter;
import org.santfeliu.trafsim.RoadGraph;
import org.santfeliu.trafsim.RoadGraph.Edge;
import org.santfeliu.trafsim.Route;
import org.santfeliu.trafsim.Route.Section;
import org.santfeliu.trafsim.RouteFinder;
import org.santfeliu.trafsim.RouteMeter;
import org.santfeliu.trafsim.Simulation;
import org.santfeliu.trafsim.TrafficSimulator;
import org.santfeliu.trafsim.Vehicles;
import org.santfeliu.trafsim.Vehicles.VehicleGroup;

/**
 *
 * @author realor
 */
public class RouteVehiclesTool extends Tool implements Painter
{
  private Router router;
  private final RouteMeter routeMeter = new RouteMeter();
  private final BasicStroke barStroke = new BasicStroke(7,
    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

  public RouteVehiclesTool(TrafficSimulator trafficSimulator)
  {
    super(trafficSimulator);
  }

  @Override
  public String getName()
  {
    return "routeVehiclesTool";
  }

  @Override
  public void start()
  {
    trafficSimulator.setIndicatorsVisible(true);
    MapViewer mapViewer = getMapViewer();
    mapViewer.setPainter(this);
    router = new Router();
    router.start();
    info("routing");
  }

  @Override
  public void stop()
  {
    MapViewer mapViewer = getMapViewer();
    mapViewer.setPainter(null);
    router.abort();
    router = null;
  }

  @Override
  public void paint(MapViewer mapViewer, Graphics2D g)
  {
    if (router != null)
    {
      g.setFont(g.getFont().deriveFont(12.0f));
      if (router.isAlive())
      {
        paintProgressBar(g);
      }
    }
  }

  void paintProgressBar(Graphics2D g)
  {
    MapViewer mapViewer = getMapViewer();
    int width = mapViewer.getWidth();
    int height = mapViewer.getHeight();
    int margin = width / 10;
    int barWidth = width - 2 * margin;
    double factor = (double)router.vehicleGroupIndex / router.vehicleGroupCount;
    int fillWidth = (int)(barWidth * factor);
    int y = height - height / 10;
    g.setStroke(barStroke);
    g.setColor(Color.LIGHT_GRAY);
    g.drawLine(margin, y, margin + barWidth, y);
    g.setColor(new Color(60, 60, 60));
    g.drawLine(margin, y, margin + fillWidth, y);
    g.setColor(Color.BLACK);
    g.drawString(router.vehicleGroupIndex + " / " + router.vehicleGroupCount,
      margin, y - 10);
  }

  class Router extends Thread
  {
    RouteFinder routeFinder;
    int vehicleGroupIndex;
    int vehicleGroupCount;
    boolean abort;

    @Override
    public void run()
    {
      Simulation simulation = getSimulation();
      Indicators indicators = simulation.getIndicators();
      indicators.reset();
      RoadGraph roadGraph = simulation.getRoadGraph();
      routeFinder = new RouteFinder(roadGraph, routeMeter);
      for (Edge edge : roadGraph.getEdges())
      {
        edge.getIndicators().reset();
      }
      Vehicles vehicles = simulation.getVehicles();
      for (VehicleGroup vehicleGroup : vehicles.getFeatures())
      {
        vehicleGroup.getIndicators().reset();
      }
      MapViewer mapViewer = getMapViewer();
      Locations locations = simulation.getLocations();
      Map<String, Group> groups = simulation.getGroups();

      vehicleGroupCount = vehicles.getFeatures().size();
      vehicleGroupIndex = 0;
      long millis0 = System.currentTimeMillis();
      while (vehicleGroupIndex < vehicleGroupCount && !abort)
      {
        VehicleGroup vehicleGroup = vehicles.getVehicleGroup(vehicleGroupIndex);
        Point3d origin = vehicleGroup.getPoint().getPosition();
        int vehicleCount = vehicleGroup.getCount();
        String groupName = vehicleGroup.getGroup();
        routeFinder.clear();
        routeFinder.setOrigin(origin, Double.POSITIVE_INFINITY);
        Group group = groups.get(groupName);
        if (group != null)
        {
          Collection<Journey> journeys = group.getJourneys();
          Iterator<Journey> jiter = journeys.iterator();
          while (jiter.hasNext() && !abort)
          {
            Journey journey = jiter.next();
            String locationName = journey.getLocationName();
            Location location = locations.getLocation(locationName);
            if (location.isDestination())
            {
              Point3d destination = location.getPoint().getPosition();
              routeFinder.setDestination(destination, Double.POSITIVE_INFINITY);
              Route route = routeFinder.getRoute();
              int journeyCount = (int)(vehicleCount * journey.getFactor());
              VehicleGroup.Indicators vehicleInd = vehicleGroup.getIndicators();
              vehicleInd.journeyCount += journeyCount;
              if (route.isEmpty())
              {
                // unrouted journey
                vehicleInd.unroutedCount += journeyCount;
              }
              else
              {
                // routed journey
                vehicleInd.routedCount += journeyCount;
                vehicleInd.distance += route.getLength() * journeyCount;
                vehicleInd.time += routeMeter.getTime(route) * journeyCount;
                for (Section section : route.getSections())
                {
                  Edge edge = section.getEdge();
                  Edge.Indicators edgeInd = edge.getIndicators();
                  edgeInd.vehicleCount += journeyCount;
                  indicators.update(edge);
                }
              }
            }
          }
        }
        vehicleGroupIndex++;
        long millis1 = System.currentTimeMillis();
        if (millis1 - millis0 > 500)
        {
          millis0 = millis1;
          mapViewer.repaint();
        }
        indicators.update(vehicleGroup);
        indicators.updateAverages();
      }
      info("completed");
      mapViewer.repaint();
    }

    void abort()
    {
      abort = true;
    }
  }
}
