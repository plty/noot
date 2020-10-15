package jlite.fp;

import java.util.function.Function;

public class Result<T> {
    public final T v;
    public final Error err;

    private Result(T v, Error err) {
        this.v = v;
        this.err = err;
    }

    public static <T> Result<T> of(T v) {
        return new Result<>(v, null);
    }

    public static <T> Result<T> err(Error err) {
        return new Result<>(null, err);
    }

    public boolean isValue() {
        return err == null;
    }

    public boolean isErr() {
        return err != null;
    }

    public <U> Result<U> then(Function<T, Result<U>> f) {
        return isErr() ? new Result<>(null, err) : f.apply(v);
    }
}
