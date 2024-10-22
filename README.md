I dedicate a considerable amount of my free time to developing and maintaining many Cordova plugins for the community ([See the list with all my maintained plugins][community_plugins]).
To help ensure this plugin is kept updated, new features are added, and bugfixes are implemented quickly, please donate a couple of dollars (or a little more if you can stretch) as this will help me to afford to dedicate time to its maintenance.
Please consider donating if you're using this plugin in an app that makes you money, or if you're asking for new features or priority bug fixes. Thank you!

[![](https://img.shields.io/static/v1?label=Sponsor%20Me&style=for-the-badge&message=%E2%9D%A4&logo=GitHub&color=%23fe8e86)](https://github.com/sponsors/eyalin)

[community_plugins]: https://github.com/EYALIN?tab=repositories&q=community&type=&language=&sort=

# FileChooserPlugin

`FileChooserPlugin` is a Cordova plugin that allows users to select files on Android and iOS. It provides an easy interface to pick files and get the file's path or content as a base64 encoded string.

## Installation

To install the plugin, use the following command:

```sh
cordova plugin add community-cordova-plugin-file-chooser
```

## Methods

### `chooseFile(options?: FileChooserOptions): Promise<string>`

Presents the user with a file chooser to pick a file.

#### Options

- `responseType` (optional): Specifies the format in which to return the selected file. Possible values are:
  - `'path'` (default): Returns the file's URI path.
  - `'base64'`: Returns the file's content as a base64 encoded string.
- `mimeType` (optional): Specifies the MIME type to filter files. Defaults to all files (`'*/*'`).

#### Example

```javascript
import { chooseFile } from 'community-cordova-plugin-file-chooser';

chooseFile({ responseType: 'base64', mimeType: 'image/*' })
  .then(fileData => {
    console.log('File data:', fileData);
  })
  .catch(error => {
    console.error('Error choosing file:', error);
  });
```

## Platform Support

- **Android**
- **iOS**

## Usage

The plugin is straightforward to use. Simply call the `chooseFile()` function with the desired options, and handle the result in the promise's `then` or `catch` block.

## Contributing

Contributions are welcome! If you find a bug or have an idea for a new feature, please open an issue or submit a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

