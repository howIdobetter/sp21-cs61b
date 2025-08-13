package gitlet;

import java.io.File;
import java.util.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Yuhao Wang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args == null || args.length == 0) {
            throw Utils.error("Please enter a command.");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                init(args);
                break;
            case "add":
                add(args);
                break;
            case "commit":
                commit(args);
                break;
            case "rm":
                rm(args);
                break;
            case "log":
                log(args);
                break;
            case "global-log":
                global_log(args);
                break;
            case "find":
                find(args);
                break;
            case "status":
                status(args);
                break;
            case "checkout":
                checkout(args);
                break;
            case "branch":
                branch(args);
                break;
            case "rm-branch":
                rm_branch(args);
                break;
            case "reset":
                reset(args);
                break;
            case "merge":
                merge(args);
                break;
            default:
                throw Utils.error("No command with that name exists.");
        }
    }

    /** judge the length of args */
    private static void judgeLength(String[] args, int length) {
        if (args.length != length) {
            throw Utils.error("Incorrect operands.");
        }
    }

    /** judge the init */
    private static boolean judgeInit() {
        File dir = Repository.GITLET_DIR;
        if (dir.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /** judge init message */
    private static void judgeInitMessage() {
        if (!judgeInit()) {
            throw Utils.error("Not in an initialized Gitlet directory.");
        }
    }

    /** the commit of init */
    private static void init(String[] args) {
        judgeLength(args, 1);
        if (judgeInit()) {
            throw Utils.error("A Gitlet version-control system already exists in the current directory.");
        }
        Repository.setupPersistence();
        Commit initCommit = new Commit("initial commit", new HashMap<>(), null);
        String sha = Utils.sha1((Object) Utils.serialize(initCommit));
        Repository.initStaged();
        Repository.changeHead(sha);
        //Repository.changeMaster(sha);
        HashMap<String, String> branches = new HashMap<>();
        branches.put("master", sha);
        Repository.initBranches("master", branches);
        initCommit.writeCommit();
    }

    /** the commit of add */
    private static void add(String[] args) {
        judgeInitMessage();
        judgeLength(args, 2);
        String filename = args[1];
        File file;
        file = Utils.join(Repository.CWD, filename);
        if (!file.exists()) {
            throw Utils.error("File does not exist.");
        }
        String contents = Utils.readContentsAsString(file);
        Blob blob = new Blob(contents);
        blob.writeBlobToStage();
        Stage stage = Stage.readStaged();
        boolean flag = true;
        Commit commit = Commit.readCommit(Repository.readHead());
        HashMap<String, String> hashmap = commit.contextHash;
        if (hashmap != null && hashmap.containsKey(filename)) {
            String hash = hashmap.get(filename);
            if (Objects.equals(hash, blob.sha1)) {
                blob.deleteBlobFromStage();
                stage.add.remove(filename);
                flag = false;
            }
        }
        hashmap = stage.add;
        HashSet<String> hashset = stage.remove;
        if (hashmap != null && hashmap.containsKey(filename)) {
            String hash = hashmap.get(filename);
            File f = Utils.join(Stage.STAGED_DIR, hash);
            f.delete();
            stage.add.put(filename, blob.sha1);
            flag = false;
        }
        if (hashset != null && hashset.contains(filename)) {
            blob.deleteBlobFromStage();
            stage.add.remove(filename);
            flag = false;
        }
        if (flag) {
            stage.add.put(filename, blob.sha1);
        }
        Utils.writeObject(Stage.stage, stage);
    }

    /** the commit of commit */
    public static void commit(String[] args) {
        /** Now ignore the situation of branch */
        judgeInitMessage();
        judgeLength(args, 2);
        /** message */
        String message = args[1];
        /** parent */
        List<String> parent = new ArrayList<>();
        parent.add(Repository.readHead());
        /** contextHash */
        File parentfile = Utils.join(Commit.COMMIT_DIR, parent.get(0));
        Commit parentcommit = Utils.readObject(parentfile, Commit.class);
        HashMap<String, String> hashmap = new HashMap<>(parentcommit.contextHash);
        Stage stage = Stage.readStaged();
        HashMap<String, String> stageadd = stage.add;
        HashSet<String> stageremove = stage.remove;
        for (String filename : stageadd.keySet()) {
            String hash = stageadd.get(filename);
            Blob blob = Blob.readBlobFromStage(hash);
            blob.writeBlob();
            hashmap.put(filename, blob.sha1);
        }
        for (String filename : stageremove) {
            hashmap.remove(filename);
        }
        Commit commit = new Commit(message, hashmap, parent);
        String sha1 = Utils.sha1((Object) Utils.serialize(commit));
        commit.writeCommit();
        Repository.changeHead(sha1);
        Branch branch = Branch.readBranch();
        String currentBranch = branch.current_branch;
        HashMap<String, String> branches = branch.branches;
        branches.put(currentBranch, sha1);
        branch.writeBranch();
        Stage.clearStaged();
    }

    /** the commit of rm */
    public static void rm(String[] args) {
        judgeInitMessage();
        judgeLength(args, 2);
        String filename = args[1];
        boolean flag = false;
        /** Unstage the file if it is currently staged for addition. */
        Stage stage = Stage.readStaged();
        HashMap<String, String> hashmap = stage.add;
        HashSet<String> hashset = stage.remove;
        hashset.remove(filename);
        if (hashmap.containsKey(filename)) {
            String hash = hashmap.get(filename);
            File f = Utils.join(Stage.STAGED_DIR, hash);
            f.delete();
            stage.add.remove(filename);
            flag = true;
        }
        /**if the file is tracked in the current commit, stage it
         * for removal and remove the file from the working directory
         * if the user has not already done so (do not remove it
         * unless it is tracked in the current commit).*/
        Commit commit = Commit.readCommit(Repository.readHead());
        HashMap<String, String> hashmap1 = commit.contextHash;
        if (hashmap1 != null && hashmap1.containsKey(filename)) {
            String hash = hashmap1.get(filename);
            File f = Utils.join(Repository.CWD, filename);
            f.delete();
            flag = true;
        }
        if (!flag) {
            throw Utils.error("No reason to remove the file.");
        }
        Utils.writeObject(Stage.stage, stage);
    }

    /** the commit of log */
    private static void log(String[] args) {
        judgeInitMessage();
        judgeLength(args, 1);
        Commit commit = Commit.readCommit(Repository.readHead());
        while (commit.parent != null) {
            printCommitFormat(commit);
            String sha = commit.parent.get(0);
            commit = Commit.readCommit(sha);
        }
        printCommitFormat(commit);
    }

    /** the commit of global_log */
    private static void global_log(String[] args) {
        judgeInitMessage();
        judgeLength(args, 1);
        List<String> filenamelist = Utils.plainFilenamesIn(Commit.COMMIT_DIR);
        for (String filename : filenamelist) {
            Commit commit = Commit.readCommit(filename);
            printCommitFormat(commit);
        }
    }

    /** print commit formally */
    private static void printCommitFormat(Commit commit) {
        String message = commit.message;
        String sha1 = Utils.sha1((Object) Utils.serialize(commit));
        String timestamp = commit.timestamp;
        String formatmessage = String.format("===\ncommit %s\nDate: %s\n%s\n", sha1, timestamp, message);
        System.out.print(formatmessage);
    }

    /** the commit of find */
    private static void find(String[] args) {
        judgeInitMessage();
        judgeLength(args, 2);
        String commitMessage = args[1];
        List<String> filenamelist = Utils.plainFilenamesIn(Commit.COMMIT_DIR);
        boolean flag = false;
        for (String filename : filenamelist) {
            Commit commit = Commit.readCommit(filename);
            String message = commit.message;
            if (message.equals(commitMessage)) {
                System.out.println(filename);
                flag = true;
            }
        }
        if (!flag) {
            throw Utils.error("Found no commit with that message.");
        }
    }

    /** the commit of status */
    private static void status(String[] args) {
        judgeInitMessage();
        judgeLength(args, 1);
        /** === Branches === */
        Branch branch = Branch.readBranch();
        String current_branch = branch.current_branch;
        HashMap<String, String> branches = branch.branches;
        String s = "=== Branches ===\n";
        for (String branch_name : branches.keySet()) {
            if (current_branch.equals(branch_name)) {
                s = String.format("%s*%s\n", s, current_branch);
            }
        }
        s = s + "\n";

        Stage stage = Stage.readStaged();
        /** === Staged Files === */
        HashMap<String, String> stage_add = stage.add;
        s += "=== Staged Files ===\n";
        if (stage_add != null) {
            for (String stage_name : stage_add.keySet()) {
                s = String.format("%s%s\n", s, stage_name);
            }
        }
        s += "\n";
        /** === Removed Files === */
        HashSet<String> stage_remove = stage.remove;
        s += "=== Removed Files ===\n";
        if (stage_remove != null) {
            for (String stage_name : stage_remove) {
                s = String.format("%s%s\n", s, stage_name);
            }
        }
        s += "\n";

        s += "=== Modifications Not Staged For Commit ===\n\n=== Untracked Files ===\n\n";

        /** print */
        System.out.print(s);
    }

    /** the commit of checkout */
    private static void checkout(String[] args) {
        judgeInitMessage();
        if (args.length == 2) {
            // Case 3: checkout [branch name]
            String branchName = args[1];
            Branch branch = Branch.readBranch();

            // If no branch with that name exists
            if (!branch.branches.containsKey(branchName)) {
                throw Utils.error("No such branch exists.");
            }

            // If that branch is the current branch
            if (branch.current_branch.equals(branchName)) {
                throw Utils.error("No need to checkout the current branch.");
            }

            // Check for untracked files that would be overwritten
            checkUntrackedFiles(branchName);

            // Get the target commit
            String targetCommitId = branch.branches.get(branchName);
            Commit targetCommit = Commit.readCommit(targetCommitId);

            // Clear the staging area
            Stage.clearStaged();

            // Update working directory to match target branch
            updateWorkingDirectory(targetCommit);

            // Update HEAD and current branch
            Repository.changeHead(targetCommitId);
            branch.current_branch = branchName;
            branch.writeBranch();

        } else if (args.length == 3 && args[1].equals("--")) {
            // Case 1: checkout -- [file name]
            String fileName = args[2];
            Commit headCommit = Commit.readCommit(Repository.readHead());
            writeFileToWorkingDirectory(headCommit, fileName);

        } else if (args.length == 4 && args[2].equals("--")) {
            // Case 2: checkout [commit id] -- [file name]
            String commitId = args[1];
            String fileName = args[3];
            Commit commit = Commit.readCommit(commitId);
            writeFileToWorkingDirectory(commit, fileName);

        } else {
            throw Utils.error("Incorrect operands.");
        }
    }

    /** Helper method to check for untracked files that would be overwritten */
    private static void checkUntrackedFiles(String targetBranchName) {
        Branch branch = Branch.readBranch();
        String targetCommitId = branch.branches.get(targetBranchName);
        Commit targetCommit = Commit.readCommit(targetCommitId);
        Commit currentCommit = Commit.readCommit(branch.branches.get(branch.current_branch));
        Stage stage = Stage.readStaged();

        List<String> workingFiles = Utils.plainFilenamesIn(Repository.CWD);
        if (workingFiles != null) {
            for (String fileName : workingFiles) {
                // Skip directories
                if (!isWorkFile(fileName)) {
                    continue;
                }

                boolean inCurrentCommit = currentCommit.contextHash.containsKey(fileName);
                boolean inStageAdd = stage.add.containsKey(fileName);
                boolean inStageRemove = stage.remove.contains(fileName);
                boolean inTargetCommit = targetCommit.contextHash.containsKey(fileName);

                // If file is untracked in current branch and would be overwritten
                if (!inCurrentCommit && !inStageAdd && !inStageRemove && inTargetCommit) {
                    throw Utils.error("There is an untracked file in the way; delete it, or add and commit it first.");
                }
            }
        }
    }

    /** Helper method to update working directory to match a commit */
    private static void updateWorkingDirectory(Commit targetCommit) {
        // First, remove all files tracked in current commit but not in target
        Branch branch = Branch.readBranch();
        Commit currentCommit = Commit.readCommit(branch.branches.get(branch.current_branch));

        for (String fileName : currentCommit.contextHash.keySet()) {
            if (!targetCommit.contextHash.containsKey(fileName)) {
                File file = Utils.join(Repository.CWD, fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
        }

        // Then write all files from target commit
        for (Map.Entry<String, String> entry : targetCommit.contextHash.entrySet()) {
            String fileName = entry.getKey();
            String blobId = entry.getValue();
            Blob blob = Blob.readBlob(blobId);
            File file = Utils.join(Repository.CWD, fileName);
            Utils.writeContents(file, blob.contents);
        }
    }

    /** Helper method to write a single file from commit to working directory */
    private static void writeFileToWorkingDirectory(Commit commit, String fileName) {
        if (!commit.contextHash.containsKey(fileName)) {
            throw Utils.error("File does not exist in that commit.");
        }
        String blobId = commit.contextHash.get(fileName);
        Blob blob = Blob.readBlob(blobId);
        File file = Utils.join(Repository.CWD, fileName);
        Utils.writeContents(file, blob.contents);
    }

    /** Helper method to check if a path is a regular file (not directory) */
    private static boolean isWorkFile(String fileName) {
        File file = Utils.join(Repository.CWD, fileName);
        return file.isFile();
    }

    /** The commit of branch */
    private static void branch(String[] args) {
        judgeInitMessage();
        judgeLength(args, 2);
        String branchName = args[1];
        Branch branch = Branch.readBranch();
        String current_branch = branch.branches.get(branchName);
        HashMap<String, String> branches = branch.branches;
        if (branches.containsKey(branchName)) {
            throw Utils.error("A branch with that name already exists.");
        }
        branches.put(branchName, current_branch);
        Repository.changeHead(branchName);
        branch.current_branch = branchName;
        branch.writeBranch();
    }

    /** The commit of rm_branch */
    private static void rm_branch(String[] args) {
        String branchName = args[1];
        Branch branch = Branch.readBranch();
        String current_branch = branch.branches.get(branchName);
        HashMap<String, String> branches = branch.branches;
        if (!branches.containsKey(branchName)) {
            throw Utils.error("A branch with that name does not exist.");
        }
        if (current_branch.equals(branchName)) {
            throw Utils.error("Cannot remove the current branch.");
        }
        branches.remove(branchName);
        branch.writeBranch();
    }

    /** The commit of reset */
    private static void reset(String[] args) {
        judgeInitMessage();
        judgeLength(args, 2);

        String commitId = args[1];

        File commitFile = Utils.join(Commit.COMMIT_DIR, commitId);
        if (!commitFile.exists()) {
            throw Utils.error("No commit with that id exists.");
        }

        Commit targetCommit = Commit.readCommit(commitId);
        Commit currentCommit = Commit.readCommit(Repository.readHead());
        Stage stage = Stage.readStaged();

        List<String> workingFiles = Utils.plainFilenamesIn(Repository.CWD);
        if (workingFiles != null) {
            for (String filename : workingFiles) {
                if (!isWorkFile(filename)) continue;

                boolean inCurrent = currentCommit.contextHash.containsKey(filename);
                boolean inStage = stage.add.containsKey(filename) || stage.remove.contains(filename);
                boolean inTarget = targetCommit.contextHash.containsKey(filename);

                if (!inCurrent && !inStage && inTarget) {
                    throw Utils.error("There is an untracked file in the way; delete it, or add and commit it first.");
                }
            }
        }

        for (String filename : currentCommit.contextHash.keySet()) {
            if (!targetCommit.contextHash.containsKey(filename)) {
                File file = Utils.join(Repository.CWD, filename);
                if (file.exists()) {
                    file.delete();
                }
            }
        }

        for (Map.Entry<String, String> entry : targetCommit.contextHash.entrySet()) {
            String filename = entry.getKey();
            String blobId = entry.getValue();
            Blob blob = Blob.readBlob(blobId);
            File file = Utils.join(Repository.CWD, filename);
            Utils.writeContents(file, blob.contents);
        }

        Branch branch = Branch.readBranch();
        branch.branches.put(branch.current_branch, commitId);
        branch.writeBranch();
        Stage.clearStaged();
        Repository.changeHead(commitId);
    }

    /** The commit of merge */
    private static void merge(String[] args) {
        judgeInitMessage();
        judgeLength(args, 2);
    }
}
