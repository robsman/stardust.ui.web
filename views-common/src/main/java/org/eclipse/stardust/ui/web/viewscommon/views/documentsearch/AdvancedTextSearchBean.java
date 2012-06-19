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
package org.eclipse.stardust.ui.web.viewscommon.views.documentsearch;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;


/**
 * @author Yogesh.Manware
 * 
 */
public class AdvancedTextSearchBean extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "advancedTextSearchBean";
   private String substituteTextForSearch;
   private String allWords;
   private String exactPhrase;
   private String orWord1;
   private String orWord2;
   private String orWord3;
   private String unWantedWords;

   private ICallbackHandler iCallbackHandler;

   public AdvancedTextSearchBean()
   {
      super("documentSearchView");
   }

   /**
    * @return fileUploadAdminDialog object
    */
   public static AdvancedTextSearchBean getInstance()
   {
      return (AdvancedTextSearchBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   @Override
   public void initialize()
   {
      substituteTextForSearch = this.getMessages().getString("advancedTextSearch.placeHolder");
      allWords = "";
      exactPhrase = "";
      orWord1 = "";
      orWord2 = "";
      orWord3 = "";
      unWantedWords = "";
   }

   public void done()
   {
      iCallbackHandler.handleEvent(EventType.APPLY);
      closePopup();
   }

   public String getFinalTextForSearch()
   {
      String newText = getTextForSearch().trim();
      if (!substituteTextForSearch.trim().equalsIgnoreCase(newText))
      {
         return newText;
      }
      return "";
   }
   
   public String getTextForSearch()
   {
      StringBuffer text = new StringBuffer();
      text.append(allWords);
      if (StringUtils.isNotEmpty(exactPhrase))
      {
         text.append(" ");
         text.append("\"").append(exactPhrase).append("\"");
      }
      if (StringUtils.isNotEmpty(orWord1))
      {
         text.append(" ");
         text.append(orWord1);
      }
      if (StringUtils.isNotEmpty(orWord1) && StringUtils.isNotEmpty(orWord2))
      {
         text.append(" ");
         text.append("OR");
         text.append(" ");
         text.append(orWord2);
      }
      else if (StringUtils.isNotEmpty(orWord2))
      {
         text.append(" ");
         text.append(orWord2);
      }

      if ((StringUtils.isNotEmpty(orWord1) || StringUtils.isNotEmpty(orWord2)) && StringUtils.isNotEmpty(orWord3))
      {
         text.append(" ");
         text.append("OR");
         text.append(" ");
         text.append(orWord3);
      }
      else if (StringUtils.isNotEmpty(orWord3))
      {
         text.append(" ");
         text.append(orWord3);
      }

      if (StringUtils.isNotEmpty(unWantedWords))
      {
         text.append(" -");
         text.append(unWantedWords);
      }

      String newText = text.toString();
      if (StringUtils.isNotEmpty(newText))
      {
         return newText;
      }
      else
      {
         initialize();
      }
      return substituteTextForSearch;
   }

   public String getAllWords()
   {
      return allWords;
   }

   public void setAllWords(String allWords)
   {
      this.allWords = allWords;
   }

   public String getExactPhrase()
   {
      return exactPhrase;
   }

   public void setExactPhrase(String exactPhrase)
   {
      this.exactPhrase = exactPhrase;
   }

   public String getOrWord1()
   {
      return orWord1;
   }

   public void setOrWord1(String orWord1)
   {
      this.orWord1 = orWord1;
   }

   public String getOrWord2()
   {
      return orWord2;
   }

   public void setOrWord2(String orWord2)
   {
      this.orWord2 = orWord2;
   }

   public String getOrWord3()
   {
      return orWord3;
   }

   public void setOrWord3(String orWord3)
   {
      this.orWord3 = orWord3;
   }

   public String getUnWantedWords()
   {
      return unWantedWords;
   }

   public void setUnWantedWords(String unWantedWord)
   {
      this.unWantedWords = unWantedWord;
   }

   public void setICallbackHandler(ICallbackHandler callbackHandler)
   {
      iCallbackHandler = callbackHandler;
   }
}
