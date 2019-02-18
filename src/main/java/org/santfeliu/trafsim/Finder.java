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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import org.santfeliu.trafsim.geom.Geometry;
import org.santfeliu.trafsim.geom.LineString;
import org.santfeliu.trafsim.geom.Point;
import org.santfeliu.trafsim.geom.Polygon;

/**
 *
 * @author realor
 */
public class Finder
{
  public static boolean findByPoint(Layer layer, Point3d worldPoint,
    double tolerance, PickInfo pick)
  {
    Point3d onEdge = new Point3d();
    Collection<Feature> features = layer.getFeatures();
    for (Feature feature : features)
    {
      Geometry geometry = feature.getGeometry();
      if (geometry instanceof Point)
      {
        Point point = (Point)geometry;
        Point3d position = point.getPosition();
        double distance = position.distance(worldPoint);
        if (distance <= tolerance && distance < pick.distance)
        {
          pick.worldPoint = worldPoint;
          pick.feature = feature;
          pick.onFeaturePoint.set(position);
          pick.distance = distance;
        }
      }
      else if (geometry instanceof LineString)
      {
        LineString lineString = (LineString)geometry;
        List<Point3d> vertices = lineString.getVertices();
        for (int i = 0; i < vertices.size() - 1; i++)
        {
          Point3d p1 = vertices.get(i);
          Point3d p2 = vertices.get(i + 1);
          double distance =
            pointToSegmentDistance(worldPoint, p1, p2, onEdge);
          if (distance <= tolerance && distance < pick.distance)
          {
            pick.worldPoint = worldPoint;
            pick.feature = feature;
            pick.onFeaturePoint.set(onEdge);
            pick.distance = distance;
            pick.edgeSegmentIndex = i;
          }
        }
      }
      else if (geometry instanceof Polygon)
      {
        // TODO: select feature if it is inside polygon
      }
    }
    return pick.feature != null;
  }

  public static boolean findByBox(Layer layer, Box box, Set<Feature> selection)
  {
    boolean found = false;
    Collection<Feature> features = layer.getFeatures();
    for (Feature feature : features)
    {
      Geometry geometry = feature.getGeometry();
      if (box.contains(geometry.getBoundingBox()))
      {
        selection.add(feature);
        found = true;
      }
    }
    return found;
  }

  private static double pointToSegmentDistance(Point3d pt,
    Point3d p1, Point3d p2, Point3d onObject)
  {
    Vector2d v = new Vector2d();
    v.x = p2.x - p1.x;
    v.y = p2.y - p1.y;
    v.normalize();
    if (Math.abs(v.x) > 0.1)
    {
      double t = v.y;
      v.y = -v.x;
      v.x = t;
    }
    else
    {
      double t = v.x;
      v.x = -v.y;
      v.y = t;
    }

    double c = -(v.x * p1.x + v.y * p1.y);
    double distance = v.x * pt.x + v.y * pt.y + c;

    onObject.x = pt.x - v.x * distance;
    onObject.y = pt.y - v.y * distance;

    distance = Math.abs(distance);
    if (Math.min(p1.x, p2.x) <= onObject.x &&
        onObject.x <= Math.max(p1.x, p2.x) &&
        Math.min(p1.y, p2.y) <= onObject.y &&
        onObject.y <= Math.max(p1.y, p2.y))
    {
      // inside segment
      distance = Math.abs(distance);
    }
    else
    {
      // outside segment
      double distance1 = pt.distance(p1);
      double distance2 = pt.distance(p2);
      if (distance1 < distance2)
      {
        distance = distance1;
        onObject.set(p1);
      }
      else
      {
        distance = distance2;
        onObject.set(p2);
      }
    }
    return distance;
  }
}
