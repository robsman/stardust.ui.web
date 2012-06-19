/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ICEsoft Technologies Canada, Corp. - initial API and implementation
 *    SunGard CSA LLC                    - additional modifications
 *******************************************************************************/

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