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
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class BuilderSpy implements Builder {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public BuilderSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("method", BuilderSpy::new);
		MRV.setDefaultReturnValuesSupplier("build", HttpRequestSpy::new);

	}

	@Override
	public Builder uri(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Builder expectContinue(boolean enable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Builder version(Version version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Builder header(String name, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Builder headers(String... headers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Builder timeout(Duration duration) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Builder setHeader(String name, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Builder GET() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Builder POST(BodyPublisher bodyPublisher) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Builder PUT(BodyPublisher bodyPublisher) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Builder DELETE() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Builder method(String method, BodyPublisher bodyPublisher) {
		return (Builder) MCR.addCallAndReturnFromMRV("method", method, "bodyPublisher",
				bodyPublisher);
	}

	@Override
	public HttpRequest build() {
		return (HttpRequest) MCR.addCallAndReturnFromMRV();
	}

	@Override
	public Builder copy() {
		// TODO Auto-generated method stub
		return null;
	}

}
