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
import org.santfeliu.trafsim.Group.Journey;

/**
 *
 * @author realor
 */
public class Distributor
{
  public Movements getMovements(int vehicleCount, Group group)
  {
    List<Movement> movementList = new ArrayList<Movement>();

    // sum all factors
    double total = 0.0;
    for (Journey journey : group.getJourneys())
    {
      total += journey.getFactor();
    }

    // assign vehicles to each location
    int assigned = 0;
    for (Journey journey : group.getJourneys())
    {
      double factor = journey.getFactor() / total;
      if (factor > 0)
      {
        double realJourneyCount = vehicleCount * factor;
        Movement movement = new Movement();
        movement.locationName = journey.getLocationName();
        movement.journeyCount = (int)Math.floor(realJourneyCount);
        movement.remainder = realJourneyCount - movement.journeyCount;
        assigned += movement.journeyCount;
        movementList.add(movement);
      }
    }
    // assign unassigned vehicles to locations randomly
    int unassigned = vehicleCount - assigned;
    double remainder = unassigned;
    for (int i = 0; i < unassigned; i++)
    {
      double r = Math.random() * remainder;
      double accum = 0;
      for (int j = 0; j < movementList.size(); j++)
      {
        Movement movement = movementList.get(j);
        if (r >= accum && r < accum + movement.remainder)
        {
          remainder -= movement.remainder;
          movement.journeyCount++;
          movement.remainder = 0;
          break;
        }
        accum += movement.remainder;
      }
    }

    // remove movements with no vehicles (journeyCount == 0)
    movementList.removeIf((e) -> e.journeyCount == 0);

    Movements movements = new Movements();
    for (Movement movement : movementList)
    {
      movements.put(movement.locationName, movement.journeyCount);
    }

    return movements;
  }

  private class Movement
  {
    private String locationName;
    private int journeyCount;
    private double remainder;

    @Override
    public String toString()
    {
      return locationName + ": " + journeyCount;
    }
  }

  public static void main(String[] args)
  {
    Group group = new Group("test");
    group.addJourney("A", 0.5);
    group.addJourney("B", 0.4);
    group.addJourney("C", 0.1);

    Distributor d = new Distributor();
    System.out.println(d.getMovements(12, group));
  }
}
