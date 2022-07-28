### TaskerAppFactory

POC for fixing Tasker App Factory issue of not being able to build apks that have `targetSdkVersion >= 30` as per https://www.reddit.com/r/tasker/comments/vz1s0f/comment/igwmgg9/ to decompress `resources.arsc` and `zipalign` the `APK` with `zip` , `unzip` and `zipalign` binaries.

Bootstrap zips are compiled for `net.dinglisch.android.appfactory` package as per https://github.com/termux/termux-packages/wiki/For-maintainers#build-bootstrap-archives with the packages `aapt`, `zip` and `unzip` only and only support android `>= 7`, with additional files removed manually. The bootstrap zips cannot be used in other app packages or secondary users or will get linker errors due to wrong `$PREFIX` of libraries.
##
