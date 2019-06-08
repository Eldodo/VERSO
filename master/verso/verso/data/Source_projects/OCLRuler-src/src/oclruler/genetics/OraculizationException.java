package oclruler.genetics;


public class OraculizationException extends Exception {
	private static final long serialVersionUID = -3804489011837609881L;
	
	String message;
	public enum RATIONALE {
		NO_POSITIVE, NO_NEGATIVE, NO_EXAMPLE
	}

	RATIONALE r;

	public OraculizationException(RATIONALE r) {
		super();
		this.r = r;
	}

	public RATIONALE getRationale() {
		return r;
	}
	
	@Override
	public String getMessage() {
		switch (getRationale()) {
		case NO_POSITIVE:
		case NO_NEGATIVE:
			String s = getRationale() == RATIONALE.NO_POSITIVE ? "positive" : "negative";
			message = "Example set does not have any " + s + " example. ";
			break;
		case NO_EXAMPLE:
			message = "Example set is empty.";
			break;
		}
		return message;
	}
}
