import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// checks every turn of each Stage 3 run, not just the final result, usage: java ReplayCheck
// a run passes only if its trace (turn-end lines, recoveries, result) matches test/<run>.expected
public class ReplayCheck {
    public static void main(String[] a) throws Exception {
        boolean ok = true;
        PrintStream out = System.out;
        for (String run : List.of("stage3-victory", "stage3-defeat")) {
            List<String> expected = Files.readAllLines(Path.of("test/" + run + ".expected"));
            // keep the game screens quiet, only the result of the check is printed
            System.setOut(new PrintStream(OutputStream.nullOutputStream()));
            List<String> actual = Replay.run("test/" + run + ".txt");
            System.setOut(out);
            ok &= compare(run, expected, actual);
        }
        System.out.println(ok ? "All replay traces match." : "Replay traces differ.");
        if (!ok) {
            System.exit(1);
        }
    }

    private static boolean compare(String run, List<String> expected, List<String> actual) {
        for (int i = 0; i < Math.max(expected.size(), actual.size()); i++) {
            String e = i < expected.size() ? expected.get(i) : "(nothing)";
            String g = i < actual.size() ? actual.get(i) : "(nothing)";
            if (!e.equals(g)) {
                System.out.println(run + ": FAIL at line " + (i + 1) + "\n  expected: " + e + "\n  actual:   " + g);
                return false;
            }
        }
        System.out.println(run + ": PASS (" + expected.size() + " lines)");
        return true;
    }
}
