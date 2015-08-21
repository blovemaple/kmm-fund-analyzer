package blove.kmm.fund.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;

public class MathUtils {
	public static DoubleBinding round(ObservableValue<Number> ori, ObservableValue<Number> precision) {
		if (ori == null) {
			return null;
		}
		return Bindings.createDoubleBinding(() -> round(ori.getValue().doubleValue(), precision.getValue().intValue()),
				ori, precision);
	}

	public static DoubleBinding round(ObservableValue<Number> ori, int precision) {
		if (ori == null) {
			return null;
		}
		return Bindings.createDoubleBinding(() -> round(ori.getValue().doubleValue(), precision), ori);
	}

	public static StringBinding roundToStr(ObservableValue<Number> ori, int precision) {
		if (ori == null) {
			return null;
		}
		return Bindings.createStringBinding(() -> {
			if (ori.getValue() == null || !Double.isFinite(ori.getValue().doubleValue())) {
				return "-";
			}
			return roundToStr(ori.getValue().doubleValue(), precision);
		} , ori);
	}

	public static Double round(Double ori, int precision) {
		return Double.valueOf(roundToStr(ori, precision));
	}

	public static String roundToStr(Double ori, int precision) {
		if (ori == null) {
			return null;
		}
		return String.format("%." + precision + "f", ori);
	}
}
