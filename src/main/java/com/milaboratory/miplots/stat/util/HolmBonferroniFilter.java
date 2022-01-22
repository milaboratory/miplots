package com.milaboratory.miplots.stat.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;

public final class HolmBonferroniFilter {
    /**
     * Performs Holm-Bonferroni method.
     *
     * https://en.wikipedia.org/wiki/Holm%E2%80%93Bonferroni_method
     *
     * @param input           ALL result of multiple comparisons
     * @param pValueExtractor function to extract P-value from input objects
     * @param fwer            family-wise error rate, e.g. 0.01 (https://en.wikipedia.org/wiki/Family-wise_error_rate)
     * @param <T>             type of the object representing single comparison result
     * @return list of significant p-values, sorted from lowest to highest (FWER controlled)
     */
    public static <T> List<T> run(Collection<T> input,
                                  ToDoubleFunction<T> pValueExtractor,
                                  double fwer) {
        ArrayList<T> res = new ArrayList<>(input);
        res.sort(Comparator.comparingDouble(pValueExtractor));
        int i = 0;
        for (; i < res.size(); i++)
            if (pValueExtractor.applyAsDouble(res.get(i)) > fwer / (res.size() - i))
                break;
        return new ArrayList<>(res.subList(0, i));
    }
}
