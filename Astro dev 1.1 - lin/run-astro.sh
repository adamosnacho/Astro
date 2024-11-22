#!/bin/bash
cd "$(dirname "$0")"
java -Djava.library.path=lib/natives-linux -jar "Astro dev 1.1 - lin.jar"
