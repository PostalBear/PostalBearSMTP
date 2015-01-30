package com.postalbear.smtp.command;

import lombok.NonNull;

/**
 * Base class for SMTP commands.
 *
 * @author Ian McFarland
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 * @author Grigory Fadeev
 */
public abstract class BaseCommand implements Command {

    private final String name;

    /**
     * @param name of the SMTP command
     */
    public BaseCommand(@NonNull String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }
}
