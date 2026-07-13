#!/bin/bash
IMAGE_PATH="/home/kenned/.gemini/antigravity-ide/brain/11f844bf-8f58-41eb-ac8e-fef6821fdafc/tessera_app_icon_1783903224374.png"

# Convert to different sizes and replace ic_launcher.webp and ic_launcher_round.webp
convert "$IMAGE_PATH" -resize 192x192 app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp
convert "$IMAGE_PATH" -resize 192x192 app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp

convert "$IMAGE_PATH" -resize 144x144 app/src/main/res/mipmap-xxhdpi/ic_launcher.webp
convert "$IMAGE_PATH" -resize 144x144 app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp

convert "$IMAGE_PATH" -resize 96x96 app/src/main/res/mipmap-xhdpi/ic_launcher.webp
convert "$IMAGE_PATH" -resize 96x96 app/src/main/res/mipmap-xhdpi/ic_launcher_round.webp

convert "$IMAGE_PATH" -resize 72x72 app/src/main/res/mipmap-hdpi/ic_launcher.webp
convert "$IMAGE_PATH" -resize 72x72 app/src/main/res/mipmap-hdpi/ic_launcher_round.webp

convert "$IMAGE_PATH" -resize 48x48 app/src/main/res/mipmap-mdpi/ic_launcher.webp
convert "$IMAGE_PATH" -resize 48x48 app/src/main/res/mipmap-mdpi/ic_launcher_round.webp

echo "Icons updated successfully."
