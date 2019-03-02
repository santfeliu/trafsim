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
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.RoadGraph.Edge;
import org.santfeliu.trafsim.geom.Geometry;

/**
 *
 * @author realor
 */
public class RoadGraph extends Layer<Edge>
{
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
        connected = edge.getTargetNode() == next;
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
    Node sourceNode;
    Node targetNode;
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

    public Node getSourceNode()
    {
      return sourceNode;
    }

    public Node getTargetNode()
    {
      return targetNode;
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
      if (isLinked())
      {
        unlinkNodes();
        Collections.reverse(lineString.getVertices());
        linkNodes();
      }
      else
      {
        Collections.reverse(lineString.getVertices());
      }
    }

    @Override
    public Geometry getGeometry()
    {
      return lineString;
    }

    @Override
    public void setGeometry(Geometry geometry)
    {
      if (geometry instanceof LineString)
      {
        if (isLinked())
        {
          unlinkNodes();
          lineString = (LineString)geometry;
          linkNodes();
        }
        else
        {
          lineString = (LineString)geometry;
        }
      }
    }

    @Override
    public Layer getLayer()
    {
      return RoadGraph.this;
    }

    @Override
    public void add()
    {
      if (!isLinked())
      {
        linkNodes();
        edges.add(this);
      }
    }

    @Override
    public void remove()
    {
      if (isLinked())
      {
        unlinkNodes();
        edges.remove(this);
      }
    }
    
    @Override
    public boolean isRemoved()
    {
      return !isLinked();
    }

    @Override
    public void transform(Matrix4d matrix)
    {
      if (isLinked())
      {
        unlinkNodes();
        super.transform(matrix);
        linkNodes();
      }
      else
      {
        super.transform(matrix);
      }
    }

    @Override
    public String toString()
    {
      return "Edge(source: " + sourceNode + ", target: " + targetNode + ")";
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

    boolean isLinked()
    {
      return sourceNode != null;
    }

    void linkNodes()
    {
      List<Point3d> vertices = lineString.getVertices();
      if (vertices.size() < 1) return;

      // link source node
      Point3d startPoint = vertices.get(0);
      sourceNode = nodes.get(startPoint);
      if (sourceNode == null)
      {
        startPoint = new Point3d(startPoint); //immutable copy
        sourceNode = new Node(new Point(startPoint));
        nodes.put(startPoint, sourceNode);
      }
      if (!sourceNode.outEdges.contains(this))
      {
        sourceNode.outEdges.add(this);
      }
      
      // link target node
      Point3d endPoint = vertices.get(vertices.size() - 1);
      targetNode = nodes.get(endPoint);
      if (targetNode == null)
      {
        endPoint = new Point3d(endPoint); // immutable copy
        targetNode = new Node(new Point(endPoint));
        nodes.put(endPoint, targetNode);
      }
      if (!targetNode.inEdges.contains(this))
      {
        targetNode.inEdges.add(this);
      }
    }

    void unlinkNodes()
    {
      if (sourceNode != null)
      {
        sourceNode.outEdges.remove(this);
        if (sourceNode.inEdges.isEmpty() && sourceNode.outEdges.isEmpty())
        {
          nodes.remove(sourceNode.point.getPosition());
        }
        sourceNode = null;
      }
      if (targetNode != null)
      {
        targetNode.inEdges.remove(this);
        if (targetNode.inEdges.isEmpty() && targetNode.outEdges.isEmpty())
        {
          nodes.remove(targetNode.point.getPosition());
        }
        targetNode = null;
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
  public void clear()
  {
    nodes.clear();
    for (Edge edge : edges)
    {
      edge.sourceNode = null;
      edge.targetNode = null;
    }
    edges.clear();
  }
  
  public void snapToGrid(double gridSize)
  {
    ArrayList<Edge> edgeList = new ArrayList<Edge>();
    edgeList.addAll(edges);
    nodes.clear();
    edges.clear();
    for (Edge edge : edgeList)
    {
      List<Point3d> vertices = edge.getLineString().getVertices();
      Point3d startPoint = vertices.get(0);
      Point3d endPoint = vertices.get(vertices.size() - 1);
      round(startPoint, gridSize);
      round(endPoint, gridSize);
      if (!startPoint.equals(endPoint))   
      {
        edge.linkNodes();
        edges.add(edge);
      }
    }
  }
  
  private void round(Point3d point, double gridSize)
  {
    point.x = Math.round(point.x / gridSize) * gridSize;
    point.y = Math.round(point.y / gridSize) * gridSize;
    point.z = Math.round(point.z / gridSize) * gridSize;
  }
}
