package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;

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

    /** write blob to ~/.gitlet/blobs */
    public void writeBlob() {
        String hash = this.sha1;
        File f = Utils.join(BLOB_DIR, hash);
        writeObject(f, this);
    }

    /** read blob from ~ */
    public static Blob readBlob(String hash) {
        File f = Utils.join(BLOB_DIR, hash);
        return readObject(f, Blob.class);
    }

    /** delete blob */
    public void deleteBlob() {
        File f = Utils.join(BLOB_DIR, sha1);
        f.delete();
    }

    /** write blob to ~/.gitlet/stage */
    public void writeBlobToStage() {
        String hash = this.sha1;
        File f = Utils.join(STAGED_DIR, hash);
        writeObject(f, this);
    }

    /** read blob from staged */
    public static Blob readBlobFromStage(String hash) {
        File f =  Utils.join(STAGED_DIR, hash);
        return readObject(f, Blob.class);
    }

    /** delete blob from staged */
    public void deleteBlobFromStage() {
        File f = Utils.join(STAGED_DIR, sha1);
        f.delete();
    }
}
