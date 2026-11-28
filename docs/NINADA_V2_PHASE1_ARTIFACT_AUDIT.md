# Ninada V2 Phase 1 Artifact Audit

**Audit date:** 2026-11-28  
**Branch:** ninada-v2-rebuild  
**Upstream:** https://huggingface.co/gnumanth/ninada

## Verified upstream metadata
- Repository metadata reports VITS, ONNX, 81.9M parameters, CC-BY-4.0, 12 language codes: sa, en, hi, kn, mr, bn, ta, te, ml, gu, pa, or.
- Manifest version read from upstream: **5.1.2**; cache name: **ninada-onnx-v5.1.2**; architecture descriptor: **native-studio-vits-24k-dual-tier-hifigan-istft**.
- The manifest has mobile model entries and separate female/mobile-female URLs, byte-size expectations, and cache keys.
- Model inventory confirms upstream contains the `onnx_mobile` directory; a model file inventory and exact model metadata/signatures must be collected independently before runtime implementation.

## Application baseline inspected
Files inspected from the main branch:
- `NinadaNeuralVoiceEngine.kt`
- `MantraAudioEngine.kt`
- `app/build.gradle.kts`
- `AndroidManifest.xml`

### Confirmed hazards
1. ONNX inference and session creation currently occur in the main application process. JNI/native SIGILL is fatal to that process and cannot reliably be caught by Kotlin exceptions.
2. Existing SessionOptions tuning was insufficient in the user's device test.
3. The download path currently accepts files based primarily on size, not a verified upstream digest before atomic promotion.
4. Tokenization, ONNX tensor binding and inference are tightly coupled inside one engine class. A model signature mismatch or native failure is therefore difficult to isolate.
5. Existing playback fallback to Android/Google TTS works in the user's test and should be preserved as the safety fallback.
6. Do not re-enable the main-process ONNX path.

## Phase 1 status
- [x] Read upstream model repository metadata and README-derived details.
- [x] Read upstream manifest (5.1.2) and vocab schema.
- [x] Confirm mobile tier is represented in repository and manifest.
- [x] Inspect Android audio engine, ONNX inference, download, tokenizer and input binding source.
- [ ] Record exact ONNX tensor signatures from the actual mobile Sanskrit models (not inferred).
- [ ] Extract and compare upstream hashes against actual model downloads.
- [ ] Device architecture, ORT native library and ABI diagnostic audit.
- [ ] Prove worker isolation by simulated process death on target hardware.

## Safe implementation sequence
1. Freeze this branch as the experimental rebuild branch; main remains unchanged.
2. Build a worker service in a distinct Android process with a binder contract and request timeout. Main app sends synthesis requests, receives PCM payload/metadata, and detects worker death.
3. The worker must only load ONNX sessions after a verified staged model download; validate digest and manifest size before atomically promoting files.
4. Keep one model/session per worker at first; sequential execution and conservative session options are initial experiment settings, not proof of native safety.
5. Keep all ONNX native calls inside worker. On timeout, malformed output, binder death or native crash: worker is marked unhealthy for this device/model and device TTS fallback is invoked.
6. First qualification: Sanskrit male/female; test short, long and punctuation-rich text; worker crash/restart; app survives; silence/clipping/RMS validation; repeatable output.
7. Only after Sanskrit qualification, add first-wave languages (hi, mr, gu, ta, te, kn, bn, en), then assess upstream ml/pa/or separately.
8. Do not claim APK/device validation until a build and actual device test results exist.

## Current outcome
This audit is evidence-based but **not a functioning worker implementation or device-qualified build**. The current stable device playback path is system TTS; neural TTS remains disabled pending isolated worker validation.
