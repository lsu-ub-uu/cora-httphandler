/*
 * Copyright 2016, 2018 Uppsala University Library
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
package se.uu.ub.cora.httphandler.urlconnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class HttpURLConnectionHandler {
	private static final int STATUS_INTERNAL_SERVER_ERROR = 500;

	private HttpURLConnection urlConnection;

	public HttpURLConnectionHandler(HttpURLConnection urlConnection) {
		this.urlConnection = urlConnection;
	}

	public String getResponseText() {
		try {
			return tryToGetResponseText();
		} catch (Exception e) {
			throw new RuntimeException("Error getting response text: ", e);
		}
	}

	private String tryToGetResponseText() throws IOException {
		InputStream inputStream = urlConnection.getInputStream();
		return getTextFromInputStream(inputStream);
	}

	private String getTextFromInputStream(InputStream inputStream) throws IOException {
		StringBuilder text = new StringBuilder();
		BufferedReader in = new BufferedReader(
				new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			text.append(inputLine);
		}
		in.close();
		return text.toString();
	}

	public InputStream getResponseBinary() {
		try {
			return urlConnection.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException("Error reading binary from response.", e);
		}
	}

	public String getErrorText() {
		try {
			return tryToGetErrorText();
		} catch (Exception e) {
			throw new RuntimeException("Error getting response text: ", e);
		}
	}

	private String tryToGetErrorText() throws IOException {
		InputStream inputStream = urlConnection.getErrorStream();
		return getTextFromInputStream(inputStream);
	}

	public int getResponseCode() {
		try {
			return urlConnection.getResponseCode();
		} catch (IOException e) {
			return STATUS_INTERNAL_SERVER_ERROR;
		}
	}

}
