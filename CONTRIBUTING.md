# Contributing

Thanks for improving this defensive research project.

## Safe contribution rules

- Never commit malware, recoverable source, bytecode, binaries, archives, encrypted payloads, memory dumps, credentials, or raw logs.
- Replace personal identifiers with explicit placeholders such as `[REDACTED_STEAMID64]`.
- Write historical infrastructure as `hxxps://example[.]invalid`; never restore a clickable malicious URL.
- Use synthetic values in tests. Do not copy a victim's data, even into a test fixture.
- Separate direct observations, inferences, and unverified hypotheses in research documentation.
- A detection-rule miss must never be described as proof of safety.

## Workflow

1. Open or reference an Issue that explains the defensive value and data source.
2. Create a focused branch and keep unrelated changes out of the PR.
3. Add or update tests and describe sanitization decisions.
4. Run:

   ```bash
   python scripts/verify_public_safety.py
   cd backend && mvn -B -ntp test
   cd ../frontend && npm ci && npm run build
   ```

5. Complete the Pull Request template, including residual risks.

## Detection rules

Rule contributions should include a safe positive fixture, a negative fixture, expected data source, likely false positives, and a short explanation of why multiple signals are combined. This repository's matcher is educational and does not implement the full YARA language.

By contributing, you agree that your contribution is licensed under the repository's MIT License and complies with these safety rules.
