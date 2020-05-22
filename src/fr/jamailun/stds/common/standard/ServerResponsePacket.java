package fr.jamailun.stds.common.standard;

import fr.jamailun.stds.common.JavaPacket;

import java.util.Objects;

public class ServerResponsePacket extends JavaPacket {

	public final static int ACCEPTED = 0;
	public final static int NAME_TAKEN = 50;
	public final static int SERVER_FULL = 10;

	private final int answer;
	public ServerResponsePacket(int answer) {
		super("serverResponse");
		this.answer = answer;
	}
	public final int getAnswer() {
		return answer;
	}
	public final boolean equals(int answer) {
		return this.answer == answer;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		return answer == ((ServerResponsePacket) o).answer;
	}

	@Override
	public int hashCode() {
		return Objects.hash(answer);
	}
}