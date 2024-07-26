import server.Server;

import java.io.IOException;

import client.Client;

public class Run {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0) {
            exitWithInvalidMenu("Please specify the mode", 1);
        }

        if (args.length > 1) {
            exitWithInvalidMenu("Too many arguments", 2);
        }

        var mode = args[0];
        var valid = validateMode(mode);
        if (!valid) {
            exitWithInvalidMenu("Invalid mode", 3);
        }

        if (mode.equals("server")) {
            Server.main(args);
        } else if (mode.equals("client")) {
            Client.main(args);
        } else {
            System.out.println("Invalid mode");
        }
    }

    private static void printUsage() {
        System.out.println("""
                Modes:
                - server
                - client""");
        System.out.println("Usage: java Run <mode>");
    }

    private static boolean validateMode(final String mode) {
        switch (mode) {
            case "server":
            case "client":
            case "s":
            case "c":
                break;
            default:
                return false;
        }

        return true;
    }

    private static void exitWithInvalidMenu(String title, int code) {
        System.out.println(title);
        printUsage();
        System.exit(code);
    }
}