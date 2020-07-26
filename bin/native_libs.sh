#!/bin/bash

if [ ! -x "$(command -v ninja)" ]; then

#  apt install -y ninja-build

  echo "install ninja-build please!"

  exit 1

fi

if [ ! -x "$(command -v cmake)" ]; then

#  apt install -y cmake

  echo "install cmake please!"

  exit 1

fi

if [ -f "$ANDROID_HOME/ndk-bundle/source.properties" ]; then

  export NDK=$ANDROID_HOME/ndk-bundle

else

  export NDK=$ANDROID_HOME/ndk/21.3.6528147

fi

export NINJA_PATH="$(command -v ninja)"

export PATH=$(echo "$ANDROID_HOME"/cmake/*/bin):$PATH

cd TMessagesProj/jni || exit 1

git submodule update --init --recursive

[ -d "ffmpeg/build" ] || ./build_ffmpeg_clang.sh

rm -rf ffmpeg/toolchain-android

./patch_ffmpeg.sh

./patch_boringssl.sh

[ -d "boringssl/build" ] || ./build_boringssl.sh