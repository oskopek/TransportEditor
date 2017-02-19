package com.oskopek.transporteditor.view.executables;

import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Optional;

/**
 * Represents an external executable with fillable parameters. The parameters should adhere to a {@code {x}} notation,
 * where x is a non-negative integer. These can get filled in by {@link #getParameters(Object...)} later.
 */
public interface ExecutableWithParameters {

    /**
     * Get the executable.
     *
     * @return the executable
     */
    String getExecutable();

    /**
     * Find the filesystem path of the executable on the system path.
     *
     * @return the path found, or an empty optional
     */
    Optional<Path> findExecutablePath();

    /**
     * Tries to determine if the given executable is a valid executable in the system.
     *
     * @return true iff an executable like this exists and is executable
     */
    default boolean isExecutableValid() {
        return findExecutablePath().map(Path::toFile).map(File::canExecute).orElse(false);
    }

    /**
     * Get the parameter string where the first {@code n} templates have been filled in by the objects
     * supplied as arguments. {@code n} is the number of {@code params}.
     *
     * @param params the parameters to substitute (their toString())
     * @return the (partially) substituted parameter string
     * @see MessageFormat
     */
    default String getParameters(Object... params) {
        return MessageFormat.format(getParameters(), params);
    }

    /**
     * Get the unsubstituted parameter string.
     *
     * @return the parameter string
     */
    String getParameters();

}
