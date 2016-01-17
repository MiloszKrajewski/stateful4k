@echo off
set mvn=call "%MAVEN_HOME%"\bin\mvn.cmd

pushd %~dp0
%mvn% install
popd
