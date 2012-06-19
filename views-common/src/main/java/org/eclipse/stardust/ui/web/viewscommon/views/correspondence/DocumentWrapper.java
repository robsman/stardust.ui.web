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
package org.eclipse.stardust.ui.web.viewscommon.views.correspondence;

import org.eclipse.stardust.engine.api.runtime.Document;

import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.Highlight;


/**
 * @author Subodh.Godbole
 *
 */
public class DocumentWrapper
{
   private Document document;
   private boolean selectable;
   private Effect clickEffect;

   /**
    * @param document
    * @param selectable
    */
   public DocumentWrapper(Document document, boolean selectable)
   {
      super();
      this.document = document;
      this.selectable = selectable;
      this.clickEffect = new Highlight("#fda505");
      this.clickEffect.setFired(true);
   }

   public void fireClickEffect()
   {
      clickEffect.setFired(false);
   }
   
   public Document getDocument()
   {
      return document;
   }

   public boolean isSelectable()
   {
      return selectable;
   }
   
   public Effect getClickEffect()
   {
      return clickEffect;
   }

   public void setClickEffect(Effect clickEffect)
   {
      this.clickEffect = clickEffect;
   }
}