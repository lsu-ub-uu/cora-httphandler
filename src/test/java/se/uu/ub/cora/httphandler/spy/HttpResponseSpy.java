/*
 * Copyright 2023 Uppsala University Library
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
package se.uu.ub.cora.httphandler.spy;

import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import javax.net.ssl.SSLSession;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class HttpResponseSpy<T> implements HttpResponse<T> {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public HttpResponseSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("body", () -> "a stream".getBytes());
		MRV.setDefaultReturnValuesSupplier("statusCode", () -> 200);
	}

	@Override
	public int statusCode() {
		return (int) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public HttpRequest request() {
		return null;
	}

	@Override
	public Optional<HttpResponse<T>> previousResponse() {
		return Optional.empty();
	}

	@Override
	public HttpHeaders headers() {
		return (HttpHeaders) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public T body() {
		return (T) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public Optional<SSLSession> sslSession() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public URI uri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Version version() {
		// TODO Auto-generated method stub
		return null;
	}

}
