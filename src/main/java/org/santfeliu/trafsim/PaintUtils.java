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
import java.awt.Graphics2D;

/**
 *
 * @author realor
 */
public class PaintUtils
{
  private static final Color HALO_COLOR = new Color(1.0f, 1.0f, 1.0f, 0.8f);

  public static void drawHaloText(String text, int x, int y, Graphics2D g)
  {
    drawHaloText(text, x, y, g, Color.BLACK, HALO_COLOR);
  }

  public static void drawHaloText(String text, int x, int y, Graphics2D g,
    Color textColor, Color haloColor)
  {
    g.setColor(haloColor);
    g.drawString(text, x - 1, y);
    g.drawString(text, x + 1, y);
    g.drawString(text, x, y - 1);
    g.drawString(text, x, y + 1);
    g.setColor(textColor);
    g.drawString(text, x, y);
  }
}
