/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.bcc.launchpad;

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;

/**
 * @author Abhay.Thappan
 * 
 */
public class MyFavoriteModel
{
   private String favoriteName;

   private static final String TLV_VIEW_ID = "trafficLightViewNew";

   /**
    * @param favoriteName
    */
   public MyFavoriteModel(String favoriteName)
   {
      super();
      this.favoriteName = favoriteName;
   }

   /**
    * @return
    */
   public String selectHTML5()
   {
      Map<String, Object> params = CollectionUtils.newTreeMap();
      params.put("preferenceId", TLV_VIEW_ID);
      params.put("preferenceName", favoriteName);
      PortalApplication.getInstance().openViewById(TLV_VIEW_ID, "id=" + favoriteName, params, null, false);
      return null;
   }

   public String getName()
   {
      return favoriteName;
   }

}
