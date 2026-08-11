# Test plan

## Automated checks

1. `python scripts/verify_public_safety.py`
   - reject forbidden artifact extensions, oversized binaries, raw C2 host, victim identifiers, private workstation paths, credentials, and private-key material.
2. `mvn -B -ntp test` from `backend/`
   - verify victim and infrastructure sanitization with synthetic inputs.
   - verify offline monitor output remains defanged.
3. `npm ci` and `npm run build` from `frontend/`
   - verify the Vue application compiles after API and wording changes.

## Manual checks

- Inspect the final tracked-file inventory.
- Search the current tree for each audited sensitive value and dangerous extension.
- Confirm CI workflow uses minimal permissions, concurrency cancellation, and immutable action SHAs.
- Confirm Git diff contains no raw sample content.
- Review README and form evidence for truthful, bounded claims.

## Live acceptance

- Draft PR exists on the authorized branch and leaves `main` unchanged.
- GitHub Actions results are observed once available; no repeated external submission on an unknown outcome.

## Pre-review local results (superseded by rework, 2026-08-12)

- `python scripts/verify_public_safety.py`: PASS, 96 files checked.
- `mvn -B -ntp test`: PASS, 3 tests, 0 failures, 0 errors, 0 skipped.
- `npm ci`: PASS, 86 locked packages installed.
- `npm run build`: PASS, Vite 5.4.21 built 2,225 modules. Existing chunk-size warnings are non-blocking and recorded as future performance work.
- `git diff --check`: PASS.
- Change budget: 13/13 production files, 375/900 production changed lines, 47/50 support files.

The independent review found that the first public-safety result missed serialized Windows paths and that historical C2 presentation required semantic corrections. These results remain evidence for the earlier tree but are not final acceptance evidence. Rework verification must additionally run `python -m unittest scripts/test_verify_public_safety.py` and both C2 service test paths.

## Post-rework local results (2026-08-12)

- `python scripts/verify_public_safety.py`: PASS, 97 files checked, including execution after Python test bytecode existed in ignored cache directories.
- `python -m unittest scripts/test_verify_public_safety.py`: PASS, 3 tests covering single/double separators, case variants, non-default drive letters, non-home absolute paths, safe placeholders, and synthetic irreversible-fingerprint matching.
- `mvn -B -ntp test`: PASS, 4 tests, 0 failures, 0 errors, 0 skipped. Empty and populated historical C2 paths are covered.
- `npm run build`: PASS, Vite 5.4.21 built 2,225 modules. Existing chunk-size warnings remain non-blocking.
- `git diff HEAD --check`: PASS.
- Change budget: 15/15 production files, 595/1,000 production changed lines, 51/52 support files.

## Round 2 rework verification (2026-08-12)

- Removed reversible victim-value fragments from the scanner; only SHA-256 fingerprints and generic contextual rules remain.
- Replaced all discovered drive-absolute event paths with environment placeholders.
- `python -m unittest scripts/test_verify_public_safety.py`: PASS, 3/3 with synthetic-only values.
- `python scripts/verify_public_safety.py`: PASS, 97 files.
- `git diff HEAD --check`: PASS.

Maven and Vite were not repeated for this narrow slice because the only Java change was a documentation comment and the SQL change replaced path literals. The previously passing 4/4 backend tests and production frontend build remain applicable; GitHub CI will execute the full gate on the published commit.
