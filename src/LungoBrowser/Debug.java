package LungoBrowser;

import java.awt.Dimension;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

public class Debug {

    public enum Level {
        IMPORTANT,
        LOG,
        WARNING,
        ERROR
    }

    public static class Event {

    }

    public static class Flag {
        public String command;
        public String description;
        public int argAmount;

        public Flag(String command, String description, int argAmount) {
            this.command = command;
            this.description = description;
            this.argAmount = argAmount;
        }
    }

    public static class cmdHook {
        private static ArrayList<Pair<String, Function<Event, Object>>> functions;

        public static void addCommandListener(String name, Function<Event, Object> hook) {
            functions.add(new Pair<>(name, hook));
        }

        public static void runCommandListeners(String name, Event evt) {
            functions.forEach(elm -> {
                if (elm.Value1 == name) {
                    elm.Value2.apply(evt);
                }
            });
        }
    }

    private static ArrayList<ArrayList<String>> AllowedFlags;
    private static ArrayList<String> FlagsDescription;
    private static ArrayList<Integer> flagValuesAmount;
    private static HashMap<String, ArrayList<String>> FlagValues;

    private static ArrayList<String> Flags;

    public static void createFlags(ArrayList<Flag> flags) {
        AllowedFlags = new ArrayList<>();
        FlagsDescription = new ArrayList<>();
        flagValuesAmount = new ArrayList<>();
        FlagValues = new HashMap<>();

        AllowedFlags.add(new ArrayList<>(Arrays.asList("help", "h")));
        AllowedFlags.add(new ArrayList<>(Arrays.asList("debug", "d")));
        AllowedFlags.add(new ArrayList<>(Arrays.asList("log", "w")));

        FlagsDescription.add("will show help!");
        FlagsDescription.add("will enable debugging");
        FlagsDescription.add("will write debug to file");

        flagValuesAmount.add(0);
        flagValuesAmount.add(0);
        flagValuesAmount.add(0);

        for (var flag : flags) {
            var flagSplit = new ArrayList<>(Arrays.asList(flag.command.split("\\|")));
            AllowedFlags.add(flagSplit);
            FlagsDescription.add(flag.description);

            flagValuesAmount.add(flag.argAmount);
        }
    }

