/*
 * Copyright 2016 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.uu.ub.cora.httphandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;

public final class HttpHandlerImp implements HttpHandler {

	private static final int STATUS_INTERNAL_SERVER_ERROR = 500;
	private HttpURLConnection urlConnection;

	private HttpHandlerImp(HttpURLConnection httpUrlConnection) {
		this.urlConnection = httpUrlConnection;
	}

	public static HttpHandlerImp usingURLConnection(HttpURLConnection httpUrlConnection) {
		return new HttpHandlerImp(httpUrlConnection);
	}

	@Override
	public void setRequestMethod(String requestMetod) {
		try {
			tryToSetRequestMethod(requestMetod);
		} catch (Exception e) {
			throw new RuntimeException("Not an ok requestMethod: ", e);
		}
	}

	private void tryToSetRequestMethod(String requestMetod) throws ProtocolException {
		urlConnection.setRequestMethod(requestMetod);
	}

	@Override
	public String getResponseText() {
		try {
			return tryToGetResponseText();
		} catch (Exception e) {
			throw new RuntimeException("Error getting response text: ", e);
		}
	}

	private String tryToGetResponseText() throws IOException {
		StringBuilder response = new StringBuilder();
		BufferedReader in = new BufferedReader(
				new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}

	@Override
	public int getResponseCode() {
		try {
			return urlConnection.getResponseCode();
		} catch (IOException e) {
			return STATUS_INTERNAL_SERVER_ERROR;
		}
	}

}
