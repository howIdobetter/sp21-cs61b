package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Utils.*;

/**
 * Represents branch information in the gitlet version control system.
 * Tracks the current branch and all branch names with their commit hashes.
 *
 * @author Yuhao Wang
 */
public class Branch implements Serializable {
    /** File storing branch information. */
    public static final File BRANCHES = join(Repository.GITLET_DIR, "branches");

    /** Name of the current active branch. */
    public String current_branch;
    /** Maps branch names to their HEAD commit SHA-1 hashes. */
    public HashMap<String, String> branches;

    /**
     * Creates a new Branch object.
     * @param current_branch the name of the current branch
     * @param branches map of branch names to commit hashes
     */
    public Branch(String current_branch, HashMap<String, String> branches) {
        this.current_branch = current_branch;
        this.branches = branches;
    }

    /** 
     * Reads Branch from disk.
     * @return the Branch object
     */
    public static Branch readBranch() {
        return readObject(BRANCHES, Branch.class);
    }

    /** 
     * Writes Branch to disk.
     */
    public void writeBranch() {
        writeObject(BRANCHES, this);
    }
}
