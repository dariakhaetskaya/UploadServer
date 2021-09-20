package ru.nsu.fit.networks.lab2.util;

import java.io.IOException;

class ProtocolViolationException extends IOException {
    ProtocolViolationException(String report){
        super(report);
    }
}
