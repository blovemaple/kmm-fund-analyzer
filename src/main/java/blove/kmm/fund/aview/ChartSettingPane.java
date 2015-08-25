package blove.kmm.fund.aview;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.FlowPane;

public class ChartSettingPane extends FlowPane {
	private BooleanProperty yAxisFrom0 = new SimpleBooleanProperty();

	public ChartSettingPane() {
		super(10, 10);
		setAlignment(Pos.CENTER_RIGHT);

		// 设置纵坐标是否从0开始的复选框
		CheckBox yAxisFrom0Box = new CheckBox("纵坐标从0开始");
		getChildren().add(yAxisFrom0Box);
		yAxisFrom0Box.setSelected(true);
		yAxisFrom0.bind(yAxisFrom0Box.selectedProperty());
	}

	public final BooleanProperty yAxisFrom0Property() {
		return this.yAxisFrom0;
	}

	public final boolean isYAxisFrom0() {
		return this.yAxisFrom0Property().get();
	}

	public final void setYAxisFrom0(final boolean yAxisFrom0) {
		this.yAxisFrom0Property().set(yAxisFrom0);
	}

}
