#!/bin/bash

if [ $# -ne "1" ]
then
  echo
  echo "Give the version to build as the command line argument!"
  echo "e.g. HEAD, GROUPER_1_3_1, etc"
  echo "e.g. buildGrouper.sh HEAD"
  echo
  exit 1
fi  

OBJ=subject

SOURCE_DIR=$CWD/..

cd /tmp
if [ ! -d $HOME/tmp/$OBJ ]; then
  /bin/mkdir $HOME/tmp/$OBJ
  /bin/chmod g+w $HOME/tmp/$OBJ
fi

cd $HOME/tmp/$OBJ

export buildDir=$HOME/tmp/$OBJ/build_$USER

if [ -d $buildDir ]; then
  /bin/rm -rf $buildDir
fi

if [ ! -d $buildDir ]; then
  /bin/mkdir $buildDir
fi

cd $buildDir

git clone -l $SOURCE_DIR

cd $buildDir/$OBJ

$M2_HOME/bin/mvn install -DskipTests

echo
echo "regular result is in $buildDir/" 
echo "binary result is in $buildDir/" 
echo

#allow someone from group to delete later on
/bin/chmod -R g+w $buildDir
