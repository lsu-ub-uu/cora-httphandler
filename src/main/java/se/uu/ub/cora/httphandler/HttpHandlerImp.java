/*
 * Copyright 2016, 2018, 2019 Uppsala University Library
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import se.uu.ub.cora.httphandler.urlconnection.HttpURLConnectionHandler;

public final class HttpHandlerImp implements HttpHandler {

	private static final int INITIAL_BUFFER_SIZE = 8192;
	private HttpURLConnection urlConnection;
	private HttpURLConnectionHandler httpURLConnectionHandler;

	private HttpHandlerImp(HttpURLConnection httpUrlConnection) {
		this.urlConnection = httpUrlConnection;
		httpURLConnectionHandler = new HttpURLConnectionHandler(urlConnection);
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
		return httpURLConnectionHandler.getResponseText();
	}

	@Override
	public int getResponseCode() {
		return httpURLConnectionHandler.getResponseCode();
	}

	@Override
	public InputStream getResponseBinary() {
		return httpURLConnectionHandler.getResponseBinary();
	}

	@Override
	public void setOutput(String outputString) {
		try {
			tryToSetOutput(outputString);
		} catch (IOException e) {
			throw new RuntimeException("Error writing output: ", e);
		}
	}

	private void tryToSetOutput(String outputString) throws IOException {
		urlConnection.setDoOutput(true);
		try (BufferedWriter bwr = new BufferedWriter(
				new OutputStreamWriter(urlConnection.getOutputStream(), StandardCharsets.UTF_8))) {
			bwr.write(outputString);
			bwr.flush();
		}
	}

	@Override
	public void setRequestProperty(String key, String value) {
		urlConnection.setRequestProperty(key, value);
	}

	@Override
	public String getErrorText() {
		return httpURLConnectionHandler.getErrorText();
	}

	@Override
	public void setStreamOutput(InputStream stream) {
		try {
			tryToSetStreamOutput(stream);
		} catch (IOException e) {
			throw new RuntimeException("Error writing output from stream: ", e);
		}
	}

	private void tryToSetStreamOutput(InputStream stream) throws IOException {
		urlConnection.setDoOutput(true);
		urlConnection.setChunkedStreamingMode(INITIAL_BUFFER_SIZE);
		try (OutputStream wr = urlConnection.getOutputStream()) {
			stream.transferTo(wr);
		}
	}

	@Override
	public String getHeaderField(String name) {
		return urlConnection.getHeaderField(name);
	}

	@Override
	public void setBasicAuthorization(String username, String password) {
		String encoded = Base64.getEncoder()
				.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		setRequestProperty("Authorization", "Basic " + encoded);
	}

}
