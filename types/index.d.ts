export type ResponseType = 'path' | 'base64';

export interface FileChooserOptions {
    responseType?: ResponseType;
    mimeType?: string;
}

export default class FileChooserManager {
    chooseFile(options: FileChooserOptions): Promise<string>;
}
