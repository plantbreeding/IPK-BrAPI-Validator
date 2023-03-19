package org.brapi.brava.cli;

import picocli.CommandLine;

@CommandLine.Command(
        name = "brava",
        subcommands = {
                VersionsSubCommand.class,
                ValidateSubCommand.class
        },
        version = "1.0",
        mixinStandardHelpOptions = true
)
public class BRAVACommand {
    public static void main(String[] args) {
        new CommandLine(new BRAVACommand()).execute(args) ;
    }
}