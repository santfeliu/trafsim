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

import java.util.Map;
import org.santfeliu.trafsim.geom.Geometry;
import org.santfeliu.trafsim.geom.Point;

/**
 *
 * @author realor
 */
public class Location extends Feature
{
  private String name;
  private String label;
  private Point point;
  private boolean origin;

  public Location(String name, String label, Point point, boolean origin)
  {
    this.name = name;
    this.label = label;
    this.point = point;
    this.origin = origin;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public Point getPoint()
  {
    return point;
  }

  public void setPoint(Point point)
  {
    this.point = point;
  }

  public boolean isOrigin()
  {
    return origin;
  }

  public void setOrigin(boolean origin)
  {
    this.origin = origin;
  }

  public boolean isDestination()
  {
    return !origin;
  }

  @Override
  public Geometry getGeometry()
  {
    return point;
  }

  @Override
  public void loadAttributes(Map attributes)
  {
    attributes.put("NAME", name);
    attributes.put("LABEL", label);
  }
}
