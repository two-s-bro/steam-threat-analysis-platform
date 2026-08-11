# CHG-20260812: Public research hardening

## Status

REVIEW_PASS_PENDING_PUBLICATION

## Request

Prepare `two-s-bro/steam-threat-analysis-platform` as a safe, truthful, actively maintained public research repository suitable for an OpenAI Codex for Open Source application.

## Acceptance criteria

- The current public tree contains no victim Steam ID, account identifier, avatar hash, private workstation path, clickable malicious infrastructure, or hard-coded database password.
- No executable, DLL, bytecode, payload, archive, raw malware log, or recoverable malware source is distributed.
- Public evidence is limited to hashes, defanged indicators, defensive analysis, and synthetic or redacted fixtures.
- The application imports only bundled sanitized samples and never reads the private evidence directory.
- Detection UI does not equate a rule miss with proof of safety.
- CI verifies public-safety policy, backend tests, and frontend build with read-only permissions and pinned actions.
- Maintainer, contribution, security, roadmap, and release expectations are documented truthfully.

## Explicit exclusions

- Executing, compiling, unpacking, decompiling, or uploading private malware artifacts.
- Rewriting or force-pushing Git history. Historical exposure remains a separately authorized cleanup.
- Fabricating users, stars, downloads, contributors, maintenance history, or adoption claims.
- Submitting the official application before the maintainer confirms identity-specific form fields.

## Change budget

- Production code/config: at most 15 files and 1,000 changed lines. The budget increased for truth-status propagation through both C2 API projections and the dashboard; acceptance scope did not expand.
- Support, test, policy, documentation, and CI: at most 52 files. The final adjustment covers one additional component report containing an equivalent injection-method hypothesis; acceptance scope did not expand.
- External effects: account `two-s-bro`; repository `two-s-bro/steam-threat-analysis-platform`; at most one commit on branch `agent/harden-public-research-release`, one branch ref update, and one Draft PR. No direct write to `main`, release, or form submission.

## Rollback

Close the Draft PR and delete its branch, or revert its single commit after merge. `main` remains untouched during review.
