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
package org.eclipse.stardust.ui.web.common;

import java.util.ArrayList;
import java.util.List;

/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class UiExtension<E extends UiElement> implements MessageSourceProvider
{
   private static final long serialVersionUID = 1L;

   private final List<E> elements = new ArrayList<E>();

   private MessageSourceProvider messagesProvider;

   private String before;
   
   private String after;
   
   public boolean addElement(E element)
   {
      element.setMessagesProvider(this);

      return this.elements.add(element);
   }
   
   public MessageSource getMessages()
   {
      return (null != messagesProvider) ? messagesProvider.getMessages() : null;
   }

   public void setMessagesProvider(MessageSourceProvider messagesProvider)
   {
      this.messagesProvider = messagesProvider;
   }

   public List<E> getElements()
   {
      return elements;
   }

   public String getBefore()
   {
      return before;
   }

   public void setBefore(String before)
   {
      this.before = before;
   }

   public String getAfter()
   {
      return after;
   }

   public void setAfter(String after)
   {
      this.after = after;
   }
}
