package org.eclipse.stardust.ui.web.modeler.edit.batch;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonPathEvaluator
{
   private static Pattern STEP_PATTERN = Pattern.compile("(\\w*)(?:\\[([^\\]]*)\\])?");

   private static Pattern PREDIACTE_PATTERN = Pattern.compile("(\\w*)\\='(\\w*)'");

   private static Pattern ARRAY_ELEMENT_PATTERN = Pattern.compile("-?\\d+");

   public JsonElement resolveExpression(JsonElement inputJson, String path)
   {
      Iterable<String> steps = Splitter.on("/").split(path);

      JsonElement currentValue = inputJson;

      for (String step : steps)
      {
         JsonElement resolvedValue = null;

         String property;
         String predicate;
         Matcher stepMatcher = STEP_PATTERN.matcher(step);
         if (stepMatcher.matches())
         {
            property = stepMatcher.group(1);
            predicate = stepMatcher.group(2);
         }
         else
         {
            throw new IllegalAccessError("Unsupported path segment: " + step);
         }

         if (currentValue.isJsonObject())
         {
            JsonObject jsonObject = currentValue.getAsJsonObject();

            if (isEmpty(property))
            {
               // no property specified, resolve to first property satisfying the predicate
               resolvedValue = findPropertyFromObject(jsonObject, predicate);
            }
            else
            {
               JsonElement value = jsonObject.get(property);
               if (null != predicate)
               {
                  if ((null != value) && value.isJsonArray())
                  {
                     // traverse array according to predicate
                     resolvedValue = findElementFromArray(value.getAsJsonArray(),
                           predicate);
                  }
                  else if (isMatchForPredicate(value, predicate))
                  {
                     resolvedValue = value;
                  }
               }
               else
               {
                  // no predicate, thus found a match
                  resolvedValue = value;
               }
            }
         }
         else if (currentValue.isJsonArray())
         {
            JsonArray jsonArray = currentValue.getAsJsonArray();
            if (null != predicate)
            {
               resolvedValue = findElementFromArray(jsonArray, predicate);
            }
            else if (isEmpty(property))
            {
               resolvedValue = currentValue;
            }
            else
            {
               throw new IllegalArgumentException("Can't resolve from array: " + step);
            }
         }

         if (null != resolvedValue)
         {
            currentValue = resolvedValue;
         }
         else
         {
            throw new IllegalArgumentException("Unresolvable step: " + step);
         }
      }

      return currentValue;
   }

   private JsonElement findElementFromArray(JsonArray jsonArray, String predicate)
   {
      Matcher elementPosMatcher = ARRAY_ELEMENT_PATTERN.matcher(predicate);
      if (elementPosMatcher.matches())
      {
         int elementPos = Integer.parseInt(elementPosMatcher.group(0));
         return jsonArray.get(elementPos);
      }
      else
      {
         // match first element from array satisfying the predicate
         for (JsonElement element : jsonArray)
         {
            if (isMatchForPredicate(element, predicate))
            {
               return element;
            }
         }
      }

      return null;
   }

   private JsonElement findPropertyFromObject(JsonObject jsonObject, String predicate)
   {
      // match first property from object satisfying the predicate
      for (Map.Entry<String, JsonElement> memberEntry : jsonObject.entrySet())
      {
         if (isMatchForPredicate(memberEntry.getValue(), predicate))
         {
            return memberEntry.getValue();
         }
      }
      return null;
   }

   private boolean isMatchForPredicate(JsonElement element, String predicate)
   {
      if (null == predicate)
      {
         return true;
      }
      else
      {
         Matcher predicateMatcher = PREDIACTE_PATTERN.matcher(predicate);
         if (predicateMatcher.matches())
         {
            String predProperty = predicateMatcher.group(1);
            String predValue = predicateMatcher.group(2);

            if (element.isJsonObject())
            {
               JsonObject jsonObject = element.getAsJsonObject();

               // filter by predicate
               if (jsonObject.has(predProperty))
               {
                  JsonElement propValue = jsonObject.get(predProperty);

                  if (propValue.isJsonPrimitive()
                        && propValue.getAsJsonPrimitive().isString())
                  {
                     if (propValue.getAsString().equals(predValue))
                     {
                        return true;
                     }
                  }
               }
            }
         }
         else
         {
            throw new IllegalArgumentException("Unsupported predicate syntax: "
                  + predicate);
         }
      }
      return false;
   }
}
