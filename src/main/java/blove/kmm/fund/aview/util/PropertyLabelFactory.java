package blove.kmm.fund.aview.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.sun.javafx.property.PropertyReference;
import com.sun.javafx.scene.control.Logging;

import blove.kmm.fund.util.MathUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import sun.util.logging.PlatformLogger;
import sun.util.logging.PlatformLogger.Level;

/**
 * 参考{@link javafx.scene.control.cell.PropertyValueFactory}实现。
 * 
 * @author blove
 *
 * @param <S>
 * @param <Object>
 */
@SuppressWarnings("restriction")
public class PropertyLabelFactory<S> implements Callback<S, ObservableValue<Label>> {

	private final String property;

	private Class<?> columnClass;
	private String previousProperty;
	private PropertyReference<Object> propertyRef;

	public PropertyLabelFactory(String property) {
		this.property = property;
	}

	public final String getProperty() {
		return property;
	}

	@Override
	public ObservableValue<Label> call(S bean) {
		if (getProperty() == null || getProperty().isEmpty() || bean == null)
			return null;

		try {
			// we attempt to cache the property reference here, as otherwise
			// performance suffers when working in large data models. For
			// a bit of reference, refer to RT-13937.
			if (columnClass == null || previousProperty == null || !columnClass.equals(bean.getClass())
					|| !previousProperty.equals(getProperty())) {

				// create a new PropertyReference
				this.columnClass = bean.getClass();
				this.previousProperty = getProperty();
				this.propertyRef = new PropertyReference<Object>(bean.getClass(), getProperty());
			}

			Label label = new Label();
			ObjectProperty<Object> rawValue = new SimpleObjectProperty<Object>();

			Field field = columnClass.getDeclaredField(getProperty());
			boolean textBound = false;
			for (Annotation anno : field.getDeclaredAnnotations()) {
				if (anno instanceof Precised) {
					DoubleBinding doubleValue = doubleFromObject(rawValue);
					int precision = ((Precised) anno).value();
					label.textProperty().bind(MathUtils.roundToStr(doubleValue, precision));

					textBound = true;
					break;
				} else if (anno instanceof Percentagized) {
					DoubleBinding doubleValue = doubleFromObject(rawValue);
					int precision = ((Percentagized) anno).value();
					label.textProperty().bind(MathUtils.roundToStr(doubleValue.multiply(100), precision).concat("%"));

					if (((Percentagized) anno).colored()) {
						label.textFillProperty().bind(Bindings.createObjectBinding(() -> {
							return doubleValue.get() > 0 ? Color.RED
									: doubleValue.get() < 0 ? Color.GREEN : Color.BLACK;
						} , doubleValue));
					}

					textBound = true;
					break;
				}
			}

			if (!textBound) {
				label.textProperty().bind(rawValue.asString());
			}

			if (propertyRef.hasProperty()) {
				rawValue.bind(propertyRef.getProperty(bean));
			} else {
				rawValue.set(propertyRef.get(bean));
			}

			return new ReadOnlyObjectWrapper<Label>(label);
		} catch (IllegalStateException | NoSuchFieldException | SecurityException e) {
			// log the warning and move on
			final PlatformLogger logger = Logging.getControlsLogger();
			if (logger.isLoggable(Level.WARNING)) {
				logger.finest("Can not retrieve property '" + getProperty() + "' in PropertyValueFactory: " + this
						+ " with provided class type: " + bean.getClass(), e);
			}
		}

		return null;
	}

	private DoubleBinding doubleFromObject(ObjectProperty<Object> rawValue) {
		return Bindings.createDoubleBinding(() -> {
			Object o = rawValue.get();
			if (o == null) {
				return Double.NaN;
			}
			return Double.valueOf(String.valueOf(rawValue.get()));
		} , rawValue);
	}
	
	public static <S> Callback<javafx.scene.control.TreeTableColumn.CellDataFeatures<S, Label>, ObservableValue<Label>> forTreeTable(
			String property) {
		PropertyLabelFactory<S> nodeFactory = new PropertyLabelFactory<>(property);
		return features -> nodeFactory.call(features.getValue().getValue());
	}

	public static <S> Callback<javafx.scene.control.TableColumn.CellDataFeatures<S, Label>, ObservableValue<Label>> forTable(
			String property) {
		PropertyLabelFactory<S> nodeFactory = new PropertyLabelFactory<>(property);
		return features -> nodeFactory.call(features.getValue());
	}

}
