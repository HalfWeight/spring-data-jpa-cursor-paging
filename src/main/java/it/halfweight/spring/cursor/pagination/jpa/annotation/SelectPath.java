package it.halfweight.spring.cursor.pagination.jpa.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation that can be used to specify the field to use for a specific parameters.
 * You can use a dot (.) to navigate over relations.
 * <p>
 * Example:
 * <pre>
 *     public static class TestEntityProjectionWithSelectPath {
 *         Long id;
 *         String stringField;
 *         String childStringField;
 *
 *         public TestEntityProjectionWithSelectPath(
 *                 Long id,
 *                 &#64;SelectPath(&#34;stringField&#34;) String stringField,
 *                 &#64;SelectPath(&#34;childEntity.stringField&#34;) String childStringField) {
 *
 *             this.id = id;
 *             this.stringField = stringField;
 *             this.childStringField = childStringField;
 *         }
 *     }
 * </pre>
 * This will define {@code childStringField} path as {@code root.get("childEntity").get("stringField")}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SelectPath {
    String value();
}
