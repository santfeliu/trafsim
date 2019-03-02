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
package org.santfeliu.trafsim.geom;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.Box;

/**
 *
 * @author realor
 */
public class Polygon extends Geometry
{
  private final List<Point3d> vertices = new ArrayList<Point3d>();

  public Polygon(List<Point3d> points)
  {
    this.vertices.addAll(points);
  }

  public List<Point3d> getVertices()
  {
    return vertices;
  }

  public double getLength()
  {
    double length = 0;
    for (int i = 0; i < vertices.size() - 1; i++)
    {
      Point3d pt1 = vertices.get(i);
      Point3d pt2 = vertices.get(i + 1);
      length += pt1.distance(pt2);
    }
    return length;
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append("Polygon(");
    for (int i = 0; i < vertices.size(); i++)
    {
      Point3d point = vertices.get(i);
      if (i > 0) buffer.append(", ");
      buffer.append("Point(").
        append(point.x).append(", ").
        append(point.y).append(", ").
        append(point.z).append(")");
    }
    buffer.append(")");
    return buffer.toString();
  }

  @Override
  public void transform(Matrix4d matrix)
  {
    for (Point3d vertex : vertices)
    {
      matrix.transform(vertex);
    }
    updateBoundingBox();
  }

  @Override
  protected void extend(Box box)
  {
    for (Point3d vertex : vertices)
    {
      box.extend(vertex);
    }
  }
}
