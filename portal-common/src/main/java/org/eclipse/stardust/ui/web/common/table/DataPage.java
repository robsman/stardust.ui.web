/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * "The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License.
 *
 * The Original Code is ICEfaces 1.5 open source software code, released
 * November 5, 2006. The Initial Developer of the Original Code is ICEsoft
 * Technologies Canada, Corp. Portions created by ICEsoft are Copyright (C)
 * 2004-2006 ICEsoft Technologies Canada, Corp. All Rights Reserved.
 *
 * Contributor(s): _____________________.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"
 * License), in which case the provisions of the LGPL License are
 * applicable instead of those above. If you wish to allow use of your
 * version of this file only under the terms of the LGPL License and not to
 * allow others to use your version of this file under the MPL, indicate
 * your decision by deleting the provisions above and replace them with
 * the notice and other provisions required by the LGPL License. If you do
 * not delete the provisions above, a recipient may use your version of
 * this file under either the MPL or the LGPL License."
 *
 */

//Note: This file is derived from http://anonsvn.icefaces.org/repo/tutorials/trunk/tutorials/icefaces1.8/samples/dataTable-JPA/dataTable/src/com/icesoft/icefaces/samples/datatable/ui/DataPage.java (r27696) 

package org.eclipse.stardust.ui.web.common.table;

import java.io.Serializable;
import java.util.List;

/**
 * A simple class that represents a "page" of data out of a longer set, ie
 * a list of objects together with info to indicate the starting row and
 * the full size of the dataset. Business methods can return instances of this type
 * when returning subsets of available data.
 */
public class DataPage<T> implements Serializable
{
   private static final long serialVersionUID = 1L;

   private int datasetSize;

   private int startRow;

   private List<T> data;
   
   private int dataSize;

   /**
    * Create an object representing a sublist of a dataset.
    *
    * @param datasetSize is the total number of matching rows
    *                    available.
    * @param startRow    is the index within the complete dataset
    *                    of the first element in the data list.
    * @param data        is a list of consecutive objects from the
    *                    dataset.
    */
   public DataPage(int datasetSize, int startRow, List<T> data)
   {
      this.datasetSize = datasetSize;
      this.startRow = startRow;
      this.data = data;
      this.dataSize = null != data ? data.size() : 0;
   }

   /**
    * Return the number of items in the full dataset.
    */
   public int getDatasetSize()
   {
      return datasetSize;
   }

   /**
    * Return the offset within the full dataset of the first
    * element in the list held by this object.
    */
   public int getStartRow()
   {
      return startRow;
   }

   /**
    * Return the list of objects held by this object, which
    * is a continuous subset of the full dataset.
    */
   public List<T> getData()
   {
      return data;
   }
   
   /**
    * @return
    */
   public int getDataSize()
   {
      return dataSize;
   }
}