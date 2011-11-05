package main.java.de.WegFetZ.CustomMusic;

public class ConnectionProtocol {
	int WAITING = 0;
	int CONFIRMEDCONNECTION = 1;
	int SENTNAMEREQUEST = 2;
	int state = WAITING;

	public int processInput(int theInput) {
		int theOutput = -5;
		if (state == WAITING) {
			theOutput = 1;
			state = CONFIRMEDCONNECTION;
		} else if (state == CONFIRMEDCONNECTION) {
			if (theInput == 2) {
				theOutput = 2;
				state = SENTNAMEREQUEST;
			} else {
				theOutput = -2;
			}
		}
		return theOutput;
	}
}