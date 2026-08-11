## Purpose

Describe the defensive value and link the Issue.

## Data safety

- [ ] No sample, binary, bytecode, archive, payload, raw log, credential, or personal identifier is included.
- [ ] Infrastructure is defanged and test values are synthetic.
- [ ] Direct observations, inferences, and hypotheses are distinguished.

## Verification

- [ ] `python scripts/verify_public_safety.py`
- [ ] `cd backend && mvn -B -ntp test`
- [ ] `cd frontend && npm ci && npm run build`

## Risk and rollback

State affected behavior, residual risks, and how to revert the change.
