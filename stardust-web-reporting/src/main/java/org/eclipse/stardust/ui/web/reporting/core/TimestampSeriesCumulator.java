package org.eclipse.stardust.ui.web.reporting.core;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

/**
 * Discrete dimension is mapped to String, a non-discrete, continuous dimension is mapped
 * to long. If the dimension is discrete, the sequence of values is assumed to be grouped
 * by dimension values; if it is continous, it is assumed to be monotonous.
 *
 * @author Marc.Gille
 *
 */
public class TimestampSeriesCumulator
{
   private final String UNDEFINED_STRING = "__UNDEFINED";

   private boolean discreteDimension;
   private boolean allowsCumulants;
   private ValueProvider factProvider;
   private ValueProvider dimensionProvider;
   private ValueProvider groupCriterionProvider;

   public TimestampSeriesCumulator(ValueProvider factProvider, ValueProvider dimensionProvider,
         boolean discreteDimension, boolean allowsCumulants)
   {
      this.discreteDimension = discreteDimension;
      this.allowsCumulants = allowsCumulants;
      this.factProvider = factProvider;
      this.dimensionProvider = dimensionProvider;
   }

   /**
    *
    * @param groupCriterionProvider
    */
   public void setGroupCriterionProvider(ValueProvider groupCriterionProvider)
   {
      this.groupCriterionProvider = groupCriterionProvider;
   }

   /**
    *
    * @param object
    * @return
    */
   private double getFact(Object object)
   {
      return ((Number) this.factProvider.getValue(object)).doubleValue();
   }

   /**
    *
    * @param object
    * @return
    */
   private Object getDimension(Object object)
   {
      return dimensionProvider.getValue(object);
   }

   /**
    * Indicates no grouping.
    *
    * @param object
    * @return
    */
   private Comparable getGroupingCriterion(Object object)
   {
      if (this.groupCriterionProvider != null)
      {
         return (Comparable) this.groupCriterionProvider.getValue(object);
      }

      return null;
   }

