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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.RoadGraph.Edge;
import org.santfeliu.trafsim.RoadGraph.Node;

/**
 *
 * @author realor
 */
public class RouteFinder
{
  private final RoadGraph roadGraph;
  private final RouteMeter routeMeter;
  private final HashMap<Node, NodeInfo> infos = new HashMap<Node, NodeInfo>();
  private final PickInfo startPick = new PickInfo();
  private final PickInfo endPick = new PickInfo();
  private Node originNode;
  private Node destinationNode;

  public RouteFinder(RoadGraph roadGraph, RouteMeter routeTimer)
  {
    this.roadGraph = roadGraph;
    this.routeMeter = routeTimer;
  }

  public void setOrigin(Point3d origin, double tolerance)
  {
    startPick.clear();
    if (Finder.findByPoint(roadGraph, origin, tolerance, startPick))
    {
      Edge startEdge = (Edge)startPick.getFeature();
      findRoutesFrom(startEdge.getTarget());
    }
  }

  public void setOrigin(Node originNode)
  {
    startPick.clear();
    findRoutesFrom(originNode);
  }

  public void setDestination(Point3d destination, double tolerance)
  {
    destinationNode = null;
    endPick.clear();
    if (Finder.findByPoint(roadGraph, destination, tolerance, endPick))
    {
      Edge endEdge = (Edge)endPick.feature;
      destinationNode = endEdge.getSource();
    }    
  }

  public void setDestination(Node destinationNode)
  {
    endPick.clear();
    this.destinationNode = destinationNode;
  }
  
  public void clear()
  {
    startPick.clear();
    endPick.clear();
    originNode = null;
    destinationNode = null;
  }
  
  public Route getRoute()
  {
    if (originNode == null || destinationNode == null) return null;
    
    Route route = new Route();
    
    NodeInfo nodeInfo = infos.get(destinationNode);
    if (isRouteInFirstEdge() && isForwardRoute())
    {
      Edge edge = (Edge)startPick.getFeature();
      route.addSection(edge, 
        startPick.getOnFeaturePoint(), 
        startPick.getEdgeSegmentIndex(), 
        endPick.getOnFeaturePoint(), 
        endPick.getEdgeSegmentIndex());
    }
    else if (nodeInfo.minTime < Double.POSITIVE_INFINITY)
    {
      if (startPick.getFeature() != null)
      {
        route.addInitialSection((Edge)startPick.getFeature(), 
          startPick.getOnFeaturePoint(), startPick.getEdgeSegmentIndex());
      }
      List<Edge> reversedEdges = new ArrayList<Edge>();
      while (nodeInfo.previous != null)
      {
        reversedEdges.add(nodeInfo.previous);
        nodeInfo = infos.get(nodeInfo.previous.getSource());
      }
      for (int i = reversedEdges.size() - 1; i >= 0; i--)
      {
        route.addSection(reversedEdges.get(i));
      }
      if (endPick.getFeature() != null)
      {
        route.addEndingSection((Edge)endPick.getFeature(), 
          endPick.getOnFeaturePoint(), endPick.getEdgeSegmentIndex());
      }
    }
    return route;
  }
  
  public Node getOriginNode()
  {
    return originNode;
  }

  public Node getDestinationNode()
  {
    return destinationNode;
  }
  
  public Point3d getOrigin()
  {
    if (startPick.getFeature() != null)
    {
      return startPick.onFeaturePoint;
    }
    if (originNode != null)
    {
      return originNode.getPoint().getPosition();
    }
    return null;
  }

  public Point3d getDestination()
  {
    if (endPick.getFeature() != null)
    {
      return endPick.onFeaturePoint;
    }
    if (destinationNode != null)
    {
      return destinationNode.getPoint().getPosition();
    }
    return null;
  }

  protected void findRoutesFrom(Node originNode)
  {
    this.originNode = originNode;
    infos.clear();
    for (Node node : roadGraph.getNodes())
    {
      infos.put(node, new NodeInfo(node));
    }
    ArrayList<Node> list = new ArrayList<Node>();
    list.add(originNode);
    NodeInfo nodeInfo = infos.get(originNode);
    nodeInfo.minTime = 0;

    while (!list.isEmpty())
    {
      Node node = list.remove(list.size() - 1);
      nodeInfo = infos.get(node);

      for (Edge edge : node.outEdges)
      {
        Node nextNode = edge.target;
        NodeInfo nextNodeInfo = infos.get(nextNode);

        double time = nodeInfo.minTime + routeMeter.getTime(edge);
        if (time < nextNodeInfo.minTime)
        {
          nextNodeInfo.minTime = time;
          nextNodeInfo.previous = edge;
          list.add(nextNode);
        }
      }
    }
  }

  protected boolean isRouteInFirstEdge()
  {
    Edge startEdge = (Edge)startPick.getFeature();
    Edge endEdge = (Edge)endPick.getFeature();
    return startEdge == endEdge && startEdge != null;
  }
  
  protected boolean isForwardRoute()
  {
    // assume route in first edge
    if (startPick.getEdgeSegmentIndex() < endPick.getEdgeSegmentIndex())
      return true;
    
    if (startPick.getEdgeSegmentIndex() == endPick.getEdgeSegmentIndex())
    {
      Edge startEdge = (Edge)startPick.getFeature();
      List<Point3d> vertices = startEdge.getLineString().getVertices();
      Point3d startPoint = vertices.get(startPick.getEdgeSegmentIndex());
      Point3d p1 = startPick.getOnFeaturePoint();
      Point3d p2 = endPick.getOnFeaturePoint();
      return startPoint.distance(p1) < startPoint.distance(p2);        
    }
    return false;    
  }
  
  protected class NodeInfo
  {
    Node node;
    double minTime;
    Edge previous;

    public NodeInfo(Node node)
    {
      this.node = node;
      minTime = Double.POSITIVE_INFINITY;
    }

    @Override
    public String toString()
    {
      return "{" + node.getPoint().getPosition() +
         ", " + minTime + ", " + node.getOutEdges().size() + "}";
    }
  }
}
