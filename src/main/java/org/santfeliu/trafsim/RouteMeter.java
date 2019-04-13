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

import org.santfeliu.trafsim.RoadGraph.Edge;
import org.santfeliu.trafsim.Route.Section;

/**
 *
 * @author realor
 */
public class RouteMeter
{
  /**
   *
   * @param distance in meters
   * @param speed in Km/h
   * @return the time to travel distance in hours
   */
  public double getLength(Route route)
  {
    return route.getLength();
  }

  /**
   *
   * @param distance in meters
   * @param speed in Km/h
   * @return time in hours
   */
  public double getTime(double distance, double speed)
  {
    return distance / (1000 * speed);
  }

  /**
   *
   * @param edge
   * @return time in hours
   */
  public double getTime(Edge edge)
  {
    return getTime(edge.getLineString().getLength(), edge.getSpeed()) +
      edge.getDelay() / 3600.0;
  }

  /**
   *
   * @param section
   * @return time in hours
   */
  public double getTime(Section section)
  {
    return getTime(section.getLineString().getLength(),
      section.getEdge().getSpeed()) + section.getEdge().getDelay() / 3600.0;
  }

  /**
   *
   * @param route
   * @return time in hours
   */
  public double getTime(Route route)
  {
    double time;
    if (route.isEmpty())
    {
      time = Double.POSITIVE_INFINITY;
    }
    else
    {
      time = 0.0;
      for (Section section : route.getSections())
      {
        time += getTime(section);
      }
    }
    return time;
  }
}
