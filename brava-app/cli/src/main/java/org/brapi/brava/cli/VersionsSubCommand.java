package org.brapi.brava.cli;

import org.brapi.brava.core.config.CollectionFactory;
import picocli.CommandLine;

/**
 * A sub command that returns the BrAPI versions that are available for Endpoints to be validated against
 */
@CommandLine.Command(
  name = "versions", mixinStandardHelpOptions = true
)
public class VersionsSubCommand implements Runnable {
    @Override
    public void run() {
        new CollectionFactory().getCollectionNames().forEach(version -> System.out.println(version));
    }
}