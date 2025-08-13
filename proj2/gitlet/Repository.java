package gitlet;

import javax.print.attribute.HashPrintServiceAttributeSet;
import java.io.File;
import static gitlet.Utils.*;
import java.util.HashMap;
import java.util.HashSet;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Yuhao Wang
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The Head directory. */
    public static final File HEAD = join(GITLET_DIR, "head");

    /* TODO: fill in the rest of this class. */
    /**
     * Persistence init method.
     */
    public static void setupPersistence() {
        GITLET_DIR.mkdir();
        Commit.COMMIT_DIR.mkdir();
        Stage.STAGED_DIR.mkdir();
        Blob.BLOB_DIR.mkdir();
    }

    /** change the head */
    public static void changeHead(String hash) {
        writeContents(HEAD, hash);
    }

    /** get the head */
    public static String readHead() {
        return readContentsAsString(HEAD);
    }

    /** init staged */
    public static void initStaged() {
        File f = Stage.stage;
        HashMap<String, String> map = new HashMap<>();
        HashSet<String> set = new HashSet<>();
        Stage stage = new Stage(map, set);
        Utils.writeContents(f, stage);
    }

    /** init branches */
    public static void initBranches(String current_branch, HashMap<String, String> branches) {
        Branch branch = new Branch(current_branch, branches);
        branch.writeBranch();
    }

    /** write to work directory */
//    public static void writeWorkDirectory(String filename, ) {
//
//    }
}
