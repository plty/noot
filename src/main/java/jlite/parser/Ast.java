package jlite.parser;

import java.util.List;

public class Ast {
    public interface Node {
    }

    public interface Expr extends Node {
    }


    public interface Stmt extends Node {
    }

    public static class Param implements Node {
        public final Type type;
        public final Id id;

        public Param(Type type, Id id) {
            this.type = type;
            this.id = id;
        }
    }

    public static class Body implements Node {
        public final List<Var> vars;
        public final List<Stmt> stmts;

        public Body(List<Var> vars, List<Stmt> stmts) {
            this.vars = vars;
            this.stmts = stmts;
        }
    }

    public static class Cls implements Node {
        public final String name;
        public final List<Field> fields;
        public final List<Method> methods;

        public Cls(String name, List<Field> fields, List<Method> methods) {
            this.name = name;
            this.fields = fields;
            this.methods = methods;
        }
    }

    public static class Id implements Node, Expr {
        public final String id;

        public Id(String id) {
            this.id = id;
        }
    }

    public static class Main extends Cls {
        public Main(String name, Method method) {
            super(name, List.of(), List.of(method));
        }

        public final Method theMethod() {
            return this.methods.get(0);
        }
    }

    public static class Member {
        public final Id id;

        Member(Id id) {
            this.id = id;
        }
    }

    public static class Field extends Member implements Node {
        public final Type type;

        public Field(Type type, Id id) {
            super(id);
            this.type = type;
        }
    }

    public static class Method extends Member implements Node {
        public final Type ret;
        public final List<Param> params;
        public final Body body;

        public Method(Id id, Type ret, List<Param> params, Body body) {
            super(id);
            this.ret = ret;
            this.params = params;
            this.body = body;
        }
    }

    public static class Program implements Node {
        public final Main main;
        public final List<Cls> classes;

        public Program(Main main, List<Cls> classes) {
            this.main = main;
            this.classes = classes;
        }
    }

    public static class Return implements Node, Stmt {
        public final Expr expr;

        public Return(Expr expr) {
            this.expr = expr;
        }
    }

    public static class Type implements Node {
        public final String name;

        public Type(String name) {
            this.name = name;
        }
    }

    public static class Var implements Node {
        public final Type type;
        public final Id id;

        public Var(Type type, Id id) {
            this.type = type;
            this.id = id;
        }
    }

    public static class Block implements Node {
        public final List<Stmt> stmts;

        public Block(List<Stmt> stmts) {
            this.stmts = stmts;
        }
    }

    public static class If implements Node, Stmt {
        public final Expr cond;
        public final Block cons;
        public final Block alt;

        public If(Expr cond, Block cons, Block alt) {
            this.cond = cond;
            this.cons = cons;
            this.alt = alt;
        }
    }

    public static class While implements Node, Stmt {
        public final Expr cond;
        public final Block block;

        public While(Expr cond, Block block) {
            this.cond = cond;
            this.block = block;
        }
    }

    public static class Call implements Node, Stmt, Expr {
        public final Expr callee;
        public final List<Expr> args;

        public Call(Expr callee, List<Expr> args) {
            this.callee = callee;
            this.args = args;
        }
    }

    public static class Syscall implements Node, Stmt {
        public final String name;
        public final List<Expr> args;

        public Syscall(String name, List<Expr> args) {
            this.name = name;
            this.args = args;
        }
    }

    public static class Lit implements Node, Stmt, Expr {
        public final Object v;

        public Lit(Object v) {
            this.v = v;
        }
    }

    public static class BinOp implements Stmt, Expr {
        public final String op;
        public final Expr l;
        public final Expr r;

        public BinOp(String op, Expr l, Expr r) {
            this.op = op;
            this.l = l;
            this.r = r;
        }
    }

    public static class UnOp implements Node, Stmt, Expr {
        public final String op;
        public final Expr e;

        public UnOp(String op, Expr e) {
            this.op = op;
            this.e = e;
        }
    }

    public static class Assignment implements Node, Stmt {
        public final Expr lhs;
        public final Expr rhs;

        public Assignment(Expr lhs, Expr rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }
    }

    public static class FieldAssignment implements Node, Stmt {
        public final Expr lhs;
        public final Id id;
        public final Expr rhs;

        public FieldAssignment(Expr v, Id id, Expr rhs) {
            this.lhs = v;
            this.id = id;
            this.rhs = rhs;
        }
    }

    public static class New implements Node, Expr {
        public final String name;

        public New(String name) {
            this.name = name;
        }
    }

    public static class Access implements Node, Expr {
        public final Expr e;
        public final Id id;

        public Access(Expr e, Id id) {
            this.e = e;
            this.id = id;
        }
    }
}
