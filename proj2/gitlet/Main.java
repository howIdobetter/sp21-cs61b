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
            stage.remove.remove(filename);
            flag = false;
        }
        if (flag) {
            stage.add.put(filename, blob.sha1);
        }
        Utils.writeObject(Stage.stage, stage);
    }

    /** the commit of commit */
    public static void commit(String[] args) {
        commit(args, null);
    }
    
    /** Creates a commit with the given message. */
    public static void commit(String[] args, List<String> parents) {
        exitIfNotInitialized();
        validateArgsLength(args, 2);
        /** message */
        String message = args[1];
        if (message.equals("") || message == null) {
            Utils.message("Please enter a commit message.");
            return;
        }
        /** parent */
        List<String> parent;
        if (parents == null) {
            parent = new ArrayList<>();
            parent.add(Repository.readHead());
        } else {
            parent = parents;
        }
        /** contextHash */
        File parentfile = Utils.join(Commit.COMMIT_DIR, parent.get(0));
        Commit parentcommit = Utils.readObject(parentfile, Commit.class);
        HashMap<String, String> hashmap = new HashMap<>(parentcommit.contextHash);
        Stage stage = Stage.readStaged();
        HashMap<String, String> stageadd = stage.add;
        HashSet<String> stageremove = stage.remove;
        if (stage.add.isEmpty() && stage.remove.isEmpty()) {
            Utils.message("No changes added to the commit.");
            return;
        }
        for (String filename : stageadd.keySet()) {
            String hash = stageadd.get(filename);
            Blob blob;
            try {
                blob = Blob.readBlobFromStage(hash);
            } catch (IllegalArgumentException e) {
                // 如果暂存文件不存在，尝试从对象库读取
                blob = Blob.readBlob(hash);
            }
            blob.writeBlob();
            hashmap.put(filename, blob.sha1);
        }
        for (String filename : stageremove) {
            hashmap.remove(filename);
        }
        Commit commit = new Commit(message, hashmap, parent);
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

    /** Removes a file from tracking. */
    public static void rm(String[] args) {
        exitIfNotInitialized();
        validateArgsLength(args, 2);
        String filename = args[1];
        boolean flag = false;
        /** Unstage the file if it is currently staged for addition. */
        Stage stage = Stage.readStaged();
        HashMap<String, String> hashmap = stage.add;
        HashSet<String> hashset = stage.remove;
        hashset.add(filename);
        if (hashmap.containsKey(filename)) {
            String hash = hashmap.get(filename);
            File f = Utils.join(Stage.STAGED_DIR, hash);
            f.delete();
            stage.add.remove(filename);
            stage.remove.remove(filename);
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
            Utils.message("No reason to remove the file.");
            return;
        }
        Utils.writeObject(Stage.stage, stage);
    }

    /** Displays commit history starting from HEAD. */
    private static void log(String[] args) {
        exitIfNotInitialized();
        validateArgsLength(args, 1);
        String Head = Repository.readHead();
        Commit commit = Commit.readCommit(Head);
        while (commit.parent != null) {
            printCommitFormat(commit, commit.sha);
            String sha = commit.parent.get(0);
            commit = Commit.readCommit(sha);
        }
        printCommitFormat(commit, commit.sha);
    }

    /** Displays all commits ever made. */
    private static void globalLog(String[] args) {
        exitIfNotInitialized();
        validateArgsLength(args, 1);
        List<String> filenamelist = Utils.plainFilenamesIn(Commit.COMMIT_DIR);
        for (String filename : filenamelist) {
            Commit commit = Commit.readCommit(filename);
            String sha = commit.sha;
            printCommitFormat(commit, sha);
        }
    }

    /** print commit formally */
    private static void printCommitFormat(Commit commit, String sha) {
        String message = commit.message;
        String sha1 = sha;
        String timestamp = commit.timestamp;
        String formatmessage = String.format("===\ncommit %s\nDate: %s\n%s\n\n", sha1, timestamp, message);
        System.out.print(formatmessage);
    }

    /** Finds and prints commits with the given message. */
    private static void find(String[] args) {
        exitIfNotInitialized();
        validateArgsLength(args, 2);
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
        /** === Branches === */
        Branch branch = Branch.readBranch();
        String currentBranch = branch.current_branch;
        HashMap<String, String> branches = branch.branches;
        String s = "=== Branches ===\n";
        List<String> branchNames = new ArrayList<>(branches.keySet());
        Collections.sort(branchNames);
        for (String branchName : branchNames) {
            if (currentBranch.equals(branchName)) {
                s = String.format("%s*%s\n", s, branchName);
            } else {
                s = String.format("%s%s\n", s, branchName);
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
            // 支持部分commit ID
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
     * Returns all commit IDs in the repository as an iterable List
     * (unordered, just the raw commit IDs from the commit directory)
     */
    public static List<String> getAllCommitIds() {
        exitIfNotInitialized();

        List<String> commitIds = Utils.plainFilenamesIn(Commit.COMMIT_DIR);
        return commitIds != null ? commitIds : Collections.emptyList();
    }

    /** Helper method to check for untracked files that would be overwritten */
    private static void checkUntrackedFiles(String targetBranchName) {
        Branch branch = Branch.readBranch();
        String targetCommitId = branch.branches.get(targetBranchName);
        Commit targetCommit = Commit.readCommit(targetCommitId);
        Commit currentCommit = Commit.readCommit(branch.branches.get(branch.current_branch));
        Stage stage = Stage.readStaged();

        // 获取当前工作目录中的所有文件
        List<String> workingFiles = Utils.plainFilenamesIn(Repository.CWD);
        if (workingFiles != null) {
            for (String fileName : workingFiles) {
                // 跳过目录和.gitlet目录
                if (!isWorkFile(fileName) || fileName.startsWith(".gitlet")) {
                    continue;
                }

                boolean inCurrentCommit = currentCommit.contextHash.containsKey(fileName);
                boolean inStageAdd = stage.add.containsKey(fileName);
                boolean inStageRemove = stage.remove.contains(fileName);
                boolean inTargetCommit = targetCommit.contextHash.containsKey(fileName);

                // 如果文件在当前分支未跟踪且会被目标分支覆盖
                if (!inCurrentCommit && !inStageAdd && inTargetCommit) {
                    // 检查文件内容是否不同
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
            writeContents(file, blob.contents);
        }
    }

    /** Helper method to write a single file from commit to working directory */
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

    /** Helper method to check if a path is a regular file (not directory) */
    private static boolean isWorkFile(String fileName) {
        File file = Utils.join(Repository.CWD, fileName);
        return file.isFile();
    }
    
    /** Helper method to find full commit ID from abbreviated ID */
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
        // 支持部分commit ID
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

        // 1. 检查前置条件
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

        // 2. 获取提交对象
        String currentId = branch.branches.get(branch.current_branch);
        String givenId = branch.branches.get(branchName);
        String splitPointId = Repository.findSplitPoint(currentId, givenId);

        // 3. 检查特殊情况
        if (splitPointId.equals(givenId)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitPointId.equals(currentId)) {
            checkout(new String[]{"checkout", branchName});
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        // 4. 检查未跟踪文件
        checkUntrackedFilesForMerge(currentId, givenId);

        // 5. 开始合并
        Commit splitCommit = Commit.readCommit(splitPointId);
        Commit currentCommit = Commit.readCommit(currentId);
        Commit givenCommit = Commit.readCommit(givenId);
        boolean conflict = false;

        // 6. 收集所有相关文件
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(splitCommit.contextHash.keySet());
        allFiles.addAll(currentCommit.contextHash.keySet());
        allFiles.addAll(givenCommit.contextHash.keySet());

        // 7. 处理每个文件
        for (String file : allFiles) {
            String splitBlob = splitCommit.contextHash.get(file);
            String currentBlob = currentCommit.contextHash.get(file);
            String givenBlob = givenCommit.contextHash.get(file);

            // Case 1: 在split点不存在
            if (splitBlob == null) {
                if (currentBlob != null && givenBlob != null && !currentBlob.equals(givenBlob)) {
                    resolveConflict(file, currentBlob, givenBlob);
                    conflict = true;
                } else if (currentBlob == null && givenBlob != null) {
                    // 检出给定分支版本
                    writeFileToWorkingDirectory(givenCommit, file);
                    stage.add.put(file, givenBlob);
                }
            }
            // Case 2: 在split点存在
            else {
                boolean changedInCurrent = !Objects.equals(splitBlob, currentBlob);
                boolean changedInGiven = !Objects.equals(splitBlob, givenBlob);

                if (!changedInCurrent && changedInGiven) {
                    if (givenBlob == null) {
                        // 给定分支删除：删除文件
                        File f = Utils.join(Repository.CWD, file);
                        if (f.exists()) f.delete();
                        stage.remove.add(file);
                        stage.add.remove(file);
                    } else {
                        // 给定分支修改：检出文件
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

        // 8. 创建合并提交
        Utils.writeObject(Stage.stage, stage);
        List<String> parents = new ArrayList<>();
        parents.add(currentId);
        parents.add(givenId);
        commit(new String[]{"commit", "Merged " + branchName + " into " + branch.current_branch + "."}, parents);

        // 9. 输出冲突信息
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** 检查未跟踪文件是否会被覆盖 */
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

        // 写入工作目录
        File f = Utils.join(Repository.CWD, file);
        writeContents(f, conflictContent);

        // 添加到暂存区
        Blob conflictBlob = new Blob(conflictContent);
        conflictBlob.writeBlobToStage();
        Stage stage = Stage.readStaged();
        stage.add.put(file, conflictBlob.sha1);
        // 确保从remove集合中移除该文件
        stage.remove.remove(file);
        Utils.writeObject(Stage.stage, stage);
    }
}
