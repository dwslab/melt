@echo off

rem SET ADD_COMMAND="-Dtest=TestLocalFile"
SET ADD_COMMAND=

SET P1_RESULT=TRUE
call mvn clean test -P1 %ADD_COMMAND%
IF %ERRORLEVEL% NEQ 0 (
  SET P1_RESULT=FALSE
)

SET P2_RESULT=TRUE
call mvn clean test -P2 %ADD_COMMAND%
IF %ERRORLEVEL% NEQ 0 (
  SET P2_RESULT=FALSE
)

SET P3_RESULT=TRUE
call mvn clean test -P3 %ADD_COMMAND%
IF %ERRORLEVEL% NEQ 0 (
  SET P3_RESULT=FALSE
)

SET P4_RESULT=TRUE
call mvn clean test -P4 %ADD_COMMAND%
IF %ERRORLEVEL% NEQ 0 (
  SET P4_RESULT=FALSE
)

SET P5_RESULT=TRUE
call mvn clean test -P5 %ADD_COMMAND%
IF %ERRORLEVEL% NEQ 0 (
  SET P5_RESULT=FALSE
)

SET P6_RESULT=TRUE
call mvn clean test -P6 %ADD_COMMAND%
IF %ERRORLEVEL% NEQ 0 (
  SET P6_RESULT=FALSE
)

SET P7_RESULT=TRUE
call mvn clean test -P7 %ADD_COMMAND%
IF %ERRORLEVEL% NEQ 0 (
  SET P7_RESULT=FALSE
)

SET P8_RESULT=TRUE
call mvn clean test -P8 %ADD_COMMAND%
IF %ERRORLEVEL% NEQ 0 (
  SET P8_RESULT=FALSE
)

SET P9_RESULT=TRUE
call mvn clean test -P9 %ADD_COMMAND%
IF %ERRORLEVEL% NEQ 0 (
  SET P9_RESULT=FALSE
)

SET P10_RESULT=TRUE
call mvn clean test -P10 %ADD_COMMAND%
IF %ERRORLEVEL% NEQ 0 (
  SET P10_RESULT=FALSE
)

echo P1: Jena:2.9.4  OWLAPI:3.3    %P1_RESULT%
echo P2: Jena:2.11.2 OWLAPI:3.4.10 %P2_RESULT%
echo P3: Jena:2.13.0 OWLAPI:3.5.7  %P3_RESULT%
echo P4: Jena:3.0.1  OWLAPI:4.0.2  %P4_RESULT%
echo P5: Jena:3.2.0  OWLAPI:4.1.4  %P5_RESULT%
echo P6: Jena:3.4.0  OWLAPI:4.2.9  %P6_RESULT%
echo P7: Jena:3.6.0  OWLAPI:4.3.2  %P7_RESULT%
echo P8: Jena:3.8.0  OWLAPI:4.5.13 %P8_RESULT%
echo P9: Jena:3.10.0 OWLAPI:5.0.5  %P9_RESULT%
echo P10:Jena:3.12.0 OWLAPI:5.1.11 %P10_RESULT%