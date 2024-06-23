package com.github.bernardodemarco.textretrieval.communication.scattergather;

import java.util.Collection;
import java.util.List;

public interface ScatterGather {
    <T> void scatter(Collection<T> data);
    List<String> gather();
    void stop();
}
