package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

/**
 * The class is to be used to represent the usage of
 * our files.
 *
 * @author Yuhao Wang
 */

public class Blob implements Serializable {
    public String contents;
    public String sha1;

    static final File BLOB_DIR = Utils.join(Repository.GITLET_DIR, "blobs");
    static final File STAGED_DIR = join(Repository.GITLET_DIR, "stage");

    public Blob(String contents) {
        this.contents = contents;
        this.sha1 = sha1((Object) serialize(this));
    }

    /** Writes blob to ~/.gitlet/blobs */
    public void writeBlob() {
        File f = Utils.join(BLOB_DIR, this.sha1);
        writeObject(f, this);
    }

    /** Reads blob from object store. */
    public static Blob readBlob(String hash) {
        File f = Utils.join(BLOB_DIR, hash);
        return readObject(f, Blob.class);
    }

    /** Deletes blob from object store. */
    public void deleteBlob() {
        File f = Utils.join(BLOB_DIR, sha1);
        f.delete();
    }

    /** Writes blob to ~/.gitlet/stage */
    public void writeBlobToStage() {
        File f = Utils.join(STAGED_DIR, this.sha1);
        writeObject(f, this);
    }

    /** Deletes blob from stage. */
    public void deleteBlobFromStage() {
        File f = Utils.join(STAGED_DIR, sha1);
        f.delete();
    }
    
    /** Reads blob from stage. */
    public static Blob readBlobFromStage(String hash) {
        File f = Utils.join(STAGED_DIR, hash);
        if (!f.exists()) {
            // If staged file doesn't exist, try reading from object store
            return readBlob(hash);
        }
        return readObject(f, Blob.class);
    }


}
