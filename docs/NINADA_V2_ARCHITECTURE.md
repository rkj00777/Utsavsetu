# NinadaV2 — Android Neural TTS Rebuild

## Purpose
Replace the fragile Ninada ONNX integration with a model-contract-driven Android implementation, while leaving the stable device/Google TTS path intact. This branch is isolated from `main`.

## Model source and evidence
- Hugging Face: `gnumanth/ninada`
- Repository layout documented upstream: `onnx_mobile/{lang}.onnx`, `onnx_mobile/{lang}_female.onnx`, `vocabs.json`, `models_manifest.json`.
- Mobile specialists: distilled iSTFTNet family; 24 kHz.
- Supported upstream languages: sa, en, hi, kn, mr, bn, ta, te, ml, gu, pa, or.
- License reported upstream: CC-BY-4.0. Preserve attribution and verify the exact file metadata before shipping.
- UtsavSetu first-wave coverage (based on existing engine): sa, hi, mr, gu, ta, te, kn, bn, en. Do not enable additional languages until app-language mapping and UI are audited.

## Non-negotiable safety boundary
Native ONNX inference MUST NOT run in the main app process during initial qualification. Implement a dedicated Android service/process for the inference worker. A native crash in that worker must not terminate the main app. Main-process playback controller must recover to existing device TTS. Avoid relying on Kotlin exception handling for native SIGILL.

## Proposed package structure
- `NinadaV2Controller`: lifecycle, language/gender selection, request/cancel, fallback.
- `NinadaV2WorkerService`: isolated inference process entry point.
- `NinadaModelCatalog`: explicit language/gender/model metadata and source paths.
- `NinadaModelInstaller`: download, temporary file, checksum/size validation, atomic promotion.
- `NinadaTokenizerAdapter`: explicit native-script tokenizer mapping; no guessed character IDs.
- `NinadaOnnxAdapter`: model-specific input/output contract, tensor construction and PCM conversion.
- `NinadaAudioValidator`: output shape, sample rate, finite values, clipping, duration and silence checks.
- `NinadaHealthStore`: qualification and worker-crash state; persist quarantine/disable state.
- `NinadaV2Diagnostics`: redacted diagnostic logs.

## Installation contract
1. Resolve model metadata from a versioned catalog.
2. Download into a temporary file; support retry, cancellation and progress.
3. Validate size and SHA-256 against catalog metadata where available.
4. Validate model compatibility in the isolated worker before marking installed.
5. Atomically move the model into the versioned cache.
6. Never mark a pack Ready solely because a file exists or exceeds a size threshold.

## Inference contract
1. Main process sends text, language, voice, speed and request ID over IPC.
2. Worker normalizes text with a language-specific policy and tokenizes with a verified vocab.
3. Worker verifies input names/types/shapes against a per-model contract.
4. Worker runs inference with conservative options; no universal assumptions about model signatures.
5. Worker converts output to validated mono PCM and returns chunks or a bounded result.
6. Main process plays audio; accompaniment remains independently controlled.
7. Timeout, worker death, invalid output or unavailable model triggers device TTS. Worker death quarantines that model/device combination pending explicit retest.

## Qualification tests
- Catalog/model manifest mapping tests for all nine first-wave languages and gender variants.
- Tokenizer tests: representative scripts, punctuation, conjuncts, Sanskrit diacritics/visarga, unknown characters, empty input.
- Download tests: interruption, resume/retry, bad hash, truncated file, disk-full, atomic install.
- Contract tests: expected model inputs/outputs, types, dimensions, output sample rate.
- PCM tests: finite values, non-empty, non-silent, reasonable amplitude/duration, no clipping.
- Worker tests: service start/stop, IPC cancellation, timeout, simulated worker death, recovery.
- Device tests: at least one low-end ARM device and target device; both voice genders where available; each language; accompaniment; repeat playback.
- Regression: Google/device fallback and app navigation continue working if worker fails.

## Rollout
Phase 0: branch, audit and contract documentation (this commit).
Phase 1: obtain exact upstream catalog and file metadata; inspect ONNX signatures and tokenizer; produce generated, versioned model catalog.
Phase 2: implement isolated worker and IPC with a fake inference backend; verify crash isolation.
Phase 3: implement download/installer and tokenizer/model adapters for Sanskrit only.
Phase 4: device validation of Sanskrit male/female; enable behind internal test switch.
Phase 5: extend adapters to the remaining currently supported languages.
Phase 6: integration, test matrix and gradual enablement; fallback remains available.

## Exit criteria
No production enablement until isolated-worker crash tests, model integrity, Sanskrit audio quality and device validation pass. This document does not claim inference/build/device validation.
