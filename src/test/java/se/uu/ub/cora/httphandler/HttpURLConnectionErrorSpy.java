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

import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.net.URL;

public class HttpURLConnectionErrorSpy extends HttpURLConnectionSpy {

	public String requestMethod;

	public HttpURLConnectionErrorSpy(URL url) {
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
		throw new ProtocolException("this is a protocol exception");
	}

	@Override
	public void setResponseCode(int responseCode) {
	}

	@Override
	public int getResponseCode() throws IOException {
		throw new IOException("this is an ioException exception");
	}

	@Override
	public void setResponseText(String responseText) {
	}

	@Override
	public InputStream getInputStream() throws IOException {
		throw new IOException("this is an ioException exception");
	}
}
