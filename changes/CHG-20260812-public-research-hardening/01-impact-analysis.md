# Impact analysis

## Risk classification

Strict. This is a public security-research repository derived from a live malware incident and includes an external GitHub publication step.

## Authoritative baseline

- Remote repository: `two-s-bro/steam-threat-analysis-platform`
- Remote branch: `main`
- Remote HEAD at analysis time: `aaa6c3b4e6ed03f3a176289e89340b3d0bfe3e01`
- Local reconstruction baseline: `5d3d52d745b25eb52bd085fb4b7aaf0b5e6fc835`

The local baseline reconstructs the remote tree for offline work and is not represented as the original Git history.

## Affected surfaces

- Backend ingestion and display of timeline evidence.
- Offline C2 metadata shown by the API.
- Frontend labels for bundled samples and rule-matching results.
- SQL demo fixtures and defensive detection rules.
- Public documentation, contributor workflow, and release process.
- GitHub Actions pull-request checks.

## Risk walls

- Private malware-evidence directory remains read-only and outside the repository.
- No sample execution, dynamic analysis, network callback, or artifact recovery.
- Defanged infrastructure must remain non-clickable.
- Sanitization occurs both before bundled fixtures are committed and again at ingestion.
- CI receives read-only repository permissions and no secrets or deployment authority.
- No history rewrite or force push under this change.

## Private evidence policy

The private corpus is used only to establish provenance for hashes and defensive conclusions. Raw executables, DLLs, `.pyc`, payloads, archives, injected code, and logs are never copied into the repository. Published hashes do not identify a victim and are useful for independent verification.

## Residual risks

- Removed values remain in existing Git history until a separately approved history rewrite is completed.
- Text-pattern matching is an educational approximation, not a full YARA engine or malware verdict.
- Project adoption and ongoing maintenance can only be demonstrated by genuine future activity.
