# NTM DE: Space — NTM: Space for NTM: Decentralized Edition (Minecraft 1.21.1)

This is a **modified version** of movblock's Minecraft 26.2 port of NTM: Space
([`ntm-next-space`](https://github.com/Warfactory-Official/ntm-next-space), commit `48ec089`, NeoForge
module), itself a port of [NTM: Space](https://github.com/JameH2/Hbm-s-Nuclear-Tech-GIT) by JamesH_2 and
Mellow and the NTM: Space community, an addon of
[Hbm's Nuclear Tech Mod](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT) by HbmMods (The Bobcat) and
contributors.

Modified by **SecretAgent12**, 2026: backported to Minecraft 1.21.1 / NeoForge 21.1 as an addon of
[NTM: Decentralized Edition](https://github.com/SecretAgent12/NTM-Decentralized-Edition) (the exact core
version is `de_version` in `gradle.properties`).

## What changed and who wrote what

- **Files from the 26.2 port** keep their original copyright lines (Java files in their SPDX headers,
  everything else in `REUSE.toml`). Most of them were changed by the backport: mechanically by conversion
  tooling (Minecraft 26.2 → 1.21.1 API, resource formats) and by hand. Deliberate deviations from the 26.2
  port are marked with `// backport:` comments in the code, fixes with `// backport-fix:`.
- **Files that exist only in the backport** carry `SPDX-FileCopyrightText: 2026 SecretAgent12`. Where such a
  file contains code moved out of the 26.2 port or out of NTM (for example the multiblock shapes in
  `com.hbmspace.blocks.multiblock`), it also carries movblock's line.

## Licenses

- The mod as a whole: **GNU LGPL-3.0-only** (`LICENSE.LESSER`, which incorporates `LICENSE` — GPL-3.0),
  the license of NTM: Space, its 26.2 port and Hbm's Nuclear Tech Mod.
- The Gradle wrapper: Apache-2.0 (`LICENSES/Apache-2.0.txt`).
- Other third-party code and assets: see `THIRD_PARTY_NOTICES` and `REUSE.toml`. The repository is
  [REUSE](https://reuse.software) compliant (`reuse lint`).

Whoever receives a built jar of this mod is entitled to its corresponding source code under the
terms of the LGPL.

## Additional terms (section 7 of the GNU GPL v3, as incorporated by the LGPL-3.0)

These additional terms apply to the files in this project whose copyright header names
SecretAgent12 as the only copyright holder (`SPDX-FileCopyrightText: 2026 SecretAgent12`)
(the "Material"). Each of these files states that the terms apply.
They do not apply to code from NTM: Space, its Minecraft 26.2 port, NTM: NEXT, Hbm's Nuclear Tech Mod
or third parties, or to contributions by other people, which stay under their own terms. They apply to
the releases whose source code includes this section.

An "unmodified release of NTM DE: Space" is a jar published by SecretAgent12, or a jar built without
changes from a release tag or a commit on a branch of
https://github.com/SecretAgent12/NTM-DE-Space-Addon, with its name, version and metadata
unchanged.

A "modified version of NTM DE: Space" is a work built from the NTM DE: Space source as a whole, directly or
through other forks (a fork), rather than one that takes individual files from it; or a copy of a
release with a changed name, version, metadata or contents.

1. **Notices (7b).** Keep every copyright notice, SPDX line, license notice and author attribution
   in the Material intact, in the files where they appear. Moving this information into a separate
   file (such as REUSE.toml or a NOTICE inside the jar) and removing it from the files does not
   satisfy this term.

2. **Attribution in the work (7b).** A work that contains the Material, other than an unmodified
   release of NTM DE: Space or a work that includes one only as an unchanged, separate file (such as a
   modpack, a server pack or a mod that bundles it as a jar-in-jar dependency), must show in its
   Appropriate Legal Notices (for a Minecraft mod: the metadata shown in the in-game mod list,
   e.g. `neoforge.mods.toml`):
   - a modified version of NTM DE: Space: the line
     "Based on NTM DE: Space by SecretAgent12 — https://github.com/SecretAgent12/NTM-DE-Space-Addon",
     and it must list SecretAgent12 among its authors;
   - any other work: the line
     "Includes code from NTM DE: Space by SecretAgent12 — https://github.com/SecretAgent12/NTM-DE-Space-Addon".

3. **Origin (7c).** Do not misrepresent the origin of the Material. In particular, do not present
   the Material, or a work that contains it, as your own original work or your own port.
   A modified version of NTM DE: Space must be marked as different from the original:
   - it must use a different name and a version number that cannot be confused with an NTM DE: Space
     release;
   - wherever it is published (project page, repository README, release page), its description
     must state prominently, at the beginning of the description and before the first image,
     video or banner, that it is based on NTM DE: Space by SecretAgent12, with a link
     to the original project. A mention only in a changelog, a credits list further down, or a
     file inside the download is not enough.

   Any other work that contains the Material (other than the works exempted in term 2) must state,
   wherever it is published, in its description or its credits, that it includes code from
   NTM DE: Space by SecretAgent12, with a link to the original project.

4. **Publicity (7d).** The names "SecretAgent12" and "NTM DE: Space" may not be used to
   promote or endorse a work that contains the Material, beyond the attribution required above and
   factual statements about its origin.

5. **Names and logo (7e).** No rights are granted to the names "NTM DE: Space" and "NTM: DE Space",
   to names confusingly similar to them, or to the NTM DE: Space logo, except to refer to this project
   and to redistribute unmodified releases.

These terms do not restrict:
- redistributing unmodified releases, on their own or as unchanged files inside other works
  (modpacks, server packs, jar-in-jar, mirrors);
- datapacks, KubeJS scripts, resource packs, configs and addons that do not contain the Material;
- contributions to this repository.

**Upstream projects.** The official repositories and releases of NTM: NEXT
(https://github.com/Warfactory-Official/ntm-next), of its port of NTM: Space
(https://github.com/Warfactory-Official/ntm-next-space) and of NTM: Space
(https://github.com/JameH2/Hbm-s-Nuclear-Tech-GIT) may incorporate the Material without
following terms 2 and 3, as long as term 1 (keeping the file headers) is followed. This permission
covers only those projects themselves, not other works based on them.

If these terms are not followed, section 8 of the GNU GPL v3 applies: your rights under the
license end, and can be restored as described there (for example, by fixing the problem within
30 days of the first notice).
