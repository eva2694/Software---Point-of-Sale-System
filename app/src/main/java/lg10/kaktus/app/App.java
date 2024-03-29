/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package lg10.kaktus.app;

import lg10.kaktus.list.LinkedList;

import static lg10.kaktus.utilities.StringUtils.join;
import static lg10.kaktus.utilities.StringUtils.split;
import static lg10.kaktus.app.MessageUtils.getMessage;

import org.apache.commons.text.WordUtils;

public class App {
    public static void main(String[] args) {
        LinkedList tokens;
        tokens = split(getMessage());
        String result = join(tokens);
        System.out.println(WordUtils.capitalize(result));
    }
}
