package com.blockvote.registrarapplication.crypto;

import java.math.BigInteger;

import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.digests.SHA1Digest;
import org.spongycastle.crypto.engines.RSAEngine;
import org.spongycastle.crypto.params.RSAKeyParameters;
import org.spongycastle.crypto.signers.PSSSigner;

import com.blockvote.registrarapplication.crypto.IToken;
import com.blockvote.registrarapplication.crypto.ITokenRequest;

public class Registrar implements IRegistrar{
	private final AsymmetricCipherKeyPair keys;

	public Registrar(BigInteger publicModulus, BigInteger publicExponent, BigInteger privateModulus, BigInteger privateExponent) {
		RSAKeyParameters publicKey = new RSAKeyParameters(false, publicModulus, publicExponent);
		RSAKeyParameters privateKey = new RSAKeyParameters(true, privateModulus, privateExponent);
		this.keys = new AsymmetricCipherKeyPair(publicKey, privateKey);	
	}

	public RSAKeyParameters getPublic() {
		return (RSAKeyParameters) keys.getPublic();
	}

	public byte[] sign(ITokenRequest tokenRequest) {
		// Sign the coin request using our private key.
		byte[] message = tokenRequest.getMessage();

		RSAEngine engine = new RSAEngine();
		engine.init(true, keys.getPrivate());

		return engine.processBlock(message, 0, message.length);
	}

	public boolean verify(IToken coin) {
		// Verify that the coin has a valid signature using our public key.
		byte[] id = coin.getID();
		byte[] signature = coin.getSignature();

		PSSSigner signer = new PSSSigner(new RSAEngine(), new SHA1Digest(), 20);
		signer.init(false, keys.getPublic());

		signer.update(id, 0, id.length);

		return signer.verifySignature(signature);
	}

}
