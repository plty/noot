package jlite.parser;

import java.util.List;

public class Ast {
    public static class Arg {
        final Type type;
        final Id id;

        public Arg(Type type, Id id) {
            this.type = type;
            this.id = id;
        }
    }


    public static class Body {
        final List<Var> vars;
        final List<Stmt> stmts;

        public Body(List<Var> vars, List<Stmt> stmts) {
            this.vars = vars;
            this.stmts = stmts;
        }
    }


    public static class Cls {
        final String name;
        final List<Var> vars;
        final List<Method> methods;

        public Cls(String name, List<Var> vars, List<Method> methods) {
            this.name = name;
            this.vars = vars;
            this.methods = methods;
        }
    }

    public interface Expr {
    }

    public static class Id implements Expr {
        final String id;

        public Id(String id) {
            this.id = id;
        }
    }

    public static class Main extends Cls {
        public Main(String name, Method method) {
            super(name, List.of(), List.of(method));
        }

        final Method theMethod() {
            return this.methods.get(0);
        }
    }

    public static class Method {
        final Id id;
        final Type ret;
        final List<Arg> args;
        final Body body;

        public Method(Id id, Type ret, List<Arg> args, Body body) {
            this.id = id;
            this.ret = ret;
            this.args = args;
            this.body = body;
        }
    }


    public static class Program {
        final Main main;
        final List<Cls> classes;

        public Program(Main main, List<Cls> classes) {
            this.main = main;
            this.classes = classes;
        }
    }

    public interface Stmt {
    }

    public static class Return implements Stmt {
        final Object value;

        public Return(Object value) {
            this.value = value;
        }
    }

    public static class Type {
        final String name;

        public Type(String name) {
            this.name = name;
        }
    }

    public static class Var {
        final Type type;
        final Id id;

        public Var(Type type, Id id) {
            this.type = type;
            this.id = id;
        }
    }

    public static class Block {
        final List<Stmt> stmts;

        public Block(List<Stmt> stmts) {
            this.stmts = stmts;
        }
    }

    public static class If implements Stmt {
        final Expr cond;
        final Block cons;
        final Block alt;

        public If(Expr cond, Block cons, Block alt) {
            this.cond = cond;
            this.cons = cons;
            this.alt = alt;
        }
    }

    public static class While implements Stmt {
        final Expr cond;
        final Block block;

        public While(Expr cond, Block block) {
            this.cond = cond;
            this.block = block;
        }
    }

    public static class Call implements Stmt, Expr {
        final Expr callee;
        final List<Expr> args;

        public Call(Expr callee, List<Expr> args) {
            this.callee = callee;
            this.args = args;
        }
    }

    public static class Lit implements Stmt, Expr {
        final Object v;

        public Lit(Object v) {
            this.v = v;
        }
    }

    public static class BinOp implements Stmt, Expr {
        final String op;
        final Expr l;
        final Expr r;

        public BinOp(String op, Expr l, Expr r) {
            this.op = op;
            this.l = l;
            this.r = r;
        }
    }

    public static class UnOp implements Stmt, Expr {
        final String op;
        final Expr e;

        public UnOp(String op, Expr e) {
            this.op = op;
            this.e = e;
        }
    }

    public static class Assignment implements Stmt {
        final Expr lhs;
        final Expr rhs;

        public Assignment(Expr lhs, Expr rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }
    }

    public static class Brace implements Expr {
        final Expr e;

        public Brace(Expr e) {
            this.e = e;
        }
    }

    public static class New implements Expr {
        final String name;

        public New(String name) {
            this.name = name;
        }
    }

    public static class Member implements Expr {
        final Expr e;
        final Id id;

        public Member(Expr e, Id id) {
            this.e = e;
            this.id = id;
        }
    }
}
