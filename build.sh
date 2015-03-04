#!/bin/bash
export JAVA_HOME=`/usr/libexec/java_home -v1.7`
activator update
activator dist
rsync target/universal/mediadb-1.0-SNAPSHOT.zip root@192.168.168.74:/root/