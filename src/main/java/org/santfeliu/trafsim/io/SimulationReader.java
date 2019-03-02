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

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import javax.vecmath.Point3d;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.santfeliu.trafsim.GenericLayer;
import org.santfeliu.trafsim.Group;
import org.santfeliu.trafsim.Locations;
import org.santfeliu.trafsim.RoadGraph;
import org.santfeliu.trafsim.Simulation;
import org.santfeliu.trafsim.Vehicles;
import org.santfeliu.trafsim.geom.LineString;
import org.santfeliu.trafsim.geom.Point;
import org.santfeliu.trafsim.geom.Polygon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author realor
 */
public class SimulationReader
{
  private final InputStream is;

  public SimulationReader(InputStream is)
  {
    this.is = is;
  }

  public Simulation read() throws IOException
  {
    Simulation simulation = new Simulation();
    try
    {
      DocumentBuilderFactory factory =
      DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(is);
      Element root = doc.getDocumentElement();
      simulation.setTitle(getString(root, "title"));
      String srsName = getString(root, "srs");
      if (srsName != null && srsName.length() > 0)
      {
        simulation.setSrsName(srsName);
      }

      Element graphElement = getElement(root, "road-graph");
      if (graphElement != null)
      {
        RoadGraph roadGraph = simulation.getRoadGraph();
        NodeList edgeList = graphElement.getElementsByTagName("edge");
        for (int i = 0; i < edgeList.getLength(); i++)
        {
          Element edgeElement = (Element)edgeList.item(i);
          Element lineStringElement = getElement(edgeElement, "line-string");
          LineString lineString = getLineString(lineStringElement);
          int speed = getInteger(edgeElement, "speed", 50);
          int lanes = getInteger(edgeElement, "lanes", 1);
          roadGraph.newEdge(lineString, speed, lanes).add();
        }
      }

      Element layersElement = getElement(root, "layers");
      if (layersElement != null)
      {
        NodeList layerList = layersElement.getElementsByTagName("layer");
        for (int i = 0; i < layerList.getLength(); i++)
        {
          Element layerNode = (Element)layerList.item(i);
          String label = layerNode.getAttribute("label");
          GenericLayer layer = new GenericLayer(label, Color.LIGHT_GRAY);
          simulation.addGenericLayer(layer);
          NodeList geomElements = layerNode.getElementsByTagName("*");
          for (int j = 0; j < geomElements.getLength(); j++)
          {
            Element geomElem = (Element)geomElements.item(j);
            String geomType = geomElem.getNodeName();
            if ("point".equals(geomType))
            {
              layer.newFeature(getPoint(geomElem)).add();
            }
            else if ("line-string".equals(geomType))
            {
              layer.newFeature(getLineString(geomElem)).add();
            }
            else if ("polygon".equals(geomType))
            {
              layer.newFeature(getPolygon(geomElem)).add();
            }
          }
        }
      }

      Element groupsElement = getElement(root, "groups");
      if (groupsElement != null)
      {
        Map<String, Group> groups = simulation.getGroups();
        NodeList groupList =
          groupsElement.getElementsByTagName("group");
        for (int i = 0; i < groupList.getLength(); i++)
        {
          Element groupElement = (Element)groupList.item(i);
          String groupName = groupElement.getAttribute("name");
          Group group = new Group(groupName);
          groups.put(groupName, group);
          NodeList fromList =
            groupElement.getElementsByTagName("location");
          for (int j = 0; j < fromList.getLength(); j++)
          {
            Element fromElement = (Element)fromList.item(j);
            String locationName = fromElement.getAttribute("name");
            if (locationName == null) locationName = "???";
            double factor = Double.parseDouble(fromElement.getAttribute("factor"));
            group.addJourney(locationName, factor);
          }
        }
      }

      Element locationsElement = getElement(root, "locations");
      if (locationsElement != null)
      {
        Locations locations = simulation.getLocations();
        NodeList locationList =
          locationsElement.getElementsByTagName("location");
        for (int i = 0; i < locationList.getLength(); i++)
        {
          Element locationElement = (Element)locationList.item(i);
          Element pointElement = getElement(locationElement, "point");
          Point point = getPoint(pointElement);
          String name = getString(locationElement, "name");
          String label = getString(locationElement, "label");
          String origin = getString(locationElement, "origin");
          boolean isOrigin = "true".equals(origin);
          locations.newLocation(name, label, point, isOrigin).add();
        }
      }


      Element vehiclesElement = getElement(root, "vehicles");
      if (vehiclesElement != null)
      {
        Vehicles vehicles = simulation.getVehicles();
        NodeList vehicleList =
          vehiclesElement.getElementsByTagName("vehicle-group");
        for (int i = 0; i < vehicleList.getLength(); i++)
        {
          Element vehicleElement = (Element)vehicleList.item(i);
          Element pointElement = getElement(vehicleElement, "point");
          Point point = getPoint(pointElement);
          int count = getInteger(vehicleElement, "count", 1);
          String group = getString(vehicleElement, "group");
          vehicles.newVehicleGroup(point, count, group).add();
        }
      }
    }
    catch (Exception ex)
    {
      throw new IOException(ex);
    }
    finally
    {
      is.close();
    }
    return simulation;
  }

  protected Element getElement(Element base, String name)
  {
    NodeList list = base.getElementsByTagName(name);
    if (list.getLength() == 0) return null;
    Element child = (Element)list.item(0);
    return child;
  }

  protected String getString(Element element, String name)
  {
    Element child = getElement(element, name);
    if (child == null) return null;
    return child.getTextContent();
  }

  protected double getDouble(Element base, String name, double defValue)
  {
    Element child = getElement(base, name);
    if (child == null) return defValue;
    return Double.parseDouble(child.getTextContent());
  }

  protected int getInteger(Element base, String name, int defValue)
  {
    Element child = getElement(base, name);
    if (child == null) return defValue;
    return Integer.parseInt(child.getTextContent());
  }

  protected Point getPoint(Element element)
  {
    String text = element.getTextContent();
    Point3d position = new Point3d();
    String coords[] = text.split(",");
    position.x = Double.parseDouble(coords[0]);
    position.y = Double.parseDouble(coords[1]);
    if (coords.length == 2)
    {
      position.z = Double.parseDouble(coords[2]);
    }
    return new Point(position);
  }

  protected LineString getLineString(Element element)
  {
    String text = element.getTextContent();
    String[] vertexArray = text.split(" ");
    ArrayList<Point3d> vertices = new ArrayList<Point3d>();
    for (String vertex : vertexArray)
    {
      Point3d point = new Point3d();
      String coords[] = vertex.split(",");
      point.x = Double.parseDouble(coords[0]);
      point.y = Double.parseDouble(coords[1]);
      if (coords.length == 2)
      {
        point.z = Double.parseDouble(coords[2]);
      }
      vertices.add(point);
    }
    return new LineString(vertices);
  }

  protected Polygon getPolygon(Element element)
  {
    String text = element.getTextContent();
    String[] vertexArray = text.split(" ");
    ArrayList<Point3d> vertices = new ArrayList<Point3d>();
    for (String vertex : vertexArray)
    {
      Point3d point = new Point3d();
      String coords[] = vertex.split(",");
      point.x = Double.parseDouble(coords[0]);
      point.y = Double.parseDouble(coords[1]);
      if (coords.length == 2)
      {
        point.z = Double.parseDouble(coords[2]);
      }
      vertices.add(point);
    }
    return new Polygon(vertices);
  }
}
