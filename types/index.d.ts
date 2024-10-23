export interface FileChooserOptions {
    mimeType?: string;
    includeBase64?: boolean;
    multiple?: boolean;
}

export interface FileChooserResponse {
    fileName: string;
    path: string;
    extension: string;
    fileSize: number;
    base64?: string;
}

export default class FileChooserManager {
    chooseFile(options: FileChooserOptions): Promise<FileChooserResponse[]>;
}
