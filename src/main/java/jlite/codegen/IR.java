package jlite.codegen;

import jlite.parser.Ast;

import java.util.List;

public class IR {
    public static abstract class Stmt {
        final String _t = this.getClass().getName();
    }

    public static class Program {
        public final List<Data> decls;
        public final List<Method> methods;

        public Program(List<Data> decls, List<Method> methods) {
            this.decls = decls;
            this.methods = methods;
        }
    }

    public static class Data {
        public final String name;
        public final List<Field> fields;

        public Data(String name, List<Field> fields) {
            this.name = name;
            this.fields = fields;
        }
    }

    public static class Field {
        public final String type;
        public final String id;

        public Field(String type, String id) {
            this.type = type;
            this.id = id;
        }
    }

    public static class Arg {
        public final String type;
        public final String id;

        public Arg(String type, String id) {
            this.type = type;
            this.id = id;
        }
    }

    public static class Method {
        public final String name;
        public final String ret;
        public final List<Param> params;
        public final List<Stmt> body;

        public Method(String name, String ret, List<Param> params, List<Stmt> body) {
            this.name = name;
            this.ret = ret;
            this.params = params;
            this.body = body;
        }
    }

    public static class Param {
        public final String type;
        public final String id;

        public Param(String type, String id) {
            this.type = type;
            this.id = id;
        }
    }

    public static class Var extends Stmt {
        public final String type;
        public final String name;

        public Var(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    public static class New extends Stmt {
        public final String lhs;
        public final String type;

        public New(String lhs, String type) {
            this.lhs = lhs;
            this.type = type;
        }
    }

    public static class Assignment extends Stmt {
        public final String lhs;
        public final String rhs;

        public Assignment(String lhs, String rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }
    }

    public static class FieldAssignment extends Stmt {
        public final String lhs;
        public final String id;
        public final String rhs;

        public FieldAssignment(String lhs, String id, String rhs) {
            this.lhs = lhs;
            this.id = id;
            this.rhs = rhs;
        }
    }

    public static class Lit extends Stmt {
        public final String lhs;
        public final Object rhs;

        public Lit(String lhs, Object rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }
    }

    public static class BinOp extends Stmt {
        public final String lhs;
        public final String op;
        public final String a;
        public final String b;

        public BinOp(String lhs, String op, String a, String b) {
            this.lhs = lhs;
            this.op = op;
            this.a = a;
            this.b = b;
        }
    }

    public static class UnOp extends Stmt {
        public final String lhs;
        public final String op;
        public final String rhs;

        public UnOp(String lhs, String op, String rhs) {
            this.lhs = lhs;
            this.op = op;
            this.rhs = rhs;
        }
    }

    public static class Access extends Stmt {
        public final String lhs;
        public final String rhs;
        public final String member;

        public Access(String lhs, String rhs, String member) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.member = member;
        }
    }

    public static class Return extends Stmt {
        public final String id;

        public Return(String id) {
            this.id = id;
        }
    }

    public static class Label extends Stmt {
        public final String name;

        public Label(String name) {
            this.name = name;
        }
    }

    public static class CondGoto extends Stmt {
        public final String cond;
        public final String target;

        public CondGoto(String cond, String target) {
            this.cond = cond;
            this.target = target;
        }
    }

    public static class Goto extends Stmt {
        public final String label;

        public Goto(String label) {
            this.label = label;
        }
    }
}
