package se.uu.ub.cora.httphandler.urlconnection;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

import java.io.InputStream;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HttpURLConnectionHandlerTest {

	private HttpURLConnectionSpy urlConnection;
	private HttpURLConnectionHandler connectionHandler;

	@BeforeMethod
	public void setUp() {
		urlConnection = new HttpURLConnectionSpy(null);
		urlConnection.setResponseText("some text åäö");
		connectionHandler = new HttpURLConnectionHandler(urlConnection);

	}

	@Test
	public void testInit() {
		InputStream inputStream = connectionHandler.getResponseBinary();
		assertNotNull(inputStream);
		assertSame(inputStream, urlConnection.returnedInputStream);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Error reading binary from response.")
	public void testErrorGetResponseBinary() {
		HttpURLConnectionErrorSpy errorConnection = new HttpURLConnectionErrorSpy(null);
		connectionHandler = new HttpURLConnectionHandler(errorConnection);
		connectionHandler.getResponseBinary();

	}
}
