#!/bin/bash

# File to check for
FILE="dokka-cli-1.9.20.jar"

# URLs to download from
URLS=(
    "https://repo1.maven.org/maven2/org/jetbrains/dokka/dokka-cli/1.9.20/dokka-cli-1.9.20.jar"
    "https://repo1.maven.org/maven2/org/jetbrains/dokka/dokka-base/1.9.20/dokka-base-1.9.20.jar"
    "https://repo1.maven.org/maven2/org/jetbrains/dokka/analysis-kotlin-descriptors/1.9.20/analysis-kotlin-descriptors-1.9.20.jar"
    "https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-html-jvm/0.8.0/kotlinx-html-jvm-0.8.0.jar"
    "https://repo1.maven.org/maven2/org/freemarker/freemarker/2.3.31/freemarker-2.3.31.jar"
)

# Check if the file exists
if [ ! -f "$FILE" ]; then
    echo "$FILE does not exist. Downloading files..."
    for URL in "${URLS[@]}"; do
        wget "$URL"
    done
fi

# Run Dokka
java -jar $FILE dokka-configuration.json