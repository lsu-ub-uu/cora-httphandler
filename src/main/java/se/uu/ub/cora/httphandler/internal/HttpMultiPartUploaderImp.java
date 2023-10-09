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

package se.uu.ub.cora.httphandler.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import se.uu.ub.cora.httphandler.HttpMultiPartUploader;
import se.uu.ub.cora.httphandler.urlconnection.HttpURLConnectionHandler;

public final class HttpMultiPartUploaderImp implements HttpMultiPartUploader {

	private static final String CONTENT_DISPOSITION = "Content-Disposition";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final int INITIAL_BUFFER_SIZE = 4096;
	private HttpURLConnection urlConnection;

	private static final String BOUNDARY = "xxxYYYxxx";
	private static final String LINE_FEED = "\n";
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	private OutputStream outputStream;
	private PrintWriter writer;
	private boolean connectionSetUp = false;
	private HttpURLConnectionHandler httpURLConnectionHandler;

	private HttpMultiPartUploaderImp(HttpURLConnection httpUrlConnection) {
		this.urlConnection = httpUrlConnection;
		httpURLConnectionHandler = new HttpURLConnectionHandler(httpUrlConnection);
	}

	public static HttpMultiPartUploader usingURLConnection(HttpURLConnection httpUrlConnection) {
		return new HttpMultiPartUploaderImp(httpUrlConnection);
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
	public String getErrorText() {
		return httpURLConnectionHandler.getErrorText();
	}

	@Override
	public void addFormField(String name, String value) {
		tryToSetUpUrlConnectionAndCreateWriter();
		appendBoundaryToWriter();
		writer.append(CONTENT_DISPOSITION + ": form-data; name=\"" + name + "\"").append(LINE_FEED);
		writer.append(CONTENT_TYPE + ":" + " text/plain; charset=" + UTF_8).append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.append(value).append(LINE_FEED);
		writer.flush();
	}

	private void tryToSetUpUrlConnectionAndCreateWriter() {
		if (!connectionSetUp) {
			setUpUrlConnectionAndCreateWriter();
		}
	}

	private void setUpUrlConnectionAndCreateWriter() {
		setUpUrlConnection();
		try {
			createWriter();
		} catch (IOException e) {
			throw new RuntimeException("Failed to upload multipart", e);
		}
	}

	private void setUpUrlConnection() {
		connectionSetUp = true;
		this.urlConnection.setUseCaches(false);
		this.urlConnection.setDoOutput(true);
		this.urlConnection.setDoInput(true);
		this.urlConnection.setRequestProperty(CONTENT_TYPE,
				"multipart/form-data; boundary=" + BOUNDARY);
		this.urlConnection.setRequestProperty("User-Agent", "HttpMultipartUploader Agent");

	}

	private void createWriter() throws IOException {
		outputStream = this.urlConnection.getOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(outputStream, UTF_8), true);
	}

	private PrintWriter appendBoundaryToWriter() {
		return writer.append("--" + BOUNDARY).append(LINE_FEED);
	}

	@Override
	public void addFilePart(String fieldName, String fileName, InputStream stream)
			throws IOException {
		tryToSetUpUrlConnectionAndCreateWriter();
		addFileInfo(fieldName, fileName);
		streamData(stream);
		writer.flush();
	}

	private void addFileInfo(String fieldName, String fileName) {
		appendBoundaryToWriter();
		writer.append(CONTENT_DISPOSITION + ": form-data; name=\"" + fieldName + "\"; filename=\""
				+ fileName + "\"").append(LINE_FEED);
		writer.append(CONTENT_TYPE + ": " + URLConnection.guessContentTypeFromName(fileName))
				.append(LINE_FEED);
		writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();
	}

	private void streamData(InputStream stream) throws IOException {
		byte[] buffer = new byte[INITIAL_BUFFER_SIZE];
		int bytesRead;
		while ((bytesRead = stream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.flush();
		stream.close();
	}

	@Override
	public void addHeaderField(String name, String value) {
		throwErrorIfConnectionAlreadySetUp(name, value);
		urlConnection.setRequestProperty(name, value);
	}

	private void throwErrorIfConnectionAlreadySetUp(String name, String value) {
		if (connectionSetUp) {
			throw new RuntimeException(
					"Headers can not be set after connection is setup on multipart requests, headerName: "
							+ name + " headerValue: " + value);
		}
	}

	@Override
	public void done() throws IOException {
		writer.append(LINE_FEED).flush();
		writer.append("--" + BOUNDARY + "--").append(LINE_FEED);
		writer.close();
	}
}
