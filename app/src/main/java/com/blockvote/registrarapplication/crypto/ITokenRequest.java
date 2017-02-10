package com.blockvote.registrarapplication.crypto;

public interface ITokenRequest {
	// The message (blind) to be signed by the registrar
	byte[] getMessage();
}
