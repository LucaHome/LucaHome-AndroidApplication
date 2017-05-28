package guepardoapps.library.lucahome.common.tools;

import java.io.Serializable;

import guepardoapps.library.lucahome.common.constants.Enables;

import guepardoapps.library.toolset.common.Logger;

public class LucaHomeLogger extends Logger implements Serializable {

	private static final long serialVersionUID = -8929350017604753398L;

	public LucaHomeLogger(String tag) {
		super(tag, Enables.LOGGING);
	}
}