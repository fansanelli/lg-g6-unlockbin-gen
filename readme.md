# LG G6 unlock.bin Generator

Use this utility to check if you have a valid unlock.bin for your LG G6 (may also work for LG G5).

After compiling, run it in with your terminal: java -jar lg-bf.jar

Imei: 356144087429995
DeviceId: 662CDCF3D09A5AED38E08DB652EC4CC6F63B24DADB2332BC0C7CD30A9924D731
OutputFile: ~/git/lg-g6-unlockbin-gen/sample/unlock.bin
Sun Oct 29 08:00:08 CET 2023 - File exists and length matches!
Sun Oct 29 08:00:08 CET 2023 - Magic numbers found and correct!
Sun Oct 29 08:00:08 CET 2023 - Start attacking first signature
Sun Oct 29 08:00:08 CET 2023 - First signature verified
Sun Oct 29 08:00:08 CET 2023 - Start attacking second signature
Sun Oct 29 08:00:08 CET 2023 - Second signature verified
Sun Oct 29 08:00:08 CET 2023 - Writing file to disk

Mostly based on: https://github.com/jaehyek/lk/blob/master/platform/lge_shared/lge_verified_boot.c

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
