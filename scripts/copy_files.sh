#!/bin/sh

cp $MESON_SOURCE_ROOT/build.gradle $MESON_BUILD_ROOT
cp $MESON_SOURCE_ROOT/settings.gradle $MESON_BUILD_ROOT
cp -r $MESON_SOURCE_ROOT/src $MESON_BUILD_ROOT

OFFLINE_REPO_SOURCE=$MESON_SOURCE_ROOT/offline-repository

if test -d "$OFFLINE_REPO_SOURCE"; then
	echo "Found offline repo for flatpak build, moving it to build directory"
	cp -r $OFFLINE_REPO_SOURCE $MESON_BUILD_ROOT
fi
