
package com.goodtech.tq.httpClient;

import com.goodtech.tq.models.WeatherModel;

public interface ApiCallback {
	
	void onResponse(boolean success, WeatherModel weather, ErrorCode errCode);

}
