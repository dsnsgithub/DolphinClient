# AxolotlClient-Fork

Fork of https://codeberg.org/AxolotlClient/AxolotlClient-mod.

To build:

```
./gradlew build
```

Use `-Paxolotlclient.modules.<version_name>=true` to add a version to the build. Add `-Paxolotlclient.modules.all=true`
to build everything. In case you use an IDE, you can also add the respective properties to `gradle.properties` files
(or remove them).