    public static void setFlags(String[] flags) {
        Flags = new ArrayList<>();
        for (int x = 0; x < flags.length; x++) {
            var val = flags[x];
            if (val.startsWith("-")) {
                var saf = false;
                for (int i = 0; i < AllowedFlags.size(); i++) {
                    var flagStrs = AllowedFlags.get(i);
                    for (var strsItem : flagStrs) {
                        if (strsItem.equals(val.substring(1))) {
                            Flags.add(flagStrs.get(0));
                            // Log(flagValuesAmount.get(i));
                            var vals = new ArrayList<String>();
                            for (var E = 0; E < flagValuesAmount.get(i); E++) {
                                // Log(flags[x + E + 1]);
                                vals.add(flags[x + E + 1]);
                            }
                            if (flagValuesAmount.get(i) > 0)
                                FlagValues.put(flagStrs.get(0), vals);
                            x += flagValuesAmount.get(i);
                            saf = true;
                            break;
                        }
                    }
                }
                if (!saf) {
                    Log(Level.IMPORTANT, String.format("the flag \"%s\" is not valid!!", val));
                    System.exit(1);
                }
            } else {
                Log(Level.IMPORTANT, String.format("the flag \"%s\" is not valid!!", val));
                System.exit(1);
            }
        }
        // Debug.Log(FlagValues);

        if (ifFlag("help")) {
            for (int i = 0; i < AllowedFlags.size(); i++) {
                var allowedFlag = AllowedFlags.get(i);
                var description = FlagsDescription.get(i);
                Log(Level.IMPORTANT, Array2SpacedString(allowedFlag) + "  # "
                        + description);
            }
        }

        if (ifFlag("log")) {
            try {
                App.writeToFile("log.txt", "");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String Array2SpacedString(ArrayList<String> arr) {
        var e = "";
        for (int i = 0; i < arr.size(); i++) {
            var elm = arr.get(i);
            e += "-" + elm;
            if (i != arr.size() - 1)
                e += ", ";
        }
        return e;
    }

    private static boolean isFlagSingle(String val) {
        if (Flags == null)
            return false;
        for (var THING : Flags) {
            var THINGS = val.split("\\|");
            for (var x : THINGS) {
                if (THING.equals(x)) {
                    return true;
                }
            }
        }
        // for (var flagItem : Flags) {
        // if (flagItem.equals(val)) {
        // return true;
        // }
        // }
        return false;
    }

    public static boolean ifFlag(String... flags) {
        for (String checkFlag : flags) {
            if (!isFlagSingle(checkFlag))
                return false;
        }
        return true;
    }

    public static ArrayList<String> getFlagValue(String flag) {
        if (FlagValues.containsKey(flag))
            return FlagValues.get(flag);
        return new ArrayList<String>();
    }

    private static String CostumeToString(Object val) {
        if (val instanceof Dimension) {
            return "Dimension[w:" + ((Dimension) val).getWidth() + " h:" + ((Dimension) val).getHeight() + "]";
        }
        return null;
    }

    private static String DisplayArray(Object[] array) {
        String output = "[";
        for (int i = 0; i < array.length; i++) {
            String temp = CostumeToString(array[i]);
            if (temp != null) {
                output += temp;
                continue;
            }
            if (array[i].getClass().isArray()) {
                output += DisplayArray((Object[]) array[i]);
                continue;
            }
            output += array[i].toString();
            if (i < array.length - 1)
                output += ", ";
        }
        return output + "]";
    }

    public static void Log(Object... msgs) {
        LogGetFunc();
        DoLog(Level.LOG, msgs);
    }

    public static void Log(Level level, Object... msgs) {
        LogGetFunc();
        DoLog(level, msgs);
    }

    public static void Error(Object... msgs) {
        LogGetFunc();
        DoLog(Level.ERROR, msgs);
    }

    public static void Warn(Object... msgs) {
        LogGetFunc();
        DoLog(Level.WARNING, msgs);
    }

    private static String CalledFrom = "";

    private static void LogGetFunc() {
        // try {
        // throw new RuntimeException();
        // } catch (RuntimeException e) {
        // var listing = new ArrayList<>(Arrays.asList(e.getStackTrace()));
        // listing.remove(0);
        // for (StackTraceElement item : listing) {
        // CalledFrom = item.getClassName() + "." + item.getMethodName() + "(" +
        // item.getFileName() + ":"
        // + item.getLineNumber() + "): ";
        // }
        // }
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            var listing = new ArrayList<>(Arrays.asList(e.getStackTrace()));
            // for (var item : listing) {
            // System.out.println(item.getFileName());
            // }
            listing.remove(0);
            listing.remove(0);
            var ThePerpetrator = listing.remove(0);
            CalledFrom = ThePerpetrator.getFileName() + ":"
                    + ThePerpetrator.getLineNumber() + ": ";
        }
    }

    private static void DoLog(Level level, Object... msgs) {
        if (Debug.Flags == null)
            return;

        String output = "";
        for (int i = 0; i < msgs.length; i++) {
            String temp = CostumeToString(msgs[i]);
            if (temp != null) {
                output += temp;
                continue;
            }
            if (msgs[i] == null) {
                output += "null";
                continue;
            } else if (msgs[i].getClass().isArray()) {
                output += DisplayArray((Object[]) msgs[i]);
                continue;
            }
            output += msgs[i].toString();
            if (i < msgs.length - 1)
                output += "  ";
        }
        output = CalledFrom + output;

        if (ifFlag("debug") || ifFlag("help")) {
            if (level == Level.LOG)
                System.out.println("\033[0;34m" + output + "\033[0;39m");
            if (level == Level.IMPORTANT)
                System.out.println("\033[1;34m" + output + "\033[0;39m");
            if (level == Level.ERROR)
                System.out.println("\033[1;91m" + output + "\033[0;39m");
            if (level == Level.WARNING)
                System.out.println("\033[0;33m" + output + "\033[0;39m");
        }

        if (ifFlag("log")) {
            try {
                App.appendToFile("log.txt", output + "\n");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void Write(byte[] data, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(data);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
