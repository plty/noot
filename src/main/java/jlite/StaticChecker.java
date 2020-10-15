package jlite;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.stream.Collectors;

import jlite.fp.Result;
import jlite.parser.Ast;

public class StaticChecker {
    public static Result<Void> check(Ast.Program p) {
        final var classes = new ImmutableList.Builder<Ast.Cls>()
                .add(p.main).addAll(p.classes).build();

        final var nonUniq = classes.stream()
                .filter(a -> classes.stream()
                        .filter(b -> a.name.equals(b.name))
                        .count() != 1
                )
                .collect(Collectors.toList());

        if (nonUniq.size() != 0)
            return Result.err(new DuplicateClassError(nonUniq));

        return classes.stream()
                .map(StaticChecker::check)
                .filter(Result::isErr)
                .findAny().orElse(Result.of(null));
    }

    public static Result<Void> check(Ast.Cls c) {
        final var members = new ImmutableList.Builder<Ast.Member>()
                .addAll(c.fields)
                .addAll(c.methods)
                .build();

        final var nonUniq = members.stream()
                .filter(a -> members.stream()
                        .filter(b -> a.id.id.equals(b.id.id))
                        .count() != 1
                )
                .collect(Collectors.toList());

        if (nonUniq.size() != 0)
            return Result.err(new DuplicateMemberError(nonUniq));

        return c.methods.stream()
                .map(StaticChecker::check)
                .filter(Result::isErr)
                .findAny().orElse(Result.of(null));
    }

    public static Result<Void> check(Ast.Method m) {
        final var nonUniq = m.params.stream()
                .filter(a -> m.params.stream()
                        .filter(b -> a.id.id.equals(b.id.id))
                        .count() != 1
                )
                .collect(Collectors.toList());

        final var vars = m.body.vars;
        return nonUniq.size() != 0
                ? Result.err(new DuplicateParamError(nonUniq))
                : Result.of(null);
    }
}

class DuplicateClassError extends Error {
    public final List<Ast.Cls> nonUniq;

    DuplicateClassError(List<Ast.Cls> nonUniq) {
        super("duplicate class found: "
                + nonUniq.stream().map(c -> c.name)
                .collect(Collectors.joining(", "))
        );
        this.nonUniq = nonUniq;
    }
}

class DuplicateMemberError extends Error {
    public final List<Ast.Member> nonUniq;

    DuplicateMemberError(List<Ast.Member> nonUniq) {
        super("duplicate member found: "
                + nonUniq.stream().map(m -> m.id.id)
                .collect(Collectors.joining(", "))
        );
        this.nonUniq = nonUniq;
    }
}

class DuplicateParamError extends Error {
    public final List<Ast.Param> nonUniq;

    DuplicateParamError(List<Ast.Param> nonUniq) {
        super("duplicate param found: "
                + nonUniq.stream().map(p -> p.id.id)
                .collect(Collectors.joining(", "))
        );
        this.nonUniq = nonUniq;
    }
}
