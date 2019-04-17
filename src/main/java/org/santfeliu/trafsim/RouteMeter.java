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
  // distance in meters between 2 consecutive vehicles (front to front)
  static double vehicleLength = 6.5;

  public static double getVehicleLength()
  {
    return vehicleLength;
  }

  public static void setVehicleLength(double vehicleLength)
  {
    RouteMeter.vehicleLength = vehicleLength;
  }

  /**
   *
   * @param route
   * @return route length in meters
   */
  public static double getLength(Route route)
  {
    return route.getLength();
  }

  /**
   *
   * @param distance in meters
   * @param speed in Km/h
   * @param delay in seconds
   * @return time in hours
   */
  public static double getTime(double distance, double speed, double delay)
  {
    return (delay / 3600) + distance / (1000 * speed);
  }

  /**
   *
   * @param edge
   * @return time in hours
   */
  public static double getTime(Edge edge)
  {
    return getTime(edge.getLineString().getLength(),
      edge.getSpeed(), edge.getDelay());
  }

  /**
   *
   * @param section
   * @return time in hours
   */
  public static double getTime(Section section)
  {
    Edge edge = section.getEdge();
    return getTime(section.getLineString().getLength(), edge.getSpeed(),
      edge.getDelay());
  }

  /**
   *
   * @param route
   * @return time in hours
   */
  public static double getTime(Route route)
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

  /**
   *
   * @param edge
   * @return average speed in Km/h for one vehicle
   */
  public static double getAverageSpeed(Edge edge)
  {
    double distance = edge.getLineString().getLength();
    return distance / (1000 * getTime(edge));
  }

  /**
   *
   * @param edge
   * @return the number of vehicles that can be on this edge
   */
  public static double getOnlineVehicles(Edge edge)
  {
    return edge.getLineString().getLength() / vehicleLength;
  }

  /**
   *
   * @param edge
   * @return edge capacity in vehicles / hour
   */
  public static double getCapacity(Edge edge)
  {
    double capacity = 1000 * edge.getSpeed() / vehicleLength;
    if (edge.getDelay() > 0.0)
    {
      capacity = Math.min(capacity, 3600.0 / edge.getDelay());
    }
    return edge.getLanes() * capacity;
  }
}
