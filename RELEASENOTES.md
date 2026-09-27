
# Release Notes

### 1.0.6 (unreleased)

* Android: picking a file no longer crashes with a `NullPointerException` in `onActivityResult` when Android killed the app while the picker was open ("Don't keep activities", low memory). The plugin saves its options (`onSaveInstanceState`) and answers the restored callback (`onRestoreStateForActivityResult`); a result with no waiting callback is ignored.
* Android: a successful result without data now fails with "No file selected" instead of never answering.
* `tests/android/run.sh`: JVM test of `FileChooserPlugin.java` including the process-death path.
