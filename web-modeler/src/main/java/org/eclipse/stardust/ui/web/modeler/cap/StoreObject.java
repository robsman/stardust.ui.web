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
package org.eclipse.stardust.ui.web.modeler.cap;

import java.awt.Point;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;

// object collects several informations needed for copy/paste
public class StoreObject
{
   public static int COMMIT = 1;
   public static int UPDATE = 2;

   private boolean copySymbols = false;
   // check if all is necessary
   private ModelType sourceModel;
   private ModelType targetModel;
   private ModelType originalModelCopy;

   private boolean sameModel = false;

   private boolean isDiagram = false;

   private DiagramType sourceDiagram;
   private DiagramType targetDiagram;
   private ProcessDefinitionType sourceProcess;
   // targetProcess is the real object
   private ProcessDefinitionType targetProcess;
   // the target Object (needed ?)
   private EObject targetObject;
   private Point mouseLocation;

   public boolean isCopySymbols()
   {
      return copySymbols;
   }
   public void setCopySymbols(boolean copySymbols)
   {
      this.copySymbols = copySymbols;
   }
   public Point getLocation()
   {
      return mouseLocation;
   }
   public void setLocation(Point location)
   {
      this.mouseLocation = location;
   }
   public DiagramType getSourceDiagram()
   {
      return sourceDiagram;
   }
   public void setSourceDiagram(DiagramType sourceDiagram)
   {
      this.sourceDiagram = sourceDiagram;
   }
   public ModelType getOriginalModelCopy()
   {
      return originalModelCopy;
   }
   public void setOriginalModelCopy(ModelType sourceModel)
   {
      this.originalModelCopy = sourceModel;
   }
   public ProcessDefinitionType getSourceProcess()
   {
      return sourceProcess;
   }
   public void setSourceProcess(ProcessDefinitionType sourceProcess)
   {
      this.sourceProcess = sourceProcess;
   }

   public ModelType getSourceModel()
   {
      return sourceModel;
   }
   public void setSourceModel(ModelType sourceModel)
   {
      this.sourceModel = sourceModel;
   }

   public ModelType getTargetModel()
   {
      return targetModel;
   }
   public void setTargetModel(ModelType targetModel)
   {
      this.targetModel = targetModel;
   }

   public ProcessDefinitionType getTargetProcess()
   {
      return targetProcess;
   }
   public void setTargetProcess(ProcessDefinitionType targetProcess)
   {
      this.targetProcess = targetProcess;

   }
   public boolean isSameModel()
   {
      return sameModel;
   }
   public void setSameModel(boolean sameModel)
   {
      this.sameModel = sameModel;
   }
   public EObject getTargetObject()
   {
      return targetObject;
   }
   public void setTargetObject(EObject targetObject)
   {
      this.targetObject = targetObject;
   }
   public boolean isDiagram()
   {
      return isDiagram;
   }
   public void setIsDiagram(boolean isDiagram)
   {
      this.isDiagram = isDiagram;
   }
   public void setTargetDiagram(DiagramType diagram)
   {
      targetDiagram = diagram;
   }
   public DiagramType getTargetDiagram()
   {
      return targetDiagram;
   }
}