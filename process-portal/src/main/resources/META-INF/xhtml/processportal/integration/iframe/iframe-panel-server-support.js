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
function performIppAiClosePanelCommand()
{
  // alert("Executing iFrame performIppAiClosePanelCommand()");

  try
  {
	  var theForm = document.forms['ippIcefacesPanelForm'];
	  if ( !theForm) {
		  // alert('Falling back to first defined form: ' + document.forms[0]);
		  theForm = document.forms[0];
	  }
	  
      if ('function' === typeof submitForm) {
    	  // Trinidad
          submitForm(theForm, null, null);
      } else if ('function' === typeof iceSubmit) {
    	  // ICEfaces
    	  iceSubmit(theForm, null, null);
      }
	  return;
  }
  catch (x) {
	  alert('Failed submitting form: ' + x);
  }
}

function confirmIppAiClosePanelCommand(commandId)
{
  // Covering IPP for iFrame Scenario 
  // Adding case for HTML5 Framework Move
  var mainIppFrame = parent ? (parent.ippPortalMain ? parent.ippPortalMain : parent) : top;

  if (mainIppFrame)
  {
    if (mainIppFrame.InfinityBpm && mainIppFrame.InfinityBpm.ProcessPortal)
    {
    	if (mainIppFrame.InfinityBpm.ProcessPortal.confirmCloseCommandFromExternalWebApp) 
    	{
    		mainIppFrame.InfinityBpm.ProcessPortal.confirmCloseCommandFromExternalWebApp(commandId);
    	}
    	else
    	{
    	      try
    	      {
    	        if ('complete' === commandId) {
    	        	mainIppFrame.InfinityBpm.ProcessPortal.completeActivity();
    	        } else if ('qaPass' === commandId) {
    	        	mainIppFrame.InfinityBpm.ProcessPortal.qaPassActivity();
    	        } else if ('qaFail' === commandId) {
    	        	mainIppFrame.InfinityBpm.ProcessPortal.qaFailActivity();
    	        } else if ('suspendAndSave' === commandId) {
    	        	mainIppFrame.InfinityBpm.ProcessPortal.suspendActivity(true);
    	        } else if ('suspend' === commandId) {
    	        	mainIppFrame.InfinityBpm.ProcessPortal.suspendActivity(false);
    	        } else if ('abort' === commandId) {
    	        	mainIppFrame.InfinityBpm.ProcessPortal.abortActivity();
    	        }
    	      }
    	      catch (x)
    	      {
    	        // probably forbidden to access location, assuming other page
    	        alert("Failed confirming close panel command: " + x.message);
    	      }
    	}
    }
    else
    {
		// trying postMessage
		try
		{
			if (mainIppFrame.postMessage)
			{
				sleep(2000);
				mainIppFrame.postMessage(commandId, "*");
				sleep(3000);
			}
			else
			{
				alert("postMessage is not supported by main window" + mainIppFrame);
			}
		}
		catch (x2)
		{
		}
    }
  }
  else
  {
	  alert('Did not find BPM Main Frame.');  
  }
  
  function sleep(ms)
  {
	var dt = new Date();
	dt.setTime(dt.getTime() + ms);
	while (new Date().getTime() < dt.getTime());
  }
}
