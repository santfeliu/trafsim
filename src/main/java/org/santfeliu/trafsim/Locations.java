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
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author realor
 */
public class Locations extends Layer<Location>
{
  private final ArrayList<Location> features = new ArrayList<Location>();

  @Override
  public String getName()
  {
    return "Locations";
  }

  public Location getLocation(String name)
  {
    Iterator<Location> iter = features.iterator();
    while (iter.hasNext())
    {
      Location location = iter.next();
      if (location.getName().equals(name)) return location;
    }
    return null;
  }

  @Override
  public void add(Location location)
  {
    features.add(location);
  }

  @Override
  public boolean remove(Feature feature)
  {
    if (feature instanceof Location)
    {
      return features.remove((Location)feature);
    }
    return false;
  }

  @Override
  public void clear()
  {
    features.clear();
  }

  @Override
  public Collection<Location> getFeatures()
  {
    return features;
  }
}
