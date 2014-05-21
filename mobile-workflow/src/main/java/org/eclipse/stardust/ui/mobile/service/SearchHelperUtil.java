package org.eclipse.stardust.ui.mobile.service;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.query.QueryResult;

/**
 * @author Shrikant.Gangal
 *
 */
public class SearchHelperUtil
{
   /**
    * @param result
    * @return
    */
   public static JsonObject getPaginationResponseObject(QueryResult result)
   {
      JsonObject pageObj = new JsonObject();
      try
      {
         if (null != result.getSubsetPolicy()) {
            pageObj.addProperty("rowFrom", result.getSubsetPolicy().getSkippedEntries());
         }
         pageObj.addProperty("resultSetSize", result.size());
         pageObj.addProperty("totalCount", result.getTotalCount());

      }
      catch (UnsupportedOperationException e)
      {
         e.printStackTrace();
         pageObj.addProperty("totalCount", -1);
      }

      return pageObj;
   }

   /**
    * @param longString
    * @param defaultVal
    * @return
    */
   public static long stringToLong(String longString, long defaultVal)
   {
      long longVal = defaultVal;
      try
      {
         longVal = Long.parseLong(longString);
      }
      catch (Exception e)
      {
         // No handling required
      }

      return longVal;
   }

   /**
    * @param intString
    * @param defaultVal
    * @return
    */
   public static int stringToInt(String intString, int defaultVal)
   {
      int intVal = defaultVal;
      try
      {
         intVal = Integer.parseInt(intString);
      }
      catch (Exception e)
      {
         // No handling required
      }

      return intVal;
   }

   /**
    * @param listString
    * @return
    */
   public static List<String> csvStringToList(String listString)
   {
      List<String> list = new ArrayList<String>();
      try
      {
         StringTokenizer tokenizer = new StringTokenizer(listString, ",");
         while (tokenizer.hasMoreTokens())
         {
            list.add(tokenizer.nextToken());
         }
      }
      catch (Exception e)
      {
         // No handling required
      }

      return list;
   }
}
