package org.optaplanner.migration.v8;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class SolutionManagerMethodsRecipeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new SolutionManagerMethodsRecipe())
                .parser(SolutionManagerMethodsRecipe.buildJavaParser());
    }

    @Test
    void summary() {
        runTest("String summary = solutionManager.getSummary(solution);",
                "String summary = solutionManager.explain(solution, SolutionUpdatePolicy.SCORE_ONLY).getSummary();");
    }

    private void runTest(String before, String after) {
        rewriteRun(java(wrap(before), wrap(after)));
    }

    private static String wrap(String content) {
        return "import org.optaplanner.core.api.solver.SolutionManager;\n" +
                "import org.optaplanner.core.api.solver.SolverFactory;\n" +
                "\n" +
                "class Test {\n" +
                "    public static void main(String[] args) {\n" +
                "       SolverFactory solverFactory = SolverFactory.create(null);\n" +
                "       SolutionManager solutionManager = SolutionManager.create(solverFactory);\n" +
                "       Object solution = null;\n" +
                "       " + content.trim() + "\n" +
                "    }" +
                "}\n";
    }

}
