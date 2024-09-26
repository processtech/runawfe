import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditUserTypeListTest {

    @Test
    public void testEmptyString() {
        String string = "";
        Assertions.assertEquals(convertComponentHtml(string), string.replaceAll("\"", "'").replaceAll("\n", "").replace("[]", "{}"));
    }

    @Test
    public void testStringOne() {
        String string = "Glass at 866 \" 284 \" Not";
        Assertions.assertEquals(convertComponentHtml(string), string.replaceAll("\"", "'").replaceAll("\n", "").replace("[]", "{}"));
    }

    @Test
    public void testStringTwo() {
        String string = "\n Max tax _21 i0 tree";
        Assertions.assertEquals(convertComponentHtml(string), string.replaceAll("\"", "'").replaceAll("\n", "").replace("[]", "{}"));
    }

    @Test
    public void testStringThree() {
        String string = "Nothing any ][]{}goes [] ant";
        Assertions.assertEquals(convertComponentHtml(string), string.replaceAll("\"", "'").replaceAll("\n", "").replace("[]", "{}"));
    }

    @Test
    public void testStringFour() {
        String string = "North \n behind [] mens \" 44";
        Assertions.assertEquals(convertComponentHtml(string), string.replaceAll("\"", "'").replaceAll("\n", "").replace("[]", "{}"));
    }

    private String convertComponentHtml(String inputString) {
        HashMap<String, String> replacements = new HashMap<String, String>() {{
            put("\"", "'");
            put("\n", "");
            put("[]", "{}");
        }};
        String regexp = "\"|\n|\\[]";
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(inputString);

        while (matcher.find()) {
            matcher.appendReplacement(sb, replacements.get(matcher.group()));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
