package blove.kmm.fund.support.price;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import blove.kmm.fund.support.price.SinaFundInfo.SinaFundDayPrice;
import blove.kmm.fund.support.price.SinaFundInfo.SinaFundResult;
import blove.kmm.fund.support.price.SinaFundInfo.SinaFundResultData;

/**
 * 价格相关数据支持。
 * 
 * @author blove
 */
public class PriceSupport {
	private SinaFundInfoService service = SinaFundInfoService.newInstance();

	private DateTimeFormatter resultDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * 获取每日净值。
	 * 
	 * @param fundCode
	 *            基金代码
	 * @param fromDate
	 *            起始日期
	 * @param toDate
	 *            结束日期
	 * @return 每日净值
	 */
	public NavigableMap<LocalDate, Double> getPrice(String fundCode, LocalDate fromDate, LocalDate toDate) {
		if (fromDate.plusDays(20).isAfter(toDate)) {
			return getPriceOnce(fundCode, fromDate, toDate);
		} else {
			NavigableMap<LocalDate, Double> res = new TreeMap<>();
			LocalDate midFromDate = LocalDate.from(fromDate);
			LocalDate midToDate = LocalDate.from(fromDate).minusDays(1);
			while (midToDate.isBefore(toDate)) {
				midFromDate = midToDate.plusDays(1);
				midToDate = midToDate.plusDays(20);
				if (midToDate.isAfter(toDate)) {
					midToDate = LocalDate.from(toDate);
				}
				res.putAll(getPriceOnce(fundCode, midFromDate, midToDate));
			}
			return res;
		}
	}

	/**
	 * 获取最新净值。
	 * 
	 * @param fundCode
	 *            基金代码
	 * @return singleton map，日期——净值
	 */
	public Map<LocalDate, Double> getNewPrice(String fundCode) {
		SortedMap<LocalDate, Double> recentPrices = getPriceOnce(fundCode, null, null);
		if (recentPrices.isEmpty()) {
			return Collections.singletonMap(null, 0D);
		}
		LocalDate date = recentPrices.lastKey();
		return Collections.singletonMap(date, recentPrices.get(date));
	}

	private NavigableMap<LocalDate, Double> getPriceOnce(String fundCode, LocalDate fromDate, LocalDate toDate) {
		if (fundCode == null || fundCode.length() != 6) {
			return Collections.emptyNavigableMap();
		}

		String fromDateStr = fromDate == null ? null : fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
		String toDateStr = toDate == null ? null : toDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
		SinaFundInfo fundInfo = service.getFundPrice(fundCode, fromDateStr, toDateStr);

		SinaFundResult result = fundInfo.getResult();
		if (result == null) {
			return Collections.emptyNavigableMap();
		}

		SinaFundResultData data = result.getData();
		if (data == null) {
			return Collections.emptyNavigableMap();
		}

		List<SinaFundDayPrice> dayPrices = data.getData();
		if (dayPrices == null) {
			return Collections.emptyNavigableMap();
		}

		NavigableMap<LocalDate, Double> res = new TreeMap<>();
		dayPrices.forEach(dayPrice -> {
			LocalDate date = LocalDate.parse(dayPrice.getFbrq(), resultDateFormat);
			res.put(date, dayPrice.getJjjz());
		});

		return res;
	}
}
