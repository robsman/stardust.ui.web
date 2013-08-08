@rem ***************************************************************************
@rem Copyright (c) 2011 SunGard CSA LLC and others.
@rem All rights reserved. This program and the accompanying materials
@rem are made available under the terms of the Eclipse Public License v1.0
@rem which accompanies this distribution, and is available at
@rem http://www.eclipse.org/legal/epl-v10.html
@rem
@rem Contributors:
@rem    SunGard CSA LLC - initial API and implementation and/or initial documentation
@rem ***************************************************************************
@echo off
setlocal

if "%ANT_HOME%"=="" (set ANT_HOME=.)

if exist %ANT_HOME%\bin\ant.bat (set ANT_EXE=%ANT_HOME%\bin\ant.bat
) else if exist %CM_HOME%\bin\antit17.bat (set ANT_EXE=%CM_HOME%\bin\antit17.bat
) else (
  echo Cannot find ANT executable. Please set environment variable: ANT_HOME & goto eof
)

%ANT_EXE% %*

:eof

endlocal