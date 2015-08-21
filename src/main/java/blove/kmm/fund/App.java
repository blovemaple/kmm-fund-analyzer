package blove.kmm.fund;

import java.util.List;

import blove.kmm.fund.aview.DateSelector;
import blove.kmm.fund.aview.FundChart;
import blove.kmm.fund.aview.FundTreeTable;
import blove.kmm.fund.aview.PriceTable;
import blove.kmm.fund.aview.TransactionTable;
import blove.kmm.fund.biz.FundBiz;
import blove.kmm.fund.support.db.AccountDao;
import blove.kmm.fund.support.price.PriceSupport;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		List<String> params = getParameters().getRaw();
		if (params.isEmpty()) {
			System.err.println("Parameter needed.");
			System.exit(1);
			return;
		}
		FundBiz biz = new FundBiz(new PriceSupport(), new AccountDao(params.get(0)));

		BorderPane pane = new BorderPane();

		FundTreeTable fundTreeTable = new FundTreeTable(biz);
		pane.setLeft(fundTreeTable);

		BorderPane fundPane = new BorderPane();
		pane.setCenter(fundPane);

		BorderPane dateAndChartPane = new BorderPane();
		fundPane.setTop(dateAndChartPane);

		DateSelector dateSelector = new DateSelector(biz);
		dateAndChartPane.setTop(dateSelector);

		FundChart chart = new FundChart(biz);
		dateAndChartPane.setCenter(chart);

		TransactionTable transTable = new TransactionTable(biz);
		fundPane.setCenter(transTable);

		PriceTable priceTable = new PriceTable(biz);
		fundPane.setRight(priceTable);

		// 将 日期选择器 的 基金ID 绑定到 基金树表 上
		dateSelector.fundIdProperty().bind(fundTreeTable.selectedFundIdProperty());

		// 将 日期选择器 的 等待状态 绑定到 其他控件的等待状态 上
		dateSelector.waitingProperty().bind(
				fundTreeTable.waitingProperty().or(transTable.waitingProperty()).or(priceTable.waitingProperty()));

		// 将 交易表 的 基金ID、时间 绑定到 日期选择器 上
		transTable.fundIdProperty().bind(dateSelector.fundIdProperty());
		transTable.fromDateProperty().bind(dateSelector.startDateProperty());
		transTable.toDateProperty().bind(dateSelector.endDateProperty());

		// 将 价格表 的 基金ID、时间 绑定到 日期选择器 上
		priceTable.fundIdProperty().bind(dateSelector.fundIdProperty());
		priceTable.fromDateProperty().bind(dateSelector.startDateProperty());
		priceTable.toDateProperty().bind(dateSelector.endDateProperty());

		// 将 图表 的 交易记录 绑定到 交易表 上，将 价格列表 绑定到 价格表 上
		chart.transactionsProperty().bind(transTable.itemsProperty());
		chart.pricesProperty().bind(priceTable.itemsProperty());

		Scene snece = new Scene(pane);
		primaryStage.setScene(snece);
		primaryStage.show();

	}

	public static void main(String[] args) {
		launch(args);
	}
}
