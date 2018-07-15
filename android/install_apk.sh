#!/usr/bin/env bash

##################################################################
#  PREPARE ENVIRONMENT
##################################################################
path=$(pwd)

$ANDROID_SDK/platform-tools/adb install -r ${path}/bin/eTamagotchi.apk
