package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import static gitlet.Utils.*;

import java.util.*;
import java.text.SimpleDateFormat;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Yuhao Wang
 */
public class Commit implements Serializable {
    /**
     * We need parent, hashes, timestamp and message.
     */

    public String message; // 提交信息
    public HashMap<String, String> contextHash; // File names
    public List<String> parent; // Parent commit hash
    public String timestamp; // Timestamp

    static final File COMMIT_DIR = Utils.join(Repository.GITLET_DIR, "commits");

    /* TODO: fill in the rest of this class. */
    public Commit(String message, HashMap<String, String> contextHash, List<String> parents) {
        this.message = message;
        this.parent = parents;
        this.contextHash = contextHash;
        this.timestamp = formatCurrentTime();
    }

    /** Get timestamp */
    /** Format current time to specified format (Sat Nov 11 12:30:00 2017 -0800) */
    private String formatCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss yyyy Z",
                Locale.US
        );
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        return sdf.format(new Date());
    }

    /** read a Commit by the sha1. */
    public static Commit readCommit(String hash1) {
        Commit m;
        File f = join(COMMIT_DIR, hash1);
        m = readObject(f, Commit.class);
        return m;
    }

    /** write a Commit. */
    public void writeCommit() {
        String hash = sha1((Object) serialize(this));
        File f = join(COMMIT_DIR, hash);
        writeObject(f, this);
    }

}
