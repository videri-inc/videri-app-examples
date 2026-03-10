#!/bin/bash -e
BUCKET="videri-apks"

APK_PATH=app/build/outputs/apk/debug

./gradlew clean assembleDebug
PACKAGE=$(jq -r .applicationId $APK_PATH/output-metadata.json)
echo "package: $PACKAGE"
VERSION=$(jq -r .elements[0].versionName $APK_PATH/output-metadata.json)
echo "version: $VERSION"
OUT_FILE=$(jq -r .elements[0].outputFile $APK_PATH/output-metadata.json)
echo "output file: $OUT_FILE"
FILE_NAME=icanvas-interface-example-${VERSION}-${USER}-debug.apk
echo "file: $FILE_NAME"
aws s3 cp --acl public-read $APK_PATH/$OUT_FILE s3://${BUCKET}/$PACKAGE/$FILE_NAME
echo "https://${BUCKET}.s3.amazonaws.com/$PACKAGE/$FILE_NAME"