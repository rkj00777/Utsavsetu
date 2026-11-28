# NinadaV2 Audit Checklist (Phase 0)

- [ ] Confirm exact upstream commit/version of Hugging Face model repository and license files.
- [ ] Capture repository file listing for all mobile ONNX specialists, vocab, manifest and sample assets.
- [ ] Inspect `models_manifest.json` entries, file hashes and URL paths.
- [ ] Inspect `vocabs.json` structure and native script token mappings.
- [ ] Inspect ONNX input/output names, types, ranks and dynamic axes for each first-wave language/gender pair.
- [ ] Confirm app-language enum and current app voice language support.
- [ ] Identify ONNX Runtime Android version, ABIs packaged, and device-specific crash context.
- [ ] Confirm old engine's cached model files and invalidate unsafe cache naming/metadata if necessary (do not delete user data).
- [ ] Prototype worker isolation and prove simulated native worker death leaves main app alive.
- [ ] Sanskrit-only test matrix passed on target phone before regional-language rollout.

## First-wave languages
Sanskrit (sa), Hindi (hi), Marathi (mr), Gujarati (gu), Tamil (ta), Telugu (te), Kannada (kn), Bengali (bn), Indian English (en).

## Evidence status
Branch and documentation scaffold only. No code implementation, APK build, or device validation is claimed yet.
