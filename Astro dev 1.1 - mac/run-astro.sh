#!/bin/bash
cd "$(dirname "$0")"
java -Djava.library.path=lib/natives-mac -jar "Astro dev 1.1 - mac.jar"
