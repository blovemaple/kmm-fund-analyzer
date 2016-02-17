package blove.kmm.fund.support.valuation;

import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import retrofit.http.GET;
import retrofit.http.Query;

public interface TTValuationService {
	public static TTValuationService newInstance() {
		RestAdapter restAdapter = new RestAdapter.Builder().setLogLevel(LogLevel.BASIC)
				.setEndpoint("http://fundex2.eastmoney.com/FundWebServices").build();
		return restAdapter.create(TTValuationService.class);
	}

	@GET("/FundDataForMobile.aspx?t=gz&rg=y&&rk=3y")
	TTValuation getValuation(@Query("fc") String symbol);
}