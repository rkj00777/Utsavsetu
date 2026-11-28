# Ninada V2 Worker Contract (Implementation Gate)

This contract defines the first implementation target; it does not assert that a worker has been implemented or tested.

## Process boundary
- Android worker service: `NinadaV2WorkerService`, declared with `android:process=":ninada_worker"`.
- Main process owns playback UI/state, TTS fallback, and accompaniment tracks.
- Worker process owns ORT environment, model session, tokenizer/model adapters and model files.
- No ONNX native session is created in the main process.

## Request/response
Request fields:
- requestId (unique)
- languageCode (allow-list)
- gender (MALE/FEMALE)
- text (bounded length)
- speed (validated range)
- modelVersion/hash (must match installed model record)

Response fields:
- requestId
- status (success / invalid_request / model_unavailable / synthesis_error)
- sampleRate (must be 24000 for accepted Ninada output)
- PCM data (bounded size)
- diagnostic code (non-sensitive)

## Operational rules
- Main process timeout is configurable and initially conservative.
- Binder death or timeout immediately returns a failure to the main playback controller; it must not block the UI.
- Worker errors must be bounded and sanitized. Do not transmit crash dumps, private text or audio to external services.
- Worker restart is allowed only after backoff. Repeated failures quarantine the current model/device combination and use system TTS until the user or app qualification flow re-enables it.
- Model installation is staged, cancellable, SHA-256 verified and atomic. Existing valid models are not overwritten until a new model passes verification.
- The worker may receive a file path only to an app-private, verified model file. Never accept arbitrary paths from IPC.
- Validate PCM: 24 kHz expected, nonempty, finite-equivalent values, duration limit, RMS and peak thresholds; reject silence and malformed output.

## Required test cases
1. Binder service unavailable and bind timeout.
2. Worker death before, during and after inference.
3. Restart/backoff and per-device/model quarantine.
4. Invalid language, gender, speed, oversized text and malformed request.
5. Download cancellation, retry, hash mismatch, truncated file and atomic promotion.
6. Sanskrit male/female sample and long mantra; ensure main process remains alive.
7. Output silence, clipping, excessive duration, invalid shape and tensor errors.
8. Device TTS fallback; ensure tanpura/tabla behavior and controls are unaffected.
9. Worker memory pressure/session close and repeated request leak checks.
10. Regression: playback start/stop, rounds, speed, gender, Japa controls and audio focus.

## Build gate
A CI build is a compile gate only. Passing CI does not qualify native inference. Release gate requires target-device tests and evidence that worker native crash leaves main app alive.
