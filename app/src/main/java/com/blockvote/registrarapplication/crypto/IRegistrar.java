package com.blockvote.registrarapplication.crypto;

import org.spongycastle.crypto.params.RSAKeyParameters;

import com.blockvote.registrarapplication.crypto.IToken;
import com.blockvote.registrarapplication.crypto.ITokenRequest;

public interface IRegistrar {
	// The registrar's RSA public key
	RSAKeyParameters getPublic();

	// Sign a token request
	byte[] sign(ITokenRequest tokenRequest);

	// Verify a token
	boolean verify(IToken token);

}
