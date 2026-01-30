package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.writeContents;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Yuhao Wang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            Utils.message("Please enter a command.");
            return;
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
                globalLog(args);
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
                rmBranch(args);
                break;
            case "reset":
                reset(args);
                break;
            case "merge":
                merge(args);
                break;
            default:
                Utils.message("No command with that name exists.");
                return;
        }
    }

    /** Validates that args has the expected length, exits program if not. */
    private static void validateArgsLength(String[] args, int length) {
        if (args.length != length) {
            Utils.message("Incorrect operands.");
            System.exit(0);
        }
    }

    /** Checks if gitlet repository is initialized. */
    private static boolean isInitialized() {
        return Repository.GITLET_DIR.exists();
    }

    /** Exits program if gitlet repository is not initialized. */
    private static void exitIfNotInitialized() {
        if (!isInitialized()) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    /** Initializes a gitlet repository. */
    private static void init(String[] args) {
        validateArgsLength(args, 1);
        if (isInitialized()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        Repository.setupPersistence();
        Commit initCommit = new Commit("initial commit", new HashMap<>(), null);
        initCommit.writeCommit();
        String sha = initCommit.sha;
        Repository.initStaged();
        Repository.changeHead(sha);
        //Repository.changeMaster(sha);
        HashMap<String, String> branches = new HashMap<>();
        branches.put("master", sha);
        Repository.initBranches("master", branches);
    }

    /** Adds a file to the staging area. */
    private static void add(String[] args) {
        exitIfNotInitialized();
        validateArgsLength(args, 2);
        String filename = args[1];
        File file;
        file = Utils.join(Repository.CWD, filename);
        if (!file.exists()) {
            Utils.message("File does not exist.");
            return;
        }
        String contents = Utils.readContentsAsString(file);
        Blob blob = new Blob(contents);
        blob.writeBlobToStage();
        Stage stage = Stage.readStaged();
        boolean fileModified = true;
        Commit commit = Commit.readCommit(Repository.readHead());
        HashMap<String, String> commitFiles = commit.contextHash;
        if (commitFiles != null && commitFiles.containsKey(filename)) {
            String hash = commitFiles.get(filename);
            if (Objects.equals(hash, blob.sha1)) {
                blob.deleteBlobFromStage();
                stage.add.remove(filename);
                fileModified = false;
            }
        }
        HashMap<String, String> stagedFiles = stage.add;
        HashSet<String> removedFiles = stage.remove;
        if (stagedFiles != null && stagedFiles.containsKey(filename)) {
            String hash = stagedFiles.get(filename);
            File f = Utils.join(Stage.STAGED_DIR, hash);
            f.delete();
            stage.add.put(filename, blob.sha1);
            fileModified = false;
        }
        if (removedFiles != null && removedFiles.contains(filename)) {
            blob.deleteBlobFromStage();
            stage.add.remove(filename);
            stage.remove.remove(filename);
            fileModified = false;
        }
        if (fileModified) {
            stage.add.put(filename, blob.sha1);
        }
        Utils.writeObject(Stage.stage, stage);
    }

    /** Creates a commit with the given message. 
     *  This is an overload that uses the default parent (current HEAD).
     */
    public static void commit(String[] args) {
        commit(args, null);
    }
    
    /** Creates a commit with the given message.
     *  @param args command line arguments [commit, message]
     *  @param parents list of parent commit SHA-1 hashes (null for single parent)
     */
    public static void commit(String[] args, List<String> parents) {
        exitIfNotInitialized();
        validateArgsLength(args, 2);
        String message = args[1];
        if (message.equals("") || message == null) {
            Utils.message("Please enter a commit message.");
            return;
        }
        List<String> parentList;
        if (parents == null) {
            parentList = new ArrayList<>();
            parentList.add(Repository.readHead());
        } else {
            parentList = parents;
        }
        File parentFile = Utils.join(Commit.COMMIT_DIR, parentList.get(0));
        Commit parentCommit = Utils.readObject(parentFile, Commit.class);
        HashMap<String, String> fileMap = new HashMap<>(parentCommit.contextHash);
        Stage stage = Stage.readStaged();
        HashMap<String, String> stagedAdded = stage.add;
        HashSet<String> stagedRemoved = stage.remove;
        if (stage.add.isEmpty() && stage.remove.isEmpty()) {
            Utils.message("No changes added to the commit.");
            return;
        }
        for (String filename : stagedAdded.keySet()) {
            String hash = stagedAdded.get(filename);
            Blob blob;
            try {
                blob = Blob.readBlobFromStage(hash);
            } catch (IllegalArgumentException e) {
                // If staged file doesn't exist, try reading from object store
                blob = Blob.readBlob(hash);
            }
            blob.writeBlob();
            fileMap.put(filename, blob.sha1);
        }
        for (String filename : stagedRemoved) {
            fileMap.remove(filename);
        }
        Commit commit = new Commit(message, fileMap, parentList);
        commit.writeCommit();
        String sha1 = commit.sha;
        Repository.changeHead(sha1);
        Branch branch = Branch.readBranch();
        String currentBranch = branch.current_branch;
        HashMap<String, String> branches = branch.branches;
        branches.put(currentBranch, sha1);
        branch.writeBranch();
        Stage.clearStaged();
    }

    /** Removes a file from tracking.
     *  Unstages the file if currently staged for addition.
     *  If tracked in current commit, stages for removal and deletes from working directory.
     */
    public static void rm(String[] args) {
        exitIfNotInitialized();
        validateArgsLength(args, 2);
        String filename = args[1];
        boolean fileRemoved = false;
        Stage stage = Stage.readStaged();
        HashMap<String, String> stagedFiles = stage.add;
        HashSet<String> removedFiles = stage.remove;
        removedFiles.add(filename);
        if (stagedFiles.containsKey(filename)) {
            String hash = stagedFiles.get(filename);
            File f = Utils.join(Stage.STAGED_DIR, hash);
            f.delete();
            stage.add.remove(filename);
            stage.remove.remove(filename);
            fileRemoved = true;
        }
        Commit commit = Commit.readCommit(Repository.readHead());
        HashMap<String, String> commitFiles = commit.contextHash;
        if (commitFiles != null && commitFiles.containsKey(filename)) {
            String hash = commitFiles.get(filename);
            File f = Utils.join(Repository.CWD, filename);
            f.delete();
            fileRemoved = true;
        }
        if (!fileRemoved) {
            Utils.message("No reason to remove the file.");
            return;
        }
        Utils.writeObject(Stage.stage, stage);
    }

    /** Displays commit history starting from HEAD. */
    private static void log(String[] args) {
        exitIfNotInitialized();
        validateArgsLength(args, 1);
        String head = Repository.readHead();
        Commit commit = Commit.readCommit(head);
        while (commit.parent != null) {
            printCommitFormat(commit, commit.sha);
            commit = Commit.readCommit(commit.parent.get(0));
        }
        printCommitFormat(commit, commit.sha);
    }

    /** Displays all commits ever made. */
    private static void globalLog(String[] args) {
        exitIfNotInitialized();
        validateArgsLength(args, 1);
        List<String> filenameList = Utils.plainFilenamesIn(Commit.COMMIT_DIR);
        for (String filename : filenameList) {
            Commit commit = Commit.readCommit(filename);
            printCommitFormat(commit, commit.sha);
        }
    }

    /** Formats and prints commit information. */
    private static void printCommitFormat(Commit commit, String sha) {
        String formatMessage = String.format("===\ncommit %s\nDate: %s\n%s\n\n", 
                                            sha, commit.timestamp, commit.message);
        System.out.print(formatMessage);
    }

    /** Finds and prints commits with the given message. */
    private static void find(String[] args) {
        exitIfNotInitialized();
        validateArgsLength(args, 2);
        String commitMessage = args[1];
        List<String> filenameList = Utils.plainFilenamesIn(Commit.COMMIT_DIR);
        boolean found = false;
        for (String filename : filenameList) {
            Commit commit = Commit.readCommit(filename);
            if (commit.message.equals(commitMessage)) {
                System.out.println(filename);
                found = true;
            }
        }
        if (!found) {
            Utils.message("Found no commit with that message.");
            return;
        }
    }

    /** Displays status of the repository. */
    private static void status(String[] args) {
        if (!isInitialized()) {
            Utils.message("Not in an initialized Gitlet directory.");
            return;
        }
        validateArgsLength(args, 1);
        Branch branch = Branch.readBranch();
        String currentBranch = branch.current_branch;
        HashMap<String, String> branches = branch.branches;
        StringBuilder statusOutput = new StringBuilder("=== Branches ===\n");
        List<String> branchNames = new ArrayList<>(branches.keySet());
        Collections.sort(branchNames);
        for (String branchName : branchNames) {
            if (currentBranch.equals(branchName)) {
                statusOutput.append(String.format("*%s\n", branchName));
            } else {
                statusOutput.append(String.format("%s\n", branchName));
            }
        }
        statusOutput.append("\n");

        Stage stage = Stage.readStaged();
        HashMap<String, String> stagedFiles = stage.add;
        statusOutput.append("=== Staged Files ===\n");
        if (stagedFiles != null) {
            for (String stageName : stagedFiles.keySet()) {
                statusOutput.append(String.format("%s\n", stageName));
            }
        }
        statusOutput.append("\n");
        HashSet<String> removedFiles = stage.remove;
        statusOutput.append("=== Removed Files ===\n");
        if (removedFiles != null) {
            for (String stageName : removedFiles) {
                statusOutput.append(String.format("%s\n", stageName));
            }
        }
        statusOutput.append("\n");

        statusOutput.append("=== Modifications Not Staged For Commit ===\n\n=== Untracked Files ===\n\n");

        System.out.print(statusOutput.toString());
    }

    /** Checks out files, commits, or branches. */
    private static void checkout(String[] args) {
        exitIfNotInitialized();
        if (args.length == 2) {
            // Case 3: checkout [branch name]
            String branchName = args[1];
            Branch branch = Branch.readBranch();

            // If no branch with that name exists
            if (!branch.branches.containsKey(branchName)) {
                Utils.message("No such branch exists.");
                return;
            }

            // If that branch is the current branch
            if (branch.current_branch.equals(branchName)) {
                Utils.message("No need to checkout the current branch.");
                return;
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
            List<String> allCommits = getAllCommitIds();
            // Support abbreviated commit IDs
            if (commitId.length() < 40) {
                commitId = findFullCommitId(commitId);
            }
            if (!allCommits.contains(commitId)) {
                Utils.message("No commit with that id exists.");
                return;
            }
            String fileName = args[3];
            Commit commit = Commit.readCommit(commitId);
            writeFileToWorkingDirectory(commit, fileName);

        } else {
            Utils.message("Incorrect operands.");
            return;
        }
    }

    /**
     * Returns all commit IDs in the repository as an iterable List.
     * The list is unordered and contains raw commit IDs from the commit directory.
     * @return list of all commit SHA-1 hashes
     */
    public static List<String> getAllCommitIds() {
        exitIfNotInitialized();

        List<String> commitIds = Utils.plainFilenamesIn(Commit.COMMIT_DIR);
        return commitIds != null ? commitIds : Collections.emptyList();
    }

    /** 
     * Helper method to check for untracked files that would be overwritten.
     * @param targetBranchName name of the branch being checked out
     */
    private static void checkUntrackedFiles(String targetBranchName) {
        Branch branch = Branch.readBranch();
        String targetCommitId = branch.branches.get(targetBranchName);
        Commit targetCommit = Commit.readCommit(targetCommitId);
        Commit currentCommit = Commit.readCommit(branch.branches.get(branch.current_branch));
        Stage stage = Stage.readStaged();

        // Get all files in current working directory
        List<String> workingFiles = Utils.plainFilenamesIn(Repository.CWD);
        if (workingFiles != null) {
            for (String fileName : workingFiles) {
                // Skip directories and .gitlet directory
                if (!isWorkFile(fileName) || fileName.startsWith(".gitlet")) {
                    continue;
                }

                boolean inCurrentCommit = currentCommit.contextHash.containsKey(fileName);
                boolean inStageAdd = stage.add.containsKey(fileName);
                boolean inStageRemove = stage.remove.contains(fileName);
                boolean inTargetCommit = targetCommit.contextHash.containsKey(fileName);

                // If file is untracked in current branch but would be overwritten by target branch
                if (!inCurrentCommit && !inStageAdd && inTargetCommit) {
                    // Check if file content is different
                    File workingFile = Utils.join(Repository.CWD, fileName);
                    String workingContent = Utils.readContentsAsString(workingFile);
                    String targetBlobId = targetCommit.contextHash.get(fileName);
                    Blob targetBlob = Blob.readBlob(targetBlobId);
                    
                    if (!workingContent.equals(targetBlob.contents)) {
                        Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                        System.exit(0);
                    }
                }
            }
        }
    }

    /** 
     * Helper method to update working directory to match a commit.
     * @param targetCommit the commit to update working directory to match
     */
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
            writeContents(file, blob.contents);
        }
    }

    /** 
     * Helper method to write a single file from commit to working directory.
     * @param commit the commit to extract the file from
     * @param fileName the name of the file to write
     */
    private static void writeFileToWorkingDirectory(Commit commit, String fileName) {
        if (!commit.contextHash.containsKey(fileName)) {
            Utils.message("File does not exist in that commit.");
            return;
        }
        String blobId = commit.contextHash.get(fileName);
        Blob blob = Blob.readBlob(blobId);
        File file = Utils.join(Repository.CWD, fileName);
        writeContents(file, blob.contents);
    }

    /** 
     * Helper method to check if a path is a regular file (not directory).
     * @param fileName the name of the file to check
     * @return true if the path is a regular file, false otherwise
     */
    private static boolean isWorkFile(String fileName) {
        File file = Utils.join(Repository.CWD, fileName);
        return file.isFile();
    }
    
    /** 
     * Helper method to find full commit ID from abbreviated ID.
     * @param abbreviatedId the abbreviated commit ID (prefix)
     * @return the full commit ID
     * @throws GitletException if no commit with that ID exists
     */
    private static String findFullCommitId(String abbreviatedId) {
        List<String> commitFiles = Utils.plainFilenamesIn(Commit.COMMIT_DIR);
        if (commitFiles == null) {
            throw Utils.error("No commit with that id exists.");
        }
        
        for (String commitId : commitFiles) {
            if (commitId.startsWith(abbreviatedId)) {
                return commitId;
            }
        }
        
        throw Utils.error("No commit with that id exists.");
    }

    /** Creates a new branch. */
    private static void branch(String[] args) {
        exitIfNotInitialized();
        validateArgsLength(args, 2);
        String branchName = args[1];
        Branch branch = Branch.readBranch();
        String currentCommitId = Repository.readHead();
        HashMap<String, String> branches = branch.branches;
        if (branches.containsKey(branchName)) {
            Utils.message("A branch with that name already exists.");
            return;
        }
        branches.put(branchName, currentCommitId);
        branch.writeBranch();
    }

    /** Removes a branch. */
    private static void rmBranch(String[] args) {
        exitIfNotInitialized();
        validateArgsLength(args, 2);
        String branchName = args[1];
        Branch branch = Branch.readBranch();
        String currentBranch = branch.current_branch;
        HashMap<String, String> branches = branch.branches;
        if (!branches.containsKey(branchName)) {
            Utils.message("A branch with that name does not exist.");
            return;
        }
        if (currentBranch.equals(branchName)) {
            Utils.message("Cannot remove the current branch.");
            return;
        }
        branches.remove(branchName);
        branch.writeBranch();
    }

    /** Resets to a given commit. */
    private static void reset(String[] args) {
        exitIfNotInitialized();
        validateArgsLength(args, 2);

        String commitId = args[1];
        // Support abbreviated commit IDs
        if (commitId.length() < 40) {
            commitId = findFullCommitId(commitId);
        }

        File commitFile = Utils.join(Commit.COMMIT_DIR, commitId);
        if (!commitFile.exists()) {
            Utils.message("No commit with that id exists.");
            return;
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
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    return;
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
            writeContents(file, blob.contents);
        }

        Branch branch = Branch.readBranch();
        branch.branches.put(branch.current_branch, commitId);
        branch.writeBranch();
        Stage.clearStaged();
        Repository.changeHead(commitId);
    }

    /** Merges the given branch into the current branch. */
    private static void merge(String[] args) {
        exitIfNotInitialized();
        validateArgsLength(args, 2);
        String branchName = args[1];

        // Check preconditions
        Stage stage = Stage.readStaged();
        if (!stage.add.isEmpty() || !stage.remove.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }

        Branch branch = Branch.readBranch();
        if (!branch.branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        if (branch.current_branch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        // Get commit objects
        String currentId = branch.branches.get(branch.current_branch);
        String givenId = branch.branches.get(branchName);
        String splitPointId = Repository.findSplitPoint(currentId, givenId);

        // Check special cases
        if (splitPointId.equals(givenId)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitPointId.equals(currentId)) {
            checkout(new String[]{"checkout", branchName});
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        // Check untracked files
        checkUntrackedFilesForMerge(currentId, givenId);

        // Begin merge
        Commit splitCommit = Commit.readCommit(splitPointId);
        Commit currentCommit = Commit.readCommit(currentId);
        Commit givenCommit = Commit.readCommit(givenId);
        boolean conflict = false;

        // Collect all relevant files
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(splitCommit.contextHash.keySet());
        allFiles.addAll(currentCommit.contextHash.keySet());
        allFiles.addAll(givenCommit.contextHash.keySet());

        // Process each file
        for (String file : allFiles) {
            String splitBlob = splitCommit.contextHash.get(file);
            String currentBlob = currentCommit.contextHash.get(file);
            String givenBlob = givenCommit.contextHash.get(file);

            // Case 1: Does not exist at split point
            if (splitBlob == null) {
                if (currentBlob != null && givenBlob != null && !currentBlob.equals(givenBlob)) {
                    resolveConflict(file, currentBlob, givenBlob);
                    conflict = true;
                } else if (currentBlob == null && givenBlob != null) {
                    // Check out given branch version
                    writeFileToWorkingDirectory(givenCommit, file);
                    stage.add.put(file, givenBlob);
                }
            }
            // Case 2: Exists at split point
            else {
                boolean changedInCurrent = !Objects.equals(splitBlob, currentBlob);
                boolean changedInGiven = !Objects.equals(splitBlob, givenBlob);

                if (!changedInCurrent && changedInGiven) {
                    if (givenBlob == null) {
                        // Given branch deleted: delete file
                        File f = Utils.join(Repository.CWD, file);
                        if (f.exists()) f.delete();
                        stage.remove.add(file);
                        stage.add.remove(file);
                    } else {
                        // Given branch modified: check out file
                        writeFileToWorkingDirectory(givenCommit, file);
                        stage.add.put(file, givenBlob);
                        stage.remove.remove(file);
                    }
                } else if (changedInCurrent && changedInGiven && !Objects.equals(currentBlob, givenBlob)) {
                    resolveConflict(file, currentBlob, givenBlob);
                    conflict = true;
                }
            }
        }

        // Create merge commit
        Utils.writeObject(Stage.stage, stage);
        List<String> parents = new ArrayList<>();
        parents.add(currentId);
        parents.add(givenId);
        commit(new String[]{"commit", "Merged " + branchName + " into " + branch.current_branch + "."}, parents);

        // Print conflict message if necessary
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** 
     * Checks if untracked files would be overwritten during merge.
     * @param currentId SHA-1 hash of current branch's commit
     * @param givenId SHA-1 hash of branch being merged in
     */
    private static void checkUntrackedFilesForMerge(String currentId, String givenId) {
        Commit currentCommit = Commit.readCommit(currentId);
        Commit givenCommit = Commit.readCommit(givenId);
        Stage stage = Stage.readStaged();

        List<String> workingFiles = Utils.plainFilenamesIn(Repository.CWD);
        if (workingFiles == null) return;

        for (String file : workingFiles) {
            boolean inCurrent = currentCommit.contextHash.containsKey(file);
            boolean inGiven = givenCommit.contextHash.containsKey(file);
            boolean staged = stage.add.containsKey(file) || stage.remove.contains(file);

            if (!inCurrent && !staged && inGiven) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    /**
     * Resolves a merge conflict by creating a conflict marker file.
     * @param file the filename with the conflict
     * @param currentBlobId SHA-1 hash of current branch's blob (null if doesn't exist)
     * @param givenBlobId SHA-1 hash of given branch's blob (null if doesn't exist)
     */
    private static void resolveConflict(String file, String currentBlobId, String givenBlobId) {
        String currentContent = currentBlobId != null ?
                Blob.readBlob(currentBlobId).contents : "";
        String givenContent = givenBlobId != null ?
                Blob.readBlob(givenBlobId).contents : "";

        String conflictContent = "<<<<<<< HEAD\n" +
                currentContent +
                "=======\n" +
                givenContent +
                ">>>>>>>\n";

        // Write to working directory
        File f = Utils.join(Repository.CWD, file);
        writeContents(f, conflictContent);

        // Add to staging area
        Blob conflictBlob = new Blob(conflictContent);
        conflictBlob.writeBlobToStage();
        Stage stage = Stage.readStaged();
        stage.add.put(file, conflictBlob.sha1);
        // Ensure file is removed from remove set
        stage.remove.remove(file);
        Utils.writeObject(Stage.stage, stage);
    }
}
