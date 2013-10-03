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