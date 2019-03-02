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

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import org.santfeliu.trafsim.Box;

/**
 *
 * @author realor
 */
public class Point extends Geometry
{
  private final Point3d position = new Point3d();

  public Point(Point3d position)
  {
    this.position.set(position);
  }

  public Point3d getPosition()
  {
    return position;
  }

  @Override
  public String toString()
  {
    return "Point(" + position.x + ", " + position.y + ", " + position.z + ")";
  }

  @Override
  public void transform(Matrix4d matrix)
  {
    matrix.transform(position);
    updateBoundingBox();
  }

  @Override
  protected void extend(Box box)
  {
    box.extend(position);
  }
}
