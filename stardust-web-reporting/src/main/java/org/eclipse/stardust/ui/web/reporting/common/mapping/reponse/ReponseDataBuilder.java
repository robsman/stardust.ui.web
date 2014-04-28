/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.common.mapping.reponse;

import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.reporting.core.RequestColumn;

public abstract class ReponseDataBuilder
{
   public abstract void addValue(RequestColumn field, Object value);
   public abstract void next();
   public abstract JsonObject getResult();
}

