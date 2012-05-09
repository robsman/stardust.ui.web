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
function emitIppAiClosePanelCommand()
{
  //alert("Executing emitIppAiClosePanelCommand()");

  var workareaFrame = parent['ipp-embedded-activity-panel'];

  if (workareaFrame)
  {
    //alert("Found embedded AI panel iFrame: " + workareaFrame);
	  
    if (workareaFrame.performIppAiClosePanelCommand)
    {
      try
      {
      	workareaFrame.performIppAiClosePanelCommand();
      }
      catch (x)
      {
        // probably forbidden to access location, assuming other page
        alert(x);
      }
    }
    else
    {
      alert('Did not find performIppAiClosePanelCommand method in embedded AI panel iFrame');
    }
  }
}
