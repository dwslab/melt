#!/bin/bash

#ADD_COMMAND="-Dtest=TestLocalFile"
ADD_COMMAND=

P1_RESULT=true
mvn clean test -P1 $ADD_COMMAND
if [ "$?" -ne 0 ]; then
   P1_RESULT=false
fi

P2_RESULT=true
mvn clean test -P2 $ADD_COMMAND
if [ "$?" -ne 0 ]; then
   P2_RESULT=false
fi

P3_RESULT=true
mvn clean test -P3 $ADD_COMMAND
if [ "$?" -ne 0 ]; then
   P3_RESULT=false
fi

P4_RESULT=true
mvn clean test -P4 $ADD_COMMAND
if [ "$?" -ne 0 ]; then
   P4_RESULT=false
fi

P5_RESULT=true
mvn clean test -P5 $ADD_COMMAND
if [ "$?" -ne 0 ]; then
   P5_RESULT=false
fi

P6_RESULT=true
mvn clean test -P6 $ADD_COMMAND
if [ "$?" -ne 0 ]; then
   P6_RESULT=false
fi

P7_RESULT=true
mvn clean test -P7 $ADD_COMMAND
if [ "$?" -ne 0 ]; then
   P7_RESULT=false
fi

P8_RESULT=true
mvn clean test -P8 $ADD_COMMAND
if [ "$?" -ne 0 ]; then
   P8_RESULT=false
fi

P9_RESULT=true
mvn clean test -P9 $ADD_COMMAND
if [ "$?" -ne 0 ]; then
   P9_RESULT=false
fi

P10_RESULT=true
mvn clean test -P10 $ADD_COMMAND
if [ "$?" -ne 0 ]; then
   P10_RESULT=false
fi

echo "P1: Jena:2.9.4  OWLAPI:3.3    $P1_RESULT"
echo "P2: Jena:2.11.2 OWLAPI:3.4.10 $P2_RESULT"
echo "P3: Jena:2.13.0 OWLAPI:3.5.7  $P3_RESULT"
echo "P4: Jena:3.0.1  OWLAPI:4.0.2  $P4_RESULT"
echo "P5: Jena:3.2.0  OWLAPI:4.1.4  $P5_RESULT"
echo "P6: Jena:3.4.0  OWLAPI:4.2.9  $P6_RESULT"
echo "P7: Jena:3.6.0  OWLAPI:4.3.2  $P7_RESULT"
echo "P8: Jena:3.8.0  OWLAPI:4.5.13 $P8_RESULT"
echo "P9: Jena:3.10.0 OWLAPI:5.0.5  $P9_RESULT"
echo "P10:Jena:3.12.0 OWLAPI:5.1.11 $P10_RESULT"