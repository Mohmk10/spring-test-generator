package com.springtest.model;

import java.util.ArrayList;
import java.util.List;

public record ClassInfo(
        String simpleName,
        String qualifiedName,
        String packageName,
        ClassType classType,
        List<AnnotationInfo> annotations,
        List<FieldInfo> fields,
        List<MethodInfo> methods,
        List<String> dependencies,
        List<String> implementedInterfaces,
        String superClass,
        String sourcePath,
        boolean isInterface,
        boolean isAbstract
) {

    public ClassInfo {
        if (simpleName == null || simpleName.isBlank()) {
            throw new IllegalArgumentException("Class simple name cannot be null or blank");
        }
        if (qualifiedName == null || qualifiedName.isBlank()) {
            throw new IllegalArgumentException("Class qualified name cannot be null or blank");
        }
        if (classType == null) {
            classType = ClassType.OTHER;
        }
        annotations = annotations == null ? List.of() : List.copyOf(annotations);
        fields = fields == null ? List.of() : List.copyOf(fields);
        methods = methods == null ? List.of() : List.copyOf(methods);
        dependencies = dependencies == null ? List.of() : List.copyOf(dependencies);
        implementedInterfaces = implementedInterfaces == null ? List.of() : List.copyOf(implementedInterfaces);
    }

    public boolean isSpringComponent() {
        return annotations.stream().anyMatch(AnnotationInfo::isSpringComponent);
    }

    public boolean isRestController() {
        return annotations.stream()
                .anyMatch(a -> "org.springframework.web.bind.annotation.RestController".equals(a.qualifiedName()));
    }

    public List<MethodInfo> getWebMappingMethods() {
        return methods.stream()
                .filter(MethodInfo::isWebMapping)
                .toList();
    }

    public List<FieldInfo> getInjectedFields() {
        return fields.stream()
                .filter(FieldInfo::injected)
                .toList();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String simpleName;
        private String qualifiedName;
        private String packageName;
        private ClassType classType = ClassType.OTHER;
        private List<AnnotationInfo> annotations = new ArrayList<>();
        private List<FieldInfo> fields = new ArrayList<>();
        private List<MethodInfo> methods = new ArrayList<>();
        private List<String> dependencies = new ArrayList<>();
        private List<String> implementedInterfaces = new ArrayList<>();
        private String superClass;
        private String sourcePath;
        private boolean isInterface;
        private boolean isAbstract;

        public Builder simpleName(String simpleName) {
            this.simpleName = simpleName;
            return this;
        }

        public Builder qualifiedName(String qualifiedName) {
            this.qualifiedName = qualifiedName;
            return this;
        }

        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder classType(ClassType classType) {
            this.classType = classType;
            return this;
        }

        public Builder annotations(List<AnnotationInfo> annotations) {
            this.annotations = new ArrayList<>(annotations);
            return this;
        }

        public Builder addAnnotation(AnnotationInfo annotation) {
            this.annotations.add(annotation);
            return this;
        }

        public Builder fields(List<FieldInfo> fields) {
            this.fields = new ArrayList<>(fields);
            return this;
        }

        public Builder addField(FieldInfo field) {
            this.fields.add(field);
            return this;
        }

        public Builder methods(List<MethodInfo> methods) {
            this.methods = new ArrayList<>(methods);
            return this;
        }

        public Builder addMethod(MethodInfo method) {
            this.methods.add(method);
            return this;
        }

        public Builder dependencies(List<String> dependencies) {
            this.dependencies = new ArrayList<>(dependencies);
            return this;
        }

        public Builder addDependency(String dependency) {
            this.dependencies.add(dependency);
            return this;
        }

        public Builder implementedInterfaces(List<String> implementedInterfaces) {
            this.implementedInterfaces = new ArrayList<>(implementedInterfaces);
            return this;
        }

        public Builder addImplementedInterface(String implementedInterface) {
            this.implementedInterfaces.add(implementedInterface);
            return this;
        }

        public Builder superClass(String superClass) {
            this.superClass = superClass;
            return this;
        }

        public Builder sourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
            return this;
        }

        public Builder isInterface(boolean isInterface) {
            this.isInterface = isInterface;
            return this;
        }

        public Builder isAbstract(boolean isAbstract) {
            this.isAbstract = isAbstract;
            return this;
        }

        public ClassInfo build() {
            return new ClassInfo(
                    simpleName,
                    qualifiedName,
                    packageName,
                    classType,
                    annotations,
                    fields,
                    methods,
                    dependencies,
                    implementedInterfaces,
                    superClass,
                    sourcePath,
                    isInterface,
                    isAbstract
            );
        }
    }
}
