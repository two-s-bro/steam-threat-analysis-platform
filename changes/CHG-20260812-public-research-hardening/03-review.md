# Independent review

## Round 1

- Frozen tree: `77fb2d58d2cc36891c15162bd7989e0db4eb8aab`
- Decision: `REWORK`

### Findings

1. P1: serialized/double-backslash user-home paths bypassed the scanner and one remained in `docs/02_持久化机制分析.md`.
2. P1: two reports exposed code-level phishing UI and route-injection reconstruction details.
3. P1: the offline C2 service fabricated fallback history and presented unverified disconnected/clean states.
4. P2: two reports disagreed on authoritative source-log line counts.

### Rework

- Removed the remaining private path and added single/double separator, case, and drive-letter regression tests.
- Replaced code/DOM/CSS excerpts with non-executable defensive summaries and detection boundaries.
- Replaced online booleans and fabricated fallback data with `NOT_CHECKED` and `HISTORICAL_OBSERVATION`; added empty and populated repository tests.
- Unified the authoritative counts at 2,595 + 18 + 104 = 2,717 source lines and marked the counting method/date.

Status: REWORK_VERIFIED_PENDING_REREVIEW

## Round 2

- Frozen tree: `99f7103882d3c767e7afa5a64f5b3d6ebd345e45`
- Decision: `REWORK`

### Findings

1. P1: the scanner rebuilt audited victim values from reversible string fragments.
2. P1: an event-specific drive-absolute Steam path remained in SQL and a Java comment, while the scanner covered only user-home paths.

### Rework

- Replaced all reversible literals/fragments with SHA-256 fingerprints and generic contextual identifier rules; tests remain entirely synthetic.
- Replaced drive-absolute event paths with `%STEAM_ROOT%` and `%SystemRoot%` placeholders.
- Expanded path rejection and tests to all Windows drive-absolute paths, including single/double separators, case variants, non-default drives, and non-home directories.

Status: REWORK_ROUND_2_VERIFIED_PENDING_REREVIEW

## Round 3

- Frozen tree: `2b402da018849f4f541da0dce2a1b105addf9cf5`
- Decision: `PASS`
- Findings: none (P0-P3)

The reviewer independently reran the public-safety scan (97 files), path/fingerprint tests (3/3), and cached diff check. It confirmed that no reversible victim fragments or Windows drive-absolute paths remained, and that earlier UI/JS, C2 status, and source-count fixes had not regressed.

Status: PASS_PENDING_PUBLICATION
