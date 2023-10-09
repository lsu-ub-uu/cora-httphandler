package se.uu.ub.cora.httphandler.spy;

import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Optional;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class HttpRequestSpy extends HttpRequest {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public HttpRequestSpy() {
		MCR.useMRV(MRV);
		// MRV.setDefaultReturnValuesSupplier("send", HttpResponseSpy<String>::new);
	}

	@Override
	public Optional<BodyPublisher> bodyPublisher() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public String method() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Duration> timeout() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public boolean expectContinue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public URI uri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Version> version() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public HttpHeaders headers() {
		// TODO Auto-generated method stub
		return null;
	}

}
