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
        final List<Method> methods;

        public Cls(String name, List<Method> methods) {
            this.name = name;
            this.methods = methods;
        }
    }

    public static class Expr {
    }

    public static class Id {
        final String name;

        public Id(String name) {
            this.name = name;
        }
    }

    public static class Main extends Cls {
        public Main(String name, Method method) {
            super(name, List.of(method));
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

    public static class Stmt {
    }

    public static class Return extends Stmt {
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
}
