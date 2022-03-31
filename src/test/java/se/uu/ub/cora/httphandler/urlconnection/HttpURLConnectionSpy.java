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

package se.uu.ub.cora.httphandler.urlconnection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpURLConnectionSpy extends HttpURLConnection {

	public String requestMethod;

	private int responseCode = 200;

	private String responseText;

	private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(999);

	public List<Boolean> dooutput = new ArrayList<>();

	public Map<String, String> requestProperties = new HashMap<>();

	private String errorText;

	private Map<String, String> headerFields = new HashMap<>();

	public List<Boolean> usecaches = new ArrayList<>();

	public List<Boolean> doinput = new ArrayList<>();
	public int setRequestPropertiesCalledNoTimes = 0;
	public int getOutputStreamCalledNoTimes = 0;

	public ByteArrayInputStream returnedInputStream;

	public String methodCalled;

	public HttpURLConnectionSpy(URL url) {
		super(url);
	}

	@Override
	public void connect() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean usingProxy() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setRequestMethod(String method) throws ProtocolException {
		requestMethod = method;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	@Override
	public int getResponseCode() throws IOException {
		methodCalled = "getResponseCode";
		return responseCode;
	}

	public void setResponseText(String responseText) {
		this.responseText = responseText;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		methodCalled = "getInputStream";
		returnedInputStream = new ByteArrayInputStream(
				responseText.getBytes(StandardCharsets.UTF_8));
		return returnedInputStream;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		getOutputStreamCalledNoTimes++;
		return byteArrayOutputStream;

	}

	public String getOutputStreamAsString() {
		return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
	}

	@Override
	public void setDoOutput(boolean dooutput) {
		this.dooutput.add(dooutput);
	}

	@Override
	public void setRequestProperty(String key, String value) {
		requestProperties.put(key, value);
		setRequestPropertiesCalledNoTimes++;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}

	@Override
	public InputStream getErrorStream() {
		methodCalled = "getErrorStream";
		return new ByteArrayInputStream(errorText.getBytes(StandardCharsets.UTF_8));
	}

	public void setHeaderField(String key, String value) {
		headerFields.put(key, value);
	}

	@Override
	public String getHeaderField(String name) {
		return headerFields.get(name);
	}

	// this.urlConnection.setUseCaches(false);
	// this.urlConnection.setDoOutput(true);
	// this.urlConnection.setDoInput(true);
	// this.urlConnection.setRequestProperty(CONTENT_TYPE,
	// "multipart/form-data; boundary=" + BOUNDARY);
	// this.urlConnection.setRequestProperty("User-Agent", "CodeJava Agent");
	// }
	@Override
	public void setUseCaches(boolean usecaches) {
		this.usecaches.add(usecaches);
	}

	@Override
	public void setDoInput(boolean doinput) {
		this.doinput.add(doinput);
	}

}
