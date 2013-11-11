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

package org.eclipse.stardust.ui.web.processportal.interaction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * @author Subodh.Godbole
 *
 */
public class InteractionRegistry
{
   public static String BEAN_ID = "infinityBpmInteractionsRegistry2";

   private final ConcurrentMap<String, Interaction> interactions = new ConcurrentHashMap<String, Interaction>();

   public void registerInteraction(Interaction interaction)
   {
      interactions.put(interaction.getId(), interaction);

      // TODO remove
      //interactions.put("test", interaction);
   }

   public void unregisterInteraction(String id)
   {
      interactions.remove(id);
   }

   public Interaction getInteraction(String id)
   {
      return interactions.get(id);
   }
}
