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

/**
 *
 * @author realor
 */
public class Indicators
{
  double totalTime = 0;
  double totalDistance = 0;
  int totalJourneyCount = 0;
  int totalRoutedCount = 0;
  int totalUnroutedCount = 0;
  double journeyAvgTime = 0;
  double journeyAvgDistance = 0;
  int maxVehiclesPerEdge = 0;

  void evaluate(Simulation simulation)
  {
    reset();
    for (VehicleGroup vehicleGroup : simulation.getVehicles().getFeatures())
    {
      update(vehicleGroup);
    }
    for (Edge edge : simulation.getRoadGraph().getEdges())
    {
      update(edge);
    }
    updateAverages();
  }

  public void reset()
  {
    totalJourneyCount = 0;
    totalRoutedCount = 0;
    totalUnroutedCount = 0;
    totalTime = 0;
    totalDistance = 0;
  }

  public void update(VehicleGroup vehicleGroup)
  {
    VehicleGroup.Indicators vehicleInd = vehicleGroup.getIndicators();
    if (vehicleInd.journeyCount > 0)
    {
      totalJourneyCount += vehicleInd.journeyCount;
      totalRoutedCount += vehicleInd.routedCount;
      totalUnroutedCount += vehicleInd.unroutedCount;
      totalTime += vehicleInd.time;
      totalDistance += vehicleInd.distance;
    }
  }

  public void update(Edge edge)
  {
    Edge.Indicators edgeInd = edge.getIndicators();
    if (edgeInd.vehicleCount > maxVehiclesPerEdge)
    {
      maxVehiclesPerEdge = edgeInd.vehicleCount;
    }
  }

  public void updateAverages()
  {
    if (totalRoutedCount == 0)
    {
      journeyAvgTime = 0;
      journeyAvgDistance = 0;
    }
    else
    {
      journeyAvgTime = totalTime / totalRoutedCount;
      journeyAvgDistance = totalDistance / totalRoutedCount;
    }
  }
}

