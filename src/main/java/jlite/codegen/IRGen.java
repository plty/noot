package jlite.codegen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import jlite.parser.Ast;
import jlite.type_check.Checker;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IRGen {
    static int lid = 0;

    public static String randstr() {
        return "__" + String.valueOf(++lid);
//        return "__" + new Random().ints(96 + 1, 96 + 26 + 1)
//                .limit(12)
//                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
//                .toString();
    }

    public static IR.Program generateProgram(Ast.Program p) {
        final var hints = Checker.check(p);
        if (hints.isErr()) {
            System.out.println(hints.err.toString());
            return null;
        }
        HintedEnv e = new HintedEnv(hints.v, ImmutableSet.of());

        final var classes = new ImmutableList.Builder<Ast.Cls>().add(p.main).addAll(p.classes).build();
        return new IR.Program(
                classes.stream()
                        .map(IRGen::genData)
                        .collect(Collectors.toList()),
                classes.stream()
                        .flatMap(c -> c.methods.stream().map(m -> genMethod(e, c, m)))
                        .collect(Collectors.toList())
        );
    }

    public static IR.Data genData(Ast.Cls c) {
        return new IR.Data(c.name, c.fields.stream()
                .map(f -> new IR.Field(f.type.name, f.id.id))
                .collect(Collectors.toList())
        );
    }

    public static IR.Method genMethod(HintedEnv e, Ast.Cls c, Ast.Method m) {
        e = e.update(m.body.vars.stream().map(var -> var.id.id).collect(Collectors.toSet()));
        e = e.update(m.params.stream().map(p -> p.id.id).collect(Collectors.toSet()));
        e = e.update(Set.of("this"));
        final var env = e;
        final var gens = m.body.stmts.stream()
                .map(s -> genStmt(env, s))
                .collect(Collectors.toList());
        final var stmts = new ImmutableList.Builder<IR.Stmt>()
                .addAll(gens.stream().flatMap(g -> g.vars.stream()).collect(Collectors.toList()))
                .addAll(gens.stream().flatMap(g -> g.stmts.stream()).collect(Collectors.toList()))
                .build();
        return new IR.Method(
                c.name + "::" + m.id.id,
                m.ret.name,
                new ImmutableList.Builder<IR.Param>()
                        .add(new IR.Param(c.name, "this"))
                        .addAll(m.params.stream()
                                .map(p -> new IR.Param(p.type.name, p.id.id))
                                .collect(Collectors.toList())
                        )
                        .build(),
                stmts
        );
    }

    public static Gcode genBlock(HintedEnv e, Ast.Block b) {
        final var gcodes = b.stmts.stream()
                .map(s -> genStmt(e, s))
                .collect(Collectors.toList());
        return new Gcode(
                null,
                gcodes.stream()
                        .flatMap(g -> g.vars.stream())
                        .collect(Collectors.toList()),
                gcodes.stream()
                        .flatMap(g -> g.stmts.stream())
                        .collect(Collectors.toList())

        );
    }

    static Gcode genStmt(HintedEnv e, Ast.Stmt s) {
        if (s instanceof Ast.Expr)
            return genExpr(e, (Ast.Expr) s);
        if (s instanceof Ast.While)
            return genWhile(e, (Ast.While) s);
        if (s instanceof Ast.If)
            return genIf(e, (Ast.If) s);
        if (s instanceof Ast.Assignment)
            return genAssignment(e, (Ast.Assignment) s);
        if (s instanceof Ast.FieldAssignment)
            return genFieldAssignment(e, (Ast.FieldAssignment) s);
        if (s instanceof Ast.Return)
            return genReturn(e, (Ast.Return) s);
        if (s instanceof Ast.Syscall)
            return genSyscall(e, (Ast.Syscall) s);
        return null;
    }

    public static Gcode genSyscall(HintedEnv e, Ast.Syscall s) {
        final var args = s.args.stream().map(arg -> genExpr(e, arg)).collect(Collectors.toList());
        return new Gcode(
                null,
                args.stream()
                        .flatMap(arg -> arg.vars.stream())
                        .collect(Collectors.toList()),
                new ImmutableList.Builder<IR.Stmt>()
                        .addAll(
                                args.stream()
                                        .flatMap(arg -> arg.stmts.stream())
                                        .collect(Collectors.toList())
                        )
                        .add(new IR.Syscall(s.name, args.stream().map(arg -> arg.ret).collect(Collectors.toList())))
                        .build()
        );
    }

    public static Gcode genWhile(HintedEnv e, Ast.While w) {
        final var notCond = randstr();
        final var startLabel = randstr();
        final var endLabel = randstr();

        final var cond = genExpr(e, w.cond);
        final var block = genBlock(e, w.block);

        return new Gcode(
                null,
                new ImmutableList.Builder<IR.Var>()
                        .addAll(cond.vars)
                        .add(new IR.Var("Bool", notCond))
                        .addAll(block.vars)
                        .build()
                ,
                new ImmutableList.Builder<IR.Stmt>()
                        .add(new IR.Label(startLabel))
                        .addAll(cond.stmts)
                        .add(new IR.UnOp(notCond, "!", cond.ret))
                        .add(new IR.CondGoto(notCond, endLabel))
                        .addAll(block.stmts)
                        .add(new IR.Goto(startLabel))
                        .add(new IR.Label(endLabel))
                        .build()
        );
    }

    public static Gcode genIf(HintedEnv e, Ast.If c) {
        final var notCond = randstr();
        final var altLabel = randstr();
        final var endLabel = randstr();

        final var cond = genExpr(e, c.cond);
        final var cons = genBlock(e, c.cons);
        final var alt = genBlock(e, c.alt);

        return new Gcode(
                null,
                new ImmutableList.Builder<IR.Var>()
                        .addAll(cond.vars)
                        .add(new IR.Var("Bool", notCond))
                        .addAll(cons.vars)
                        .addAll(alt.vars)
                        .build(),
                new ImmutableList.Builder<IR.Stmt>()
                        .addAll(cond.stmts)
                        .add(new IR.UnOp(notCond, "!", cond.ret))
                        .add(new IR.CondGoto(notCond, altLabel))
                        .addAll(cons.stmts)
                        .add(new IR.Goto(endLabel))
                        .add(new IR.Label(altLabel))
                        .addAll(alt.stmts)
                        .add(new IR.Label(endLabel))
                        .build()
        );
    }

    public static Gcode genAssignment(HintedEnv e, Ast.Assignment s) {
        final var rhs = genExpr(e, s.rhs);
        return new Gcode(
                null,
                new ImmutableList.Builder<IR.Var>()
                        .addAll(rhs.vars)
                        .build(),
                new ImmutableList.Builder<IR.Stmt>()
                        .addAll(rhs.stmts)
                        .add(new IR.FieldAssignment("this", ((Ast.Id) s.lhs).id, rhs.ret))
                        .build()
        );
    }

    public static Gcode genFieldAssignment(HintedEnv e, Ast.FieldAssignment s) {
        final var lhs = genExpr(e, s.lhs);
        final var rhs = genExpr(e, s.rhs);
        return new Gcode(
                null,
                new ImmutableList.Builder<IR.Var>()
                        .addAll(lhs.vars)
                        .addAll(rhs.vars)
                        .build(),
                new ImmutableList.Builder<IR.Stmt>()
                        .addAll(lhs.stmts)
                        .addAll(rhs.stmts)
                        .add(new IR.FieldAssignment(lhs.ret, s.id.id, rhs.ret))
                        .build()
        );
    }

    public static Gcode genReturn(HintedEnv e, Ast.Return s) {
        if (s.expr == null) {
            return new Gcode(
                    null,
                    ImmutableList.of(),
                    ImmutableList.of(new IR.Return(null))
            );
        }

        final var gen = genExpr(e, s.expr);
        return new Gcode(
                gen.ret,
                gen.vars,
                new ImmutableList.Builder<IR.Stmt>()
                        .addAll(gen.stmts)
                        .add(new IR.Return(gen.ret))
                        .build()
        );
    }

    static Gcode genExpr(HintedEnv e, Ast.Expr ex) {
        if (ex instanceof Ast.Lit)
            return genLit(e, (Ast.Lit) ex);
        if (ex instanceof Ast.BinOp)
            return genBinOp(e, (Ast.BinOp) ex);
        if (ex instanceof Ast.UnOp)
            return genUnOp(e, (Ast.UnOp) ex);
        if (ex instanceof Ast.New)
            return genNew(e, (Ast.New) ex);
        if (ex instanceof Ast.Access)
            return genAccess(e, (Ast.Access) ex);
        if (ex instanceof Ast.Call)
            return genCall(e, (Ast.Call) ex);
        if (ex instanceof Ast.Id)
            return genId(e, (Ast.Id) ex);
        return null;
    }

    public static Gcode genId(HintedEnv e, Ast.Id ex) {
        if (e.locals.contains(ex.id))
            return new Gcode(ex.id, ImmutableList.of(), ImmutableList.of());

        final var res = randstr();
        return new Gcode(
                res,
                ImmutableList.of(new IR.Var(e.typeof(ex), res)),
                ImmutableList.of(new IR.Access(res, "this", ex.id))
        );
    }

    public static Gcode genLit(HintedEnv e, Ast.Lit ex) {
        final var res = randstr();
        return new Gcode(
                res,
                List.of(new IR.Var(e.typeof(ex), res)),
                List.of(new IR.Lit(res, ex.v))
        );
    }

    public static Gcode genBinOp(HintedEnv e, Ast.BinOp ex) {
        final var res = randstr();
        final var lhs = genExpr(e, ex.l);
        final var rhs = genExpr(e, ex.r);
        return new Gcode(
                res,
                new ImmutableList.Builder<IR.Var>()
                        .addAll(lhs.vars)
                        .addAll(rhs.vars)
                        .add(new IR.Var(e.typeof(ex), res))
                        .build()
                ,
                new ImmutableList.Builder<IR.Stmt>()
                        .addAll(lhs.stmts)
                        .addAll(rhs.stmts)
                        .add(new IR.BinOp(res, ex.op, lhs.ret, rhs.ret))
                        .build()
        );
    }

    public static Gcode genUnOp(HintedEnv e, Ast.UnOp ex) {
        final var res = randstr();
        final var rhs = genExpr(e, ex.e);
        return new Gcode(
                res,
                List.of(new IR.Var(e.typeof(ex), res)),
                new ImmutableList.Builder<IR.Stmt>()
                        .addAll(rhs.stmts)
                        .add(new IR.UnOp(res, ex.op, rhs.ret))
                        .build()
        );
    }

    public static Gcode genNew(HintedEnv e, Ast.New ex) {
        final var res = randstr();
        return new Gcode(
                res,
                List.of(new IR.Var(ex.name, res)),
                List.of(new IR.New(res, ex.name))
        );
    }

    public static Gcode genAccess(HintedEnv e, Ast.Access ex) {
        final var res = randstr();
        final var rhs = genExpr(e, ex.e);
        return new Gcode(
                res,
                new ImmutableList.Builder<IR.Var>()
                        .addAll(rhs.vars)
                        .add(new IR.Var(e.typeof(ex), res))
                        .build(),
                new ImmutableList.Builder<IR.Stmt>()
                        .addAll(rhs.stmts)
                        .add(new IR.Access(res, rhs.ret, ex.id.id))
                        .build()
        );
    }

    public static Gcode genCall(HintedEnv e, Ast.Call ex) {
        final var res = randstr();
        final var args = ex.args.stream().map(arg -> genExpr(e, arg)).collect(Collectors.toList());

        if (ex.callee instanceof Ast.Id) {
            return new Gcode(
                    res,
                    new ImmutableList.Builder<IR.Var>()
                            .addAll(args.stream()
                                    .flatMap(arg -> arg.vars.stream())
                                    .collect(Collectors.toList())
                            )
                            .add(new IR.Var(e.typeof(ex), res))
                            .build(),
                    new ImmutableList.Builder<IR.Stmt>()
                            .addAll(
                                    args.stream()
                                            .flatMap(arg -> arg.stmts.stream())
                                            .collect(Collectors.toList())
                            )
                            .add(new IR.Call(
                                    res,
                                    e.typeof(ex.callee),
                                    new ImmutableList.Builder<String>()
                                            .add("this")
                                            .addAll(args.stream().map(arg -> arg.ret).collect(Collectors.toList()))
                                            .build()
                            ))
                            .build()
            );
        }

        final var callee = ((Ast.Access) ex.callee);
        final var lhs = genExpr(e, callee.e);
        return new Gcode(
                res,
                new ImmutableList.Builder<IR.Var>()
                        .addAll(lhs.vars)
                        .addAll(args.stream()
                                .flatMap(arg -> arg.vars.stream())
                                .collect(Collectors.toList())
                        )
                        .add(new IR.Var(e.typeof(ex), res))
                        .build(),
                new ImmutableList.Builder<IR.Stmt>()
                        .addAll(lhs.stmts)
                        .addAll(
                                args.stream()
                                        .flatMap(arg -> arg.stmts.stream())
                                        .collect(Collectors.toList())
                        )
                        .add(new IR.Call(
                                res,
                                e.typeof(ex.callee),
                                new ImmutableList.Builder<String>()
                                        .add(lhs.ret)
                                        .addAll(args.stream().map(arg -> arg.ret).collect(Collectors.toList()))
                                        .build()
                        ))
                        .build()
        );

    }

    public static class Gcode {
        public final String ret;
        public final List<IR.Var> vars;
        public final List<IR.Stmt> stmts;

        public Gcode(String ret, List<IR.Var> vars, List<IR.Stmt> stmts) {
            this.ret = ret;
            this.vars = vars;
            this.stmts = stmts;
        }
    }

    public static class HintedEnv {
        public final Map<Ast.Node, String> hints;
        public final Set<String> locals;

        public HintedEnv(Map<Ast.Node, String> hints, Set<String> locals) {
            this.hints = hints;
            this.locals = locals;
        }

        public String typeof(Ast.Node n) {
            return hints.get(n);
        }

        public HintedEnv update(Set<String> locals) {
            Set<String> l = new HashSet<>(this.locals);
            l.addAll(locals);
            return new HintedEnv(hints, l);
        }
    }
}
