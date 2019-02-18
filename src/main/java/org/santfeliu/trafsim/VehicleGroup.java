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
public class VehicleGroup extends Feature
{
  private Point point;
  private int count;
  private String group;
  private Indicators indicators;

  public VehicleGroup(Point point, int count, String group)
  {
    this.point = point;
    this.count = count;
    this.group = group;
  }

  public Point getPoint()
  {
    return point;
  }

  public void setPoint(Point point)
  {
    this.point = point;
  }

  public int getCount()
  {
    return count;
  }

  public void setCount(int count)
  {
    this.count = count;
  }

  public String getGroup()
  {
    return group;
  }

  public void setGroup(String group)
  {
    this.group = group;
  }

  @Override
  public Geometry getGeometry()
  {
    return point;
  }
  
  @Override
  public void loadAttributes(Map attributes)
  {
    attributes.put("COUNT", count);
    attributes.put("GROUP", group);
  }
  
  public Indicators getIndicators()
  {
    if (indicators == null) indicators = new Indicators();
    return indicators;
  }
  
  public class Indicators
  {
    public int journeyCount;
    public int routedCount;
    public int unroutedCount;
    public double distance;
    public double time;
    
    public void reset()
    {
      journeyCount = 0;
      routedCount = 0;
      unroutedCount = 0;
      distance = 0;
      time = 0;
    }

    double getJourneyAvgTime()
    {
      if (routedCount == 0) return 0;
      return time / routedCount;
    }

    double getJourneyAvgDistance()
    {
      if (routedCount == 0) return 0;
      return distance / routedCount;
    }
  }
}
