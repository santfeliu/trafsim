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

import org.santfeliu.trafsim.geom.LineString;
import org.santfeliu.trafsim.geom.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.RoadGraph.Edge;
import org.santfeliu.trafsim.geom.Geometry;

/**
 *
 * @author realor
 */
public class RoadGraph extends Layer<Edge>
{
  private int roundDecimals = 1;
  private final HashMap<Point3d, Node> nodes = new HashMap<Point3d, Node>();
  private final ArrayList<Edge> edges = new ArrayList<Edge>();

  public class Node
  {
    Point point;
    List<Edge> inEdges = new ArrayList<Edge>();
    List<Edge> outEdges = new ArrayList<Edge>();

    public Node(Point point)
    {
      this.point = point;
    }

    public Point getPoint()
    {
      return point;
    }

    public List<Edge> getInEdges()
    {
      return inEdges;
    }

    public List<Edge> getOutEdges()
    {
      return outEdges;
    }

    public boolean isConnectedTo(Node next)
    {
      boolean connected = false;
      Iterator<Edge> iter = outEdges.iterator();
      while (iter.hasNext() && !connected)
      {
        Edge edge = iter.next();
        connected = edge.getTarget() == next;
      }
      return connected;
    }

    @Override
    public String toString()
    {
      Point3d position = point.getPosition();
      return "Node(" + position.x + ", " + position.y + ", " + position.z +
        ", inEdges: " + inEdges.size() + ", outEdges: " + outEdges.size() + ")";
    }
  }

  public class Edge extends Feature
  {
    Node source;
    Node target;
    LineString lineString;
    int speed; // Km/h
    int lanes;
    Indicators indicators;

    Edge(LineString lineString, int speed, int lanes)
    {
      this.lineString = lineString;
      this.speed = speed;
      this.lanes = lanes;
    }

    public LineString getLineString()
    {
      return lineString;
    }

    public Node getSource()
    {
      return source;
    }

    public Node getTarget()
    {
      return target;
    }

    public int getSpeed()
    {
      return speed;
    }

    public void setSpeed(int speed)
    {
      this.speed = speed;
    }

    public void setLanes(int lanes)
    {
      this.lanes = lanes;
    }

    public int getLanes()
    {
      return lanes;
    }

    public void reverse()
    {
      unlinkNodes();
      Collections.reverse(lineString.getVertices());
      linkNodes();
    }

    @Override
    public Geometry getGeometry()
    {
      return lineString;
    }

    @Override
    public String toString()
    {
      return "Edge(source: " + source + ", target: " + target + ")";
    }

    public Indicators getIndicators()
    {
      if (indicators == null) indicators = new Indicators();
      return indicators;
    }

    @Override
    public void loadAttributes(Map attributes)
    {
      attributes.put("SPEED", speed);
      attributes.put("LANES", lanes);
    }

    void linkNodes()
    {
      List<Point3d> vertices = lineString.getVertices();
      if (vertices.size() < 1) return;

      Point3d startPoint = round(vertices.get(0));
      Point3d endPoint = round(vertices.get(vertices.size() - 1));

      source = nodes.get(startPoint);
      if (source == null)
      {
        source = new Node(new Point(startPoint));
        nodes.put(startPoint, source);
      }
      target = nodes.get(endPoint);
      if (target == null)
      {
        target = new Node(new Point(endPoint));
        nodes.put(endPoint, target);
      }
      source.outEdges.add(this);
      target.inEdges.add(this);
    }

    void unlinkNodes()
    {
      source.outEdges.remove(this);
      target.inEdges.remove(this);

      if (source.inEdges.isEmpty() && source.outEdges.isEmpty())
      {
        nodes.remove(source.point.getPosition());
      }
      if (target.inEdges.isEmpty() && target.outEdges.isEmpty())
      {
        nodes.remove(target.point.getPosition());
      }
    }

    public class Indicators
    {
      static final double VEHICLE_LENGTH = 4.5; // meters
      static final double VEHICLE_SEPARATION = 2; // meters
      public int vehicleCount;

      /* vehicles / minute */
      public double getCapacity()
      {
        return lanes * (1000 * speed) /
          (60.0 * (VEHICLE_LENGTH + VEHICLE_SEPARATION));
      }

      public void reset()
      {
        vehicleCount = 0;
      }
    }
  }

  public int getRoundDecimals()
  {
    return roundDecimals;
  }

  public void setRoundDecimals(int roundDecimals)
  {
    this.roundDecimals = roundDecimals;
  }

  public Edge newEdge(LineString lineString, int speed, int lanes)
  {
    return new Edge(lineString, speed, lanes);
  }

  public Collection<Node> getNodes()
  {
    return nodes.values();
  }

  public Collection<Edge> getEdges()
  {
    return edges;
  }

  public int getNodeCount()
  {
    return nodes.size();
  }

  public int getEdgeCount()
  {
    return edges.size();
  }

  @Override
  public String getName()
  {
    return "RoadGraph";
  }

  @Override
  public Collection<Edge> getFeatures()
  {
    return edges;
  }

  @Override
  public void add(Edge edge)
  {
    edges.add(edge);
    edge.linkNodes();
  }

  @Override
  public boolean remove(Feature feature)
  {
    if (feature instanceof Edge)
    {
      Edge edge = (Edge)feature;
      edge.unlinkNodes();
      return edges.remove(edge);
    }
    return false;
  }

  @Override
  public void clear()
  {
    nodes.clear();
    edges.clear();
  }

  private Point3d round(Point3d pt)
  {
    double round = Math.pow(10, roundDecimals);
    pt.x = (Math.round(pt.x * round) / round);
    pt.y = (Math.round(pt.y * round) / round);
    pt.z = (Math.round(pt.z * round) / round);
    return pt;
  }
}
