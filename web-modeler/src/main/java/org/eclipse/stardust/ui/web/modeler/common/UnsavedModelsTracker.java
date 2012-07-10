package org.eclipse.stardust.ui.web.modeler.common;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Shrikant.Gangal
 * 
 */
public class UnsavedModelsTracker
{
   private static UnsavedModelsTracker tracker = new UnsavedModelsTracker();

   /**
    * @return
    */
   public static UnsavedModelsTracker getInstance()
   {
      return tracker;
   }

   private Set<String> unsavedModels = new HashSet<String>();

   /**
    * @param modelId
    */
   public synchronized void notifyModelModfied(String modelId)
   {
      unsavedModels.add(modelId);
   }

   /**
    * @param modelId
    */
   public synchronized void notifyAllModelsSaved()
   {
      unsavedModels.clear();
   }

   public Set<String> getUnsavedModels()
   {
      return unsavedModels;
   }
}
