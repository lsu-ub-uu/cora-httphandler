/*
 * Copyright 2016, 2018, 2019, 2023 Uppsala University Library
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

package se.uu.ub.cora.httphandler.internal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.urlconnection.HttpURLConnectionHandler;

public final class HttpHandlerImp implements HttpHandler {

	private static final int INITIAL_BUFFER_SIZE = 8192;
	private HttpURLConnection urlConnection;
	private HttpURLConnectionHandler httpURLConnectionHandler;
	private Builder builder;
	private HttpClient httpClient;
	private String requestMetod;
	private BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();
	private static final List<String> REQUEST_METHODS = List.of("GET", "HEAD", "POST", "PUT",
			"DELETE", "PATCH");

	private HttpHandlerImp(HttpURLConnection httpUrlConnection) {
		this.urlConnection = httpUrlConnection;
		httpURLConnectionHandler = new HttpURLConnectionHandler(urlConnection);
	}

	private HttpHandlerImp(Builder builder, HttpClient newHttpClient) {
		this.builder = builder;
		this.httpClient = newHttpClient;
	}

	public static HttpHandler usingBuilderAndHttpClient(Builder builder, HttpClient newHttpClient) {
		return new HttpHandlerImp(builder, newHttpClient);
	}

	public static HttpHandlerImp usingURLConnection(HttpURLConnection httpUrlConnection) {
		return new HttpHandlerImp(httpUrlConnection);
	}

	@Override
	public void setRequestMethod(String requestMetod) {
		if (!REQUEST_METHODS.contains(requestMetod)) {
			throw new RuntimeException("Not an ok requestMethod: " + requestMetod);
		}
		this.requestMetod = requestMetod;
	}

	@Override
	public void setRequestProperty(String key, String value) {
		urlConnection.setRequestProperty(key, value);
	}

	@Override
	public String getResponseText() {
		try {
			return tryToGetResponseText();
		} catch (Exception e) {
			throw new RuntimeException("Error getting response text: ", e);
		}
	}

	private String tryToGetResponseText() throws IOException, InterruptedException {
		BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();
		HttpResponse<String> response = buildRequestAndSend(bodyHandler);
		return response.body();
	}

	private <T> HttpResponse<T> buildRequestAndSend(BodyHandler<T> bodyHandler)
			throws IOException, InterruptedException {
		Builder methodBuilder = builder.method(requestMetod, bodyPublisher);
		HttpRequest httpRequest = methodBuilder.build();
		return httpClient.send(httpRequest, bodyHandler);
	}

	@Override
	public int getResponseCode() {
		try {
			return tryToGetResponseCode();
		} catch (Exception e) {
			return 500;
		}
	}

	private int tryToGetResponseCode() throws IOException, InterruptedException {
		BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();
		HttpResponse<String> response = buildRequestAndSend(bodyHandler);
		return response.statusCode();
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

	public Builder onlyForTestGetBuilder() {
		return builder;
	}

	public HttpClient onlyForTestGetHttpClient() {
		return httpClient;
	}

}
