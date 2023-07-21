package LungoBrowser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version {

    public enum Stage {
        Alpha("A"), Beta("B"), ReleaseCandidate("RC"), Release("R"), PostRelease("PR");

        private final String Short;

        Stage(String N) {
            this.Short = N;
        }

        public String toString() {
            return Short;
        }

        public static Stage toStage(String N) {
            switch (N) {
                case "A":
                    return Alpha;
                case "B":
                    return Beta;
                case "RC":
                    return ReleaseCandidate;
                case "R":
                    return Release;
                case "PR":
                    return PostRelease;
                default:
                    break;
            }
            return null;
        }
    }

    private Stage _Stage;
    private Integer Major;
    private Integer Minor;
    private Integer Patch;

    private final String regex = "(A|B|RC|R|PR).*([1-9]).*\\..*([1-9]).*\\..*([0-9])";

    public Version(Stage stage, int major, int minor, int patch) {
        _Stage = stage;
        Major = major;
        Minor = minor;
        Patch = patch;
    }

    public Version(String version) {
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            // System.out.println("Full match: " + matcher.group(0));

            for (int i = 1; i <= matcher.groupCount(); i++) {
                switch (i) {
                    case 1:
                        _Stage = Stage.toStage(matcher.group(i));
                        break;
                    case 2:
                        Major = Integer.parseInt(matcher.group(i));
                        break;
                    case 3:
                        Minor = Integer.parseInt(matcher.group(i));
                        break;
                    case 4:
                        Patch = Integer.parseInt(matcher.group(i));
                        break;
                }
                // System.out.println("Group " + i + ": _" + matcher.group(i) + "_");
            }
            // System.out.println(Major);
        }
    }

    public String toString() {
        return _Stage.toString() + Major.toString() + "." + Minor.toString() + "." + Patch.toString();
    }
}