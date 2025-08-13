package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import static gitlet.Utils.*;

public class Branch implements Serializable {
    public static final File BRANCHES = join(Repository.GITLET_DIR, "branches");

    /** We need current_branch, branches */
    public String current_branch;
    public HashMap<String, String> branches;

    public Branch(String current_branch, HashMap<String, String> branches) {
        this.current_branch = current_branch;
        this.branches = branches;
    }

    /** read Branch */
    public static Branch readBranch() {
        return readObject(BRANCHES, Branch.class);
    }

    /** write Branch */
    public void writeBranch() {
        writeObject(BRANCHES, this);
    }
}
