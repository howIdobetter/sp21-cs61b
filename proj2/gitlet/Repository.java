package gitlet;

import java.io.File;
import static gitlet.Utils.*;

import java.util.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Yuhao Wang
 */
public class Repository {
    /**
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

    /** Changes the HEAD pointer to the given hash. */
    public static void changeHead(String hash) {
        writeContents(HEAD, hash);
    }

    /** Gets the current HEAD commit hash. */
    public static String readHead() {
        return readContentsAsString(HEAD);
    }

    /** Initializes the staging area. */
    public static void initStaged() {
        HashMap<String, String> addMap = new HashMap<>();
        HashSet<String> removeSet = new HashSet<>();
        Stage stage = new Stage(addMap, removeSet);
        Utils.writeObject(Stage.stage, stage);
    }

    /** Initializes branches with the given current branch and branches map. */
    public static void initBranches(String currentBranch, HashMap<String, String> branches) {
        Branch branch = new Branch(currentBranch, branches);
        branch.writeBranch();
    }

    /** Finds the split point (LCA) between two commits. */
    public static String findSplitPoint(String currentId, String givenId) {
        Queue<String> queue = new LinkedList<>();
        Map<String, Integer> depthMap = new HashMap<>();
        Set<String> currentAncestors = new HashSet<>();

        int depth = 0;
        String commitId = currentId;
        while(commitId != null) {
            currentAncestors.add(commitId);
            depthMap.put(commitId, depth++);
            Commit c = Commit.readCommit(commitId);
            commitId = c.parent != null && !c.parent.isEmpty() ? c.parent.get(0) : null;
        }

        queue.add(givenId);
        int minDepth = Integer.MAX_VALUE;
        String lca = null;

        while (!queue.isEmpty()) {
            String id = queue.poll();
            if (currentAncestors.contains(id)) {
                int d = depthMap.get(id);
                if (d < minDepth) {
                    minDepth = d;
                    lca = id;
                }
            }
            Commit c = Commit.readCommit(id);
            if (c.parent != null) {
                queue.addAll(c.parent);
            }
        }
        return lca;
    }
}
