package org.eclipse.stardust.ui.web.modeler.marshaling;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface ModelMarshaller
{
   JsonElement toJson(EObject element);

   JsonObject toModelJson(EObject model);

   JsonObject  toProcessDiagramJson(EObject model, String processId);

   /**
    * Invoked before the client starts marshaling a set of elements sequentially.
    * Useful to set up caches.
    */
   void init();

   /**
    * Invoked after the client finished marshaling a set of elements.
    * Useful to cleanup caches.
    */
   void done();
}
