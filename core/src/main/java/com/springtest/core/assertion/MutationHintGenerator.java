package com.springtest.core.assertion;

import com.springtest.core.model.MethodInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MutationHintGenerator {

    public List<String> generateHints(MethodInfo method) {
        List<String> hints = new ArrayList<>();

        hints.add("MUTATION TESTING HINTS:");
        hints.add("");
        hints.add("Your test might pass even with mutated code. Consider adding:");
        hints.add("");

        if (!method.isVoidReturn()) {
            hints.add("1. Verify exact field values, not just non-null:");
            hints.add("   - assertThat(result.getField()).isEqualTo(expectedValue);");
            hints.add("");
        }

        if (method.getParameters().size() > 0) {
            hints.add("2. Verify mock interactions:");
            hints.add("   - verify(repository).findById(userId);");
            hints.add("   - verifyNoMoreInteractions(repository);");
            hints.add("");
        }

        if (!method.isVoidReturn()) {
            hints.add("3. Add boundary assertions:");
            hints.add("   - assertThat(result.getCreatedAt()).isAfter(testStartTime);");
            hints.add("");
        }

        hints.add("4. Test inverse condition:");
        hints.add(String.format("   - Add test for %s_WhenNotFound case", method.getName()));

        return hints;
    }

    public String generateHintComment(List<String> hints) {
        StringBuilder sb = new StringBuilder();
        sb.append("/*\n");
        for (String hint : hints) {
            sb.append(" * ").append(hint).append("\n");
        }
        sb.append(" */");
        return sb.toString();
    }
}