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

import java.io.Serializable;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author realor
 */
public class Box implements Serializable
{
  public double xmin, ymin, zmin, xmax, ymax, zmax;

  public Box()
  {
    reset();
  }

  public Box(double xmin, double ymin, double zmin,
    double xmax, double ymax, double zmax)
  {
    this.xmin = xmin;
    this.ymin = ymin;
    this.zmin = zmin;

    this.xmax = xmax;
    this.ymax = ymax;
    this.zmax = zmax;
  }

  public void extend(Point3d point)
  {
    if (point.x > xmax) xmax = point.x;
    if (point.x < xmin) xmin = point.x;

    if (point.y > ymax) ymax = point.y;
    if (point.y < ymin) ymin = point.y;

    if (point.z > zmax) zmax = point.z;
    if (point.z < zmin) zmin = point.z;
  }

  public void extend(Box box)
  {
    if (box.xmin < xmin) xmin = box.xmin;
    if (box.xmax > xmax) xmax = box.xmax;

    if (box.ymin < ymin) ymin = box.ymin;
    if (box.ymax > ymax) ymax = box.ymax;

    if (box.zmin < zmin) zmin = box.zmin;
    if (box.zmax > zmax) zmax = box.zmax;
  }

  public final void reset()
  {
    xmin = Double.POSITIVE_INFINITY;
    ymin = Double.POSITIVE_INFINITY;
    zmin = Double.POSITIVE_INFINITY;

    xmax = Double.NEGATIVE_INFINITY;
    ymax = Double.NEGATIVE_INFINITY;
    zmax = Double.NEGATIVE_INFINITY;
  }

  public double getWidth()
  {
    return xmax - xmin;
  }

  public double getHeight()
  {
    return ymax - ymin;
  }

  public double getVolume()
  {
    return zmax - zmin;
  }

  public void move(Vector3d vector)
  {
    xmin += vector.x;
    xmax += vector.x;

    ymin += vector.y;
    ymax += vector.y;

    zmin += vector.z;
    zmax += vector.z;
  }

  public void scale(double factor)
  {
    xmin *= factor;
    xmax *= factor;

    ymin *= factor;
    ymax *= factor;

    zmin *= factor;
    zmax *= factor;
  }

  public void set(Box box)
  {
    xmin = box.xmin;
    ymin = box.ymin;
    zmin = box.zmin;

    xmax = box.xmax;
    ymax = box.ymax;
    zmax = box.zmax;
  }

  public boolean overlaps(Box box)
  {
    if (xmin > box.xmax) return false;
    if (xmax < box.xmin) return false;

    if (ymin > box.ymax) return false;
    if (ymax < box.ymin) return false;

    if (zmin > box.zmax) return false;
    if (zmax < box.zmin) return false;

    return true;
  }

  public boolean contains(Point3d point)
  {
    if (xmin > point.x) return false;
    if (xmax < point.x) return false;

    if (ymin > point.y) return false;
    if (ymax < point.y) return false;

    if (zmin > point.z) return false;
    if (zmax < point.z) return false;

    return true;
  }

  public boolean contains(Box box)
  {
    return (xmin <= box.xmin && box.xmax <= xmax) &&
      (ymin <= box.ymin && box.ymax <= ymax) &&
      (zmin <= box.zmin && box.zmax <= zmax);
  }

  public boolean isUndefined()
  {
    return xmin > xmax || ymin > ymax || zmin > zmax;
  }

  @Override
  public String toString()
  {
    return "(" + xmin + ", " + ymin + ", " + zmin + ", " +
            xmax + ", " + ymax + ", " + zmax + ")";
  }
}
