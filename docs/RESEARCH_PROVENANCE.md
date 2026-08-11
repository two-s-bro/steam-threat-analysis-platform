# Research provenance and evidence tiers

## Source event

The project is derived from a privately retained Steam client hijacking incident observed on 2026-06-15 and 2026-06-16. Initial documentation was prepared on 2026-06-17. The raw corpus remains outside this repository.

Read-only inventory on 2026-08-12 established:

- 973 files totaling approximately 105,042,544 bytes;
- 2,595 lines in the patch log;
- 18 lines in the startup log;
- 104 lines in the historical download/heartbeat log;
- eight selected artifact hashes published in `evidence/sample-hashes.json`.

No artifact was executed, compiled, unpacked, uploaded, moved, or modified during the public-repository audit.

## Evidence tiers

| Tier | Meaning | Public example |
|------|---------|----------------|
| Verified metadata | Recomputed without executing a sample | SHA-256, byte size, line count |
| Direct observation | Present in retained logs or filesystem inventory | timestamp, filename, Run-key name |
| Defensive inference | Supported by multiple observations but not dynamically confirmed | likely component role or attack phase |
| Unverified hypothesis | Plausible but not established | encrypted payload contents or exact injection method |

Documentation should use these labels and avoid upgrading an inference into a fact.

## Publication transformation

The public dataset is produced by selecting only defense-relevant records, removing victim and workstation identifiers, replacing infrastructure dots with `[.]`, replacing URL schemes with `hxxp(s)`, and re-running a sanitizer before persistence. The 11 bundled lines are illustrative redacted fixtures, not a full copy of the source logs.

## Reproducibility

Readers can validate the structure, hashes, tests, and transformation policy without receiving a sample. Independent sample matching should be performed only by an authorized security team that already possesses lawfully obtained evidence; this project will not distribute it.
