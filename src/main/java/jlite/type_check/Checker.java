package jlite.type_check;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import jlite.Env;
import jlite.TypeIndexer;
import jlite.fp.Result;
import jlite.parser.Ast;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Checker {
    public static Result<Map<Ast.Node, String>> check(Ast.Program p) {
        Env e = TypeIndexer.index(p);
        return onProgram(e, p);
    }

    static Result<Map<Ast.Node, String>> onProgram(Env e, Ast.Program p) {
        final var classes = new ImmutableList.Builder<Ast.Cls>()
                .add(p.main)
                .addAll(p.classes)
                .build();

        return classes.stream()
                .map(c -> onClass(e, c))
                .reduce(Result.of(ImmutableMap.of()), TypeHint::join);
    }

    static Result<Map<Ast.Node, String>> onClass(Env e, Ast.Cls c) {
        final var env = e.update(
                ((Env.ClassType) e.types.get(c.name)).fields,
                ((Env.ClassType) e.types.get(c.name)).methods
        );

        return c.methods.stream()
                .map(m -> onMethod(env, c, m))
                .reduce(Result.of(ImmutableMap.of()), TypeHint::join);
    }

    static Result<Map<Ast.Node, String>> onMethod(Env e, Ast.Cls c, Ast.Method m) {
        final var env = e.update(
                new ImmutableMap.Builder<String, Env.ClassType>()
                        .put(Map.entry("this", (Env.ClassType) e.types.get(c.name)))
                        .putAll(m.params.stream()
                                .map(p -> Map.entry(p.id.id, (Env.ClassType) e.types.get(p.type.name)))
                                .collect(Collectors.toList())
                        )
                        .build(),
                ImmutableMap.of()
        );
        return onBody(env, m.body);
    }

    static Result<Map<Ast.Node, String>> onBody(Env e, Ast.Body b) {
        final var env = e.update(
                new ImmutableMap.Builder<String, Env.ClassType>()
                        .putAll(b.vars.stream()
                                .map(v -> Map.entry(v.id.id, (Env.ClassType) e.types.get(v.type.name)))
                                .collect(Collectors.toList())
                        )
                        .build(),
                ImmutableMap.of()
        );

        return b.stmts.stream()
                .map(s -> onStmt(env, s))
                .reduce(Result.of(ImmutableMap.of()), TypeHint::join);
    }

    static Result<Map<Ast.Node, String>> onId(Env e, Ast.Id id) {
        final var c = e.vars.get(id.id);
        final var m = e.methods.get(id.id);
        return Result.of(ImmutableMap.of(id, (c != null ? c : m).name));
    }

    static Result<Map<Ast.Node, String>> onStmt(Env e, Ast.Stmt s) {
        if (s instanceof Ast.Expr)
            return onExpr(e, (Ast.Expr) s);
        if (s instanceof Ast.While)
            return onWhile(e, (Ast.While) s);
        if (s instanceof Ast.If)
            return onIf(e, (Ast.If) s);
        if (s instanceof Ast.Assignment)
            return onAssignment(e, (Ast.Assignment) s);
        if (s instanceof Ast.FieldAssignment)
            return onFieldAssignment(e, (Ast.FieldAssignment) s);
        if (s instanceof Ast.Return)
            return onReturn(e, (Ast.Return) s);
        if (s instanceof Ast.Syscall)
            return onSyscall(e, (Ast.Syscall) s);
        return Result.err(new Error(String.format("Can't resolve %s", s)));
    }

    static Result<Map<Ast.Node, String>> onExpr(Env e, Ast.Expr ex) {
        if (ex instanceof Ast.Lit)
            return onLit(e, (Ast.Lit) ex);
        if (ex instanceof Ast.BinOp)
            return onBinOp(e, (Ast.BinOp) ex);
        if (ex instanceof Ast.UnOp)
            return onUnOp(e, (Ast.UnOp) ex);
        if (ex instanceof Ast.New)
            return onNew(e, (Ast.New) ex);
        if (ex instanceof Ast.Access)
            return onAccess(e, (Ast.Access) ex);
        if (ex instanceof Ast.Call)
            return onCall(e, (Ast.Call) ex);
        if (ex instanceof Ast.Id)
            return onId(e, (Ast.Id) ex);
        return Result.err(new Error(String.format("Can't resolve %s", ex)));
    }

    static Result<Map<Ast.Node, String>> onSyscall(Env e, Ast.Syscall c) {
        if (c.name.equals("readln") && c.args.size() != 1)
            return Result.err(new Error("readln only accept one parameter"));
        if (c.name.equals("println") && c.args.size() != 1)
            return Result.err(new Error("readln only accept one parameter"));

        return c.args.stream()
                .map(p -> onExpr(e, p))
                .reduce(Result.of(ImmutableMap.of()), TypeHint::join);
    }

    static Result<Map<Ast.Node, String>> onCall(Env e, Ast.Call c) {
        return onExpr(e, c.callee).then(r -> {
            final var callEnv = onExpr(e, c.callee);
            final var argsEnv = c.args.stream()
                    .map(p -> onExpr(e, p))
                    .reduce(Result.of(ImmutableMap.of()), TypeHint::join);
            return TypeHint.join(callEnv, argsEnv).then(t -> {
                final var m = (Env.MethodType) e.types.get(t.get(c.callee));
                final var expects = m.params.stream()
                        .map(p -> p.name)
                        .collect(Collectors.joining(", "));
                final var args = c.args.stream()
                        .map(a -> e.types.get(t.get(a)).name)
                        .collect(Collectors.joining(", "));
                if (!expects.equals(args))
                    return Result.err(new Error(String.format("expects (%s) got (%s) as args", expects, args)));
                return TypeHint.join(Result.of(t), Result.of(ImmutableMap.of(c, m.ret.name)));
            });
        });
    }

    static Result<Map<Ast.Node, String>> onLit(Env e, Ast.Lit l) {
        if (l.v instanceof Integer)
            return Result.of(ImmutableMap.of(l, "Int"));
        if (l.v instanceof Boolean)
            return Result.of(ImmutableMap.of(l, "Bool"));
        if (l.v instanceof String)
            return Result.of(ImmutableMap.of(l, "String"));
        return Result.err(new Error("should be impossible to happen"));
    }

    static Result<Map<Ast.Node, String>> onBinOp(Env e, Ast.BinOp o) {
        final var joined = TypeHint.join(onExpr(e, o.l), onExpr(e, o.r));
        if (joined.isErr())
            return joined;
        final var ts = joined.v;

        if (List.of("+").contains(o.op)) {
            if (!ts.get(o.l).equals(ts.get(o.r)) || !List.of("Int", "String").contains(ts.get(o.l)))
                return Result.err(new Error("type mismatch on BinOp"));
            return TypeHint.join(joined, Result.of(ImmutableMap.of(o, ts.get(o.l))));
        }
        if (List.of("-", "*", "/").contains(o.op)) {
            if (!ts.get(o.l).equals("Int") || !ts.get(o.r).equals("Int"))
                return Result.err(new Error("type mismatch on BinOp"));
            return TypeHint.join(joined, Result.of(ImmutableMap.of(o, ts.get(o.l))));
        }
        if (List.of(">", ">=", "<", "<=").contains(o.op)) {
            if (!ts.get(o.l).equals("Int") || !ts.get(o.r).equals("Int"))
                return Result.err(new Error("type mismatch on BinOp"));
            return TypeHint.join(joined, Result.of(ImmutableMap.of(o, "Bool")));
        }
        if (List.of("==", "!=").contains(o.op)) {
            if (!ts.get(o.l).equals(ts.get(o.r)) || !List.of("Int", "Bool", "String").contains(ts.get(o.l)))
                return Result.err(new Error("type mismatch on BinOp"));
            return TypeHint.join(joined, Result.of(ImmutableMap.of(o, "Bool")));
        }
        if (List.of("||", "&&").contains(o.op)) {
            if (!ts.get(o.l).equals("Bool") || !ts.get(o.r).equals("Bool"))
                return Result.err(new Error("type mismatch on BinOp"));
            return TypeHint.join(joined, Result.of(ImmutableMap.of(o, "Bool")));
        }
        return Result.err(new Error("should be impossible to happen"));
    }

    static Result<Map<Ast.Node, String>> onUnOp(Env e, Ast.UnOp o) {
        return onExpr(e, o.e).then(ts -> {
            if (ts.get(o.e).equals("Int") && o.op.equals("-"))
                return TypeHint.join(Result.of(ts), Result.of(ImmutableMap.of(o, "Int")));
            if (ts.get(o.e).equals("Bool") && o.op.equals("!"))
                return TypeHint.join(Result.of(ts), Result.of(ImmutableMap.of(o, "Bool")));
            return Result.err(new Error());
        });
    }

    static Result<Map<Ast.Node, String>> onNew(Env e, Ast.New o) {
        if (!e.types.containsKey(o.name))
            return Result.err(new Error("the class name is not recognized"));
        return Result.of(ImmutableMap.of(o, o.name));
    }

    static Result<Map<Ast.Node, String>> onAccess(Env e, Ast.Access a) {
        return onExpr(e, a.e).then(r -> {
            final var c = ((Env.ClassType) e.types.get(r.get(a.e)))
                    .fields.get(a.id.id);
            final var m = ((Env.ClassType) e.types.get(r.get(a.e)))
                    .methods.get(a.id.id);
            final var t = c != null ? c : m;
            return TypeHint.join(
                    Result.of(r),
                    Result.of(ImmutableMap.of(a, t.name))
            );
        });
    }

    static Result<Map<Ast.Node, String>> onWhile(Env e, Ast.While w) {
        final var condResult = onExpr(e, w.cond);
        if (condResult.isErr())
            return condResult;

        return w.block.stmts.stream()
                .map(s -> onStmt(e, s))
                .reduce(Result.of(ImmutableMap.of()), TypeHint::join);
    }

    static Result<Map<Ast.Node, String>> onIf(Env e, Ast.If b) {
        final var condResult = onExpr(e, b.cond);
        if (condResult.isErr())
            return condResult;

        return onExpr(e, b.cond)
                .then(r -> TypeHint.join(
                        Result.of(r),
                        b.cons.stmts.stream()
                                .map(s -> onStmt(e, s))
                                .reduce(Result.of(ImmutableMap.of()), TypeHint::join)
                ))
                .then(r -> TypeHint.join(
                        Result.of(r),
                        b.alt.stmts.stream()
                                .map(s -> onStmt(e, s))
                                .reduce(Result.of(ImmutableMap.of()), TypeHint::join)

                ));
    }

    static Result<Map<Ast.Node, String>> onAssignment(Env e, Ast.Assignment b) {
        return TypeHint.join(onExpr(e, b.lhs), onExpr(e, b.rhs))
                .then(ts -> {
                    if (!ts.get(b.lhs).equals(ts.get(b.rhs)))
                        return Result.err(new Error("type mismatch on assignment"));
                    return Result.of(ts);
                });
    }

    static Result<Map<Ast.Node, String>> onFieldAssignment(Env e, Ast.FieldAssignment b) {
        return TypeHint.join(onExpr(e, b.lhs), onExpr(e, b.rhs))
                .then(ts -> {
                    final var lhs = ((Env.ClassType) e.types.get(ts.get(b.lhs))).fields.get(b.id.id).name;
                    final var rhs = ts.get(b.rhs);
                    if (!lhs.equals(rhs))
                        return Result.err(new Error("type mismatch on field assignment"));
                    return TypeHint.join(Result.of(ts), Result.of(ImmutableMap.of(b, lhs)));
                });
    }

    static Result<Map<Ast.Node, String>> onReturn(Env e, Ast.Return r) {
        if (r.expr == null) {
            return Result.of(ImmutableMap.of());
        }
        return onExpr(e, r.expr).then(ts -> TypeHint.join(
                Result.of(ts),
                Result.of(ImmutableMap.of(r, ts.get(r.expr))))
        );
    }
}