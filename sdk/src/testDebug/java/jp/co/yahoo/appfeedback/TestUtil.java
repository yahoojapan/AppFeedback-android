package jp.co.yahoo.appfeedback;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by taicsuzu on 2017/05/01.
 */

public class TestUtil {
    /**
     * static finalなフィールドを書き換える
     * @param field
     * @param newValue
     * @throws Exception
     */
    public static void setFinalStatic(Field field, Object newValue) throws Exception {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.setAccessible(true);
        field.set(null, newValue);
    }
}
