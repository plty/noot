package jlite.type_check;

import com.google.common.collect.ImmutableMap;
import jlite.fp.MultiError;
import jlite.fp.Result;
import jlite.parser.Ast;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

class TypeHint {

    public static Result<Map<Ast.Node, String>> join(Result<Map<Ast.Node, String>> a, Result<Map<Ast.Node, String>> b) {
        if (a.isErr() || b.isErr())
            return a.isErr() ? a : b;
        return Result.of(new ImmutableMap.Builder<Ast.Node, String>().putAll(a.v).putAll(b.v).build());
    }

    public static Collector<
            Result<Map<Ast.Node, String>>,
            Result<Map<Ast.Node, String>>,
            Result<Map<Ast.Node, String>>
            >
    collector() {
        return Collector.of(() -> Result.of(ImmutableMap.of()), TypeHint::join, TypeHint::join);
    }
}
