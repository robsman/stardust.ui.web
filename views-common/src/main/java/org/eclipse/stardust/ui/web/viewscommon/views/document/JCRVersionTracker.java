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
package org.eclipse.stardust.ui.web.viewscommon.views.document;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.GenericRepositoryTreeViewBean;

/**
 * @author Yogesh.Manware
 *
 */
public class JCRVersionTracker implements IVersionTracker
{
   private static final Logger logger = LogManager.getLogger(GenericRepositoryTreeViewBean.class);
   private final static int LOWEST_VERSION = 1;
   private final static int MIN_DIFF = 1;
   private int currentVersionNo;
   private Map<Integer, Document> versions = new HashMap<Integer, Document>();
   private int latestVersion;
   private Document document;

   /**
    * @param document
    */
   public JCRVersionTracker(Document document)
   {
      this.document = document;
      initialize();
   }

   /**
    * Sets document version map sorted based on last modification date
    */
   private void initialize()
   {
      this.latestVersion = LOWEST_VERSION;
      this.currentVersionNo = LOWEST_VERSION;

      try
      {
         @SuppressWarnings("unchecked")
         List<Document> docVersionList = DocumentMgmtUtility.getDocumentVersions(document);
         if (null != docVersionList)
         {
            Map<Date, Document> tempMap = new TreeMap<Date, Document>();

            for (Document document1 : docVersionList)
            {
               tempMap.put(document1.getDateLastModified(), document1);
            }

            // if the list is not completely sortable by date use default ordering.
            if (tempMap.size() < docVersionList.size())
            {
               // documents have same date or no date at all. No change in ordering.
               int version = LOWEST_VERSION;
               for (Document document : docVersionList)
               {
                  versions.put(version, document);
                  this.latestVersion = version;
                  version = version + MIN_DIFF;
               }
            }
            else
            {
               // sort by modified date
               int version = LOWEST_VERSION;
               Document tempDoc;

               for (Date d : tempMap.keySet())
               {
                  tempDoc = tempMap.get(d);
                  versions.put(version, tempDoc);
                  this.latestVersion = version;
                  version = version + MIN_DIFF;
               }
            }
            this.currentVersionNo = latestVersion;
         }
      }
      catch (Exception e)
      {
         if (logger.isDebugEnabled())
         {
            logger.debug("The document does not have any versions, Id: " + document.getId());
         }
      }
   }

   public String getCurrentVersionNo()
   {
      return String.valueOf(currentVersionNo);
   }

   public JCRDocument shiftToPreviousVersion()
   {
      currentVersionNo = currentVersionNo - MIN_DIFF;
      return new JCRDocument(versions.get(currentVersionNo), this);
   }

   public IDocumentContentInfo shiftToNextVersion()
   {
      currentVersionNo = currentVersionNo + MIN_DIFF;
      return new JCRDocument(versions.get(currentVersionNo), this);
   }

   public boolean isLatestVersion()
   {
      return this.latestVersion == this.currentVersionNo;
   }

   public boolean hasPreviousVersion()
   {
      return currentVersionNo > LOWEST_VERSION;
   }

   public boolean hasNextVersion()
   {
      return latestVersion > currentVersionNo;
   }

   // cannot downcast the map to JCRDocument
   public Map<Integer, Document> getVersions()
   {
      return versions;
   }

   public JCRDocument getLatestVersion()
   {
      return new JCRDocument(versions.get(latestVersion), this);
   }

   @Override
   public IDocumentContentInfo shiftToVersion(int version)
   {
      currentVersionNo = version;
      return new JCRDocument(versions.get(currentVersionNo), this);
   }
}
