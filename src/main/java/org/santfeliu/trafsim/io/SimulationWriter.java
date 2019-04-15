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
package org.santfeliu.trafsim.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.GenericLayer;
import org.santfeliu.trafsim.GenericLayer.GenericFeature;
import org.santfeliu.trafsim.Group;
import org.santfeliu.trafsim.Group.Journey;
import org.santfeliu.trafsim.Locations.Location;
import org.santfeliu.trafsim.Movements;
import org.santfeliu.trafsim.RoadGraph;
import org.santfeliu.trafsim.RoadGraph.Edge;
import org.santfeliu.trafsim.Simulation;
import org.santfeliu.trafsim.Vehicles.VehicleGroup;
import org.santfeliu.trafsim.geom.Geometry;
import org.santfeliu.trafsim.geom.LineString;
import org.santfeliu.trafsim.geom.Point;
import org.santfeliu.trafsim.geom.Polygon;

/**
 *
 * @author realor
 */
public class SimulationWriter extends XMLWriter
{
  private static final String VERSION = "1.1";

  public SimulationWriter(OutputStream os) throws IOException
  {
    super(os);
  }

  public void write(Simulation simulation) throws IOException
  {
    try
    {
      writePreambule();
      startTag("traffic-simulation");
      writeAttribute("version", VERSION);

      String title = simulation.getTitle();
      if (title == null) title = "Simulation";

      startTag("title");
      writeText(title);
      endTag("title");

      startTag("srs");
      writeText(simulation.getSrsName());
      endTag("srs");

      startTag("duration");
      writeText(simulation.getDuration());
      endTag("duration");

      startTag("road-graph");
      RoadGraph roadGraph = simulation.getRoadGraph();
      Collection<Edge> edges = roadGraph.getFeatures();
      for (Edge edge : edges)
      {
        writeEdge(edge);
      }
      endTag("road-graph");

      startTag("layers");
      List<GenericLayer> layers = simulation.getGenericLayers();
      for (GenericLayer layer : layers)
      {
        writeLayer(layer);
      }
      endTag("layers");

      startTag("groups");
      for (Group group : simulation.getGroups().values())
      {
        startTag("group");
        writeAttribute("name", group.getName());
        for (Journey journey : group.getJourneys())
        {
          startTag("location");
          writeAttribute("name", journey.getLocationName());
          writeAttribute("factor", journey.getFactor());
          endTag("location");
        }
        endTag("group");
      }
      endTag("groups");

      startTag("locations");
      Collection<Location> locations = simulation.getLocations().getFeatures();
      for (Location location : locations)
      {
        writeLocation(location);
      }
      endTag("locations");

      startTag("vehicles");
      Collection<VehicleGroup> vehicles = simulation.getVehicles().getFeatures();
      for (VehicleGroup vehicleGroup : vehicles)
      {
        writeVehicleGroup(vehicleGroup);
      }
      endTag("vehicles");

      endTag("traffic-simulation");

    }
    finally
    {
      close();
    }
  }

  protected void writeEdge(Edge edge)
  {
    startTag("edge");

    writeGeometry(edge.getLineString());

    startTag("speed");
    writeText(edge.getSpeed());
    endTag("speed");

    startTag("lanes");
    writeText(edge.getLanes());
    endTag("lanes");

    startTag("stop");
    writeText(edge.getStopFactor());
    endTag("stop");

    endTag("edge");
  }

  protected void writeLayer(GenericLayer layer)
  {
    startTag("layer");
    writeAttribute("label", layer.getLabel());

    Collection<GenericFeature> features = layer.getFeatures();
    for (GenericFeature feature : features)
    {
      writeGeometry(feature.getGeometry());
    }

    endTag("layer");
  }

  protected void writeLocation(Location location)
  {
    startTag("location");

    startTag("name");
    writeText(location.getName());
    endTag("name");

    startTag("label");
    writeText(location.getLabel());
    endTag("label");

    startTag("point");
    writeGeometry(location.getPoint());
    endTag("point");

    startTag("origin");
    writeText(location.isOrigin());
    endTag("origin");

    endTag("location");
  }

  protected void writeVehicleGroup(VehicleGroup vehicleGroup)
  {
    startTag("vehicle-group");

    startTag("point");
    writeGeometry(vehicleGroup.getPoint());
    endTag("point");

    startTag("count");
    writeText(vehicleGroup.getCount());
    endTag("count");

    startTag("group");
    writeText(vehicleGroup.getGroup());
    endTag("group");

    Movements movements = vehicleGroup.getMovements();
    if (movements != null)
    {
      startTag("movements");
      Set<Map.Entry<String, Integer>> entrySet = movements.entrySet();
      for (Map.Entry<String, Integer> entry : entrySet)
      {
        startTag("location");
        writeAttribute("name", entry.getKey());
        writeAttribute("count", entry.getValue());
        endTag("location");
      }
      endTag("movements");
    }
    endTag("vehicle-group");
  }

  protected void writeGeometry(Geometry geometry)
  {
    if (geometry instanceof Point)
    {
      startTag("point");
      writePoint3d(((Point)geometry).getPosition());
      endTag("point");
    }
    else if (geometry instanceof LineString)
    {
      startTag("line-string");
      List<Point3d> vertices = ((LineString)geometry).getVertices();
      for (int i = 0; i < vertices.size(); i++)
      {
        if (i > 0) writer.print(" ");
        writePoint3d(vertices.get(i));
      }
      endTag("line-string");
    }
    else if (geometry instanceof Polygon)
    {
      startTag("polygon");
      List<Point3d> vertices = ((Polygon)geometry).getVertices();
      for (int i = 0; i < vertices.size(); i++)
      {
        if (i > 0) writer.print(" ");
        writePoint3d(vertices.get(i));
      }
      endTag("polygon");
    }
  }
}
