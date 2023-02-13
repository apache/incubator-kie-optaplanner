package org.optaplanner.migration.v8;

import java.util.Arrays;
import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class SolutionManagerMethodsRecipe extends Recipe {

    private static final MatcherMeta[] MATCHER_METAS = {
            new MatcherMeta("getSummary(Object)"),
            //new MatcherMeta("explainScore(Object)"),
            //new MatcherMeta("updateScore(Object)")
    };

    @Override
    public String getDisplayName() {
        return "SolutionManager: explain(), update()";
    }

    @Override
    public String getDescription() {
        return "Use `explain()` and `update()` " +
                "   instead of `explainScore()`, `updateScore()` and `getSummary()`.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getSingleSourceApplicableTest() {
        return new JavaIsoVisitor<>() {
            @Override
            public J.CompilationUnit visitCompilationUnit(J.CompilationUnit compilationUnit,
                    ExecutionContext executionContext) {
                for (MatcherMeta matcherMeta : MATCHER_METAS) {
                    doAfterVisit(new UsesMethod<>(matcherMeta.methodMatcher));
                }
                return compilationUnit;
            }
        };
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {

            @Override
            public Expression visitExpression(Expression expression, ExecutionContext executionContext) {
                final Expression e = super.visitExpression(expression, executionContext);

                MatcherMeta matcherMeta = Arrays.stream(MATCHER_METAS).filter(m -> m.methodMatcher.matches(e))
                        .findFirst().orElse(null);
                if (matcherMeta == null) {
                    return e;
                }
                J.MethodInvocation mi = (J.MethodInvocation) e;
                Expression select = mi.getSelect();
                List<Expression> arguments = mi.getArguments();

                if (matcherMeta.methodName.contains("Summary")) {
                    String pattern = "#{any(" + matcherMeta.classFqn + ")}"
                            + ".explain(#{any()})"
                            + ".getSummary()";
                    JavaTemplate template = JavaTemplate.builder(() -> getCursor().getParentOrThrow(), pattern)
                            .javaParser(() -> buildJavaParser().build())
                            .imports("org.optaplanner.core.api.solver.SolutionUpdatePolicy")
                            .build();
                    return e.withTemplate(template, e.getCoordinates().replace(), select, arguments.get(0));
                } else {
                    throw new UnsupportedOperationException();
                    /*
                    String method = matcherMeta.methodName.replace("Score", "")
                            .replace(")", ", SolutionUpdatePolicy.SCORE_ONLY)");
                    String pattern = "#{any(" + matcherMeta.classFqn + ")}"
                            + " "
                            + method;
                    JavaTemplate template = JavaTemplate.builder(() -> getCursor().getParentOrThrow(), pattern)
                            .javaParser(() -> buildJavaParser().build())
                            .imports("org.optaplanner.core.api.solver.SolutionUpdatePolicy")
                            .build();
                    return e.withTemplate(template, e.getCoordinates().replace(), select, arguments.get(0));
                     */
                }
            }
        };
    }

    public static JavaParser.Builder buildJavaParser() {
        return JavaParser.fromJavaVersion().classpath("optaplanner-core-impl");
    }

    private static final class MatcherMeta {

        public final String classFqn;
        public final MethodMatcher methodMatcher;
        public final String methodName;

        public MatcherMeta(String method) {
            this.classFqn = "org.optaplanner.core.api.solver.SolutionManager";
            this.methodMatcher = new MethodMatcher(classFqn + " " + method);
            this.methodName = method;
        }
    }

}
