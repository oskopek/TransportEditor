package com.oskopek.transporteditor.persistence;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;

public class ErrorDetectionListener implements ANTLRErrorListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private boolean fail = false;
    private Exception reason = null;
    private String reasonString = null;

    public synchronized boolean isFail() {
        return fail;
    }

    public Exception getReason() {
        return reason;
    }

    public String getReasonString() {
        return reasonString;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> arg0, Object arg1, int arg2, int arg3, String arg4,
            RecognitionException arg5) {
        fail = true;
        logger.error("Syntax Error: {}", arg4);
        reason = arg5;
        reasonString = arg4;
    }

    @Override
    public void reportAmbiguity(Parser arg0, DFA arg1, int arg2, int arg3, boolean arg4, BitSet arg5,
            ATNConfigSet arg6) {
        logger.warn("Ambiguity");
    }

    @Override
    public void reportAttemptingFullContext(Parser arg0, DFA arg1, int arg2, int arg3, BitSet arg4, ATNConfigSet arg5) {
        logger.warn("Attempting full context");
    }

    @Override
    public void reportContextSensitivity(Parser arg0, DFA arg1, int arg2, int arg3, int arg4, ATNConfigSet arg5) {
        logger.warn("Context sensitivity");
    }
}