   /**
    *
    * @param list
    * @param cumulationInterval
    * @return
    */
   public JsonArray createCumulatedSeriesGroup(List<Object> list, long cumulationInterval, JsonArray groupIds)
   {
      TreeMap<Comparable, List<Object>> groups = new TreeMap<Comparable, List<Object>>();

      // Group objects

      for (Object object : list)
      {
         Comparable groupingCriterion = getGroupingCriterion(object);

         if (groupingCriterion == null)
         {
            groupingCriterion = "-";
         }

         List<Object> groupList = groups.get(groupingCriterion);

         if (groupList == null)
         {
            groups.put(groupingCriterion, groupList = new ArrayList<Object>());
            groupIds.add(new JsonPrimitive(groupingCriterion.toString()));
         }

         groupList.add(object);
      }

      JsonArray seriesGroup = new JsonArray();

      for (Comparable key : groups.keySet())
      {
         JsonArray series = new JsonArray();

         seriesGroup.add(series);

         Object currentDimension = this; // Null is a valid value, hence this
         // as a dummy
         long count = 0;
         double currentValue = 0;
         double currentMaximum = 0;
         double currentMinimum = Double.MAX_VALUE;
         double currentSum = 0;
         double currentSquareSum = 0;

         JsonArray pair = null;

         System.out.println("New series " + key);

         for (Object object : groups.get(key))
         {
            // Initialize timestamp for first entry

            System.out.println("Dimension " + getDimension(object));

            if (currentDimension == this)
            {
               currentDimension = getDimension(object);

               // Create new pair for current interval

               pair = new JsonArray();

               series.add(pair);

               // Add dimension to pair

               if (discreteDimension)
               {
                  if (currentDimension != null)
                  {
                     pair.add(new JsonPrimitive(currentDimension.toString()));
                  }
                  else
                  {
                     pair.add(new JsonPrimitive(UNDEFINED_STRING));
                  }
               }
               else
               {
                  pair.add(new JsonPrimitive(((Long) currentDimension).longValue()));
               }
            }

            if ((!discreteDimension && ((Long) getDimension(object)).longValue() >= ((Long) currentDimension)
                  .longValue() + cumulationInterval)
                  || getDimension(object) == null || !getDimension(object).equals(currentDimension))
            {
               // Close current interval

               System.out.println("Closing interval " + currentDimension + " and count " + count);

               if (allowsCumulants)
               {
                  double average = currentSum / count;
                  double sigma = count == 1 ? 0 : Math.sqrt((currentSquareSum - currentSum * currentSum / count)
                        / count - 1);

                  System.out.println("===> Average         " + average);
                  System.out.println("===> Sigma           " + sigma);
                  System.out.println("===> Current Minimum " + currentMinimum);
                  System.out.println("===> Current Maximum " + currentMaximum);

                  pair.add(new JsonPrimitive(average - sigma));
                  pair.add(new JsonPrimitive(currentMinimum));
                  pair.add(new JsonPrimitive(currentMaximum));
                  pair.add(new JsonPrimitive(average + sigma));
                  pair.add(new JsonPrimitive(count));
               }
               else
               {
                  pair.add(new JsonPrimitive(count));
               }

               if (discreteDimension)
               {
                  currentDimension = getDimension(object);
               }
               else
               {
                  currentDimension = new Long(((Long) currentDimension).longValue() + cumulationInterval);
               }

               // Populate intervals between the last and the current one

               while (!discreteDimension
                     && ((Long) getDimension(object)).longValue() > ((Long) currentDimension).longValue()
                           + cumulationInterval)
               {
                  pair = new JsonArray();

                  series.add(pair);

                  pair.add(new JsonPrimitive(((Long) currentDimension).longValue()));

                  if (allowsCumulants)
                  {
                     pair.add(new JsonPrimitive(0));
                     pair.add(new JsonPrimitive(0));
                     pair.add(new JsonPrimitive(0));
                     pair.add(new JsonPrimitive(0));
                  }
                  else
                  {
                     pair.add(new JsonPrimitive(0));
                  }

                  pair = null;

                  currentDimension = new Long(((Long) currentDimension).longValue() + cumulationInterval);
               }

               // Create new pair for current interval

               pair = new JsonArray();

               series.add(pair);

               // Add dimension to pair

               if (discreteDimension)
               {
                  if (currentDimension == null)
                  {
                     pair.add(new JsonPrimitive(""));
                  }
                  else
                  {
                     pair.add(new JsonPrimitive(currentDimension.toString()));
                  }
               }
               else
               {
                  pair.add(new JsonPrimitive(((Long) currentDimension).longValue()));
               }

               // (Re)initialize values

               count = 0;
               currentValue = 0;
               currentMaximum = 0;
               currentMinimum = Double.MAX_VALUE;
               currentSum = 0;
               currentSquareSum = 0;
            }

            count++;

            if (allowsCumulants)
            {
               currentValue = getFact(object);
               currentMaximum = Math.max(currentValue, currentMaximum);
               currentMinimum = Math.min(currentValue, currentMinimum);
               currentSum += currentValue;
               currentSquareSum += currentValue * currentValue;

               System.out.println("New Value " + currentValue);
               System.out.println("Minimum   " + currentMinimum);
               System.out.println("Maximum   " + currentMaximum);
            }
         }

         // Close last open interval

         if (pair != null)
         {
            System.out.println("Closing final interval " + count);

            if (allowsCumulants)
            {
               double average = currentSum / count;
               double sigma = count == 1 ? 0 : Math.sqrt((currentSquareSum - currentSum * currentSum / count) / count
                     - 1);

               System.out.println("===> Average         " + average);
               System.out.println("===> Sigma           " + sigma);
               System.out.println("===> Current Minimum " + currentMinimum);
               System.out.println("===> Current Maximum " + currentMaximum);

               pair.add(new JsonPrimitive(average - sigma));
               pair.add(new JsonPrimitive(currentMinimum));
               pair.add(new JsonPrimitive(currentMaximum));
               pair.add(new JsonPrimitive(average + sigma));
               pair.add(new JsonPrimitive(count));
            }
            else
            {
               pair.add(new JsonPrimitive(count));
            }
         }
      }

      return seriesGroup;
   }
}
