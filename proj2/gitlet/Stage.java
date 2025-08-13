package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.Utils.*;

public class Stage implements Serializable {
    /** persitence path */
    static final File STAGED_DIR = join(Repository.GITLET_DIR, "stage");
    static final File stage = join(STAGED_DIR, "stageinformation");

    public HashMap<String, String> add;
    public HashSet<String> remove;

    public Stage(HashMap<String, String> add, HashSet<String> remove) {
        this.add = add;
        this.remove = remove;
    }

    /** clear the staged area */
    public static void clearStaged() {
        List<String> stagedBlobs = plainFilenamesIn(STAGED_DIR);

        if (stagedBlobs != null && stagedBlobs.isEmpty()) {
            return;
        }

        for (String blobName : stagedBlobs) {
            File blobFile = join(STAGED_DIR, blobName);
            blobFile.delete();
        }
    }

    /** write the stage */
    public static void writeStaged(Stage s) {
        writeObject(stage, s);
    }

    /** read the stage */
    public static Stage readStaged() {
        return readObject(stage, Stage.class);
    }
}
