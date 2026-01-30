package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

/**
 * Represents a blob (Binary Large OBject) in the gitlet version control system.
 * A blob stores the contents of a file at a specific point in time.
 *
 * @author Yuhao Wang
 */
public class Blob implements Serializable {
    /** The contents of the file. */
    public String contents;
    /** SHA-1 hash of this blob. */
    public String sha1;

    /** Directory for storing blob objects. */
    static final File BLOB_DIR = Utils.join(Repository.GITLET_DIR, "blobs");
    /** Directory for storing staged blobs. */
    static final File STAGED_DIR = join(Repository.GITLET_DIR, "stage");

    /**
     * Creates a new Blob with the given contents.
     * @param contents the file contents to store
     */
    public Blob(String contents) {
        this.contents = contents;
        this.sha1 = sha1((Object) serialize(this));
    }

    /**
     * Writes blob to ~/.gitlet/blobs directory.
     */
    public void writeBlob() {
        File f = Utils.join(BLOB_DIR, this.sha1);
        writeObject(f, this);
    }

    /**
     * Reads blob from object store.
     * @param hash SHA-1 hash of the blob to read
     * @return the Blob object
     */
    public static Blob readBlob(String hash) {
        File f = Utils.join(BLOB_DIR, hash);
        return readObject(f, Blob.class);
    }

    /**
     * Deletes blob from object store.
     */
    public void deleteBlob() {
        File f = Utils.join(BLOB_DIR, sha1);
        f.delete();
    }

    /**
     * Writes blob to ~/.gitlet/stage directory.
     */
    public void writeBlobToStage() {
        File f = Utils.join(STAGED_DIR, this.sha1);
        writeObject(f, this);
    }

    /**
     * Deletes blob from stage.
     */
    public void deleteBlobFromStage() {
        File f = Utils.join(STAGED_DIR, sha1);
        f.delete();
    }
    
    /**
     * Reads blob from stage. If not found in stage, tries object store.
     * @param hash SHA-1 hash of the blob to read
     * @return the Blob object
     */
    public static Blob readBlobFromStage(String hash) {
        File f = Utils.join(STAGED_DIR, hash);
        if (!f.exists()) {
            // If staged file doesn't exist, try reading from object store
            return readBlob(hash);
        }
        return readObject(f, Blob.class);
    }


}
