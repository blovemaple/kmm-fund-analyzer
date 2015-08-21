package blove.kmm.fund.aview.util;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Percentagized {
	/**
	 * 精度小数位数。
	 */
	int value();

	/**
	 * 是否用红绿颜色。
	 */
	boolean colored() default true;
}
