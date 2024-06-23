package com.github.bernardodemarco.textretrieval.communication.scattergather;

import java.util.List;
import java.util.Collection;

public interface ScatterGather {
    <T> void scatter(Collection<T> data);
    List<String> gather();
    void stop();
}
