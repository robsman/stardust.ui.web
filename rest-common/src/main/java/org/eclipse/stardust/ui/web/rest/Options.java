/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
public class Options
{
   public int pageSize;
   public int skip;

   /**
    * @param pageSize
    * @param skip
    */
   public Options(int pageSize, int skip)
   {
      super();
      this.pageSize = pageSize;
      this.skip = skip;
   }
}