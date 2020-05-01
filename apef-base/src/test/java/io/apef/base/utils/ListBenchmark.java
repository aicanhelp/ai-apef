package io.apef.base.utils;


import io.apef.testing.benchmark.AbstractMicrobenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Threads(1)
@Warmup(iterations = 0)
@Measurement(iterations = 5)
public class ListBenchmark extends AbstractMicrobenchmark {
    List<Integer> arrayList1 = new ArrayList<>(1000);
    List<Integer> arrayList2 = new ArrayList<>();
    LinkedList<Integer> linkedList = new LinkedList<>();


    //about 339584
    @Benchmark
    public void benchmarkArrays() {
        Integer[] arrays = new Integer[1000];
        for (int i = 0; i < 1000; i++) {
            arrays[i] = i;
        }
    }

    @Benchmark
    public void benchmarkArrayList1() {
        for (int i = 0; i < 1000; i++) {
            arrayList1.add(i);
        }
    }

    @Benchmark
    public void benchmarkArrayList2() {
        for (int i = 0; i < 1000; i++) {
            arrayList2.add(i);
        }
    }

    @Benchmark
    public void benchmarkLinkedList() {
        for (int i = 0; i < 1000; i++) {
            linkedList.add(i);
        }
    }
}
