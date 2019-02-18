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
package org.santfeliu.trafsim.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.vecmath.Point3d;

/**
 *
 * @author realor
 */
public class XMLWriter
{
  protected boolean tagOpen = false;
  protected final PrintWriter writer;

  public XMLWriter(OutputStream os) throws IOException
  {
    writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
  }

  protected void writePreambule()
  {
    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
  }

  protected void writePoint3d(Point3d point)
  {
    writeText(point.getX() + "," + point.getY() + "," + point.getZ());
  }

  protected void startTag(String tag)
  {
    closeStartTag();
    writer.print("<" + tag);
    tagOpen = true;
  }

  protected void writeAttribute(String name, Object value)
  {
    if (tagOpen)
    {
      writer.print(" " + name + "=\"" + value + "\"");
    }
  }

  protected void writeText(Object text)
  {
    closeStartTag();
    if (text != null)
    {
      writer.print(String.valueOf(text));
    }
  }

  protected void endTag(String tag)
  {
    closeStartTag();
    writer.print("</" + tag + ">");
  }

  protected void closeStartTag()
  {
    if (tagOpen) writer.write(">");
    tagOpen = false;
  }

  public void close()
  {
    writer.close();
  }
}
