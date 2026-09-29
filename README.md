I dedicate a considerable amount of my free time to developing and maintaining many Cordova plugins for the community ([See the list with all my maintained plugins][community_plugins]).
To help ensure this plugin is kept updated, new features are added, and bugfixes are implemented quickly, please donate a couple of dollars (or a little more if you can stretch) as this will help me to afford to dedicate time to its maintenance.
Please consider donating if you're using this plugin in an app that makes you money, or if you're asking for new features or priority bug fixes. Thank you!

[![](https://img.shields.io/static/v1?label=Sponsor%20Me&style=for-the-badge&message=%E2%9D%A4&logo=GitHub&color=%23fe8e86)](https://github.com/sponsors/eyalin)

[community_plugins]: https://github.com/EYALIN?tab=repositories&q=community&type=&language=&sort=

# FileChooserPlugin

`FileChooserPlugin` is a Cordova plugin that lets users pick one or more files on Android and iOS
(via `ACTION_GET_CONTENT` / `UIDocumentPickerViewController`), returning each file's name,
extension, size and — optionally — its content as a base64 string.

## Installation

To install the plugin, use the following command:

```sh
cordova plugin add community-cordova-plugin-file-chooser
```

## Methods

### `chooseFile(options?: FileChooserOptions): Promise<FileChooserResponse[]>`

Presents the user with a file chooser to pick one or more files, and resolves with an array —
always one entry per picked file, even when `multiple` is not set.

#### Options (`FileChooserOptions`)

- `mimeType` (optional, `string`): filters which files can be picked. Defaults to all files.
  - **Android**: an Android MIME type / glob passed straight to the picker's `Intent.setType()`
    (e.g. `'image/*'`, `'application/pdf'`, or `'*/*'` for everything).
  - **iOS**: a **UTI** (Uniform Type Identifier), not a MIME type, passed to
    `UIDocumentPickerViewController`'s document types (e.g. `'public.image'`, `'com.adobe.pdf'`,
    or `'public.data'` for everything — the default when `mimeType` is omitted). Passing an
    Android-style MIME type such as `'image/*'` on iOS will not match anything.
- `includeBase64` (optional, `boolean`, default `false`): also read the file's full content and
  return it as a base64 string on `base64`. Leave this off when you only need metadata — it avoids
  reading the file into memory.
- `multiple` (optional, `boolean`, default `false`): allow picking more than one file in a single
  chooser session.

#### Response (`FileChooserResponse`, one per picked file)

- `fileName` (`string`): the display name (from the system's `OpenableColumns.DISPLAY_NAME` on
  Android, or the picked file's last path component on iOS), falling back to the trailing segment
  of the file's URI when the system can't supply a name.
- `path` (`string`): the file's URI (Android, `content://…`) or filesystem path (iOS).
- `extension` (`string`): the file extension without the leading dot (e.g. `'db'`), derived from
  `fileName`.
- `fileSize` (`number`): size in bytes (from `OpenableColumns.SIZE` on Android, falling back to the
  content resolver's `AssetFileDescriptor` length; from `NSData.length` on iOS).
- `base64` (`string`, only when `includeBase64: true`): the file's full content, base64-encoded.

#### Example

```javascript
FileChooserPlugin.chooseFile({ includeBase64: true, mimeType: 'application/pdf' })
  .then(files => {
    console.log('Picked file:', files[0].fileName, files[0].fileSize);
  })
  .catch(error => {
    console.error('Error choosing file:', error);
  });
```

`FileChooserPlugin` is a Cordova global (`window.FileChooserPlugin`), not an ES module export —
there is no `import { chooseFile } from 'community-cordova-plugin-file-chooser'`. TypeScript
consumers can still get typed access via the shipped `types/index.d.ts`:

```typescript
import FileChooserManager, { FileChooserOptions, FileChooserResponse } from 'community-cordova-plugin-file-chooser';

declare const FileChooserPlugin: FileChooserManager;
```

## Platform Support

- **Android**
- **iOS**

## Errors

The returned promise rejects (with a plain string message) when:
- the user cancels the picker,
- the picker returns without a result the plugin can read (e.g. the app process was killed while
  the picker was open and there's no pending callback to answer),
- or, with `includeBase64: true`, the file couldn't be read (including a 0-byte read, which is
  treated as a failure rather than an empty file).

## Contributing

Contributions are welcome! If you find a bug or have an idea for a new feature, please open an issue or submit a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
