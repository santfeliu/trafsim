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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import org.santfeliu.trafsim.GenericLayer.GenericFeature;
import org.santfeliu.trafsim.geom.Geometry;

/**
 *
 * @author realor
 */
public class GenericLayer extends Layer<GenericFeature>
{
  private String label;
  private final ArrayList<GenericFeature> features =
    new ArrayList<GenericFeature>();
  private Color color;

  public GenericLayer(String label, Color color)
  {
    this.label = label;
    this.color = color;
  }

  public GenericFeature newFeature(Geometry geometry)
  {
    return new GenericFeature(geometry);
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public Color getColor()
  {
    return color;
  }

  public void setColor(Color color)
  {
    this.color = color;
  }

  @Override
  public String getName()
  {
    return label;
  }

  @Override
  public void clear()
  {
    for (GenericFeature feature : features)
    {
      feature.removed = true;
    }
    features.clear();
  }

  @Override
  public Collection<GenericFeature> getFeatures()
  {
    return features;
  }

  public class GenericFeature extends Feature
  {
    private Geometry geometry;
    private boolean removed;

    GenericFeature(Geometry geometry)
    {
      this.geometry = geometry;
      this.removed = true;
    }

    @Override
    public Geometry getGeometry()
    {
      return geometry;
    }

    @Override
    public void setGeometry(Geometry geometry)
    {
      this.geometry = geometry;
    }

    @Override
    public Layer getLayer()
    {
      return GenericLayer.this;
    }

    @Override
    public void add()
    {
      if (removed)
      {
        features.add(this);
        removed = false;
      }
    }

    @Override
    public void remove()
    {
      if (!removed)
      {
        features.remove(this);
        removed = true;
      }
    }
    
    @Override
    public boolean isRemoved()
    {
      return removed;
    }
  }
}
