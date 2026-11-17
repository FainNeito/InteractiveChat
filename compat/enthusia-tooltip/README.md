# Enthusia tooltip compatibility patch

This is a narrowly scoped source patch for the production InteractiveChat 2026.1.1.0 JAR. It converts the trusted Vault rank prefix into a formatted component for tab suggestions and resolves Nexo glyphs without parsing player names as markup. It is not a replacement for the upstream source build and does not apply to arbitrary InteractiveChat versions.

The unmodified input JAR must have SHA-256 `FDD389DEB66B393F2FCBFCBE7937F824CD7F329DB410194B77BD38BB94BBE76A`. Compile the two sources with Java 25 against that JAR, Paper API 26.2 build 135, and the server's unrelocated Adventure 5.2.0 libraries. The resulting class files can be used to replace the matching entries in a copy of the input JAR. Preserve the original JAR for rollback.

The checks in `tests` cover 23 configured rank prefixes, legacy and RGB formatting, an icon resolver and rejected action tags. The exact resulting production-derived patch was also tested on the test server. Nexo integration uses reflection because InteractiveChat's shaded Adventure classes and the server's Adventure classes are distinct.

The expected SHA-256 for `TooltipBridge.class` is `fa96abf9f8ebae8c40e2be9727c8e656fb26863f3875233f821898c0b46fae16`; for `OutTabCompletePacketHandler.class` it is `1ad042e54c062010972e0b023e9059d2c4b8f67880dc76df2d9f8633c904c7d5`. Verify the base JAR and class hashes before packaging. Never install two copies of InteractiveChat at once.
