/*
 ***********************************************************************
 * Copyright (c) 2000-2006 The Apache Software Foundation.             *
 * All rights reserved.                                                *
 * ------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License"); you *
 * may not use this file except in compliance with the License. You    *
 * may obtain a copy of the License at:                                *
 *                                                                     *
 *     http://www.apache.org/licenses/LICENSE-2.0                      *
 *                                                                     *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS,   *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
 * implied.  See the License for the specific language governing       *
 * permissions and limitations under the License.                      *
 ***********************************************************************
 */
package com.postalbear.smtp.io;

import java.io.IOException;
import java.io.InputStream;

import static com.postalbear.smtp.SmtpConstants.*;

/**
 * Removes the dot-stuffing happening during the SMTP message transfer
 * <p>
 * Copy pasted from SubethaSMTP
 */
public class DotUnstuffingInputStream extends InputStream {

    private final InputStream in;
    /**
     * An array to hold the last two bytes read off the stream.
     * This allows the stream to detect CRLF sequences even when they occur across read boundaries.
     */
    protected int[] last = {-1, -1};

    public DotUnstuffingInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * Read through the stream, checking for CRLF
     *
     * @return the byte read from the stream
     * @throws java.io.IOException
     */
    @Override
    public int read() throws IOException {
        int b = in.read();
        if (DOT == b && CR == last[0] && LF == last[1]) {
            //skip this '.' because it should have been stuffed
            b = in.read();
        }
        last[0] = last[1];
        last[1] = b;
        return b;
    }
}
