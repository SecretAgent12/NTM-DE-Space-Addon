# NTM DE: Space

[NTM: Space](https://github.com/JameH2/Hbm-s-Nuclear-Tech-GIT), the space travel addon for Hbm's Nuclear Tech
Mod, for [NTM: Decentralized Edition](https://github.com/SecretAgent12/NTM-Decentralized-Edition) on
Minecraft 1.21.1 (NeoForge): rockets, planets, moons, spacesuits and atmospheres.

It is based on movblock's Minecraft 26.2 port of NTM: Space by JamesH_2 and Mellow
([ntm-next-space](https://github.com/Warfactory-Official/ntm-next-space)), backported by me to 1.21.1 as an
addon of NTM: Decentralized Edition.

**This is an unofficial build.** It is not affiliated with HbmMods, the NTM: Space authors or Warfactory.
Please report bugs of this build to me, in the [issues of this repository](https://github.com/SecretAgent12/NTM-DE-Space-Addon/issues),
not upstream.

## Requirements

- Minecraft 1.21.1, NeoForge 21.1
- NTM: Decentralized Edition, **exactly** the version this build is made for (`de_version` in
  `gradle.properties`). Each Space version is built for one NTM: DE version and will not load with another.

## Building

The core is not taken from a maven repository: this project compiles and runs against the jar of an
NTM: Decentralized Edition checkout next to it.

1. Clone NTM: Decentralized Edition next to this folder and build it (`gradlew build`). Its folder and
   version are `de_project` and `de_version` in `gradle.properties`.
2. Here: `gradlew build` (the jar ends up in `build/libs`), `gradlew runClient` for a dev run.

Dev runs only (`gradle.properties`, nothing of it ends up in the jar):

- `dev_sodium`: Sodium in the dev client (`false`: the vanilla renderer)
- `dev_stack`: `aeronautics` (Create, Sable and Create: Aeronautics, as in the core), `scalablelux` or `none`

## How the port is done

Most of the code is the 26.2 port's, brought to 1.21.1 by the same conversion tooling as the core and by hand.
Changes against the 26.2 port are marked in the code: `// backport:` for deliberate differences,
`// backport-fix:` for bug fixes.

## Credits

- **JamesH_2 and Mellow** and the NTM: Space community: NTM: Space
- **movblock**: the Minecraft 26.2 port this build is based on
- **HbmMods (The Bobcat)** and contributors: Hbm's Nuclear Tech Mod
- **SecretAgent12** (me): the backport to 1.21.1 for NTM: Decentralized Edition

## License

LGPL-3.0-only, like NTM: Space and Hbm's Nuclear Tech Mod. See `NOTICE.md` for what was changed and who wrote
what, `LICENSE.LESSER` / `LICENSE` for the license texts and `REUSE.toml` for per-file attribution.

**Forks and redistribution.** You are free to fork NTM DE: Space, use it in modpacks and build on it under
the LGPL-3.0. My own code comes with a few additional terms (section 7 of the GPL v3, see
[NOTICE.md](NOTICE.md)), and the first one applies to everyone: keep the copyright headers in my files.

- A fork of NTM DE: Space shows "Based on NTM DE: Space by SecretAgent12" in its mod metadata and at the
  beginning of its project page, lists me among its authors and uses its own name.
- A project that only takes some of my files adds "Includes code from NTM DE: Space by SecretAgent12" to
  its mod metadata and mentions it, with a link, in its description or credits.
- Modpacks, server packs and mods that bundle the unmodified jar don't need to do anything.

**Contributions** are accepted under the LGPL-3.0. The additional terms in NOTICE.md apply only to
SecretAgent12's own files, not to contributions by others.
