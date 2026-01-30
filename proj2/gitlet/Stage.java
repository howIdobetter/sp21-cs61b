package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.Utils.*;

/**
 * Represents the staging area in the gitlet version control system.
 * Tracks files staged for addition and removal.
 *
 * @author Yuhao Wang
 */
public class Stage implements Serializable {
    /** Directory for storing staged files. */
    static final File STAGED_DIR = join(Repository.GITLET_DIR, "stage");
    /** File storing stage information. */
    static final File stage = join(STAGED_DIR, "stageinformation");

    /** Maps filenames to blob SHA-1 hashes for files staged for addition. */
    public HashMap<String, String> add;
    /** Set of filenames staged for removal. */
    public HashSet<String> remove;

    /**
     * Creates a new Stage object.
     * @param add map of filenames to blob hashes for addition
     * @param remove set of filenames for removal
     */
    public Stage(HashMap<String, String> add, HashSet<String> remove) {
        this.add = add;
        this.remove = remove;
    }

    /**
     * Clears the staged area by deleting all staged blobs.
     */
    public static void clearStaged() {
        List<String> stagedBlobs = plainFilenamesIn(STAGED_DIR);

        if (stagedBlobs == null || stagedBlobs.isEmpty()) {
            return;
        }

        for (String blobName : stagedBlobs) {
            File blobFile = join(STAGED_DIR, blobName);
            blobFile.delete();
        }
    }

    /**
     * Writes the stage to disk.
     * @param s the Stage object to write
     */
    public static void writeStaged(Stage s) {
        writeObject(stage, s);
    }

    /**
     * Reads the stage from disk. Creates new stage if none exists.
     * @return the Stage object
     */
    public static Stage readStaged() {
        if (!stage.exists()) {
            HashMap<String, String> map = new HashMap<>();
            HashSet<String> set = new HashSet<>();
            Stage newStage = new Stage(map, set);
            writeObject(stage, newStage);
            return newStage;
        }
        return readObject(stage, Stage.class);
    }

    /**
     * Adds a file to the staging area for addition.
     * Removes the file from the removal set if present.
     * @param filename the name of the file
     * @param blobId the SHA-1 hash of the blob
     */
    public void add(String filename, String blobId) {
        add.put(filename, blobId);
        remove.remove(filename);
    }
}
