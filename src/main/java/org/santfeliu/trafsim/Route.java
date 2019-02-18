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
import java.util.List;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.RoadGraph.Edge;
import org.santfeliu.trafsim.geom.LineString;

/**
 *
 * @author realor
 */
public class Route
{
  private final List<Section> sections;

  public Route()
  {
    this.sections = new ArrayList<Section>();
  }
  
  public Point3d getOrigin()
  {
    if (sections.isEmpty()) return null;
    return sections.get(0).getVertices().get(0);
  }

  public Point3d getDestination()
  {
    if (sections.isEmpty()) return null;
    List<Point3d> vertices = sections.get(sections.size() - 1).getVertices();
    return vertices.get(vertices.size() - 1);
  }
  
  public Section addInitialSection(Edge startEdge, Point3d point, int index)
  {
    List<Point3d> startPoints = new ArrayList<Point3d>();
    startPoints.add(point);
    List<Point3d> vertices = startEdge.getLineString().getVertices();
    startPoints.addAll(vertices.subList(index + 1, vertices.size()));
    Section section = new Section(new LineString(startPoints), startEdge);
    sections.add(section);
    return section;
  }

  public Section addEndingSection(Edge endEdge, Point3d point, int index)
  {
    List<Point3d> endPoints = new ArrayList<Point3d>();
    List<Point3d> vertices = endEdge.getLineString().getVertices();
    endPoints.addAll(vertices.subList(0, index + 1));
    endPoints.add(point);
    Section section = new Section(new LineString(endPoints), endEdge);
    sections.add(section);
    return section;
  }
  
  public Section addSection(Edge edge)
  {
    LineString lineString = edge.getLineString();
    Section section = new Section(lineString, edge);
    sections.add(section);
    return section;
  }
  
  public Section addSection(Edge edge,
    Point3d p1, int index1, Point3d p2, int index2)
  {
    List<Point3d> points = new ArrayList<Point3d>();
    List<Point3d> vertices = edge.getLineString().getVertices();
    points.add(p1);
    points.addAll(vertices.subList(index1 + 1, index2 + 1));
    points.add(p2);
    Section section = new Section(new LineString(points), edge);
    sections.add(section);
    return section;
  }
  
  public boolean isEmpty()
  {
    return sections.isEmpty();
  }
  
  public class Section
  {
    LineString lineString;
    Edge edge;
   
    Section(LineString lineString, Edge edge)
    {
      this.lineString = lineString;
      this.edge = edge;
    }
    
    public LineString getLineString()
    {
      return lineString;
    }
    
    public List<Point3d> getVertices()
    {
      return lineString.getVertices();
    }
    
    public Edge getEdge()
    {
      return edge;
    }
    
    public double getLength()
    {
      return lineString.getLength();
    }
  }
  
  public List<Section> getSections()
  {
    return sections;
  }
    
  public double getLength()
  {
    double length;
    if (sections.isEmpty())
    {
      length = Double.POSITIVE_INFINITY;
    }
    else
    {
      length = 0;
      for (Section section : sections)
      {
        length += section.getLength();
      }
    }
    return length;
  }  
  
  public LineString getLineString()
  {
    int lastSection = sections.size() - 1;
    List<Point3d> vertices = new ArrayList<Point3d>();
    for (int i = 0; i < sections.size(); i++)
    {
      Section section = sections.get(i);
      vertices.addAll(section.getVertices());
      if (i < lastSection) vertices.remove(vertices.size() - 1);
    }
    return new LineString(vertices);
  }
}
