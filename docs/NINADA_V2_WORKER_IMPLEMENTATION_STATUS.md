# Ninada V2 Worker Implementation — Experimental Status

**Branch:** ninada-v2-rebuild  
**Date:** 2026-11-28

## Implemented in source
- Added `NinadaV2WorkerService`, registered in manifest on process `:ninada_worker`.
- Service owns its process-local `NinadaNeuralVoiceEngine` instance and performs synthesis requests there.
- Added Messenger IPC contract for bounded text, language allow-list, gender and speed; returns 24kHz PCM results.
- Added main-process `NinadaV2WorkerClient` with a 45-second request timeout and failure-to-null behavior.
- Recitation flow now calls worker IPC after the existing model acquisition path. Failure flows through existing device TTS fallback logic.
- The experimental branch remains isolated from main.

## Important caveats
- These changes are **source implementation only**; no APK build or compile verification has been performed in this environment.
- The worker process is separate but not Android's restricted isolatedProcess mode: it must access the app-private files directory for models. It is declared with `android:process=":ninada_worker"`.
- Worker/native process crash handling, service binding, Messenger payload limits, large PCM transfer, cancellation behavior and model integrity checks require compile and device tests.
- The 45-second timeout does not establish safe inference runtime for longer recitations.
- The existing model downloader still needs robust manifest hash validation before it can be treated as verified installer.
- Worker crash/timeout recovery must be tested on physical hardware. Do not merge or release until this is done.

## Next test gate
1. Compile debug build.
2. Static review IPC/client/service Kotlin and Android manifest.
3. Install and confirm system TTS fallback unaffected.
4. Download one Sanskrit model pack; validate size and checksum independently.
5. Run short Sanskrit synthesis through worker.
6. Test worker crash while main app and accompaniment remain alive.
7. Test long recitation, female voice, stop/cancel, repeated requests and worker restart.
8. Only after Sanskrit passes, expand qualification to remaining supported languages.
