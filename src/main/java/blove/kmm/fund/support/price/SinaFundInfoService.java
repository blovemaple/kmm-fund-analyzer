package blove.kmm.fund.support.price;

import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import retrofit.http.GET;
import retrofit.http.Query;

public interface SinaFundInfoService {
	public static SinaFundInfoService newInstance() {
		RestAdapter restAdapter = new RestAdapter.Builder().setLogLevel(LogLevel.BASIC)
				.setEndpoint("http://stock.finance.sina.com.cn/fundInfo/api/openapi.php").build();
		return restAdapter.create(SinaFundInfoService.class);
	}

	@GET("/CaihuiFundInfoService.getNav")
	SinaFundInfo getFundPrice(@Query("symbol") String symbol, @Query("datefrom") String dateFrom,
			@Query("dateto") String dateTo);
}
