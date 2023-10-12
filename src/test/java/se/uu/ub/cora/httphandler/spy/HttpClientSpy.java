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

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class HttpClientSpy extends HttpClient {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public HttpClientSpy() {
		MCR.useMRV(MRV);
		HttpResponseSpy<InputStream> inputStreamresponseSpy = new HttpResponseSpy<>();
		inputStreamresponseSpy.MRV.setDefaultReturnValuesSupplier("body", InputStreamSpy::new);
		MRV.setDefaultReturnValuesSupplier("send", () -> inputStreamresponseSpy);
	}

	@Override
	public Optional<CookieHandler> cookieHandler() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<Duration> connectTimeout() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Redirect followRedirects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<ProxySelector> proxy() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public SSLContext sslContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SSLParameters sslParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Authenticator> authenticator() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Version version() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Executor> executor() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public <T> HttpResponse<T> send(HttpRequest request, BodyHandler<T> responseBodyHandler)
			throws IOException, InterruptedException {
		return (HttpResponse<T>) MCR.addCallAndReturnFromMRV("request", request,
				"responseBodyHandler", responseBodyHandler);
	}

	@Override
	public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
			BodyHandler<T> responseBodyHandler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
			BodyHandler<T> responseBodyHandler, PushPromiseHandler<T> pushPromiseHandler) {
		// TODO Auto-generated method stub
		return null;
	}

}
