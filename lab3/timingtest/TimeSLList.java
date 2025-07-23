package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns = new AList<>();
        AList<Integer> opCounts = new AList<Integer>();
        AList<Double> times = new AList<>();
        Ns.addLast(1000);
        Ns.addLast(2000);
        Ns.addLast(4000);
        Ns.addLast(8000);
        Ns.addLast(16000);
        Ns.addLast(32000);
        Ns.addLast(64000);
        Ns.addLast(128000);
        int M = 10000;
        for (int N = 0; N < Ns.size(); N += 1) {
            opCounts.addLast(M);
        }
        AList<Integer> ANs = new AList<>();
        ANs.addLast(1000);
        ANs.addLast(2000);
        ANs.addLast(4000);
        ANs.addLast(8000);
        ANs.addLast(16000);
        ANs.addLast(32000);
        ANs.addLast(64000);
        ANs.addLast(128000);
        for (int N = 0; N < ANs.size(); N += 1) {
            SLList<Integer> Operation = new SLList<>();
            int num =  ANs.get(N);
            for (int opCount = 0; opCount < num; opCount += 1) {
                Operation.addLast(1);
            }
            Stopwatch sw = new Stopwatch();
            for (int opCount = 0; opCount < M; opCount += 1) {
                int x = Operation.getLast();
            }
            double timePerOp = sw.elapsedTime();
            times.addLast(timePerOp);
        }
        printTimingTable(Ns, times, opCounts);
    }

}
