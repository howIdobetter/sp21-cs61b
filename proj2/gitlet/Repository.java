package gitlet;

import java.io.File;
import static gitlet.Utils.*;

import java.util.*;

/** Represents a gitlet repository.
 *  Provides static methods for repository operations and maintains
 *  references to key repository directories and files.
 *
 *  @author Yuhao Wang
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The HEAD file storing current commit hash. */
    public static final File HEAD = join(GITLET_DIR, "head");

    /** 
     * Initializes persistence by creating necessary directories.
     * Creates .gitlet, commits, stage, and blobs directories.
     */
    public static void setupPersistence() {
        GITLET_DIR.mkdir();
        Commit.COMMIT_DIR.mkdir();
        Stage.STAGED_DIR.mkdir();
        Blob.BLOB_DIR.mkdir();
    }

    /** 
     * Changes the HEAD pointer to the given hash.
     * @param hash the SHA-1 hash to set as HEAD
     */
    public static void changeHead(String hash) {
        writeContents(HEAD, hash);
    }

    /** 
     * Gets the current HEAD commit hash.
     * @return SHA-1 hash of the current HEAD commit
     */
    public static String readHead() {
        return readContentsAsString(HEAD);
    }

    /** 
     * Initializes the staging area with empty add and remove sets.
     */
    public static void initStaged() {
        HashMap<String, String> addMap = new HashMap<>();
        HashSet<String> removeSet = new HashSet<>();
        Stage stage = new Stage(addMap, removeSet);
        Utils.writeObject(Stage.stage, stage);
    }

    /** 
     * Initializes branches with the given current branch and branches map.
     * @param currentBranch the name of the current branch
     * @param branches map of branch names to commit hashes
     */
    public static void initBranches(String currentBranch, HashMap<String, String> branches) {
        Branch branch = new Branch(currentBranch, branches);
        branch.writeBranch();
    }

    /** 
     * Finds the split point (LCA - Lowest Common Ancestor) between two commits.
     * Uses BFS to traverse the commit history and find the nearest common ancestor.
     * @param currentId SHA-1 hash of current branch's commit
     * @param givenId SHA-1 hash of branch being merged
     * @return SHA-1 hash of the split point commit
     */
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
