// Copyright (c) The Diem Core Contributors
// SPDX-License-Identifier: Apache-2.0

package com.diem;

/**
 * DiemException is root exception of all exceptions created in this
 * and sub packages.
 */
public class DiemException extends Exception {
    public DiemException(Throwable e) {
        super(e);
    }

    public DiemException(String msg) {
        super(msg);
    }
}
