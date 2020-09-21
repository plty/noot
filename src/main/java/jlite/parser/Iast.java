package jlite.parser;

public class Iast {
    public static class TypeId {
        final Ast.Type type;
        final Ast.Id id;

        public TypeId(Ast.Type type, Ast.Id id) {
            this.type = type;
            this.id = id;
        }
    }
}
