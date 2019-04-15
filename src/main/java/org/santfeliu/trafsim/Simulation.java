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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author realor
 */
public class Simulation
{
  private String title;
  private String srsName = "EPSG:25831";
  private double duration = 0; // hours
  private final ArrayList<Layer> layers;
  private final Map<String, Group> groups;
  private final Indicators indicators;

  public Simulation()
  {
    layers = new ArrayList<Layer>();
    layers.add(new RoadGraph()); // roadGraph
    layers.add(new Locations()); // locations
    layers.add(new Vehicles()); // vehicles
    groups = Collections.synchronizedMap(new HashMap<String, Group>());
    indicators = new Indicators();
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getSrsName()
  {
    return srsName;
  }

  public void setSrsName(String srsName)
  {
    this.srsName = srsName;
  }

  public double getDuration()
  {
    return duration;
  }

  public void setDuration(double duration)
  {
    this.duration = duration;
  }

  public RoadGraph getRoadGraph()
  {
    return (RoadGraph)layers.get(0);
  }

  public Locations getLocations()
  {
    return (Locations)layers.get(1);
  }

  public Vehicles getVehicles()
  {
    return (Vehicles)layers.get(2);
  }

  public Layer getLayer(int index)
  {
    return layers.get(index);
  }

  public int getLayerCount()
  {
    return layers.size();
  }

  public Map<String, Group> getGroups()
  {
    return groups;
  }

  public Indicators getIndicators()
  {
    return indicators;
  }

  public void addGenericLayer(GenericLayer layer)
  {
    layers.add(layer);
  }

  public List<GenericLayer> getGenericLayers()
  {
    List<GenericLayer> baseLayers = new ArrayList<GenericLayer>();
    for (int i = 3; i < layers.size(); i++)
    {
      baseLayers.add((GenericLayer)layers.get(i));
    }
    return baseLayers;
  }

  public void clearGenericLayers()
  {
    while (layers.size() > 3)
    {
      layers.remove(layers.size() - 1);
    }
  }

  public Box getBoundingBox(Box box)
  {
    if (box == null) box = new Box();

    for (Layer layer : layers)
    {
      Collection<Feature> features = layer.getFeatures();
      for (Feature feature : features)
      {
        box.extend(feature.getGeometry().getBoundingBox());
      }
    }
    return box;
  }
}
