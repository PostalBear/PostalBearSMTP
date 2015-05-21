package com.postalbear.smtp.grizzly.codec;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;

/**
 * Concrete implementations which are intended to process data from client should implement this interface.
 *
 * @author Grigory Fadeev.
 */
public interface Decoder<T> {

    /**
     * Check whether input contains enough data for processing.
     *
     * @param storage to store/retrieve processing information
     * @param input   to check
     * @return true if enough data available
     */
    boolean hasEnoughData(Connection storage, Buffer input);

    /**
     * Get complete data chunk from input for further processing.
     *
     * @param storage to store/retrieve processing information
     * @param input   to get data from
     * @return complete chunk of data for further processing
     * @throws IllegalStateException if not enough data available
     */
    T getData(Connection storage, Buffer input);

    /**
     * Release resources acquired by decoder.
     *
     * @param storage
     */
    void release(Connection storage);
}
