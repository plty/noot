package jlite;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Env {
    public final Map<String, Type> types;
    public final ImmutableMap<String, ClassType> vars;
    public final ImmutableMap<String, MethodType> methods;

    public Env() {
        this.types = new HashMap<>();
        this.vars = ImmutableMap.of();
        this.methods = ImmutableMap.of();
    }

    private Env(Map<String, Type> types, ImmutableMap<String, ClassType> vars, ImmutableMap<String, MethodType> methods) {
        this.types = types;
        this.vars = vars;
        this.methods = methods;
    }

    public Env update(Map<String, ClassType> vars, Map<String, MethodType> methods) {
        final var updVars = new ImmutableList.Builder<Map.Entry<String, ClassType>>()
                .addAll(this.vars.entrySet().asList())
                .addAll(new ArrayList<>(vars.entrySet()))
                .build()
                .stream()
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));
        final var updMethods = new ImmutableList.Builder<Map.Entry<String, MethodType>>()
                .addAll(this.methods.entrySet().asList())
                .addAll(new ArrayList<>(methods.entrySet()))
                .build()
                .stream()
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));
        return new Env(types, updVars, updMethods);
    }

    public static abstract class Type {
        public final String name;

        protected Type(String name) {
            this.name = name;
        }

        public abstract boolean isClass();

        public abstract boolean isMethod();
    }

    public static class ClassType extends Type {
        public final Map<String, ClassType> fields;
        public final Map<String, MethodType> methods;

        public ClassType(String name) {
            super(name);
            this.fields = new HashMap<>();
            this.methods = new HashMap<>();
        }

        @Override
        public boolean isClass() {
            return true;
        }

        @Override
        public boolean isMethod() {
            return false;
        }
    }

    public static class MethodType extends Type {
        public final ClassType ret;
        public final List<ClassType> params;

        public MethodType(String name, ClassType ret, List<ClassType> params) {
            super(name);
            this.ret = ret;
            this.params = params;
        }

        @Override
        public boolean isClass() {
            return false;
        }

        @Override
        public boolean isMethod() {
            return true;
        }
    }
}
