package jlite;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import jlite.parser.Ast;

import java.util.stream.Collectors;

public class TypeIndexer {
    public static Env index(Ast.Program program) {
        Env e = new Env();

        e.types.putAll(ImmutableMap.of(
                "Void", new Env.ClassType("Void"),
                "Int", new Env.ClassType("Int"),
                "Bool", new Env.ClassType("Bool"),
                "String", new Env.ClassType("String")
        ));

        final var classes = new ImmutableList.Builder<Ast.Cls>()
                .add(program.main)
                .addAll(program.classes)
                .build();
        classes.forEach(c -> e.types.put(c.name, new Env.ClassType(c.name)));
        classes.forEach(c -> c.fields
                .forEach(f -> ((Env.ClassType) e.types.get(c.name))
                        .fields.put(f.id.id, (Env.ClassType) e.types.get(f.type.name))
                )
        );
        classes.forEach(c ->
                c.methods.forEach(m -> {
                    final var name =
                            String.format("%s::%s", c.name, m.id.id);
                    final var method = new Env.MethodType(
                            name,
                            (Env.ClassType) e.types.get(m.ret.name),
                            m.params.stream()
                                    .map(p -> p.type.name)
                                    .map(e.types::get)
                                    .map(t -> (Env.ClassType) t)
                                    .collect(Collectors.toList())
                    );

                    e.types.put(name, method);
                    ((Env.ClassType) e.types.get(c.name)).methods.put(m.id.id, method);
                })
        );
        return e;
    }
}
