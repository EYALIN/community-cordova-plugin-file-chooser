package android.database;

import java.io.Closeable;

// PLU-92 fake: a fixed-row cursor for OpenableColumns.DISPLAY_NAME / SIZE queries.
public class Cursor implements Closeable {
    private final String[] columns;
    private final Object[] values;

    public Cursor(String[] columns, Object[] values) {
        this.columns = columns;
        this.values = values;
    }

    public boolean moveToFirst() {
        return columns != null && columns.length > 0;
    }

    public int getColumnIndex(String name) {
        if (columns == null) return -1;
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(name)) return i;
        }
        return -1;
    }

    public boolean isNull(int index) {
        return index < 0 || values[index] == null;
    }

    public String getString(int index) {
        return (String) values[index];
    }

    public long getLong(int index) {
        return values[index] == null ? 0 : ((Number) values[index]).longValue();
    }

    @Override
    public void close() {}
}
