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
		MRV.setDefaultReturnValuesSupplier("body", String::new);
		MRV.setDefaultReturnValuesSupplier("statusCode", () -> 200);
	}

	@Override
	public int statusCode() {
		return (int) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public HttpRequest request() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<HttpResponse<T>> previousResponse() {
		// TODO Auto-generated method stub
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
