#! /bin/bash

cd bin/ \
  && jar cvfm ../../BukkitUpdater.jar ../MANIFEST.MF * \
  && cp ../../BukkitUpdater.jar ../ \
  && md5sum ../BukkitUpdater.jar > ../BukkitUpdater.jar.md5
