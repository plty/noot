package jlite.fp;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MultiError extends Error {
    public final List<Error> errs;

    private MultiError(List<Error> errs) {
        super(errs.stream().map(Error::toString).collect(Collectors.joining(", ")));
        this.errs = errs;
    }

    public static MultiError join(List<Error> errs) {
        if (errs.size() == 0) return null;
        return new MultiError(errs);
    }

    public static <T> Collector<Result<T>, ImmutableList.Builder<Error>, Result<Void>> collector() {
        return Collector.of(
                ImmutableList.Builder<Error>::new,
                (a, b) -> {
                    if (b.isErr()) a.add(b.err);
                },
                (a, b) -> a.addAll(b.build()),
                (b) -> Result.err(MultiError.join(b.build()))
        );
    }
}
