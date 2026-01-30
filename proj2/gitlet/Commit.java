package gitlet;

import java.io.File;
import java.io.Serializable;
import static gitlet.Utils.*;

import java.util.*;
import java.text.SimpleDateFormat;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Yuhao Wang
 */
public class Commit implements Serializable {
    /**
     * We need parent, hashes, timestamp and message.
     */

    /** Commit message. */
    public String message;
    /** Maps file names to blob SHA-1 hashes. */
    public HashMap<String, String> contextHash;
    /** List of parent commit SHA-1 hashes. */
    public List<String> parent;
    /** Commit timestamp. */
    public String timestamp;
    /** SHA-1 hash of this commit. */
    public String sha;

    static final File COMMIT_DIR = Utils.join(Repository.GITLET_DIR, "commits");

    public Commit(String message, HashMap<String, String> contextHash, List<String> parents) {
        this.message = message;
        this.parent = parents;
        this.contextHash = contextHash;
        this.timestamp = formatCurrentTime();
    }

    /**
     * Formats current time to specified format (Sat Nov 11 12:30:00 2017 -0800).
     * @return formatted timestamp string
     */
    private String formatCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss yyyy Z",
                Locale.US
        );
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        return sdf.format(new Date());
    }

    /** Reads a Commit by the SHA-1 hash. */
    public static Commit readCommit(String hash) {
        File f = join(COMMIT_DIR, hash);
        return readObject(f, Commit.class);
    }

    /** Writes a Commit to disk. */
    public void writeCommit() {
        String hash = sha1((Object) serialize(this));
        this.sha = hash;
        File f = join(COMMIT_DIR, hash);
        writeObject(f, this);
    }

}
