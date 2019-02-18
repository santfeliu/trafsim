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

import javax.vecmath.Point3d;

/**
 *
 * @author realor
 */
public class Projector
{
  private int width;
  private int height;
  private final Box window = new Box();
  private double sx, sy;
  private double ox, oy;
  private boolean adjustAspectRatio = true;

  public void setWindow(Box window)
  {
    this.window.set(window);
  }
  
  public Box getWindow()
  {
    return window;
  }

  public boolean isAdjustAspectRatio()
  {
    return adjustAspectRatio;
  }

  public void setAdjustAspectRatio(boolean adjustAspectRatio)
  {
    this.adjustAspectRatio = adjustAspectRatio;
  }

  public int getWidth()
  {
    return width;
  }

  public void setWidth(int width)
  {
    this.width = width;
  }

  public int getHeight()
  {
    return height;
  }

  public void setHeight(int height)
  {
    this.height = height;
  }
  
  public void project(Point3d world, java.awt.Point device)
  {
    device.x = (int)Math.round(world.x * sx + ox);
    device.y = (int)Math.round(height - (world.y * sy + oy));
  }

  public void unproject(java.awt.Point device, Point3d world)
  {
    world.x = (device.x - ox) / sx;
    world.y = (height - device.y - oy) / sy;
    world.z = 0;
  }
  
  public void update()
  {
    if (adjustAspectRatio)
    {
      double deviceRatio = (double)width / (double)height;
      double windowRatio = window.getWidth() / window.getHeight();
      if (deviceRatio > windowRatio)
      {
        double extra = 0.5 * (deviceRatio * window.getHeight() - window.getWidth());
        window.xmin -= extra;
        window.xmax += extra;
      }
      else
      {
        double extra = 0.5 * (window.getWidth() / deviceRatio - window.getHeight());
        window.ymin -= extra;
        window.ymax += extra;      
      }
    }
    sx = width / (window.xmax - window.xmin);
    sy = height / (window.ymax - window.ymin);
    ox = -(width * window.xmin) / (window.xmax - window.xmin);
    oy = -(height * window.ymin) / (window.ymax - window.ymin);
  }
  
  public double getScaleX()
  {
    return sx;
  }

  public double getScaleY()
  {
    return sy;
  }
}
